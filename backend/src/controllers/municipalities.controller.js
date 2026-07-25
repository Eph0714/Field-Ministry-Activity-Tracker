const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

async function list(req, res) {
  const { rows } = await pool.query(`SELECT * FROM municipalities ORDER BY name ASC`);
  res.json(rows);
}

async function create(req, res) {
  const { name } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });

  const { rows } = await pool.query(`INSERT INTO municipalities (name) VALUES ($1) RETURNING *`, [name]);
  await logAudit(req.user.id, 'CREATE', 'municipality', rows[0].id, { name });
  res.status(201).json(rows[0]);
}

async function update(req, res) {
  const { id } = req.params;
  const { name } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });

  const { rows } = await pool.query(
    `UPDATE municipalities SET name = $1 WHERE id = $2 RETURNING *`,
    [name, id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Municipality not found' });
  await logAudit(req.user.id, 'UPDATE', 'municipality', id, { name });
  res.json(rows[0]);
}

async function remove(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(`DELETE FROM municipalities WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'Municipality not found' });
  await logAudit(req.user.id, 'DELETE', 'municipality', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, create, update, remove };
