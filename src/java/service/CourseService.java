// ==================== 4. CourseService.java ====================
/**
 * 課程業務邏輯服務類別
 * 整合業務規則驗證、多執行緒處理和檔案I/O功能
 * 展示Java EE企業級開發的核心概念
 */
package service;

import entity.Student;
import entity.Course;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CourseService {
    
    // ============ 常數定義 ============
    private static final Logger LOGGER = Logger.getLogger(CourseService.class.getName());
    
    // 學分限制常數
    private static final int MAX_CREDITS = 25;  // 每學期最大學分限制
    private static final int MIN_CREDITS = 12;  // 每學期最小學分限制
    
    // 服務物件
    private DatabaseManager dbManager;
    private ExecutorService executor;  // 執行緒池，展示Concurrency技術
    
    // ============ 建構子 ============
    
    /**
     * 建構子 - 初始化服務所需的物件
     * 使用ExecutorService展示多執行緒並發處理能力
     */
    public CourseService() {
        this.dbManager = DatabaseManager.getInstance();
        // 建立固定大小的執行緒池 (展示Java Concurrency)
        this.executor = Executors.newFixedThreadPool(3);
        
        LOGGER.info("CourseService 初始化完成，執行緒池大小：3");
    }
    
    // ============ 課程搜尋方法 (展示多執行緒和Lambda) ============
    
    /**
     * 非同步搜尋課程
     * 展示Java Concurrency和Lambda表達式的應用
     * 
     * @param keyword 搜尋關鍵字
     * @return Future物件，可以取得搜尋結果
     */
    public Future<List<Course>> searchCoursesAsync(String keyword) {
        // 使用Lambda表達式和Future進行非同步處理
        return executor.submit(() -> {
            try {
                // 模擬搜尋處理時間
                Thread.sleep(100);
                
                List<Course> allCourses = dbManager.getAllCourses();
                List<Course> results = new ArrayList<>();
                
                // 使用Stream API進行篩選 (展示Lambda和Streams)
                for (Course course : allCourses) {
                    if (course.matches(keyword)) {
                        results.add(course);
                    }
                }
                
                LOGGER.info("非同步搜尋完成：關鍵字 '" + keyword + "' 找到 " + results.size() + " 門課程");
                return results;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "搜尋被中斷", e);
                return new ArrayList<>();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "搜尋過程發生錯誤", e);
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 同步搜尋課程 (傳統方法)
     * @param keyword 搜尋關鍵字
     * @return 課程列表
     */
    public List<Course> searchCourses(String keyword) {
        try {
            List<Course> allCourses = dbManager.getAllCourses();
            List<Course> results = new ArrayList<>();
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return allCourses;
            }
            
            // 進行關鍵字搜尋
            for (Course course : allCourses) {
                if (course.matches(keyword.trim())) {
                    results.add(course);
                }
            }
            
            LOGGER.info("同步搜尋完成：關鍵字 '" + keyword + "' 找到 " + results.size() + " 門課程");
            return results;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "搜尋課程失敗", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 取得所有課程
     * @return 課程列表
     */
    public List<Course> getAllCourses() {
        try {
            return dbManager.getAllCourses();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "取得課程列表失敗", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 依照系所篩選課程
     * 展示集合框架的使用
     * 
     * @param department 系所名稱
     * @return 篩選後的課程列表
     */
    public List<Course> getCoursesByDepartment(String department) {
        List<Course> allCourses = getAllCourses();
        List<Course> results = new ArrayList<>();
        
        try {
            for (Course course : allCourses) {
                // 如果沒有指定系所或系所匹配，則加入結果
                if (department == null || department.trim().isEmpty() || 
                    course.getDepartment().equals(department.trim())) {
                    results.add(course);
                }
            }
            
            LOGGER.info("系所篩選完成：" + department + " 共 " + results.size() + " 門課程");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "系所篩選發生錯誤", e);
        }
        
        return results;
    }
    
    // ============ 學生選課業務邏輯 (展示業務規則和異常處理) ============
    
    /**
     * 學生選課 - 包含完整的業務規則驗證
     * 展示異常處理和業務邏輯設計
     * 
     * @param student 學生物件
     * @param course 課程物件
     * @return 選課結果訊息
     */
    public String enrollCourse(Student student, Course course) {
        try {
            // 輸入驗證
            if (student == null) {
                return "選課失敗：學生資訊不存在";
            }
            
            if (course == null) {
                return "選課失敗：課程資訊不存在";
            }
            
            // 1. 檢查學分限制
            if (!student.canEnrollMoreCredits(course.getCredits())) {
                return String.format("選課失敗：超過學分上限 (%d 學分)，目前已選 %d 學分", 
                                   MAX_CREDITS, student.getTotalCredits());
            }
            
            // 2. 檢查課程是否已滿
            if (!course.hasAvailableSlots()) {
                return "選課失敗：課程已額滿 (" + course.getCurrentStudents() + "/" + 
                       course.getMaxStudents() + ")";
            }
            
            // 3. 檢查是否已選過此課程
            if (dbManager.isEnrolled(student.getStudentId(), course.getCourseId())) {
                return "選課失敗：您已經選過此課程";
            }
            
            // 4. 檢查必修課程的特殊規則
            if ("必修".equals(course.getType())) {
                String prerequisiteCheck = checkPrerequisites(student, course);
                if (prerequisiteCheck != null) {
                    return prerequisiteCheck;
                }
            }
            
            // 5. 執行選課操作 (使用synchronized確保執行緒安全)
            String result = performEnrollment(student, course);
            
            // 6. 如果選課成功，記錄操作並更新學生資訊
            if ("選課成功".equals(result)) {
                student.addCredits(course.getCredits());
                course.addStudent();
                
                // 記錄操作日誌
                logOperation("ENROLL", student.getStudentCode(), course.getCourseCode(), "成功");
                
                LOGGER.info(String.format("學生 %s 成功選課：%s (%d學分)", 
                           student.getStudentCode(), course.getName(), course.getCredits()));
            }
            
            return result;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "選課過程發生異常", e);
            return "選課失敗：系統錯誤，請稍後再試";
        }
    }
    
    /**
     * 執行選課操作 (同步方法確保執行緒安全)
     * @param student 學生物件
     * @param course 課程物件
     * @return 操作結果
     */
    private synchronized String performEnrollment(Student student, Course course) {
        return dbManager.enrollCourse(student.getStudentId(), course.getCourseId());
    }
    
    /**
     * 學生退選
     * 展示業務邏輯的完整性
     * 
     * @param student 學生物件
     * @param course 課程物件
     * @return 退選結果訊息
     */
    public String dropCourse(Student student, Course course) {
        try {
            // 輸入驗證
            if (student == null || course == null) {
                return "退選失敗：資料不完整";
            }
            
            // 檢查是否確實有選此課程
            if (!dbManager.isEnrolled(student.getStudentId(), course.getCourseId())) {
                return "退選失敗：您尚未選擇此課程";
            }
            
            // 執行退選操作
            String result = dbManager.dropCourse(student.getStudentId(), course.getCourseId());
            
            // 如果退選成功，更新學生和課程資訊
            if ("退選成功".equals(result)) {
                student.removeCredits(course.getCredits());
                course.removeStudent();
                
                // 記錄操作日誌
                logOperation("DROP", student.getStudentCode(), course.getCourseCode(), "成功");
                
                LOGGER.info(String.format("學生 %s 成功退選：%s (%d學分)", 
                           student.getStudentCode(), course.getName(), course.getCredits()));
            }
            
            return result;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "退選過程發生異常", e);
            return "退選失敗：系統錯誤，請稍後再試";
        }
    }
    
    /**
     * 取得學生已選課程
     * @param student 學生物件
     * @return 已選課程列表
     */
    public List<Course> getStudentCourses(Student student) {
        try {
            if (student == null) {
                return new ArrayList<>();
            }
            
            return dbManager.getStudentCourses(student.getStudentId());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "取得學生課程失敗", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 計算學生已選學分總數
     * 展示集合操作和業務計算
     * 
     * @param student 學生物件
     * @return 學分總數
     */
    public int calculateTotalCredits(Student student) {
        try {
            List<Course> courses = getStudentCourses(student);
            int totalCredits = 0;
            
            // 累加所有課程的學分
            for (Course course : courses) {
                totalCredits += course.getCredits();
            }
            
            LOGGER.fine("學生 " + student.getStudentCode() + " 總學分：" + totalCredits);
            return totalCredits;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "計算學分失敗", e);
            return 0;
        }
    }
    
    /**
     * 檢查學生選課是否符合規定
     * 展示業務規則驗證
     * 
     * @param student 學生物件
     * @return 檢查結果訊息
     */
    public String validateEnrollment(Student student) {
        try {
            int totalCredits = calculateTotalCredits(student);
            
            if (totalCredits < MIN_CREDITS) {
                return String.format("警告：學分不足 %d 學分 (目前：%d 學分，還需：%d 學分)", 
                                   MIN_CREDITS, totalCredits, MIN_CREDITS - totalCredits);
            } else if (totalCredits > MAX_CREDITS) {
                return String.format("錯誤：超過學分上限 %d 學分 (目前：%d 學分，超出：%d 學分)", 
                                   MAX_CREDITS, totalCredits, totalCredits - MAX_CREDITS);
            } else {
                return String.format("學分符合規定 (%d 學分，還可選：%d 學分)", 
                                   totalCredits, MAX_CREDITS - totalCredits);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "驗證選課狀態失敗", e);
            return "無法驗證選課狀態";
        }
    }
    
    // ============ 檔案I/O功能 (展示Files, Input/Output Streams) ============
    
    /**
     * 匯出學生選課記錄為CSV檔案
     * 展示檔案I/O和字元編碼處理
     * 
     * @param student 學生物件
     * @param filePath 檔案路徑
     * @return 是否匯出成功
     */
    public boolean exportStudentCoursesToCSV(Student student, String filePath) {
        PrintWriter writer = null;
        
        try {
            // 使用UTF-8編碼建立檔案寫入器
            writer = new PrintWriter(new FileWriter(filePath, StandardCharsets.UTF_8));
            
            // 寫入BOM (Byte Order Mark) 讓Excel正確顯示中文
            writer.write('\uFEFF');
            
            // 寫入CSV標題行
            writer.println("課程代碼,課程名稱,學分數,開課系所,課程類型,授課教師,上課時間,教室");
            
            // 取得學生課程
            List<Course> courses = getStudentCourses(student);
            
            // 寫入課程資料
            for (Course course : courses) {
                writer.printf("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    escapeCSVField(course.getCourseCode()),
                    escapeCSVField(course.getName()),
                    course.getCredits(),
                    escapeCSVField(course.getDepartment()),
                    escapeCSVField(course.getType()),
                    escapeCSVField(course.getTeacher()),
                    escapeCSVField(course.getTimeSchedule()),
                    escapeCSVField(course.getClassroom())
                );
            }
            
            writer.flush();
            
            LOGGER.info("成功匯出學生 " + student.getStudentCode() + " 的選課記錄到 " + filePath);
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "匯出CSV檔案失敗", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "匯出過程發生錯誤", e);
            return false;
        } finally {
            // 確保資源被正確關閉 (展示資源管理)
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "關閉檔案寫入器失敗", e);
                }
            }
        }
    }
    
    /**
     * 產生學生選課報告 (純文字格式)
     * 展示字串處理和格式化
     * 
     * @param student 學生物件
     * @return 報告內容
     */
    public String generateCourseReport(Student student) {
        try {
            StringBuilder report = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // 報告標題
            report.append("=".repeat(60)).append("\n");
            report.append("                    學生選課報告\n");
            report.append("=".repeat(60)).append("\n");
            report.append("產生時間：").append(dateFormat.format(new Date())).append("\n");
            report.append("學    號：").append(student.getStudentCode()).append("\n");
            report.append("姓    名：").append(student.getName()).append("\n");
            report.append("系    所：").append(student.getDepartment()).append("\n");
            report.append("-".repeat(60)).append("\n");
            
            // 課程列表
            List<Course> courses = getStudentCourses(student);
            report.append("已選課程 (共 ").append(courses.size()).append(" 門)：\n\n");
            
            int totalCredits = 0;
            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                totalCredits += course.getCredits();
                
                report.append(String.format("%2d. %-10s %-25s %d學分 %-8s %s\n",
                    i + 1,
                    course.getCourseCode(),
                    course.getName(),
                    course.getCredits(),
                    course.getType(),
                    course.getTeacher() != null ? course.getTeacher() : "未指定"
                ));
                
                // 添加上課時間資訊
                if (course.getTimeSchedule() != null && !course.getTimeSchedule().isEmpty()) {
                    report.append(String.format("    上課時間：%s", course.getTimeSchedule()));
                    if (course.getClassroom() != null && !course.getClassroom().isEmpty()) {
                        report.append(String.format("  教室：%s", course.getClassroom()));
                    }
                    report.append("\n");
                }
                report.append("\n");
            }
            
            // 統計資訊
            report.append("-".repeat(60)).append("\n");
            report.append("學分統計：\n");
            report.append("  總學分數：").append(totalCredits).append(" 學分\n");
            report.append("  剩餘可選：").append(MAX_CREDITS - totalCredits).append(" 學分\n");
            report.append("  選課狀態：").append(validateEnrollment(student)).append("\n");
            
            // 學分分布統計
            report.append("\n課程類型分布：\n");
            int requiredCredits = 0, electiveCredits = 0, generalCredits = 0;
            
            for (Course course : courses) {
                switch (course.getType()) {
                    case "必修":
                        requiredCredits += course.getCredits();
                        break;
                    case "選修":
                        electiveCredits += course.getCredits();
                        break;
                    case "通識":
                        generalCredits += course.getCredits();
                        break;
                }
            }
            
            report.append("  必修課程：").append(requiredCredits).append(" 學分\n");
            report.append("  選修課程：").append(electiveCredits).append(" 學分\n");
            report.append("  通識課程：").append(generalCredits).append(" 學分\n");
            
            report.append("=".repeat(60)).append("\n");
            
            return report.toString();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "產生課程報告失敗", e);
            return "報告產生失敗：" + e.getMessage();
        }
    }
    
    // ============ 私有輔助方法 ============
    
    /**
     * 檢查先修課程條件
     * @param student 學生
     * @param course 課程
     * @return 檢查結果，null表示通過
     */
    private String checkPrerequisites(Student student, Course course) {
        // 這裡可以實作先修課程的檢查邏輯
        // 簡化版本暫時返回通過
        return null;
    }
    
    /**
     * 處理CSV欄位的特殊字元
     * @param field 欄位內容
     * @return 處理後的欄位
     */
    private String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }
        
        // 如果包含逗號、引號或換行符號，需要用引號包圍並處理引號
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return field.replace("\"", "\"\"");
        }
        
        return field;
    }
    
    /**
     * 記錄操作日誌
     * 展示檔案I/O和日誌管理
     * 
     * @param operation 操作類型
     * @param studentCode 學號
     * @param courseCode 課程代碼
     * @param status 狀態
     */
    private void logOperation(String operation, String studentCode, String courseCode, String status) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String logMessage = String.format("[%s] %s - 學生:%s, 課程:%s, 狀態:%s",
                dateFormat.format(new Date()), operation, studentCode, courseCode, status);
            
            // 輸出到系統日誌
            LOGGER.info(logMessage);
            
            // 同時寫入檔案日誌 (使用try-with-resources確保資源關閉)
            try (PrintWriter logWriter = new PrintWriter(
                    new FileWriter("course_operations.log", true))) {
                logWriter.println(logMessage);
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "寫入操作日誌失敗", e);
        }
    }
    
    // ============ 資源管理方法 ============
    
    /**
     * 關閉服務並清理資源
     * 展示資源管理和執行緒池關閉
     */
    public void shutdown() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                
                // 等待執行中的任務完成
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
                
                LOGGER.info("CourseService 執行緒池已關閉");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "關閉執行緒池時被中斷", e);
        }
    }
    
    /**
     * 取得執行緒池狀態資訊 (用於監控)
     * @return 狀態描述
     */
    public String getExecutorStatus() {
        if (executor == null) {
            return "執行緒池未初始化";
        }
        
        if (executor.isShutdown()) {
            return "執行緒池已關閉";
        }
        
        return "執行緒池運行中";
    }
}