package io.github.js.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64URL {

    private Base64URL() {
    }

    public static String base64URLFromString(String rawString) {
        return base64URLFromBytes(rawString.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64URLFromBytes(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
    }

    public static String stringFromBase64URL(String base64URL) {
        return new String(Base64.getUrlDecoder().decode(base64URL));
    }

}
