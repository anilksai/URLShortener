package com.example.demo.util;

import com.example.demo.exception.InvalidUrlException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Set;

public final class UrlSafetyGuard {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final Set<String> DENIED_HOSTS = Set.of("localhost", "127.0.0.1", "0.0.0.0", "::1");

    private UrlSafetyGuard() {
    }

    public static String normalizeAndValidate(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidUrlException("originalUrl is required");
        }

        String candidate = rawUrl.trim();
        if (candidate.length() > 2048) {
            throw new InvalidUrlException("originalUrl exceeds max length of 2048 characters");
        }

        URI uri;
        try {
            uri = new URI(candidate).normalize();
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("originalUrl is not a valid URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException("Only http and https URLs are allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("originalUrl must include a valid host");
        }

        String normalizedHost = host.toLowerCase();
        if (DENIED_HOSTS.contains(normalizedHost) || normalizedHost.endsWith(".localhost")) {
            throw new InvalidUrlException("localhost and local network targets are not allowed");
        }

        if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
            throw new InvalidUrlException("User info in the URL is not allowed");
        }

        // Reject CRLF injection via percent-encoding and raw control characters
        String raw = candidate;
        String lower = raw.toLowerCase();
        if (lower.contains("%0a") || lower.contains("%0d") || raw.contains("\n") || raw.contains("\r")) {
            throw new InvalidUrlException("Control characters in URL are not allowed");
        }

        // Validate port if present
        int port = uri.getPort();
        if (port != -1 && (port <= 0 || port > 65535)) {
            throw new InvalidUrlException("URL port is out of range");
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress()) {
                throw new InvalidUrlException("Private or local network targets are not allowed");
            }
        } catch (UnknownHostException ignored) {
            // DNS lookup failure is allowed — host may be reachable later; no additional check.
        }

        // Rebuild normalized URI without fragment to avoid client-side-only fragments
        try {
            String normalized = new URI(
                    uri.getScheme().toLowerCase(),
                    null,
                    uri.getHost().toLowerCase(),
                    uri.getPort(),
                    uri.getPath() == null ? "/" : uri.getPath(),
                    uri.getQuery(),
                    null
            ).toString();
            return normalized;
        } catch (URISyntaxException e) {
            // Fallback to original candidate if rebuild fails
            return candidate;
        }
    }
}
