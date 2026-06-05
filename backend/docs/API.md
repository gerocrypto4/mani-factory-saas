# API - Mani Factory SaaS (Backend)

## Version: v1

Base path: `/api/v1`

OpenAPI:
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

### Public (sin login)

- `GET /api/v1/public/catalog/products?tenantId={tenantId}` - Catalogo publico (solo productos activos)
- `POST /api/v1/public/orders?tenantId={tenantId}` - Crear pedido publico
  - Body:
    - `name`
    - `businessName` (opcional)
    - `phone` (opcional)
    - `city` (opcional)
    - `preferredTransport` (opcional)
    - `items`: `[{ "productId": 2, "quantity": 3 }]`

### Tenants

- `GET /api/v1/tenants` - Listar tenants
- `POST /api/v1/tenants` - Crear tenant
  - Body: `{ "name": "Mi Empresa", "slug": "mi-empresa", "active": true }`
- `GET /api/v1/tenants/{id}` - Obtener tenant por id
- `PUT /api/v1/tenants/{id}` - Actualizar tenant
  - Body: `{ "name": "Nombre", "active": true }`
- `DELETE /api/v1/tenants/{id}` - Eliminar tenant
- Requiere rol `SUPERADMIN`

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
- Si el usuario no es `SUPERADMIN`, `tenantId` debe coincidir con el tenant del JWT

Ejemplo DTOs en `src/main/java/com/manifactory/backend/clients/dto`.

### Products

- `GET /api/v1/products?tenantId={tenantId}` - Listar productos de un tenant
- `POST /api/v1/products` - Crear producto
  - Body: `{ "tenantId": 1, "name": "Producto", "description": "...", "price": 9.99 }`
- `GET /api/v1/products/{id}?tenantId={tenantId}` - Obtener producto por id
- `PUT /api/v1/products/{id}?tenantId={tenantId}` - Actualizar producto
- `DELETE /api/v1/products/{id}?tenantId={tenantId}` - Eliminar producto
- Si el usuario no es `SUPERADMIN`, `tenantId` debe coincidir con el tenant del JWT

### Orders

- `GET /api/v1/orders?tenantId={tenantId}` - Listar órdenes del tenant
- `POST /api/v1/orders` - Crear orden
  - Body: `{ "tenantId": 1, "clientId": 1, "items": [{ "productId": 2, "quantity": 3 }] }`
  - `unitPrice` se toma del catalogo del producto en backend (no del cliente)
- `GET /api/v1/orders/{id}?tenantId={tenantId}` - Obtener orden por id
- `PUT /api/v1/orders/{id}/status?tenantId={tenantId}` - Actualizar estado de orden
  - Body: `{ "status": "CONFIRMED" }`
- `DELETE /api/v1/orders/{id}?tenantId={tenantId}` - Eliminar orden
- Si el usuario no es `SUPERADMIN`, `tenantId` debe coincidir con el tenant del JWT

### Finances

- `POST /api/v1/finances?tenantId={tenantId}` - Crear entrada de ingreso o egreso
  - Requiere JWT. `SUPERADMIN` puede pasar cualquier `tenantId`; otros roles usan el `tenantId` del JWT.
  - Body:
    - `type`: `"INCOME"` | `"EXPENSE"`
    - `category`:
      - Para `INCOME`: `"cobranzas"`, `"ajustes"`, `"otros ingresos"`
      - Para `EXPENSE`: `"insumos"`, `"sueldos"`, `"alquileres"`, `"transporte"`, `"otros gastos"`
    - `amount`: número positivo (mínimo 0.01, 2 decimales)
    - `entryDate`: `"YYYY-MM-DD"` (fecha pasada o presente)
    - `note`: string (opcional)
  - Categoría inválida para el tipo → `400`
- `GET /api/v1/finances?tenantId={tenantId}&month={1-12}&year={yyyy}` - Listar entradas del período
  - Requiere JWT.
  - Si `month` y `year` se omiten: devuelve el mes actual.
  - Si se provee solo uno de los dos: `400`.
  - Devuelve lista ordenada por `entryDate DESC`, luego `createdAt DESC`.
- `GET /api/v1/finances/summary?tenantId={tenantId}&month={1-12}&year={yyyy}` - Resumen financiero del período
  - Requiere JWT.
  - Devuelve:
    - `systemSales`: suma de `total` de pedidos en estado `CONFIRMED`, `SHIPPED` o `DELIVERED` del período (calculado desde orders, no ingresado manualmente)
    - `salesOrdersCount`: cantidad de esos pedidos
    - `manualIncome`: suma de entradas tipo `INCOME` del período
    - `expenses`: suma de entradas tipo `EXPENSE` del período
    - `totalIncome`: `systemSales + manualIncome`
    - `netResult`: `totalIncome - expenses`
    - `expenseByCategory`: array de `{ category, total }` para cada categoría de egreso
- `DELETE /api/v1/finances/{id}?tenantId={tenantId}` - Eliminar entrada
  - Requiere JWT.
  - Si el `id` no pertenece al tenant → `400`.

### Users

- `GET /api/v1/users` - Listar usuarios
  - Query opcional: `tenantId`
- `POST /api/v1/users` - Crear usuario
  - Body: `{ "username": "operador1", "password": "secret", "tenantId": 1, "role": "ADMIN", "active": true }`
- `PUT /api/v1/users/{id}` - Actualizar tenant/rol/estado
  - Body: `{ "tenantId": 1, "role": "USER", "active": true }`
- `PUT /api/v1/users/{id}/password` - Cambiar password
  - Body: `{ "password": "newSecret" }`
- Requiere rol `SUPERADMIN`

### Auth

- `POST /api/v1/auth/login`
  - Body: `{ "username": "admin", "password": "admin" }`
  - Devuelve JWT con claims `tenantId` y `role`
- `POST /api/v1/auth/change-password`
  - Requiere JWT válido
  - Body: `{ "currentPassword": "Actual123!", "newPassword": "NuevaFuerte#123" }`
  - Devuelve `204 No Content`
  - Política de password:
    - mínimo 8 caracteres
    - al menos 1 número

## Ejemplos reales (auth/users/roles)

1. Login (obtiene JWT):
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

2. Listar usuarios (solo `SUPERADMIN`):
```bash
curl http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <JWT>"
```

3. Crear usuario `ADMIN` para tenant 1:
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_SUPERADMIN>" \
  -d "{\"username\":\"operador1\",\"password\":\"clave1234\",\"tenantId\":1,\"role\":\"ADMIN\",\"active\":true}"
```

4. Cambio de password propio:
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d "{\"currentPassword\":\"admin123\",\"newPassword\":\"nuevo1234\"}"
```
