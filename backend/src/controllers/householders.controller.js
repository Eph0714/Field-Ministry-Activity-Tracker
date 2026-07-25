const { v4: uuidv4 } = require('uuid');
const pool = require('../config/db');
const { logAudit } = require('../utils/audit');
const { uploadPhotoToStorage } = require('../utils/photoStorage');

async function list(req, res) {
  const { search, municipality_id, barangay_id, status, updated_since } = req.query;
  const conditions = ['h.is_deleted = false'];
  const params = [];

  if (search) {
    params.push(`%${search}%`);
    conditions.push(`(h.name ILIKE $${params.length} OR h.address ILIKE $${params.length})`);
  }
  if (municipality_id) {
    params.push(municipality_id);
    conditions.push(`h.municipality_id = $${params.length}`);
  }
  if (barangay_id) {
    params.push(barangay_id);
    conditions.push(`h.barangay_id = $${params.length}`);
  }
  if (status) {
    params.push(status);
    conditions.push(`h.status = $${params.length}`);
  }
  if (updated_since) {
    params.push(updated_since);
    conditions.push(`h.updated_at > $${params.length}`);
  }

  const { rows } = await pool.query(
    `SELECT h.*, m.name AS municipality_name, b.name AS barangay_name
     FROM householders h
     LEFT JOIN municipalities m ON m.id = h.municipality_id
     LEFT JOIN barangays b ON b.id = h.barangay_id
     WHERE ${conditions.join(' AND ')}
     ORDER BY h.name ASC`,
    params
  );
  res.json(rows);
}

async function getOne(req, res) {
  const { id } = req.params;
  const { rows } = await pool.query(
    `SELECT h.*, m.name AS municipality_name, b.name AS barangay_name
     FROM householders h
     LEFT JOIN municipalities m ON m.id = h.municipality_id
     LEFT JOIN barangays b ON b.id = h.barangay_id
     WHERE h.id = $1 AND h.is_deleted = false`,
    [id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Householder not found' });
  res.json(rows[0]);
}

async function history(req, res) {
  const { id } = req.params;
  const [src, bs, rv] = await Promise.all([
    pool.query(
      `SELECT s.*, u.name AS publisher_name FROM searching_sessions s
       JOIN users u ON u.id = s.publisher_id
       WHERE s.householder_id = $1 AND s.is_deleted = false ORDER BY s.start_time DESC`,
      [id]
    ),
    pool.query(
      `SELECT bs.*, u.name AS publisher_name FROM bible_studies bs
       JOIN users u ON u.id = bs.publisher_id
       WHERE bs.householder_id = $1 AND bs.is_deleted = false ORDER BY bs.start_time DESC`,
      [id]
    ),
    pool.query(
      `SELECT rv.*, u.name AS publisher_name FROM return_visits rv
       JOIN users u ON u.id = rv.publisher_id
       WHERE rv.householder_id = $1 AND rv.is_deleted = false ORDER BY rv.visit_datetime DESC`,
      [id]
    ),
  ]);
  res.json({ searching: src.rows, bibleStudies: bs.rows, returnVisits: rv.rows });
}

function extractFields(body) {
  return {
    name: body.name,
    address: body.address || null,
    latitude: body.latitude ?? null,
    longitude: body.longitude ?? null,
    status: body.status || 'Potential',
    topic: body.topic || null,
    remarks: body.remarks || null,
    municipality_id: body.municipality_id || null,
    barangay_id: body.barangay_id || null,
  };
}

async function create(req, res) {
  const f = extractFields(req.body);
  if (!f.name) return res.status(400).json({ error: 'name is required' });

  const uuid = req.body.uuid || uuidv4();
  try {
    const { rows } = await pool.query(
      `INSERT INTO householders (uuid, name, address, latitude, longitude, status, topic, remarks, municipality_id, barangay_id, created_by)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
       RETURNING *`,
      [uuid, f.name, f.address, f.latitude, f.longitude, f.status, f.topic, f.remarks, f.municipality_id, f.barangay_id, req.user.id]
    );
    await logAudit(req.user.id, 'CREATE', 'householder', rows[0].id, { name: f.name });
    res.status(201).json(rows[0]);
  } catch (err) {
    if (err.code === '23505') {
      const { rows } = await pool.query(`SELECT * FROM householders WHERE uuid = $1`, [uuid]);
      if (rows.length > 0) return res.status(200).json(rows[0]);
    }
    throw err;
  }
}

async function update(req, res) {
  const { id } = req.params;
  const f = extractFields(req.body);
  if (!f.name) return res.status(400).json({ error: 'name is required' });

  const { rows } = await pool.query(
    `UPDATE householders
     SET name = $1, address = $2, latitude = $3, longitude = $4, status = $5, topic = $6, remarks = $7,
         municipality_id = $8, barangay_id = $9
     WHERE id = $10 AND is_deleted = false
     RETURNING *`,
    [f.name, f.address, f.latitude, f.longitude, f.status, f.topic, f.remarks, f.municipality_id, f.barangay_id, id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Householder not found' });
  await logAudit(req.user.id, 'UPDATE', 'householder', id, { name: f.name });
  res.json(rows[0]);
}

async function setPotentialRv(req, res) {
  const { id } = req.params;
  const { is_potential_rv } = req.body;
  const { rows } = await pool.query(
    `UPDATE householders SET is_potential_rv = $1 WHERE id = $2 AND is_deleted = false RETURNING *`,
    [!!is_potential_rv, id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Householder not found' });
  res.json(rows[0]);
}

async function uploadPhoto(req, res) {
  const { id } = req.params;
  if (!req.file) return res.status(400).json({ error: 'No photo uploaded' });

  const photoUrl = await uploadPhotoToStorage(req.file);
  const { rows } = await pool.query(
    `UPDATE householders SET photo_url = $1 WHERE id = $2 AND is_deleted = false RETURNING *`,
    [photoUrl, id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Householder not found' });
  await logAudit(req.user.id, 'UPDATE_PHOTO', 'householder', id, null);
  res.json(rows[0]);
}

async function remove(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(
    `UPDATE householders SET is_deleted = true WHERE id = $1 AND is_deleted = false`,
    [id]
  );
  if (rowCount === 0) return res.status(404).json({ error: 'Householder not found' });
  await logAudit(req.user.id, 'DELETE', 'householder', id, null);
  res.json({ message: 'Deleted' });
}

module.exports = { list, getOne, history, create, update, setPotentialRv, uploadPhoto, remove };
