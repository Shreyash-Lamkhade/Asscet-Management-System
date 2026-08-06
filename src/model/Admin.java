package model;

public class Admin extends AppUser {
    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }
}
