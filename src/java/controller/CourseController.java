// ==================== 6. CourseController.java ====================
/**
 * 課程選課控制器
 * 整合JSF控制邏輯和業務服務
 */
package controller;

import entity.Student;
import entity.Course;
import service.CourseService;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.io.Serializable;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

@ManagedBean(name = "courseController")
@ViewScoped
public class CourseController implements Serializable {
    
    // ============ 屬性定義 ============
    private static final Logger LOGGER = Logger.getLogger(CourseController.class.getName());
    
    // 服務物件
    private CourseService courseService;
    
    // 頁面資料
    private List<Course> allCourses;         // 所有課程
    private List<Course> filteredCourses;    // 篩選後的課程
    private List<Course> myCourses;          // 我的選課
    private Course selectedCourse;           // 目前選中的課程
    
    // 搜尋條件
    private String searchKeyword;            // 搜尋關鍵字
    private String selectedDepartment;       // 選中的系所
    private String selectedType;             // 選中的課程類型
    
    // ============ 建構子和初始化 ============
    
    /**
     * 建構子
     */
    public CourseController() {
        courseService = new CourseService();
        allCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
        myCourses = new ArrayList<>();
        selectedCourse = new Course();
        
        // 初始化載入資料
        loadCourses();
        loadMyCourses();
    }
    
    // ============ 頁面操作方法 ============
    
    /**
     * 載入所有課程
     */
    public void loadCourses() {
        try {
            allCourses = courseService.getAllCourses();
            filteredCourses = new ArrayList<>(allCourses);
            
            LOGGER.info("成功載入 " + allCourses.size() + " 門課程");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "載入課程失敗", e);
            addErrorMessage("載入課程失敗: " + e.getMessage());
        }
    }
    
    /**
     * 搜尋課程 (支援即時搜尋)
     */
    public void searchCourses() {
        try {
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                // 如果搜尋關鍵字為空，顯示所有課程
                filteredCourses = new ArrayList<>(allCourses);
            } else {
                // 使用非同步搜尋
                Future<List<Course>> searchFuture = courseService.searchCoursesAsync(searchKeyword.trim());
                filteredCourses = searchFuture.get();  // 等待搜尋結果
            }
            
            // 如果有系所篩選，進一步篩選
            if (selectedDepartment != null && !selectedDepartment.isEmpty()) {
                filterByDepartment();
            }
            
            addInfoMessage("搜尋到 " + filteredCourses.size() + " 門課程");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "搜尋課程失敗", e);
            addErrorMessage("搜尋失敗，請稍後再試");
        }
    }
    
    /**
     * 清除搜尋條件
     */
    public void clearSearch() {
        searchKeyword = "";
        selectedDepartment = "";
        selectedType = "";
        filteredCourses = new ArrayList<>(allCourses);
        
        addInfoMessage("已清除搜尋條件");
    }
    
    /**
     * 依系所篩選
     */
    public void filterByDepartment() {
        if (selectedDepartment == null || selectedDepartment.isEmpty()) {
            filteredCourses = new ArrayList<>(allCourses);
        } else {
            filteredCourses = courseService.getCoursesByDepartment(selectedDepartment);
        }
    }
    
    // ============ 選課相關方法 ============
    
    /**
     * 選課操作
     * @param course 要選的課程
     */
    public void enrollCourse(Course course) {
        try {
            Student currentStudent = getCurrentStudent();
            
            if (currentStudent == null) {
                addErrorMessage("請先登入");
                return;
            }
            
            // 執行選課
            String result = courseService.enrollCourse(currentStudent, course);
            
            if ("選課成功".equals(result)) {
                addSuccessMessage("選課成功：" + course.getName());
                
                // 重新載入資料
                loadCourses();
                loadMyCourses();
                
            } else {
                addErrorMessage(result);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "選課操作失敗", e);
            addErrorMessage("選課失敗: " + e.getMessage());
        }
    }
    
    /**
     * 退選操作
     * @param course 要退選的課程
     */
    public void dropCourse(Course course) {
        try {
            Student currentStudent = getCurrentStudent();
            
            if (currentStudent == null) {
                addErrorMessage("請先登入");
                return;
            }
            
            // 執行退選
            String result = courseService.dropCourse(currentStudent, course);
            
            if ("退選成功".equals(result)) {
                addSuccessMessage("退選成功：" + course.getName());
                
                // 重新載入資料
                loadCourses();
                loadMyCourses();
                
            } else {
                addErrorMessage(result);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "退選操作失敗", e);
            addErrorMessage("退選失敗: " + e.getMessage());
        }
    }
    
    /**
     * 載入我的選課記錄
     */
    public void loadMyCourses() {
        try {
            Student currentStudent = getCurrentStudent();
            
            if (currentStudent != null) {
                myCourses = courseService.getStudentCourses(currentStudent);
                LOGGER.fine("載入了 " + myCourses.size() + " 筆選課記錄");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "載入選課記錄失敗", e);
            addErrorMessage("載入選課記錄失敗: " + e.getMessage());
        }
    }
    
    /**
     * 檢查學生是否已選過指定課程
     * @param course 課程物件
     * @return 是否已選課
     */
    public boolean isAlreadyEnrolled(Course course) {
        if (course == null) return false;
        
        for (Course myCourse : myCourses) {
            if (myCourse.getCourseId() == course.getCourseId()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 計算已選學分總數
     * @return 已選學分數
     */
    public int getTotalSelectedCredits() {
        int totalCredits = 0;
        for (Course course : myCourses) {
            totalCredits += course.getCredits();
        }
        return totalCredits;
    }
    
    /**
     * 取得選課狀態訊息
     * @return 狀態訊息
     */
    public String getEnrollmentStatus() {
        Student currentStudent = getCurrentStudent();
        if (currentStudent != null) {
            return courseService.validateEnrollment(currentStudent);
        }
        return "未登入";
    }
    
    // ============ 檔案匯出功能 ============
    
    /**
     * 匯出我的選課記錄為CSV
     */
    public void exportMyCoursesToCSV() {
        try {
            Student currentStudent = getCurrentStudent();
            if (currentStudent == null) {
                addErrorMessage("請先登入");
                return;
            }
            
            // 準備檔案下載
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            
            // 設定回應標頭
            String fileName = "我的選課記錄_" + currentStudent.getStudentCode() + ".csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setCharacterEncoding("UTF-8");
            
            // 產生CSV內容
            StringBuilder csvContent = new StringBuilder();
            csvContent.append('\uFEFF');  // BOM for Excel
            csvContent.append("課程代碼,課程名稱,學分數,開課系所,課程類型,授課教師,上課時間,教室\n");
            
            for (Course course : myCourses) {
                csvContent.append(String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    course.getCourseCode(),
                    course.getName(),
                    course.getCredits(),
                    course.getDepartment(),
                    course.getType(),
                    course.getTeacher(),
                    course.getTimeSchedule() != null ? course.getTimeSchedule() : "",
                    course.getClassroom() != null ? course.getClassroom() : ""
                ));
            }
            
            // 寫入回應
            response.getWriter().write(csvContent.toString());
            response.getWriter().flush();
            
            // 完成回應
            context.responseComplete();
            
            LOGGER.info("學生 " + currentStudent.getStudentCode() + " 匯出選課記錄");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "匯出CSV失敗", e);
            addErrorMessage("匯出失敗: " + e.getMessage());
        }
    }
    
    // ============ 輔助方法 ============
    
    /**
     * 取得目前登入的學生
     * @return 學生物件，如果未登入則返回null
     */
    private Student getCurrentStudent() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        if (session != null && session.getAttribute("isLoggedIn") != null) {
            Student student = new Student();
            student.setStudentId((Integer) session.getAttribute("studentId"));
            student.setStudentCode((String) session.getAttribute("studentCode"));
            student.setName((String) session.getAttribute("studentName"));
            student.setDepartment((String) session.getAttribute("department"));
            student.setTotalCredits((Integer) session.getAttribute("totalCredits"));
            return student;
        }
        
        return null;
    }
    
    /**
     * 更新Session中的學生資訊
     * @param student 學生物件
     */
    private void updateStudentSession(Student student) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        if (session != null) {
            session.setAttribute("totalCredits", student.getTotalCredits());
        }
    }
    
    // ============ 訊息處理方法 ============
    
    /**
     * 添加成功訊息
     * @param message 訊息內容
     */
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "成功", message));
    }
    
    /**
     * 添加錯誤訊息
     * @param message 訊息內容
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "錯誤", message));
    }
    
    /**
     * 添加資訊訊息
     * @param message 訊息內容
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "資訊", message));
    }
    
    // ============ Getter 和 Setter 方法 ============
    
    public List<Course> getAllCourses() {
        return allCourses;
    }
    
    public void setAllCourses(List<Course> allCourses) {
        this.allCourses = allCourses;
    }
    
    public List<Course> getFilteredCourses() {
        return filteredCourses;
    }
    
    public void setFilteredCourses(List<Course> filteredCourses) {
        this.filteredCourses = filteredCourses;
    }
    
    public List<Course> getMyCourses() {
        return myCourses;
    }
    
    public void setMyCourses(List<Course> myCourses) {
        this.myCourses = myCourses;
    }
    
    public Course getSelectedCourse() {
        return selectedCourse;
    }
    
    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
    }
    
    public String getSearchKeyword() {
        return searchKeyword;
    }
    
    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
    
    public String getSelectedDepartment() {
        return selectedDepartment;
    }
    
    public void setSelectedDepartment(String selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }
    
    public String getSelectedType() {
        return selectedType;
    }
    
    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }
    
    // ============ JSF生命週期方法 ============
    
    /**
     * 銷毀時清理資源
     */
    public void destroy() {
        if (courseService != null) {
            courseService.shutdown();
        }
        LOGGER.info("CourseController 資源清理完成");
    }
}