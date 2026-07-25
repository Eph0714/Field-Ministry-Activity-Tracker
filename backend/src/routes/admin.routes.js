const express = require('express');
const ctrl = require('../controllers/admin.controller');
const { requireAuth, requireAdmin } = require('../middleware/auth');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/audit-logs', requireAuth, requireAdmin, asyncHandler(ctrl.listAuditLogs));
router.delete('/audit-logs', requireAuth, requireAdmin, asyncHandler(ctrl.clearAuditLogs));

module.exports = router;
