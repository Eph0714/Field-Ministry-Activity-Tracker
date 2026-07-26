const express = require('express');
const ctrl = require('../controllers/geo.controller');
const { requireAuth, requireAdmin } = require('../middleware/auth');
const upload = require('../middleware/upload');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

// Public (no auth) - these must be usable from the Sign Up form, before the
// user has an account/token.
router.get('/regions', asyncHandler(ctrl.listRegions));
router.get('/provinces', asyncHandler(ctrl.listProvinces));
router.get('/municipalities', asyncHandler(ctrl.listMunicipalities));
router.get('/barangays', asyncHandler(ctrl.listBarangays));

// Admin-only management
router.post('/regions', requireAuth, requireAdmin, asyncHandler(ctrl.createRegion));
router.put('/regions/:id', requireAuth, requireAdmin, asyncHandler(ctrl.updateRegion));
router.delete('/regions/:id', requireAuth, requireAdmin, asyncHandler(ctrl.deleteRegion));

router.post('/provinces', requireAuth, requireAdmin, asyncHandler(ctrl.createProvince));
router.put('/provinces/:id', requireAuth, requireAdmin, asyncHandler(ctrl.updateProvince));
router.delete('/provinces/:id', requireAuth, requireAdmin, asyncHandler(ctrl.deleteProvince));

router.post('/municipalities', requireAuth, requireAdmin, asyncHandler(ctrl.createMunicipality));
router.put('/municipalities/:id', requireAuth, requireAdmin, asyncHandler(ctrl.updateMunicipality));
router.delete('/municipalities/:id', requireAuth, requireAdmin, asyncHandler(ctrl.deleteMunicipality));

router.post('/barangays', requireAuth, requireAdmin, asyncHandler(ctrl.createBarangay));
router.put('/barangays/:id', requireAuth, requireAdmin, asyncHandler(ctrl.updateBarangay));
router.delete('/barangays/:id', requireAuth, requireAdmin, asyncHandler(ctrl.deleteBarangay));

router.post('/import/:level', requireAuth, requireAdmin, upload.uploadCsv.single('file'), asyncHandler(ctrl.importCsv));

module.exports = router;
