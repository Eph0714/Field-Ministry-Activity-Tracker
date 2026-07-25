const pool = require('../config/db');

function buildFilters(req, alias, params) {
  const conditions = [`${alias}.is_deleted = false`];
  const { publisher_id, municipality_id, barangay_id, date_from, date_to } = req.query;

  if (publisher_id) {
    params.push(publisher_id);
    conditions.push(`${alias}.publisher_id = $${params.length}`);
  }
  if (municipality_id) {
    params.push(municipality_id);
    conditions.push(`h.municipality_id = $${params.length}`);
  }
  if (barangay_id) {
    params.push(barangay_id);
    conditions.push(`h.barangay_id = $${params.length}`);
  }
  if (date_from) {
    params.push(date_from);
    conditions.push(`${alias}.start_time >= $${params.length}`);
  }
  if (date_to) {
    params.push(date_to);
    conditions.push(`${alias}.start_time <= $${params.length}`);
  }
  return conditions;
}

async function searchingSummary(req, res) {
  const params = [];
  const conditions = buildFilters(req, 's', params);

  const { rows } = await pool.query(
    `SELECT u.id AS publisher_id, u.name AS publisher_name,
            COUNT(*) AS session_count,
            COALESCE(SUM(s.duration_seconds), 0) AS total_seconds
     FROM searching_sessions s
     JOIN householders h ON h.id = s.householder_id
     JOIN users u ON u.id = s.publisher_id
     WHERE ${conditions.join(' AND ')}
     GROUP BY u.id, u.name
     ORDER BY u.name ASC`,
    params
  );
  res.json(rows);
}

async function bibleStudySummary(req, res) {
  const params = [];
  const conditions = buildFilters(req, 'bs', params);

  const { rows } = await pool.query(
    `SELECT u.id AS publisher_id, u.name AS publisher_name,
            COUNT(*) AS study_count,
            COALESCE(SUM(bs.duration_seconds), 0) AS total_seconds
     FROM bible_studies bs
     JOIN householders h ON h.id = bs.householder_id
     JOIN users u ON u.id = bs.publisher_id
     WHERE ${conditions.join(' AND ')}
     GROUP BY u.id, u.name
     ORDER BY u.name ASC`,
    params
  );
  res.json(rows);
}

async function returnVisitSummary(req, res) {
  const params = [];
  // return_visits has no start_time column; filter on visit_datetime instead.
  const conditions = ['rv.is_deleted = false'];
  const { publisher_id, municipality_id, barangay_id, date_from, date_to } = req.query;
  if (publisher_id) { params.push(publisher_id); conditions.push(`rv.publisher_id = $${params.length}`); }
  if (municipality_id) { params.push(municipality_id); conditions.push(`h.municipality_id = $${params.length}`); }
  if (barangay_id) { params.push(barangay_id); conditions.push(`h.barangay_id = $${params.length}`); }
  if (date_from) { params.push(date_from); conditions.push(`rv.visit_datetime >= $${params.length}`); }
  if (date_to) { params.push(date_to); conditions.push(`rv.visit_datetime <= $${params.length}`); }

  const { rows } = await pool.query(
    `SELECT u.id AS publisher_id, u.name AS publisher_name, COUNT(*) AS visit_count
     FROM return_visits rv
     JOIN householders h ON h.id = rv.householder_id
     JOIN users u ON u.id = rv.publisher_id
     WHERE ${conditions.join(' AND ')}
     GROUP BY u.id, u.name
     ORDER BY u.name ASC`,
    params
  );
  res.json(rows);
}

async function potentialReturnVisits(req, res) {
  const conditions = ['h.is_deleted = false', 'h.is_potential_rv = true'];
  const params = [];
  const { municipality_id, barangay_id } = req.query;
  if (municipality_id) { params.push(municipality_id); conditions.push(`h.municipality_id = $${params.length}`); }
  if (barangay_id) { params.push(barangay_id); conditions.push(`h.barangay_id = $${params.length}`); }

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

async function summary(req, res) {
  const srcParams = [];
  const srcConditions = buildFilters(req, 's', srcParams);
  const bsParams = [];
  const bsConditions = buildFilters(req, 'bs', bsParams);

  const rvConditions = ['rv.is_deleted = false'];
  const rvParams = [];
  const { publisher_id, municipality_id, barangay_id, date_from, date_to } = req.query;
  if (publisher_id) { rvParams.push(publisher_id); rvConditions.push(`rv.publisher_id = $${rvParams.length}`); }
  if (municipality_id) { rvParams.push(municipality_id); rvConditions.push(`h.municipality_id = $${rvParams.length}`); }
  if (barangay_id) { rvParams.push(barangay_id); rvConditions.push(`h.barangay_id = $${rvParams.length}`); }
  if (date_from) { rvParams.push(date_from); rvConditions.push(`rv.visit_datetime >= $${rvParams.length}`); }
  if (date_to) { rvParams.push(date_to); rvConditions.push(`rv.visit_datetime <= $${rvParams.length}`); }

  const [srcResult, bsResult, rvResult] = await Promise.all([
    pool.query(
      `SELECT COUNT(*) AS session_count, COALESCE(SUM(s.duration_seconds), 0) AS total_seconds
       FROM searching_sessions s JOIN householders h ON h.id = s.householder_id
       WHERE ${srcConditions.join(' AND ')}`,
      srcParams
    ),
    pool.query(
      `SELECT COUNT(*) AS study_count, COALESCE(SUM(bs.duration_seconds), 0) AS total_seconds
       FROM bible_studies bs JOIN householders h ON h.id = bs.householder_id
       WHERE ${bsConditions.join(' AND ')}`,
      bsParams
    ),
    pool.query(
      `SELECT COUNT(*) AS visit_count
       FROM return_visits rv JOIN householders h ON h.id = rv.householder_id
       WHERE ${rvConditions.join(' AND ')}`,
      rvParams
    ),
  ]);

  res.json({
    searching: srcResult.rows[0],
    bibleStudies: bsResult.rows[0],
    returnVisits: rvResult.rows[0],
  });
}

module.exports = { searchingSummary, bibleStudySummary, returnVisitSummary, potentialReturnVisits, summary };
