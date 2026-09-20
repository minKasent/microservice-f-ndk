package com.dev.sharing.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow = "kịch bản" được Temporal lưu lại theo từng bước. Khi worker crash, Temporal sẽ
 * tự "replay" event history để dựng lại state rồi tiếp tục đúng chỗ đang chạy dở.
 *
 * Quy tắc bất di bất dịch: trong workflow KHÔNG được làm I/O trực tiếp
 * (gọi DB, gọi API, Thread.sleep, lấy System.currentTimeMillis, sinh UUID…).
 * Mọi side-effect đều phải gói trong một Activity, để Temporal lưu được input/output vào
 * event history. Khi replay, Temporal đọc lại event đó thay vì gọi lại — bảo đảm
 * workflow chạy DETERMINISTIC (chạy lại ra cùng kết quả).
 */
// @WorkflowInterface đánh dấu đây là contract của workflow (giống @FeignClient của Feign).
// Bắt buộc phải có để Temporal generate proxy.
@WorkflowInterface
public interface MoneyTransferWorkflow {

  // @WorkflowMethod đánh dấu method ENTRY POINT — chỉ được có 1 method @WorkflowMethod
  // trong 1 workflow interface. Các method khác (nếu có) sẽ dùng @SignalMethod hoặc @QueryMethod.
  @WorkflowMethod
  String transfer(String fromAccountId, String toAccountId, long amount);
}
