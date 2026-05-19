# UniPlan — Plataforma Universitaria Inteligente

## Descripción General

UniPlan es una plataforma universitaria moderna desarrollada con arquitectura híbrida relacional + documental, diseñada para gestionar eventos universitarios, participación estudiantil y administración de organizadores mediante una experiencia visual premium basada en glassmorphism y dashboards dinámicos.

El sistema implementa:

- Autenticación JWT
- Roles separados
- Persistencia políglota
- MongoDB + PostgreSQL
- Spring Boot + Thymeleaf
- Arquitectura MVC
- Dashboards personalizados por rol
- Gestión de eventos
- Participación estudiantil

---

# Arquitectura del Proyecto

## Tecnologías principales

### Backend
- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- Spring Data MongoDB
- Hibernate

### Bases de datos

#### PostgreSQL (Neon)
Usado para:
- usuarios
- organizadores
- registros
- relaciones
- autenticación

#### MongoDB Atlas
Usado para:
- eventos
- metadata flexible
- tags
- estadísticas

### Frontend
- Thymeleaf
- HTML5
- CSS3
- JavaScript Vanilla

---

# Roles del Sistema

El sistema implementa 3 roles principales:

## ADMIN
Control global de la plataforma.

### Puede:
- gestionar eventos
- gestionar organizadores
- controlar participación
- exportar datos CSV

---

## ORGANIZER
Gestión operativa de eventos.

### Puede:
- crear eventos
- gestionar asistentes
- controlar cupos
- registrar asistencia

---

## STUDENT
Experiencia universitaria.

### Puede:
- explorar eventos
- inscribirse
- ver actividad
- registrar participación

---

# Usuarios de Prueba

## ADMIN

```text
Correo:
admin@uniplan.com

Contraseña:
123456
```

---

## ORGANIZER

```text
Correo:
organizer@uniplan.com

Contraseña:
123456
```

---

## STUDENT

```text
Correo:
student@uniplan.com

Contraseña:
123456
```

---

# Flujo General del Sistema

```text
Register
↓
Login
↓
JWT generado
↓
sessionStorage
↓
Redirección por rol
↓
Dashboard correspondiente
↓
Gestión de funcionalidades
```

---

# Cómo Clonar y Ejecutar el Proyecto

# 1 — Clonar el repositorio

```bash
git clone URL_DEL_REPOSITORIO
```

---

# 2 — Abrir el proyecto

Abrir la carpeta del proyecto en:

```text
Visual Studio Code
o
IntelliJ IDEA
```

---

# 3 — Verificar Java 17

Ejecutar:

```bash
java -version
```

Debe mostrar algo parecido a:

```text
Java 17
```

---

# 4 — Ejecutar el proyecto

## Windows

```bash
.\mvnw spring-boot:run
```

## Mac/Linux

```bash
./mvnw spring-boot:run
```

---

# 5 — Verificar que inició correctamente

Debe aparecer algo parecido a:

```text
Tomcat started on port 8080
Started UniplanBackendApplication
```

---

# 6 — Abrir la aplicación

```text
http://localhost:8080/login
```

---

# Importante

El proyecto ya incluye:

- configuración PostgreSQL
- configuración MongoDB Atlas
- JWT Secret
- configuración Spring Security

Por lo tanto:

NO es necesario configurar variables de entorno
ni modificar `application.properties`
para ejecutar el sistema.

---

# URLs Principales

## Login

```text
/login
```

---

## Register

```text
/register
```

---

## Admin

```text
/admin/home
```

---

## Organizer

```text
/organizer/home
```

---

## Student

```text
/student/home
```

---

# Estructura del Proyecto

## Backend

```text
src/main/java/com/uniplan/uniplan_backend
```

---

## controllers/

Contiene:
- endpoints REST
- navegación MVC
- controladores web

### Ejemplos

```text
AuthController
EventController
ViewController
```

---

## services/

Lógica de negocio del sistema.

### Ejemplos

```text
AuthService
EventService
```

---

## repositories/

Acceso a bases de datos.

### PostgreSQL
```text
UserRepository
OrganizerRepository
```

### MongoDB
```text
EventRepository
```

---

## dto/

Objetos de transferencia de datos.

### Ejemplos

```text
LoginRequest
RegisterRequest
CreateEventRequest
EventResponse
```

---

## security/

Configuración JWT y Spring Security.

### Ejemplos

```text
JwtService
JwtFilter
SecurityConfig
CustomUserDetailsService
```

---

## model/

Entidades del sistema.

### Relacionales
```text
users
organizers
event_registrations
```

### Documentales
```text
events
```

---

# Frontend

## templates/

Vistas HTML Thymeleaf.

### Ejemplos

```text
login.html
register.html
admin-home.html
student-home.html
organizer-home.html
create-event.html
```

---

## static/css/

Estilos globales.

### Ejemplo

```text
home-style.css
```

---

## static/js/

Lógica frontend.

### Ejemplos

```text
auth.js
event.js
```

---

# Dashboards

## Admin Dashboard

### Funcionalidades

#### Eventos
- Crear evento
- Ver eventos
- Editar evento
- Cancelar evento

#### Organizadores
- Registrar organizador
- Ver organizadores
- Desactivar organizador

#### Participación
- Ver inscritos
- Registrar asistencia
- Ver cupos

#### Resumen
- Eventos activos
- Usuarios registrados
- Participación total
- Exportar CSV

---

## Organizer Dashboard

### Funcionalidades

#### Eventos
- Crear evento
- Mis eventos
- Eventos finalizados

#### Participación
- Ver inscritos
- Registrar asistencia
- Ver cupos

#### Actividad
- Notificaciones
- Última actividad
- Recordatorios

---

## Student Dashboard

### Funcionalidades

#### Eventos
- Explorar eventos
- Mis inscripciones
- Próximos eventos

#### Participación
- Registrar asistencia
- Horas acumuladas
- Eventos completados

#### Actividad
- Favoritos
- Notificaciones
- Última actividad

---

# Seguridad

El sistema implementa:

## JWT Authentication

El token se almacena en:

```javascript
sessionStorage
```

---

## Auth Guards

Cada dashboard verifica:

```javascript
role === "ADMIN"
role === "ORGANIZER"
role === "STUDENT"
```

---

## Spring Security

Protección de rutas mediante:

```java
.hasRole()
.hasAnyRole()
```

---

# Persistencia Políglota

## PostgreSQL

Usado para:
- integridad relacional
- autenticación
- relaciones fuertes

---

## MongoDB

Usado para:
- eventos
- metadata dinámica
- tags
- búsquedas flexibles

---

# Cómo Crear un Nuevo Requerimiento

## IMPORTANTE

Antes de programar:
SIEMPRE analizar la arquitectura existente.

No crear archivos innecesarios.
No duplicar lógica.
Seguir el patrón actual del proyecto.

---

# Flujo Correcto para Agregar Funcionalidades

---

# 1 — Analizar el modelo

Preguntarse:

```text
¿Va en PostgreSQL o MongoDB?
```

## PostgreSQL
Para:
- relaciones
- usuarios
- integridad

## MongoDB
Para:
- metadata flexible
- documentos
- eventos dinámicos

---

# 2 — Crear o modificar el modelo

## Relacional

```java
@Entity
@Table(...)
```

## Documental

```java
@Document(collection = "...")
```

---

# 3 — Crear Repository

## PostgreSQL

```java
extends JpaRepository
```

## MongoDB

```java
extends MongoRepository
```

---

# 4 — Crear DTOs

Separar:
- request
- response

Ejemplo:

```text
CreateEventRequest
EventResponse
```

---

# 5 — Crear Service

Aquí va:
- lógica de negocio
- validaciones
- persistencia

NO poner lógica en controllers.

---

# 6 — Crear Controller

Agregar:
- endpoints REST
- rutas
- responses

---

# 7 — Configurar Security

SIEMPRE revisar:

```java
SecurityConfig
```

Agregar:
- rutas permitidas
- roles autorizados

Ejemplo:

```java
.requestMatchers("/admin/**")
.hasRole("ADMIN")
```

---

# 8 — Agregar ViewController

Si la funcionalidad tiene HTML:

```java
@GetMapping(...)
return "vista";
```

---

# 9 — Crear Vista HTML

Ubicación:

```text
templates/
```

Seguir:
- mismo fondo
- mismo glassmorphism
- mismo navbar
- misma arquitectura visual

---

# 10 — Reutilizar CSS

PRIMERO:
intentar reutilizar:

```text
home-style.css
```

NO crear CSS innecesario.

---

# 11 — Crear JS

Ubicación:

```text
static/js/
```

Ejemplos:
- fetch APIs
- auth
- eventos
- interacción frontend

---

# 12 — Conectar Frontend + Backend

Verificar:
- rutas
- fetch
- JWT
- sessionStorage
- redirects

---

# Convenciones del Proyecto

## HTML
- glassmorphism oscuro
- liquid glass
- diseño premium
- responsive

---

## CSS
- reutilizable
- modular
- elegante
- minimalista

---

## JavaScript
- limpio
- modular
- fetch API
- JWT headers

---

# Buenas Prácticas del Proyecto

## SIEMPRE

✅ reutilizar estilos  
✅ reutilizar componentes  
✅ mantener arquitectura MVC  
✅ separar lógica  
✅ usar DTOs  
✅ proteger rutas  
✅ validar roles  
✅ mantener consistencia visual  

---

## NUNCA

❌ lógica en HTML  
❌ lógica pesada en controllers  
❌ duplicar CSS  
❌ mezclar Mongo y JPA incorrectamente  
❌ crear vistas inconsistentes  
❌ hardcodear JWT  

---

# Estado Actual del Proyecto

## Implementado

✅ Login JWT  
✅ Register  
✅ Roles  
✅ MongoDB Atlas  
✅ PostgreSQL Neon  
✅ Dashboards premium  
✅ Crear eventos  
✅ Listar eventos  
✅ Búsqueda de eventos  
✅ Seguridad por rol  
✅ Glassmorphism UI  
✅ Persistencia híbrida  
✅ Arquitectura MVC  



# Autor

Proyecto desarrollado como plataforma universitaria inteligente utilizando arquitectura híbrida moderna basada en Spring Boot + MongoDB + PostgreSQL.
