package com.dev.sharing.temporal.workflow;

import com.dev.sharing.temporal.activity.AccountActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Implementation của workflow. Class NÀY (không phải instance) được đăng ký với worker
 * qua Worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class).
 *
 * Temporal sẽ tự `new` instance mỗi lần có workflow execution mới → KHÔNG được dùng
 * @Autowired / Spring DI ở đây. Mọi dependency phải lấy qua Workflow.* helper.
 */
public class MoneyTransferWorkflowImpl implements MoneyTransferWorkflow {

  /**
   * Stub này là PROXY do Temporal sinh ra — KHÔNG phải bean Spring, KHÔNG phải instance
   * AccountActivityImpl. Mỗi lời gọi account.withdraw(...) thực chất là:
   *   1. Workflow ghi event "ScheduleActivityTask" vào history
   *   2. Temporal server đẩy task vào task queue
   *   3. Một worker (có thể là worker khác process) pick lên và chạy
   *   4. Kết quả ghi lại vào history; workflow nhận về và đi tiếp
   *
   * Vì stub này được tạo lại mỗi lần workflow replay → an toàn để là field.
   */
  private final AccountActivity account = Workflow.newActivityStub(
      AccountActivity.class,
      ActivityOptions.newBuilder()
          // startToCloseTimeout = thời gian tối đa cho MỘT lần chạy activity.
          // Quá thời gian này → Temporal coi là timeout, áp dụng RetryOptions để thử lại.
          .setStartToCloseTimeout(Duration.ofSeconds(10))
          .setRetryOptions(RetryOptions.newBuilder()
              // Lần retry đầu cách thất bại đầu 1 giây.
              .setInitialInterval(Duration.ofSeconds(1))
              // Mỗi lần retry tiếp theo, khoảng chờ ×2 (1s → 2s → 4s → 8s …)
              .setBackoffCoefficient(2.0)
              // Cap khoảng chờ ở 30s, tránh chờ vô tận.
              .setMaximumInterval(Duration.ofSeconds(30))
              // Tối đa 5 lần thử (bao gồm cả lần đầu). Hết mà vẫn fail → workflow nhận
              // ActivityFailure, có thể catch để compensate.
              .setMaximumAttempts(5)
              .build())
          .build());

  /**
   * Workflow logic. Đọc như code thường, nhưng phải hiểu:
   *   - Khi crash giữa 2 dòng, restart sẽ REPLAY toàn bộ event history → đến đúng dòng
   *     đang chạy dở. Activity đã success trước crash sẽ KHÔNG bị gọi lại
   *     (Temporal đọc kết quả từ history).
   *   - Vì sẽ replay nhiều lần → tuyệt đối không dùng `new Random()`, `LocalDateTime.now()`,
   *     `UUID.randomUUID()` trực tiếp. Nếu cần → dùng Workflow.newRandom(), Workflow.currentTimeMillis(),
   *     Workflow.randomUUID().
   */
  @Override
  public String transfer(String fromAccountId, String toAccountId, long amount) {
    // 2 lời gọi bên dưới là TUẦN TỰ — withdraw xong (và đã ghi vào history) mới sang deposit.
    // Muốn chạy song song → dùng Async.function(account::withdraw, ...).
    account.withdraw(fromAccountId, amount);
    account.deposit(toAccountId, amount);

    // Giá trị return này cũng được Temporal lưu vào history dưới event WorkflowExecutionCompleted.
    // Caller gọi getResult() sẽ đọc lại từ đây.
    return "TRANSFERRED " + amount + " from " + fromAccountId + " to " + toAccountId;
  }
}
