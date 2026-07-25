const crypto = require('crypto');
const path = require('path');
const { createClient } = require('@supabase/supabase-js');

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);
const BUCKET = process.env.SUPABASE_STORAGE_BUCKET || 'photos';

async function uploadPhotoToStorage(file) {
  const ext = path.extname(file.originalname) || '.jpg';
  const objectName = `${Date.now()}-${crypto.randomBytes(6).toString('hex')}${ext}`;

  const { error } = await supabase.storage.from(BUCKET).upload(objectName, file.buffer, {
    contentType: file.mimetype,
    upsert: false,
  });

  if (error) {
    throw new Error(`Photo upload failed: ${error.message}`);
  }

  const { data } = supabase.storage.from(BUCKET).getPublicUrl(objectName);
  return data.publicUrl;
}

module.exports = { uploadPhotoToStorage };
