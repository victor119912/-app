const express = require('express');
const pool = require('../config/db');

const router = express.Router();

const eventFields = `
  event_id AS id,
  COALESCE(NULLIF(\`活動名稱\`, ''), '未命名活動') AS title,
  COALESCE(\`藝人\`, '') AS artist,
  COALESCE(\`搶票時間\`, '') AS saleTime,
  COALESCE(\`活動時間\`, '') AS activityTime,
  COALESCE(\`活動地點\`, '') AS venue,
  COALESCE(\`活動地址\`, '') AS address,
  COALESCE(\`票價\`, '') AS price,
  COALESCE(\`票種\`, '') AS ticketType,
  COALESCE(\`網址\`, '') AS url,
  COALESCE(\`來源網站\`, '') AS source,
  COALESCE(\`資料來源檔\`, '') AS sourceFile,
  created_at AS createdAt
`;

function buildEventWhere(query) {
  const conditions = [];
  const params = {};
  const keyword = String(query.keyword || query.q || '').trim();
  const venue = String(query.venue || query.location || '').trim();
  const source = String(query.source || '').trim();
  const startDate = String(query.startDate || '').trim();
  const endDate = String(query.endDate || '').trim();
  const featured = String(query.featured || '').trim() === '1';

  if (featured) {
    conditions.push(`(
      \`活動名稱\` IS NOT NULL AND
      \`活動名稱\` <> '' AND
      \`活動名稱\` NOT LIKE '%Tickets in Japan%' AND
      (
        \`活動時間\` REGEXP '20[0-9]{2}' OR
        \`搶票時間\` REGEXP '20[0-9]{2}' OR
        (\`活動地點\` IS NOT NULL AND \`活動地點\` <> '' AND \`活動地點\` <> '未提供')
      )
    )`);
  }

  if (keyword) {
    conditions.push(`(
      \`活動名稱\` LIKE :keyword OR
      \`藝人\` LIKE :keyword OR
      \`活動地點\` LIKE :keyword OR
      \`活動地址\` LIKE :keyword OR
      \`票價\` LIKE :keyword OR
      \`票種\` LIKE :keyword OR
      \`來源網站\` LIKE :keyword
    )`);
    params.keyword = `%${keyword}%`;
  }

  if (venue && venue !== '全部') {
    conditions.push(`(\`活動地點\` LIKE :venue OR \`活動地址\` LIKE :venue)`);
    params.venue = `%${venue}%`;
  }

  if (source && source !== '全部') {
    conditions.push(`\`來源網站\` LIKE :source`);
    params.source = `%${source}%`;
  }

  if (startDate && endDate) {
    conditions.push(`
      STR_TO_DATE(
        REPLACE(REGEXP_SUBSTR(\`活動時間\`, '[0-9]{4}[./-][0-9]{1,2}[./-][0-9]{1,2}'), '.', '-'),
        '%Y-%m-%d'
      ) BETWEEN :startDate AND :endDate
    `);
    params.startDate = startDate;
    params.endDate = endDate;
  }

  return {
    whereSql: conditions.length ? `WHERE ${conditions.join(' AND ')}` : '',
    params
  };
}

router.get('/', async (req, res) => {
  try {
    const limit = Math.max(Math.min(Number(req.query.limit || 100), 300), 1);
    const offset = Math.max(Number(req.query.offset || 0), 0);
    const { whereSql, params } = buildEventWhere(req.query);

    const [rows] = await pool.execute(
      `
      SELECT ${eventFields}
      FROM events
      ${whereSql}
      ORDER BY created_at DESC, event_id DESC
      LIMIT ${limit} OFFSET ${offset}
      `,
      params
    );

    const [[countRow]] = await pool.execute(
      `SELECT COUNT(*) AS total FROM events ${whereSql}`,
      params
    );

    res.json({
      total: Number(countRow.total || 0),
      items: rows
    });
  } catch (err) {
    console.error('GET /api/events error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.get('/meta', async (_req, res) => {
  try {
    const [venues] = await pool.query(`
      SELECT \`活動地點\` AS venue, COUNT(*) AS total
      FROM events
      WHERE \`活動地點\` IS NOT NULL AND \`活動地點\` <> ''
      GROUP BY \`活動地點\`
      ORDER BY total DESC
      LIMIT 30
    `);

    const [sources] = await pool.query(`
      SELECT \`來源網站\` AS source, COUNT(*) AS total
      FROM events
      WHERE \`來源網站\` IS NOT NULL AND \`來源網站\` <> ''
      GROUP BY \`來源網站\`
      ORDER BY total DESC
      LIMIT 20
    `);

    res.json({ venues, sources });
  } catch (err) {
    console.error('GET /api/events/meta error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const [rows] = await pool.execute(
      `SELECT ${eventFields} FROM events WHERE event_id = :id LIMIT 1`,
      { id: Number(req.params.id) }
    );

    if (!rows.length) {
      return res.status(404).json({ error: 'not_found' });
    }

    res.json(rows[0]);
  } catch (err) {
    console.error('GET /api/events/:id error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

module.exports = router;
