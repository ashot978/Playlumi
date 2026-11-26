package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** More offline educational activities. Each round has one verifiable correct answer. */
public final class ExtraGames {
    public static final int SPELLING = 7, FIRST_LAST = 8, PICTURES = 9, RHYMES = 10,
            COMPARE = 11, MATH = 12, SEQUENCES = 13, OPPOSITES = 14, CATEGORIES = 15;
    public static final String[] LABELS = {"Spelling", "Begin & eind", "Woord & plaatje", "Rijmen",
            "Meer of minder", "Sommen", "Getallenrij", "Tegenstellingen", "Wat hoort erbij?"};
    public static final String[] ICONS = {"✏️", "🔠", "🖼️", "🎶", "⚖️", "➕", "🔢", "↔️", "🧠"};
    public static final String[][] VARIANT_LABELS = {
            {"Juiste spelling", "Nog een spelling"},
            {"Beginletter", "Eindletter"},
            {"Plaatje naar woord", "Woord naar plaatje"},
            {"Wat rijmt?", "Wat rijmt niet?"},
            {"Waar zijn er meer?", "Waar zijn er minder?"},
            {"Optellen", "Aftrekken"},
            {"Verder tellen", "Terugtellen"},
            {"Zoek het tegendeel", "Maak het paar"},
            {"Wat hoort erbij?", "Wat hoort er niet bij?"}
    };
    // Rows: emoji, word, plausible incorrect spellings. The first ten entries are easy,
    // second ten intermediate, last ten more advanced.
    private static final String[][] WORDS = {
            {"🐮","koe","kou","ko","kue"}, {"🐱","kat","kad","ket","kt"},
            {"🐟","vis","fis","vies","vs"}, {"🐶","hond","hont","hondt","hon"},
            {"🐔","kip","kib","kiep","ki"}, {"🐵","aap","aapd","ap","aep"},
            {"🐻","beer","ber","beir","beeer"}, {"🐭","muis","mies","muus","mus"},
            {"🐸","kikker","kiker","kikkerr","kikkar"}, {"🐰","konijn","konyn","konin","konijnn"},
            {"🐴","paard","paart","pard","paardt"}, {"🦁","leeuw","leew","leuw","leeu"},
            {"🐯","tijger","teiger","tiger","tijjer"}, {"🐷","varken","varkn","varkenr","varkem"},
            {"🐑","schaap","sgaap","schap","schaab"}, {"🦆","eend","eent","end","eendt"},
            {"🦋","vlinder","vlindr","vlinderh","vlinter"}, {"🐌","slak","slag","slaak","slk"},
            {"🐝","bij","bey","bii","bi"}, {"🦊","vos","fos","voss","vo"},
            {"🐘","olifant","oliefant","olifand","olifantt"}, {"🐬","dolfijn","dolfyn","dolfain","dolfein"},
            {"🦒","giraf","giraff","girav","girafh"}, {"🦉","uil","uyl","uilh","uill"},
            {"🐧","pinguïn","pingwin","pinguin","penguïn"}, {"🐢","schildpad","schiltpad","schildpat","schilpad"},
            {"🐊","krokodil","krokedil","krokodiel","krokodiln"}, {"🦓","zebra","sebra","zebbra","zebar"},
            {"🐒","aapje","apje","aapjee","aapjeh"}, {"🦀","krab","krapp","krabb","krabh"}
    };
    // Each group contains words that rhyme; all answers are chosen only from explicit groups.
    private static final String[][] RHYME_GROUPS = {
            {"kat","rat","mat","vat"}, {"maan","haan","baan","kraan"},
            {"huis","muis","pluis","buis"}, {"beer","peer","weer","meer"},
            {"boom","room","droom","stroom"}, {"vis","mis","sis","gis"},
            {"roos","doos","boos","loos"}, {"bal","hal","stal","dal"},
            {"kop","pop","stop","top"}, {"jas","tas","plas","was"},
            {"boot","noot","groot","rood"}, {"tak","dak","pak","bak"}
    };
    private static final String[][] OPPOSITE_WORD_PAIRS = {
            {"groot","klein"}, {"warm","koud"}, {"hoog","laag"}, {"nat","droog"},
            {"licht","donker"}, {"vol","leeg"}, {"blij","verdrietig"},
            {"snel","langzaam"}, {"open","dicht"}, {"hard","zacht"},
            {"dik","dun"}, {"lang","kort"}, {"oud","jong"}, {"boven","onder"},
            {"binnen","buiten"}, {"vroeg","laat"}, {"voor","achter"}, {"dag","nacht"}
    };
    private static final String[][] GROUPS = {
            {"🐱","🐶","🐮","🚗","dieren"},
            {"🍎","🍌","🍐","🚲","fruit"},
            {"🚗","🚌","🚕","🐸","voertuigen"},
            {"🌹","🌻","🌷","⚽","bloemen"},
            {"⚽","🏀","🎾","🐱","ballen"},
            {"🥕","🥦","🌽","🛵","groente"},
            {"🐟","🐬","🦈","🍕","zeedieren"},
            {"👟","👢","🥿","🍓","schoenen"},
            {"☀️","⭐","🌙","🍔","hemel"},
            {"✏️","🖍️","🖊️","🐶","schrijven"}
    };
    private ExtraGames() {}

    private static int bound(int level) { return level == 0 ? 3 : level == 1 ? 4 : 6; }
    private static int numberMax(int level) { return level == 0 ? 5 : level == 1 ? 10 : 20; }
    private static List<Integer> ink(int count) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) colors.add(0xff30484f);
        return colors;
    }
    private static LearningGame.Round round(String question, String picture, List<String> options, String correct, Random random) {
        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(correct);
        if (correctIndex < 0 || Collections.frequency(options, correct) != 1)
            throw new IllegalStateException("Ambiguous answer: " + correct);
        return new LearningGame.Round(question, picture, 0xff385b86, options, ink(options.size()), correctIndex);
    }
    private static void uniqueAdd(List<String> options, String value) {
        if (!options.contains(value)) options.add(value);
    }
    private static List<String> distractors(String answer, String alphabet, int count, Random random) {
        List<String> options = new ArrayList<>();
        options.add(answer);
        while (options.size() < count) {
            String candidate = String.valueOf(alphabet.charAt(random.nextInt(alphabet.length())));
            uniqueAdd(options, candidate);
        }
        return options;
    }
    public static LearningGame.Round next(int mode, int variant, int level, Random random) {
        if (variant < 0 || variant > 1 || level < 0 || level > 2) throw new IllegalArgumentException("settings");
        switch (mode) {
            case SPELLING: return spelling(variant, level, random);
            case FIRST_LAST: return firstLast(variant, level, random);
            case PICTURES: return pictures(variant, level, random);
            case RHYMES: return rhymes(variant, level, random);
            case COMPARE: return compare(variant, level, random);
            case MATH: return PlaylumiMathPractice.next(variant,level,random);
            case SEQUENCES: return sequences(variant, level, random);
            case OPPOSITES: return opposites(variant, level, random);
            case CATEGORIES: return categories(variant, level, random);
            default: throw new IllegalArgumentException("mode");
        }
    }
    private static String[] wordAt(int level, Random random) {
        int base = level * 10;
        return WORDS[base + random.nextInt(10)];
    }
    private static LearningGame.Round spelling(int variant, int level, Random random) {
        String[] word = wordAt(level, random);
        String correct = word[1];
        List<String> options = new ArrayList<>();
        options.add(correct);
        options.add(word[variant == 0 ? 2 : 3]);
        options.add(word[variant == 0 ? 3 : 2]);
        if (level > 0) uniqueAdd(options, word[4]);
        return round(variant == 0 ? "Welk woord is goed geschreven?" : "Welk woord past bij het dier?",
                word[0] + "\n✏️", options, correct, random);
    }
    private static LearningGame.Round firstLast(int variant, int level, Random random) {
        return PlaylumiLetterChallenges.next(variant==1,level,random);
    }
    private static LearningGame.Round pictures(int variant, int level, Random random) {
        int base = level * 10, index = base + random.nextInt(10);
        List<Integer> indices = new ArrayList<>();
        indices.add(index);
        while (indices.size() < bound(level)) uniqueAddIndex(indices, base + random.nextInt(10));
        List<String> answers = new ArrayList<>();
        for (int i : indices) answers.add(WORDS[i][variant == 0 ? 1 : 0]);
        String correct = WORDS[index][variant == 0 ? 1 : 0];
        return round(variant == 0 ? "Welk woord hoort bij dit plaatje?" : "Welk plaatje hoort bij dit woord?",
                WORDS[index][variant == 0 ? 0 : 1], answers, correct, random);
    }
    private static void uniqueAddIndex(List<Integer> list, int index) { if (!list.contains(index)) list.add(index); }
    private static LearningGame.Round rhymes(int variant, int level, Random random) {
        int groupIndex = random.nextInt(level == 0 ? 4 : level == 1 ? 8 : RHYME_GROUPS.length);
        String[] group = RHYME_GROUPS[groupIndex];
        String seed = group[0], rhyme = group[1 + random.nextInt(3)];
        int other = (groupIndex + 1 + random.nextInt(RHYME_GROUPS.length - 1)) % RHYME_GROUPS.length;
        String odd = RHYME_GROUPS[other][1];
        List<String> answers = new ArrayList<>();
        String correct;
        if (variant == 0) {
            correct = rhyme;
            answers.add(rhyme); answers.add(odd);
            int another = (other + 1) % RHYME_GROUPS.length;
            if (another == groupIndex) another = (another + 1) % RHYME_GROUPS.length;
            uniqueAdd(answers, RHYME_GROUPS[another][2]);
        } else {
            correct = odd;
            answers.add(group[1]); answers.add(group[2]); answers.add(odd);
            if (level > 0) uniqueAdd(answers, group[3]);
        }
        return round(variant == 0 ? "Welk woord rijmt op " + seed + "?" : "Welk woord rijmt NIET op " + seed + "?",
                "🎵 " + seed, answers, correct, random);
    }
    private static LearningGame.Round compare(int variant, int level, Random random) {
        int max = numberMax(level), left = 1 + random.nextInt(max), right = 1 + random.nextInt(max);
        if (right == left) right = right == max ? right - 1 : right + 1;
        List<String> answers = new ArrayList<>();
        answers.add("Links"); answers.add("Rechts");
        boolean leftCorrect = variant == 0 ? left > right : left < right;
        String prompt = variant == 0 ? "Waar zie je MEER?" : "Waar zie je MINDER?";
        return round(prompt, "⬅️ " + left + "       " + right + " ➡️", answers, leftCorrect ? "Links" : "Rechts", random);
    }
    private static LearningGame.Round math(int variant, int level, Random random) {
        int max = numberMax(level);
        int a, b, result;
        if (variant == 0) {
            a = 1 + random.nextInt(max - 1);
            b = 1 + random.nextInt(max - a);
            result = a + b;
        } else {
            a = 2 + random.nextInt(max - 1);
            b = 1 + random.nextInt(a - 1);
            result = a - b;
        }
        String correct = String.valueOf(result);
        List<String> answers = new ArrayList<>();
        answers.add(correct);
        int needed = bound(level);
        while (answers.size() < needed) {
            int guess = random.nextInt(max + 1);
            uniqueAdd(answers, String.valueOf(guess));
        }
        return round(variant == 0 ? "Hoeveel is dit samen?" : "Hoeveel blijven er over?",
                a + (variant == 0 ? " + " : " − ") + b + " = ?", answers, correct, random);
    }
    private static LearningGame.Round sequences(int variant,int level,Random random){
        return PlaylumiNumberChallenges.sequence(level,variant==1?-1:1,false,false,random);
    }
    private static LearningGame.Round opposites(int variant, int level, Random random) {
        int available = level == 0 ? 6 : level == 1 ? 12 : OPPOSITE_WORD_PAIRS.length;
        int index = random.nextInt(available);
        String promptWord = OPPOSITE_WORD_PAIRS[index][variant == 0 ? 0 : 1];
        String correct = OPPOSITE_WORD_PAIRS[index][variant == 0 ? 1 : 0];
        List<String> answers = new ArrayList<>();
        answers.add(correct);
        while (answers.size() < bound(level)) {
            int other = random.nextInt(available);
            uniqueAdd(answers, OPPOSITE_WORD_PAIRS[other][variant == 0 ? 1 : 0]);
        }
        return round(variant == 0 ? "Wat is het tegenovergestelde?" : "Maak het tegenstellingenpaar af!",
                "↔️  " + promptWord, answers, correct, random);
    }
    private static LearningGame.Round categories(int variant, int level, Random random) {
        int available = level == 0 ? 4 : level == 1 ? 7 : GROUPS.length;
        String[] group = GROUPS[random.nextInt(available)];
        List<String> answers = new ArrayList<>();
        String correct;
        if (variant == 0) {
            correct = group[1];
            answers.add(group[1]); answers.add(group[3]);
            answers.add(GROUPS[(available > 1 ? 1 : 0)][3]);
            if (answers.get(2).equals(group[3])) answers.set(2, "🪁");
        } else {
            correct = group[3];
            answers.add(group[0]); answers.add(group[1]); answers.add(group[2]); answers.add(group[3]);
        }
        return round(variant == 0 ? "Welk plaatje hoort bij " + group[4] + "?" : "Welk plaatje hoort er NIET bij?",
                group[0] + " " + group[2], answers, correct, random);
    }
}
