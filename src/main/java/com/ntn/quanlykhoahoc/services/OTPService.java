package com.ntn.quanlykhoahoc.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OTPService {
    private static final int LIMIT = 5;
    private static final int BLOCK_MINUTES = 10;

    private static final Map<String, Integer> otpCount = new HashMap<>();
    private static final Map<String, LocalDateTime> blockMap = new HashMap<>();

    public boolean canRequestOTP(String email) {
        if (blockMap.containsKey(email) && LocalDateTime.now().isBefore(blockMap.get(email))) {
            return false;
        }

        int count = otpCount.getOrDefault(email, 0);
        if (count >= LIMIT) {
            blockMap.put(email, LocalDateTime.now().plusMinutes(BLOCK_MINUTES));
            otpCount.put(email, 0);
            return false;
        }

        otpCount.put(email, count + 1);
        return true;
    }

    public String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
