-- ==================== Derby 資料庫版本 CourseDB.sql ====================
-- 適用於 Apache Derby 資料庫的建立腳本

-- ==================== 資料表建立 ====================

-- 1. 學生資料表 (Derby版本)
CREATE TABLE students (
    student_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    student_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    total_credits INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (student_id)
);

-- 建立索引
CREATE INDEX idx_student_code ON students(student_code);
CREATE INDEX idx_department ON students(department);

-- 2. 課程資料表 (Derby版本)
CREATE TABLE courses (
    course_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    course_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    credits INTEGER NOT NULL DEFAULT 3,
    department VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL DEFAULT '選修',
    teacher VARCHAR(50),
    max_students INTEGER NOT NULL DEFAULT 50,
    current_students INTEGER DEFAULT 0,
    time_schedule VARCHAR(50),
    classroom VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (course_id),
    CONSTRAINT chk_type CHECK (type IN ('必修', '選修', '通識')),
    CONSTRAINT uk_course_code UNIQUE (course_code)
);

-- 建立索引
CREATE INDEX idx_course_code ON courses(course_code);
CREATE INDEX idx_course_department ON courses(department);
CREATE INDEX idx_course_type ON courses(type);
CREATE INDEX idx_course_active ON courses(is_active);

-- 3. 選課記錄表 (Derby版本)
CREATE TABLE enrollments (
    enrollment_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    student_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ENROLLED',
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (enrollment_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT chk_status CHECK (status IN ('ENROLLED', 'DROPPED')),
    CONSTRAINT uk_student_course UNIQUE (student_id, course_id)
);

-- 建立索引
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);

-- ==================== 觸發器建立 (Derby版本) ====================

-- Derby 的觸發器語法與 MySQL 不同
-- 選課時自動更新課程人數
CREATE TRIGGER tr_enrollment_insert
    AFTER INSERT ON enrollments
    REFERENCING NEW AS newrow
    FOR EACH ROW
    UPDATE courses 
    SET current_students = current_students + 1
    WHERE course_id = newrow.course_id AND newrow.status = 'ENROLLED';

-- 退選時自動更新課程人數  
CREATE TRIGGER tr_enrollment_update
    AFTER UPDATE ON enrollments
    REFERENCING OLD AS oldrow NEW AS newrow
    FOR EACH ROW
    UPDATE courses 
    SET current_students = 
        CASE 
            WHEN oldrow.status = 'ENROLLED' AND newrow.status = 'DROPPED' 
            THEN current_students - 1
            WHEN oldrow.status = 'DROPPED' AND newrow.status = 'ENROLLED' 
            THEN current_students + 1
            ELSE current_students
        END
    WHERE course_id = newrow.course_id;

-- ==================== 插入測試資料 ====================

-- 插入學生測試資料
INSERT INTO students (student_code, name, department, password, total_credits) VALUES
('41043001', '王小明', '資訊工程學系', '123456', 15),
('41043002', '李小華', '資訊工程學系', '123456', 12),
('41043003', '張小美', '電機工程學系', '123456', 18),
('41043004', '陳小強', '數學系', '123456', 9),
('41043005', '林小雅', '資訊工程學系', '123456', 21);

-- 插入課程測試資料
INSERT INTO courses (course_code, name, credits, department, type, teacher, max_students, current_students, time_schedule, classroom) VALUES
-- 資工系課程
('CS101', '計算機概論', 3, '資訊工程學系', '必修', '李志明教授', 50, 25, '週一 9:10-12:00', 'E301'),
('CS201', '資料結構與演算法', 3, '資訊工程學系', '必修', '陳大華副教授', 45, 30, '週二 9:10-12:00', 'E302'),
('CS202', '網頁程式設計', 3, '資訊工程學系', '選修', '王美玲助教授', 40, 22, '週一 13:10-16:00', 'C201'),
('CS301', '軟體工程', 3, '資訊工程學系', '必修', '劉大同教授', 35, 18, '週三 10:10-12:00', 'E304'),
('CS302', '資料庫系統', 3, '資訊工程學系', '選修', '陳大華副教授', 30, 15, '週五 13:10-16:00', 'C202'),

-- 數學系課程
('MATH101', '微積分(一)', 4, '數學系', '必修', '張小芳教授', 60, 45, '週二 8:10-10:00, 週四 8:10-10:00', 'A101'),
('MATH201', '微積分(二)', 4, '數學系', '必修', '張小芳教授', 55, 42, '週二 10:10-12:00, 週四 10:10-12:00', 'A101'),
('MATH301', '線性代數', 3, '數學系', '選修', '黃大明教授', 40, 28, '週三 13:10-16:00', 'A203'),

-- 電機系課程
('EE101', '電路學(一)', 3, '電機工程學系', '必修', '林志華教授', 50, 35, '週一 10:10-12:00, 週三 9:10-10:00', 'F201'),
('EE201', '電子學', 3, '電機工程學系', '必修', '吳大偉副教授', 45, 32, '週二 13:10-16:00', 'F202'),

-- 通識課程
('GE101', '英文(一)', 2, '通識教育中心', '必修', 'John Smith', 30, 28, '週三 14:10-16:00', 'B205'),
('GE102', '英文(二)', 2, '通識教育中心', '必修', 'Mary Johnson', 30, 30, '週五 9:10-11:00', 'B201'),
('GE201', '哲學概論', 2, '通識教育中心', '通識', '張大文教授', 50, 25, '週四 13:10-15:00', 'H201'),
('GE202', '心理學導論', 2, '通識教育中心', '通識', '李小萍副教授', 45, 20, '週五 15:10-17:00', 'H202'),

-- 體育課程
('PE101', '體育(一)', 1, '體育室', '必修', '劉教練', 40, 35, '週四 15:10-17:00', '體育館'),
('PE102', '體育(二)', 1, '體育室', '必修', '陳教練', 40, 32, '週二 15:10-17:00', '體育館');

-- 插入選課記錄測試資料
INSERT INTO enrollments (student_id, course_id, status) VALUES
-- 王小明 (41043001) 的選課記錄
(1, 1, 'ENROLLED'),  -- 計算機概論
(1, 2, 'ENROLLED'),  -- 資料結構與演算法
(1, 3, 'ENROLLED'),  -- 網頁程式設計
(1, 11, 'ENROLLED'), -- 英文(一)
(1, 15, 'ENROLLED'), -- 體育(一)

-- 李小華 (41043002) 的選課記錄
(2, 1, 'ENROLLED'),  -- 計算機概論
(2, 2, 'ENROLLED'),  -- 資料結構與演算法
(2, 6, 'ENROLLED'),  -- 微積分(一)
(2, 11, 'ENROLLED'), -- 英文(一)

-- 張小美 (41043003) 的選課記錄
(3, 9, 'ENROLLED'),  -- 電路學(一)
(3, 10, 'ENROLLED'), -- 電子學
(3, 6, 'ENROLLED'),  -- 微積分(一)
(3, 7, 'ENROLLED'),  -- 微積分(二)
(3, 12, 'ENROLLED'), -- 英文(二)
(3, 16, 'ENROLLED'), -- 體育(二)

-- 陳小強 (41043004) 的選課記錄
(4, 6, 'ENROLLED'),  -- 微積分(一)
(4, 8, 'ENROLLED'),  -- 線性代數
(4, 13, 'ENROLLED'), -- 哲學概論

-- 林小雅 (41043005) 的選課記錄
(5, 1, 'ENROLLED'),  -- 計算機概論
(5, 2, 'ENROLLED'),  -- 資料結構與演算法
(5, 3, 'ENROLLED'),  -- 網頁程式設計
(5, 4, 'ENROLLED'),  -- 軟體工程
(5, 5, 'ENROLLED'),  -- 資料庫系統
(5, 6, 'ENROLLED'),  -- 微積分(一)
(5, 11, 'ENROLLED'), -- 英文(一)
(5, 15, 'ENROLLED'); -- 體育(一)

-- ==================== 檢視建立 (Derby版本) ====================

-- 學生選課狀況檢視
CREATE VIEW student_course_view AS
SELECT 
    s.student_code,
    s.name AS student_name,
    s.department AS student_department,
    c.course_code,
    c.name AS course_name,
    c.credits,
    c.type AS course_type,
    c.teacher,
    c.time_schedule,
    c.classroom,
    e.status,
    e.enrollment_date
FROM students s
JOIN enrollments e ON s.student_id = e.student_id
JOIN courses c ON e.course_id = c.course_id
WHERE e.status = 'ENROLLED';

-- 課程選課統計檢視
CREATE VIEW course_enrollment_stats AS
SELECT 
    c.course_code,
    c.name AS course_name,
    c.credits,
    c.department,
    c.type,
    c.teacher,
    c.max_students,
    c.current_students,
    (c.max_students - c.current_students) AS available_slots,
    c.time_schedule,
    c.classroom
FROM courses c
WHERE c.is_active = true;

-- ==================== 測試查詢 ====================

-- 查詢所有學生
SELECT * FROM students;

-- 查詢所有課程
SELECT * FROM courses;

-- 查詢選課記錄
SELECT * FROM student_course_view;

-- 查詢課程統計
SELECT * FROM course_enrollment_stats;

-- 查詢王小明的選課記錄
SELECT * FROM student_course_view WHERE student_code = '41043001';

-- 查詢資工系的課程
SELECT * FROM course_enrollment_stats WHERE department = '資訊工程學系';

-- ==================== 資料驗證 ====================

-- 檢查資料是否正確插入
SELECT 
    'students' AS table_name, 
    COUNT(*) AS record_count 
FROM students
UNION ALL
SELECT 'courses', COUNT(*) FROM courses
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments;

-- 檢查選課人數是否正確
SELECT 
    c.course_code,
    c.current_students AS recorded,
    COUNT(e.enrollment_id) AS actual
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id AND e.status = 'ENROLLED'
GROUP BY c.course_id, c.course_code, c.current_students;

