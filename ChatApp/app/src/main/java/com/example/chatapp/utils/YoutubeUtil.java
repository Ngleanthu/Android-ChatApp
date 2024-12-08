package com.example.chatapp.utils;
import android.content.Context;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class YoutubeUtil {
    // Checks if the message contains any YouTube link
    public static boolean containsYouTubeLink(String message) {
        String youtubeRegex = "(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/[\\w?=&-]+";
        Pattern pattern = Pattern.compile(youtubeRegex);
        Matcher matcher = pattern.matcher(message);
        return matcher.find();
    }

    // Extracts the first YouTube video ID from a message
    public static String extractYouTubeId(String message) {
        String pattern = "(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([^&\\s]+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static void addYouTubeWebView(LinearLayout parentLayout, String videoId, Context context) {
// Set the layout parameters for the WebView
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Width: match_parent
                (int) (200 * context.getResources().getDisplayMetrics().density) // Height: 250dp converted to pixels
        );
        WebView webView = new WebView(context);
        String video = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+videoId+"\""+ "title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>";
        webView.loadData(video, "text/html","utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setLayoutParams(layoutParams);

// Add WebView to a parent layout (e.g., RelativeLayout or LinearLayout)
        parentLayout.addView(webView);

    }


}
