const { v4: uuidv4 } = require('uuid');
const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

function scopeToPublisher(req, conditions, params) {
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`rv.publisher_id = $${params.length}`);
  } else if (req.query.publisher_id) {
    params.push(req.query.publisher_id);
    conditions.push(`rv.publisher_id = $${params.length}`);
  }
}

async function list(req, res) {
  const { householder_id, updated_since } = req.query;
  const conditions = ['rv.is_deleted = false'];
  const params = [];

  scopeToPublisher(req, conditions, params);

  if (householder_id) {
    params.push(householder_id);
    conditions.push(`rv.householder_id = $${params.length}`);
  }
  if (updated_since) {
    params.push(updated_since);
    conditions.push(`rv.updated_at > $${params.length}`);
  }

  const { rows } = await pool.query(
    `SELECT rv.*, h.name AS householder_name, u.name AS publisher_name
     FROM return_visits rv
     JOIN householders h ON h.id = rv.householder_id
     JOIN users u ON u.id = rv.publisher_id
     WHERE ${conditions.join(' AND ')}
     ORDER BY rv.visit_datetime DESC`,
    params
  );
  res.json(rows);
}

async function create(req, res) {
  const { householder_id, visit_datetime, outcome_notes, is_potential_rv } = req.body;
  if (!householder_id) return res.status(400).json({ error: 'householder_id is required' });

  const uuid = req.body.uuid || uuidv4();
  try {
    const { rows } = await pool.query(
      `INSERT INTO return_visits (uuid, householder_id, publisher_id, visit_datetime, outcome_notes)
       VALUES ($1, $2, $3, COALESCE($4, NOW()), $5)
       RETURNING *`,
      [uuid, householder_id, req.user.id, visit_datetime || null, outcome_notes || null]
    );
    if (is_potential_rv !== undefined) {
      await pool.query(`UPDATE householders SET is_potential_rv = $1 WHERE id = $2`, [!!is_potential_rv, householder_id]);
    }
    await logAudit(req.user.id, 'CREATE', 'return_visit', rows[0].id, null);
    res.status(201).json(rows[0]);
  } catch (err) {
    if (err.code === '23505') {
      const { rows } = await pool.query(`SELECT * FROM return_visits WHERE uuid = $1`, [uuid]);
      if (rows.length > 0) return res.status(200).json(rows[0]);
    }
    throw err;
  }
}

async function update(req, res) {
  const { id } = req.params;
  const { visit_datetime, outcome_notes } = req.body;

  const conditions = ['id = $1', 'is_deleted = false'];
  const params = [id];
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`publisher_id = $${params.length}`);
  }

  const { rows } = await pool.query(
    `UPDATE return_visits
     SET visit_datetime = COALESCE($${params.length + 1}, visit_datetime), outcome_notes = $${params.length + 2}
     WHERE ${conditions.join(' AND ')}
     RETURNING *`,
    [...params, visit_datetime || null, outcome_notes || null]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Return visit not found' });
  await logAudit(req.user.id, 'UPDATE', 'return_visit', id, null);
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
    `UPDATE return_visits SET is_deleted = true WHERE ${conditions.join(' AND ')}`,
    params
  );
  if (rowCount === 0) return res.status(404).json({ error: 'Return visit not found' });
  await logAudit(req.user.id, 'DELETE', 'return_visit', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, create, update, remove };
