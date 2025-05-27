// ==================== 2. Course.java ====================
/**
 * 課程實體類別
 * 展示物件導向設計和封裝概念
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Course implements Serializable {
    
    // ============ 屬性定義 ============
    private int courseId;           // 課程ID
    private String courseCode;      // 課程代碼
    private String name;            // 課程名稱
    private int credits;            // 學分數
    private String department;      // 開課系所
    private String type;            // 課程類型 (必修/選修)
    private String teacher;         // 授課教師
    private int maxStudents;        // 最大選課人數
    private int currentStudents;    // 目前選課人數
    private String timeSchedule;    // 上課時間
    private String classroom;       // 教室
    
    // ============ 建構子 ============
    
    /**
     * 預設建構子
     */
    public Course() {
        this.currentStudents = 0;
        this.maxStudents = 50;
    }
    
    /**
     * 帶參數建構子
     * @param courseCode 課程代碼
     * @param name 課程名稱
     * @param credits 學分數
     * @param department 開課系所
     * @param type 課程類型
     * @param teacher 授課教師
     */
    public Course(String courseCode, String name, int credits, 
                  String department, String type, String teacher) {
        this();
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.department = department;
        this.type = type;
        this.teacher = teacher;
    }
    
    // ============ 業務方法 ============
    
    /**
     * 檢查是否還有名額
     * @return 是否有剩餘名額
     */
    public boolean hasAvailableSlots() {
        return currentStudents < maxStudents;
    }
    
    /**
     * 取得剩餘名額
     * @return 剩餘名額數
     */
    public int getAvailableSlots() {
        return Math.max(0, maxStudents - currentStudents);
    }
    
    /**
     * 取得選課滿額百分比
     * @return 選課百分比 (0-100)
     */
    public double getEnrollmentPercentage() {
        if (maxStudents == 0) return 0;
        return ((double) currentStudents / maxStudents) * 100;
    }
    
    /**
     * 增加選課人數 (線程安全)
     * @return 是否成功增加
     */
    public synchronized boolean addStudent() {
        if (hasAvailableSlots()) {
            currentStudents++;
            return true;
        }
        return false;
    }
    
    /**
     * 減少選課人數 (線程安全)
     * @return 是否成功減少
     */
    public synchronized boolean removeStudent() {
        if (currentStudents > 0) {
            currentStudents--;
            return true;
        }
        return false;
    }
    
    /**
     * 檢查課程名稱或代碼是否包含關鍵字
     * @param keyword 搜尋關鍵字
     * @return 是否匹配
     */
    public boolean matches(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return (name != null && name.toLowerCase().contains(lowerKeyword)) ||
               (courseCode != null && courseCode.toLowerCase().contains(lowerKeyword)) ||
               (teacher != null && teacher.toLowerCase().contains(lowerKeyword));
    }
    
    // ============ Getter 和 Setter 方法 ============
    
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    
    public int getCurrentStudents() { return currentStudents; }
    public void setCurrentStudents(int currentStudents) { this.currentStudents = currentStudents; }
    
    public String getTimeSchedule() { return timeSchedule; }
    public void setTimeSchedule(String timeSchedule) { this.timeSchedule = timeSchedule; }
    
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    
    // ============ 覆寫方法 ============
    
    @Override
    public String toString() {
        return String.format("Course{%s - %s (%d學分) - %s}", 
                           courseCode, name, credits, teacher);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseCode.equals(course.courseCode);
    }
    
    @Override
    public int hashCode() {
        return courseCode.hashCode();
    }
}