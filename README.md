# 售票資訊 Android App

這個專案分成兩個部分：

- `mobile-app`：Android Studio 專案，使用 Kotlin + Jetpack Compose。
- `backend`：Node.js + Express API，負責連接 Aiven Cloud MySQL 並提供資料給 App。

資料流程：

```text
Android 模擬器
  -> http://10.0.2.2:4000
本機 Node.js 後端
  -> Aiven Cloud MySQL
雲端資料庫
```

也就是說，Android App 不會直接連資料庫。App 會呼叫本機後端，本機後端再用 `.env` 裡的資料庫帳密連到 Aiven MySQL。

## 給別人使用的流程

對方從 GitHub 下載專案後，需要照這個順序操作。

1. 安裝必要工具

需要先安裝：

- Node.js
- Android Studio
- Android Emulator 或實體 Android 手機

2. 建立後端資料庫設定

到 `backend` 資料夾，把 `.env.example` 複製成 `.env`。

```text
backend/.env.example -> backend/.env
```

然後在 `backend/.env` 填入你私下提供的 Aiven MySQL 密碼。

範例：

```env
PORT=4000
JWT_SECRET=change-me
DB_HOST=ticketdb-ticket63.f.aivencloud.com
DB_PORT=13599
DB_NAME=defaultdb
DB_USER=avnadmin
DB_PASSWORD=請填入你私下提供的密碼
DB_SSL=true
```

不要把真正的 `.env` 上傳到 GitHub。

3. 啟動後端

在專案根目錄雙擊：

```text
啟動後端.bat
```

第一次執行時，它會自動安裝後端套件。成功後後端會啟動在：

```text
http://localhost:4000
```

可以用瀏覽器確認：

```text
http://localhost:4000/api/health
```

看到 `{"ok":true}` 代表後端已經成功連上 Aiven MySQL。

4. 用 Android Studio 開 App

在 Android Studio 開啟這個資料夾：

```text
mobile-app
```

啟動 Android Emulator 後按 Run。Android 模擬器會用 `http://10.0.2.2:4000` 連到電腦上的後端，所以只要後端有先啟動，App 就會讀到 Aiven MySQL 裡的資料。

## 常見問題

如果 App 沒有資料，先檢查：

- `啟動後端.bat` 是否還開著。
- `http://localhost:4000/api/health` 是否回傳 `{"ok":true}`。
- `backend/.env` 是否有填正確的 Aiven MySQL 密碼。
- Aiven MySQL 是否允許對方的網路 IP 連線。
- Android App 是否仍然使用 `http://10.0.2.2:4000`。

如果是實體手機，不要使用 `10.0.2.2`。需要把 App 的 API 位址改成電腦的區網 IP，例如：

```text
http://192.168.x.x:4000
```

## 專案資料庫

目前資料庫是 Aiven Cloud MySQL，不是內建在 Android Studio 裡的 SQLite。

GitHub 會保存：

- 後端程式碼
- Android App 程式碼
- `.env.example`
- `schema.sql`

GitHub 不會保存：

- 真正的資料庫密碼
- Aiven 內的實際資料
- `backend/.env`
- `node_modules`
