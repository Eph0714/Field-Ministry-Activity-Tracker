const express = require('express');
const ctrl = require('../controllers/auth.controller');
const { requireAuth } = require('../middleware/auth');
const upload = require('../middleware/upload');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.post('/signup', asyncHandler(ctrl.signup));
router.post('/login', asyncHandler(ctrl.login));
router.get('/me', requireAuth, asyncHandler(ctrl.me));
router.post('/change-password', requireAuth, asyncHandler(ctrl.changePassword));
router.post('/photo', requireAuth, upload.single('photo'), asyncHandler(ctrl.uploadPhoto));

module.exports = router;
