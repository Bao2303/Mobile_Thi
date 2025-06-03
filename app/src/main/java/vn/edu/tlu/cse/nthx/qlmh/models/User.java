package vn.edu.tlu.cse.nthx.qlmh.models;

public class User {
    private int id;
    private String email;
    private String password;
    private String role; // "Khách hàng" hoặc "Nhân viên"
    private String name;

    // Constructor trống cần thiết cho một số thư viện hoặc thao tác DB
    public User() {
    }

    public User(int id, String email, String password, String role, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}