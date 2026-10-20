package com.oimc.aimin.agent.retrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中文/中英混合分词：连续汉字切成 bigram（无词典依赖，适合医学长尾术语），
 * 英文与数字保留完整 token（如 curb、65、sglt2）。
 */
public final class ChineseTokenizer {

    private static final Pattern TOKEN = Pattern.compile("[\\u4e00-\\u9fa5]+|[a-z0-9][a-z0-9._-]*");

    private ChineseTokenizer() {
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return tokens;
        }
        Matcher matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group();
            if (isCjk(token)) {
                if (token.length() == 1) {
                    tokens.add(token);
                } else {
                    for (int i = 0; i < token.length() - 1; i++) {
                        tokens.add(token.substring(i, i + 2));
                    }
                }
            } else if (token.length() >= 2 || token.matches("\\d+")) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static boolean isCjk(String token) {
        return token.charAt(0) >= '\u4e00' && token.charAt(0) <= '\u9fa5';
    }
}
