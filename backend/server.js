require('dotenv').config();

const { createApp } = require('./app');

const PORT = Number(process.env.PORT || 4000);
const app = createApp();

app.listen(PORT, () => {
  console.log(`Ticket API running on http://localhost:${PORT}`);
});
