# 🧠   Individuals API - оркестратор аутентификации

## Описание

Вам предстоит реализовать Individuals API — первый микросервис в монорепозитории, выполняющий роль оркестратора аутентификации.
Он взаимодействует с **Keycloak** через REST API и предоставляет клиентам внешние точки входа для:

* `регистрации`
* `логина`
* `обновления токена`
* `получения информации о текущем пользователе`
*
Все данные пользователей хранятся в Keycloak, а сам микросервис не содержит собственной базы данных.
Сервис должен быть полностью интегрирован в инфраструктуру мониторинга и логирования с помощью Prometheus,
Loki и Grafana, поддерживать метрики (через Spring Actuator) и логгировать события в JSON-формате.


Функциональные возможности

- POST /v1/auth/registration — регистрация нового пользователя (без авторизации)
- POST /v1/auth/login — аутентификация пользователя по email и паролю (без авторизации)
- POST /v1/auth/refresh-token — обновление access/refresh токена (без авторизации)
- GET /v1/auth/me — получение информации о текущем пользователе (требуется Bearer Token)

## Быстрый старт через Docker Compose

```bash
docker-compose up --build -d
```
## Команда для генерации DTO
```bash
./gradlew clean openApiGenerate build
```
## Команда для проверки health
```bash
curl http://localhost:8081/actuator/health

```bash
curl http://localhost:8081/actuator
```
## Keycloak
http://localhost:8080/realms/individuals-api-realm/.well-known/openid-configuration

## Grafana dashboard
* 9628 PostgreSQL Database
* 1860 Node Exporter Full
* https://grafana.com/grafana/dashboards/

## loki
* http://localhost:3100/loki/api/v1/label/job/values

## Архитектура проекта
Проект оформлен как монорепозиторий, в котором:

* каждый сервис находится в отдельной директории ("/individuals-api", "/user-service", и т.д.);
* единый "docker-compose.yaml" управляет запуском сервисов и инфраструктуры;
* логгирование и метрики централизованно настраиваются в Grafana.


```
payment-system/
├── docker-compose.yaml
├── prometheus/prometheus.yml
├── grafana/provisioning/...
├── loki/loki-config.yaml
│
├── individuals-api/
│   ├── Dockerfile
│   ├── openapi/individuals-api.yaml       # OpenAPI спецификация
│   ├── build.gradle.kts                   # Генерация DTO из YAML
│   └── src/main/java/com/example/
│       ├── controller/AuthController.java
│       ├── service/TokenService.java
│       ├── service/UserService.java
│       └── client/KeycloakClient.java
│   └── resources/
│       ├── application.yml
│       ├── logback-spring.xml
│       └── realm-config.json              # Импорт в Keycloak
└── README.md
```
## Технологии
* Java 21
* Spring Boot 3.5.0
* Spring WebFlux
* Spring Security
* Spring Boot Actuator
* Gradle (Kotlin DSL)
* Keycloak 24
* Prometheus
* Micrometer
* Logback (JSON формат)
* Loki
* OpenAPI 3.0 (в формате YAML)
* OpenAPI Generator Plugin (для Gradle)
* JUnit 5
*
## Автор
[Kovtunov Vladimir](https://github.com/WoldemarK)
[Software Engineering Telegram](https://t.me/K_Waldemar)