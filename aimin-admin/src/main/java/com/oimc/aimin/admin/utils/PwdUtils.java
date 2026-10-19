package com.oimc.aimin.admin.utils;

import cn.dev33.satoken.secure.BCrypt;

public class PwdUtils {

    public static String bcryptPasswordEncoder (String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static Boolean verifyBCryptHash(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
