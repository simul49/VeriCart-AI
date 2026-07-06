package com.vericart.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderNoGenerator {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String generate() {
        String timestamp = LocalDateTime.now().format(DT);
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "VC" + timestamp + random;
    }
}
