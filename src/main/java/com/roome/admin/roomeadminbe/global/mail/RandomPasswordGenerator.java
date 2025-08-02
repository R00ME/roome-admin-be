package com.roome.admin.roomeadminbe.global.mail;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RandomPasswordGenerator {
    public static String generateRandomPassword() {
        int length = 10;

        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%";
        String all = lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // 반드시 하나씩 포함
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        // 나머지 자리수 랜덤 채우기
        for (int i = 3; i < length; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }

        // 조합 재배치
        List<Character> chars = sb.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        // 결과
        StringBuilder password = new StringBuilder();
        chars.forEach(password::append);

        return password.toString();
    }
}
