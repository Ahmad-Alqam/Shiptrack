package com.shiptrack.util;
// Provides reusable session-checking methods.
import com.shiptrack.model.Role;
import com.shiptrack.model.User;

import jakarta.servlet.http.HttpSession;

public final class SessionHelper {

    private SessionHelper() {
    }

    public static User getLoggedInUser(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            return (User) userObj;
        }
        return null;
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getLoggedInUser(session) != null;
    }

    public static boolean hasRole(HttpSession session, Role role) {
        User user = getLoggedInUser(session);
        return user != null && user.getRole() == role;
    }

    public static Long getUserId(HttpSession session) {
        User user = getLoggedInUser(session);
        return (user != null) ? user.getId() : null;
    }
}