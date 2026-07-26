const pool = require('../config/db');
const { logAudit } = require('../utils/audit');

// Regions -----------------------------------------------------------------

async function listRegions(req, res) {
  const { rows } = await pool.query(`SELECT * FROM ph_regions ORDER BY name ASC`);
  res.json(rows);
}

async function createRegion(req, res) {
  const { psgc_code, name, code } = req.body;
  if (!psgc_code || !name) return res.status(400).json({ error: 'psgc_code and name are required' });
  const { rows } = await pool.query(
    `INSERT INTO ph_regions (psgc_code, name, code) VALUES ($1, $2, $3) RETURNING *`,
    [psgc_code, name, code || null]
  );
  await logAudit(req.user.id, 'CREATE', 'ph_region', rows[0].id, { name });
  res.status(201).json(rows[0]);
}

async function updateRegion(req, res) {
  const { id } = req.params;
  const { name, code } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });
  const { rows } = await pool.query(
    `UPDATE ph_regions SET name = $1, code = $2 WHERE id = $3 RETURNING *`,
    [name, code || null, id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Region not found' });
  await logAudit(req.user.id, 'UPDATE', 'ph_region', id, { name });
  res.json(rows[0]);
}

async function deleteRegion(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(`DELETE FROM ph_regions WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'Region not found' });
  await logAudit(req.user.id, 'DELETE', 'ph_region', id, null);
  res.json({ message: 'Deleted' });
}

// Provinces ---------------------------------------------------------------

async function listProvinces(req, res) {
  const { region_id } = req.query;
  if (!region_id) return res.status(400).json({ error: 'region_id is required' });
  const { rows } = await pool.query(
    `SELECT * FROM ph_provinces WHERE region_id = $1 ORDER BY name ASC`,
    [region_id]
  );
  res.json(rows);
}

async function createProvince(req, res) {
  const { region_id, psgc_code, name } = req.body;
  if (!region_id || !psgc_code || !name) {
    return res.status(400).json({ error: 'region_id, psgc_code and name are required' });
  }
  const { rows } = await pool.query(
    `INSERT INTO ph_provinces (region_id, psgc_code, name) VALUES ($1, $2, $3) RETURNING *`,
    [region_id, psgc_code, name]
  );
  await logAudit(req.user.id, 'CREATE', 'ph_province', rows[0].id, { name });
  res.status(201).json(rows[0]);
}

async function updateProvince(req, res) {
  const { id } = req.params;
  const { name } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });
  const { rows } = await pool.query(`UPDATE ph_provinces SET name = $1 WHERE id = $2 RETURNING *`, [name, id]);
  if (rows.length === 0) return res.status(404).json({ error: 'Province not found' });
  await logAudit(req.user.id, 'UPDATE', 'ph_province', id, { name });
  res.json(rows[0]);
}

async function deleteProvince(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(`DELETE FROM ph_provinces WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'Province not found' });
  await logAudit(req.user.id, 'DELETE', 'ph_province', id, null);
  res.json({ message: 'Deleted' });
}

// Municipalities/Cities -----------------------------------------------------

async function listMunicipalities(req, res) {
  const { province_id, search } = req.query;
  if (!province_id) return res.status(400).json({ error: 'province_id is required' });
  const params = [province_id];
  let query = `SELECT * FROM ph_municipalities WHERE province_id = $1`;
  if (search) {
    params.push(`%${search}%`);
    query += ` AND name ILIKE $${params.length}`;
  }
  query += ` ORDER BY name ASC`;
  const { rows } = await pool.query(query, params);
  res.json(rows);
}

async function createMunicipality(req, res) {
  const { province_id, psgc_code, name, type } = req.body;
  if (!province_id || !psgc_code || !name) {
    return res.status(400).json({ error: 'province_id, psgc_code and name are required' });
  }
  const { rows } = await pool.query(
    `INSERT INTO ph_municipalities (province_id, psgc_code, name, type) VALUES ($1, $2, $3, $4) RETURNING *`,
    [province_id, psgc_code, name, type === 'City' ? 'City' : 'Municipality']
  );
  await logAudit(req.user.id, 'CREATE', 'ph_municipality', rows[0].id, { name });
  res.status(201).json(rows[0]);
}

async function updateMunicipality(req, res) {
  const { id } = req.params;
  const { name, type } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });
  const { rows } = await pool.query(
    `UPDATE ph_municipalities SET name = $1, type = $2 WHERE id = $3 RETURNING *`,
    [name, type === 'City' ? 'City' : 'Municipality', id]
  );
  if (rows.length === 0) return res.status(404).json({ error: 'Municipality not found' });
  await logAudit(req.user.id, 'UPDATE', 'ph_municipality', id, { name });
  res.json(rows[0]);
}

async function deleteMunicipality(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(`DELETE FROM ph_municipalities WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'Municipality not found' });
  await logAudit(req.user.id, 'DELETE', 'ph_municipality', id, null);
  res.json({ message: 'Deleted' });
}

// Barangays -----------------------------------------------------------------

async function listBarangays(req, res) {
  const { municipality_id, search } = req.query;
  if (!municipality_id) return res.status(400).json({ error: 'municipality_id is required' });
  const params = [municipality_id];
  let query = `SELECT * FROM ph_barangays WHERE municipality_id = $1`;
  if (search) {
    params.push(`%${search}%`);
    query += ` AND name ILIKE $${params.length}`;
  }
  query += ` ORDER BY name ASC`;
  const { rows } = await pool.query(query, params);
  res.json(rows);
}

async function createBarangay(req, res) {
  const { municipality_id, psgc_code, name } = req.body;
  if (!municipality_id || !psgc_code || !name) {
    return res.status(400).json({ error: 'municipality_id, psgc_code and name are required' });
  }
  const { rows } = await pool.query(
    `INSERT INTO ph_barangays (municipality_id, psgc_code, name) VALUES ($1, $2, $3) RETURNING *`,
    [municipality_id, psgc_code, name]
  );
  await logAudit(req.user.id, 'CREATE', 'ph_barangay', rows[0].id, { name });
  res.status(201).json(rows[0]);
}

async function updateBarangay(req, res) {
  const { id } = req.params;
  const { name } = req.body;
  if (!name) return res.status(400).json({ error: 'name is required' });
  const { rows } = await pool.query(`UPDATE ph_barangays SET name = $1 WHERE id = $2 RETURNING *`, [name, id]);
  if (rows.length === 0) return res.status(404).json({ error: 'Barangay not found' });
  await logAudit(req.user.id, 'UPDATE', 'ph_barangay', id, { name });
  res.json(rows[0]);
}

async function deleteBarangay(req, res) {
  const { id } = req.params;
  const { rowCount } = await pool.query(`DELETE FROM ph_barangays WHERE id = $1`, [id]);
  if (rowCount === 0) return res.status(404).json({ error: 'Barangay not found' });
  await logAudit(req.user.id, 'DELETE', 'ph_barangay', id, null);
  res.json({ message: 'Deleted' });
}

// CSV import (admin bulk update tool) -----------------------------------
// Expected CSV columns per level:
//   regions:       psgc_code,name,code
//   provinces:     psgc_code,parent_psgc_code,name
//   municipalities: psgc_code,parent_psgc_code,name,type
//   barangays:     psgc_code,parent_psgc_code,name

const LEVEL_CONFIG = {
  regions: { table: 'ph_regions', parentTable: null, parentColumn: null },
  provinces: { table: 'ph_provinces', parentTable: 'ph_regions', parentColumn: 'region_id' },
  municipalities: { table: 'ph_municipalities', parentTable: 'ph_provinces', parentColumn: 'province_id' },
  barangays: { table: 'ph_barangays', parentTable: 'ph_municipalities', parentColumn: 'municipality_id' },
};

function parseCsv(text) {
  const lines = text.split(/\r?\n/).filter((l) => l.trim().length > 0);
  const header = lines[0].split(',').map((h) => h.trim());
  return lines.slice(1).map((line) => {
    const cells = line.split(',').map((c) => c.trim());
    const row = {};
    header.forEach((h, i) => (row[h] = cells[i]));
    return row;
  });
}

async function importCsv(req, res) {
  const { level } = req.params;
  const config = LEVEL_CONFIG[level];
  if (!config) return res.status(400).json({ error: 'Invalid level. Use regions, provinces, municipalities, or barangays.' });
  if (!req.file) return res.status(400).json({ error: 'No CSV file uploaded' });

  const rows = parseCsv(req.file.buffer.toString('utf8'));
  let parentIdByPsgc = null;
  if (config.parentTable) {
    const { rows: parents } = await pool.query(`SELECT id, psgc_code FROM ${config.parentTable}`);
    parentIdByPsgc = new Map(parents.map((p) => [p.psgc_code, p.id]));
  }

  let inserted = 0;
  let skipped = 0;
  for (const row of rows) {
    if (!row.psgc_code || !row.name) {
      skipped++;
      continue;
    }
    try {
      if (level === 'regions') {
        await pool.query(
          `INSERT INTO ph_regions (psgc_code, name, code) VALUES ($1, $2, $3)
           ON CONFLICT (psgc_code) DO UPDATE SET name = EXCLUDED.name, code = EXCLUDED.code`,
          [row.psgc_code, row.name, row.code || null]
        );
      } else {
        const parentId = parentIdByPsgc.get(row.parent_psgc_code);
        if (!parentId) {
          skipped++;
          continue;
        }
        if (level === 'provinces') {
          await pool.query(
            `INSERT INTO ph_provinces (psgc_code, region_id, name) VALUES ($1, $2, $3)
             ON CONFLICT (psgc_code) DO UPDATE SET name = EXCLUDED.name, region_id = EXCLUDED.region_id`,
            [row.psgc_code, parentId, row.name]
          );
        } else if (level === 'municipalities') {
          await pool.query(
            `INSERT INTO ph_municipalities (psgc_code, province_id, name, type) VALUES ($1, $2, $3, $4)
             ON CONFLICT (psgc_code) DO UPDATE SET name = EXCLUDED.name, province_id = EXCLUDED.province_id, type = EXCLUDED.type`,
            [row.psgc_code, parentId, row.name, row.type === 'City' ? 'City' : 'Municipality']
          );
        } else if (level === 'barangays') {
          await pool.query(
            `INSERT INTO ph_barangays (psgc_code, municipality_id, name) VALUES ($1, $2, $3)
             ON CONFLICT (psgc_code) DO UPDATE SET name = EXCLUDED.name, municipality_id = EXCLUDED.municipality_id`,
            [row.psgc_code, parentId, row.name]
          );
        }
      }
      inserted++;
    } catch (e) {
      skipped++;
    }
  }

  await logAudit(req.user.id, 'IMPORT_CSV', `ph_${level}`, null, { inserted, skipped });
  res.json({ message: 'Import complete', inserted, skipped, totalRows: rows.length });
}

module.exports = {
  listRegions, createRegion, updateRegion, deleteRegion,
  listProvinces, createProvince, updateProvince, deleteProvince,
  listMunicipalities, createMunicipality, updateMunicipality, deleteMunicipality,
  listBarangays, createBarangay, updateBarangay, deleteBarangay,
  importCsv,
};
