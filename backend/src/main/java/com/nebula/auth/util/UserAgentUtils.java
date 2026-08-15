package com.nebula.auth.util;

import jakarta.servlet.http.HttpServletRequest;

public class UserAgentUtils {

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            return "127.0.0.1 (Localhost)";
        }
        return ip != null ? ip : "Unknown";
    }

    public static String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown Browser";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Microsoft Edge";
        if (ua.contains("chrome") && !ua.contains("chromium")) return "Google Chrome";
        if (ua.contains("firefox")) return "Mozilla Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Apple Safari";
        if (ua.contains("opera") || ua.contains("opr/")) return "Opera";
        return "Standard Browser";
    }

    public static String extractDevice(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Desktop";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobile Device";
        if (ua.contains("ipad") || ua.contains("tablet")) return "Tablet";
        return "Desktop (PC/Mac)";
    }

    public static String extractOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown OS";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 10.0")) return "Windows 10/11";
        if (ua.contains("windows nt 6.3")) return "Windows 8.1";
        if (ua.contains("windows nt 6.1")) return "Windows 7";
        if (ua.contains("windows")) return "Windows OS";
        if (ua.contains("mac os x") || ua.contains("macintosh")) return "macOS";
        if (ua.contains("android")) return "Android OS";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("cpu os")) return "iOS";
        if (ua.contains("linux")) return "Linux OS";
        return "Unknown OS";
    }

    public static String extractLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || ipAddress.contains("127.0.0.1") || ipAddress.contains("Localhost") || ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.")) {
            return "Local Network (Development)";
        }
        return "San Francisco, US"; // Fallback location display for public IPs
    }
}
