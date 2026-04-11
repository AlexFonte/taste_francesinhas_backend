# passos para arrancar la base de datos en docker

## 1. Descargar las imágenes de Docker Hub
docker compose -f database.yml pull

## 3. Arrancar los contenedores en segundo plano

docker compose -f database.yml up -d


## 4. Comprobar que están corriendo
docker compose -f database.yml ps

## 5. Ver logs si algo falla
# el -f significa follow, se queda escuchando en tiempo real
# ctrl+c para salir
docker compose -f database.yml logs -f

# 6. Ver logs solo de postgres
docker compose -f database.yml logs postgres

# 7. Ver logs solo de pgadmin
docker compose -f database.yml logs pgadmin

# 8. Entrar a la terminal de postgres para escribir SQL
docker compose -f database.yml exec postgres psql -U admin -d tastefrancesinha

# 9. Parar los contenedores sin borrar nada
# los datos se conservan, puedes volver a arrancarlos con start
docker compose -f database.yml stop

# 10. Volver a arrancar después de un stop
docker compose -f database.yml start

# 11. Reiniciar los contenedores
# equivale a stop + start
docker compose -f database.yml restart

# 12. Parar y eliminar los contenedores
# los datos del volumen pg_data se conservan
docker compose -f database.yml down

# 13. Parar y eliminar TODO incluyendo los datos
# ⚠️ esto borra la base de datos entera
# úsalo solo si quieres empezar desde cero o cambiar el init.sql
docker compose -f database.yml down -v

# 14. Forzar recrear los contenedores sin borrar datos
# útil si cambias algo en el database.yml
docker compose -f database.yml up -d --force-recreate

# 16. Ver todos los volúmenes de Docker
# aquí verás pg_data con los datos de postgres
docker volume ls

# flujo habitual del día a día
# --- start ---
docker compose -f database.yml start

# --- stop ---
docker compose -f database.yml stop

# --- si cambias el init.sql y necesitas resetear ---
docker compose -f database.yml down -v
docker compose -f database.yml up -d