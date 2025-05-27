# 智慧型選課管理系統 🎓

> 基於 Java EE 技術開發的現代化選課管理系統，提供完整的學生選課解決方案


# 📖 專案簡介

智慧型選課管理系統是一個採用 MVC 架構設計的企業級 Web 應用程式，專為大學選課需求而開發。
系統整合了 Java Server Faces、Apache Derby 資料庫，以及完整的業務邏輯處理，展現了企業級 Java 應用開發的最佳實踐。

### 🎯 專案目標
- 提供學生便利直觀的選課體驗
- 實現即時的課程資訊更新
- 確保選課資料的完整性和一致性
- 展示現代 Java Web 開發技術



# ✨ 系統特色

### 🎨 使用者體驗
- 現代化介面：基於 JSF 2.2 的響應式網頁設計
- 直觀操作：簡潔明瞭的操作流程，降低學習成本
- 即時回饋：Ajax 技術實現無刷新頁面互動
- 友善提示：完整的錯誤處理和使用者引導

### ⚡ 技術亮點
- 即時更新：課程餘額即時同步，選課狀態立即反映
- 智慧驗證：多層次的資料驗證和業務規則檢查
- 併發控制：支援多用戶同時操作，防止選課衝突
- 自動化管理：資料庫觸發器自動維護資料一致性

### 🔒 安全可靠
- Session 管理：完整的用戶認證和會話控制
- 資料保護：SQL 注入防護和輸入驗證
- 事務處理：確保資料操作的原子性
- 異常處理：完善的錯誤處理和恢復機制



# 🛠️ 技術架構

### 系統架構圖

🌐 前端層 (Presentation Layer)

├─ JSF Components + XHTML + CSS

├─ 登入介面 (login.xhtml)

├─ 選課頁面 (course-selection.xhtml)  

├─ 個人中心 (my-courses.xhtml)

└─ 課程搜尋與篩選功能

  ⬇️ HTTP/Ajax 通訊 ⬇️

🎛️ 控制層 (Controller Layer)

├─ JSF ManagedBean Controllers

├─ LoginController (登入控制)

├─ CourseController (課程控制)

└─ Session 管理與頁面導航

  ⬇️ Method Call 調用 ⬇️

⚙️ 業務層 (Business Logic Layer)

├─ Java Service Classes

├─ CourseService (選課業務邏輯)

├─ DatabaseManager (資料管理)

├─ 業務規則驗證與併發控制

└─ 異常處理與日誌記錄

  ⬇️ JDBC/SQL 連接 ⬇️

🗄️ 資料層 (Data Access Layer)


├─ Apache Derby Database

├─ students (學生資料表)

├─ courses (課程資料表)

├─ enrollments (選課記錄表)

└─ 觸發器與約束管理

### 前端技術棧
- JSF 2.2 - Java Server Faces 企業級前端框架
- XHTML - 語義化標記語言
- CSS3 - 現代化響應式樣式設計
- JavaScript - 客戶端互動和驗證
- Ajax - 非同步資料更新技術

### 後端技術棧
- Java EE - 企業級開發平台
- Servlet - Web 應用程式基礎
- JDBC - 資料庫連接和操作
- Apache Derby - 輕量級關聯資料庫
- MVC Pattern - 模型-視圖-控制器架構

### 開發工具
- NetBeans IDE 8.2+ - 整合開發環境
- GlassFish Server 4.1+ - Java EE 應用伺服器
- Apache Ant - 專案建置工具
- Git - 版本控制系統



# 🚀 快速開始

 軟體需求
- Java JDK 8 或以上版本
- NetBeans IDE 8.2 或以上版本
- GlassFish Server 4.1 或以上版本
- Apache Derby (通常內建於 NetBeans)

 硬體需求
- 記憶體：至少 4GB RAM
- 儲存空間：至少 2GB 可用空間
- 處理器：支援 Java 的現代處理器



# 🔧 安裝步驟

### 1. 環境準備

 檢查 Java 版本
    java -version

 確認應顯示 JDK 8 或以上版本
    javac -version


### 2. 取得專案

 從 GitHub 克隆專案
    git clone https://github.com/您的用戶名/CourseSelectionSystem.git
    cd CourseSelectionSystem


### 3. 資料庫設定
詳細步驟請參考 ['database/database_setup.md'](database/database_setup.md)

快速設定：
1. 在 NetBeans 中啟動 Java DB (Derby) 服務
2. 建立名為 'CourseDB' 的資料庫
3. 執行 'database/CourseDB.sql' 腳本

 4. 專案部署
- 在 NetBeans 中開啟專案 (File → Open Project)
- 右鍵點擊專案 → Clean and Build
- 確認 GlassFish Server 已啟動
- 右鍵點擊專案 → Deploy

 5. 訪問系統
開啟瀏覽器，訪問：
    http://localhost:8080/CourseSelectionSystem/



# 🎯 系統功能

### 👤 學生功能模組

### 🔐 身份認證
- 安全登入：學號密碼驗證機制
- 會話管理：自動登入狀態維護
- 安全登出：完整的會話清理

### 📚 課程管理
- 課程瀏覽：完整的課程資訊展示
- 智慧搜尋：支援課程名稱、代碼、教師搜尋
- 分類篩選：依系所、課程類型篩選
- 詳細資訊：課程大綱、時間、地點等完整資訊

### ⚡ 選課操作
- 即時選課：一鍵選課，即時更新
- 快速退選：簡單退選操作
- 衝突檢測：自動檢查時間衝突
- 餘額提醒：即時顯示課程剩餘名額

### 📊 個人管理
- 選課記錄：完整的個人選課歷史
- 學分統計：自動計算已選學分和剩餘學分
- 狀態追蹤：選課狀態實時監控
- 報表匯出：個人選課記錄匯出功能



# 🔧 系統管理功能

### 📋 資料管理
- 自動驗證：多層次的資料完整性檢查
- 併發控制：防止超選和資料衝突
- 事務處理：確保操作的原子性
- 觸發器：自動維護資料一致性

### 📈 統計分析
- 選課統計：課程熱門度分析
- 學分分析：學生學分分布統計
- 系統監控：使用狀況和效能監控



# 🧪 測試資料

### 🎓 學生測試帳號

|   學號    |  密碼  |  姓名  |    系所    | 已選學分 | 狀態 |
|----------|--------|-------|------------|----|-------|
| 41043001 | 123456 | 王小明 | 資訊工程學系 | 15 | 活躍 ✅ |
| 41043002 | 123456 | 李小華 | 資訊工程學系 | 12 | 活躍 ✅ |
| 41043003 | 123456 | 張小美 | 電機工程學系 | 18 | 活躍 ✅ |
| 41043004 | 123456 | 陳小強 | 數學系      | 9  | 活躍 ✅ |
| 41043005 | 123456 | 林小雅 | 資訊工程學系 | 21 | 活躍 ✅ |

### 📚 課程資料概覽

| 課程類型 | 課程數量 | 總學分    |    說明     |
|---------|--------|----------|-------------|
| 必修課程 | 8 門   |  24 學分 | 各系所必修課程 |
| 選修課程 | 4 門   |  12 學分 | 專業選修課程   |
| 通識課程 | 4 門   |  8 學分 | 通識教育課程   |
| 總計總計 | 16 門   |  44 學分 | 完整課程體系   |



# 📁 專案結構

CourseSelectionSystem/

|--- 📁 src/java/                     Java 原始碼

|--------- 📁 entity/                   實體類別層

|--------------- 📄 Student.java          學生實體

|--------------- 📄 Course.java           課程實體

|--------- 📁 service/                  業務邏輯層

|--------------- 📄 CourseService.java    課程業務邏輯

|--------------- 📄 DatabaseManager.java  資料庫管理

|--------- 📁 controller/               控制層

|---------------📄 LoginController.java  登入控制器

|--------------- 📄 CourseController.java 課程控制器

|--- 📁 web/                          前端資源

|--------- 📁 WEB-INF/                  Web 配置

|--------------- 📄 web.xml               部署描述文件

|--------- 📄 login.xhtml               登入頁面

|--------- 📄 course-selection.xhtml    選課主頁

|--------- 📄 my-courses.xhtml          個人選課頁面

|--- 📁 nbproject/                    NetBeans 專案配置

|------ 📁 database/                     資料庫相關

|--------- 📄 CourseDB.sql              資料庫建立腳本

|--------- 📄 database_setup.md         資料庫設定說明

|------ 📄 build.xml                     Ant 建置腳本

|------ 📄 README.md                     專案說明文件



# 🔧 開發說明

### 📝 程式碼規範
- 註解完整：所有 public 類別和方法都有詳細的 JavaDoc 註解
- 命名規範：遵循 Java 標準命名慣例
- 異常處理：完整的 try-catch-finally 錯誤處理機制
- 資源管理：適當的資源釋放和記憶體管理

### 🗄️ 資料庫設計原則
- 正規化設計：採用第三正規化，避免資料冗餘
- 完整性約束：主鍵、外鍵、唯一性、檢查約束完整
- 索引最佳化：關鍵查詢欄位建立適當索引
- 觸發器應用：自動維護衍生資料的一致性

### 🏗️ 架構設計模式
- MVC 架構：清楚的模型-視圖-控制器分離
- 單例模式：DatabaseManager 採用單例設計
- 資料存取物件：封裝資料庫操作邏輯
- 業務邏輯分層：將業務規則與資料存取分離
