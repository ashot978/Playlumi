package nl.playlumi.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Random;

public class MainActivity extends Activity {
    private CountingGame game;
    private LinearLayout root, choices;
    private TextView prompt, stars, feedback, progress;
    private Button next;
    private int completed;
    private String message = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        game = new CountingGame(new Random());
        completed = getPreferences(MODE_PRIVATE).getInt("completed", 0);
        if (state != null) {
            game.restore(state.getInt("answer"), state.getBoolean("solved"));
            message = state.getString("message", "");
        }
        buildScreen();
        render();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("answer", game.answer());
        state.putBoolean("solved", game.isSolved());
        state.putString("message", message);
        super.onSaveInstanceState(state);
    }

    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(Color.rgb(239, 249, 244));
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24)); root.setGravity(Gravity.CENTER);
        scroll.addView(root); setContentView(scroll);
        TextView title = text("Playlumi", 28, Color.rgb(33, 77, 69)); root.addView(title);
        progress = text("", 15, Color.rgb(67, 95, 88)); root.addView(progress);
        prompt = text("Hoeveel sterren zie je?", 24, Color.rgb(31, 55, 51));
        LinearLayout.LayoutParams promptParams = new LinearLayout.LayoutParams(-1, -2); promptParams.setMargins(0, dp(30), 0, dp(14)); root.addView(prompt, promptParams);
        stars = text("", 42, Color.rgb(163, 93, 0)); stars.setPadding(dp(12), dp(24), dp(12), dp(24));
        stars.setBackground(background(Color.WHITE, 8)); root.addView(stars, new LinearLayout.LayoutParams(-1, -2));
        feedback = text("", 19, Color.rgb(42, 91, 70)); feedback.setMinHeight(dp(58)); root.addView(feedback);
        choices = new LinearLayout(this); choices.setGravity(Gravity.CENTER); choices.setOrientation(LinearLayout.HORIZONTAL); root.addView(choices, new LinearLayout.LayoutParams(-1, -2));
        for (int i = 1; i <= 5; i++) {
            final int number = i; Button b = new Button(this); b.setText(String.valueOf(i)); b.setTextSize(24); b.setAllCaps(false);
            b.setContentDescription("Antwoord " + i); b.setMinWidth(0); b.setMinimumWidth(0); b.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(64), 1); p.setMargins(dp(2), 0, dp(2), 0); choices.addView(b, p);
            b.setOnClickListener(v -> choose(number));
        }
        next = new Button(this); next.setText("Nog een keer"); next.setTextSize(20); next.setAllCaps(false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, dp(64)); np.setMargins(0, dp(16), 0, 0); root.addView(next, np);
        next.setOnClickListener(v -> { game.nextRound(); message = ""; render(); });
    }
    private void choose(int number) {
        if (game.isSolved()) return;
        if (game.choose(number)) {
            completed++;
            getPreferences(MODE_PRIVATE).edit().putInt("completed", completed).apply();
            message = "✓ Goed geteld!";
        } else message = "Probeer het nog eens.";
        render(); feedback.announceForAccessibility(message);
    }
    private void render() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < game.answer(); i++) { if (i > 0) display.append(' '); display.append('★'); }
        stars.setText(display.toString()); stars.setContentDescription(game.answer() + " sterren");
        progress.setText("Samen geteld: " + completed);
        feedback.setText(message);
        for (int i = 0; i < choices.getChildCount(); i++) choices.getChildAt(i).setEnabled(!game.isSolved());
        next.setVisibility(game.isSolved() ? View.VISIBLE : View.INVISIBLE);
    }
}
