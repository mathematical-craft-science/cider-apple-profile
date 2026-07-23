# Cider Analytical Platform Backend

## Локальный запуск

### Требования

- Java 25;
- Docker с Docker Compose;
- frontend CAP для полного OIDC login flow.

### Инфраструктура

Из корня проекта запустите весь локальный стек:

```bash
docker compose -f docker/compose.yml up -d
docker compose -f docker/compose.yml ps
```

Будут запущены:

| Сервис | Адрес | Назначение |
|---|---|---|
| CAP PostgreSQL | `localhost:5832` | Основная база данных CAP |
| Keycloak | `http://localhost:8180` | Локальный OIDC provider |
| Keycloak Admin Console | `http://localhost:8180/admin` | Управление realm и пользователями |
| Mailpit SMTP | `localhost:1025` | Локальная отправка писем CAP и Keycloak |
| Mailpit UI | `http://localhost:8025` | Просмотр invitation и verification писем |

Локальные credentials Keycloak Admin Console: `admin` / `admin`. Они предназначены только для `start-dev`.
Для переопределения значений скопируйте [docker/.env.example](docker/.env.example) в `docker/.env` и передайте
его через `docker compose --env-file docker/.env -f docker/compose.yml ...`.

### Backend

Профиль `local` автоматически выбирает Keycloak:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Backend доступен на `http://localhost:8088`. Swagger UI доступен без авторизации по адресу
`http://localhost:8088/swagger-ui.html`.

Чтобы временно использовать Clerk:

```bash
CAP_SECURITY_PROVIDER=CLERK ./gradlew bootRun --args='--spring.profiles.active=local'
```

### Локальная регистрация

Frontend Keycloak adapter должен использовать:

- authority: `http://localhost:8180/realms/cap`;
- client ID: `cap-frontend`;
- flow: Authorization Code с PKCE S256;
- scopes: `openid profile email`.

После регистрации откройте verification email в Mailpit на `http://localhost:8025`, подтвердите адрес и
завершите создание credentials. После первого authenticated backend request CAP создаст локальный account и default
workspace.

### Остановка и сброс

Остановить контейнеры с сохранением данных:

```bash
docker compose -f docker/compose.yml down
```

Полностью удалить локальные CAP и Keycloak данные:

```bash
docker compose -f docker/compose.yml down -v
```

Команда `down -v` необратимо удаляет локальные volumes.

Keycloak импортирует realm только при первом создании базы. Изменения `cap-realm.json` не перезаписывают существующий
realm. Для повторного bootstrap удалите локальный Keycloak volume либо примените изменение через Admin Console.

## Подготовка Keycloak к серверу

Образ из `docker/keycloak/Dockerfile` по умолчанию запускается через `start --optimized --import-realm`. На сервере:

- используйте отдельную PostgreSQL;
- задавайте DB, SMTP и bootstrap admin credentials через secret store;
- установите публичный `KC_HOSTNAME`, например `https://auth.example.com`;
- завершайте TLS на reverse proxy и задайте `KC_PROXY_HEADERS=xforwarded`;
- используйте одинаковый публичный URL в frontend authority, backend issuer и `KC_HOSTNAME`;
- не используйте `start-dev` и локальные credentials.
