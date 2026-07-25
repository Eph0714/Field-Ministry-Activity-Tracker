const { Pool, types } = require('pg');

// Keep DATE/TIMESTAMP as plain strings (not JS Date objects) so the Android
// client always receives predictable "YYYY-MM-DD"/"YYYY-MM-DD HH:MM:SS" strings.
const DATE_OID = 1082;
const TIMESTAMP_OID = 1114;
types.setTypeParser(DATE_OID, (val) => val);
types.setTypeParser(TIMESTAMP_OID, (val) => val);

const pool = new Pool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT) || 5432,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  max: 10,
  ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
});

module.exports = pool;
