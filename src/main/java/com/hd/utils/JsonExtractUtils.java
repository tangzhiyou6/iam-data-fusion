package com.hd.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

public final class JsonExtractUtils {

    private JsonExtractUtils() {
    }

    private static final ObjectMapper LENIENT_MAPPER = createLenientMapper();

    /** Markdown 代码块（标准多行） */
    private static final Pattern MARKDOWN_FENCE = Pattern.compile(
            "```(?:json|JSON|Json)?\\s*\\r?\\n([\\s\\S]*?)\\r?\\n\\s*```",
            Pattern.MULTILINE);

    private static final Pattern MARKDOWN_FENCE_ANY = Pattern.compile(
            "```(?:json|JSON|Json)?\\s*([\\s\\S]*?)\\s*```");

    private static final Pattern INVISIBLE_CHARS = Pattern.compile(
            "[\\uFEFF\\u200B\\u200C\\u200D\\u2060\\u00AD]");

    private static final int MAX_CANDIDATES = 128;

    public static String extract(String text) {
        return tryExtract(text).orElseThrow(() ->
                new IllegalArgumentException("未能从文本中提取到有效 JSON，长度="
                        + (text == null ? 0 : text.length())));
    }

    public static Optional<String> tryExtract(String text) {
        ExtractResult result = extractDetail(text);
        return result.isSuccess() ? Optional.of(result.getJson()) : Optional.empty();
    }

    public static ExtractResult extractDetail(String text) {
        if (StringUtils.isBlank(text)) {
            return ExtractResult.fail("输入为空");
        }

        // 先轻量清洗（保留中文引号等正文标点，避免破坏合法 JSON 字符串内容）
        ExtractResult light = extractDetailOnNormalized(preprocessLight(text));
        if (light.isSuccess()) {
            return light;
        }

        // 再尝试把智能引号当 JSON 定界符的脏格式（仅作回退，不作为默认预处理）
        String heavy = preprocessHeavy(text);
        if (!heavy.equals(preprocessLight(text))) {
            ExtractResult heavyResult = extractDetailOnNormalized(heavy);
            if (heavyResult.isSuccess()) {
                return heavyResult.isRepaired()
                        ? heavyResult
                        : ExtractResult.ok(heavyResult.getJson(), heavyResult.getStrategy(), true);
            }
        }

        return light;
    }

    private static ExtractResult extractDetailOnNormalized(String normalized) {
        // 1) 整段即 JSON（必须完整消费，禁止 readTree 静默丢弃后续值）
        Optional<ParsedJson> whole = parseStrict(normalized, 0);
        if (whole.isPresent()) {
            return ExtractResult.ok(whole.get().json, Strategy.WHOLE, false);
        }

        // 2) Markdown 代码块：整块解析 + 块内平衡扫描，选最优（优先完整数组）
        ExtractResult fromFence = pickBestFromSources(
                extractMarkdownBlocks(normalized), Strategy.MARKDOWN_FENCE);
        if (fromFence.isSuccess()) {
            return fromFence;
        }

        // 3) 全文括号平衡扫描；多个顶层值合并为数组
        ExtractResult fromScan = extractByBalancedScan(normalized);
        if (fromScan.isSuccess()) {
            return fromScan;
        }

        // 4) 整段软修复
        Optional<String> repairedWhole = tryRepairAndValidate(normalized);
        if (repairedWhole.isPresent()) {
            return ExtractResult.ok(repairedWhole.get(), Strategy.REPAIR, true);
        }

        return ExtractResult.fail("未找到可解析的 JSON 对象或数组");
    }

    public static <T> T extractAs(String text, Class<T> clazz) {
        String json = extract(text);
        try {
            return LENIENT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T extractAs(String text, TypeReference<T> typeRef) {
        String json = extract(text);
        try {
            return LENIENT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + typeRef.getType(), e);
        }
    }

    public static JsonNode extractAsNode(String text) {
        String json = extract(text);
        try {
            return LENIENT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 解析为 JsonNode 失败", e);
        }
    }

    public static Map<String, Object> extractAsMap(String text) {
        return extractAs(text, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> List<T> extractAsList(String text, Class<T> elementType) {
        String json = extract(text);
        try {
            return LENIENT_MAPPER.readValue(json,
                    LENIENT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 反序列化为 List 失败: " + elementType.getSimpleName(), e);
        }
    }

    public static boolean containsJson(String text) {
        return tryExtract(text).isPresent();
    }

    private static ExtractResult pickBestFromSources(List<String> sources, Strategy strategy) {
        ParsedJson best = null;
        boolean bestRepaired = false;
        for (String source : sources) {
            if (StringUtils.isBlank(source)) {
                continue;
            }
            Optional<ParsedJson> direct = parseStrict(source, -1);
            if (direct.isPresent()) {
                if (best == null || isBetter(direct.get(), best)) {
                    best = direct.get();
                    bestRepaired = false;
                }
            } else {
                Optional<String> repaired = tryRepairAndValidate(source);
                if (repaired.isPresent()) {
                    Optional<ParsedJson> rp = parseStrict(repaired.get(), -1);
                    if (rp.isPresent() && (best == null || isBetter(rp.get(), best))) {
                        best = rp.get();
                        bestRepaired = true;
                    }
                }
            }
            ExtractResult nested = extractByBalancedScan(source);
            if (nested.isSuccess()) {
                Optional<ParsedJson> nestedParsed = parseStrict(nested.getJson(), -1);
                if (nestedParsed.isPresent() && (best == null || isBetter(nestedParsed.get(), best))) {
                    best = nestedParsed.get();
                    bestRepaired = nested.isRepaired();
                }
            }
        }
        if (best != null) {
            return ExtractResult.ok(best.json, strategy, bestRepaired);
        }
        return ExtractResult.fail("无候选");
    }

    private static ExtractResult extractByBalancedScan(String text) {
        List<CandidateSpan> spans = scanBalancedCandidates(text);
        List<ParsedJson> parsed = new ArrayList<>();
        boolean anyRepaired = false;

        for (CandidateSpan span : spans) {
            Optional<ParsedJson> valid = parseStrict(span.text, span.start);
            if (valid.isPresent()) {
                parsed.add(valid.get());
                continue;
            }
            Optional<String> repaired = tryRepairAndValidate(span.text);
            if (repaired.isPresent()) {
                Optional<ParsedJson> repairedParsed = parseStrict(repaired.get(), span.start);
                if (repairedParsed.isPresent()) {
                    parsed.add(repairedParsed.get());
                    anyRepaired = true;
                }
            }
        }
        if (parsed.isEmpty()) {
            return ExtractResult.fail("平衡扫描无合法 JSON");
        }

        // 多个互不嵌套的顶层值 → 合并为完整数组（避免只返回第一个对象）
        List<ParsedJson> topLevel = selectTopLevelValues(parsed);
        if (topLevel.size() > 1) {
            try {
                List<JsonNode> nodes = new ArrayList<>(topLevel.size());
                for (ParsedJson p : topLevel) {
                    nodes.add(p.node);
                }
                return ExtractResult.ok(LENIENT_MAPPER.writeValueAsString(nodes),
                        Strategy.BALANCED_SCAN, anyRepaired);
            } catch (JsonProcessingException e) {
                // fall through
            }
        }

        return ExtractResult.ok(pickBestParsed(parsed).json, Strategy.BALANCED_SCAN, anyRepaired);
    }

    private static List<ParsedJson> selectTopLevelValues(List<ParsedJson> parsed) {
        List<ParsedJson> sorted = new ArrayList<>(parsed);
        sorted.sort((a, b) -> Integer.compare(b.rawLength, a.rawLength));
        List<ParsedJson> top = new ArrayList<>();
        for (ParsedJson cur : sorted) {
            boolean covered = false;
            for (ParsedJson existing : top) {
                if (existing.rawIndex >= 0 && cur.rawIndex >= 0
                        && existing.rawLength > cur.rawLength
                        && cur.rawIndex >= existing.rawIndex
                        && cur.rawIndex + cur.rawLength <= existing.rawIndex + existing.rawLength) {
                    covered = true;
                    break;
                }
                if ((existing.rawIndex < 0 || cur.rawIndex < 0)
                        && existing.json.length() > cur.json.length()
                        && existing.json.contains(cur.json)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                top.add(cur);
            }
        }
        top.sort(Comparator.comparingInt(p -> p.rawIndex < 0 ? Integer.MAX_VALUE : p.rawIndex));
        return top;
    }

    private static ParsedJson pickBestParsed(List<ParsedJson> parsed) {
        ParsedJson best = parsed.get(0);
        for (int i = 1; i < parsed.size(); i++) {
            if (isBetter(parsed.get(i), best)) {
                best = parsed.get(i);
            }
        }
        return best;
    }

    private static boolean isBetter(ParsedJson candidate, ParsedJson current) {
        if (candidate.json.length() != current.json.length()) {
            return candidate.json.length() > current.json.length();
        }
        if (candidate.node.isArray() != current.node.isArray()) {
            return candidate.node.isArray();
        }
        if (candidate.node.isArray() && current.node.isArray()
                && candidate.node.size() != current.node.size()) {
            return candidate.node.size() > current.node.size();
        }
        return false;
    }

    private static String preprocessLight(String text) {
        String s = text.trim();
        s = INVISIBLE_CHARS.matcher(s).replaceAll("");
        s = s
                .replace('\uFF5B', '{').replace('\uFF5D', '}')
                .replace('\uFF3B', '[').replace('\uFF3D', ']');
        return s.trim();
    }

    private static String preprocessHeavy(String text) {
        String s = preprocessLight(text);
        return s
                .replace('\u201C', '"').replace('\u201D', '"')
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u300C', '"').replace('\u300D', '"')
                .replace('\u300E', '"').replace('\u300F', '"')
                .replace('\uFF02', '"').replace('\uFF07', '\'');
    }

    private static List<String> extractMarkdownBlocks(String text) {
        List<String> blocks = new ArrayList<>();
        Matcher m = MARKDOWN_FENCE.matcher(text);
        while (m.find()) {
            String body = StringUtils.trimToEmpty(m.group(1));
            if (!body.isEmpty()) {
                blocks.add(body);
            }
        }
        Matcher m2 = MARKDOWN_FENCE_ANY.matcher(text);
        while (m2.find()) {
            String body = StringUtils.trimToEmpty(m2.group(1));
            if (!body.isEmpty() && !blocks.contains(body)) {
                blocks.add(body);
            }
        }
        return blocks;
    }

    private static List<CandidateSpan> scanBalancedCandidates(String text) {
        List<CandidateSpan> candidates = new ArrayList<>();
        int n = text.length();
        for (int i = 0; i < n && candidates.size() < MAX_CANDIDATES; i++) {
            char c = text.charAt(i);
            if (c != '{' && c != '[') {
                continue;
            }
            int end = findBalancedEnd(text, i);
            if (end > i) {
                candidates.add(new CandidateSpan(text.substring(i, end + 1), i));
            }
        }
        // 外层（更长）优先保留
        candidates.sort((a, b) -> Integer.compare(b.text.length(), a.text.length()));
        if (candidates.size() > MAX_CANDIDATES) {
            return new ArrayList<>(candidates.subList(0, MAX_CANDIDATES));
        }
        return candidates;
    }

    private static int findBalancedEnd(String text, int start) {
        char first = text.charAt(start);
        if (first != '{' && first != '[') {
            return -1;
        }
        Deque<Character> stack = new ArrayDeque<>();
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '{' || c == '[') {
                stack.push(c);
            } else if (c == '}' || c == ']') {
                if (stack.isEmpty()) {
                    return -1;
                }
                char top = stack.peek();
                boolean match = (c == '}' && top == '{') || (c == ']' && top == '[');
                if (!match) {
                    return -1;
                }
                stack.pop();
                if (stack.isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    // ==================== 校验 / 修复 ====================

    private static Optional<String> tryRepairAndValidate(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Optional.empty();
        }
        return parseStrict(softRepair(raw.trim()), -1).map(p -> p.json);
    }

    private static Optional<ParsedJson> parseStrict(String candidate, int rawIndex) {
        if (StringUtils.isBlank(candidate)) {
            return Optional.empty();
        }
        String trimmed = candidate.trim();
        int trimOffset = candidate.indexOf(trimmed);
        char first = trimmed.charAt(0);
        if (first != '{' && first != '[') {
            return Optional.empty();
        }
        JsonParser parser = null;
        try {
            parser = LENIENT_MAPPER.getFactory().createParser(trimmed);
            JsonNode node = LENIENT_MAPPER.readTree(parser);
            if (node == null || !node.isContainerNode()) {
                return Optional.empty();
            }
            JsonToken trailing = parser.nextToken();
            if (trailing != null) {
                return Optional.empty();
            }
            String normalizedJson = LENIENT_MAPPER.writeValueAsString(node);
            int index = rawIndex < 0 ? -1 : rawIndex + Math.max(trimOffset, 0);
            return Optional.of(new ParsedJson(normalizedJson, node, index, trimmed.length()));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (Exception ignored) {
                    /* ignore */
                }
            }
        }
    }

    private static String softRepair(String json) {
        String s = json;
        s = stripLineCommentsOutsideStrings(s);
        s = stripBlockCommentsOutsideStrings(s);
        s = s.replaceAll(",\\s*}", "}");
        s = s.replaceAll(",\\s*]", "]");
        return s.trim();
    }

    private static String stripLineCommentsOutsideStrings(String s) {
        StringBuilder out = new StringBuilder(s.length());
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                out.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }
            if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/') {
                i += 2;
                while (i < s.length() && s.charAt(i) != '\n' && s.charAt(i) != '\r') {
                    i++;
                }
                i--;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private static String stripBlockCommentsOutsideStrings(String s) {
        StringBuilder out = new StringBuilder(s.length());
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                out.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }
            if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/')) {
                    i++;
                }
                i++;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private static ObjectMapper createLenientMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_YAML_COMMENTS.mappedFeature());
        return mapper;
    }

    public enum Strategy {
        WHOLE,
        MARKDOWN_FENCE,
        BALANCED_SCAN,
        REPAIR,
        NONE
    }

    public static final class ExtractResult {
        private final boolean success;
        private final String json;
        private final Strategy strategy;
        private final boolean repaired;
        private final String message;

        private ExtractResult(boolean success, String json, Strategy strategy,
                              boolean repaired, String message) {
            this.success = success;
            this.json = json;
            this.strategy = strategy;
            this.repaired = repaired;
            this.message = message;
        }

        static ExtractResult ok(String json, Strategy strategy, boolean repaired) {
            return new ExtractResult(true, json, strategy, repaired, "OK");
        }

        static ExtractResult fail(String message) {
            return new ExtractResult(false, null, Strategy.NONE, false, message);
        }

        public Optional<String> toOptional() {
            return success ? Optional.of(json) : Optional.empty();
        }

        public boolean isSuccess() {
            return success;
        }

        public String getJson() {
            return json;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public boolean isRepaired() {
            return repaired;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final class CandidateSpan {
        private final String text;
        private final int start;

        private CandidateSpan(String text, int start) {
            this.text = text;
            this.start = start;
        }
    }

    private static final class ParsedJson {
        private final String json;
        private final JsonNode node;
        private final int rawIndex;
        private final int rawLength;

        private ParsedJson(String json, JsonNode node, int rawIndex, int rawLength) {
            this.json = json;
            this.node = node;
            this.rawIndex = rawIndex;
            this.rawLength = rawLength;
        }
    }
}