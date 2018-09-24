package com.vistorieja.emailws.util;

import java.util.Base64;

public class CriptoUtil {

    public static String encrypt(String email){
        return Base64.getEncoder().encodeToString(email.getBytes());
    }

    public static String decrypt(String email){
        return new String(Base64.getDecoder().decode(email));
    }
}
