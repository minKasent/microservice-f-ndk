# DevSharing — Kubernetes Manifests

Thư mục này chứa toàn bộ Kubernetes manifests để deploy hệ thống **DevSharing** lên K8s (sử dụng **k3d** cho môi trường local).

---

## Cấu trúc thư mục

```
k8s/
├── namespace.yaml                    # Namespace "devsharing" — tạo trước tiên
├── shared/                           # ConfigMap dùng chung cho nhiều service
│   ├── configmap-otel.yaml           # OpenTelemetry: endpoint, protocol, exporters
│   ├── configmap-spring-common.yaml  # Spring Cloud: Eureka URI, Config Server URI, Loki URL
│   └── configmap-db-common.yaml      # Database: DB_USER, DB_PASSWORD dùng chung
├── discovery-service/                # Eureka Service Discovery
├── config-server/                    # Spring Cloud Config Server
├── api-gateway/                      # API Gateway — NodePort 30888 (expose ra ngoài)
├── identity-service/                 # Auth Service  — NodePort 30001 (expose ra ngoài)
├── notification-service/             # Kafka consumer + MongoDB
├── credit-service/                   # Quản lý tín dụng
├── content-service/                  # Quản lý nội dung khóa học
└── purchase-service/                 # Xử lý giao dịch mua bán
```

Mỗi thư mục service gồm 3 file: `configmap.yaml`, `deployment.yaml`, `service.yaml`.

---

## Kiến trúc ConfigMap (Phân tầng)

### Shared ConfigMaps

```
shared/configmap-base-urls.yaml      → PROTOCOL + HOST cho Gateway, Identity, Swagger UI
shared/configmap-otel.yaml           → OTEL endpoint, protocol, exporters
shared/configmap-spring-common.yaml  → Eureka URI, Config Server URI, Loki URL
```

### Service-specific ConfigMaps

```
<service>/configmap.yaml → OTEL_SERVICE_NAME, DB_URL, DB_USER, DB_PASSWORD, 
                            API_DOCS_PATH, OAUTH2_*_PATH, ...
```

### URL Composition trong Deployment

Mỗi Deployment compose URLs động từ các biến:

```yaml
envFrom:
  - configMapRef:
      name: <service>-config        # Service-specific config
  - configMapRef:
      name: base-urls-config        # PROTOCOL + HOST
  - configMapRef:
      name: otel-common-config      # OTEL config
  - configMapRef:
      name: spring-common-config    # Spring Cloud config

env:
  # Bước 1: Compose base URLs
  - name: BASE_GATEWAY_URL
    value: "$(GATEWAY_PROTOCOL)://$(GATEWAY_HOST)"
  
  - name: BASE_IDENTITY_URL
    value: "$(IDENTITY_PROTOCOL)://$(IDENTITY_HOST)"
  
  # Bước 2: Compose final URLs
  - name: API_DOCS_SERVER
    value: "$(BASE_GATEWAY_URL)$(API_DOCS_PATH)"
  
  - name: API_DOCS_OAUTH2_AUTH_URL
    value: "$(BASE_IDENTITY_URL)$(OAUTH2_AUTHORIZE_PATH)"
  
  - name: API_DOCS_OAUTH2_TOKEN_URL
    value: "$(BASE_IDENTITY_URL)$(OAUTH2_TOKEN_PATH)"
```

**Lợi ích:**
- **Single source of truth**: Chỉ cần sửa `GATEWAY_HOST` trong `base-urls-config` → tất cả URLs tự động update
- **Environment-agnostic**: Dễ dàng chuyển đổi giữa local (NodePort) và production (Ingress)
- **Layered composition**: URLs được build theo tầng, dễ hiểu và maintain

---

## NodePort — Truy cập từ bên ngoài

| Service            | NodePort | URL truy cập                         |
|--------------------|----------|--------------------------------------|
| `api-gateway`      | `30888`  | http://localhost:30888               |
| `identity-service` | `30001`  | http://localhost:30001               |
| `swagger-ui`       | `30080`  | http://localhost:30080/swagger-ui    |

> **Lưu ý k3d**: Khi tạo cluster k3d, cần map port NodePort ra host machine:
>
> ```bash
> k3d cluster create devsharing \
>   -p "30888:30888@loadbalancer" \
>   -p "30001:30001@loadbalancer" \
>   -p "30080:30080@loadbalancer"
> ```

---

## Swagger UI — API Documentation

Swagger UI tổng hợp tất cả API docs của các microservice vào một giao diện duy nhất.

### Truy cập

```
http://localhost:30080/swagger-ui
```

### OAuth2 Configuration

Tất cả services đều được cấu hình với **OAuth2 Authorization Code Flow + PKCE**:

```yaml
# Trong application-swagger.yaml (Config Server)
api-docs:
  oauth2:
    authorization-url: ${API_DOCS_OAUTH2_AUTH_URL}
    token-url: ${API_DOCS_OAUTH2_TOKEN_URL}
```

**Quan trọng:** Services phải chạy với profile `swagger` để load OAuth2 config:

```yaml
# service/configmap.yaml
SPRING_PROFILES_ACTIVE: "swagger"
```

Nếu dùng profile khác (như `prod`), Swagger UI sẽ hiển thị placeholder `${api-docs.oauth2.authorization-url}` thay vì URL thực tế.

### URLs được tạo ra

**Local (k3d):**
```
API_DOCS_SERVER=http://192.168.128.2:30888/identity
API_DOCS_OAUTH2_AUTH_URL=http://192.168.128.2:30001/oauth2/authorize
API_DOCS_OAUTH2_TOKEN_URL=http://192.168.128.2:30001/oauth2/token
```

**Production (Ingress):**
```
API_DOCS_SERVER=https://api.devsharing.com/identity
API_DOCS_OAUTH2_AUTH_URL=https://auth.devsharing.com/oauth2/authorize
API_DOCS_OAUTH2_TOKEN_URL=https://auth.devsharing.com/oauth2/token
```

---

## Hướng dẫn Deploy

### Bước 1 — Tạo k3d cluster

```bash
k3d cluster create devsharing \
  -p "30888:30888@loadbalancer" \
  -p "30001:30001@loadbalancer" \
  -p "30080:30080@loadbalancer"
```

### Bước 2 — Import Docker images vào k3d

Vì `imagePullPolicy: Never`, K8s không pull từ registry mà dùng image local đã build.

```bash
k3d image import devsharing-discovery:2.0.0      -c devsharing
k3d image import devsharing-config:2.0.0        -c devsharing
k3d image import devsharing-api-gateway:2.0.0    -c devsharing
k3d image import devsharing-identity:2.0.0       -c devsharing
k3d image import devsharing-notification:2.0.0   -c devsharing
k3d image import devsharing-credit:2.0.0         -c devsharing
k3d image import devsharing-content:2.0.0        -c devsharing
k3d image import devsharing-purchase:2.0.0       -c devsharing
```

### Bước 3 — Apply Namespace (bắt buộc làm TRƯỚC)

```bash
kubectl apply -f k8s/namespace.yaml
```

### Bước 4 — Apply shared ConfigMaps

```bash
kubectl apply -f k8s/shared/
```

### Bước 5 — Apply theo thứ tự khởi động

```bash
# 1. Discovery Service (Eureka) — các service khác cần đăng ký vào đây
kubectl apply -f k8s/discovery-service/

# 2. Config Server — các service khác cần fetch config từ đây khi bootstrap
kubectl apply -f k8s/config-server/

# Chờ 2 service nền tảng này Ready trước khi tiếp tục
kubectl rollout status deployment/discovery-service -n devsharing
kubectl rollout status deployment/config-server -n devsharing

# 3. API Gateway và các business service
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/identity-service/
kubectl apply -f k8s/notification-service/
kubectl apply -f k8s/credit-service/
kubectl apply -f k8s/content-service/
kubectl apply -f k8s/purchase-service/
```

---

## Kiểm tra trạng thái

```bash
# Xem tất cả Pod trong namespace devsharing
kubectl get pods -n devsharing

# Xem tất cả Service (kiểm tra NodePort)
kubectl get svc -n devsharing

# Xem logs của một service
kubectl logs -f deployment/identity-service -n devsharing

# Mô tả chi tiết một Pod (debug lỗi khởi động)
kubectl describe pod -l app=identity-service -n devsharing

# Exec vào container để debug
kubectl exec -it deployment/identity-service -n devsharing -- sh
```

---

## Cập nhật cấu hình

Khi cần thay đổi cấu hình (ví dụ: cập nhật OTEL endpoint):

```bash
# 1. Sửa file configmap tương ứng
# 2. Apply lại ConfigMap
kubectl apply -f k8s/shared/configmap-otel.yaml

# 3. Restart Deployment để Pod nhận cấu hình mới
kubectl rollout restart deployment -n devsharing
```

---

## Chuyển đổi môi trường (Local ↔ Production)

### Local (k3d với NodePort)

```yaml
# k8s/shared/configmap-base-urls.yaml
data:
  GATEWAY_PROTOCOL: "http"
  GATEWAY_HOST: "192.168.128.3:30888"
  
  IDENTITY_PROTOCOL: "http"
  IDENTITY_HOST: "192.168.128.3:30001"
  
  SWAGGER_UI_PROTOCOL: "http"
  SWAGGER_UI_HOST: "192.168.128.3:30080"
```

### Production (Ingress với domain)

```yaml
# k8s/shared/configmap-base-urls.yaml
data:
  GATEWAY_PROTOCOL: "https"
  GATEWAY_HOST: "api.devsharing.com"
  
  IDENTITY_PROTOCOL: "https"
  IDENTITY_HOST: "auth.devsharing.com"
  
  SWAGGER_UI_PROTOCOL: "https"
  SWAGGER_UI_HOST: "docs.devsharing.com"
```

### Apply changes

```bash
# 1. Sửa configmap-base-urls.yaml
# 2. Apply ConfigMap
kubectl apply -f k8s/shared/configmap-base-urls.yaml

# 3. Restart tất cả Deployments để nhận config mới
kubectl rollout restart deployment -n devsharing
```

**Chi tiết:** Xem `k8s/shared/README-base-urls.md` để biết thêm về Ingress configuration và best practices.

---

## Troubleshooting

### OAuth2 URLs hiển thị placeholder `${api-docs.oauth2.authorization-url}`

**Triệu chứng:**
```
Authorization URL: ${api-docs.oauth2.authorization-url}
Token URL: http://192.168.128.2:30001/oauth2/token
```

**Nguyên nhân:**
Service đang chạy với profile khác `swagger` (ví dụ: `prod`), không load `application-swagger.yaml` từ Config Server.

**Giải pháp:**
```yaml
# service/configmap.yaml
SPRING_PROFILES_ACTIVE: "swagger"  # ✅ Phải là "swagger"
```

Sau đó restart:
```bash
kubectl apply -f k8s/<service>/configmap.yaml
kubectl rollout restart deployment/<service> -n devsharing
```

### Kiểm tra env vars trong Pod

```bash
# Xem tất cả env vars
kubectl exec -it deployment/purchase-service -n devsharing -- env | grep -E "(BASE_|API_DOCS_)"

# Output mong đợi:
# BASE_GATEWAY_URL=http://192.168.128.2:30888
# BASE_IDENTITY_URL=http://192.168.128.2:30001
# API_DOCS_SERVER=http://192.168.128.2:30888/purchase
# API_DOCS_OAUTH2_AUTH_URL=http://192.168.128.2:30001/oauth2/authorize
# API_DOCS_OAUTH2_TOKEN_URL=http://192.168.128.2:30001/oauth2/token
```

### URLs không đúng sau khi update ConfigMap

**Nguyên nhân:** Pod chưa restart để nhận config mới.

**Giải pháp:**
```bash
kubectl rollout restart deployment/<service> -n devsharing
```

### Swagger UI health check fail (404 error)

**Triệu chứng:**
```
"/etc/nginx/html/index.html" is not found (2: No such file or directory)
```

**Nguyên nhân:** Health check probe đang gọi vào path `/` nhưng Swagger UI phục vụ nội dung tại `/swagger-ui/`.

**Giải pháp:**
```yaml
# swagger-ui/deployment.yaml
livenessProbe:
  httpGet:
    path: /swagger-ui/  # ✅ Đúng path
    port: 8080
```

---

## Lưu ý quan trọng

- **DB_USER / DB_PASSWORD** là plain text — chỉ dùng cho mục đích học tập. Production nên dùng **Kubernetes Secret**.
- **JWT_AUTO_KEYGEN: true** trong identity-service — mỗi lần restart sẽ sinh key mới, làm hỏng các JWT token cũ. Production nên mount RSA key từ Secret.
- Các service có `initialDelaySeconds` lớn (90-120s) vì Spring Boot cần thời gian fetch config từ Config Server và đăng ký Eureka.
- MySQL, MongoDB, Kafka cần được deploy riêng (không có trong manifest này). Cập nhật các URI trong ConfigMap tương ứng khi đã có địa chỉ.
- **Spring Profile:** Tất cả services phải dùng profile `swagger` để OAuth2 URLs hoạt động đúng trong Swagger UI.
