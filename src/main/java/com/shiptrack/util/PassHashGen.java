package com.shiptrack.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PassHashGen {
     private PassHashGen() {
    }
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("driver@123");
        System.out.println(hash);
    }
}