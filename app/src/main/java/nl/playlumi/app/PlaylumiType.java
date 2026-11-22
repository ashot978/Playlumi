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
        int firstBreak = body.indexOf('\n');
        int secondBreak = firstBreak < 0 ? -1 : body.indexOf('\n', firstBreak + 1);
        if (secondBreak >= 0) {
            // Category cards: giant illustration, large category name, small explanation.
            styled.setSpan(new RelativeSizeSpan(1.6f), 0, firstBreak, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new RelativeSizeSpan(1.02f), firstBreak + 1, secondBreak,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new StyleSpan(Typeface.BOLD), firstBreak + 1, secondBreak,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new RelativeSizeSpan(.78f), secondBreak + 1, body.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new ForegroundColorSpan(PlaylumiVisualStyle.MUTED), secondBreak + 1, body.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (firstBreak >= 0) {
            styled.setSpan(new StyleSpan(Typeface.BOLD), 0, firstBreak, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (containsEmoji(body.substring(0, firstBreak))) {
                int split = body.indexOf(' ');
                if (split < 0 || split > firstBreak) split = firstBreak;
                styled.setSpan(new RelativeSizeSpan(1.30f), 0, split, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            styled.setSpan(new RelativeSizeSpan(description.isEmpty() ? 1.0f : .86f),
                    firstBreak + 1, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!description.isEmpty()) styled.setSpan(new ForegroundColorSpan(PlaylumiVisualStyle.MUTED),
                    firstBreak + 1, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (containsEmoji(body)) {
            int split=body.indexOf(' ');
            if(split>0)styled.setSpan(new RelativeSizeSpan(1.27f),0,split,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
