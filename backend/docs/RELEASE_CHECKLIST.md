# Release Checklist

## Seguridad
- [ ] `JWT_SECRET` configurado y no default.
- [ ] `jwt.allow-insecure-dev-key=false` en produccion.
- [ ] `AUTH_BOOTSTRAP_ENABLED=false` en produccion.
- [ ] Si bootstrap esta activo en algun entorno: no usar credenciales default.
- [ ] `APP_CORS_ALLOWED_ORIGINS` definido con dominios reales de frontend.
- [ ] Rate limit habilitado (`app.rate-limit.enabled=true`).

## Base de datos
- [ ] `spring.flyway.enabled=true`.
- [ ] Migraciones aplicadas (incluye `V2__order_items_product_fk.sql`).
- [ ] Backup reciente antes de deploy.

## API y estabilidad
- [ ] `./mvnw.cmd -q test` en verde.
- [ ] Swagger deshabilitado en produccion.
- [ ] Health endpoint responde (`/actuator/health`).
- [ ] Validar login, crear usuario, crear producto, crear orden.

## Operacion
- [ ] Variables de entorno documentadas y cargadas en el entorno.
- [ ] Logs de aplicacion y errores centralizados.
- [ ] Plan de rollback definido.
