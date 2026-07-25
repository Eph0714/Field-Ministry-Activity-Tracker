const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

async function list(req, res) {
  const { municipality_id } = req.query;
  const params = [];
  let query = `SELECT b.*, m.name AS municipality_name FROM barangays b
               JOIN municipalities m ON m.id = b.municipality_id`;
  if (municipality_id) {
    params.push(municipality_id);
    query += ` WHERE b.municipality_id = $1`;
  }
  query += ` ORDER BY m.name ASC, b.name ASC`;

  const { rows } = await pool.query(query, params);
  res.json(rows);
}

async function create(req, res) {
  const { municipality_id, name } = req.body;
  if (!municipality_id || !name) {
    return res.status(400).json({ error: 'municipality_id and name are required' });
  }

  const { rows } = await pool.query(
    `INSERT INTO barangays (municipality_id, name) VALUES ($1, $2) RETURNING *`,
    [municipality_id, name]
  );
  await logAudit(req.user.id, 'CREATE', 'barangay', rows[0].id, { name });
  res.status(201).json(rows[0]);
}

async function update(req, res) {
  const { id } = req.params;
  const { name } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });

  const { rows } = await pool.query(`UPDATE barangays SET name = $1 WHERE id = $2 RETURNING *`, [name, id]);
  if (rows.length === 0) return res.status(404).json({ error: 'Barangay not found' });
  await logAudit(req.user.id, 'UPDATE', 'barangay', id, { name });
  res.json(rows[0]);
}

async function remove(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(`DELETE FROM barangays WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'Barangay not found' });
  await logAudit(req.user.id, 'DELETE', 'barangay', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, create, update, remove };
