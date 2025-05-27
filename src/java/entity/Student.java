// ==================== 1. Student.java ====================
/**
 * 學生實體類別
 * 整合本學期學習的物件導向程式設計概念
 */
package entity;

import java.io.Serializable;
import java.util.Date;

public class Student implements Serializable {
    
    // ============ 屬性定義 ============
    private int studentId;          // 學生ID
    private String studentCode;     // 學號
    private String name;            // 姓名
    private String department;      // 系所
    private String password;        // 密碼
    private int totalCredits;       // 已選學分
    
    // ============ 建構子 ============
    
    /**
     * 預設建構子
     */
    public Student() {
        this.totalCredits = 0;
    }
    
    /**
     * 帶參數建構子
     * @param studentCode 學號
     * @param name 姓名
     * @param department 系所
     * @param password 密碼
     */
    public Student(String studentCode, String name, String department, String password) {
        this();
        this.studentCode = studentCode;
        this.name = name;
        this.department = department;
        this.password = password;
    }
    
    // ============ 業務方法 ============
    
    /**
     * 檢查密碼是否正確
     * @param inputPassword 輸入的密碼
     * @return 是否正確
     */
    public boolean checkPassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }
    
    /**
     * 檢查是否可以選更多學分
     * @param additionalCredits 要新增的學分
     * @return 是否可以選課
     */
    public boolean canEnrollMoreCredits(int additionalCredits) {
        final int MAX_CREDITS = 25;  // 最大學分限制
        return (this.totalCredits + additionalCredits) <= MAX_CREDITS;
    }
    
    /**
     * 新增學分
     * @param credits 學分數
     */
    public void addCredits(int credits) {
        this.totalCredits += credits;
    }
    
    /**
     * 減少學分
     * @param credits 學分數
     */
    public void removeCredits(int credits) {
        this.totalCredits = Math.max(0, this.totalCredits - credits);
    }
    
    // ============ Getter 和 Setter 方法 ============
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }
    
    // ============ 覆寫方法 ============
    
    @Override
    public String toString() {
        return "Student{" +
                "studentCode='" + studentCode + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", totalCredits=" + totalCredits +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return studentCode.equals(student.studentCode);
    }
    
    @Override
    public int hashCode() {
        return studentCode.hashCode();
    }
}