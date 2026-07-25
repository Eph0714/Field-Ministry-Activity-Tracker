const express = require('express');
const ctrl = require('../controllers/bibleStudies.controller');
const { requireAuth } = require('../middleware/auth');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/', requireAuth, asyncHandler(ctrl.list));
router.post('/', requireAuth, asyncHandler(ctrl.create));
router.put('/:id', requireAuth, asyncHandler(ctrl.update));
router.delete('/:id', requireAuth, asyncHandler(ctrl.remove));

module.exports = router;
