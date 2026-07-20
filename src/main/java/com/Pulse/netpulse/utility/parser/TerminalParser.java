package com.Pulse.netpulse.utility.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TerminalParser {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private boolean headerSent = false;

    private static final Pattern CLEANER = Pattern.compile("(\u001b\\[[;?0-9]*[a-zA-Z])|(\u001b\\]3008;[^\u0007]*(\u0007|\\\\))|(]3008;)|(start=[^\\n]*?(\u001b\\\\|$))|(end=[^\\n]*?(\u001b\\\\|$))|(\r\n\r)");

    private String currentHostname = "Connecting...";

    public String parseToStructuredJson(String rawInput) {

        String cleaned = CLEANER.matcher(rawInput).replaceAll("");
        cleaned = cleaned.trim();

        if (cleaned.isEmpty()) return null;

        String type = classifyLine(cleaned);

        if ("PROMPT".equals(type)) {
            this.currentHostname = cleaned.replace(":~$", "").replace(":$", "");
            return null;
        }

        Map<String, Object> jsonMap = new LinkedHashMap<>();

        jsonMap.put("hostname", this.currentHostname);

        // (Optional) Initial header
        if (!headerSent) {
            jsonMap.put("metadata", "First-Connection");
            headerSent = true;
        }

        jsonMap.put("timestamp", LocalDateTime.now().format(timeFormatter));
        jsonMap.put("type", type);
        jsonMap.put("content", cleaned);

        try {
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            return null;
        }
    }

    private String classifyLine(String content) {
        if (content.contains("error") || content.contains("failed")) return "ERROR";
        if (content.contains("success")) return "SUCCESS";
        if (content.contains("admin@")) return "PROMPT";
        return "DEFAULT";
    }
}