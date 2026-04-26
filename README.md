# Taste Francesinhas - Backend

API REST desarrollada para el Trabajo Final de Máster (TFM) en la UOC 2025-2026 (2025-2). 
Permite descubrir, proponer y valorar **francesinhas** (sandwich típico de Oporto, el "croque-monsieur" típico portugués) en restaurantes de la zona.

> Aplicación complementaria al frontend Angular ubicado en `../taste_francesinhas_frontend`.

---

## Stack

| Tecnología      | Versión  |
|-----------------|----------|
| Java            | 25       |
| Spring Boot     | 4.0.5    |
| Spring Security | 7.x      |
| PostgreSQL      | 18.x     |
| JJWT            | 0.12.6   |
| Lombok          | última   |
| Maven Wrapper   | 3.9.x    |

---

## Requisitos previos

- **JDK 25** (Temurin / Oracle / Zulu)
- **Docker Desktop** (para la base de datos local)
- **Git**
- (Opcional) IntelliJ IDEA o VS Code con extensión Java

> Maven **NO** hace falta instalarlo: el repo incluye `mvnw` / `mvnw.cmd`.

---

## Instalación rápida (5 pasos)

### 1. Clonar el repo

```bash
git clone <url-del-repo>
cd taste_franceseinhas_backend
```

### 2. Arrancar PostgreSQL con Docker

```bash
docker compose -f src/main/resources/static/database/postgres_db.yml up -d
```

Esto levanta dos contenedores:

| Contenedor              | Puerto local | Acceso                      |
|-------------------------|--------------|-----------------------------|
| `tastefrancesinha_db`   | `5432`       | `postgres` / sin contraseña |
| `tastefrancesinha_admin`| `8080`       | http://localhost:8080 - `admin@admin.com` / `admin123` |

> **Nota:** pgAdmin ocupa el puerto `8080`. Por eso la aplicación arranca por defecto en `8082` localmente. Si pones la app en `8080`, cambia el puerto de pgAdmin antes.

### 3. Crear el schema y cargar datos de ejemplo

Conéctate al contenedor y ejecuta `init.sql`:

```bash
docker exec -i tastefrancesinha_db psql -U postgres -d postgres < src/main/resources/static/database/init.sql
```

O bien desde pgAdmin: **Servers → Postgres → Query Tool**, abrir `init.sql` y ejecutar.

Esto crea el schema `taste_francesinhas`, todas las tablas y siembra:
- 14 restaurantes activos + 1 pendiente
- Francesinhas aceptadas y 3 pendientes de revisión
- Usuarios de prueba (`USER` y `ADMIN`)

### 4. Configurar variables de entorno

Crea un fichero `.env` en la raíz del módulo o expórtalas en tu shell:

```bash
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=
DB_SHOW_SQL=false
PORT=8082
ALLOWED_ORIGINS=http://localhost:4200
JWT_SECRET=cambia-esto-por-una-clave-aleatoria-de-al-menos-256-bits
```

> **Generar `JWT_SECRET`** rápido:
> `openssl rand -base64 64`
> En Windows (PowerShell): `[Convert]::ToBase64String((1..64 | %{[byte](Get-Random -Max 256)}))`

En IntelliJ: **Run → Edit Configurations → Environment variables**.

### 5. Arrancar la aplicación

```bash
./mvnw spring-boot:run         # Linux / macOS / Git Bash
mvnw.cmd spring-boot:run       # Windows CMD/PowerShell
```

API disponible en: **http://localhost:8082/tastefrancesinhas**

Healthcheck: `GET http://localhost:8082/tastefrancesinhas/actuator/health` → `{"status":"UP"}`

Swagger UI: **http://localhost:8082/tastefrancesinhas/swagger-ui.html**

---

## Comandos útiles de Docker

```bash
# Parar sin borrar datos
docker compose -f src/main/resources/static/database/postgres_db.yml stop

# Volver a arrancar (datos preservados)
docker compose -f src/main/resources/static/database/postgres_db.yml start

# Reset total (borra el volumen y todos los datos)
docker compose -f src/main/resources/static/database/postgres_db.yml down -v
docker compose -f src/main/resources/static/database/postgres_db.yml up -d

# Logs en vivo
docker logs -f tastefrancesinha_db
```

---

## Estructura del código

```
src/main/java/com/app/tastefrancesinhasbackend/
|__ config/          # SecurityConfig, ApiConstants
|__ controller/      # AuthController, FrancesinhaController, ReviewController, ...
|__ dto/             # Records de entrada/salida (FrancesinhaDTO, AuthDTO, ...)
|__ entity/          # Entidades JPA (User, Francesinha, Restaurant, Review, Favorite)
|   |__ enums/       # Role, FrancesinhaStatus, FrancesinhaType
|__ exception/       # GlobalExceptionHandler (formato RFC 7807)
|__ repository/      # Interfaces Spring Data JPA
|__ security/        # JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl
|__ service/         # Lógica de negocio
|__ spec/            # Specifications JPA para filtros dinámicos
```

---

## Endpoints principales

### Auth
| Método | Ruta            | Auth | Descripción            |
|--------|-----------------|------|------------------------|
| POST   | `/auth/signup`  | -    | Registro               |
| POST   | `/auth/login`   | -    | Login                  |
| POST   | `/auth/refresh` | -    | Renueva par de tokens  |

### Restaurantes
| Método | Ruta                | Auth         | Descripción                  |
|--------|---------------------|--------------|------------------------------|
| GET    | `/restaurants`      | -            | Listado paginado (solo activos) |
| GET    | `/restaurants/{id}` | -            | Detalle                      |
| POST   | `/restaurants`      | USER / ADMIN | Crea un restaurante (nace inactivo) |

### Francesinhas
| Método | Ruta                                | Auth  | Descripción                     |
|--------|-------------------------------------|-------|---------------------------------|
| GET    | `/francesinhas`                     | -     | Listado aceptadas (paginado)    |
| GET    | `/francesinhas/{id}`                | -     | Detalle                         |
| GET    | `/francesinhas/stats`               | ADMIN | Contadores (pending/accepted/…) |
| POST   | `/francesinhas/propose`             | USER  | Propuesta nueva (queda PENDING) |
| GET    | `/francesinhas/pending`             | ADMIN | Listado pendientes              |
| GET    | `/francesinhas/pending/{id}`        | ADMIN | Detalle sin filtro de estado    |
| GET    | `/francesinhas/pending/{id}/reviews`| ADMIN | Reviews de una pendiente        |
| PATCH  | `/francesinhas/pending/{id}/status` | ADMIN | Aprobar / rechazar              |

### Reviews
| Método | Ruta                                    | Auth         | Descripción           |
|--------|-----------------------------------------|--------------|-----------------------|
| GET    | `/francesinhas/{id}/reviews`            | -            | Listado paginado      |
| POST   | `/francesinhas/{id}/reviews`            | USER / ADMIN | Crear (1 por usuario) |
| DELETE | `/francesinhas/{id}/reviews/{reviewId}` | USER / ADMIN | Borrar la propia      |

> El POST acepta un flag opcional `propuesta: true` - usado por el flujo "Proponer" para asociar la primera review a una francesinha aún estado PENDING.

### Favoritos
| Método | Ruta                          | Auth | Descripción                   |
|--------|-------------------------------|------|-------------------------------|
| GET    | `/favorites`                  | USER | Listado paginado              |
| GET    | `/favorites/{francesinhaId}`  | USER | Check `{ isFavorite: bool }`  |
| POST   | `/favorites/{francesinhaId}`  | USER | Toggle (añade o elimina)      |

---

## Seguridad

- **BCrypt** cost factor 12 para el hash de contraseñas
- Roles: `USER` y `ADMIN` (ADMIN no puede valorar ni marcar favoritos)
- Autorización a nivel de URL (`SecurityConfig`) + método (`@PreAuthorize`)
- Errores en formato **RFC 7807 ProblemDetail**
- **401** cuando el token está caducado/ausente, **403** cuando el rol no es el permitido

### JWT dual-token

La autenticación es **stateless**: no se guarda sesión en servidor, cada request se valida con su propio token. Hay dos tipos:

| Token             | Duración | Cómo se envía                          | Para qué sirve                                         |
|-------------------|----------|----------------------------------------|--------------------------------------------------------|
| **Access token**  | 1 hora   | Header `Authorization: Bearer <token>` | Autenticar cualquier petición a la API                 |
| **Refresh token** | 7 días   | Body de `POST /auth/refresh`           | Obtener un nuevo par cuando el access token caduca     |

Ambos se firman con `HS256` y la clave `JWT_SECRET`. El refresh lleva un claim extra `type=refresh` que `JwtService.isValid()` rechaza explícitamente - así un refresh **no** puede usarse como access token.

#### Claims del access token

```json
{
  "sub": "alex@example.com",          // email del usuario (subject)
  "role": "USER",                     // ROLE_USER | ROLE_ADMIN
  "iat": 1745692800,                  // issued at (epoch seconds)
  "exp": 1745696400                   // expiration (iat + 1h)
}
```

#### Claims del refresh token

```json
{
  "sub": "alex@example.com",
  "type": "refresh",                  // marca distintiva
  "iat": 1745692800,
  "exp": 1746297600                   // iat + 7 días
}
```

#### Flujo de renovación

```
1. POST /auth/login            → { accessToken, refreshToken, name, email, role }
2. GET /favorites              → Authorization: Bearer <accessToken>
   ...una hora después...
3. GET /favorites              → 401 Unauthorized (access caducado)
4. POST /auth/refresh          → body { refreshToken: "..." }
                                 → { accessToken, refreshToken, ... }   (par nuevo)
5. GET /favorites              → Authorization: Bearer <nuevo accessToken>
```

> **Decisión**: el endpoint `/auth/refresh` devuelve **un par nuevo** (rotación), no solo un access nuevo. Así el refresh anterior queda invalidado por expiración natural en 7 días desde el último uso.

#### Configuración (variables de entorno)

| Variable                | Default        | Descripción                                  |
|-------------------------|----------------|----------------------------------------------|
| `JWT_SECRET`            | (obligatorio)  | Clave HMAC ≥ 256 bits en Base64              |
| `app.jwt.expiration`    | `3600000` ms   | TTL del access token (1 h)                   |
| `app.jwt.refresh-expiration` | `604800000` ms | TTL del refresh token (7 días)          |

#### Punto de entrada para no autenticados

`SecurityConfig` declara un `AuthenticationEntryPoint` que devuelve **401** con cuerpo `application/problem+json` (mismo formato que `GlobalExceptionHandler`):

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Token caducado o ausente"
}
```

Esto reemplaza el comportamiento por defecto de Spring Security (que devolvería 403 a peticiones anónimas) y permite al frontend distinguir limpiamente:
- **401** → sesión expirada → hacer logout y redirigir a login
- **403** → autenticado pero sin permisos → mostrar mensaje, no logout

---

## Pruebas con Bruno

El repo incluye una colección **Bruno** lista para usar en `Bruno/TasteFrancesinhas/`:

1. Instalar [Bruno](https://www.usebruno.com/).
2. Abrir la carpeta `Bruno/TasteFrancesinhas/`.
3. Seleccionar el environment `Local` o `Prod`.
4. Ejecutar `Auth/Login` - el script guarda el token en una variable de colección.
5. El resto de requests autenticadas leerán ese token automáticamente.

---

## Ejemplos cURL

```bash
# Registro
curl -X POST http://localhost:8082/tastefrancesinhas/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Alex","email":"alex@example.com","password":"Password1"}'

# Login
curl -X POST http://localhost:8082/tastefrancesinhas/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alex@example.com","password":"Password1"}'

# Listado público
curl http://localhost:8082/tastefrancesinhas/francesinhas

# Endpoint autenticado
curl http://localhost:8082/tastefrancesinhas/favorites \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

---

## Despliegue

- **Backend**: Railway (variables de entorno gestionadas en el panel del proyecto).
- **Base de datos**: Supabase (PostgreSQL gestionado).
- `ddl-auto: validate` - Hibernate **no** modifica el schema; cualquier cambio se aplica como migración manual en Supabase.

---

## Troubleshooting

| Síntoma                                          | Causa probable                         | Solución                                      |
|--------------------------------------------------|----------------------------------------|-----------------------------------------------|
| `Connection refused` al arrancar Spring          | PostgreSQL no está levantado           | `docker compose ... up -d` y esperar healthy  |
| `Schema "taste_francesinhas" not found`          | No se ejecutó `init.sql`               | Ejecutar paso 3 del bloque de instalación     |
| `JWT signature does not match`                   | `JWT_SECRET` cambió entre arranques    | Mantener el mismo secret o re-loguearse       |
| `403 Forbidden` con token aparentemente válido   | Endpoint requiere otro rol             | Revisar `@PreAuthorize` del controller        |
| pgAdmin no arranca, puerto 8080 ocupado          | La app Spring está usando 8080         | Cambiar `PORT=8082` (o el de pgAdmin)         |