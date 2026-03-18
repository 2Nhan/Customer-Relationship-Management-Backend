# 🚀 CRM Project API

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8+-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

A production-oriented CRM backend for managing users, leads, quotations, orders, and customer analytics with secure JWT
authentication.
> 📖 **For detailed project analysis, system architecture, workflows, and usage guides, please refer to the [Project Documentation](https://docs.google.com/document/d/1y0Q0doghAPiCr0dGmXIamyfkWEguaOmY8_CGu8jVnyA/edit?usp=sharing)**

---

## 📋 Table of Contents

- [🎯 Key Features](#-key-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Architecture Highlights](#-architecture-highlights)
- [🚀 Getting Started](#-getting-started)
- [📚 API Documentation](#-api-documentation)
- [🗄️ Database Schema](#️-database-schema)
- [🐳 Deployment](#-deployment)
- [📄 License](#-license)

---

## 🎯 Key Features

- ✅ Secure stateless authentication using JWT access/refresh tokens, Spring Security, and role-based authorization (
  RBAC).
- ✅ Lead pipeline management with customizable stages, lead assignment, activity tracking, and lead conversion
  workflows.
- ✅ End-to-end sales flow from quotation creation to order generation and status tracking.
- ✅ CSV import for leads and products with validation and invalid-row feedback, support small businesses to migrate old
  data to the system.
- ✅ Automatic quotation PDF generation from business data for consistent, ready-to-send documents.
- ✅ Media/document handling via Cloudinary (images + generated quotation PDFs).
- ✅ Email quotation delivery via Resend with PDF attachments.
- ✅ Dashboard/reporting endpoints for revenue, lead conversion, and task completion trends.
- ✅ Scheduled jobs that automatically mark expired activities/quotations.

## 🛠️ Tech Stack

### 📝 Language

- Java 21

### 🎯 Frameworks & Libraries

- 📦 Spring Boot 3.2.5
- 📦 Spring Web (REST API)
- 📦 Spring Validation
- 📦 Spring Data JPA (Hibernate)
- 📦 Spring Security
- 📦 Spring Scheduling
- 📦 Springdoc OpenAPI / Swagger UI
- 📦 Lombok
- 📦 MapStruct
- 📦 Apache Commons CSV
- 📦 OpenPDF (LibrePDF)
- 📦 Nimbus JOSE JWT

### 💾 Database & Storage

- 🗄️ MySQL (primary relational database)
- ☁️ Cloudinary (asset/document hosting)

### 🚀 Infrastructure & Deployment

- 🐳 Docker multi-stage build (Maven build stage + Temurin JRE runtime)
- ⚙️ Configurable runtime via environment variables (`application.yml`)

## 🏗️ Architecture Highlights

- 🎯 Layered architecture (`controller -> service -> repository -> entity`) for clear separation of concerns.
- 📨 Uniform response contract via `MyApiResponse` to keep API output predictable for frontend clients.
- 🔐 Method-level authorization (`@PreAuthorize`) for sensitive administrative operations.
- 🔑 Custom JWT decoder to validate access tokens and enforce stateless security flow.
- 📊 Rich JPA querying with `JOIN FETCH` and two-step pagination for heavy aggregate views.

### ⚡ Performance Optimizations

- 🚀 **`Deferred Join`** in order/quotation flows: query only IDs first, then fetch details by ID list to keep paging
  stable
  and reduce heavy joins during count/page slicing.
- ⚡ **`JOIN FETCH` relation loading** in repositories to reduce N+1 query behavior on detail/list endpoints.

## 🚀 Getting Started

### ✅ Prerequisites

- ☕ Java 21
- 🗄️ MySQL 8+
- 🐳 Docker (optional, for containerized run)
- 📦 (Optional) Cloudinary account + Resend API key for file/email features

### 📥 Installation

```powershell
# from project root
.\mvnw.cmd clean install
```

### ⚙️ Environment Variables

Create a `.env` file in the project root (loaded by `spring-dotenv`) and configure values like below:

```env
PORT=8088

DB_HOST=your_db_host
DB_PORT=your_db_port
DB_NAME=your_db_name
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

SECRET_KEY=your_access_token_secret
REFRESH_KEY=your_refresh_token_secret
ACCESS_TOKEN_DURATION=???
REFRESH_TOKEN_DURATION=???

CLOUDINARY_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

MAIL_API_KEY=your_resend_api_key
MAIL_SENDER=no-reply@your-domain.com
```

### ▶️ Run Locally

```powershell
.\mvnw.cmd spring-boot:run
```

Application base URL:

- `http://localhost:8088/crm`

## 📚 API Documentation

- 📘 Swagger UI: `http://localhost:8088/crm/swagger-ui.html`
- 📄 OpenAPI JSON: `http://localhost:8088/crm/api-docs`
- 📋 OpenAPI YAML: `http://localhost:8088/crm/openapi.yml`

## 🗄️ Database Schema

<img width="975" height="1198" alt="image" src="https://github.com/user-attachments/assets/3ea33759-f1c7-4cc0-985a-402ba22487cd" />


## 🐳 Deployment

### 🐳 Docker (Recommended)

```powershell
docker build -t crm-project-api .
docker run -d --name crm-project-api -p 8088:8088 --env-file .env crm-project-api
```

Notes:

- Container exposes port `8088`.
- API context path is `/crm`.
- Ensure MySQL is reachable from the container environment.

## 📄 License

- OpenAPI metadata declares **Apache 2.0**.
- Consider adding a root `LICENSE` file to make licensing explicit in the repository.

