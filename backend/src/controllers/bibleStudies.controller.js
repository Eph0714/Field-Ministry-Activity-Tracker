const { v4: uuidv4 } = require('uuid');
const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

function scopeToPublisher(req, conditions, params) {
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`bs.publisher_id = $${params.length}`);
  } else if (req.query.publisher_id) {
    params.push(req.query.publisher_id);
    conditions.push(`bs.publisher_id = $${params.length}`);
  }
}

async function list(req, res) {
  const { householder_id, updated_since } = req.query;
  const conditions = ['bs.is_deleted = false'];
  const params = [];

  scopeToPublisher(req, conditions, params);

  if (householder_id) {
    params.push(householder_id);
    conditions.push(`bs.householder_id = $${params.length}`);
  }
  if (updated_since) {
    params.push(updated_since);
    conditions.push(`bs.updated_at > $${params.length}`);
  }

  const { rows } = await pool.query(
    `SELECT bs.*, h.name AS householder_name, u.name AS publisher_name
     FROM bible_studies bs
     JOIN householders h ON h.id = bs.householder_id
     JOIN users u ON u.id = bs.publisher_id
     WHERE ${conditions.join(' AND ')}
     ORDER BY bs.start_time DESC`,
    params
  );
  res.json(rows);
}

async function create(req, res) {
  const { householder_id, publication, start_time, end_time, duration_seconds } = req.body;
  if (!householder_id) return res.status(400).json({ error: 'householder_id is required' });

  const uuid = req.body.uuid || uuidv4();
  try {
    const { rows } = await pool.query(
      `INSERT INTO bible_studies (uuid, householder_id, publisher_id, publication, start_time, end_time, duration_seconds)
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING *`,
      [uuid, householder_id, req.user.id, publication || null, start_time || null, end_time || null, duration_seconds || 0]
    );
    await logAudit(req.user.id, 'CREATE', 'bible_study', rows[0].id, null);
    res.status(201).json(rows[0]);
  } catch (err) {
    if (err.code === '23505') {
      const { rows } = await pool.query(`SELECT * FROM bible_studies WHERE uuid = $1`, [uuid]);
      if (rows.length > 0) return res.status(200).json(rows[0]);
    }
    throw err;
  }
}

async function update(req, res) {
  const { id } = req.params;
  const { publication, start_time, end_time, duration_seconds } = req.body;

  const conditions = ['id = $1', 'is_deleted = false'];
  const params = [id];
  if (req.user.role === 'publisher') {
    params.push(req.user.id);
    conditions.push(`publisher_id = $${params.length}`);
  }

  const { rows } = await pool.query(
    `UPDATE bible_studies
     SET publication = $${params.length + 1}, start_time = $${params.length + 2},
         end_time = $${params.length + 3}, duration_seconds = $${params.length + 4}
     WHERE ${conditions.join(' AND ')}
     RETURNING *`,
    [...params, publication || null, start_time || null, end_time || null, duration_seconds || 0]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Bible study not found' });
  await logAudit(req.user.id, 'UPDATE', 'bible_study', id, null);
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
    `UPDATE bible_studies SET is_deleted = true WHERE ${conditions.join(' AND ')}`,
    params
  );
  if (rowCount === 0) return res.status(404).json({ error: 'Bible study not found' });
  await logAudit(req.user.id, 'DELETE', 'bible_study', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, create, update, remove };
