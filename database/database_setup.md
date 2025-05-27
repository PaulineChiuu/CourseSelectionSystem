# 資料庫設定說明

------------------------------------------------
一、 資料庫環境需求

必要軟體:
- Apache Derby (Java DB) - 已內建於 NetBeans
- NetBeans IDE 8.2 或以上版本
- GlassFish Server 4.1 或以上版本
- Java JDK 8 或以上版本

資料庫規格:
- 資料庫名稱：'CourseDB'
- 用戶名稱：'app'
- 密碼：'app'
- 連線 URL：'jdbc:derby://localhost:1527/CourseDB'

------------------------------------------------
二、 資料庫建立步驟

使用 NetBeans 建立

步驟 1：啟動 Derby 服務
1. 開啟 NetBeans IDE
2. 在左側視窗點擊 Services 標籤
3. 展開 Databases 節點
4. 找到 Java DB 節點
5. 右鍵點擊 Java DB → 選擇 Start Server
6. 等待狀態顯示為 "running"

步驟 2：建立資料庫
1. 右鍵點擊 Java DB → 選擇 Create Database
2. 填入資料庫資訊：
   - Database Name: 'CourseDB'
   - User Name: 'app'
   - Password: 'app'
   - Confirm Password: 'app'
3. 點擊 OK 建立資料庫

步驟 3：執行 SQL 腳本
1. 在 Databases 下找到新建的 'CourseDB' 連接
2. 右鍵點擊連接 → 選擇 Connect
3. 右鍵點擊已連接的資料庫 → 選擇 Execute Command
4. 開啟 'database/CourseDB.sql' 檔案
5. 複製所有 SQL 內容到執行視窗
6. 點擊 Run SQL 執行腳本
7. 檢查執行結果，確認沒有錯誤

步驟 4：驗證資料庫建立
執行以下查詢確認資料表建立成功：

    -- 檢查資料表
    SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T';

    -- 檢查資料筆數
    SELECT COUNT() AS student_count FROM students;
    SELECT COUNT() AS course_count FROM courses;
    SELECT COUNT() AS enrollment_count FROM enrollments;


預期結果：
- 3 個主要資料表：'STUDENTS', 'COURSES', 'ENROLLMENTS'
- 學生資料：5 筆
- 課程資料：16 筆
- 選課記錄：24 筆


------------------------------------------------
三、 連線設定檢查

在專案中的資料庫連線參數
確認 'src/service/DatabaseManager.java' 中的連線設定：

    private static final String DB_URL = "jdbc:derby://localhost:1527/CourseDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";


測試連線
在 NetBeans 中執行以下測試：

    -- 測試基本查詢
    SELECT  FROM students WHERE student_code = '41043001';
    SELECT  FROM courses WHERE course_code = 'CS101';

    -- 測試關聯查詢
    SELECT s.name, c.name 
    FROM students s 
    JOIN enrollments e ON s.student_id = e.student_id
    JOIN courses c ON e.course_id = c.course_id
    WHERE s.student_code = '41043001';


------------------------------------------------
四、測試資料說明

學生測試帳號

|    學號   |  密碼  |  姓名  |   系所   | 已選學分 |
|----------|--------|-------|------------|----|
| 41043001 | 123456 | 王小明 | 資訊工程學系 | 15 |
| 41043002 | 123456 | 李小華 | 資訊工程學系 | 12 |
| 41043003 | 123456 | 張小美 | 電機工程學系 | 18 |
| 41043004 | 123456 | 陳小強 | 數學系      | 9  |
| 41043005 | 123456 | 林小雅 | 資訊工程學系 | 21 |


課程類型說明

| 類型 |     說明      | 課程數量 |
|-----|---------------|------|
| 必修 | 學系規定必須修習 | 8 門 |
| 選修 | 學系選修課程    | 4 門 |
| 通識 | 通識教育課程    | 4 門 |
