# Rollback Plan

Objetivo: volver a un estado estable rapidamente si un deploy rompe login, checkout o el dashboard.

## Antes de deploy
- Confirmar backup reciente de la base de datos.
- Guardar la version desplegada actual como referencia.
- Registrar el commit o tag de release.

## Si el deploy falla
1. Revertir la version de la aplicacion al ultimo commit/tag estable.
2. Restaurar la base de datos desde el backup mas reciente si hubo migraciones incompatibles.
3. Verificar `health`, login y public order flow.
4. Revisar logs y confirmar que no haya corrupcion de datos.

## Criterios para rollback
- Login no responde o devuelve 500.
- `/checkout` o `/api/v1/public/orders` falla de forma repetida.
- Migra una tabla de forma incompatible.
- Se rompe la compatibilidad con frontend o CORS.

## Checklist minimo
- `JWT_SECRET` definido en el entorno.
- `APP_CORS_ALLOWED_ORIGINS` definido para el frontend.
- `AUTH_BOOTSTRAP_ENABLED=false` en produccion.
- Backup probado antes del corte.
- Tag de release disponible para volver atras.
