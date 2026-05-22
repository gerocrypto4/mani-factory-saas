# API - Mani Factory SaaS (Backend)

## Version: v1

Base path: `/api/v1`

### Tenants

- `GET /api/v1/tenants` - Listar tenants
- `POST /api/v1/tenants` - Crear tenant
  - Body: `{ "name": "Mi Empresa", "slug": "mi-empresa", "active": true }`
- `GET /api/v1/tenants/{id}` - Obtener tenant por id
- `PUT /api/v1/tenants/{id}` - Actualizar tenant
  - Body: `{ "name": "Nombre", "active": true }`
- `DELETE /api/v1/tenants/{id}` - Eliminar tenant

Responses follow the ApiResponse wrapper (success, message, data).

Ejemplo minimal DTOs y formatos en `src/main/java/com/manifactory/backend/tenants/dto`.
