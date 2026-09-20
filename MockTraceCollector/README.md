# Mock OTLP Trace Collector

Mock gRPC server để in cấu trúc trace/span mà OpenTelemetry Java Agent gửi đi (cùng payload Tempo nhận).

Default port **4319** để không đụng Tempo trên **4317**.

## Cài đặt

```powershell
cd D:\Khoa\Microservice-F\MockTraceCollector
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
```

## Chạy

```powershell
cd D:\Khoa\Microservice-F\MockTraceCollector
$env:PYTHONUTF8 = "1"
$env:PYTHONIOENCODING = "utf-8"
.\.venv\Scripts\python.exe -u mock_otlp_receiver.py
```

Port khác:

```powershell
.\.venv\Scripts\python.exe -u mock_otlp_receiver.py 4318
```

Stop: `Ctrl+C`.

Trace in ra **chính terminal này** khi Java service export OTLP.

## Cấu hình Java service

IntelliJ VM options — đổi endpoint từ Tempo (`4317`) sang mock collector (`4319`):

```text
-javaagent:D:/Khoa/Microservice-F/infra/otel/opentelemetry-javaagent.jar
-Dotel.service.name=identity-service
-Dotel.exporter.otlp.endpoint=http://localhost:4319
-Dotel.exporter.otlp.protocol=grpc
-Dotel.traces.exporter=otlp
-Dotel.metrics.exporter=none
-Dotel.logs.exporter=none
-Dotel.propagators=tracecontext,baggage
```

## Xem kết quả

Gọi API (ví dụ identity-service) rồi đọc console Python:

```powershell
curl http://localhost:8001/actuator/health
```

Console hiển thị:

- Resource attributes (`service.name`, host, process, …)
- Span: trace id, span id, parent, kind, duration, attributes, events, links, status
