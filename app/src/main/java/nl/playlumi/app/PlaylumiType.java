package nl.playlumi.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

/** Child-friendly type hierarchy inside the existing native Android tiles. */
public final class PlaylumiType {
    private PlaylumiType() {}

    public static SpannableString tileText(String title, String description) {
        String body = description.isEmpty() ? title : title + "\n" + description;
        SpannableString styled = new SpannableString(body);
        int newline = body.indexOf('\n');
        int firstLineEnd = newline < 0 ? body.length() : newline;
        if (firstLineEnd > 0) {
            styled.setSpan(new StyleSpan(Typeface.BOLD), 0, firstLineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (newline >= 0) {
            String prefix = body.substring(0, newline);
            // A leading pictograph gets to be the visual icon, not another small letter.
            if (containsEmoji(prefix)) {
                int iconEnd = prefix.indexOf(' ');
                if (iconEnd < 0) iconEnd = prefix.length();
                if (iconEnd > 0) styled.setSpan(new RelativeSizeSpan(1.8f), 0, iconEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (newline + 1 < body.length()) {
                styled.setSpan(new RelativeSizeSpan(description.isEmpty() ? 1.12f : .76f),
                        newline + 1, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (!description.isEmpty()) styled.setSpan(new ForegroundColorSpan(Color.rgb(76, 93, 113)),
                        newline + 1, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else if (containsEmoji(body)) {
            int split=body.indexOf(' ');
            if(split>0)styled.setSpan(new RelativeSizeSpan(1.5f),0,split,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return styled;
    }

    public static SpannableString emphasizedQuestion(String question) {
        SpannableString styled = new SpannableString(question);
        if(question.length()>0)styled.setSpan(new StyleSpan(Typeface.BOLD),0,question.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styled;
    }
    private static boolean containsEmoji(String text) {
        return !text.isEmpty() && (Character.getType(text.codePointAt(0)) == Character.OTHER_SYMBOL ||
                Character.isSurrogate(text.charAt(0)));
    }
}
