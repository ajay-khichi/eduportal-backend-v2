#!/bin/sh
# Daily PostgreSQL backup script for shared-postgres container.
# Optionally uploads to Oracle Cloud Object Storage via a Pre-Authenticated Request (PAR) URL.
set -eu

BACKUP_DIR="${BACKUP_DIR:-/mnt/pgdata/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_FILE="${BACKUP_DIR}/all-dbs-${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

docker exec shared-postgres pg_dumpall -U "$POSTGRES_USER" | gzip > "$BACKUP_FILE"
echo "Created local backup: $BACKUP_FILE"

# Optional off-VM upload to OCI Object Storage (create a bucket-level PAR URL with object write access)
if [ -n "${OCI_PAR_URL:-}" ]; then
  curl -fsS -X PUT --data-binary "@${BACKUP_FILE}" "${OCI_PAR_URL%/}/all-dbs-${TIMESTAMP}.sql.gz"
  echo "Uploaded backup to OCI Object Storage"
fi

# Delete old local dumps beyond RETENTION_DAYS
find "$BACKUP_DIR" -name "all-dbs-*.sql.gz" -type f -mtime +"$RETENTION_DAYS" -delete
