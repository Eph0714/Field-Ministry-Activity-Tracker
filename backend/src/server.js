require('dotenv').config();
const app = require('./app');
const { startAuditLogRetention } = require('./utils/auditRetention');

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Field Ministry Tracker backend listening on port ${PORT}`);
  startAuditLogRetention();
});
