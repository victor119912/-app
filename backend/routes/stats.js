const express = require('express');
const pool = require('../config/db');

const router = express.Router();

function extractPrices(priceText) {
  if (!priceText) return [];
  return Array.from(String(priceText).replace(/,/g, '').matchAll(/\d+/g))
    .map(match => Number(match[0]))
    .filter(value => Number.isFinite(value));
}

function firstMonth(activityTime) {
  const match = String(activityTime || '').match(/(\d{4})[./-](\d{1,2})/);
  if (!match) return null;
  return `${match[1]}.${match[2].padStart(2, '0')}`;
}

router.get('/summary', async (_req, res) => {
  try {
    const [[events]] = await pool.query('SELECT COUNT(*) AS total FROM events');
    const [[artists]] = await pool.query('SELECT COUNT(*) AS total FROM artists');
    const [[venues]] = await pool.query('SELECT COUNT(*) AS total FROM venues');
    const [[reminders]] = await pool.query('SELECT COUNT(*) AS total FROM reminders');
    const [latest] = await pool.query(`
      SELECT event_id AS id, \`活動名稱\` AS title, \`活動時間\` AS activityTime, \`活動地點\` AS venue
      FROM events
      ORDER BY created_at DESC, event_id DESC
      LIMIT 5
    `);

    res.json({
      events: Number(events.total || 0),
      artists: Number(artists.total || 0),
      venues: Number(venues.total || 0),
      reminders: Number(reminders.total || 0),
      latest
    });
  } catch (err) {
    console.error('GET /api/stats/summary error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.get('/price', async (_req, res) => {
  try {
    const [rows] = await pool.query(`
      SELECT event_id AS id, \`活動名稱\` AS title, \`票價\` AS price
      FROM events
      WHERE \`票價\` IS NOT NULL AND \`票價\` <> ''
    `);

    const buckets = {
      freeOrUnknown: 0,
      under1000: 0,
      between1000And3000: 0,
      between3000And6000: 0,
      over6000: 0
    };

    const events = rows.map(row => {
      const prices = extractPrices(row.price);
      if (!prices.length) {
        buckets.freeOrUnknown += 1;
        return { ...row, minPrice: null, maxPrice: null };
      }

      const minPrice = Math.min(...prices);
      const maxPrice = Math.max(...prices);
      if (maxPrice < 1000) buckets.under1000 += 1;
      else if (maxPrice < 3000) buckets.between1000And3000 += 1;
      else if (maxPrice < 6000) buckets.between3000And6000 += 1;
      else buckets.over6000 += 1;

      return { ...row, minPrice, maxPrice };
    });

    const priced = events.filter(item => item.maxPrice !== null);
    const averageMaxPrice = priced.length
      ? Math.round(priced.reduce((sum, item) => sum + item.maxPrice, 0) / priced.length)
      : 0;

    res.json({
      total: rows.length,
      priced: priced.length,
      averageMaxPrice,
      buckets,
      topExpensive: priced
        .sort((a, b) => b.maxPrice - a.maxPrice)
        .slice(0, 10)
    });
  } catch (err) {
    console.error('GET /api/stats/price error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.get('/time', async (_req, res) => {
  try {
    const [rows] = await pool.query(`
      SELECT \`活動時間\` AS activityTime
      FROM events
      WHERE \`活動時間\` IS NOT NULL AND \`活動時間\` <> ''
    `);

    const monthMap = new Map();
    for (const row of rows) {
      const month = firstMonth(row.activityTime);
      if (month) monthMap.set(month, (monthMap.get(month) || 0) + 1);
    }

    const months = Array.from(monthMap.entries())
      .map(([month, total]) => ({ month, total }))
      .sort((a, b) => a.month.localeCompare(b.month));

    const busiestMonths = [...months]
      .sort((a, b) => b.total - a.total)
      .slice(0, 6);

    res.json({
      total: rows.length,
      months,
      busiestMonths
    });
  } catch (err) {
    console.error('GET /api/stats/time error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.get('/venue', async (_req, res) => {
  try {
    const [rows] = await pool.query(`
      SELECT COALESCE(NULLIF(\`活動地點\`, ''), '未提供場地') AS venue, COUNT(*) AS total
      FROM events
      GROUP BY COALESCE(NULLIF(\`活動地點\`, ''), '未提供場地')
      ORDER BY total DESC
      LIMIT 15
    `);

    res.json({ venues: rows });
  } catch (err) {
    console.error('GET /api/stats/venue error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

module.exports = router;
