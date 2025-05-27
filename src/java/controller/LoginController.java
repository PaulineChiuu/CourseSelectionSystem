// ==================== 5. LoginController.java ====================
/**
 * 登入控制器
 * 整合JSF ManagedBean和Session管理
 */
package controller;

import entity.Student;
import service.DatabaseManager;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

@ManagedBean(name = "loginController")
@RequestScoped
public class LoginController implements Serializable {
    
    // ============ 屬性定義 ============
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    
    private String studentCode;     // 學號
    private String password;        // 密碼
    private boolean rememberMe;     // 記住我
    
    private DatabaseManager dbManager;
    
    // ============ 建構子 ============
    
    /**
     * 建構子
     */
    public LoginController() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // ============ 登入相關方法 ============
    
    /**
     * 執行登入驗證
     * @return 導航結果
     */
    public String login() {
        try {
            // 輸入驗證
            if (studentCode == null || studentCode.trim().isEmpty()) {
                addErrorMessage("請輸入學號");
                return null;
            }
            
            if (password == null || password.trim().isEmpty()) {
                addErrorMessage("請輸入密碼");
                return null;
            }
            
            // 學號格式驗證
            if (!isValidStudentCode(studentCode.trim())) {
                addErrorMessage("學號格式不正確 (應為8位數字)");
                return null;
            }
            
            // 驗證學生身份
            Student student = dbManager.authenticateStudent(studentCode.trim(), password);
            
            if (student != null) {
                // 登入成功，建立Session
                createUserSession(student);
                
                LOGGER.info("學生登入成功: " + student.getStudentCode() + " (" + student.getName() + ")");
                addSuccessMessage("登入成功，歡迎 " + student.getName());
                
                // 重導向到選課頁面
                return "course-selection?faces-redirect=true";
                
            } else {
                addErrorMessage("學號或密碼錯誤");
                LOGGER.warning("登入失敗，學號: " + studentCode);
                return null;
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "登入過程發生錯誤", e);
            addErrorMessage("系統錯誤，請稍後再試");
            return null;
        }
    }
    
    /**
     * 登出操作
     * @return 導航結果
     */
    public String logout() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            
            if (session != null) {
                String studentCode = (String) session.getAttribute("studentCode");
                String studentName = (String) session.getAttribute("studentName");
                
                // 清除Session
                session.invalidate();
                
                LOGGER.info("學生登出: " + studentCode + " (" + studentName + ")");
            }
            
            addInfoMessage("您已成功登出");
            return "login?faces-redirect=true";
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "登出過程發生錯誤", e);
            return "login?faces-redirect=true";
        }
    }
    
    /**
     * 檢查是否已登入
     * @return 是否已登入
     */
    public boolean isLoggedIn() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        if (session != null) {
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            return isLoggedIn != null && isLoggedIn;
        }
        
        return false;
    }
    
    /**
     * 取得目前登入的學生資訊
     * @return 學生物件
     */
    public Student getCurrentStudent() {
        if (!isLoggedIn()) {
            return null;
        }
        
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        Student student = new Student();
        student.setStudentId((Integer) session.getAttribute("studentId"));
        student.setStudentCode((String) session.getAttribute("studentCode"));
        student.setName((String) session.getAttribute("studentName"));
        student.setDepartment((String) session.getAttribute("department"));
        student.setTotalCredits((Integer) session.getAttribute("totalCredits"));
        
        return student;
    }
    
    // ============ 私有輔助方法 ============
    
    /**
     * 建立使用者Session
     * @param student 學生物件
     */
    private void createUserSession(Student student) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        
        // 儲存學生資訊到Session
        session.setAttribute("studentId", student.getStudentId());
        session.setAttribute("studentCode", student.getStudentCode());
        session.setAttribute("studentName", student.getName());
        session.setAttribute("department", student.getDepartment());
        session.setAttribute("totalCredits", student.getTotalCredits());
        session.setAttribute("isLoggedIn", true);
        
        // 設定Session逾時時間（30分鐘）
        session.setMaxInactiveInterval(30 * 60);
    }
    
    /**
     * 驗證學號格式
     * @param studentCode 學號
     * @return 是否格式正確
     */
    private boolean isValidStudentCode(String studentCode) {
        return studentCode != null && 
               studentCode.matches("\\d{8}") &&  // 8位數字
               studentCode.length() == 8;
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
    
    public String getStudentCode() {
        return studentCode;
    }
    
    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}