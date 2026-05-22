# Mani Factory SaaS - Backend Project Instructions

## 🎯 Project Overview

**Mani Factory SaaS** es un **Sistema SaaS Multi-Tenant Escalable y Adaptable** diseñado para gestionar operaciones de manufactura/comercio de productos.

### 🏢 Concepto SaaS Multi-Tenant

**CRÍTICO**: Este NO es un sistema mono-usuário. Es un **SaaS** donde:
- Cada **cliente (tenant)** tiene su **propio espacio aislado** en la plataforma
- Cada tenant ve **solo sus datos** (pedidos, stock, usuarios, finanzas)
- Múltiples tenants comparten la misma infraestructura pero datos 100% aislados
- Cada tenant puede tener **configuración diferente**

### 🔄 Adaptabilidad a Distintos Rubros

El sistema NO está hardcodeado para "maní". Es adaptable a cualquier rubro:
- **Textil**: Telas, confecciones, lotes de producción
- **Alimentos**: Jamón, queso, bebidas, harinas, etc.
- **Electrónica**: Componentes, ensamblado, control de calidad
- **Cualquier manufactura**: Que requiera gestión de pedidos + stock + producción + finanzas

### 📋 Configuración por Tenant

Cada tenant configurará:
- **Nombre de su empresa**
- **Productos** que vende (nombre, precio, descripción)
- **Datos de contacto** de su empresa
- **Usuarios internos** (empleados)
- **Transportistas/Proveedores**
- **Métodos de pago** (si aplica)
- **Reglas de producción** (tiempos, costos, etc.)

### Ejemplo Inicial
- Tenant 1 (Mani Factory): Vende Jamón, Queso, Salame, Pizza (bolsas 1kg)
- Tenant 2 (TextilX): Vende Telas y Confecciones
- Tenant 3 (BebidasCo): Vende Bebidas en botellas 500ml y 1L

### Objetivo Principal
Desarrollar un sistema integral que permita a cada tenant:
- Gestión de pedidos online (venta a clientes)
- Control de inventario/stock
- Gestión de producción (manufacturing)
- Seguimiento financiero (ingresos/egresos)
- Panel administrativo interno
- Página pública para sus clientes

---

## 🏗️ Stack Tecnológico Completo

### Backend
- **Spring Boot** 4.0.6
- **Java** 21
- **Spring Security** con JWT
- **Spring Data JPA** / Hibernate
- **PostgreSQL**
- **Maven**
- **Lombok** para reducir boilerplate
- **WebSockets** para notificaciones en tiempo real
- **REST API** architecture

### Frontend (Desacoplado)
- **React**
- **Tailwind CSS**
- **Axios**
- **React Router**

### Base de Datos
- **PostgreSQL** (Cloud-ready)
- Preparada para multi-tenancy
- Migraciones automáticas con Hibernate (ddl-auto: update)

### Deploy
- **Docker**
- **Railway** / **Render** (Cloud platforms)
- PostgreSQL Cloud

---

## 📋 Flujo de Pedidos (Customer Journey)

### Proceso para Clientes
1. Cliente accede a página pública
2. Visualiza catálogo de productos
3. Agrega productos al carrito
4. Completa formulario con datos
5. Selecciona transporte preferido
6. Envía pedido
7. Pedido se registra en base de datos
8. Administrador recibe notificación (WebSocket)

### Datos Capturados del Cliente
- **Nombre** (requerido)
- **Negocio** (nombre comercial)
- **Teléfono** (requerido)
- **Ciudad** (requerido)
- **Transporte Preferido** (dropdown: seleccionar opción)

### Estados de Pedido
- **PENDIENTE**: Recibido, esperando procesamiento
- **CONFIRMADO**: Validado por admin
- **EN_PRODUCCION**: En proceso de elaboración
- **LISTO**: Completado, esperando envío
- **ENVIADO**: En tránsito
- **ENTREGADO**: Cliente recibió
- **CANCELADO**: Rechazado o anulado

---

## 🗂️ Arquitectura Backend - Estructura de Módulos

```
backend/src/main/java/com/manifactory/
├── auth/              # Autenticación, login, JWT
├── users/             # Gestión de usuarios internos
├── tenants/           # Multi-tenancy, configuración por cliente
├── products/          # Catálogo de productos
├── orders/            # Sistema de pedidos
├── clients/           # Información y historial de clientes
├── stock/             # Control de inventario
├── finance/           # Gestión de ingresos/egresos
├── production/        # Planificación y ejecución de producción
├── common/            # Utilities compartidas
├── config/            # Configuraciones (Security, WebSocket, DB)
├── exception/         # Manejo de excepciones personalizado
├── security/          # Filtros y handlers de seguridad
└── BackendApplication.java
```

### Estructura Interna de Cada Módulo
```
modulename/
├── controller/        # REST Endpoints
├── service/           # Lógica de negocio
├── repository/        # Acceso a datos (JPA)
├── dto/               # Data Transfer Objects
├── entity/            # Entidades JPA
├── mapper/            # Conversión Entity ↔ DTO
├── exception/         # Excepciones específicas del módulo
└── [constants/]       # Constantes si aplica
```

---

## 🛢️ Base de Datos - Modelo Multi-Tenant

### Configuración Actual
```properties
Database: PostgreSQL
Host: localhost:5432
Database name: mani_saas
Username: postgres
Auto-schema-update: Habilitado (Hibernate ddl-auto: update)
```

### Principio Multi-Tenancy
**CRÍTICO**: Todas las entidades deben incluir `tenant_id` para garantizar aislamiento de datos.

### Entidad Tenant (Base de Multi-Tenancy)
```sql
CREATE TABLE tenant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Entidades Principales (Modelo de Datos)

#### 1. **User** (Usuarios Internos)
```
- id
- tenant_id (FK)
- email
- password (BCrypt)
- full_name
- role (SUPER_ADMIN, ADMIN, EMPLOYEE)
- active
- created_at
```

#### 2. **Product** (Catálogo)
```
- id
- tenant_id (FK)
- name (Jamón, Queso, Salame, Pizza)
- description
- price
- weight (1kg estándar)
- image_url
- active
- created_at
```

#### 3. **Client** (Clientes Externos)
```
- id
- tenant_id (FK)
- name
- business_name
- email
- phone
- city
- preferred_transport
- contact_history
- created_at
```

#### 4. **Order** (Pedidos)
```
- id
- tenant_id (FK)
- client_id (FK)
- order_number (unique per tenant)
- total_amount
- status (PENDIENTE, CONFIRMADO, EN_PRODUCCION, LISTO, ENVIADO, ENTREGADO, CANCELADO)
- notes
- created_at
- updated_at
```

#### 5. **OrderItem** (Líneas de Pedido)
```
- id
- tenant_id (FK)
- order_id (FK)
- product_id (FK)
- quantity
- unit_price
- subtotal
```

#### 6. **StockMovement** (Control de Inventario)
```
- id
- tenant_id (FK)
- product_id (FK)
- movement_type (ENTRADA, SALIDA, AJUSTE)
- quantity
- reason (compra, producción, venta, pérdida)
- reference_order_id (FK - nullable)
- created_at
```

#### 7. **FinanceMovement** (Ingresos/Egresos)
```
- id
- tenant_id (FK)
- type (INGRESO, EGRESO)
- amount
- category (venta, costo_producción, transporte, otro)
- description
- reference_order_id (FK - nullable)
- created_at
```

#### 8. **ProductionRecord** (Registro de Producción)
```
- id
- tenant_id (FK)
- product_id (FK)
- batch_number
- quantity_planned
- quantity_produced
- status (PLANIFICADA, EN_PROCESO, COMPLETADA, CANCELADA)
- production_date
- created_at
```

### Estándar de Timestamps
Todas las entidades deben incluir:
- `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE)
- `deleted_at` (TIMESTAMP - para soft delete si aplica)

---

## 🔐 Seguridad - Autenticación y Autorización

### Autenticación
- **Spring Security** + **JWT**
- **BCryptPasswordEncoder** para contraseñas
- **Access Token** obligatorio en todas las requests (Bearer token)
- Token incluye: `user_id`, `tenant_id`, `role`, `exp`

### Roles y Permisos
```
SUPER_ADMIN
├── Acceso a todos los tenants
├── Gestión de tenants
├── Gestión de usuarios globales
└── Auditoría

ADMIN (por tenant)
├── Gestión de usuarios
├── Acceso a todos los módulos
├── Pedidos (crear, editar, cancelar)
├── Stock (ver, ajustar)
├── Finanzas (ver, registrar)
└── Reportes

EMPLOYEE (por tenant)
├── Visualizar pedidos asignados
├── Actualizar estado de producción
├── Ver stock disponible
└── Reportes limitados
```

### Protección de Endpoints
```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public ResponseEntity<?> getOrders(@RequestParam Long tenantId) { ... }
```

---

## 📚 Módulos y Sus Responsabilidades

### **auth** (Autenticación)
- Login/Logout
- Generación de JWT
- Refresh token
- Registro de usuarios

### **users** (Gestión de Usuarios Internos)
- CRUD de usuarios
- Asignación de roles
- Cambio de contraseña
- Solo para usuarios internos (ADMIN, EMPLOYEE)

### **tenants** (Multi-Tenancy)
- Creación de nuevos tenants
- Configuración por tenant
- Planes/límites
- Suspensión/activación

### **products** (Catálogo)
- CRUD de productos
- Gestión de precios
- Imágenes/descripciones
- Disponibilidad

### **orders** (Sistema de Pedidos)
- Crear pedidos desde clientes
- CRUD de pedidos (admin)
- Cambio de estado
- Notificaciones de cambios
- Historial de pedidos

### **clients** (Clientes Externos)
- Registro de clientes
- Historial de pedidos
- Datos de contacto
- Preferencias (transporte)

### **stock** (Control de Inventario)
- Movimientos de stock
- Alertas de bajo stock
- Ajustes de inventario
- Reportes de stock

### **finance** (Gestión Financiera)
- Registro de ingresos (ventas)
- Registro de egresos (costos)
- Reconciliación
- Reportes financieros

### **production** (Producción)
- Planificación de batches
- Asignación de tareas
- Seguimiento de producción
- Historial de producción

---

## 🎨 Estructura de Respuestas API

### Response Exitosa
```json
{
  "success": true,
  "message": "Operación completada",
  "data": {
    "id": 1,
    "name": "Jamón",
    "price": 15.50
  }
}
```

### Response con Error
```json
{
  "success": false,
  "message": "El producto no existe",
  "error_code": "PRODUCT_NOT_FOUND",
  "timestamp": "2026-05-22T10:30:00Z"
}
```

### Paginación
```json
{
  "success": true,
  "data": [...],
  "pagination": {
    "page": 1,
    "size": 20,
    "total_elements": 150,
    "total_pages": 8
  }
}
```

---

## ✅ Reglas de Desarrollo - CRÍTICAS

### 1. **Lógica de Negocio**
- ❌ NUNCA en controllers
- ✅ SIEMPRE en service layer
- Controllers solo reciben requests y llaman a services

### 2. **DTOs (Data Transfer Objects)**
- ✅ Usar DTOs para TODAS las responses
- ✅ NUNCA retornar entidades directamente
- Crear DTOs específicos para cada operación:
  - `CreateOrderDTO`
  - `UpdateOrderDTO`
  - `OrderResponseDTO`

### 3. **Validaciones**
- ✅ Usar **Bean Validation** (@NotNull, @NotBlank, @Email, etc.)
- ✅ Validaciones en DTOs, no en entities
- ✅ Mensajes descriptivos en errores

### 4. **Multi-Tenancy**
- ✅ SIEMPRE incluir `tenant_id` en queries
- ✅ Verificar que usuario pertenece al tenant
- ✅ Filtrar datos por tenant en repository queries

### 5. **Seguridad con JWT**
- ✅ TODAS las rutas protegidas requieren JWT
- ✅ Extraer tenant_id del JWT
- ✅ Validar rol del usuario en endpoints

### 6. **Estructura de Código**
```
modulename/
├── controller/XyzController.java
├── service/XyzService.java (interface + impl)
├── repository/XyzRepository.java (extends JpaRepository)
├── dto/CreateXyzDTO.java
├── dto/UpdateXyzDTO.java
├── dto/XyzResponseDTO.java
├── entity/XyzEntity.java
└── mapper/XyzMapper.java (Entity ↔ DTO)
```

### 7. **Manejo de Excepciones**
```java
// Crear excepciones personalizadas
public class OrderNotFoundException extends RuntimeException { }

// En service
if (order == null) {
    throw new OrderNotFoundException("Order with ID " + id + " not found");
}

// En controller advice
@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<?> handleOrderNotFound(OrderNotFoundException ex) {
    return ResponseEntity.status(404).body(
        new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage())
    );
}
```

---

## 💻 Patrones de Código y Ejemplos

### Entity Pattern
```java
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;  // CRÍTICO: Multi-tenancy
    
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Repository Pattern
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // SIEMPRE filtrar por tenant_id
    
    List<Order> findByTenantIdAndStatus(Long tenantId, OrderStatus status);
    
    List<Order> findByTenantIdAndClientId(Long tenantId, Long clientId);
    
    Optional<Order> findByTenantIdAndId(Long tenantId, Long orderId);
    
    // Busqueda personalizada
    @Query("SELECT o FROM Order o WHERE o.tenantId = ?1 AND o.status = ?2 ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByStatus(Long tenantId, OrderStatus status);
}
```

### Service Pattern
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final ClientRepository clientRepository;
    private final OrderMapper mapper;
    private final StockService stockService;
    
    public OrderResponseDTO createOrder(Long tenantId, CreateOrderDTO dto) {
        // Validar cliente pertenece al tenant
        Client client = clientRepository.findByTenantIdAndId(tenantId, dto.getClientId())
            .orElseThrow(() -> new ClientNotFoundException("Client not found"));
        
        // Validar stock disponible
        stockService.validateStock(tenantId, dto.getItems());
        
        // Crear orden
        Order order = Order.builder()
            .tenantId(tenantId)
            .client(client)
            .orderNumber(generateOrderNumber(tenantId))
            .status(OrderStatus.PENDIENTE)
            .totalAmount(calculateTotal(dto))
            .build();
        
        Order saved = repository.save(order);
        
        // Actualizar stock
        stockService.decreaseStock(tenantId, dto.getItems());
        
        return mapper.toResponseDTO(saved);
    }
    
    public OrderResponseDTO getOrder(Long tenantId, Long orderId) {
        Order order = repository.findByTenantIdAndId(tenantId, orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return mapper.toResponseDTO(order);
    }
}
```

### Controller Pattern
```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
public class OrderController {
    private final OrderService service;
    private final JwtTokenProvider tokenProvider;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(
        @RequestHeader("Authorization") String token,
        @Valid @RequestBody CreateOrderDTO dto) {
        
        Long tenantId = tokenProvider.getTenantIdFromToken(token);
        OrderResponseDTO response = service.createOrder(tenantId, dto);
        
        return ResponseEntity.status(201).body(
            ApiResponse.success("Order created successfully", response)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
        @RequestHeader("Authorization") String token,
        @PathVariable Long id) {
        
        Long tenantId = tokenProvider.getTenantIdFromToken(token);
        OrderResponseDTO response = service.getOrder(tenantId, id);
        
        return ResponseEntity.ok(
            ApiResponse.success("Order retrieved", response)
        );
    }
}
```

### DTO Pattern
```java
// Create DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDTO> items;
    
    private String notes;
}

// Update DTO
@Data
public class UpdateOrderDTO {
    @NotNull
    @Pattern(regexp = "PENDIENTE|CONFIRMADO|EN_PRODUCCION|LISTO|ENVIADO|ENTREGADO|CANCELADO")
    private String status;
    
    private String notes;
}

// Response DTO
@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private String clientName;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
}
```

### Mapper Pattern
```java
@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final ClientMapper clientMapper;
    private final OrderItemMapper itemMapper;
    
    public OrderResponseDTO toResponseDTO(Order entity) {
        return OrderResponseDTO.builder()
            .id(entity.getId())
            .orderNumber(entity.getOrderNumber())
            .clientName(entity.getClient().getName())
            .status(entity.getStatus().name())
            .totalAmount(entity.getTotalAmount())
            .items(entity.getItems().stream()
                .map(itemMapper::toDTO)
                .collect(Collectors.toList()))
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
```
