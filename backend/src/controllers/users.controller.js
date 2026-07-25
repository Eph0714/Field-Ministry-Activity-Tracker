const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

const SAFE_COLUMNS = `id, uuid, name, email, role, photo_url, is_active, approval_status, created_at, updated_at`;

async function list(req, res) {
  const { rows } = await pool.query(
    `SELECT ${SAFE_COLUMNS} FROM users WHERE approval_status = 'approved' ORDER BY name ASC`
  );
  res.json(rows);
}

async function pendingSignups(req, res) {
  const { rows } = await pool.query(
    `SELECT ${SAFE_COLUMNS} FROM users WHERE approval_status = 'pending' ORDER BY created_at ASC`
  );
  res.json(rows);
}

async function approveSignup(req, res) {
  const { id } = req.params;
  const { rows } = await pool.query(
    `UPDATE users SET approval_status = 'approved' WHERE id = $1 AND approval_status = 'pending' RETURNING ${SAFE_COLUMNS}`,
    [id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Pending signup not found' });
  await logAudit(req.user.id, 'SIGNUP_APPROVED', 'user', id, null);
  res.json(rows[0]);
}

async function rejectSignup(req, res) {
  const { id } = req.params;
  const { rows } = await pool.query(
    `DELETE FROM users WHERE id = $1 AND approval_status = 'pending' RETURNING name, email`,
    [id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Pending signup not found' });
  await logAudit(req.user.id, 'SIGNUP_DECLINED', 'user', id, rows[0]);
  res.json({ message: 'Signup declined' });
}

async function create(req, res) {
  const { name, email, password, role } = req.body;
  if (!name || !email || !password || !role) {
    return res.status(400).json({ error: 'name, email, password and role are required' });
  }
  if (!['publisher', 'overseer', 'admin'].includes(role)) {
    return res.status(400).json({ error: 'Invalid role' });
  }

  const passwordHash = await bcrypt.hash(password, 10);
  try {
    const { rows } = await pool.query(
      `INSERT INTO users (uuid, name, email, password_hash, role, approval_status)
       VALUES ($1, $2, $3, $4, $5, 'approved')
       RETURNING ${SAFE_COLUMNS}`,
      [uuidv4(), name, email, passwordHash, role]
    );
    await logAudit(req.user.id, 'CREATE', 'user', rows[0].id, { email, role });
    res.status(201).json(rows[0]);
  } catch (err) {
    if (err.code === '23505') {
      return res.status(409).json({ error: 'An account with this email already exists' });
    }
    throw err;
  }
}

async function update(req, res) {
  const { id } = req.params;
  const { name, role, is_active, password } = req.body;

  if (role && !['publisher', 'overseer', 'admin'].includes(role)) {
    return res.status(400).json({ error: 'Invalid role' });
  }

  const { rows: existingRows } = await pool.query(`SELECT * FROM users WHERE id = $1`, [id]);
  const existing = existingRows[0];
  if (!existing) return res.status(404).json({ error: 'User not found' });

  const nextName = name !== undefined ? name : existing.name;
  const nextRole = role !== undefined ? role : existing.role;
  const nextIsActive = is_active !== undefined ? is_active : existing.is_active;
  const nextPasswordHash = password ? await bcrypt.hash(password, 10) : existing.password_hash;

  const { rows } = await pool.query(
    `UPDATE users SET name = $1, role = $2, is_active = $3, password_hash = $4 WHERE id = $5
     RETURNING ${SAFE_COLUMNS}`,
    [nextName, nextRole, nextIsActive, nextPasswordHash, id]
  );
  await logAudit(req.user.id, 'UPDATE', 'user', id, { name: nextName, role: nextRole, is_active: nextIsActive });
  res.json(rows[0]);
}

async function remove(req, res) {
  const { id } = req.params;
  if (Number(id) === req.user.id) {
    return res.status(400).json({ error: 'Cannot delete your own account' });
  }
  const { rowCount } = await pool.query(`DELETE FROM users WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'User not found' });
  await logAudit(req.user.id, 'DELETE', 'user', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, pendingSignups, approveSignup, rejectSignup, create, update, remove };
