# User Journey - Dev Sharing Platform

## Tổng quan
File này mô tả chi tiết hành trình của người dùng từ khi đăng ký tài khoản, apply thành content creator, tạo content cho đến khi người dùng khác mua content.

**Base URL**: `http://localhost:8888` (API Gateway)

---

## 1. Đăng ký tài khoản (User Registration)

### 1.1. Đăng ký tài khoản mới

**Endpoint**: `POST /api/v1/users/account`

**Request Body**:
```json
{
  "username": "nguyenvana",
  "password": "password123",
  "email": "nguyenvana@example.com"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "nguyenvana",
    "email": "nguyenvana@example.com",
    "message": "Registration successful. Please check your email to verify your account."
  }
}
```

### 1.2. Xác thực email

Sau khi đăng ký, user sẽ nhận được email chứa verification code.

**Endpoint**: `GET /api/v1/users/account/verify?code={verificationCode}`

**Example**: `GET /api/v1/users/account/verify?code=abc123xyz456`

**Response**:
```json
{
  "success": true,
  "data": null,
  "message": "Email verified successfully"
}
```

---

## 2. Apply thành Content Creator

### 2.1. Đăng nhập để lấy access token

**Endpoint**: `POST /oauth2/token` (hoặc endpoint login của bạn)

**Request Body**:
```json
{
  "username": "nguyenvana",
  "password": "password123"
}
```

**Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

**Lưu ý**: Sử dụng access_token này trong header `Authorization: Bearer {access_token}` cho các request tiếp theo.

### 2.2. Submit creator application

**Endpoint**: `POST /api/v1/creator-applications`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body**:
```json
{
  "reason": "Tôi là một lập trình viên Java với 5 năm kinh nghiệm trong việc phát triển các ứng dụng Spring Boot microservices. Tôi đã từng làm việc cho nhiều công ty công nghệ lớn và tham gia vào các dự án quy mô enterprise. Tôi muốn chia sẻ kiến thức và kinh nghiệm của mình về kiến trúc microservices, design patterns, và best practices trong Java development để giúp đỡ cộng đồng developer Việt Nam. Tôi tin rằng việc chia sẻ kiến thức không chỉ giúp người khác mà còn giúp bản thân mình học hỏi và phát triển hơn.",
  "portfolioUrl": "https://github.com/nguyenvana",
  "experienceYears": 5,
  "specialization": "Java Backend Development, Spring Boot, Microservices Architecture"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "applicationId": 1,
    "userId": 1,
    "reason": "Tôi là một lập trình viên Java với 5 năm kinh nghiệm...",
    "portfolioUrl": "https://github.com/nguyenvana",
    "experienceYears": 5,
    "specialization": "Java Backend Development, Spring Boot, Microservices Architecture",
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00",
    "reviewedAt": null,
    "reviewerId": null,
    "reviewNote": null
  }
}
```

### 2.3. Kiểm tra trạng thái application

**Endpoint**: `GET /api/v1/creator-applications/my-applications`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "applicationId": 1,
        "userId": 1,
        "status": "APPROVED",
        "createdAt": "2024-01-15T10:30:00",
        "reviewedAt": "2024-01-15T14:20:00",
        "reviewerId": 100,
        "reviewNote": "Application approved. Welcome to our creator community!"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

## 3. Tạo Content (Content Creator)

### 3.1. Tạo content mới

**Endpoint**: `POST /contents`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body**:
```json
{
  "title": "Xây dựng Microservices với Spring Boot và Spring Cloud",
  "description": "Khóa học toàn diện về cách xây dựng hệ thống microservices từ đầu sử dụng Spring Boot và Spring Cloud. Bạn sẽ học cách thiết kế, phát triển, deploy và monitor một hệ thống microservices production-ready. Khóa học bao gồm: Service Discovery với Eureka, API Gateway, Config Server, Circuit Breaker với Resilience4j, Distributed Tracing, và nhiều hơn nữa.",
  "categoryId": 1,
  "level": "INTERMEDIATE",
  "price": 299000,
  "thumbnail": "https://example.com/thumbnails/spring-microservices.jpg"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "contentId": 1,
    "title": "Xây dựng Microservices với Spring Boot và Spring Cloud",
    "description": "Khóa học toàn diện về cách xây dựng hệ thống microservices...",
    "categoryId": 1,
    "categoryName": "Backend Development",
    "level": "INTERMEDIATE",
    "price": 299000,
    "thumbnail": "https://example.com/thumbnails/spring-microservices.jpg",
    "status": "DRAFT",
    "creatorId": 1,
    "creatorName": "nguyenvana",
    "viewCount": 0,
    "purchaseCount": 0,
    "createdAt": "2024-01-16T09:00:00",
    "updatedAt": "2024-01-16T09:00:00"
  }
}
```

### 3.2. Thêm nội dung cho content (Blocks)

Content được tổ chức theo cấu trúc cây với các blocks. Mỗi block có thể chứa text, code, image, video, v.v.

#### 3.2.1. Tạo Chapter 1 (PAGE block)

**Endpoint**: `POST /contents/1/blocks`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body**:
```json
{
  "parentBlockId": null,
  "type": "PAGE",
  "textContent": "Chapter 1: Giới thiệu về Microservices",
  "properties": null,
  "position": 1,
  "isFree": true
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "blockId": 1,
    "contentId": 1,
    "parentBlockId": null,
    "type": "PAGE",
    "textContent": "Chapter 1: Giới thiệu về Microservices",
    "properties": null,
    "position": 1,
    "isFree": true,
    "children": []
  }
}
```

#### 3.2.2. Thêm heading cho Chapter 1

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 1,
  "type": "HEADING_1",
  "textContent": "Microservices là gì?",
  "properties": null,
  "position": 1,
  "isFree": true
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "blockId": 2,
    "contentId": 1,
    "parentBlockId": 1,
    "type": "HEADING_1",
    "textContent": "Microservices là gì?",
    "properties": null,
    "position": 1,
    "isFree": true,
    "children": []
  }
}
```

#### 3.2.3. Thêm paragraph giải thích

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 1,
  "type": "PARAGRAPH",
  "textContent": "Microservices là một kiến trúc phần mềm trong đó ứng dụng được chia thành nhiều service nhỏ, độc lập. Mỗi service chạy trong process riêng của nó và giao tiếp với nhau thông qua các cơ chế nhẹ như HTTP REST API hoặc message queue. Kiến trúc này cho phép các team phát triển, deploy và scale các service một cách độc lập.",
  "properties": null,
  "position": 2,
  "isFree": true
}
```

#### 3.2.4. Thêm callout (highlight box)

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 1,
  "type": "CALLOUT",
  "textContent": "💡 Lưu ý: Microservices không phải là giải pháp cho mọi vấn đề. Với các ứng dụng nhỏ, monolithic architecture có thể là lựa chọn tốt hơn.",
  "properties": "{\"icon\": \"💡\", \"color\": \"blue\"}",
  "position": 3,
  "isFree": true
}
```

#### 3.2.5. Thêm heading 2

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 1,
  "type": "HEADING_2",
  "textContent": "Ưu điểm của Microservices",
  "properties": null,
  "position": 4,
  "isFree": true
}
```

#### 3.2.6. Thêm bullet list

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 1,
  "type": "BULLET_LIST",
  "textContent": "• Độc lập trong việc deploy và scale\n• Dễ dàng maintain và test\n• Công nghệ đa dạng - mỗi service có thể dùng tech stack khác nhau\n• Fault isolation - lỗi ở một service không ảnh hưởng toàn bộ hệ thống\n• Team autonomy - các team có thể làm việc độc lập",
  "properties": null,
  "position": 5,
  "isFree": true
}
```

#### 3.2.7. Tạo Chapter 2 (Nội dung trả phí)

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": null,
  "type": "PAGE",
  "textContent": "Chapter 2: Thiết lập Spring Boot Microservices",
  "properties": null,
  "position": 2,
  "isFree": false
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "blockId": 7,
    "contentId": 1,
    "parentBlockId": null,
    "type": "PAGE",
    "textContent": "Chapter 2: Thiết lập Spring Boot Microservices",
    "properties": null,
    "position": 2,
    "isFree": false,
    "children": []
  }
}
```

#### 3.2.8. Thêm heading cho Chapter 2

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "HEADING_1",
  "textContent": "Tạo Service đầu tiên với Spring Boot",
  "properties": null,
  "position": 1,
  "isFree": false
}
```

#### 3.2.9. Thêm paragraph hướng dẫn

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "PARAGRAPH",
  "textContent": "Chúng ta sẽ bắt đầu bằng việc tạo một User Service đơn giản. Service này sẽ quản lý thông tin người dùng và expose REST API để các service khác có thể sử dụng.",
  "properties": null,
  "position": 2,
  "isFree": false
}
```

#### 3.2.10. Thêm code block với Maven dependencies

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "CODE",
  "textContent": "<dependencies>\n    <dependency>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-web</artifactId>\n    </dependency>\n    <dependency>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-data-jpa</artifactId>\n    </dependency>\n    <dependency>\n        <groupId>org.springframework.cloud</groupId>\n        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>\n    </dependency>\n    <dependency>\n        <groupId>com.h2database</groupId>\n        <artifactId>h2</artifactId>\n        <scope>runtime</scope>\n    </dependency>\n</dependencies>",
  "properties": "{\"language\": \"xml\", \"fileName\": \"pom.xml\"}",
  "position": 3,
  "isFree": false
}
```

#### 3.2.11. Thêm heading cho phần Entity

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "HEADING_2",
  "textContent": "Tạo User Entity",
  "properties": null,
  "position": 4,
  "isFree": false
}
```

#### 3.2.12. Thêm code block cho User Entity

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "CODE",
  "textContent": "@Entity\n@Table(name = \"users\")\n@Data\n@NoArgsConstructor\n@AllArgsConstructor\npublic class User {\n    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n    private Long id;\n    \n    @Column(nullable = false, unique = true)\n    private String username;\n    \n    @Column(nullable = false)\n    private String email;\n    \n    @Column(nullable = false)\n    private String password;\n    \n    @Enumerated(EnumType.STRING)\n    private UserRole role;\n    \n    private LocalDateTime createdAt;\n    private LocalDateTime updatedAt;\n}",
  "properties": "{\"language\": \"java\", \"fileName\": \"User.java\"}",
  "position": 5,
  "isFree": false
}
```

#### 3.2.13. Thêm heading cho Repository

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "HEADING_2",
  "textContent": "Tạo User Repository",
  "properties": null,
  "position": 6,
  "isFree": false
}
```

#### 3.2.14. Thêm code block cho Repository

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 7,
  "type": "CODE",
  "textContent": "@Repository\npublic interface UserRepository extends JpaRepository<User, Long> {\n    Optional<User> findByUsername(String username);\n    Optional<User> findByEmail(String email);\n    boolean existsByUsername(String username);\n    boolean existsByEmail(String email);\n}",
  "properties": "{\"language\": \"java\", \"fileName\": \"UserRepository.java\"}",
  "position": 7,
  "isFree": false
}
```

#### 3.2.15. Thêm Chapter 3

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": null,
  "type": "PAGE",
  "textContent": "Chapter 3: Service Discovery với Eureka",
  "properties": null,
  "position": 3,
  "isFree": false
}
```

#### 3.2.16. Thêm nội dung cho Chapter 3

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 15,
  "type": "HEADING_1",
  "textContent": "Thiết lập Eureka Server",
  "properties": null,
  "position": 1,
  "isFree": false
}
```

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 15,
  "type": "PARAGRAPH",
  "textContent": "Eureka là một service registry của Netflix OSS, được tích hợp vào Spring Cloud. Nó cho phép các microservices tự động đăng ký và khám phá nhau mà không cần hardcode địa chỉ IP hoặc hostname.",
  "properties": null,
  "position": 2,
  "isFree": false
}
```

#### 3.2.17. Thêm image minh họa

**Endpoint**: `POST /contents/1/blocks`

**Request Body**:
```json
{
  "parentBlockId": 15,
  "type": "IMAGE",
  "textContent": "Eureka Architecture Diagram",
  "properties": "{\"url\": \"https://example.com/images/eureka-architecture.png\", \"alt\": \"Eureka Architecture\", \"width\": 800}",
  "position": 3,
  "isFree": false
}
```

### 3.3. Xem preview content với blocks

**Endpoint**: `GET /contents/1/blocks`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response**: Trả về cấu trúc cây của tất cả blocks

### 3.4. Submit content để review

**Endpoint**: `POST /contents/1/submit-review`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response**:
```json
{
  "success": true,
  "data": {
    "contentId": 1,
    "title": "Xây dựng Microservices với Spring Boot và Spring Cloud",
    "status": "PENDING_REVIEW",
    "updatedAt": "2024-01-16T15:30:00"
  }
}
```

### 3.5. Publish content (sau khi được approve)

**Endpoint**: `POST /contents/1/publish`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response**:
```json
{
  "success": true,
  "data": {
    "contentId": 1,
    "title": "Xây dựng Microservices với Spring Boot và Spring Cloud",
    "status": "PUBLISHED",
    "publishedAt": "2024-01-17T10:00:00"
  }
}
```

---

## 4. User khác mua Content (Purchase Flow)

### 4.1. User khác đăng ký và đăng nhập

Giả sử có user mới tên "tranthib" đã đăng ký và đăng nhập thành công, nhận được access token.

### 4.2. Tìm kiếm content

**Endpoint**: `POST /contents/published/search`

**Request Body**:
```json
{
  "keyword": "microservices",
  "categoryId": null,
  "level": null,
  "minPrice": null,
  "maxPrice": null,
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "contentId": 1,
        "title": "Xây dựng Microservices với Spring Boot và Spring Cloud",
        "description": "Khóa học toàn diện về cách xây dựng hệ thống microservices...",
        "thumbnail": "https://example.com/thumbnails/spring-microservices.jpg",
        "price": 299000,
        "level": "INTERMEDIATE",
        "categoryName": "Backend Development",
        "creatorName": "nguyenvana",
        "viewCount": 150,
        "purchaseCount": 12,
        "averageRating": 4.8,
        "createdAt": "2024-01-16T09:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### 4.3. Xem chi tiết content

**Endpoint**: `GET /contents/1`

**Headers**:
```
Authorization: Bearer {tranthib_access_token}
```

**Response**: Trả về thông tin chi tiết content

### 4.4. Xem preview (free blocks)

**Endpoint**: `GET /contents/1/blocks/free`

**Response**: Trả về các blocks có `isFree: true` (Chapter 1 trong ví dụ này)

### 4.5. Mua content

**Endpoint**: `POST /api/v1/purchases`

**Headers**:
```
Authorization: Bearer {tranthib_access_token}
```

**Request Body**:
```json
{
  "contentId": 1
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "purchaseId": 1,
    "contentId": 1,
    "contentTitle": "Xây dựng Microservices với Spring Boot và Spring Cloud",
    "buyerId": 2,
    "buyerName": "tranthib",
    "creatorId": 1,
    "creatorName": "nguyenvana",
    "price": 299000,
    "status": "COMPLETED",
    "purchasedAt": "2024-01-18T14:30:00",
    "transactions": [
      {
        "transactionId": 1,
        "type": "DEBIT",
        "amount": 299000,
        "description": "Purchase content: Xây dựng Microservices với Spring Boot và Spring Cloud"
      }
    ]
  }
}
```

### 4.6. Truy cập toàn bộ content sau khi mua

**Endpoint**: `GET /contents/1/blocks`

**Headers**:
```
Authorization: Bearer {tranthib_access_token}
```

**Response**: Trả về TẤT CẢ blocks (bao gồm cả free và premium) vì user đã mua content

### 4.7. Xem danh sách content đã mua

**Endpoint**: `GET /api/v1/purchases/my-purchases?page=0&size=10`

**Headers**:
```
Authorization: Bearer {tranthib_access_token}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "purchaseId": 1,
        "contentId": 1,
        "contentTitle": "Xây dựng Microservices với Spring Boot và Spring Cloud",
        "contentThumbnail": "https://example.com/thumbnails/spring-microservices.jpg",
        "creatorName": "nguyenvana",
        "price": 299000,
        "status": "COMPLETED",
        "purchasedAt": "2024-01-18T14:30:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

---

## 5. Bonus: Rating và Review (Optional)

### 5.1. Đánh giá content sau khi mua

**Endpoint**: `POST /api/v1/ratings`

**Headers**:
```
Authorization: Bearer {tranthib_access_token}
```

**Request Body**:
```json
{
  "contentId": 1,
  "rating": 5,
  "review": "Khóa học rất chi tiết và dễ hiểu. Code examples rất thực tế và có thể áp dụng ngay vào dự án. Highly recommended!"
}
```

---

## Tổng kết Flow

1. **User Registration** → Verify Email
2. **Login** → Get Access Token
3. **Apply Creator** → Wait for Approval
4. **Create Content** → Add Blocks (Chapters, Text, Code, Images)
5. **Submit for Review** → Publish Content
6. **Other Users** → Search → View Preview → Purchase → Access Full Content
7. **Optional**: Rate & Review

## Lưu ý quan trọng

- Tất cả các endpoint (trừ registration, verify, login, và public search) đều yêu cầu authentication token
- Content được tổ chức theo cấu trúc cây với blocks, cho phép tạo nội dung phong phú
- Có thể set một số blocks là `isFree: true` để làm preview
- Sau khi mua, user có thể truy cập toàn bộ nội dung
- Price được tính bằng VNĐ (đơn vị nhỏ nhất)
