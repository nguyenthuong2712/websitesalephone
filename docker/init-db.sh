#!/bin/bash
set -euo pipefail

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
SQLCMD_COMMON_ARGS=(-S localhost -U sa -P "${MSSQL_SA_PASSWORD}" -C -b)
SA_PASSWORD="${MSSQL_SA_PASSWORD}"
DB_NAME="${DB_NAME:-manager_sale_phone}"
BACKUP_FILE="/var/opt/mssql/backup/manager_sale_phone.bak"
DATA_FILE="/var/opt/mssql/data/${DB_NAME}.mdf"
LOG_FILE="/var/opt/mssql/data/${DB_NAME}_log.ldf"

echo "[init-db] Waiting for SQL Server..."
for i in {1..60}; do
  if "${SQLCMD}" "${SQLCMD_COMMON_ARGS[@]}" -Q "SELECT 1" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

if ! "${SQLCMD}" "${SQLCMD_COMMON_ARGS[@]}" -Q "SELECT 1" >/dev/null 2>&1; then
  echo "[init-db] SQL Server did not become ready in time."
  exit 1
fi

DB_EXISTS=$("${SQLCMD}" "${SQLCMD_COMMON_ARGS[@]}" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.databases WHERE name='${DB_NAME}'")

if [[ "${DB_EXISTS}" == "1" ]]; then
  echo "[init-db] Database ${DB_NAME} already exists. Skipping restore."
  exit 0
fi

echo "[init-db] Reading logical file names from backup..."
FILELIST=$("${SQLCMD}" "${SQLCMD_COMMON_ARGS[@]}" -h -1 -W -s "|" -Q "RESTORE FILELISTONLY FROM DISK = '${BACKUP_FILE}'")
DATA_LOGICAL=$(echo "${FILELIST}" | awk -F"|" 'NR==1 {print $1}')
LOG_LOGICAL=$(echo "${FILELIST}" | awk -F"|" 'NR==2 {print $1}')

if [[ -z "${DATA_LOGICAL}" || -z "${LOG_LOGICAL}" ]]; then
  echo "[init-db] Could not detect logical names from backup file."
  exit 1
fi

echo "[init-db] Restoring ${DB_NAME} from ${BACKUP_FILE}..."
"${SQLCMD}" "${SQLCMD_COMMON_ARGS[@]}" -Q "RESTORE DATABASE [${DB_NAME}] FROM DISK = '${BACKUP_FILE}' WITH MOVE '${DATA_LOGICAL}' TO '${DATA_FILE}', MOVE '${LOG_LOGICAL}' TO '${LOG_FILE}', REPLACE, RECOVERY"

echo "[init-db] Restore completed."
