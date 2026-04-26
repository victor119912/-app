# 售票資訊整合 API

這個資料夾是手機 App 使用的後端 API，負責讀取 MySQL 售票資料、搜尋活動、儲存提醒與提供統計分析。

## 啟動方式

```bash
npm install
npm start
```

啟動後打開：

```text
http://localhost:4000/api/health
```

## 主要 API

- `GET /api/events`：活動列表，可用 `keyword`、`venue`、`source`、`startDate`、`endDate` 篩選
- `GET /api/events/meta`：場地與來源篩選資料
- `GET /api/reminders`：提醒列表
- `POST /api/reminders`：新增提醒
- `DELETE /api/reminders/:id`：刪除提醒
- `GET /api/stats/summary`：總覽統計
- `GET /api/stats/price`：票價分析
- `GET /api/stats/time`：活動月份分析
- `GET /api/stats/venue`：場地熱門排行

## 展示注意

Android 模擬器呼叫電腦本機後端時，App 內使用 `http://10.0.2.2:4000`。
如果改用實體手機展示，請把 App 的 API 位址改成電腦區網 IP，例如 `http://192.168.x.x:4000`。
