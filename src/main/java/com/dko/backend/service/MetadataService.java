package com.dko.backend.service;

import com.dko.backend.dto.MetadataResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class MetadataService {

    public MetadataResponse fetchMetadata(String urlString) {
        try {
            // Add protocol if missing for Jsoup
            String finalUrl = urlString;
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                finalUrl = "https://" + finalUrl;
            }

            Document doc = Jsoup.connect(finalUrl)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            String title = doc.title();

            // Check for common "blocked" or "security" titles
            if (isBlockedTitle(title)) {
                title = extractTitleFromHostname(finalUrl);
            }

            if (title == null || title.isEmpty()) {
                title = extractTitleFromHostname(finalUrl);
            }

            String favicon = extractFavicon(doc, finalUrl);

            return new MetadataResponse(title, favicon);
        } catch (Exception e) {
            // Fallback: use hostname as title
            String fallbackTitle = extractTitleFromHostname(urlString);
            return new MetadataResponse(fallbackTitle, null);
        }
    }

    private boolean isBlockedTitle(String title) {
        if (title == null)
            return false;
        String lowTitle = title.toLowerCase();
        return lowTitle.contains("blocked") ||
                lowTitle.contains("security check") ||
                lowTitle.contains("robot check") ||
                lowTitle.contains("captcha") ||
                lowTitle.contains("access denied") ||
                lowTitle.contains("just a moment");
    }

    private String extractTitleFromHostname(String urlString) {
        try {
            String host = new URL(urlString.startsWith("http") ? urlString : "https://" + urlString).getAuthority();
            if (host.startsWith("www."))
                host = host.substring(4);
            // Capitalize first letter
            return host.substring(0, 1).toUpperCase() + host.substring(1);
        } catch (Exception e) {
            return urlString;
        }
    }

    private String extractFavicon(Document doc, String baseUrl) {
        try {
            // 1. Try <link rel="icon">
            Element icon = doc.select("link[rel~=(?i)^(shortcut|icon)$]").first();
            if (icon != null) {
                return icon.attr("abs:href");
            }

            // 2. Try default /favicon.ico
            URL url = new URL(baseUrl);
            return url.getProtocol() + "://" + url.getAuthority() + "/favicon.ico";
        } catch (Exception e) {
            return null;
        }
    }
}
