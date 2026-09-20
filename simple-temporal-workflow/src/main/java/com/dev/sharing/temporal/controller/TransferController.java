package com.dev.sharing.temporal.controller;

import com.dev.sharing.temporal.config.TemporalProperties;
import com.dev.sharing.temporal.dto.TransferRequest;
import com.dev.sharing.temporal.dto.TransferResponse;
import com.dev.sharing.temporal.workflow.MoneyTransferWorkflow;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST API để START workflow và POLL trạng thái.
 *
 * Đây là phía CLIENT (caller) — chỉ cần WorkflowClient. KHÔNG cần biết gì về Worker.
 * Trong thực tế, client có thể nằm ở service khác hoàn toàn với worker; cả hai chỉ
 * "gặp nhau" qua Temporal server.
 */
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

  private final WorkflowClient workflowClient;
  private final TemporalProperties properties;

  /**
   * Start workflow ở chế độ ASYNC: trả về workflowId ngay, không chờ workflow chạy xong.
   * Vào Temporal Web UI (http://localhost:8233) để theo dõi từng bước workflow chạy.
   */
  @PostMapping
  public TransferResponse start(@Valid @RequestBody TransferRequest request) {
    // workflowId là DEDUPE KEY ở phía Temporal:
    //   - Nếu start 2 lần với cùng workflowId trong cùng namespace → mặc định bị từ chối
    //     (DUPLICATE). Đây là tính năng quan trọng để tránh double-execute khi client retry.
    //   - Trong production: dùng business key (vd: "transfer-" + idempotency-key của client),
    //     KHÔNG dùng UUID random như demo.
    String workflowId = "transfer-" + UUID.randomUUID();

    // newWorkflowStub trả về proxy có TYPE đúng với workflow interface.
    // Method gọi trên stub này KHÔNG chạy code workflow ngay — chỉ là "đặt vé".
    MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        WorkflowOptions.newBuilder()
            // Task queue PHẢI khớp với task queue worker đang poll → worker mới pick được.
            .setTaskQueue(properties.getTaskQueue())
            .setWorkflowId(workflowId)
            .build());

    // WorkflowClient.start(...) là call NON-BLOCKING:
    //   - Gửi request "StartWorkflow" lên Temporal server
    //   - Server ghi event WorkflowExecutionStarted vào history rồi trả về ngay
    //   - Method này return → controller trả response cho HTTP client
    //   - Workflow code sẽ chạy ở worker, BẤT ĐỒNG BỘ với HTTP request
    //
    // Method reference `workflow::transfer` cho Temporal biết phải gọi method nào;
    // 3 tham số sau là argument truyền vào.
    WorkflowClient.start(workflow::transfer,
        request.fromAccountId(), request.toAccountId(), request.amount());

    // fromTyped chuyển stub typed → stub untyped để lấy được runId.
    // workflowId + runId định danh DUY NHẤT 1 lần execution (workflowId có thể bị reuse
    // nếu config WorkflowIdReusePolicy; runId thì luôn unique).
    return new TransferResponse(
        workflowId,
        WorkflowStub.fromTyped(workflow).getExecution().getRunId());
  }

  /**
   * Poll trạng thái + lấy kết quả nếu workflow đã COMPLETED.
   *
   * Cách production-hơn: dùng Query method (@QueryMethod trong workflow) để đọc state
   * giữa chừng mà KHÔNG cần đợi workflow xong. Demo này chỉ minh hoạ describe() và getResult().
   */
  @GetMapping("/{workflowId}")
  public Map<String, Object> status(@PathVariable String workflowId) {
    // newUntypedWorkflowStub: chỉ cần workflowId, không cần biết workflow interface gì.
    // Hữu ích khi caller không có dependency tới workflow class (vd: tool monitoring).
    WorkflowStub stub = workflowClient.newUntypedWorkflowStub(workflowId);

    // describe() = gọi DescribeWorkflowExecution lên server → lấy metadata (status, start time, …).
    // KHÔNG block — trả về ngay status hiện tại.
    WorkflowExecutionStatus status = stub.describe().getStatus();

    Map<String, Object> body = new java.util.LinkedHashMap<>();
    body.put("workflowId", workflowId);
    body.put("status", status.name());

    // Chỉ gọi getResult() khi đã COMPLETED, vì getResult() BLOCK đến khi workflow xong.
    // (Block ở đây = HTTP request bị giữ; production thường tránh kiểu này.)
    if (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED) {
      body.put("result", stub.getResult(String.class));
    }
    return body;
  }
}
