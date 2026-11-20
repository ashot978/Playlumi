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
import android.widget.GridLayout;
import java.util.Random;

public class MainActivity extends Activity {
    private CountingGame game;
    private MatchingGame colors, letters;
    private LinearLayout root;
    private LinearLayout worlds, levels;
    private GridLayout choices;
    private TextView prompt, stars, feedback, progress;
    private Button next;
    private int completed;
    private String message = "";
    private int world;
    private int mode;
    private int matched;
    private static final String[] NAMES = {"sterren", "bloemen", "ballonnen"};
    private static final String[] SYMBOLS = {"★", "✿", "🎈"};
    private static final String[] COLOR_NAMES = {"rood", "blauw", "groen", "geel"};
    private static final String[] SHAPES = {"●", "■", "▲", "◆"};
    private static final int[] COLOR_VALUES = {0xffb52727, 0xff1762a5, 0xff26773d, 0xff936800};
    private static final String[] UPPER = {"A", "B", "M", "S", "O"};
    private static final String[] LOWER = {"a", "b", "m", "s", "o"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        game = new CountingGame(new Random());
        colors = new MatchingGame(new Random(), COLOR_NAMES.length);
        letters = new MatchingGame(new Random(), UPPER.length);
        game.setMaximum(getPreferences(MODE_PRIVATE).getInt("maximum", 5));
        world = getPreferences(MODE_PRIVATE).getInt("world", 0);
        completed = getPreferences(MODE_PRIVATE).getInt("completed", 0);
        matched = getPreferences(MODE_PRIVATE).getInt("matched", 0);
        if (state != null) {
            game.restore(state.getInt("answer"), state.getBoolean("solved"));
            colors.restore(state.getInt("color"), state.getBoolean("colorSolved"));
            letters.restore(state.getInt("letter"), state.getBoolean("letterSolved"));
            mode = state.getInt("mode");
            message = state.getString("message", "");
        }
        buildScreen();
        render();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("answer", game.answer());
        state.putBoolean("solved", game.isSolved());
        state.putInt("color", colors.answer()); state.putBoolean("colorSolved", colors.isSolved());
        state.putInt("letter", letters.answer()); state.putBoolean("letterSolved", letters.isSolved());
        state.putInt("mode", mode);
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
        LinearLayout games = new LinearLayout(this); games.setGravity(Gravity.CENTER); root.addView(games);
        String[] gameNames = {"Tellen", "Kleuren", "Letters"};
        for (int i = 0; i < gameNames.length; i++) {
            final int selected = i;
            Button b = new Button(this); b.setText(gameNames[i]); b.setAllCaps(false);
            games.addView(b, new LinearLayout.LayoutParams(0, dp(56), 1));
            b.setOnClickListener(v -> { mode = selected; message = ""; render(); });
        }
        worlds = new LinearLayout(this); worlds.setGravity(Gravity.CENTER); root.addView(worlds);
        for (int i = 0; i < NAMES.length; i++) {
            final int selected = i;
            Button b = new Button(this); b.setText(NAMES[i]); b.setAllCaps(false);
            worlds.addView(b, new LinearLayout.LayoutParams(0, dp(56), 1));
            b.setOnClickListener(v -> { world = selected; getPreferences(MODE_PRIVATE).edit().putInt("world", world).apply(); game.nextRound(); message = ""; render(); });
        }
        levels = new LinearLayout(this); levels.setGravity(Gravity.CENTER); root.addView(levels);
        for (int limit : new int[]{3, 5, 10}) {
            Button b = new Button(this); b.setText("Tot " + limit); b.setAllCaps(false);
            levels.addView(b, new LinearLayout.LayoutParams(0, dp(56), 1));
            b.setOnClickListener(v -> { game.setMaximum(limit); getPreferences(MODE_PRIVATE).edit().putInt("maximum", limit).apply(); message = ""; render(); });
        }
        prompt = text("", 24, Color.rgb(31, 55, 51));
        LinearLayout.LayoutParams promptParams = new LinearLayout.LayoutParams(-1, -2); promptParams.setMargins(0, dp(30), 0, dp(14)); root.addView(prompt, promptParams);
        stars = text("", 42, Color.rgb(163, 93, 0)); stars.setPadding(dp(12), dp(24), dp(12), dp(24));
        stars.setBackground(background(Color.WHITE, 8)); root.addView(stars, new LinearLayout.LayoutParams(-1, -2));
        feedback = text("", 19, Color.rgb(42, 91, 70)); feedback.setMinHeight(dp(58)); root.addView(feedback);
        choices = new GridLayout(this); choices.setColumnCount(5); choices.setAlignmentMode(GridLayout.ALIGN_BOUNDS); root.addView(choices, new LinearLayout.LayoutParams(-1, -2));
        for (int i = 1; i <= 10; i++) {
            final int number = i; Button b = new Button(this); b.setText(String.valueOf(i)); b.setTextSize(24); b.setAllCaps(false);
            b.setContentDescription("Antwoord " + i); b.setMinWidth(0); b.setMinimumWidth(0); b.setPadding(0, 0, 0, 0);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams(); p.width = 0; p.height = dp(64); p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); p.setMargins(dp(2), 0, dp(2), 0); choices.addView(b, p);
            b.setOnClickListener(v -> choose(number));
        }
        next = new Button(this); next.setText("Nog een keer"); next.setTextSize(20); next.setAllCaps(false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, dp(64)); np.setMargins(0, dp(16), 0, 0); root.addView(next, np);
        next.setOnClickListener(v -> { if (mode == 0) game.nextRound(); else if (mode == 1) colors.nextRound(); else letters.nextRound(); message = ""; render(); });
    }
    private void choose(int number) {
        boolean solved = mode == 0 ? game.isSolved() : mode == 1 ? colors.isSolved() : letters.isSolved();
        if (solved) return;
        boolean correct = mode == 0 ? game.choose(number) : mode == 1 ? colors.choose(number - 1) : letters.choose(number - 1);
        if (correct) {
            if (mode == 0) { completed++; getPreferences(MODE_PRIVATE).edit().putInt("completed", completed).apply(); }
            else { matched++; getPreferences(MODE_PRIVATE).edit().putInt("matched", matched).apply(); }
            message = "✓ Goed gedaan!";
        } else message = "Probeer het nog eens.";
        render(); feedback.announceForAccessibility(message);
    }
    private void render() {
        worlds.setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        levels.setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        int count = mode == 0 ? game.maximum() : mode == 1 ? COLOR_NAMES.length : LOWER.length;
        boolean solved = mode == 0 ? game.isSolved() : mode == 1 ? colors.isSolved() : letters.isSolved();
        stars.setTextColor(mode == 1 ? COLOR_VALUES[colors.answer()] : Color.rgb(163, 93, 0));
        if (mode == 0) {
            StringBuilder display = new StringBuilder();
            for (int i = 0; i < game.answer(); i++) { if (i > 0) display.append(i % 5 == 0 ? '\n' : ' '); display.append(SYMBOLS[world]); }
            prompt.setText("Hoeveel " + NAMES[world] + " zie je?");
            stars.setText(display.toString()); stars.setContentDescription(game.answer() + " " + NAMES[world]);
        } else if (mode == 1) {
            prompt.setText("Zoek dezelfde kleur en vorm");
            stars.setText(SHAPES[colors.answer()]); stars.setContentDescription(COLOR_NAMES[colors.answer()] + " " + SHAPES[colors.answer()]);
        } else {
            prompt.setText("Zoek de kleine letter");
            stars.setText(UPPER[letters.answer()]); stars.setContentDescription("Hoofdletter " + UPPER[letters.answer()]);
        }
        progress.setText("Geteld: " + completed + "  •  Gevonden: " + matched);
        feedback.setText(message);
        for (int i = 0; i < choices.getChildCount(); i++) {
            Button b = (Button) choices.getChildAt(i);
            b.setVisibility(i < count ? View.VISIBLE : View.GONE); b.setEnabled(!solved);
            if (i < count) {
                String label = mode == 0 ? String.valueOf(i + 1) : mode == 1 ? SHAPES[i] : LOWER[i];
                b.setText(label); b.setTextColor(mode == 1 ? COLOR_VALUES[i] : Color.BLACK);
                b.setContentDescription(mode == 1 ? COLOR_NAMES[i] + " " + SHAPES[i] : "Antwoord " + label);
            }
        }
        next.setVisibility(solved ? View.VISIBLE : View.INVISIBLE);
    }
}
