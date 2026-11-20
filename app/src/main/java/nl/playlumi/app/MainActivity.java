package nl.playlumi.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Random;

/** Big tap targets, no time limits, local-only progress, no account or ads. */
public class MainActivity extends Activity {
    private LearningGame game;
    private LinearLayout root, gameButtons, levelButtons, variantButtons;
    private TextView ageLabel, variantLabel;
    private Button menuButton;
    private boolean home = true;
    private GridLayout choices;
    private TextView prompt, picture, feedback, progress, subtitle;
    private Button next;
    private int completed, streak;
    private String message = "";

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        game = new LearningGame(new Random());
        completed = getPreferences(MODE_PRIVATE).getInt("completed_all", 0);
        streak = getPreferences(MODE_PRIVATE).getInt("streak", 0);
        int mode = getPreferences(MODE_PRIVATE).getInt("game_mode", 0);
        int variant = getPreferences(MODE_PRIVATE).getInt("game_variant", 0);
        int level = getPreferences(MODE_PRIVATE).getInt("chosen_age", 0);
        if (saved != null) { mode = saved.getInt("mode", mode); variant = saved.getInt("variant", variant); level = saved.getInt("level", level); }
        game.configure(mode, variant, level);
        if (saved != null) home = saved.getBoolean("home", true);
        buildScreen();
        render();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putBoolean("home", home);
        out.putInt("mode", game.mode()); out.putInt("variant", game.variant()); out.putInt("level", game.level());
        super.onSaveInstanceState(out);
    }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String s, int sp, int color) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(color); v.setGravity(Gravity.CENTER);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private Button button(String label, int color) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(16);
        b.setTextColor(Color.rgb(29, 58, 58)); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setPadding(dp(4), dp(4), dp(4), dp(4)); b.setMinHeight(dp(58));
        return b;
    }
    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(Color.rgb(239, 249, 244));
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(18), dp(14), dp(32));
        scroll.addView(root); setContentView(scroll);
        TextView title = text("✨ Playlumi", 30, 0xff236558); root.addView(title);
        subtitle = text("Kies een spelletje", 15, 0xff5b7771); root.addView(subtitle);
        progress = text("", 14, 0xff4a6660);
        progress.setPadding(0, dp(8), 0, dp(12)); root.addView(progress);
        menuButton = button("⌂ Terug naar spelletjes", 0xffd5f1e5);
        root.addView(menuButton, new LinearLayout.LayoutParams(-1, dp(52)));
        menuButton.setOnClickListener(v -> { home = true; message = ""; render(); });
        gameButtons = new LinearLayout(this); gameButtons.setOrientation(LinearLayout.VERTICAL); root.addView(gameButtons);
        for (int row = 0; row < 3; row++) {
            LinearLayout line = new LinearLayout(this); line.setGravity(Gravity.CENTER); gameButtons.addView(line);
            for (int col = 0; col < (row == 2 ? 1 : 3); col++) {
                final int index = row * 3 + col;
                String[] icons = {"⭐", "🎨", "🔤", "🔷", "🔢", "🧩", "🐾"};
                Button b = button(icons[index] + "\n" + LearningGame.GAME_NAMES[index], 0xffdcf4e9);
                b.setTextSize(14);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(78), 1);
                p.setMargins(dp(2), dp(2), dp(2), dp(2)); line.addView(b, p);
                b.setOnClickListener(v -> { home = false; select(index, 0, game.level()); });
            }
        }
        ageLabel = text("Kies leeftijd (wordt onthouden)", 17, 0xff28685b);
        ageLabel.setPadding(0, dp(12), 0, dp(3)); root.addView(ageLabel);
        levelButtons = new LinearLayout(this); root.addView(levelButtons);
        String[] levelNames = {"🌱 3–4 jaar", "🌼 5–6 jaar", "🚀 7+ jaar"};
        for (int i = 0; i < 3; i++) {
            final int chosen = i;
            Button b = button(levelNames[i], 0xffe6f1fa); b.setTextSize(13);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(58), 1);
            p.setMargins(dp(2), 0, dp(2), 0); levelButtons.addView(b, p);
            b.setOnClickListener(v -> select(game.mode(), game.variant(), chosen));
        }
        variantLabel = text("Speel op twee manieren", 17, 0xff28685b);
        variantLabel.setPadding(0, dp(12), 0, dp(2)); root.addView(variantLabel);
        variantButtons = new LinearLayout(this); root.addView(variantButtons);
        for (int i = 0; i < 2; i++) {
            final int chosen = i;
            Button b = button(i == 0 ? "Spel 1" : "Spel 2", 0xffffebc6);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(54), 1);
            p.setMargins(dp(3), 0, dp(3), 0); variantButtons.addView(b, p);
            b.setOnClickListener(v -> select(game.mode(), chosen, game.level()));
        }
        prompt = text("", 23, 0xff223d38);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
        pp.setMargins(0, dp(18), 0, dp(10)); root.addView(prompt, pp);
        picture = text("", 45, 0xffbd7135); picture.setMinHeight(dp(135));
        picture.setPadding(dp(8), dp(18), dp(8), dp(18)); picture.setBackground(bg(Color.WHITE, 20));
        root.addView(picture, new LinearLayout.LayoutParams(-1, -2));
        feedback = text("", 18, 0xff237254); feedback.setMinHeight(dp(52)); root.addView(feedback);
        choices = new GridLayout(this); choices.setColumnCount(3);
        choices.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        root.addView(choices, new LinearLayout.LayoutParams(-1, -2));
        next = button("🌟 Volgende vraag", 0xffffd987); next.setTextSize(20);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, dp(65));
        np.setMargins(0, dp(14), 0, 0); root.addView(next, np);
        next.setOnClickListener(v -> { game.nextRound(); message = ""; render(); });
    }
    private void select(int mode, int variant, int level) {
        game.configure(mode, variant, level); message = ""; streak = 0;
        getPreferences(MODE_PRIVATE).edit().putInt("game_mode", mode)
                .putInt("game_variant", variant).putInt("game_level", level)
                .putInt("chosen_age", level).putInt("streak", streak).apply();
        render();
    }
    private void choose(int index) {
        if (game.round().isSolved()) return;
        if (game.round().choose(index)) {
            completed++; streak++;
            getPreferences(MODE_PRIVATE).edit().putInt("completed_all", completed).putInt("streak", streak).apply();
            message = game.mode() == LearningGame.WORDS ? "⭐ Goed gedaan! " + game.round().display.substring(game.round().display.indexOf("\n") + 1).replace("_", game.round().answers.get(game.round().correctIndex)) : (streak > 1 && streak % 5 == 0 ? "🎉 Super! " + streak + " goed achter elkaar!" : "⭐ Goed gedaan!");
        } else {
            streak = 0; getPreferences(MODE_PRIVATE).edit().putInt("streak", streak).apply();
            message = "Bijna! Probeer nog eens 💛";
        }
        render();
        feedback.announceForAccessibility(message);
    }
    private void render() {
        LearningGame.Round round = game.round();
        subtitle.setText(home ? "Kies een spelletje • je leeftijd wordt bewaard" : LearningGame.GAME_NAMES[game.mode()] + " • Spel " + (game.variant() + 1));
        gameButtons.setVisibility(home ? View.VISIBLE : View.GONE);
        levelButtons.setVisibility(home ? View.VISIBLE : View.GONE);
        ageLabel.setVisibility(home ? View.VISIBLE : View.GONE);
        variantLabel.setVisibility(home ? View.GONE : View.VISIBLE);
        variantButtons.setVisibility(home ? View.GONE : View.VISIBLE);
        menuButton.setVisibility(home ? View.GONE : View.VISIBLE);
        prompt.setVisibility(home ? View.GONE : View.VISIBLE);
        picture.setVisibility(home ? View.GONE : View.VISIBLE);
        feedback.setVisibility(home ? View.GONE : View.VISIBLE);
        choices.setVisibility(home ? View.GONE : View.VISIBLE);
        next.setVisibility(home ? View.GONE : (round.isSolved() ? View.VISIBLE : View.GONE));
        progress.setText("🌟 " + completed + " opdrachten goed  •  " + streak + " op rij");
        for (int i = 0; i < gameButtons.getChildCount(); i++) {
            LinearLayout line = (LinearLayout) gameButtons.getChildAt(i);
            for (int j = 0; j < line.getChildCount(); j++) {
                Button b = (Button) line.getChildAt(j);
                b.setAlpha(i * 3 + j == game.mode() ? 1f : 0.62f);
            }
        }
        for (int i = 0; i < levelButtons.getChildCount(); i++)
            levelButtons.getChildAt(i).setAlpha(i == game.level() ? 1f : 0.55f);
        for (int i = 0; i < variantButtons.getChildCount(); i++)
            variantButtons.getChildAt(i).setAlpha(i == game.variant() ? 1f : 0.55f);
        prompt.setText(round.prompt);
        picture.setText(round.display); picture.setTextColor(round.displayColor);
        picture.setContentDescription(round.prompt + ". " + round.display);
        feedback.setText(message);
        choices.removeAllViews();
        for (int i = 0; i < round.answers.size(); i++) {
            final int selected = i;
            String value = round.answers.get(i);
            Button b = button(value, 0xffffffff);
            b.setTextSize(game.mode() == LearningGame.COLORS || game.mode() == LearningGame.PATTERNS ? 16 : 25);
            if (game.mode() == LearningGame.COLORS || game.mode() == LearningGame.PATTERNS) {
                b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xfff5f6ff));
                b.setTextColor(round.answerColors.get(i));
            }
            b.setContentDescription("Kies " + value);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.height = dp(70); p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            p.setMargins(dp(3), dp(3), dp(3), dp(3));
            choices.addView(b, p);
            b.setEnabled(!round.isSolved());
            b.setOnClickListener(v -> choose(selected));
        }
        next.setVisibility(home ? View.GONE : (round.isSolved() ? View.VISIBLE : View.GONE));
    }
}
