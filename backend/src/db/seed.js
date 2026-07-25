require('dotenv').config();
const { Client, types } = require('pg');

types.setTypeParser(1082, (val) => val);
types.setTypeParser(1114, (val) => val);

// Congregation territory (municipalities/barangays) is specific to this
// congregation's assignment — add it via the app's Admin master-list screens
// instead of seeding placeholder data here. This script is left as a template
// for one-off local seeding during development if you want sample data.
const MUNICIPALITIES = [];

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
    if (MUNICIPALITIES.length === 0) {
      console.log('No seed data configured — nothing to do. Add municipalities/barangays via the Admin screens.');
      return;
    }

    for (const m of MUNICIPALITIES) {
      const { rows } = await client.query(
        `INSERT INTO municipalities (name) VALUES ($1)
         ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
         RETURNING id`,
        [m.name]
      );
      const municipalityId = rows[0].id;
      for (const barangayName of m.barangays || []) {
        await client.query(
          `INSERT INTO barangays (municipality_id, name) VALUES ($1, $2)
           ON CONFLICT (municipality_id, name) DO NOTHING`,
          [municipalityId, barangayName]
        );
      }
    }
    console.log('Seed complete.');
  } finally {
    await client.end();
  }
}

main().catch((err) => {
  console.error('Seed failed:', err);
  process.exit(1);
});
