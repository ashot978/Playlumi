package nl.playlumi.app;

import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.view.animation.DecelerateInterpolator;
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
    private static final int SESSION_ROUNDS = 200;
    private static final int BG = 0xfff9f5ff, INK = 0xff343056, MUTED = 0xff6a6590;
    private static final int[] TILE = {0xffd9f3ff,0xffffe1ae,0xffe8d7ff,0xffcef8dd,0xffffd4e6,0xfffff3a9};
    private static final String[] ICONS = {"⭐","🎨","🔤","🔷","🔢","🧩","🐾",
            "✏️","🔠","🖼️","🎶","⚖️","➕","🔢","↔️","🧠",
            "🕒","🌦️","🧭","🏠","🥕","🦁","📏","💡"};
    private static final String[] AGES = {"3–4 jaar", "5–6 jaar", "7+ jaar"};
    private static final String[] AGE_ICONS = {"🐣","🦋","🚀"};
    private static final String[] LEVELS = {"Makkelijk","Gemiddeld","Uitdagend"};
    private static final String[] LEVEL_ICONS = {"🌱","🌻","🏆"};
    private LearningGame game;
    private QuestionHistory recent;
    private int page, age, difficulty, completed, streak, sessionSolved, sessionAttempts;
    private String feedback = "";
    private LinearLayout root;
    private FrameLayout stage;
    private TextView feedbackView;
    private java.util.ArrayList<Button> answerButtons = new java.util.ArrayList<>();
    private boolean resolvingAnswer;
    private boolean switchingScreen;
    private int questionColor = 0xff7650c6;

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
        text.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        text.setIncludeFontPadding(true);
        text.setLineSpacing(dp(2), 1.06f);
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
        b.setText(PlaylumiType.tileText(title, description));
        b.setTextColor(INK);
        b.setTextSize(description.isEmpty() ? 19 : 17);
        b.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        b.setLineSpacing(dp(3), 1.04f);
        b.setGravity(Gravity.CENTER);
        android.graphics.drawable.RippleDrawable ripple =
                new android.graphics.drawable.RippleDrawable(
                        ColorStateList.valueOf(0x44ffffff), shape(background, 23), null);
        b.setBackground(ripple);
        b.setElevation(dp(6));
        b.setMinHeight(dp(height)); b.setPadding(dp(12), dp(10), dp(12), dp(10));
        b.setContentDescription((title + " " + description).trim());
        PlaylumiMotion.tactile(b);
        return b;
    }
    private void tileRow(Button[] items) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        for (Button item : items) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(142), 1);
            params.setMargins(dp(6), dp(6), dp(6), dp(6));
            row.addView(item, params);
        }
        root.addView(row);
    }
    private void header(String title, String description) {
        TextView logo = label("✦  P L A Y L U M I  ✦", 16, 0xff925bce);
        logo.setLetterSpacing(.08f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(42));
        lp.setMargins(0, dp(2), 0, dp(4));
        logo.setBackground(shape(0xfff1e6ff, 26));
        root.addView(logo, lp);
        TextView heading = label(title, page == PLAY ? 27 : 31, 0xff4e378e);
        heading.setGravity(Gravity.CENTER);
        heading.setMinHeight(dp(60));
        heading.setPadding(dp(6), dp(6), dp(6), dp(6));
        root.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        addLabel(description, 15, MUTED, 1, 16);
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
        if (switchingScreen) return;
        if (stage == null) { page = destination; feedback = ""; showPage(); return; }
        switchingScreen = true;
        FrameLayout exiting = stage;
        exiting.animate().cancel();
        exiting.animate().alpha(0f).translationX(-dp(18)).setDuration(135)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (stage != exiting) { switchingScreen = false; return; }
                    page = destination;
                    feedback = "";
                    switchingScreen = false;
                    showPage();
                }).start();
    }
    private void showPage() {
        stage = new FrameLayout(this);
        android.graphics.drawable.GradientDrawable gradient =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0xffe8f8ff, 0xfffff4fa, 0xfffff9db});
        stage.setBackground(gradient);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(36));
        scroll.addView(root);
        stage.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(stage);
        stage.setAlpha(0f);
        stage.setTranslationX(dp(18));
        stage.animate().alpha(1f).translationX(0f).setDuration(240)
                .setInterpolator(new DecelerateInterpolator()).start();
        final LinearLayout screen = root;
        screen.post(() -> { if (root == screen) PlaylumiMotion.reveal(screen); });
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
        header("🎈 Hoe oud ben jij?", "Kies jouw avontuur! We bewaren je keuze.");
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
        header("🌟 Wat gaan we spelen?", "Leeftijd: " + AGES[age] + "  •  Kies een speltegel");
        Button change = tile("🎂 Leeftijd aanpassen", "Je keuze wordt bewaard", 0xffe4f4ed, 56);
        root.addView(change, new LinearLayout.LayoutParams(-1, dp(66)));
        change.setOnClickListener(v -> navigate(AGE));
        addLabel("🌟 " + completed + " goede opdrachten", 15, 0xff2a8067, 14, 8);
        for (int i = 0; i < LearningGame.GAME_NAMES.length; i += 2) {
            if (i == 0) addLabel("🌈 Ontdekken & leren", 20, INK, 14, 8);
            if (i == 8) addLabel("🔤 Taal & slim denken", 20, INK, 20, 8);
            if (i == 16) addLabel("🌍 De wereld om je heen", 20, INK, 20, 8);
            Button[] buttons = new Button[2];
            for (int j = 0; j < 2; j++) {
                int idx = i + j;
                if (idx >= LearningGame.GAME_NAMES.length) {
                    buttons[j] = tile("", "", BG, 142); buttons[j].setEnabled(false);
                } else {
                    buttons[j] = tile(ICONS[idx] + "\n" + LearningGame.GAME_NAMES[idx],
                            "5 spelvarianten", TILE[idx % TILE.length], 142);
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
                "🌈 Kies jouw uitdaging!");
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
        if (game.mode() >= NewWorldGames.FIRST) {
            return NewWorldGames.VARIANTS[game.mode() - NewWorldGames.FIRST];
        }
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
                    "200 vragen • afwisselende opdrachten", TILE[(game.mode() + i) % TILE.length], 91);
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
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(SESSION_ROUNDS);
        progressBar.setProgress(sessionSolved);
        progressBar.setProgressTintList(ColorStateList.valueOf(0xff48b79e));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(0xffe4e8f1));
        LinearLayout.LayoutParams progressLayout = new LinearLayout.LayoutParams(-1, dp(12));
        progressLayout.setMargins(dp(8), 0, dp(8), dp(16));
        root.addView(progressBar, progressLayout);
        if (sessionSolved >= SESSION_ROUNDS) {
            addLabel("🏆 Geweldig gespeeld!", 29, 0xff29846f, 25, 15);
            addLabel("Je hebt 200 opdrachten goed opgelost!", 18, INK, 0, 24);
            Button again = tile("🔄 Nog 200 vragen", "Blijf oefenen!", TILE[1], 92);
            root.addView(again, new LinearLayout.LayoutParams(-1, dp(92)));
            again.setOnClickListener(v -> { sessionSolved = 0; game.nextRound(); saveRecent(); showPage(); });
            return;
        }
        TextView question = label(round.prompt, 29, 0xff523092);
        question.setMinHeight(dp(78));
        question.setPadding(dp(12), dp(13), dp(12), dp(13));
        question.setBackground(shape(0xfff1e5ff, 24));
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(-1, -2);
        qlp.setMargins(0, dp(14), 0, dp(17));
        root.addView(question, qlp);
        TextView display = label(round.display, game.mode() == LearningGame.WORDS ? 52 : 48, round.displayColor);
        display.setMinHeight(dp(180)); display.setPadding(dp(12), dp(22), dp(12), dp(22));
        display.setBackground(shape(Color.WHITE, 32));
        display.setElevation(dp(5));
        display.setContentDescription(round.prompt + " " + round.display);
        root.addView(display, new LinearLayout.LayoutParams(-1, -2));
        feedbackView = label(feedback.isEmpty() ? "👇 Tik op het goede antwoord!" : feedback, 20,
                feedback.startsWith("⭐") || feedback.startsWith("🎉") ? 0xff248365 : MUTED);
        LinearLayout.LayoutParams feedbackLayout = new LinearLayout.LayoutParams(-1, -2);
        feedbackLayout.setMargins(0, dp(14), 0, dp(12));
        root.addView(feedbackView, feedbackLayout);
        answerButtons.clear();
        resolvingAnswer = false;
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(round.answers.size() <= 4 ? 2 : 3);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        root.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        for (int i = 0; i < round.answers.size(); i++) {
            final int selected = i;
            String value = round.answers.get(i);
            Button b = tile(value, "", TILE[(game.mode() + i) % TILE.length], 96);
            b.setTextSize(value.length() >= 12 ? 16 : value.length() >= 8 ? 19 : value.length() >= 5 ? 23 : 30);
            b.setEnabled(!round.isSolved());
            b.setContentDescription("Antwoord: " + value);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.height = dp(96);
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            p.setMargins(dp(6), dp(6), dp(6), dp(6));
            grid.addView(b, p);
            answerButtons.add(b);
            b.setOnClickListener(v -> choose(selected, b));
        }
        if (round.isSolved()) {
            Button next = tile("🌟 Volgende opdracht", "", TILE[3], 74);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(74));
            p.setMargins(0, dp(18), 0, 0);
            root.addView(next, p);
            next.setOnClickListener(v -> { game.nextRound(); saveRecent(); feedback = ""; showPage(); });
        }
    }
    private void choose(int index, Button tapped) {
        if (resolvingAnswer || game.round().isSolved()) return;
        sessionAttempts++;
        if (game.round().choose(index)) {
            resolvingAnswer = true;
            completed++; streak++; sessionSolved++;
            getPreferences(MODE_PRIVATE).edit().putInt("completed_all", completed).putInt("streak", streak).apply();
            feedback = sessionSolved % 10 == 0 ? "🎉 Al " + sessionSolved + " opdrachten goed!" : "⭐ Goed gedaan!";
            feedbackView.setText(feedback);
            feedbackView.setTextColor(0xff248365);
            for (Button button : answerButtons) button.setEnabled(false);
            tapped.setBackgroundTintList(ColorStateList.valueOf(0xffb7efd5));
            PlaylumiMotion.pop(tapped);
            PlaylumiMotion.pop(feedbackView);
            PlaylumiMotion.celebrate(stage, streak);
            feedbackView.announceForAccessibility(feedback);
            final LearningGame.Round solvedRound = game.round();
            final FrameLayout activeStage = stage;
            activeStage.postDelayed(() -> {
                if (stage == activeStage && page == PLAY && game.round() == solvedRound) {
                    resolvingAnswer = false;
                    showPage();
                }
            }, 740L);
        } else {
            streak = 0;
            getPreferences(MODE_PRIVATE).edit().putInt("streak", 0).apply();
            feedback = "Bijna! Probeer het nog eens 💛";
            feedbackView.setText(feedback);
            feedbackView.setTextColor(0xffb87632);
            PlaylumiMotion.shake(tapped);
            feedbackView.announceForAccessibility(feedback);
        }
    }
}
