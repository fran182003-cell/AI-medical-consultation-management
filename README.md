# Medical Consultation Management System with Encryption and AI Assistance

Bachelor's thesis (TFG) in Telematics Engineering, University of Jaén. A web application for managing medical consultations: appointment schedule, per-patient consultation history, an admin panel for managing doctors, and AI-generated responses from an external model.

## Features

- **JWT authentication** with role-based access control (`ADMIN` / `USER`) using Spring Security.
- **Encryption of clinical data at rest**: diagnosis, treatment, anamnesis and the AI response are encrypted with AES-256-GCM through Google Tink before being stored in the database.
- **Input validation and security logging**: validation failures are recorded with timestamp and IP address in `log_seguridad.txt`.
- **AI integration**: the backend forwards the doctor's prompt to an external service (e.g. a Colab notebook exposed through ngrok).
- **React frontend**, included as a production build in `src/main/resources/static`.

## Tech stack

Java 17 · Spring Boot 4 (Web MVC, Security, Data JPA, Validation) · H2 · jjwt · Google Tink · React

## Getting started

### 1. Requirements

- JDK 17 or later
- Maven is not required: the project ships with the Maven wrapper (`mvnw`)

### 2. Generate the keys

Keys are **not included in the repository**. By default the application looks for them in the `claves/` folder (ignored by git):

| File | Purpose |
|---|---|
| `claves/tink_keyset.json` | AES-256-GCM master key used to encrypt clinical data |
| `claves/jwt_secret.key` | HS256 key used to sign JWT tokens |

To generate them, uncomment and run the `GeneradorClaves` and `Generador_clave_token_firma` classes once (in that order; the first one creates the `claves/` folder), then comment them out again.

The JWT key can also be generated with OpenSSL:

```bash
mkdir -p claves
openssl rand -out claves/jwt_secret.key 32
```

### 3. Configuration

The values in `application.properties` can be overridden with environment variables:

| Variable | Description | Default |
|---|---|---|
| `RUTA_CLAVE_MAESTRA` | Path to the Tink keyset | `./claves/tink_keyset.json` |
| `RUTA_CLAVE_JWT` | Path to the JWT key | `./claves/jwt_secret.key` |
| `IA_URL` | AI service endpoint | `https://xxxx.ngrok-free.dev/ia/generar-respuesta` |

While `IA_URL` still contains `xxxx`, the application starts normally but response generation returns a configuration warning.

### 4. Run

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

The application is then available at <http://localhost:8081>.

## Demo data and credentials

On startup the application loads four fictitious patients and their appointments for the current day.

| Username | Password | Role |
|---|---|---|
| `admin` | `1234` | Administrator |

The local H2 database uses `sa` / `123`, and its console is available at `/h2-console`.

> **Warning:** these credentials, the open H2 console and the preloaded data exist only to make the thesis demo easier. They must not be used in a real deployment.

## Project structure

```
src/main/java/com/example/demo/
├── AuthController.java            Login and token issuance
├── ConsultasController.java       API for appointments, patients, consultations, doctors and AI
├── SecurityConfig.java            Access rules and CORS
├── JwtUtils.java / JwtAuthenticationFilter.java
├── TinkConfig.java / AttributeEncryptor.java   Attribute encryption
├── GlobalExceptionHandler.java    Validation and security log
├── IAService.java                 AI service client
├── DataLoader.java                Demo data
└── dto/                           Request and response objects
src/main/resources/
├── application.properties
└── static/                        Compiled React frontend
```

## Notes

The source code, comments and user interface are written in Spanish.

## Author

Francisco Sánchez Orozco
