# Taste Francesinhas - Backend

API REST desarrollada para el Trabajo Final de Máster (TFM) en la UOC. Permite descubrir, proponer y valorar
francesinhas (el "croque-monsieur" típico portugués) en restaurantes de la zona.

## Stack tecnológico

| Tecnología      | Versión |
|-----------------|---------|
| Java            | 25      |
| Spring Boot     | 4.0.5   |
| Spring Security | 7.x     |
| PostgreSQL      | 18.x    |
| JJWT            | 0.12.6  |
| Lombok          | -       |
| Maven           | 3.9.x   |

## Arquitectura

```
src/main/java/com/app/tastefrancesinhasbackend/
|-- config/          # SecurityConfig (CORS, filtros, BCrypt)
|-- controller/      # Controladores REST
|-- dto/             # Records de entrada/salida con método response() de mapeo
|-- entity/          # Entidades JPA
|   |-- enums/       # Role, FrancesinhaStatus, FrancesinhaType
|-- exception/       # Excepciones custom + GlobalExceptionHandler (RFC 7807)
|-- repository/      # Interfaces Spring Data JPA
|-- security/        # JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl
|-- service/         # Lógica de negocio
```

## Arrancar la base de datos (Docker)

Los ficheros de Docker están en `src/main/resources/static/database/`.

```bash
# Arrancar PostgreSQL + pgAdmin
docker compose -f src/main/resources/static/database/postgres_db.yml up -d

# Parar sin borrar datos
docker compose -f src/main/resources/static/database/postgres_db.yml stop

# Resetear completamente (borra todos los datos)
docker compose -f src/main/resources/static/database/postgres_db.yml down -v
docker compose -f src/main/resources/static/database/postgres_db.yml up -d
```

## Arrancar la aplicación

```bash
./mvnw spring-boot:run
```

La API queda disponible en: `http://localhost:8082/tastefrancesinhas`

## Configuración (`application.yaml`)

| Propiedad         | Valor                                       |
|-------------------|---------------------------------------------|
| Puerto            | `8082`                                      |
| Context path      | `/tastefrancesinhas`                        |
| Base de datos     | `jdbc:postgresql://localhost:5432/postgres` |
| Schema            | `taste_francesinhas`                        |
| JWT access token  | 1 hora                                      |
| JWT refresh token | 7 días                                      |

> En producción sustituir `app.jwt.secret` por una clave segura y actualizar los CORS con la URL real del frontend.

## Endpoints

### Auth

| Método | Ruta            | Auth | Descripción               |
|--------|-----------------|------|---------------------------|
| POST   | `/auth/signup`  | No   | Registro, devuelve tokens |
| POST   | `/auth/login`   | No   | Login, devuelve tokens    |
| POST   | `/auth/refresh` | No   | Renueva el par de tokens  |
| POST   | `/auth/logout`  | No   | Logout (stateless)        |

### Restaurantes

| Método | Ruta                | Auth         | Descripción                  |
|--------|---------------------|--------------|------------------------------|
| GET    | `/restaurants`      | No           | Lista todos los restaurantes |
| GET    | `/restaurants/{id}` | No           | Detalle de un restaurante    |
| POST   | `/restaurants`      | USER / ADMIN | Propone un nuevo restaurante |

> Los restaurantes nunca se eliminan. También se crean automáticamente cuando se propone una francesinha en un local que
> aún no existe.

### Francesinhas

| Método | Ruta                                | Auth  | Descripción                             |
|--------|-------------------------------------|-------|-----------------------------------------|
| GET    | `/francesinhas`                     | No    | Lista francesinhas aceptadas            |
| GET    | `/francesinhas/{id}`                | No    | Detalle (solo si aceptada)              |
| POST   | `/francesinhas/propose`             | USER  | Propone una francesinha (queda PENDING) |
| GET    | `/francesinhas/pending`             | ADMIN | Lista las pendientes de revisión        |
| GET    | `/francesinhas/pending/{id}`        | ADMIN | Detalle sin filtro de estado            |
| PATCH  | `/francesinhas/pending/{id}/status` | ADMIN | Acepta o rechaza                        |

### Reviews

| Método | Ruta                                    | Auth         | Descripción                      |
|--------|-----------------------------------------|--------------|----------------------------------|
| GET    | `/francesinhas/{id}/reviews`            | No           | Lista reviews de una francesinha |
| POST   | `/francesinhas/{id}/reviews`            | USER / ADMIN | Crea una review (1 por usuario)  |
| DELETE | `/francesinhas/{id}/reviews/{reviewId}` | USER / ADMIN | Borra la propia review           |

> El body de `POST /francesinhas/{id}/reviews` acepta un flag opcional `propuesta` (boolean).
> Cuando se envía como `true` el backend busca la francesinha en estado `PENDING` (usado por el
> flujo de *Proponer* del frontend, que encadena la creación de la francesinha y su primera review
> antes de que el admin la apruebe). Si se omite o es `false`, se busca en estado `ACCEPTED`
> (review normal sobre una francesinha ya publicada).

### Favoritos

| Método | Ruta                         | Auth | Descripción                       |
|--------|------------------------------|------|-----------------------------------|
| GET    | `/favorites`                 | USER | Lista los favoritos del usuario   |
| POST   | `/favorites/{francesinhaId}` | USER | Toggle favorito (añade o elimina) |

> Los favoritos solo son accesibles para usuarios con rol `USER`. Los `ADMIN` no tienen favoritos.

## Seguridad

- Autenticación stateless con **JWT Bearer token**
- Contraseñas hasheadas con **BCrypt** (cost factor 12)
- Roles: `USER` y `ADMIN`
- Autorización a nivel de método con `@PreAuthorize`
- Errores devueltos en formato **RFC 7807 ProblemDetail**

## Ejemplos de uso

```bash
# Registro
curl -X POST http://localhost:8082/tastefrancesinhas/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8082/tastefrancesinhas/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Listado público de francesinhas
curl http://localhost:8082/tastefrancesinhas/francesinhas

# Proponer una francesinha (requiere token)
curl -X POST http://localhost:8082/tastefrancesinhas/francesinhas/propose \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"restaurantId":1,"name":"Francesinha Especial","price":12.50,"type":"ESPECIAL"}'
