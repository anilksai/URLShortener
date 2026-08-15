package com.example.demo.util;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class Base64ShortCodeService implements ShortCodeService {
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public String randomCode(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be greater than zero");
        StringBuilder sb = new StringBuilder(length);
        while (sb.length() < length) {
            byte[] randomBytes = new byte[32];
            RNG.nextBytes(randomBytes);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            for (int i = 0; i < encoded.length() && sb.length() < length; i++) {
                sb.append(encoded.charAt(i));
            }
        }
        return sb.substring(0, length);
    }
}
