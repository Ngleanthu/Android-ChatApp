package com.example.chatapp.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmojiConverter {
    // Define a mapping of text symbols to emojis
    private static final Map<String, String> emojiMap = new HashMap<>();

    static {
        emojiMap.put("O:)", "\uD83D\uDE07");
        emojiMap.put(":)", "\uD83D\uDE00");
        emojiMap.put(":(", "\uD83D\uDE1E");
        emojiMap.put(";)", "\uD83D\uDE09");
        emojiMap.put(":D", "\uD83D\uDE03");
        emojiMap.put(":P", "\uD83D\uDE1B");
        emojiMap.put(";P", "\uD83D\uDE1C");
        emojiMap.put(":O", "\uD83D\uDE2E");
        emojiMap.put(":|", "\uD83D\uDE10");
        emojiMap.put(":S", "\uD83D\uDE16");
        emojiMap.put(":'(", "\uD83D\uDE22");
        emojiMap.put("XD", "\uD83D\uDE02");
        emojiMap.put("<3", "❤️");
        emojiMap.put("</3", "\uD83D\uDC94");
        emojiMap.put(":')", "\uD83D\uDE02");
        emojiMap.put(":o", "\uD83D\uDE32");
        emojiMap.put(":x", "\uD83D\uDE36");
        emojiMap.put("B)", "\uD83D\uDE0E");
        emojiMap.put(">:(", "\uD83D\uDE21");
        emojiMap.put(":/", "\uD83D\uDE15");
        emojiMap.put("-_-", "\uD83D\uDE11");
        emojiMap.put("^_^", "\uD83D\uDE0A");
        emojiMap.put("T_T", "\uD83D\uDE2D");
        emojiMap.put(">:D", "\uD83D\uDE06");
        emojiMap.put("D:", "\uD83D\uDE27");
        emojiMap.put("o.O", "\uD83D\uDE33");
        emojiMap.put(">:O", "\uD83D\uDE32");
        emojiMap.put("xD", "\uD83D\uDE02");
        emojiMap.put(":-*", "\uD83D\uDE18");
        emojiMap.put(":3", "\uD83D\uDE3A");
        emojiMap.put(":v", "\uD83D\uDE0B");
    }

    // Method to replace symbols with emojis
    public static String replaceWithEmojis(String text) {
        List<String> keys = new ArrayList<>(emojiMap.keySet());
        keys.sort((a, b) -> Integer.compare(b.length(), a.length())); // Descending order by length

        // Perform replacements
        for (String emoticon : keys) {
            text = text.replace(emoticon, emojiMap.get(emoticon));
        }
        return text;
    }
}

