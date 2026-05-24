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

Successful responses return resource DTOs directly (or lists of DTOs).
Errors return a standardized `ApiError` payload from `GlobalExceptionHandler`.

Ejemplo minimal DTOs y formatos en `src/main/java/com/manifactory/backend/tenants/dto`.

### Clients

- `GET /api/v1/clients?tenantId={tenantId}` - Listar clientes de un tenant
- `POST /api/v1/clients?tenantId={tenantId}` - Crear cliente
  - Body: `{ "name": "Nombre", "businessName": "Negocio", "email": "x@x.com", "phone": "123", "city": "Ciudad", "preferredTransport": "Transporte" }`
- `GET /api/v1/clients/{id}?tenantId={tenantId}` - Obtener cliente por id
- `PUT /api/v1/clients/{id}?tenantId={tenantId}` - Actualizar cliente
  - Body: `{ "name": "Nombre", "businessName": "Negocio", "email": "x@x.com" }`
- `DELETE /api/v1/clients/{id}?tenantId={tenantId}` - Eliminar cliente

Ejemplo DTOs en `src/main/java/com/manifactory/backend/clients/dto`.

### Products

- `GET /api/v1/products?tenantId={tenantId}` - Listar productos de un tenant
- `POST /api/v1/products` - Crear producto
  - Body: `{ "tenantId": 1, "name": "Producto", "description": "...", "price": 9.99 }`
- `GET /api/v1/products/{id}?tenantId={tenantId}` - Obtener producto por id
- `PUT /api/v1/products/{id}?tenantId={tenantId}` - Actualizar producto
- `DELETE /api/v1/products/{id}?tenantId={tenantId}` - Eliminar producto

### Orders

- `GET /api/v1/orders?tenantId={tenantId}` - Listar órdenes del tenant
- `POST /api/v1/orders` - Crear orden
  - Body: `{ "tenantId": 1, "clientId": 1, "items": [{ "productId": 2, "quantity": 3, "unitPrice": 10.50 }] }`
- `GET /api/v1/orders/{id}?tenantId={tenantId}` - Obtener orden por id
- `PUT /api/v1/orders/{id}/status?tenantId={tenantId}` - Actualizar estado de orden
  - Body: `{ "status": "CONFIRMED" }`
- `DELETE /api/v1/orders/{id}?tenantId={tenantId}` - Eliminar orden
