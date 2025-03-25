package com.ntn.quanlykhoahoc.session;

public class SessionManager {
    private static String loggedInEmail;

    public static void setLoggedInEmail(String email) {
        loggedInEmail = email;
    }

    public static String getLoggedInEmail() {
        return loggedInEmail;
    }

    public static void logout() {
        loggedInEmail = null;
    }
}
