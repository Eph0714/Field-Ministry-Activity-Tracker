// One-off script to populate the national PH address reference tables
// (ph_regions/ph_provinces/ph_municipalities/ph_barangays) from a public,
// well-established PSGC-derived dataset. Run once against the live DB
// (like migrate.js/seed.js) - NOT invoked from the app, since this is a
// single shared database, not a per-device local one.
//
// Usage: node src/db/seedPhAddress.js
require('dotenv').config();
const https = require('https');
const { Client, types } = require('pg');

types.setTypeParser(1082, (val) => val);
types.setTypeParser(1114, (val) => val);

const BASE_URL = 'https://raw.githubusercontent.com/isaacdarcilla/philippine-addresses/master';

const LOWERCASE_WORDS = new Set(['of', 'del', 'dela', 'de', 'la', 'las', 'los', 'y', 'sa', 'ng', 'and', 'the']);

function titleCase(raw) {
  const words = raw.trim().toLowerCase().split(/\s+/);
  const result = words.map((w, i) => {
    if (i > 0 && LOWERCASE_WORDS.has(w)) return w;
    return w.charAt(0).toUpperCase() + w.slice(1);
  });
  return result.join(' ').replace(/\bNcr\b/g, 'NCR');
}

function fetchJson(path) {
  return new Promise((resolve, reject) => {
    https
      .get(`${BASE_URL}/${path}`, (res) => {
        if (res.statusCode !== 200) {
          reject(new Error(`Failed to fetch ${path}: HTTP ${res.statusCode}`));
          return;
        }
        let data = '';
        res.on('data', (chunk) => (data += chunk));
        res.on('end', () => {
          try {
            resolve(JSON.parse(data));
          } catch (e) {
            reject(e);
          }
        });
      })
      .on('error', reject);
  });
}

async function batchInsert(client, table, columns, rows, conflictColumn) {
  const CHUNK_SIZE = 500;
  let inserted = 0;
  for (let i = 0; i < rows.length; i += CHUNK_SIZE) {
    const chunk = rows.slice(i, i + CHUNK_SIZE);
    const values = [];
    const placeholders = chunk.map((row, rowIndex) => {
      const base = rowIndex * columns.length;
      row.forEach((v) => values.push(v));
      const params = columns.map((_, colIndex) => '$' + (base + colIndex + 1));
      return '(' + params.join(', ') + ')';
    });
    const sql = `INSERT INTO ${table} (${columns.join(', ')}) VALUES ${placeholders.join(', ')}
                 ON CONFLICT (${conflictColumn}) DO NOTHING`;
    const result = await client.query(sql, values);
    inserted += result.rowCount;
  }
  return inserted;
}

async function main() {
  const client = new Client({
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT) || 5432,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
  });

  await client.connect();
  try {
    console.log('Fetching source dataset...');
    const [regions, provinces, cities, barangays] = await Promise.all([
      fetchJson('region.json'),
      fetchJson('province.json'),
      fetchJson('city.json'),
      fetchJson('barangay.json'),
    ]);
    console.log(`Fetched: ${regions.length} region rows, ${provinces.length} province rows, ${cities.length} city rows, ${barangays.length} barangay rows`);

    // Regions ------------------------------------------------------------
    const regionRows = dedupeBy(regions, (r) => r.psgc_code).map((r) => [r.psgc_code, titleCase(r.region_name), r.region_code]);
    const regionsInserted = await batchInsert(client, 'ph_regions', ['psgc_code', 'name', 'code'], regionRows, 'psgc_code');
    console.log(`Regions inserted: ${regionsInserted} (of ${regionRows.length} unique source rows)`);

    const regionIdByCode = await loadCodeMap(client, 'ph_regions', 'code', 'id');

    // Provinces (includes NCR's 4 districts, modeled as pseudo-provinces
    // since NCR has no true provinces but cities still need an intermediate
    // parent to fit the Region -> Province -> Municipality chain) ----------
    const provinceRows = dedupeBy(provinces, (p) => p.psgc_code)
      .filter((p) => regionIdByCode.has(p.region_code))
      .map((p) => [p.psgc_code, regionIdByCode.get(p.region_code), titleCase(p.province_name), p.province_code]);
    const provincesInserted = await batchInsert(
      client,
      'ph_provinces',
      ['psgc_code', 'region_id', 'name'],
      provinceRows.map(([psgc, regionId, name]) => [psgc, regionId, name]),
      'psgc_code'
    );
    console.log(`Provinces inserted: ${provincesInserted} (of ${provinceRows.length} unique source rows)`);

    const provinceIdByCode = await loadProvinceCodeMap(client, provinces);

    // Municipalities/Cities ------------------------------------------------
    const cityRows = dedupeBy(cities, (c) => c.psgc_code)
      .filter((c) => provinceIdByCode.has(c.province_code))
      .map((c) => {
        const name = titleCase(c.city_name);
        const type = /city/i.test(c.city_name) ? 'City' : 'Municipality';
        return [c.psgc_code, provinceIdByCode.get(c.province_code), name, type, c.city_code];
      });
    const citiesInserted = await batchInsert(
      client,
      'ph_municipalities',
      ['psgc_code', 'province_id', 'name', 'type'],
      cityRows.map(([psgc, provinceId, name, type]) => [psgc, provinceId, name, type]),
      'psgc_code'
    );
    console.log(`Municipalities/Cities inserted: ${citiesInserted} (of ${cityRows.length} unique source rows)`);

    const { rows: municipalityRows } = await client.query('SELECT id, psgc_code FROM ph_municipalities');
    const municipalityIdByPsgc = new Map(municipalityRows.map((r) => [r.psgc_code, r.id]));
    const cityCodeToPsgc = new Map(cities.map((c) => [c.city_code, c.psgc_code]));

    // Barangays --------------------------------------------------------
    const barangayRows = dedupeBy(barangays, (b) => b.brgy_code)
      .map((b) => {
        const municipalityPsgc = cityCodeToPsgc.get(b.city_code);
        const municipalityId = municipalityPsgc ? municipalityIdByPsgc.get(municipalityPsgc) : null;
        if (!municipalityId) return null;
        return [b.brgy_code, municipalityId, titleCase(b.brgy_name)];
      })
      .filter(Boolean);
    const barangaysInserted = await batchInsert(
      client,
      'ph_barangays',
      ['psgc_code', 'municipality_id', 'name'],
      barangayRows,
      'psgc_code'
    );
    console.log(`Barangays inserted: ${barangaysInserted} (of ${barangayRows.length} unique, resolvable source rows)`);

    const counts = await client.query(`
      SELECT
        (SELECT COUNT(*) FROM ph_regions) AS regions,
        (SELECT COUNT(*) FROM ph_provinces) AS provinces,
        (SELECT COUNT(*) FROM ph_municipalities) AS municipalities,
        (SELECT COUNT(*) FROM ph_barangays) AS barangays
    `);
    console.log('Final DB counts:', counts.rows[0]);
    console.log('Official PSA reference totals (approx, may vary slightly by PSGC vintage): 17-18 regions, ~82 true provinces (+NCR districts), ~1,634 cities/municipalities, ~42,046 barangays.');
  } finally {
    await client.end();
  }
}

function dedupeBy(arr, keyFn) {
  const seen = new Set();
  const result = [];
  for (const item of arr) {
    const key = keyFn(item);
    if (!key || seen.has(key)) continue;
    seen.add(key);
    result.push(item);
  }
  return result;
}

async function loadCodeMap(client, table, codeColumn, idColumn) {
  const { rows } = await client.query(`SELECT ${idColumn} AS id, ${codeColumn} AS code FROM ${table}`);
  return new Map(rows.map((r) => [r.code, r.id]));
}

async function loadProvinceCodeMap(client, provinces) {
  // province_code (source dataset key, e.g. "0128") -> ph_provinces.id, resolved via psgc_code
  const psgcByProvinceCode = new Map(provinces.map((p) => [p.province_code, p.psgc_code]));
  const { rows } = await client.query('SELECT id, psgc_code FROM ph_provinces');
  const idByPsgc = new Map(rows.map((r) => [r.psgc_code, r.id]));
  const result = new Map();
  for (const [provinceCode, psgc] of psgcByProvinceCode) {
    if (idByPsgc.has(psgc)) result.set(provinceCode, idByPsgc.get(psgc));
  }
  return result;
}

main().catch((err) => {
  console.error('PH address seed failed:', err);
  process.exit(1);
});
