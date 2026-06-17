package org.tradebook.journal.features.auth;

public class AuthConstants {
    public static final String MSG_REGISTER_SUCCESS = "User registered successfully";
    public static final String MSG_LOGIN_SUCCESS = "User logged in successfully";
    public static final String MSG_LOGOUT_SUCCESS = "User logged out successfully";
    public static final String MSG_EMAIL_IN_USE = "Email already in use";
    public static final String MSG_USERNAME_IN_USE = "Username already in use";
    public static final String MSG_FORGOT_PASSWORD_SUCCESS = "If an account exists with that email, a password reset link has been sent";
    public static final String MSG_RESET_PASSWORD_SUCCESS = "Password has been reset successfully. You can now log in with your new password";
    public static final String MSG_INVALID_RESET_TOKEN = "Invalid or already used password reset token";
    public static final String MSG_EXPIRED_RESET_TOKEN = "Password reset token has expired. Please request a new one";

    private AuthConstants() {
        // Private constructor to prevent instantiation
    }
}
