# Microservice Platform (Dev Sharing)

Nền tảng kiến trúc Microservices cho hệ thống chia sẻ nội dung và khóa học trực tuyến (Dev Sharing), xây dựng trên nền tảng **Spring Boot 3/4**, **Spring Cloud**, **Apache Kafka**, **Temporal**, và bộ giải pháp **Full Observability (Grafana Stack + OpenTelemetry)**.

---

## 📑 Mục lục
1. [Kiến trúc tổng quan](#-kiến-trúc-tổng-quan)
2. [Danh mục dịch vụ (Service Catalog)](#-danh-mục-dịch-vụ-service-catalog)
3. [Điểm nổi bật về kiến trúc](#-điểm-nổi-bật-về-kiến-trúc)
   - [Distributed Transaction: Saga Pattern](#1-distributed-transaction-saga-pattern)
   - [Distributed Tracing & Observability](#2-distributed-tracing--observability)
   - [High Availability & Cloud-Native Deployment](#3-high-availability--cloud-native-deployment)
4. [Hạ tầng & Cổng dịch vụ](#-hạ-tầng--cổng-dịch-vụ)
5. [Hướng dẫn cài đặt & Chạy hệ thống](#-hướng-dẫn-cài-đặt--chạy-hệ-thống)
   - [Yêu cầu môi trường](#1-yêu-cầu-môi-trường)
   - [Khởi chạy hạ tầng (Docker Compose)](#2-khởi-chạy-hạ-tầng-docker-compose)
   - [Khởi chạy các Microservices](#3-khởi-chạy-các-microservices)
6. [Tài liệu API & Kiểm thử](#-tài-liệu-api--kiểm-thử)

---

## 🏛 Kiến trúc tổng quan

```
                       [ Client / Mobile App / Web / Swagger UI ]
                                          │
                                          ▼
                             ┌─────────────────────────┐
                             │   API Gateway (8080)    │
                             └────────────┬────────────┘
                                          │
     ┌───────────────────┬────────────────┼──────────────────┬──────────────────┐
     ▼                   ▼                ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐ ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Identity    │  │   Purchase   │ │   Content    │   │    Credit    │   │ Notification │
│   Service    │  │   Service    │ │   Service    │   │   Service    │   │   Service    │
│    (8001)    │  │    (8006)    │ │    (8003)    │   │    (8002)    │   │    (8004)    │
└──────┬───────┘  └──────┬───────┘ └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                 │                │                  │                  │
       │                 └───────► [ Kafka Cluster ] ◄───────┘                  │
       │                                  │ (Saga Events & Notifications)       │
       ▼                                  ▼                                     ▼
 [ MySQL / Mongo ]                  [ Database ]                          [ Mail Server ]
```

---

## 🧩 Danh mục dịch vụ (Service Catalog)

| Dịch vụ | Cổng | Mô tả chi tiết | Công nghệ cốt lõi |
| :--- | :--- | :--- | :--- |
| **`discovery-server`** | `8761` | Service Registry & Discovery | Netflix Eureka Server |
| **`config-server`** | `6969` | Centralized Configuration Server | Spring Cloud Config |
| **`api-gateway`** | `8080` | Unified API Gateway, Routing & CORS | Spring Cloud Gateway, Reactive |
| **`identity-service`** | `8001` | Auth, OAuth2 Server, User Management | Spring Security, OAuth2, JWT, JPA |
| **`credit-service`** | `8002` | Quản lý số dư ví, nạp tiền, hoa hồng | Spring Data JPA, Liquibase, SCCV |
| **`content-service`** | `8003` | Quản lý khóa học, bài học, kiểm duyệt | Spring Data JPA, MySQL, Kafka |
| **`notification-service`** | `8004` | Gửi email thông báo, lịch sử dispatch | Spring Kafka, Email Provider |
| **`purchase-service`** | `8006` | Điều phối mua hàng, thư viện người dùng | Saga Orchestrator, Kafka, JPA |
| **`mentoring-service`** | `8005` | Đặt lịch tư vấn và kết nối mentor | Spring Data JPA, Rest API |
| **`rating-service`** | `8007` | Đánh giá, xếp hạng nội dung khóa học | Spring Data JPA, Liquibase |
| **`reporting-service`** | `8008` | Tổng hợp báo cáo thống kê, phân tích | Spring Boot, Data Processing |
| **`common-lib`** | N/A | Thư viện dùng chung (Saga DTOs, Exception) | Shared Maven Library |

---

## 🚀 Điểm nổi bật về kiến trúc

### 1. Distributed Transaction: Saga Pattern
- Sử dụng mô hình **Orchestration Saga** qua **Apache Kafka**:
  1. `purchase-service` khởi tạo Saga với Semantic Lock (`status = PENDING`) nhằm ngăn chặn Double-Spending.
  2. Bắn command `VERIFY_CONTENT` sang `content-service` để kiểm tra tính hợp lệ và trạng thái bài giảng.
  3. Bắn command `DEDUCT_CREDIT` sang `credit-service` để trừ ví của người mua.
  4. Bắn command `ADD_COMMISSION` sang `credit-service` để cộng hoa hồng cho giảng viên.
  5. Bắn command `INCREMENT_PURCHASE_COUNT` để cập nhật lượt mua.
  6. Thêm khóa học vào thư viện người mua và cập nhật `PurchaseStatus = COMPLETED`.
  7. **Compensating Transactions**: Nếu bất kỳ bước nào thất bại, hệ thống tự động hoàn tiền (`REFUND_CREDIT`), hủy hoa hồng và đánh dấu Saga thất bại mà không làm mất tính toàn vẹn dữ liệu.
  8. **Idempotency**: Các participant đều lưu `idempotencyKey` trong bảng `processed_saga_commands` để đảm bảo cơ chế Exactly-Once processing trên Kafka.

### 2. Distributed Tracing & Observability
- **OpenTelemetry Java Agent**: Tự động inject trace context và baggage qua tất cả các cuộc gọi REST (OpenFeign) và Kafka messages.
- **Grafana Alloy & Tempo**: Thu thập OTLP traces (`gRPC: 4317`) tập trung, trực quan hóa Trace Waterfall trong Grafana.
- **Grafana Loki & Prometheus**: Thu thập log tập trung và metrics hệ thống (JVM, CPU, Kafka lag, HTTP request latencies).

### 3. High Availability & Cloud-Native Deployment
- Hỗ trợ triển khai linh hoạt:
  - **Local Development**: Docker Compose nhanh chóng (`infra/docker-compose.yml`).
  - **Kubernetes Ready**: Toàn bộ manifest cấu hình K8s nằm tại `infra/k8s/` với ConfigMaps, Services, và Deployments có định nghĩa `resources` (requests/limits) chuẩn production.

---

## 🛠 Hạ tầng & Cổng dịch vụ

| Thành phần | Cổng Host | Chức năng |
| :--- | :--- | :--- |
| **MySQL 8** | `3306` | Cơ sở dữ liệu quan hệ cho các dịch vụ chính |
| **MongoDB 7** | `27017` | NoSQL database |
| **Apache Kafka** | `9092`, `19092` | Event Streaming & Saga message broker |
| **Kafka UI** | `8088` | Quản lý topics, consumer groups, messages |
| **Grafana** | `3000` | Dashboard giám sát (Username/Password: `admin`/`admin`) |
| **Prometheus** | `9090` | Time-series database lưu trữ metrics |
| **Grafana Tempo** | `3110` | Trace backend trực quan hóa trace waterfall |
| **Grafana Loki** | `3100` | Log aggregation engine |
| **Grafana Alloy** | `12345`, `4317` | OpenTelemetry Collector pipeline |
| **Temporal Server & UI** | `7233`, `8233` | Workflow Engine cho các quy trình dài hạn |
| **Swagger UI** | `8089` | Trình duyệt và thử nghiệm API tập trung |

---

## 🏁 Hướng dẫn cài đặt & Chạy hệ thống

### 1. Yêu cầu môi trường
- **JDK**: Java 17 hoặc 21 (Temurin khuyến nghị).
- **Maven**: Version 3.9 trở lên.
- **Docker & Docker Compose**: Docker Desktop hoặc Docker Engine trên Linux/macOS/Windows (WSL2).

### 2. Khởi chạy hạ tầng (Docker Compose)
Chạy toàn bộ cơ sở dữ liệu, message broker và observability stack:

```bash
cd infra
docker compose up -d
```

Kiểm tra trạng thái các container đang chạy:
```bash
docker compose ps
```

### 3. Khởi chạy các Microservices
1. **Cài đặt thư viện dùng chung (`common-lib`)**:
   ```bash
   cd common-lib
   mvn clean install -DskipTests
   cd ..
   ```

2. **Khởi động các dịch vụ cốt lõi theo thứ tự**:
   - `config-server` (Port 6969)
   - `discovery-server` (Port 8761)
   - `api-gateway` (Port 8080)
   - `identity-service` (Port 8001)
   - Các business services còn lại: `credit-service`, `content-service`, `notification-service`, `purchase-service`, `mentoring-service`, `rating-service`.

---

## 📖 Tài liệu API & Kiểm thử
- **Swagger UI**: Truy cập `http://localhost:8089` để xem tài liệu API chi tiết của toàn bộ hệ thống.
- **Kafka UI**: Truy cập `http://localhost:8088` để kiểm tra các topics `purchase.saga-replies`, `purchase.content-command`, `purchase.credit-command`.
- **Grafana**: Truy cập `http://localhost:3000` để xem đồ thị traces, metrics và logs.
