// ==================== 3. DatabaseManager.java ====================
/**
 * 資料庫管理類別
 * 整合JDBC資料庫存取技術和異常處理
 */
package service;

import entity.Student;
import entity.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseManager {
    
    // ============ 常數定義 ============
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    
    // 資料庫連接參數
    private static final String DB_URL = "jdbc:derby://localhost:1527/CourseDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";
    
    // SQL查詢語句
    private static final String SELECT_STUDENT_LOGIN = 
        "SELECT * FROM students WHERE student_code = ? AND password = ?";
    
    private static final String SELECT_ALL_COURSES = 
        "SELECT * FROM courses WHERE is_active = true";
    
    private static final String SELECT_STUDENT_COURSES = 
        "SELECT c.* FROM courses c " +
        "JOIN enrollments e ON c.course_id = e.course_id " +
        "WHERE e.student_id = ? AND e.status = 'ENROLLED'";
    
    private static final String INSERT_ENROLLMENT = 
        "INSERT INTO enrollments (student_id, course_id, status) VALUES (?, ?, 'ENROLLED')";
    
    private static final String DELETE_ENROLLMENT = 
        "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
    
    private static final String UPDATE_COURSE_STUDENTS = 
        "UPDATE courses SET current_students = current_students + ? WHERE course_id = ?";
    
    private static final String CHECK_ENROLLMENT_EXISTS = 
        "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";
    
    // ============ 單例模式實作 ============
    private static DatabaseManager instance;
    
    /**
     * 私有建構子
     */
    private DatabaseManager() {
        try {
            // 載入MySQL驅動程式
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            LOGGER.info("MySQL驅動程式載入成功");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL驅動程式載入失敗", e);
            throw new RuntimeException("資料庫驅動程式載入失敗", e);
        }
    }
    
    /**
     * 取得DatabaseManager的唯一實例
     * @return DatabaseManager實例
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    // ============ 資料庫連接方法 ============
    
    /**
     * 取得資料庫連接
     * @return 資料庫連接物件
     * @throws SQLException 如果無法取得連接
     */
    private Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            LOGGER.fine("資料庫連接建立成功");
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "無法取得資料庫連接", e);
            throw e;
        }
    }
    
    /**
     * 關閉資料庫相關資源
     * @param conn 連接物件
     * @param stmt Statement物件
     * @param rs ResultSet物件
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
            LOGGER.fine("資料庫資源關閉完成");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "關閉資料庫資源時發生錯誤", e);
        }
    }
    
    // ============ 學生相關方法 ============
    
    /**
     * 驗證學生登入
     * @param studentCode 學號
     * @param password 密碼
     * @return 學生物件，如果驗證失敗則返回null
     */
    public Student authenticateStudent(String studentCode, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(SELECT_STUDENT_LOGIN);
            pstmt.setString(1, studentCode);
            pstmt.setString(2, password);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("student_id"));
                student.setStudentCode(rs.getString("student_code"));
                student.setName(rs.getString("name"));
                student.setDepartment(rs.getString("department"));
                student.setPassword(rs.getString("password"));
                student.setTotalCredits(rs.getInt("total_credits"));
                
                LOGGER.info("學生登入成功: " + studentCode);
                return student;
            } else {
                LOGGER.warning("學生登入失敗: " + studentCode);
                return null;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "學生登入驗證發生錯誤", e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    // ============ 課程相關方法 ============
    
    /**
     * 取得所有可選課程
     * @return 課程列表
     */
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(SELECT_ALL_COURSES);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseCode(rs.getString("course_code"));
                course.setName(rs.getString("name"));
                course.setCredits(rs.getInt("credits"));
                course.setDepartment(rs.getString("department"));
                course.setType(rs.getString("type"));
                course.setTeacher(rs.getString("teacher"));
                course.setMaxStudents(rs.getInt("max_students"));
                course.setCurrentStudents(rs.getInt("current_students"));
                course.setTimeSchedule(rs.getString("time_schedule"));
                course.setClassroom(rs.getString("classroom"));
                
                courses.add(course);
            }
            
            LOGGER.info("成功載入 " + courses.size() + " 門課程");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "載入課程列表失敗", e);
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return courses;
    }
    
    /**
     * 取得學生已選課程
     * @param studentId 學生ID
     * @return 已選課程列表
     */
    public List<Course> getStudentCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(SELECT_STUDENT_COURSES);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseCode(rs.getString("course_code"));
                course.setName(rs.getString("name"));
                course.setCredits(rs.getInt("credits"));
                course.setDepartment(rs.getString("department"));
                course.setType(rs.getString("type"));
                course.setTeacher(rs.getString("teacher"));
                course.setMaxStudents(rs.getInt("max_students"));
                course.setCurrentStudents(rs.getInt("current_students"));
                course.setTimeSchedule(rs.getString("time_schedule"));
                course.setClassroom(rs.getString("classroom"));
                
                courses.add(course);
            }
            
            LOGGER.info("學生 " + studentId + " 已選 " + courses.size() + " 門課程");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "載入學生課程失敗", e);
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return courses;
    }
    
    // ============ 選課相關方法 ============
    
    /**
     * 學生選課 (支援並發控制)
     * @param studentId 學生ID
     * @param courseId 課程ID
     * @return 選課結果訊息
     */
    public synchronized String enrollCourse(int studentId, int courseId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);  // 開始交易
            
            // 1. 檢查是否已經選過此課程
            pstmt = conn.prepareStatement(CHECK_ENROLLMENT_EXISTS);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                conn.rollback();
                return "您已經選過此課程";
            }
            rs.close();
            pstmt.close();
            
            // 2. 檢查課程是否還有名額
            pstmt = conn.prepareStatement("SELECT current_students, max_students FROM courses WHERE course_id = ?");
            pstmt.setInt(1, courseId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int current = rs.getInt("current_students");
                int max = rs.getInt("max_students");
                
                if (current >= max) {
                    conn.rollback();
                    return "課程已額滿";
                }
            } else {
                conn.rollback();
                return "課程不存在";
            }
            rs.close();
            pstmt.close();
            
            // 3. 新增選課記錄
            pstmt = conn.prepareStatement(INSERT_ENROLLMENT);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            if (rows > 0) {
                conn.commit();  // 提交交易
                LOGGER.info("學生 " + studentId + " 成功選課，課程ID: " + courseId);
                return "選課成功";
            } else {
                conn.rollback();
                return "選課失敗";
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "交易回滾失敗", ex);
            }
            LOGGER.log(Level.SEVERE, "選課過程發生錯誤", e);
            return "系統錯誤，選課失敗";
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢復自動提交失敗", e);
            }
            closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * 學生退選 (支援並發控制)
     * @param studentId 學生ID
     * @param courseId 課程ID
     * @return 退選結果訊息
     */
    public synchronized String dropCourse(int studentId, int courseId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);  // 開始交易
            
            // 1. 刪除選課記錄
            pstmt = conn.prepareStatement(DELETE_ENROLLMENT);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            if (rows > 0) {
                // 2. 更新課程選課人數
                pstmt = conn.prepareStatement(UPDATE_COURSE_STUDENTS);
                pstmt.setInt(1, -1);  // 減少1人
                pstmt.setInt(2, courseId);
                pstmt.executeUpdate();
                
                conn.commit();  // 提交交易
                LOGGER.info("學生 " + studentId + " 成功退選，課程ID: " + courseId);
                return "退選成功";
            } else {
                conn.rollback();
                return "您尚未選擇此課程";
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "交易回滾失敗", ex);
            }
            LOGGER.log(Level.SEVERE, "退選過程發生錯誤", e);
            return "系統錯誤，退選失敗";
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "恢復自動提交失敗", e);
            }
            closeResources(conn, pstmt, null);
        }
    }
    
    /**
     * 檢查學生是否已選某課程
     * @param studentId 學生ID
     * @param courseId 課程ID
     * @return 是否已選課
     */
    public boolean isEnrolled(int studentId, int courseId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(CHECK_ENROLLMENT_EXISTS);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "檢查選課狀態失敗", e);
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return false;
    }
}