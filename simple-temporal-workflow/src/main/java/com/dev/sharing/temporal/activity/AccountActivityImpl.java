package com.dev.sharing.temporal.activity;

import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bản giả lập (in-memory). Trong dự án thật, đây sẽ là chỗ gọi xuống DB hoặc service tài khoản
 * (qua JPA repository, RestTemplate, OpenFeign…). Vì là @Component → có thể inject mọi
 * bean Spring khác như bình thường (UNLIKE workflow class).
 *
 * Để minh hoạ tính năng retry tự động của Temporal, withdraw được thiết kế "fail 2 lần đầu
 * rồi mới success" cho mỗi accountId. Học viên vào Temporal Web UI sẽ thấy 3 lần attempt
 * được lưu trong event history → chứng minh được retry là server-side, không phải client code.
 */
// @Component để Spring tạo bean → TemporalConfig.moneyTransferWorker(..., AccountActivity)
// inject được instance này và đăng ký với worker.
@Slf4j
@Component
public class AccountActivityImpl implements AccountActivity {

  // ConcurrentHashMap vì activity có thể chạy song song nhiều thread (worker pool).
  // AtomicInteger để increment thread-safe.
  // Lưu ý: state in-memory này MẤT khi process restart → demo only.
  private final ConcurrentHashMap<String, AtomicInteger> attemptCounter = new ConcurrentHashMap<>();

  @Override
  public void withdraw(String accountId, long amount) {
    // computeIfAbsent: nếu chưa có counter cho accountId thì tạo mới với value 0,
    // sau đó incrementAndGet trả về số attempt hiện tại (1, 2, 3, …).
    int attempt = attemptCounter
        .computeIfAbsent(accountId, k -> new AtomicInteger())
        .incrementAndGet();

    log.info("withdraw  account={} amount={} attempt={}", accountId, amount, attempt);

    // Cố tình fail 2 lần đầu để demo retry.
//    if (attempt < 3) {
//      // ApplicationFailure.newFailure(...) là cách "chuẩn" để báo lỗi từ activity.
//      // Tham số 2 ("TRANSIENT_ERROR") là error TYPE — workflow có thể catch theo type
//      // và quyết định: retry tiếp hay compensate.
//      //
//      // Vì sao không throw RuntimeException? Cũng được, nhưng:
//      //   - RuntimeException mặc định là RETRYABLE → Temporal sẽ retry theo RetryOptions
//      //   - ApplicationFailure cho phép set non-retryable nếu muốn dừng retry ngay (vd:
//      //     ApplicationFailure.newNonRetryableFailure(...) cho lỗi "tài khoản không tồn tại").
//      throw ApplicationFailure.newFailure(
//          "Tạm thời chưa rút được, thử lại sau (attempt=" + attempt + ")",
//          "TRANSIENT_ERROR");
//    }
    // Thành công ở attempt 3 → Temporal ghi ActivityTaskCompleted vào history.
    // Lần workflow replay sau (nếu có crash) sẽ KHÔNG gọi lại withdraw — đọc kết quả từ history.
  }

  @Override
  public void deposit(String accountId, long amount) {
    // Activity đơn giản, không fail. Trong thực tế cũng nên log để debug khi production gặp lỗi.
    log.info("deposit   account={} amount={}", accountId, amount);
  }
}
