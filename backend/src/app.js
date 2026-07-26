const express = require('express');
const cors = require('cors');

const authRoutes = require('./routes/auth.routes');
const municipalitiesRoutes = require('./routes/municipalities.routes');
const barangaysRoutes = require('./routes/barangays.routes');
const usersRoutes = require('./routes/users.routes');
const householdersRoutes = require('./routes/householders.routes');
const searchingRoutes = require('./routes/searching.routes');
const bibleStudiesRoutes = require('./routes/bibleStudies.routes');
const returnVisitsRoutes = require('./routes/returnVisits.routes');
const reportsRoutes = require('./routes/reports.routes');
const adminRoutes = require('./routes/admin.routes');
const geoRoutes = require('./routes/geo.routes');
const errorHandler = require('./middleware/errorHandler');

const app = express();

app.set('trust proxy', true);
app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.use('/api/auth', authRoutes);
app.use('/api/municipalities', municipalitiesRoutes);
app.use('/api/barangays', barangaysRoutes);
app.use('/api/users', usersRoutes);
app.use('/api/householders', householdersRoutes);
app.use('/api/searching', searchingRoutes);
app.use('/api/bible-studies', bibleStudiesRoutes);
app.use('/api/return-visits', returnVisitsRoutes);
app.use('/api/reports', reportsRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/geo', geoRoutes);

app.use((req, res) => res.status(404).json({ error: 'Not found' }));
app.use(errorHandler);

module.exports = app;
