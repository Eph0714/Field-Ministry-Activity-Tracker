const pool = require('../config/db');

const RETENTION_DAYS = 7;
const TWELVE_HOURS_MS = 12 * 60 * 60 * 1000;

async function purgeOldAuditLogs() {
  try {
    const result = await pool.query(
      `DELETE FROM audit_logs WHERE created_at < NOW() - ($1 || ' days')::interval`,
      [RETENTION_DAYS]
    );
    if (result.rowCount > 0) {
      console.log(`Audit retention: purged ${result.rowCount} rows older than ${RETENTION_DAYS} days`);
    }
  } catch (err) {
    console.error('Audit retention purge failed:', err.message);
  }
}

function startAuditLogRetention() {
  purgeOldAuditLogs();
  setInterval(purgeOldAuditLogs, TWELVE_HOURS_MS);
}

module.exports = { startAuditLogRetention };
