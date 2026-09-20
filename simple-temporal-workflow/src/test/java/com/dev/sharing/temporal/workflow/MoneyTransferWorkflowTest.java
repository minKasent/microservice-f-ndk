package com.dev.sharing.temporal.workflow;

import com.dev.sharing.temporal.activity.AccountActivity;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Test workflow KHÔNG cần Temporal server thật — TestWorkflowEnvironment chạy server giả
 * trong cùng JVM, activity được mock bằng Mockito để cô lập logic workflow.
 *
 * Đây là điểm sướng nhất của Temporal so với saga tự viết: test workflow logic như test
 * unit thuần. Không cần Kafka, không cần DB, không cần stub HTTP.
 */
class MoneyTransferWorkflowTest {

  // Task queue test — tách hẳn với task queue production để tránh nhầm.
  private static final String TASK_QUEUE = "test-money-transfer";

  private TestWorkflowEnvironment env;
  private Worker worker;
  private WorkflowClient client;
  private AccountActivity activity;

  @BeforeEach
  void setUp() {
    // Tạo một Temporal server "giả" chạy in-process (gRPC nội bộ).
    // Mỗi test có env riêng → không bị ảnh hưởng state giữa các test.
    env = TestWorkflowEnvironment.newInstance();

    // Tạo worker poll task queue test.
    worker = env.newWorker(TASK_QUEUE);

    // Đăng ký workflow thật — đây là cái muốn test.
    worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);

    // Mock activity bằng Mockito → workflow gọi activity sẽ trả về null (default mock)
    // và KHÔNG chạy code AccountActivityImpl thật. Cô lập 100% logic workflow.
    activity = mock(AccountActivity.class);
    worker.registerActivitiesImplementations(activity);

    // Bật worker + lấy client để start workflow.
    env.start();
    client = env.getWorkflowClient();
  }

  @AfterEach
  void tearDown() {
    // Đóng test server, giải phóng port + thread pool. Bắt buộc tránh leak.
    env.close();
  }

  @Test
  void transfer_callsWithdrawThenDeposit_andReturnsSummary() {
    // Tạo stub giống hệt cách production làm — qua WorkflowClient + WorkflowOptions.
    MoneyTransferWorkflow workflow = client.newWorkflowStub(
        MoneyTransferWorkflow.class,
        WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());

    // Gọi method workflow trực tiếp (không qua .start) → BLOCKING, chạy đến khi xong.
    // Cách viết test này thuận lợi nhất; production code thì nên non-blocking.
    String result = workflow.transfer("ACC-1", "ACC-2", 100);

    // Assert giá trị return của workflow.
    assertThat(result).isEqualTo("TRANSFERRED 100 from ACC-1 to ACC-2");

    // Assert thứ tự gọi activity: withdraw TRƯỚC deposit (saga ordering).
    // Nếu lập trình viên tương lai đảo thứ tự → test này fail ngay → tránh bug.
    InOrder order = inOrder(activity);
    order.verify(activity).withdraw("ACC-1", 100);
    order.verify(activity).deposit("ACC-2", 100);
  }
}
