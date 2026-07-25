require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { Client, types } = require('pg');

types.setTypeParser(1082, (val) => val);
types.setTypeParser(1114, (val) => val);

// Add future new columns here so an already-deployed DB picks them up too —
// CREATE TABLE IF NOT EXISTS above can't retrofit columns onto an existing table.
const NEW_COLUMNS = [
  // { table: 'householders', column: 'some_new_field', definition: 'TEXT' },
];

async function ensureColumns(client) {
  for (const { table, column, definition } of NEW_COLUMNS) {
    const { rows } = await client.query(
      `SELECT 1 FROM information_schema.columns WHERE table_name = $1 AND column_name = $2`,
      [table, column]
    );
    if (rows.length === 0) {
      console.log(`Adding column ${table}.${column}`);
      await client.query(`ALTER TABLE ${table} ADD COLUMN ${column} ${definition}`);
    }
  }
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
    const schemaSql = fs.readFileSync(path.join(__dirname, 'schema.pg.sql'), 'utf8');
    console.log('Running schema.pg.sql...');
    await client.query(schemaSql);

    console.log('Checking for new columns on existing tables...');
    await ensureColumns(client);

    console.log('Migration complete.');
  } finally {
    await client.end();
  }
}

main().catch((err) => {
  console.error('Migration failed:', err);
  process.exit(1);
});
