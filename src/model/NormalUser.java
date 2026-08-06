package model;

public class NormalUser extends AppUser {
    @Override
    public String getDashboardTitle() {
        return "User Dashboard";
    }
}
