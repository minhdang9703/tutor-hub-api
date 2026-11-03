# TutorHub

Backend REST kết nối phụ huynh với gia sư đã được xác minh, xây dựng bằng Spring Boot.

## 01. Project Overview

TutorHub giải quyết bài toán tìm, đánh giá và đặt lịch học với gia sư một cách có kiểm soát. Phụ huynh tạo yêu cầu hoặc đặt buổi học; gia sư hoàn thiện hồ sơ, ứng tuyển và quản lý lịch; quản trị viên kiểm duyệt hồ sơ, giấy tờ và nội dung cần xử lý.

Mục tiêu của hệ thống là bảo đảm phân quyền rõ ràng, không lộ thông tin liên hệ trước khi booking được xác nhận, và chống trùng lịch ở cấp database.

## 02. Features

- Đăng ký, xác minh email, JWT access/refresh token, đổi/đặt lại mật khẩu và soft-delete tài khoản.
- Hồ sơ phụ huynh, children; hồ sơ gia sư, môn dạy, availability, giấy tờ và luồng kiểm duyệt.
- Tìm kiếm gia sư; tạo yêu cầu tìm gia sư, ứng tuyển, accept/reject/withdraw và tự hết hạn yêu cầu.
- Booking một buổi hoặc định kỳ, state machine, kiểm tra availability, chống trùng lịch và cancellation window.
- Đánh giá sau buổi học hoàn tất, chat, notification, report, audit log và dashboard quản trị.

## 03. Tech Stack

| Nhóm | Công nghệ |
|---|---|
| Runtime | Java 21, Spring Boot 3.5, Maven |
| API & security | Spring Web, Validation, Spring Security, JWT HS256, springdoc OpenAPI |
| Data | PostgreSQL 16, Spring Data JPA, JdbcTemplate, Flyway |
| Object storage | MinIO (S3-compatible) |
| Email & local tooling | Spring Mail, Mailpit, Docker Compose |
| Testing | JUnit 5, MockMvc, Testcontainers PostgreSQL |

## 04. System Architecture

Ứng dụng là modular monolith: REST controller gọi service nghiệp vụ; service áp dụng role/ownership/business rule rồi dùng JPA repository hoặc `JdbcTemplate` để truy cập PostgreSQL. MinIO lưu object, Mailpit phục vụ email ở local.

```text
Client → REST API → JWT/role checks → Business services → PostgreSQL
                                            ├→ MinIO
                                            └→ SMTP/Mailpit
```

Xem chi tiết về module, luồng xác thực, tính nhất quán booking và vận hành tại [docs/architecture.md](docs/architecture.md).

## 05. Project Structure

```text
src/main/java/com/tutorhub/
├── auth, user, catalog
├── parent, tutor, request, booking, review
├── chat, notification, report, admin
└── common/                 # security, exception, config, storage, audit
src/main/resources/
├── application*.yml
└── db/migration/           # Flyway migrations
src/test/java/              # unit and integration tests
docs/                       # architecture and operational documentation
```

## 06. Getting Started

Yêu cầu: JDK 21, Maven 3.9+ và PostgreSQL 16 cài trực tiếp trên máy. Docker không cần thiết.

Tạo user và database local một lần:

```sql
CREATE ROLE tutorhub LOGIN PASSWORD 'tutorhub';
CREATE DATABASE tutorhub OWNER tutorhub;
```

Sao chép `.env.example` thành `.env`, thay các giá trị cục bộ bằng cấu hình thật (đặc biệt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` và `JWT_SECRET`). Ứng dụng tự nạp `.env` như file properties khi khởi động. Trên PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
mvn spring-boot:run
```

Flyway tự tạo schema và seed dữ liệu khi ứng dụng khởi động. Cài MinIO cục bộ khi kiểm thử upload avatar/giấy tờ; cài Mailpit hoặc cấu hình SMTP thật khi kiểm thử email. Chúng không cần chạy chỉ để khởi động API và gọi các endpoint không dùng upload/email.

- Swagger: `http://localhost:8080/swagger-ui.html`
- Mailpit: `http://localhost:8025`
- Health: `http://localhost:8080/actuator/health`

## 07. Configuration

Sao chép `.env.example`; `application.yml` tự import `.env` khi file này tồn tại. Biến môi trường của hệ điều hành vẫn có độ ưu tiên cao hơn. Không commit `.env` hoặc secret thật.

| Biến | Mục đích |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Kết nối PostgreSQL |
| `JWT_SECRET` | Secret Base64 tối thiểu 32 byte cho JWT |
| `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` | Object storage; chỉ cần khi dùng upload |
| `CORS_ALLOWED_ORIGINS` | Origins được phép gọi API |
| `BOOKING_CANCELLATION_MINUTES` | Thời gian tối thiểu trước giờ học để được hủy; mặc định 360 phút |
| `SPRING_PROFILES_ACTIVE` | Profile Spring Boot cần chạy, ví dụ `dev` |

## 08. Database Design

PostgreSQL là source of truth. Flyway quản lý schema; `V1__init_schema.sql` và `V2__seed_reference_data.sql` là bất biến, mọi thay đổi phải nằm trong migration mới.

Các aggregate chính là `users`, parent/tutor profiles, requests/applications, bookings/reviews, conversations/messages, notifications, reports và audit logs. Tiền tệ lưu `BIGINT` (VND); thời gian lưu `TIMESTAMPTZ`.

Booking sử dụng exclusion constraint `ex_bookings_no_overlap` để bảo đảm đồng thời chỉ có một booking cho một khung giờ của cùng gia sư. Sơ đồ quan hệ và chi tiết ràng buộc nằm trong [docs/architecture.md](docs/architecture.md).

## 09. API Documentation

Tất cả API nghiệp vụ có tiền tố `/api/v1`. Swagger/OpenAPI chạy tại `http://localhost:8080/swagger-ui.html` khi app được khởi động.

Các endpoint công khai gồm auth, catalog và GET tutor. Endpoint còn lại dùng header:

```http
Authorization: Bearer <access-token>
```

Role `PARENT`, `TUTOR` và `ADMIN` được kiểm tra cả ở security layer và service layer; các response không trả JPA entity trực tiếp.

## 10. Testing

```sh
mvn test
```

Unit test chạy không cần hạ tầng ngoài. Integration test dùng Testcontainers PostgreSQL để chạy Flyway với database thật và được skip khi Docker daemon không khả dụng.

## 11. Deployment

- `Dockerfile` tạo image đa giai đoạn cho ứng dụng.
- `docker-compose.yml` dành cho dependencies local.
- `docker-compose.prod.yml` là mẫu app + PostgreSQL + Caddy reverse proxy.
- GitHub Actions tại `.github/workflows/ci.yml` chạy test và build Docker image cho pull request/push vào `main`.

Xem [docs/backup.md](docs/backup.md) để backup và kiểm tra khôi phục PostgreSQL.

## 12. Roadmap & License

Ưu tiên tiếp theo là bổ sung integration test PostgreSQL cho request/application, booking recurring và 20 booking đồng thời; sau đó hoàn thiện endpoint-level OpenAPI examples, quy trình provision ADMIN riêng và giám sát production.

Repository hiện chưa có file license. Chỉ thêm license khi chủ sở hữu dự án xác định loại giấy phép phù hợp.
