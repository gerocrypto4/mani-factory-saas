# Tenant 1 - Blueprint UX/UI Web Comercial (CRIS-JOR)

## Objetivo
Definir una guia visual y funcional para que la web de CRIS-JOR transmita operacion real, confianza B2B y conversion de pedidos mayoristas.

Referencia de estilo analizada: sitio industrial/corporativo moderno con foco en conversion.

---

## Resumen ejecutivo
La web debe verse como empresa seria con operacion solida:
- clara en propuesta comercial
- simple para pedir
- profesional en tono visual
- mobile-first real para trafico desde WhatsApp

No buscamos "una landing linda". Buscamos "una maquina de pedidos".

---

## Hallazgos clave del analisis UX/UI

### 1) Jerarquia visual clara
- El usuario entiende rapido:
  - que vende la marca
  - para quien
  - accion principal
  - como contactarse/pedir
- Criterio: reducir ruido, aumentar conversion.

### 2) Sensacion corporativa moderna
- Patrones esperados:
  - fondos limpios
  - aire visual
  - tipografia grande
  - grids ordenados
  - colores sobrios
  - CTAs fuertes
  - fotos reales
- Resultado esperado: pasar de "emprendimiento" a "proveedor serio".

### 3) Ritmo visual profesional
Secuencia recomendada:
1. Hero
2. Beneficio principal
3. Confianza/prueba social
4. Productos
5. Proceso
6. CTA
7. Contacto

### 4) Copy operativo
Lenguaje de negocio (no marketing vacio):
- distribucion mayorista
- pedidos rapidos
- stock
- cobertura
- atencion

### 5) UX de conversion
- CTA visible
- navegacion simple
- pocas decisiones simultaneas
- bloques cortos

---

## Arquitectura de Home recomendada (CRIS-JOR)
1. Header fijo
- Logo CRIS-JOR
- Catalogo
- Como comprar
- Distribucion
- Contacto
- CTA destacado: "Hacer pedido"

2. Hero principal
- Mensaje directo:
  - "Mani saborizado para comercios que necesitan vender mas."
- Subtexto de alcance mayorista
- CTAs: "Ver catalogo" + "Hacer pedido"
- Confianza rapida: produccion propia, atencion directa, entregas constantes
- Visual con fotos reales de producto/fabrica

3. Barra de confianza
- Produccion propia
- Pedidos mayoristas
- Envios coordinados
- Atencion personalizada

4. Productos destacados
- Cards con:
  - imagen real
  - nombre/sabor
  - presentacion
  - badge comercial
  - agregar al carrito

5. Como comprar (4 pasos)
1. Elegis productos
2. Armas pedido
3. Confirmamos transporte
4. Recibis mercaderia

6. Beneficios comerciales
- stock constante
- atencion rapida
- produccion estable
- entregas coordinadas

7. Quienes somos
- fotos reales de fabrica/produccion
- texto corto de respaldo operativo

8. CTA intermedio fuerte
- "Queres empezar a vender CRIS-JOR en tu negocio?"
- Boton: "Solicitar pedido mayorista"

9. FAQ real
- minimo de compra
- envio
- pagos
- tiempos de despacho

10. Footer corporativo
- navegacion
- contacto
- WhatsApp
- Instagram
- ubicacion/horario

---

## Sistema de diseno (base)

### Paleta
- `primary`: `#C6862E`
- `primaryDark`: `#B37422`
- `dark`: `#111111`
- `darkSoft`: `#1B1B1B`
- `surface`: `#F6F4EF`
- `text`: `#101010`
- `muted`: `#6B7280`
- `border`: `#E5E7EB`
- `success`: `#15803D`
- `error`: `#DC2626`

### Tipografia
- Headings: Space Grotesk (alternativa: Sora)
- Body: Inter

### Reglas visuales
- Evitar:
  - neones
  - gradients exagerados
  - glassmorphism fuerte
  - animaciones innecesarias
- Priorizar:
  - fotos reales
  - sombras suaves
  - microanimaciones utiles
  - aire visual

---

## UX del flujo de pedidos

### Flujo
1. Explorar catalogo
2. Carrito lateral sticky/drawer
3. Checkout simple en una pantalla
4. Exito con proximo paso claro

### Campos checkout (minimos)
- Nombre
- Negocio
- Telefono
- Ciudad
- Transporte
- Comentarios (opcional)

### Validaciones clave
- telefono valido
- al menos 1 producto
- nombre/negocio completos

### Mensaje de exito
"Pedido recibido correctamente. Nuestro equipo va a confirmar stock y coordinacion de envio."

### CTAs recomendados
- Hacer pedido
- Ver catalogo
- Agregar al pedido
- Continuar pedido
- Confirmar pedido mayorista

---

## Responsive strategy
- Mobile first real (principal canal esperado)
- Mobile:
  - hero vertical
  - CTA visible sin scroll
  - catalogo 1 columna
  - carrito fullscreen
- Tablet:
  - 2 columnas productos
- Desktop:
  - hasta 4 columnas
  - max width 1440px

---

## Plan de implementacion por fases

### MVP (frontend comercial)
- Home premium corporativa
- Catalogo + carrito
- Checkout limpio
- Integracion WhatsApp basica
- Estados carga/error
- Imagenes optimizadas

### Fase 2
- Dashboard comercial ampliado
- Historial/recompra
- Favoritos y busqueda avanzada

### Fase 3
- Full multi-tenant UX
- Roles avanzados
- Panel distribuidores
- Analytics y automatizacion logistica

---

## Criterios de aceptacion UX
- Hero explica propuesta en <= 3 segundos.
- CTA principal visible sin scroll en mobile.
- Agregar al carrito en 1 click con feedback inmediato.
- Checkout en una sola pantalla sin friccion.
- No perder carrito al navegar.
- Lighthouse objetivo: 90+ con LCP < 2.5s.

---

## Conclusion operativa
La web de CRIS-JOR debe comunicar:
"Producimos en serio. Respondemos pedidos. Somos confiables para volumen."

Ese mensaje es el motor de conversion B2B.
