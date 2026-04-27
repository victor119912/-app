const express = require('express');
const {
  normalizeEmail,
  validateEmail,
  validatePassword,
  hashPassword,
  verifyPassword,
  createToken
} = require('../auth');

function createAuthRouter({ pool, secret, now, requireAuth }) {
  const router = express.Router();

  router.post('/register', async (req, res) => {
    try {
      const email = normalizeEmail(req.body?.email);
      const password = String(req.body?.password || '');

      if (!validateEmail(email) || !validatePassword(password)) {
        return res.status(400).json({
          error: 'invalid_fields',
          need: ['email', 'password']
        });
      }

      const passwordHash = hashPassword(password);
      const [result] = await pool.execute(
        `
        INSERT INTO users (email, password_hash)
        VALUES (:email, :passwordHash)
        `,
        { email, passwordHash }
      );

      const [rows] = await pool.execute(
        `SELECT id, email, created_at AS createdAt FROM users WHERE id = :id LIMIT 1`,
        { id: result.insertId }
      );

      const user = rows[0];
      res.status(201).json({
        token: createToken(user, secret, now),
        user
      });
    } catch (err) {
      if (err.code === 'ER_DUP_ENTRY') {
        return res.status(409).json({ error: 'email_exists' });
      }
      console.error('POST /api/auth/register error:', err);
      res.status(500).json({ error: 'server_error', message: err.message });
    }
  });

  router.post('/login', async (req, res) => {
    try {
      const email = normalizeEmail(req.body?.email);
      const password = String(req.body?.password || '');

      if (!email || !password) {
        return res.status(400).json({ error: 'missing_fields', need: ['email', 'password'] });
      }

      const [rows] = await pool.execute(
        `SELECT id, email, password_hash FROM users WHERE email = :email LIMIT 1`,
        { email }
      );
      const user = rows[0];

      if (!user || !verifyPassword(password, user.password_hash)) {
        return res.status(401).json({ error: 'invalid_credentials' });
      }

      res.json({
        token: createToken(user, secret, now),
        user: { id: user.id, email: user.email }
      });
    } catch (err) {
      console.error('POST /api/auth/login error:', err);
      res.status(500).json({ error: 'server_error', message: err.message });
    }
  });

  router.get('/me', requireAuth, async (req, res) => {
    res.json({ user: req.user });
  });

  router.delete('/me', requireAuth, async (req, res) => {
    try {
      const password = String(req.body?.password || '');
      if (!password) {
        return res.status(400).json({ error: 'missing_fields', need: ['password'] });
      }

      const [rows] = await pool.execute(
        `SELECT id, email, password_hash FROM users WHERE email = :email LIMIT 1`,
        { email: req.user.email }
      );
      const user = rows[0];

      if (!user || !verifyPassword(password, user.password_hash)) {
        return res.status(401).json({ error: 'invalid_credentials' });
      }

      const [result] = await pool.execute(
        `DELETE FROM users WHERE id = :userId`,
        { userId: req.user.id }
      );

      res.json({ ok: result.affectedRows > 0 });
    } catch (err) {
      console.error('DELETE /api/auth/me error:', err);
      res.status(500).json({ error: 'server_error', message: err.message });
    }
  });

  return router;
}

module.exports = createAuthRouter;
