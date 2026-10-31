# 車機 Launcher

仿特斯拉介面風格的 Android 車機桌面（Launcher），以 Kotlin + Jetpack Compose 撰寫。

目標車機：Android 14、Qualcomm SM6125（trinket, arm64）、4GB RAM，寬螢幕橫向。版面會依螢幕大小自動調整，1920×720、1280×480 這類長條螢幕都適用。

## 目前功能（v0.1 骨架）

- 可設為預設桌面（HOME launcher）
- 左側車輛狀態區：檔位、時速（目前固定 0，之後接 GPS）、時間日期、電量
- 右側地圖區（目前是示意圖，之後接地圖 SDK）
- 底部 Dock：常用 App、所有 App、系統設定
- App 抽屜：列出所有已安裝 App、可搜尋；長按 App 可「加入 Dock / 從 Dock 移除」或開啟 App 資訊
- 沉浸模式：隱藏系統列（從螢幕邊緣滑動可叫出）
- 安裝/移除 App 時自動更新清單

## 下載與安裝

1. 到本 repo 的 **Releases → 最新版（自動編譯）** 下載 `CarLauncher-xx.apk`
   （或到 **Actions** 頁面點最新一次成功的編譯，下載 `CarLauncher-apk`）。
2. 把 APK 放進 USB 隨身碟或用瀏覽器下載到車機，用檔案管理員安裝（需允許「安裝未知來源 App」）。
3. 按車機的 Home 鍵，選擇「車機 Launcher」並設為「一律」。
   若沒有跳出選擇視窗：設定 → 應用程式 → 預設應用程式 → 主畫面應用程式。

> 每次編譯都用同一把簽章金鑰（`app/sideload.keystore`，僅供自行安裝使用），所以新版可以直接覆蓋安裝，不用先移除。

### 想換回原廠桌面

設定 → 應用程式 → 預設應用程式 → 主畫面應用程式，選回原本的桌面即可。

## 開發

```bash
./gradlew assembleDebug   # 需要 Android SDK（compileSdk 35）
```

推到 `main` 分支會由 GitHub Actions 自動編譯並更新「最新版」Release；推 `v*` 標籤會建立正式 Release。

## 接下來

- [ ] 音樂卡片（NotificationListenerService + MediaSession）
- [ ] 真正的地圖（深色樣式）
- [ ] GPS 時速、日夜模式自動切換
- [ ] 開機自動啟動、動畫細節
