const express = require('express');
const ctrl = require('../controllers/householders.controller');
const { requireAuth, requireAdmin } = require('../middleware/auth');
const upload = require('../middleware/upload');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/', requireAuth, asyncHandler(ctrl.list));
router.get('/:id', requireAuth, asyncHandler(ctrl.getOne));
router.get('/:id/history', requireAuth, asyncHandler(ctrl.history));
router.post('/', requireAuth, asyncHandler(ctrl.create));
router.put('/:id', requireAuth, asyncHandler(ctrl.update));
router.put('/:id/potential-rv', requireAuth, asyncHandler(ctrl.setPotentialRv));
router.post('/:id/photo', requireAuth, upload.single('photo'), asyncHandler(ctrl.uploadPhoto));
router.delete('/:id', requireAuth, requireAdmin, asyncHandler(ctrl.remove));

module.exports = router;
