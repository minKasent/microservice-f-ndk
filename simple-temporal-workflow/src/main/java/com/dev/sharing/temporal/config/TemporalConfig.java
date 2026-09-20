package com.dev.sharing.temporal.config;

import com.dev.sharing.temporal.activity.AccountActivity;
import com.dev.sharing.temporal.workflow.MoneyTransferWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Nơi wire Temporal Java SDK vào Spring. 4 thứ cần dựng đúng thứ tự:
 *
 *   WorkflowServiceStubs   →  gRPC connection tới Temporal server (port 7233).
 *   WorkflowClient         →  API để START / SIGNAL / QUERY workflow từ phía caller (controller).
 *   WorkerFactory          →  Factory tạo các Worker. Mỗi process chỉ cần 1.
 *   Worker                 →  Long-poll task queue, nhận task rồi thực thi workflow/activity code.
 *
 * Lưu ý: tách "client" và "worker" là chủ ý của Temporal.
 *   - Client: caller (app khác, controller…) chỉ cần WorkflowClient để start/signal.
 *   - Worker: process chứa code workflow/activity, cần WorkerFactory + Worker.
 *   - Một process có thể vừa làm client vừa làm worker (như demo này).
 */
@Slf4j
@Configuration
// Nạp TemporalProperties từ application.yml (prefix "temporal.*") vào IoC container.
@EnableConfigurationProperties(TemporalProperties.class)
// Lombok sinh constructor inject các final field bên dưới (chỉ có `properties`).
@RequiredArgsConstructor
public class TemporalConfig {

  private final TemporalProperties properties;

  // Giữ reference để @PreDestroy có thể đóng gRPC connection sạch sẽ khi app shutdown.
  // Field thường (không phải @Bean) — gán giá trị trong method @Bean bên dưới.
  private WorkflowServiceStubs serviceStubs;

  // Giữ reference để dùng lại trong @Bean Worker và @Bean WorkerLifecycle.
  // (Có thể bỏ field này nếu tin vào Spring inject; để cho tường minh chỗ shutdown.)
  private WorkerFactory workerFactory;

  /**
   * Bean #1 — kết nối gRPC tới Temporal server.
   * `target` lấy từ application.yml: temporal.target=127.0.0.1:7233.
   * Đây là kết nối DUY NHẤT tới server; mọi Workflow/Activity request đều đi qua đây.
   */
  @Bean
  public WorkflowServiceStubs workflowServiceStubs() {
    serviceStubs = WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder()
            .setTarget(properties.getTarget())
            .build());
    return serviceStubs;
  }

  /**
   * Bean #2 — client để controller GỌI workflow (start / signal / query / getResult).
   * Namespace là "không gian tên" ở server, dùng để cô lập workflow giữa các team/env.
   * `default` là namespace mặc định khi chạy `temporal server start-dev`.
   *
   * Spring sẽ tự inject bean WorkflowServiceStubs ở trên vào tham số method này.
   */
  @Bean
  public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
    return WorkflowClient.newInstance(
        serviceStubs,
        WorkflowClientOptions.newBuilder()
            .setNamespace(properties.getNamespace())
            .build());
  }

  /**
   * Bean #3 — factory để tạo các Worker. 1 process chỉ có 1 WorkerFactory.
   * Lưu ý: factory tạo ra rồi nhưng CHƯA start. Phải gọi factory.start() ở dưới
   * (trong WorkerLifecycle.start) thì worker mới bắt đầu long-poll task queue.
   */
  @Bean
  public WorkerFactory workerFactory(WorkflowClient workflowClient) {
    workerFactory = WorkerFactory.newInstance(workflowClient);
    return workerFactory;
  }

  /**
   * Bean #4 — Worker thực thụ. 1 Worker = 1 task queue.
   *
   * Quy ước: workflow và activity dùng chung 1 task queue thì worker poll cả 2 loại task.
   * Có thể tạo nhiều Worker, mỗi cái 1 task queue, để tách CPU/IO workload.
   *
   * Spring inject `accountActivity` ở đây (vì AccountActivityImpl đã đánh @Component),
   * sau đó ta đăng ký instance đó cho Temporal.
   */
  @Bean
  public Worker moneyTransferWorker(WorkerFactory workerFactory, AccountActivity accountActivity) {
    // Tạo Worker gắn vào task queue đã cấu hình trong application.yml.
    Worker worker = workerFactory.newWorker(properties.getTaskQueue());

    // Workflow phải đăng ký bằng CLASS (không phải instance). Vì sao?
    // Mỗi lần có workflow execution mới, Temporal sẽ tự `new` một instance để
    // bảo đảm workflow state sạch, tránh share state giữa các execution.
    worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);

    // Activity thì ngược lại — đăng ký INSTANCE. Vì activity là code "bình thường"
    // có side-effect (gọi DB/API), nên dùng bean Spring có sẵn để inject dependencies.
    worker.registerActivitiesImplementations(accountActivity);

    log.info("Money transfer Temporal worker registered - taskQueue: {}", properties.getTaskQueue());
    return worker;
  }

  /**
   * Bean #5 — vòng đời start/stop của WorkerFactory. Tách thành bean SmartLifecycle
   * (thay vì gọi factory.start() ngay trong @PostConstruct) để Spring tự gọi đúng lúc:
   *   - start() chạy SAU khi mọi bean khác đã sẵn sàng
   *   - stop()  chạy TRƯỚC khi shutdown các bean khác, có awaitTermination cho worker
   *             chạy nốt task đang dở (graceful shutdown).
   */
  @Bean
  public WorkerLifecycle workerLifecycle(WorkerFactory workerFactory) {
    return new WorkerLifecycle(workerFactory);
  }

  /**
   * Đóng kết nối gRPC sau cùng khi app shutdown. @PreDestroy chạy SAU
   * SmartLifecycle.stop(), nên ở đây worker đã shutdown rồi mới đóng connection.
   */
  @PreDestroy
  public void shutdown() {
    if (serviceStubs != null) {
      serviceStubs.shutdownNow();
    }
  }

  /**
   * SmartLifecycle là interface Spring dành cho component cần kiểm soát thứ tự start/stop.
   * Spring sẽ tự gọi start() khi context up xong, gọi stop() khi context shutdown.
   *
   * Vì sao không dùng @PostConstruct cho start?
   *   - @PostConstruct chạy ngay khi bean tạo xong → có thể chạy TRƯỚC khi các bean khác
   *     (controller, activity bean…) sẵn sàng → worker pick task lên mà activity chưa
   *     inject được dependency → fail. SmartLifecycle với phase = MAX_VALUE bảo đảm
   *     start CUỐI CÙNG, sau khi tất cả bean đã sẵn sàng.
   */
  @Slf4j
  @RequiredArgsConstructor
  public static class WorkerLifecycle implements SmartLifecycle {
    private final WorkerFactory workerFactory;
    // volatile vì isRunning() có thể được gọi từ thread khác (Spring lifecycle thread).
    private volatile boolean running = false;

    @Override
    public void start() {
      // factory.start() bật long-poll loop cho TẤT CẢ worker đã đăng ký với factory.
      // Từ giây này worker bắt đầu nhận task từ Temporal server.
      workerFactory.start();
      running = true;
      log.info("Temporal WorkerFactory started");
    }

    @Override
    public void stop() {
      log.info("Stopping Temporal WorkerFactory...");
      // shutdown() báo worker dừng nhận task mới (đóng long-poll).
      workerFactory.shutdown();
      // awaitTermination chờ tối đa 30s cho các task ĐANG chạy dở hoàn tất.
      // Nếu hết 30s vẫn còn task → bị bỏ dở (Temporal sẽ tự retry ở worker khác/khi restart).
      workerFactory.awaitTermination(30, TimeUnit.SECONDS);
      running = false;
    }

    @Override
    public boolean isRunning() {
      return running;
    }

    /**
     * Phase điều khiển thứ tự start/stop giữa các SmartLifecycle:
     *   - start: phase THẤP trước, CAO sau   → MAX_VALUE = start CUỐI CÙNG
     *   - stop:  phase CAO  trước, THẤP sau  → MAX_VALUE = stop  ĐẦU TIÊN
     *
     * Worker phải start cuối (sau khi bean khác ready) và stop đầu (để graceful trước
     * khi DB connection / Kafka producer của activity bị đóng).
     */
    @Override
    public int getPhase() {
      return Integer.MAX_VALUE;
    }
  }
}
