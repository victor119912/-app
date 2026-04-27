const crypto = require('node:crypto');

const TOKEN_TTL_SECONDS = 60 * 60 * 24 * 30;
const PASSWORD_KEY_LENGTH = 64;
const PASSWORD_ITERATIONS = 120000;
const PASSWORD_DIGEST = 'sha512';

function normalizeEmail(email) {
  return String(email || '').trim().toLowerCase();
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizeEmail(email));
}

function validatePassword(password) {
  return String(password || '').length >= 6;
}

function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = crypto
    .pbkdf2Sync(String(password), salt, PASSWORD_ITERATIONS, PASSWORD_KEY_LENGTH, PASSWORD_DIGEST)
    .toString('hex');
  return `pbkdf2:${PASSWORD_ITERATIONS}:${salt}:${hash}`;
}

function verifyPassword(password, storedHash) {
  const [scheme, iterationsText, salt, hash] = String(storedHash || '').split(':');
  if (scheme !== 'pbkdf2' || !iterationsText || !salt || !hash) return false;

  const iterations = Number(iterationsText);
  if (!Number.isFinite(iterations) || iterations <= 0) return false;

  const candidate = crypto
    .pbkdf2Sync(String(password), salt, iterations, Buffer.from(hash, 'hex').length, PASSWORD_DIGEST)
    .toString('hex');

  return crypto.timingSafeEqual(Buffer.from(candidate, 'hex'), Buffer.from(hash, 'hex'));
}

function base64UrlEncode(value) {
  return Buffer.from(JSON.stringify(value)).toString('base64url');
}

function sign(value, secret) {
  return crypto.createHmac('sha256', secret).update(value).digest('base64url');
}

function createToken(user, secret, now = () => Math.floor(Date.now() / 1000)) {
  const header = base64UrlEncode({ alg: 'HS256', typ: 'JWT' });
  const payload = base64UrlEncode({
    sub: String(user.id),
    email: user.email,
    iat: now(),
    exp: now() + TOKEN_TTL_SECONDS
  });
  const body = `${header}.${payload}`;
  return `${body}.${sign(body, secret)}`;
}

function verifyToken(token, secret, now = () => Math.floor(Date.now() / 1000)) {
  const parts = String(token || '').split('.');
  if (parts.length !== 3) return null;

  const [header, payload, signature] = parts;
  const body = `${header}.${payload}`;
  const expected = sign(body, secret);
  if (
    signature.length !== expected.length ||
    !crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected))
  ) {
    return null;
  }

  try {
    const data = JSON.parse(Buffer.from(payload, 'base64url').toString('utf8'));
    const userId = Number(data.sub);
    if (!Number.isInteger(userId) || userId <= 0) return null;
    if (Number(data.exp || 0) <= now()) return null;
    return { userId, email: data.email };
  } catch (_err) {
    return null;
  }
}

function authMiddleware({ pool, secret, now }) {
  return async (req, res, next) => {
    try {
      const header = String(req.headers.authorization || '');
      const token = header.startsWith('Bearer ') ? header.slice(7).trim() : '';
      const session = verifyToken(token, secret, now);

      if (!session) {
        return res.status(401).json({ error: 'unauthorized' });
      }

      const [rows] = await pool.execute(
        `SELECT id, email, created_at AS createdAt FROM users WHERE id = :id LIMIT 1`,
        { id: session.userId }
      );

      if (!rows.length) {
        return res.status(401).json({ error: 'unauthorized' });
      }

      req.user = rows[0];
      next();
    } catch (err) {
      console.error('auth middleware error:', err);
      res.status(500).json({ error: 'server_error', message: err.message });
    }
  };
}

module.exports = {
  normalizeEmail,
  validateEmail,
  validatePassword,
  hashPassword,
  verifyPassword,
  createToken,
  verifyToken,
  authMiddleware
};
