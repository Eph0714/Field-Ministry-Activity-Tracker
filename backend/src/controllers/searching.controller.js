const { v4: uuidv4 } = require('uuid');
const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

function scopeToPublisher(req, conditions, params) {
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`s.publisher_id = $${params.length}`);
  } else if (req.query.publisher_id) {
    params.push(req.query.publisher_id);
    conditions.push(`s.publisher_id = $${params.length}`);
  }
}

async function list(req, res) {
  const { householder_id, updated_since } = req.query;
  const conditions = ['s.is_deleted = false'];
  const params = [];

  scopeToPublisher(req, conditions, params);

  if (householder_id) {
    params.push(householder_id);
    conditions.push(`s.householder_id = $${params.length}`);
  }
  if (updated_since) {
    params.push(updated_since);
    conditions.push(`s.updated_at > $${params.length}`);
  }

  const { rows } = await pool.query(
    `SELECT s.*, h.name AS householder_name, u.name AS publisher_name
     FROM searching_sessions s
     JOIN householders h ON h.id = s.householder_id
     JOIN users u ON u.id = s.publisher_id
     WHERE ${conditions.join(' AND ')}
     ORDER BY s.start_time DESC`,
    params
  );
  res.json(rows);
}

async function create(req, res) {
  const {
    householder_id, language_spoken, preferred_language, marital_status, age,
    contact_number, remarks, start_time, end_time, duration_seconds,
  } = req.body;

  if (!householder_id) return res.status(400).json({ error: 'householder_id is required' });

  const uuid = req.body.uuid || uuidv4();
  try {
    const { rows } = await pool.query(
      `INSERT INTO searching_sessions
       (uuid, householder_id, publisher_id, language_spoken, preferred_language, marital_status, age,
        contact_number, remarks, start_time, end_time, duration_seconds)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
       RETURNING *`,
      [uuid, householder_id, req.user.id, language_spoken || null, preferred_language || null,
       marital_status || null, age || null, contact_number || null, remarks || null,
       start_time || null, end_time || null, duration_seconds || 0]
    );
    await logAudit(req.user.id, 'CREATE', 'searching_session', rows[0].id, null);
    res.status(201).json(rows[0]);
  } catch (err) {
    if (err.code === '23505') {
      const { rows } = await pool.query(`SELECT * FROM searching_sessions WHERE uuid = $1`, [uuid]);
      if (rows.length > 0) return res.status(200).json(rows[0]);
    }
    throw err;
  }
}

async function update(req, res) {
  const { id } = req.params;
  const {
    language_spoken, preferred_language, marital_status, age,
    contact_number, remarks, start_time, end_time, duration_seconds,
  } = req.body;

  const conditions = ['id = $1', 'is_deleted = false'];
  const params = [id];
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`publisher_id = $${params.length}`);
  }

  const { rows } = await pool.query(
    `UPDATE searching_sessions
     SET language_spoken = $${params.length + 1}, preferred_language = $${params.length + 2},
         marital_status = $${params.length + 3}, age = $${params.length + 4},
         contact_number = $${params.length + 5}, remarks = $${params.length + 6},
         start_time = $${params.length + 7}, end_time = $${params.length + 8}, duration_seconds = $${params.length + 9}
     WHERE ${conditions.join(' AND ')}
     RETURNING *`,
    [...params, language_spoken || null, preferred_language || null, marital_status || null, age || null,
     contact_number || null, remarks || null, start_time || null, end_time || null, duration_seconds || 0]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Searching session not found' });
  await logAudit(req.user.id, 'UPDATE', 'searching_session', id, null);
  res.json(rows[0]);
}

async function remove(req, res) {
  const { id } = req.params;
  const conditions = ['id = $1', 'is_deleted = false'];
  const params = [id];
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`publisher_id = $${params.length}`);
  }

  const { rowCount } = await pool.query(
    `UPDATE searching_sessions SET is_deleted = true WHERE ${conditions.join(' AND ')}`,
    params
  );
  if (rowCount === 0) return res.status(404).json({ error: 'Searching session not found' });
  await logAudit(req.user.id, 'DELETE', 'searching_session', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, create, update, remove };
