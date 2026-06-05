# Mani Factory SaaS - Backend

Resumen rápido: backend Spring Boot modular para un sistema SaaS multi-tenant (primer tenant: fábrica de maní).

Desarrollo (rápido):

1. Compilar:
```bash
./mvnw.cmd clean install -DskipTests
```

2. Ejecutar:
```bash
./mvnw.cmd spring-boot:run
```

3. Tests:
```bash
./mvnw.cmd test
```

CI/CD:
- Hay un flujo de GitHub Actions en `.github/workflows/ci.yml`.
- El pipeline ejecuta `./mvnw -q test` desde `backend/backend`.

Configuración de entorno:
- Crea `backend/.env` a partir de `backend/.env.example`.
- Las variables compatibles son:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET`
  - `JWT_EXPIRATION_MS`
- `src/main/resources/application.properties` exige `JWT_SECRET` y mantiene la configuración de release segura.

Perfiles recomendados:
- `dev`: arranque local con valores comodines para desarrollo.
- `prod`: requiere `APP_CORS_ALLOWED_ORIGINS` real y deshabilita bootstrap de usuarios.

Ejemplo para desarrollo local:
```bash
SPRING_PROFILES_ACTIVE=dev
```

Notas importantes:
- No comites `src/main/resources/application.properties` (está en `.gitignore`).
- No comites `.env`.
- Rama de desarrollo: `dev/initial-setup` (trabaja en branches por feature y abre PR hacia `main`).
- Archivo de contexto para IA/desarrolladores: `copilot-instructions.md`.

Módulo inicial implementado: `tenants` (entidad, repo, service, controller, DTOs, mapper, tests).

Endpoints principales (ver `docs/API.md` para detalles).

Base de datos y migraciones:
- El proyecto usa Flyway.
- Migraciones en `src/main/resources/db/migration`.
- Baseline actual: `V1__baseline.sql`.
- JPA queda en modo `validate` para evitar drift de esquema.

Auth (estado actual):
- `POST /api/v1/auth/login` ahora recibe solo:
  - `{ "username": "...", "password": "..." }`
- El `tenantId` y `role` del JWT se resuelven desde el usuario autenticado en base de datos.
- Se crea un usuario bootstrap autom\u00e1tico al iniciar (si no existe):
  - `username=admin`
  - `password=admin`
  - `tenantId=1`
  - `role=SUPERADMIN`
- Cambiar esa clave inmediatamente en entornos reales.
- Endpoint para cambio de password de usuario autenticado:
  - `POST /api/v1/auth/change-password`
  - Body: `{ "currentPassword": "...", "newPassword": "..." }`

Política mínima de password:
- Mínimo 8 caracteres.
- Debe incluir al menos 1 número.

Configuraci\u00f3n bootstrap auth (opcional):
- `AUTH_BOOTSTRAP_ENABLED` (default `true`)
- `AUTH_BOOTSTRAP_USERNAME` (default `admin`)
- `AUTH_BOOTSTRAP_PASSWORD` (default `admin`)
- `AUTH_BOOTSTRAP_TENANT_ID` (default `1`)
- `AUTH_BOOTSTRAP_ROLE` (default `SUPERADMIN`)

Autorizaci\u00f3n actual:
- `/api/v1/tenants/**` requiere `ROLE_SUPERADMIN`
- `/api/v1/users/**` requiere `ROLE_SUPERADMIN`

OpenAPI / Swagger:
- UI (dev): `/swagger-ui.html`
- JSON: `/v3/api-docs`
- En `prod` queda deshabilitado por seguridad.

Hardening aplicado:
- JWT inseguro por default bloqueado (`jwt.allow-insecure-dev-key=false` por defecto).
- Bootstrap con credenciales default bloqueado salvo override expl\u00edcito.
- CORS configurable por `app.cors.allowed-origins`.
- Handler JSON uniforme para `401` y `403`.
- Rate limiting configurable por `app.rate-limit.*` (incluye login).
- Errores 500 sanitizados (sin exponer detalles internos).
- Ordenes validan pertenencia tenant para cliente/producto y precio de catalogo.

Release:
- Checklist operativa en `docs/RELEASE_CHECKLIST.md`.
- Plan de rollback en `docs/ROLLBACK_PLAN.md`.
