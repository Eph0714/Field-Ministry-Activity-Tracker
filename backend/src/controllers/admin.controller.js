const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

async function listAuditLogs(req, res) {
  const { rows } = await pool.query(
    `SELECT a.*, u.name AS user_name
     FROM audit_logs a
     LEFT JOIN users u ON u.id = a.user_id
     ORDER BY a.created_at DESC
     LIMIT 500`
  );
  res.json(rows);
}

async function clearAuditLogs(req, res) {
  await pool.query(`DELETE FROM audit_logs`);
  await logAudit(req.user.id, 'AUDIT_LOG_CLEARED', null, null, null);
  res.json({ message: 'Audit logs cleared' });
}

module.exports = { listAuditLogs, clearAuditLogs };
