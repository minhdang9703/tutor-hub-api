# PostgreSQL backup

Run this on the production host; keep the output outside the Docker volume and encrypt/off-site copy it.

```sh
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U "$POSTGRES_USER" -Fc "$POSTGRES_DB" > tutorhub-$(date +%F).dump
```

Verify a backup regularly by restoring it into an empty PostgreSQL database:

```sh
pg_restore --clean --if-exists --dbname=tutorhub_restore tutorhub-YYYY-MM-DD.dump
```
