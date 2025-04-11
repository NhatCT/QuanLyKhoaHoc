package com.ntn.quanlykhoahoc.services;

    import org.mindrot.jbcrypt.BCrypt;

    public class PasswordService {
        public String hashPassword(String password) {
            return BCrypt.hashpw(password, BCrypt.gensalt(10));
        }
    }