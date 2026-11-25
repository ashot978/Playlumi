package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Android-independent engine for six learning activities with two variants each. */
public final class LearningGame {
    public static final int COUNT = 0, COLORS = 1, LETTERS = 2, SHAPES = 3, NUMBERS = 4, PATTERNS = 5, WORDS = 6;
    public static final String[] GAME_NAMES = {"Tellen", "Kleuren", "Letters", "Vormen", "Cijfers", "Patronen", "Woordjes", "Spelling", "Begin & eind", "Woord & plaatje", "Rijmen", "Meer of minder", "Sommen", "Getallenrij", "Tegenstellingen", "Wat hoort erbij?", "Klokkijken", "Weer & seizoenen", "Plaats & richting", "Dagelijkse dingen", "Eten & drinken", "Dierenwereld", "Meten & vergelijken", "Slim nadenken"};
    public static final String[] VARIANT_NAMES = {"Herkennen", "Anders oefenen"};
    public static final String[] COLOR_NAMES = {"rood", "blauw", "groen", "geel", "paars", "oranje"};
    public static final int[] COLOR_VALUES = {0xffd93646, 0xff2472c8, 0xff2b994f, 0xffd0a000, 0xff894bc1, 0xffdf7623};
    public static final String[] SHAPE_NAMES = {"cirkel", "vierkant", "driehoek", "ruit", "ster", "hart"};
    public static final String[] SHAPE_SYMBOLS = {"●", "■", "▲", "◆", "★", "♥"};
    public static final String[] UPPER = {"A", "B", "M", "S", "O", "P", "K", "T", "L", "R"};
    public static final String[] LOWER = {"a", "b", "m", "s", "o", "p", "k", "t", "l", "r"};
    public static final String[] OBJECTS = {"★", "✿", "●", "♥", "◆", "☀"};
    public static final String[] OBJECT_NAMES = {"sterren", "bloemen", "ballen", "hartjes", "ruiten", "zonnetjes"};
    private final Random random;
    private int mode, variant, level;
    private Round current;
    private QuestionHistory history;

    public static final class Round {
        public final String prompt, display;
        public final int displayColor, correctIndex;
        public final List<String> answers;
        public final List<Integer> answerColors;
        private boolean solved;
        Round(String prompt, String display, int displayColor, List<String> answers, List<Integer> answerColors, int correctIndex) {
            this.prompt = prompt; this.display = display; this.displayColor = displayColor;
            this.answers = Collections.unmodifiableList(new ArrayList<>(answers));
            this.answerColors = Collections.unmodifiableList(new ArrayList<>(answerColors));
            this.correctIndex = correctIndex;
        }
        public boolean isSolved() { return solved; }
        public boolean choose(int index) {
            if (solved || index != correctIndex) return false;
            solved = true;
            return true;
        }
    }

    public LearningGame(Random random) { this.random = random; nextRound(); }
    public int mode() { return mode; }
    public int variant() { return variant; }
    /** Lowercase matching is intentionally a visual-only exercise. */
    public boolean allowsQuestionNarration() { return mode != LETTERS || variant != 0; }
    public int level() { return level; }
    public Round round() { return current; }
    public void setHistory(QuestionHistory history) { this.history = history; }
    public void configure(int mode, int variant, int level) {
        if (mode < 0 || mode >= GAME_NAMES.length || variant < 0 || variant > 4 || level < 0 || level > 2)
            throw new IllegalArgumentException("Invalid game configuration");
        this.mode = mode; this.variant = variant; this.level = level;
        nextRound();
    }
    private int pool(int max) { return Math.min(max, level == 0 ? 3 : level == 1 ? 5 : 10); }
    private int pick(int bound) { return random.nextInt(bound); }

    public void nextRound() {
        if (history == null) { generateRound(); return; }
        Round best = null;
        int oldest = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < 60; attempt++) {
            generateRound();
            int recency = history.recency(current);
            if (recency == 0) { best = current; break; }
            if (recency < oldest) { oldest = recency; best = current; }
        }
        current = best;
        history.remember(current);
    }
    private void generateRound() {
        if (mode >= NewWorldGames.FIRST) { current = NewWorldGames.next(mode, variant, level, random); return; }
        if (variant >= 2) { current = VariantGames.next(mode, variant, level, random); return; }
        switch (mode) {
            case COUNT: makeCount(); break;
            case COLORS: makeColors(); break;
            case LETTERS: makeLetters(); break;
            case SHAPES: makeShapes(); break;
            case NUMBERS: makeNumbers(); break;
            case PATTERNS: makePatterns(); break;
            case WORDS: makeWords(); break;
            case ExtraGames.SPELLING: case ExtraGames.FIRST_LAST: case ExtraGames.PICTURES:
            case ExtraGames.RHYMES: case ExtraGames.COMPARE: case ExtraGames.MATH:
            case ExtraGames.SEQUENCES: case ExtraGames.OPPOSITES: case ExtraGames.CATEGORIES:
                current = ExtraGames.next(mode, variant, level, random); break;
            default: throw new IllegalStateException("mode");
        }
    }
    private void make(String prompt, String display, int displayColor, List<String> answers, List<Integer> colors, String correct) {
        int idx = answers.indexOf(correct);
        if (idx < 0) throw new IllegalStateException("Answer missing");
        current = new Round(prompt, display, displayColor, answers, colors, idx);
    }
    private List<String> shuffled(String[] source, int count) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) values.add(source[i]);
        Collections.shuffle(values, random);
        return values;
    }
    private List<Integer> clearColors(int size) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < size; i++) colors.add(0xff263b40);
        return colors;
    }
    private void makeCount() {
        int max = pool(10), number = 1 + pick(max), object = variant == 0 ? pick(3) : 3 + pick(3);
        StringBuilder shown = new StringBuilder();
        for (int i = 0; i < number; i++) {
            if (i > 0) shown.append(i % 5 == 0 ? "\n" : "  ");
            shown.append(OBJECTS[object]);
        }
        String[] all = new String[max];
        for (int i = 0; i < max; i++) all[i] = "" + (i + 1);
        List<String> answers = shuffled(all, max);
        make("Hoeveel " + OBJECT_NAMES[object] + " zie je?", shown.toString(), 0xffde8a34, answers, clearColors(answers.size()), "" + number);
    }
    private void makeColors() {
        int count = pool(COLOR_NAMES.length), correct = pick(count);
        List<String> answers = shuffled(COLOR_NAMES, count);
        List<Integer> paints = new ArrayList<>();
        for (String name : answers) {
            int i = 0; while (!COLOR_NAMES[i].equals(name)) i++;
            paints.add(COLOR_VALUES[i]);
        }
        if (variant == 0) make("Welke kleur zie je?", "●", COLOR_VALUES[correct], answers, paints, COLOR_NAMES[correct]);
        else make("Tik op " + COLOR_NAMES[correct], "🎨", 0xff34495e, answers, paints, COLOR_NAMES[correct]);
    }
    private void makeLetters() {
        int count = pool(UPPER.length), correct = pick(count);
        boolean lowercase = variant == 0;
        String[] source = lowercase ? LOWER : UPPER;
        List<String> answers = shuffled(source, count);
        make(lowercase ? "Zoek de kleine letter" : "Zoek de hoofdletter",
                lowercase ? UPPER[correct] : LOWER[correct], 0xff574a9e,
                answers, clearColors(answers.size()), source[correct]);
    }
    private void makeShapes() {
        int count = pool(SHAPE_NAMES.length), correct = pick(count);
        List<String> answers = shuffled(variant == 0 ? SHAPE_SYMBOLS : SHAPE_NAMES, count);
        String answer = variant == 0 ? SHAPE_SYMBOLS[correct] : SHAPE_NAMES[correct];
        make(variant == 0 ? "Zoek dezelfde vorm" : "Welke vorm is dit?",
                SHAPE_SYMBOLS[correct], 0xffbc5c95, answers, clearColors(answers.size()), answer);
    }
    private void makeNumbers() {
        int count = pool(10);
        int correct = variant == 0 ? 1 + pick(count) : 1 + pick(Math.max(1, count - 1));
        int actual = variant == 0 ? correct : correct + 1;
        String[] all = new String[count];
        for (int i = 0; i < count; i++) all[i] = "" + (i + 1);
        List<String> answers = shuffled(all, count);
        make(variant == 0 ? "Welk cijfer zie je?" : "Welk getal komt na " + correct + "?",
                variant == 0 ? "" + correct : correct + "  →  ?", 0xff237c88,
                answers, clearColors(answers.size()), "" + actual);
    }
    private void makePatterns() {
        int count = pool(COLOR_NAMES.length), first = pick(count), second = (first + 1 + pick(count - 1)) % count;
        String[] emoji = {"🔴", "🔵", "🟢", "🟡", "🟣", "🟠"};
        String pattern = variant == 0 ? emoji[first] + " " + emoji[second] + " " + emoji[first] + " ?" :
                emoji[first] + " " + emoji[first] + " " + emoji[second] + " " + emoji[first] + " " + emoji[first] + " ?";
        String correct = COLOR_NAMES[variant == 0 ? second : second];
        List<String> answers = shuffled(COLOR_NAMES, count);
        List<Integer> paints = new ArrayList<>();
        for (String name : answers) {
            int i = 0; while (!COLOR_NAMES[i].equals(name)) i++;
            paints.add(COLOR_VALUES[i]);
        }
        make("Welke kleur komt hierna?", pattern, 0xff333e53, answers, paints, correct);
    }
    private static final String[][] ANIMALS = {
        {"koe","kat","vis","mus","kip","mol","uil","aap","bok","das"},
        {"hond","beer","muis","geit","haan","poes","eend","duif","slak","wolf"},
        {"konijn","paard","leeuw","tijger","schaap","varken","dolfijn","olifant","giraf","panda"}
    };
    private static final String[][] ICONS = {
        {"🐮","🐱","🐟","🐦","🐔","🐾","🦉","🐵","🐐","🦡"},
        {"🐶","🐻","🐭","🐐","🐓","🐱","🦆","🕊","🐌","🐺"},
        {"🐰","🐴","🦁","🐯","🐑","🐷","🐬","🐘","🦒","🐼"}
    };
    private void makeWords() {
        int index = pick(ANIMALS[level].length);
        String word = ANIMALS[level][index];
        int missing = variant == 0 ? word.length() / 2 : (level == 0 ? 0 : word.length() - 1);
        String correct = String.valueOf(word.charAt(missing));
        String display = ICONS[level][index] + "\n" + word.substring(0, missing) + "_" + word.substring(missing + 1);
        List<String> answers = new ArrayList<>();
        answers.add(correct);
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int count = level == 0 ? 3 : level == 1 ? 4 : 6;
        while (answers.size() < count) {
            String letter = String.valueOf(alphabet.charAt(pick(alphabet.length())));
            if (!answers.contains(letter)) answers.add(letter);
        }
        Collections.shuffle(answers, random);
        make("Welke letter ontbreekt?", display, 0xff59418a, answers, clearColors(answers.size()), correct);
    }

}
