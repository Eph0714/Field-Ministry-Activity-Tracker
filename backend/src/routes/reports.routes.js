const express = require('express');
const ctrl = require('../controllers/reports.controller');
const { requireAuth, requireOverseer } = require('../middleware/auth');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/searching-summary', requireAuth, requireOverseer, asyncHandler(ctrl.searchingSummary));
router.get('/bible-study-summary', requireAuth, requireOverseer, asyncHandler(ctrl.bibleStudySummary));
router.get('/return-visit-summary', requireAuth, requireOverseer, asyncHandler(ctrl.returnVisitSummary));
router.get('/potential-return-visits', requireAuth, requireOverseer, asyncHandler(ctrl.potentialReturnVisits));
router.get('/summary', requireAuth, requireOverseer, asyncHandler(ctrl.summary));

module.exports = router;
