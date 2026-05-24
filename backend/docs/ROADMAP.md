# Roadmap / Checklist

Estado: actualizado post-Flyway baseline.

## Alineación con README

Sí, este roadmap está alineado con la base del `README.md`:
- Backend Spring Boot modular.
- SaaS multi-tenant.
- Seguridad JWT.
- Trabajo incremental por módulos.

El README describe el estado general y setup.  
Este roadmap define los siguientes pasos para llevar esa base a nivel productivo.

## Checklist Priorizada

### 1) Migraciones DB (Flyway) [Completado]
- [x] Agregar dependencia Flyway.
- [x] Crear baseline `V1__baseline.sql` con tablas actuales:
  - tenants
  - clients
  - products
  - orders
  - order_items
  - app_users
- [x] Ajustar configuración para entornos (`dev/test/prod`).
- [x] Validar arranque con tests en verde.

### 2) Seguridad de credenciales [Pendiente]
- [x] Política mínima de password (longitud/complejidad).
- [x] Endpoint `change my password` para usuario autenticado.
- [ ] Política para usuario bootstrap (cambio obligatorio inicial o desactivación controlada).

### 3) Observabilidad minima [Completado]
- [x] Agregar Actuator (`health`, `info`).
- [x] Logging de seguridad y errores criticos con formato consistente.

### 4) Contrato API formal [En progreso]
- [x] Agregar OpenAPI/Swagger.
- [x] Mantener `docs/API.md` alineado con OpenAPI.
- [x] Incluir ejemplos reales de auth/users/roles.

### 5) Testing de integracion avanzado [En progreso]
- [x] Flujo completo login -> token -> acceso por rol.
- [x] Casos cross-tenant (bloqueos esperados).
- [ ] Cobertura de endpoints users (create/update/password) con seguridad real.


