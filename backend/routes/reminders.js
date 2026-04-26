const express = require('express');
const pool = require('../config/db');

const router = express.Router();

async function getDemoUserId() {
  await pool.execute(
    `
    INSERT IGNORE INTO users (email, password_hash)
    VALUES ('demo@ticket-app.local', 'demo-only')
    `
  );

  const [[user]] = await pool.execute(
    `SELECT id FROM users WHERE email = 'demo@ticket-app.local' LIMIT 1`
  );

  return user.id;
}

router.get('/', async (_req, res) => {
  try {
    const userId = await getDemoUserId();
    const [rows] = await pool.execute(
      `
      SELECT
        id,
        user_id AS userId,
        title,
        sale_at AS saleAt,
        offsets_minutes AS offsetsMinutes,
        enabled,
        created_at AS createdAt,
        updated_at AS updatedAt
      FROM reminders
      WHERE user_id = :userId
      ORDER BY sale_at ASC, id DESC
      `,
      { userId }
    );

    res.json(rows);
  } catch (err) {
    console.error('GET /api/reminders error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.post('/', async (req, res) => {
  try {
    const userId = await getDemoUserId();
    const { title, saleAt, sale_at, offsetsMinutes, offsets_minutes, enabled } = req.body;
    const finalTitle = String(title || '').trim();
    const finalSaleAt = String(saleAt || sale_at || '').trim();
    const finalOffsets = offsetsMinutes ?? offsets_minutes ?? [30];

    if (!finalTitle || !finalSaleAt) {
      return res.status(400).json({
        error: 'missing_fields',
        need: ['title', 'saleAt']
      });
    }

    const offsetsJson = typeof finalOffsets === 'string'
      ? finalOffsets
      : JSON.stringify(finalOffsets);

    const [result] = await pool.execute(
      `
      INSERT INTO reminders (user_id, title, sale_at, offsets_minutes, enabled)
      VALUES (:userId, :title, :saleAt, :offsets, :enabled)
      `,
      {
        userId,
        title: finalTitle,
        saleAt: finalSaleAt,
        offsets: offsetsJson,
        enabled: enabled ?? 1
      }
    );

    const [rows] = await pool.execute(
      `
      SELECT
        id,
        user_id AS userId,
        title,
        sale_at AS saleAt,
        offsets_minutes AS offsetsMinutes,
        enabled,
        created_at AS createdAt,
        updated_at AS updatedAt
      FROM reminders
      WHERE id = :id
      `,
      { id: result.insertId }
    );

    res.status(201).json(rows[0]);
  } catch (err) {
    console.error('POST /api/reminders error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const userId = await getDemoUserId();
    const [result] = await pool.execute(
      `DELETE FROM reminders WHERE id = :id AND user_id = :userId`,
      { id: Number(req.params.id), userId }
    );

    res.json({ ok: true, deleted: result.affectedRows });
  } catch (err) {
    console.error('DELETE /api/reminders/:id error:', err);
    res.status(500).json({ error: 'server_error', message: err.message });
  }
});

module.exports = router;
