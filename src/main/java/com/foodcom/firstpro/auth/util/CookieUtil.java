package com.foodcom.firstpro.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly); // JS 접근 방지 (보안 핵심)
        cookie.setSecure(secure);     // HTTPS에서만 전송 (운영 시 필수)
        response.addCookie(cookie);
    }


}
