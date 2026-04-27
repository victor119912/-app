# Backend API

這是 Android App 使用的後端 API。它使用 Node.js + Express，並透過 `mysql2` 連接 Aiven Cloud MySQL。

## 設定

第一次使用時，請把 `.env.example` 複製成 `.env`，再填入真正的資料庫密碼。

```text
.env.example -> .env
```

必要設定：

```env
PORT=4000
JWT_SECRET=change-me
DB_HOST=ticketdb-ticket63.f.aivencloud.com
DB_PORT=13599
DB_NAME=defaultdb
DB_USER=avnadmin
DB_PASSWORD=請填入私下提供的密碼
DB_SSL=true
```

`.env` 不要提交到 GitHub。

## 啟動

在專案根目錄雙擊 `啟動後端.bat`，或在這個資料夾執行：

```bash
npm install
npm start
```

後端預設啟動在：

```text
http://localhost:4000
```

健康檢查：

```text
http://localhost:4000/api/health
```

成功時會回傳：

```json
{"ok":true}
```

## API

- `GET /api/events`：取得活動列表，可使用 `keyword`、`venue`、`source`、`startDate`、`endDate` 篩選。
- `GET /api/events/meta`：取得場地與來源選項。
- `GET /api/events/:id`：取得單一活動。
- `GET /api/reminders`：取得提醒。
- `POST /api/reminders`：新增提醒。
- `DELETE /api/reminders/:id`：刪除提醒。
- `GET /api/stats/summary`：取得統計總覽。
- `GET /api/stats/price`：取得票價統計。
- `GET /api/stats/time`：取得活動時間統計。
- `GET /api/stats/venue`：取得場地統計。

## Android 模擬器

Android 模擬器連電腦本機服務時，要使用：

```text
http://10.0.2.2:4000
```

這個位址代表「模擬器裡看到的電腦本機」。因此後端必須先在電腦上啟動，App 才會有資料。
