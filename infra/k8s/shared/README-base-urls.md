# Base URLs Configuration Guide

## Tổng quan

File `configmap-base-urls.yaml` chứa cấu hình URLs cơ bản cho toàn hệ thống. Thiết kế này hỗ trợ cả môi trường **local (k3d)** và **production (Ingress)** mà không cần thay đổi code.

## Cơ chế hoạt động

### 1. ConfigMap chứa PROTOCOL + HOST

```yaml
data:
  GATEWAY_PROTOCOL: "http"
  GATEWAY_HOST: "localhost:30888"
```

### 2. Deployment compose URLs động

```yaml
env:
  - name: BASE_GATEWAY_URL
    value: "$(GATEWAY_PROTOCOL)://$(GATEWAY_HOST)"
  
  - name: API_DOCS_SERVER
    value: "$(BASE_GATEWAY_URL)$(API_DOCS_PATH)"
```

### 3. Kết quả cuối cùng

```
GATEWAY_PROTOCOL: "http"
GATEWAY_HOST: "localhost:30888"
    ↓
BASE_GATEWAY_URL = "http://localhost:30888"
    ↓
API_DOCS_PATH: "/identity"
    ↓
API_DOCS_SERVER = "http://localhost:30888/identity"
```

---

## Môi trường Local (k3d với NodePort)

### Cấu hình

```yaml
# k8s/shared/configmap-base-urls.yaml
data:
  # API Gateway
  GATEWAY_PROTOCOL: "http"
  GATEWAY_HOST: "localhost:30888"
  
  # Identity Service
  IDENTITY_PROTOCOL: "http"
  IDENTITY_HOST: "localhost:30001"
  
  # Swagger UI
  SWAGGER_UI_PROTOCOL: "http"
  SWAGGER_UI_HOST: "localhost:30080"
```

### URLs được tạo ra

- API Gateway: `http://localhost:30888`
- Identity Service: `http://localhost:30001`
- Swagger UI: `http://localhost:30080`

### Truy cập

```bash
# API Gateway
curl http://localhost:30888/identity/actuator/health

# Identity Service (direct)
curl http://localhost:30001/actuator/health

# Swagger UI
open http://localhost:30080/swagger-ui
```

---

## Môi trường Production (Ingress với domain)

### Cấu hình

```yaml
# k8s/shared/configmap-base-urls.yaml
data:
  # API Gateway
  GATEWAY_PROTOCOL: "https"
  GATEWAY_HOST: "api.devsharing.com"
  
  # Identity Service
  IDENTITY_PROTOCOL: "https"
  IDENTITY_HOST: "auth.devsharing.com"
  
  # Swagger UI
  SWAGGER_UI_PROTOCOL: "https"
  SWAGGER_UI_HOST: "docs.devsharing.com"
```

### URLs được tạo ra

- API Gateway: `https://api.devsharing.com`
- Identity Service: `https://auth.devsharing.com`
- Swagger UI: `https://docs.devsharing.com`

### Truy cập

```bash
# API Gateway
curl https://api.devsharing.com/identity/actuator/health

# Identity Service (direct)
curl https://auth.devsharing.com/actuator/health

# Swagger UI
open https://docs.devsharing.com/swagger-ui
```

---

## Chiến lược Ingress cho Production

### Option 1: Subdomain-based routing (khuyến nghị)

Mỗi service có subdomain riêng:

```yaml
# Ingress cho API Gateway
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-gateway-ingress
spec:
  rules:
    - host: api.devsharing.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api-gateway
                port:
                  number: 8080
```

```yaml
# Ingress cho Identity Service
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: identity-service-ingress
spec:
  rules:
    - host: auth.devsharing.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: identity-service
                port:
                  number: 8080
```

**ConfigMap:**
```yaml
GATEWAY_HOST: "api.devsharing.com"
IDENTITY_HOST: "auth.devsharing.com"
```

### Option 2: Path-based routing

Tất cả qua một domain với path khác nhau:

```yaml
# Ingress tổng hợp
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: devsharing-ingress
spec:
  rules:
    - host: devsharing.com
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: api-gateway
                port:
                  number: 8080
          - path: /auth
            pathType: Prefix
            backend:
              service:
                name: identity-service
                port:
                  number: 8080
```

**ConfigMap:**
```yaml
GATEWAY_HOST: "devsharing.com/api"
IDENTITY_HOST: "devsharing.com/auth"
```

---

## Chuyển đổi giữa môi trường

### Từ Local → Production

1. **Cập nhật ConfigMap:**

```bash
kubectl edit configmap base-urls-config -n devsharing
```

Thay đổi:
```yaml
# Trước (Local)
GATEWAY_PROTOCOL: "http"
GATEWAY_HOST: "192.168.128.3:30888"

# Sau (Production)
GATEWAY_PROTOCOL: "https"
GATEWAY_HOST: "api.devsharing.com"
```

2. **Restart tất cả Deployments:**

```bash
kubectl rollout restart deployment -n devsharing
```

3. **Verify:**

```bash
kubectl exec -it deployment/identity-service -n devsharing -- env | grep BASE_GATEWAY_URL
# Output: BASE_GATEWAY_URL=https://api.devsharing.com
```

### Từ Production → Local

Làm ngược lại, đổi về `http` và `192.168.128.3:30888`.

---

## Best Practices

### 1. Sử dụng Kustomize cho multi-environment

Tạo overlays cho từng môi trường:

```
k8s/
├── base/
│   └── configmap-base-urls.yaml  # Template chung
├── overlays/
    ├── local/
    │   └── configmap-base-urls-patch.yaml
    └── production/
        └── configmap-base-urls-patch.yaml
```

**local/configmap-base-urls-patch.yaml:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: base-urls-config
data:
  GATEWAY_PROTOCOL: "http"
  GATEWAY_HOST: "192.168.128.3:30888"
```

**production/configmap-base-urls-patch.yaml:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: base-urls-config
data:
  GATEWAY_PROTOCOL: "https"
  GATEWAY_HOST: "api.devsharing.com"
```

Deploy:
```bash
# Local
kubectl apply -k k8s/overlays/local

# Production
kubectl apply -k k8s/overlays/production
```

### 2. Sử dụng Helm values

Nếu dùng Helm, tạo `values.yaml`:

```yaml
# values-local.yaml
baseUrls:
  gateway:
    protocol: http
    host: "192.168.128.3:30888"

# values-production.yaml
baseUrls:
  gateway:
    protocol: https
    host: "api.devsharing.com"
```

### 3. CI/CD Pipeline

Tự động apply ConfigMap theo môi trường:

```yaml
# .github/workflows/deploy.yml
- name: Deploy to Production
  run: |
    kubectl apply -f k8s/shared/configmap-base-urls-production.yaml
    kubectl rollout restart deployment -n devsharing
```

---

## Troubleshooting

### URLs không đúng sau khi update ConfigMap

**Nguyên nhân:** Pod chưa restart để nhận config mới.

**Giải pháp:**
```bash
kubectl rollout restart deployment/identity-service -n devsharing
```

### Kiểm tra URLs đang được sử dụng

```bash
# Xem env vars trong Pod
kubectl exec -it deployment/identity-service -n devsharing -- env | grep -E "(BASE_|API_DOCS_)"

# Output:
# BASE_GATEWAY_URL=http://192.168.128.3:30888
# API_DOCS_SERVER=http://192.168.128.3:30888/identity
# API_DOCS_OAUTH2_AUTH_URL=http://192.168.128.3:30001/oauth2/authorize
```

### HTTPS không hoạt động

Đảm bảo:
1. Ingress Controller đã cài đặt
2. TLS certificate đã được tạo (cert-manager)
3. Ingress có cấu hình TLS:

```yaml
spec:
  tls:
    - hosts:
        - api.devsharing.com
      secretName: api-devsharing-tls
```

---

## Tóm tắt

| Môi trường | Protocol | Host | Use Case |
|---|---|---|---|
| **Local** | `http` | `192.168.128.3:30888` | Development, k3d với NodePort |
| **Production** | `https` | `api.devsharing.com` | Production, Ingress với domain |

**Chuyển đổi:** Chỉ cần sửa 2 giá trị (`PROTOCOL` và `HOST`) trong `configmap-base-urls.yaml`, sau đó restart Deployments.
