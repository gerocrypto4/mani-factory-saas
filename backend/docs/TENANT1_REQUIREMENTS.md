# Tenant 1 - Fabrica Mani Saborizados

## Contexto
Primer cliente (primer tenant) del sistema SaaS.

Canal de venta principal: mayorista.
Producto actual: bolsas de 1 kg de mani saborizado.

Sabores iniciales:
- Jamon
- Queso
- Salame
- Pizza

Guia UX/UI oficial para web comercial:
- `TENANT1_WEB_UX_BLUEPRINT.md`

---

## Objetivo general
Construir una primera version simple, profesional y mobile-first que permita:
- Tomar pedidos online de clientes mayoristas.
- Operar internamente pedidos, clientes, stock, caja y produccion.
- Escalar por etapas sin frenar el negocio.

---

## Requisitos funcionales

### 1. Web publica para clientes (pedidos)
- [ ] Home con branding:
  - logo/foto de fabrica
  - mensaje comercial: "Mani saborizado por mayor - envios a todo el pais"
- [ ] Catalogo de productos:
  - foto
  - descripcion corta
  - precio mayorista
  - stock disponible (opcional)
- [ ] Carrito y armado de pedido:
  - seleccionar cantidad por producto
  - agregar/quitar items
  - ver total
- [ ] Formulario de cliente en checkout:
  - nombre
  - negocio
  - telefono
  - ciudad
  - transporte preferido
- [ ] Confirmacion de pedido:
  - mensaje de exito
  - identificador de pedido
- [ ] Registro automatico:
  - pedido guardado en panel interno

### 2. Panel interno (operacion diaria)
- [ ] Login con usuario y password.
- [ ] Modulo clientes:
  - nombre, telefono, direccion
  - historial de compras
  - deuda/saldo (para ventas fiadas)
- [ ] Modulo pedidos:
  - pendientes
  - enviados
  - entregados
  - cancelados
- [ ] Modulo stock:
  - materias primas e insumos (mani, saborizantes, bolsas, cajas)
  - descuento automatico de stock al crear pedido
- [ ] Modulo ingresos/egresos:
  - ventas
  - gastos
  - transporte
  - materia prima
  - sueldos
  - impuestos
  - resumen mensual (ganancia, gasto, ventas)
- [ ] Modulo produccion:
  - registro diario de produccion por producto
  - ejemplo: "hoy se produjeron 120 bolsas de queso"

### 3. Automatizaciones deseadas
- [ ] Notificacion automatica del pedido por WhatsApp (ideal).
- [ ] Notificacion automatica del pedido por email (ideal).
- [ ] Generacion de comprobante PDF (ideal).

---

## Fases recomendadas (prioridad)

### Fase 1 - MVP (hacer primero)
- [ ] Web publica de pedidos:
  - home + catalogo + carrito + checkout + confirmacion
- [ ] Panel interno minimo:
  - pedidos
  - clientes basicos
  - stock basico
  - ingresos/egresos basicos
- [ ] Mobile-first y UX simple para uso diario.

### Fase 2 - Operacion avanzada
- [ ] Deuda/saldo por cliente.
- [ ] Produccion con reportes por periodo.
- [ ] Notificaciones automaticas (WhatsApp/email/PDF).

### Fase 3 - Expansion
- [ ] Facturacion.
- [ ] Integracion MercadoPago.
- [ ] Seguimiento de envios.
- [ ] Estadisticas avanzadas.
- [ ] App movil.
- [ ] Catalogo digital avanzado.
- [ ] Precios especiales por cliente.

---

## Criterios de aceptacion del MVP
- [ ] Un cliente puede hacer un pedido completo desde el celular sin asistencia.
- [ ] El pedido se guarda correctamente y aparece en panel interno.
- [ ] El equipo puede cambiar estado del pedido (pendiente/enviado/entregado/cancelado).
- [ ] El stock basico refleja el impacto del pedido.
- [ ] Existe visibilidad minima de ingresos/egresos del mes.

---

## Nota de estrategia
No construir "todo perfecto" de entrada.
Primero resolver operacion real diaria (pedidos + clientes + stock basico + caja basica), luego iterar por etapas.
