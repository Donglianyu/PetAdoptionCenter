package util;

import model.User;

public class SessionContext {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isStaff() {
        return currentUser instanceof model.Staff;
    }

    public static boolean isAdopter() {
        return currentUser instanceof model.Adopter;
    }
}

