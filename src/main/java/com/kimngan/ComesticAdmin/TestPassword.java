package com.kimngan.ComesticAdmin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main (String[] args){
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
    }
}
