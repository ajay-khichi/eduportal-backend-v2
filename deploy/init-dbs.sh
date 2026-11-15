#!/bin/sh
set -eu

create_app_db() {
  db_name="$1"
  db_user="$2"
  db_pass="$3"

  if [ -z "$db_pass" ]; then
    echo "Skipping $db_name (no password configured)"
    return 0
  fi

  echo "Creating role '$db_user' and database '$db_name'..."
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
      IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${db_user}') THEN
        EXECUTE format('CREATE USER %I WITH PASSWORD %L', '${db_user}', '${db_pass}');
      END IF;
    END
    \$\$;

    SELECT 'CREATE DATABASE ${db_name} OWNER ${db_user}'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${db_name}')\gexec

    GRANT ALL PRIVILEGES ON DATABASE ${db_name} TO ${db_user};
EOSQL
}

create_app_db "$EDUPORTAL_DB_NAME" "$EDUPORTAL_DB_USER" "$EDUPORTAL_DB_PASSWORD"
create_app_db "${APP2_DB_NAME:-app2_db}" "${APP2_DB_USER:-app2_user}" "${APP2_DB_PASSWORD:-}"
create_app_db "${APP3_DB_NAME:-app3_db}" "${APP3_DB_USER:-app3_user}" "${APP3_DB_PASSWORD:-}"
