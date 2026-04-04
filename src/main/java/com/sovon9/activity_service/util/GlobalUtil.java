package com.sovon9.activity_service.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GlobalUtil {

    public static String toGlobalId(String type, Object id) {
        return Base64.getEncoder()
                .encodeToString((type + ":" + id).getBytes(StandardCharsets.UTF_8));
    }

    public static String[] fromGlobalId(String globalId) {
        String decoded = new String(Base64.getDecoder().decode(globalId), StandardCharsets.UTF_8);
        return decoded.split(":", 2);
    }
}
