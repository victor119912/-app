const express = require('express');
const cors = require('cors');
require('dotenv').config();

const pool = require('./config/db');
const eventsRoute = require('./routes/events');
const remindersRoute = require('./routes/reminders');
const statsRoute = require('./routes/stats');

const app = express();

app.use(cors());
app.use(express.json());

app.get('/', (_req, res) => {
  res.json({
    name: '售票資訊整合 API',
    status: 'running',
    endpoints: [
      '/api/events',
      '/api/events/meta',
      '/api/reminders',
      '/api/stats/summary',
      '/api/stats/price',
      '/api/stats/time',
      '/api/stats/venue'
    ]
  });
});

app.get('/api/health', async (_req, res) => {
  try {
    const [[row]] = await pool.query('SELECT 1 AS ok');
    res.json({ ok: row.ok === 1 });
  } catch (err) {
    res.status(500).json({ ok: false, message: err.message });
  }
});

app.use('/api/events', eventsRoute);
app.use('/api/reminders', remindersRoute);
app.use('/api/stats', statsRoute);

app.use((req, res) => {
  res.status(404).json({ error: 'not_found', path: req.path });
});

const PORT = Number(process.env.PORT || 4000);
app.listen(PORT, () => {
  console.log(`售票資訊整合 API running on http://localhost:${PORT}`);
});
