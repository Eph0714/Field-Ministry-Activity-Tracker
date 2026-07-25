const express = require('express');
const ctrl = require('../controllers/barangays.controller');
const { requireAuth, requireAdmin } = require('../middleware/auth');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/', requireAuth, asyncHandler(ctrl.list));
router.post('/', requireAuth, requireAdmin, asyncHandler(ctrl.create));
router.put('/:id', requireAuth, requireAdmin, asyncHandler(ctrl.update));
router.delete('/:id', requireAuth, requireAdmin, asyncHandler(ctrl.remove));

module.exports = router;
