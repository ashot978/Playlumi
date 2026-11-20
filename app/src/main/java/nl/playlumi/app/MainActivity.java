package nl.playlumi.app;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Random;

/** Tile-based, screen-by-screen learning experience with no ads, timers or accounts. */
public class MainActivity extends Activity {
    private static final int AGE = 0, CATEGORIES = 1, DIFFICULTY = 2, VARIANT = 3, PLAY = 4;
    private static final int SESSION_ROUNDS = 100;
    private static final int BG = 0xfff5f9fb, INK = 0xff244454, MUTED = 0xff59717b;
    private static final int[] TILE = {0xffdeefff,0xffffe7cf,0xffeee1ff,0xffd9f5e8,0xffffe3ef,0xfffff2be};
    private static final String[] ICONS = {"⭐","🎨","🔤","🔷","🔢","🧩","🐾",
            "✏️","🔠","🖼️","🎶","⚖️","➕","🔢","↔️","🧠"};
    private static final String[] AGES = {"3–4 jaar", "5–6 jaar", "7+ jaar"};
    private static final String[] AGE_ICONS = {"🐣","🦋","🚀"};
    private static final String[] LEVELS = {"Makkelijk","Gemiddeld","Uitdagend"};
    private static final String[] LEVEL_ICONS = {"🌱","🌻","🏆"};
    private LearningGame game;
    private QuestionHistory recent;
    private int page, age, difficulty, completed, streak, sessionSolved, sessionAttempts;
    private String feedback = "";
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        game = new LearningGame(new Random());
        recent = new QuestionHistory(getPreferences(MODE_PRIVATE).getString("recent_questions", ""));
        game.setHistory(recent);
        age = getPreferences(MODE_PRIVATE).getInt("chosen_age", -1);
        completed = getPreferences(MODE_PRIVATE).getInt("completed_all", 0);
        streak = getPreferences(MODE_PRIVATE).getInt("streak", 0);
        difficulty = age < 0 ? 0 : age;
        int mode = getPreferences(MODE_PRIVATE).getInt("game_mode", 0);
        int variant = getPreferences(MODE_PRIVATE).getInt("game_variant", 0);
        if (state != null) {
            age = state.getInt("age", age);
            mode = state.getInt("mode", mode);
            variant = state.getInt("variant", variant);
            difficulty = state.getInt("difficulty", difficulty);
            page = state.getInt("page", age < 0 ? AGE : CATEGORIES);
            sessionSolved = state.getInt("sessionSolved");
            sessionAttempts = state.getInt("sessionAttempts");
        } else page = age < 0 ? AGE : CATEGORIES;
        game.configure(mode, variant, difficulty);
        saveRecent();
        showPage();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("age", age); out.putInt("mode", game.mode());
        out.putInt("variant", game.variant()); out.putInt("difficulty", difficulty);
        out.putInt("page", page); out.putInt("sessionSolved", sessionSolved);
        out.putInt("sessionAttempts", sessionAttempts);
        super.onSaveInstanceState(out);
    }

    @Override public void onBackPressed() {
        if (page == AGE) super.onBackPressed();
        else if (page == CATEGORIES) navigate(AGE);
        else navigate(page == PLAY ? VARIANT : page - 1);
    }

    private int dp(int amount) {
        return (int) (amount * getResources().getDisplayMetrics().density + .5f);
    }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView label(String content, int size, int color) {
        TextView text = new TextView(this);
        text.setText(content); text.setTextColor(color); text.setTextSize(size);
        text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        text.setGravity(Gravity.CENTER);
        return text;
    }
    private void addLabel(String content, int size, int color, int top, int bottom) {
        TextView t = label(content, size, color);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(top), 0, dp(bottom)); root.addView(t, p);
    }
    private Button tile(String title, String description, int background, int height) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(description.isEmpty() ? title : title + "\n" + description);
        b.setTextColor(INK);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundTintList(ColorStateList.valueOf(background));
        b.setMinHeight(dp(height)); b.setPadding(dp(8), dp(8), dp(8), dp(8));
        b.setContentDescription((title + " " + description).trim());
        return b;
    }
    private void tileRow(Button[] items) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        for (Button item : items) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(104), 1);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            row.addView(item, params);
        }
        root.addView(row);
    }
    private void header(String title, String description) {
        addLabel("✨ Playlumi", 29, 0xff24847a, 0, 4);
        addLabel(title, 23, INK, 12, 4);
        addLabel(description, 15, MUTED, 0, 15);
    }
    private void back(int target) {
        Button b = tile("← Terug", "", 0xffe8edf3, 49);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(49));
        p.setMargins(0, 0, 0, dp(12)); root.addView(b, p);
        b.setOnClickListener(v -> navigate(target));
    }
    private void saveRecent() {
        getPreferences(MODE_PRIVATE).edit().putString("recent_questions", recent.serialize()).apply();
    }
    private void navigate(int destination) {
        page = destination; feedback = ""; showPage();
    }
    private void showPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(15), dp(18), dp(15), dp(30));
        scroll.addView(root); setContentView(scroll);
        switch (page) {
            case AGE: showAge(); break;
            case CATEGORIES: showCategories(); break;
            case DIFFICULTY: showDifficulty(); break;
            case VARIANT: showVariant(); break;
            case PLAY: showPlay(); break;
            default: throw new IllegalStateException("page");
        }
    }
    private void showAge() {
        header("Voor welke leeftijd spelen we?", "Kies één keer. Playlumi onthoudt je keuze.");
        for (int i = 0; i < AGES.length; i++) {
            final int selected = i;
            Button b = tile(AGE_ICONS[i] + "  " + AGES[i],
                    i == 0 ? "Ontdekken & herkennen" : i == 1 ? "Letters, woorden & tellen" : "Lezen, rekenen & puzzelen",
                    TILE[i], 96);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(96));
            p.setMargins(0, dp(6), 0, dp(6));
            root.addView(b, p);
            b.setOnClickListener(v -> {
                age = selected; difficulty = selected;
                getPreferences(MODE_PRIVATE).edit().putInt("chosen_age", age).apply();
                game.configure(game.mode(), game.variant(), difficulty);
                saveRecent();
                navigate(CATEGORIES);
            });
        }
        if (age >= 0) addLabel("Geselecteerd: " + AGES[age], 15, MUTED, 12, 0);
    }
    private void showCategories() {
        header("Wat gaan we spelen?", "Leeftijd: " + AGES[age] + "  •  Kies een speltegel");
        Button change = tile("🎂 Leeftijd aanpassen", "Je keuze wordt bewaard", 0xffe4f4ed, 56);
        root.addView(change, new LinearLayout.LayoutParams(-1, dp(66)));
        change.setOnClickListener(v -> navigate(AGE));
        addLabel("🌟 " + completed + " goede opdrachten", 15, 0xff2a8067, 14, 8);
        for (int i = 0; i < LearningGame.GAME_NAMES.length; i += 2) {
            Button[] buttons = new Button[2];
            for (int j = 0; j < 2; j++) {
                int idx = i + j;
                if (idx >= LearningGame.GAME_NAMES.length) {
                    buttons[j] = tile("", "", BG, 104); buttons[j].setEnabled(false);
                } else {
                    buttons[j] = tile(ICONS[idx] + "\n" + LearningGame.GAME_NAMES[idx],
                            "5 spelvarianten", TILE[idx % TILE.length], 104);
                    buttons[j].setOnClickListener(v -> {
                        difficulty = age;
                        game.configure(idx, 0, difficulty);
                        saveRecent();
                        getPreferences(MODE_PRIVATE).edit().putInt("game_mode", idx).apply();
                        navigate(DIFFICULTY);
                    });
                }
            }
            tileRow(buttons);
        }
        addLabel("Geen tijdslimiet • Geen reclame • Je kunt altijd terug", 13, MUTED, 15, 0);
    }
    private void showDifficulty() {
        back(CATEGORIES);
        header(ICONS[game.mode()] + " " + LearningGame.GAME_NAMES[game.mode()],
                "Hoe moeilijk mag het zijn?");
        addLabel("Voorgesteld voor jouw leeftijd: " + LEVELS[age], 14, MUTED, 0, 12);
        for (int i = 0; i < LEVELS.length; i++) {
            final int chosen = i;
            Button b = tile(LEVEL_ICONS[i] + "  " + LEVELS[i],
                    i == 0 ? "Korte woorden en kleine aantallen" :
                    i == 1 ? "Meer letters en grotere aantallen" : "Lange woorden en extra uitdaging",
                    TILE[(i + 3) % TILE.length], 95);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(95));
            p.setMargins(0, dp(6), 0, dp(6)); root.addView(b, p);
            b.setOnClickListener(v -> {
                difficulty = chosen;
                game.configure(game.mode(), game.variant(), difficulty);
                saveRecent();
                navigate(VARIANT);
            });
        }
    }
    private String[] variantNames() {
        String[] first;
        if (game.mode() >= ExtraGames.SPELLING) {
            first = ExtraGames.VARIANT_LABELS[game.mode() - ExtraGames.SPELLING];
        } else if (game.mode() == LearningGame.WORDS) {
            first = new String[]{"Middelste letter", "Begin- of eindletter"};
        } else {
            switch (game.mode()) {
                case LearningGame.COUNT: first = new String[]{"Voorwerpen tellen", "Andere voorwerpen"}; break;
                case LearningGame.COLORS: first = new String[]{"Welke kleur?", "Zoek de kleur"}; break;
                case LearningGame.LETTERS: first = new String[]{"Zoek klein", "Zoek groot"}; break;
                case LearningGame.SHAPES: first = new String[]{"Zoek dezelfde vorm", "Noem de vorm"}; break;
                case LearningGame.NUMBERS: first = new String[]{"Herken het cijfer", "Volgende cijfer"}; break;
                case LearningGame.PATTERNS: first = new String[]{"Om-en-om", "Dubbel patroon"}; break;
                default: first = new String[]{"Spel 1","Spel 2"};
            }
        }
        String[] names = new String[5];
        names[0] = first[0]; names[1] = first[1];
        for (int i = 0; i < 3; i++) names[i+2] = VariantGames.NAMES[game.mode()][i];
        return names;
    }
    private void showVariant() {
        back(DIFFICULTY);
        header(ICONS[game.mode()] + " " + LearningGame.GAME_NAMES[game.mode()],
                LEVELS[difficulty] + "  •  Kies hoe je wilt spelen");
        String[] names = variantNames();
        for (int i = 0; i < names.length; i++) {
            final int selected = i;
            String[] variants = {"🎯", "🎲", "🧠", "✨", "🚀"};
            Button b = tile(variants[i] + "  " + names[i],
                    "100 vragen • afwisselende opdrachten", TILE[(game.mode() + i) % TILE.length], 91);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(91));
            p.setMargins(0, dp(5), 0, dp(5)); root.addView(b, p);
            b.setOnClickListener(v -> {
                game.configure(game.mode(), selected, difficulty);
                saveRecent();
                getPreferences(MODE_PRIVATE).edit().putInt("game_variant", selected).apply();
                sessionSolved = 0; sessionAttempts = 0;
                navigate(PLAY);
            });
        }
    }
    private void showPlay() {
        back(VARIANT);
        LearningGame.Round round = game.round();
        header(ICONS[game.mode()] + " " + LearningGame.GAME_NAMES[game.mode()],
                variantNames()[game.variant()] + "  •  " + LEVELS[difficulty]);
        addLabel("⭐ " + sessionSolved + "/" + SESSION_ROUNDS +
                "   •   Totaal: " + completed + "   •   Reeks: " + streak,
                15, 0xff27856e, 0, 10);
        if (sessionSolved >= SESSION_ROUNDS) {
            addLabel("🏆 Geweldig gespeeld!", 29, 0xff29846f, 25, 15);
            addLabel("Je hebt 100 opdrachten goed opgelost!", 18, INK, 0, 24);
            Button again = tile("🔄 Nog 100 vragen", "Blijf oefenen!", TILE[1], 92);
            root.addView(again, new LinearLayout.LayoutParams(-1, dp(92)));
            again.setOnClickListener(v -> { sessionSolved = 0; game.nextRound(); saveRecent(); showPage(); });
            return;
        }
        addLabel(round.prompt, 23, INK, 8, 12);
        TextView display = label(round.display, game.mode() == LearningGame.WORDS ? 40 : 44, round.displayColor);
        display.setMinHeight(dp(138)); display.setPadding(dp(12), dp(20), dp(12), dp(20));
        display.setBackground(shape(Color.WHITE, 22));
        display.setContentDescription(round.prompt + " " + round.display);
        root.addView(display, new LinearLayout.LayoutParams(-1, -2));
        addLabel(feedback.isEmpty() ? "Kies het juiste antwoord 👇" : feedback,
                17, feedback.startsWith("⭐") || feedback.startsWith("🎉") ? 0xff248365 : MUTED, 14, 12);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(round.answers.size() <= 4 ? 2 : 3);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        root.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        for (int i = 0; i < round.answers.size(); i++) {
            final int selected = i;
            String value = round.answers.get(i);
            Button b = tile(value, "", TILE[(game.mode() + i) % TILE.length], 85);
            b.setTextSize(value.length() >= 9 ? 15 : value.length() >= 6 ? 18 : 25);
            b.setEnabled(!round.isSolved());
            b.setContentDescription("Antwoord: " + value);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.height = dp(85);
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            p.setMargins(dp(4), dp(4), dp(4), dp(4));
            grid.addView(b, p);
            b.setOnClickListener(v -> choose(selected));
        }
        if (round.isSolved()) {
            Button next = tile("🌟 Volgende opdracht", "", TILE[3], 74);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(74));
            p.setMargins(0, dp(18), 0, 0);
            root.addView(next, p);
            next.setOnClickListener(v -> { game.nextRound(); saveRecent(); feedback = ""; showPage(); });
        }
    }
    private void choose(int index) {
        if (game.round().isSolved()) return;
        sessionAttempts++;
        if (game.round().choose(index)) {
            completed++; streak++; sessionSolved++;
            getPreferences(MODE_PRIVATE).edit().putInt("completed_all", completed).putInt("streak", streak).apply();
            feedback = sessionSolved % 10 == 0 ? "🎉 Al " + sessionSolved + " opdrachten goed!" : "⭐ Goed gedaan!";
        } else {
            streak = 0;
            getPreferences(MODE_PRIVATE).edit().putInt("streak", 0).apply();
            feedback = "Bijna! Probeer het nog eens 💛";
        }
        showPage();
    }
}
