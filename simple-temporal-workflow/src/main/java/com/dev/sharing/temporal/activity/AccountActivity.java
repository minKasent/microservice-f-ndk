package com.dev.sharing.temporal.activity;

import io.temporal.activity.ActivityInterface;

/**
 * Activity = đơn vị công việc có side-effect (gọi DB, gọi API ngoài, gửi mail, in log…).
 * Temporal lưu input + output của từng activity vào event history, nhờ vậy:
 *   - Workflow replay sẽ KHÔNG gọi lại activity đã success → side-effect chỉ chạy 1 lần
 *   - Activity fail sẽ tự retry theo RetryOptions khai báo bên workflow
 *
 * Phân chia trách nhiệm:
 *   - Workflow: chỉ "điều phối" (gọi activity nào, theo thứ tự nào, retry/compensate ra sao)
 *   - Activity: làm việc thật, được phép I/O, được phép non-deterministic
 */
// @ActivityInterface đánh dấu interface để Temporal generate proxy.
// Tất cả method public trong interface đều mặc định là activity method
// → không cần thêm @ActivityMethod (chỉ thêm khi muốn đổi tên activity).
@ActivityInterface
public interface AccountActivity {

  void withdraw(String accountId, long amount);

  void deposit(String accountId, long amount);
}
