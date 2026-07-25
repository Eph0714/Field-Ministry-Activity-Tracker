const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const pool = require('../config/db');
const { logAudit } = require('../utils/audit');
const { uploadPhotoToStorage } = require('../utils/photoStorage');

function signToken(user) {
  return jwt.sign(
    { id: user.id, email: user.email, name: user.name, role: user.role },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
  );
}

function toUserDto(row) {
  return {
    id: row.id,
    uuid: row.uuid,
    name: row.name,
    email: row.email,
    role: row.role,
    photo_url: row.photo_url,
    is_active: row.is_active,
    approval_status: row.approval_status,
  };
}

async function signup(req, res) {
  const { name, email, password } = req.body;
  if (!name || !email || !password) {
    return res.status(400).json({ error: 'name, email and password are required' });
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const uuid = uuidv4();

  try {
    const { rows } = await pool.query(
      `INSERT INTO users (uuid, name, email, password_hash, role, approval_status)
       VALUES ($1, $2, $3, $4, 'publisher', 'pending')
       RETURNING *`,
      [uuid, name, email, passwordHash]
    );
    await logAudit(rows[0].id, 'SIGNUP', 'user', rows[0].id, { email });
    res.status(201).json({ message: 'Signup received, pending admin approval' });
  } catch (err) {
    if (err.code === '23505') {
      return res.status(409).json({ error: 'An account with this email already exists' });
    }
    throw err;
  }
}

async function login(req, res) {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'email and password are required' });
  }

  const { rows } = await pool.query(`SELECT * FROM users WHERE email = $1 AND is_active = true`, [email]);
  const user = rows[0];
  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials', code: 'INVALID_CREDENTIALS' });
  }

  const valid = await bcrypt.compare(password, user.password_hash);
  if (!valid) {
    return res.status(401).json({ error: 'Invalid credentials', code: 'INVALID_CREDENTIALS' });
  }

  if (user.approval_status === 'pending') {
    return res.status(403).json({ error: 'Account pending admin approval', code: 'ACCOUNT_PENDING' });
  }
  if (user.approval_status === 'rejected') {
    return res.status(403).json({ error: 'Account was rejected', code: 'ACCOUNT_REJECTED' });
  }

  const token = signToken(user);
  await logAudit(user.id, 'LOGIN', 'user', user.id, null);
  res.json({ token, user: toUserDto(user) });
}

async function me(req, res) {
  const { rows } = await pool.query(`SELECT * FROM users WHERE id = $1`, [req.user.id]);
  if (rows.length === 0) return res.status(404).json({ error: 'User not found' });
  res.json(toUserDto(rows[0]));
}

async function changePassword(req, res) {
  const { currentPassword, newPassword } = req.body;
  if (!currentPassword || !newPassword) {
    return res.status(400).json({ error: 'currentPassword and newPassword are required' });
  }

  const { rows } = await pool.query(`SELECT * FROM users WHERE id = $1`, [req.user.id]);
  const user = rows[0];
  const valid = await bcrypt.compare(currentPassword, user.password_hash);
  if (!valid) {
    return res.status(401).json({ error: 'Current password is incorrect' });
  }

  const passwordHash = await bcrypt.hash(newPassword, 10);
  await pool.query(`UPDATE users SET password_hash = $1 WHERE id = $2`, [passwordHash, user.id]);
  await logAudit(user.id, 'CHANGE_PASSWORD', 'user', user.id, null);
  res.json({ message: 'Password changed' });
}

async function uploadPhoto(req, res) {
  if (!req.file) return res.status(400).json({ error: 'No photo uploaded' });

  const photoUrl = await uploadPhotoToStorage(req.file);
  await pool.query(`UPDATE users SET photo_url = $1 WHERE id = $2`, [photoUrl, req.user.id]);
  await logAudit(req.user.id, 'UPDATE_PHOTO', 'user', req.user.id, null);
  res.json({ photo_url: photoUrl });
}

module.exports = { signup, login, me, changePassword, uploadPhoto };
