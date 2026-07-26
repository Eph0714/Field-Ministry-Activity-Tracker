-- Field Ministry Activity Tracker — Postgres (Supabase) schema
-- Safe to re-run: every CREATE uses IF NOT EXISTS. Column/constraint changes to
-- already-deployed tables are handled by migrate.js's ensureColumns() step, not here.

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Reference data (congregation territory) -----------------------------------

CREATE TABLE IF NOT EXISTS municipalities (
  id SERIAL PRIMARY KEY,
  name VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS barangays (
  id SERIAL PRIMARY KEY,
  municipality_id INT NOT NULL REFERENCES municipalities(id) ON DELETE CASCADE,
  name VARCHAR(150) NOT NULL,
  UNIQUE (municipality_id, name)
);

CREATE INDEX IF NOT EXISTS idx_barangay_municipality ON barangays(municipality_id);

-- Users -----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'publisher' CHECK (role IN ('publisher', 'overseer', 'admin')),
  photo_url TEXT,
  is_active BOOLEAN NOT NULL DEFAULT true,
  approval_status VARCHAR(20) NOT NULL DEFAULT 'approved' CHECK (approval_status IN ('pending', 'approved', 'rejected')),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Householders (master profile) ------------------------------------------

CREATE TABLE IF NOT EXISTS householders (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  address TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  photo_url TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'Potential' CHECK (status IN ('BS', 'RV', 'Potential')),
  topic TEXT,
  remarks TEXT,
  municipality_id INT REFERENCES municipalities(id) ON DELETE SET NULL,
  barangay_id INT REFERENCES barangays(id) ON DELETE SET NULL,
  is_potential_rv BOOLEAN NOT NULL DEFAULT false,
  created_by INT REFERENCES users(id) ON DELETE SET NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_householder_municipality ON householders(municipality_id);
CREATE INDEX IF NOT EXISTS idx_householder_barangay ON householders(barangay_id);
CREATE INDEX IF NOT EXISTS idx_householder_updated_at ON householders(updated_at);
CREATE INDEX IF NOT EXISTS idx_householder_potential_rv ON householders(is_potential_rv) WHERE is_potential_rv = true;

DROP TRIGGER IF EXISTS trg_householders_updated_at ON householders;
CREATE TRIGGER trg_householders_updated_at BEFORE UPDATE ON householders
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Searching (SRC) sessions -------------------------------------------------

CREATE TABLE IF NOT EXISTS searching_sessions (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  householder_id INT NOT NULL REFERENCES householders(id) ON DELETE CASCADE,
  publisher_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  language_spoken VARCHAR(100),
  preferred_language VARCHAR(100),
  marital_status VARCHAR(30),
  age INT,
  contact_number VARCHAR(30),
  remarks TEXT,
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  duration_seconds INT NOT NULL DEFAULT 0,
  is_deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_searching_householder ON searching_sessions(householder_id);
CREATE INDEX IF NOT EXISTS idx_searching_publisher ON searching_sessions(publisher_id);
CREATE INDEX IF NOT EXISTS idx_searching_updated_at ON searching_sessions(updated_at);
CREATE INDEX IF NOT EXISTS idx_searching_start_time ON searching_sessions(start_time);

DROP TRIGGER IF EXISTS trg_searching_updated_at ON searching_sessions;
CREATE TRIGGER trg_searching_updated_at BEFORE UPDATE ON searching_sessions
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Bible Study (BS) sessions -------------------------------------------------

CREATE TABLE IF NOT EXISTS bible_studies (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  householder_id INT NOT NULL REFERENCES householders(id) ON DELETE CASCADE,
  publisher_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  publication VARCHAR(150),
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  duration_seconds INT NOT NULL DEFAULT 0,
  is_deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bs_householder ON bible_studies(householder_id);
CREATE INDEX IF NOT EXISTS idx_bs_publisher ON bible_studies(publisher_id);
CREATE INDEX IF NOT EXISTS idx_bs_updated_at ON bible_studies(updated_at);
CREATE INDEX IF NOT EXISTS idx_bs_start_time ON bible_studies(start_time);

DROP TRIGGER IF EXISTS trg_bs_updated_at ON bible_studies;
CREATE TRIGGER trg_bs_updated_at BEFORE UPDATE ON bible_studies
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Return Visits (RV) ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS return_visits (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  householder_id INT NOT NULL REFERENCES householders(id) ON DELETE CASCADE,
  publisher_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  visit_datetime TIMESTAMP NOT NULL DEFAULT NOW(),
  outcome_notes TEXT,
  is_deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rv_householder ON return_visits(householder_id);
CREATE INDEX IF NOT EXISTS idx_rv_publisher ON return_visits(publisher_id);
CREATE INDEX IF NOT EXISTS idx_rv_updated_at ON return_visits(updated_at);
CREATE INDEX IF NOT EXISTS idx_rv_visit_datetime ON return_visits(visit_datetime);

DROP TRIGGER IF EXISTS trg_rv_updated_at ON return_visits;
CREATE TRIGGER trg_rv_updated_at BEFORE UPDATE ON return_visits
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Audit log -------------------------------------------------------------

CREATE TABLE IF NOT EXISTS audit_logs (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE SET NULL,
  action VARCHAR(50) NOT NULL,
  entity_type VARCHAR(50),
  entity_id INT,
  details TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);

-- Philippine national address reference data (PSGC) ------------------------
-- Separate from municipalities/barangays above (this congregation's small,
-- hand-managed territory list) - these four tables hold the full national
-- Region/Province/Municipality/Barangay hierarchy, used only for user
-- registration/profile addresses.

CREATE TABLE IF NOT EXISTS ph_regions (
  id SERIAL PRIMARY KEY,
  psgc_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS ph_provinces (
  id SERIAL PRIMARY KEY,
  region_id INT NOT NULL REFERENCES ph_regions(id) ON DELETE CASCADE,
  psgc_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ph_province_region ON ph_provinces(region_id);

CREATE TABLE IF NOT EXISTS ph_municipalities (
  id SERIAL PRIMARY KEY,
  province_id INT NOT NULL REFERENCES ph_provinces(id) ON DELETE CASCADE,
  psgc_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  type VARCHAR(20) NOT NULL DEFAULT 'Municipality' CHECK (type IN ('City', 'Municipality'))
);

CREATE INDEX IF NOT EXISTS idx_ph_municipality_province ON ph_municipalities(province_id);
CREATE INDEX IF NOT EXISTS idx_ph_municipality_name ON ph_municipalities(name);

CREATE TABLE IF NOT EXISTS ph_barangays (
  id SERIAL PRIMARY KEY,
  municipality_id INT NOT NULL REFERENCES ph_municipalities(id) ON DELETE CASCADE,
  psgc_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ph_barangay_municipality ON ph_barangays(municipality_id);
CREATE INDEX IF NOT EXISTS idx_ph_barangay_name ON ph_barangays(name);
