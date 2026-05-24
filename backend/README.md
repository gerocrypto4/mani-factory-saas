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
- `src/main/resources/application.properties` usa valores por defecto si no hay variables de entorno.

Notas importantes:
- No comites `src/main/resources/application.properties` (está en `.gitignore`).
- No comites `.env`.
- Rama de desarrollo: `dev/initial-setup` (trabaja en branches por feature y abre PR hacia `main`).
- Archivo de contexto para IA/desarrolladores: `copilot-instructions.md`.

Módulo inicial implementado: `tenants` (entidad, repo, service, controller, DTOs, mapper, tests).

Endpoints principales (ver `docs/API.md` para detalles).
