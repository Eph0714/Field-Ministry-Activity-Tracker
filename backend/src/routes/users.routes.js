const express = require('express');
const ctrl = require('../controllers/users.controller');
const { requireAuth, requireAdmin } = require('../middleware/auth');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/', requireAuth, requireAdmin, asyncHandler(ctrl.list));
router.get('/pending-signups', requireAuth, requireAdmin, asyncHandler(ctrl.pendingSignups));
router.post('/:id/approve-signup', requireAuth, requireAdmin, asyncHandler(ctrl.approveSignup));
router.post('/:id/reject-signup', requireAuth, requireAdmin, asyncHandler(ctrl.rejectSignup));
router.post('/', requireAuth, requireAdmin, asyncHandler(ctrl.create));
router.put('/:id', requireAuth, requireAdmin, asyncHandler(ctrl.update));
router.delete('/:id', requireAuth, requireAdmin, asyncHandler(ctrl.remove));

module.exports = router;
