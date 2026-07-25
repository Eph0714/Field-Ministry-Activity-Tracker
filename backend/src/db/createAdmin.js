// One-off script to bootstrap the very first admin account, since signup always
// creates a 'pending' publisher and only an existing admin can approve anyone.
// Usage: node src/db/createAdmin.js "Full Name" email@example.com "password"
require('dotenv').config();
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const { Client, types } = require('pg');

types.setTypeParser(1082, (val) => val);
types.setTypeParser(1114, (val) => val);

async function main() {
  const [name, email, password] = process.argv.slice(2);
  if (!name || !email || !password) {
    console.error('Usage: node src/db/createAdmin.js "Full Name" email@example.com "password"');
    process.exit(1);
  }

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
    const passwordHash = await bcrypt.hash(password, 10);
    const { rows } = await client.query(
      `INSERT INTO users (uuid, name, email, password_hash, role, approval_status)
       VALUES ($1, $2, $3, $4, 'admin', 'approved')
       ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, role = 'admin', approval_status = 'approved'
       RETURNING id, name, email, role`,
      [uuidv4(), name, email, passwordHash]
    );
    console.log('Admin account ready:', rows[0]);
  } finally {
    await client.end();
  }
}

main().catch((err) => {
  console.error('Failed to create admin:', err);
  process.exit(1);
});
