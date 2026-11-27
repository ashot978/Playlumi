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
    public static final String[] UPPER = {"A","B","M","S","O","P","K","T","L","R","D","E","F","G","H","I","N","U","C","J","Q","V","W","X","Y","Z"};
    public static final String[] LOWER = {"a","b","m","s","o","p","k","t","l","r","d","e","f","g","h","i","n","u","c","j","q","v","w","x","y","z"};
    public static final String[] OBJECTS = {"★", "✿", "●", "♥", "◆", "☀"};
    public static final String[] OBJECT_NAMES = {"sterren", "bloemen", "ballen", "hartjes", "ruiten", "zonnetjes"};
    private final Random random;
    private int mode, variant, level;
    private Round current;
    private QuestionHistory history;

    public static final class Round {
        public final String prompt, display;
        public final int displayColor, correctIndex, fingerCount;
        public final List<String> answers;
        public final List<Integer> answerColors;
        private boolean solved;
        private int rejected;
        Round(String prompt, String display, int displayColor, List<String> answers, List<Integer> answerColors, int correctIndex) {
            this(prompt,display,displayColor,answers,answerColors,correctIndex,0);
        }
        Round(String prompt,String display,int displayColor,List<String> answers,List<Integer> answerColors,int correctIndex,int fingerCount) {
            this.prompt = prompt; this.display = display; this.displayColor = displayColor;this.fingerCount=fingerCount;
            int[] order=PlaylumiAnswerOrder.indices(answers);
            ArrayList<String> ordered=new ArrayList<>();ArrayList<Integer> inks=new ArrayList<>();
            int newCorrect=-1;
            for(int i=0;i<order.length;i++){
                ordered.add(answers.get(order[i]));inks.add(answerColors.get(order[i]));
                if(order[i]==correctIndex)newCorrect=i;
            }
            if(newCorrect<0)throw new IllegalArgumentException("correct answer");
            this.answers=Collections.unmodifiableList(ordered);
            this.answerColors=Collections.unmodifiableList(inks);
            this.correctIndex=newCorrect;
        }
        public boolean isSolved() { return solved; }
        public int letterBlank() {
            int index=display.indexOf('_');
            if(index<0||index!=display.lastIndexOf('_'))return -1;
            String answer=blankReplacement();
            return answer.codePointCount(0,answer.length())==1 &&
                    Character.isLetter(answer.codePointAt(0))?index:-1;
        }
        public String blankReplacement(){
            return PlaylumiLetterChallenges.replacement(prompt,answers.get(correctIndex));
        }
        /** Never reveal an incorrect guess as part of the word. */
        public String visibleDisplay() {
            int blank=letterBlank();
            if(!solved || blank<0)return display;
            return display.substring(0,blank)+blankReplacement()+display.substring(blank+1);
        }
        public int rejectedMask(){return rejected;}
        public boolean isRejected(int index){return index>=0&&index<answers.size()&&index<31&&(rejected&(1<<index))!=0;}
        public void restoreRejected(int mask){
            rejected=mask&((1<<Math.min(answers.size(),30))-1)&~(1<<correctIndex);
        }
        public boolean choose(int index) {
            if(solved||index<0||index>=answers.size()||isRejected(index))return false;
            if(index!=correctIndex){if(index<31)rejected|=1<<index;return false;}
            solved=true;return true;
        }
    }

    public LearningGame(Random random) { this.random = random; nextRound(); }
    public int mode() { return mode; }
    public int variant() { return variant; }
    /** Lowercase matching is intentionally a visual-only exercise. */
    public boolean allowsQuestionNarration() { return mode != LETTERS || variant != 0; }
    public int level() { return level; }
    public Round round() { return current; }
    /** Preserve the exact question and its solved state on configuration changes. */
    public void restoreRound(String prompt,String display,int displayColor,List<String> answers,
                             List<Integer> colors,int correct,int fingers,boolean solved) {
        restoreRound(prompt,display,displayColor,answers,colors,correct,fingers,solved,0);
    }
    /** Reorder legacy spatial answers and their rejected flags as a single operation. */
    public void restoreRound(String prompt,String display,int displayColor,List<String> answers,
                             List<Integer> colors,int correct,int fingers,boolean solved,int rejectedMask) {
        if(prompt==null||display==null||answers==null||colors==null||answers.size()<2||answers.size()>30||
                answers.size()!=colors.size()||correct<0||correct>=answers.size()||fingers<0||fingers>10)
            throw new IllegalArgumentException("Invalid saved round");
        for(int i=0;i<answers.size();i++){
            if(answers.get(i)==null||answers.get(i).trim().isEmpty()||colors.get(i)==null)
                throw new IllegalArgumentException("Invalid saved choice");
        }
        int[] order=PlaylumiAnswerOrder.indices(answers);
        int mapped=0;
        for(int i=0;i<order.length;i++)if((rejectedMask&(1<<order[i]))!=0)mapped|=1<<i;
        Round restored=new Round(prompt,display,displayColor,answers,colors,correct,fingers);
        restored.restoreRejected(mapped);
        if(solved)restored.choose(restored.correctIndex);
        current=restored;
    }
    public void setHistory(QuestionHistory history) { this.history = history; }
    public static int maxVariant(int mode){
        if(PlaylumiSkillPractice.supportsMode(mode))return PlaylumiSkillPractice.VARIANT;
        if(mode==ExtraGames.MATH)return PlaylumiThinkingPractice.PLACE_VALUE;
        if(mode==PlaylumiOddOneOut.MODE)return PlaylumiOddOneOut.VARIANT;
        if(mode==PlaylumiFocusedCounting.MODE||mode==PlaylumiWeekdays.MODE||mode==PlaylumiReadingPractice.MODE)return 5;
        if(mode==ExtraGames.COMPARE||mode==PlaylumiThinkingPractice.DAILY_THINGS)return 5;
        return 4;
    }
    public void configure(int mode, int variant, int level) {
        if (mode < 0 || mode >= GAME_NAMES.length || variant < 0 || variant > maxVariant(mode) || level < 0 || level > 2)
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
        if(PlaylumiSkillPractice.supports(mode,variant)){current=PlaylumiSkillPractice.next(mode,variant,level,random);return;}
        if(mode==PlaylumiOddOneOut.MODE&&variant==PlaylumiOddOneOut.VARIANT){
            current=PlaylumiOddOneOut.next(level,random);return;
        }
        if(mode==PlaylumiFocusedCounting.MODE&&variant==PlaylumiFocusedCounting.VARIANT){
            current=PlaylumiFocusedCounting.next(level,random);return;
        }
        if(mode==PlaylumiWeekdays.MODE&&variant==PlaylumiWeekdays.VARIANT){
            current=PlaylumiWeekdays.next(level,random);return;
        }
        if(mode==PlaylumiReadingPractice.MODE&&variant==PlaylumiReadingPractice.VARIANT){
            current=PlaylumiReadingPractice.next(level,random);return;
        }
        if(PlaylumiThinkingPractice.supports(mode,variant)){
            current=PlaylumiThinkingPractice.next(mode,variant,level,random);return;
        }
        if (mode >= NewWorldGames.FIRST) { current = NewWorldGames.next(mode, variant, level, random); return; }
        if(mode==ExtraGames.MATH){current=PlaylumiMathPractice.next(variant,level,random);return;}
        if(mode==NUMBERS && variant<=2) {current=numberRound(variant,level,random);return;}
        if (variant >= 2) { current = VariantGames.next(mode, variant, level, random); return; }
        switch (mode) {
            case COUNT: makeCount(); break;
            case COLORS: makeColors(); break;
            case LETTERS: makeLetters(); break;
            case SHAPES: makeShapes(); break;
            case NUMBERS: current=numberRound(variant,level,random); break;
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
        int count=level==0?3:level==1?4:6, available=level==0?10:level==1?18:UPPER.length;
        int correct=pick(available);
        boolean lowercase=variant==0;
        String[] source=lowercase?LOWER:UPPER;
        List<String> others=new ArrayList<>();
        for(int i=0;i<available;i++)if(i!=correct)others.add(source[i]);
        Collections.shuffle(others,random);
        List<String> answers=new ArrayList<>();answers.add(source[correct]);
        for(int i=0;i<count-1;i++)answers.add(others.get(i));
        Collections.shuffle(answers,random);
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
    private static final String[] NUMBER_WORDS={"één","twee","drie","vier","vijf","zes","zeven","acht","negen","tien"};
    public static String numberWord(int number) {
        if(number<1||number>10)throw new IllegalArgumentException("number");
        return NUMBER_WORDS[number-1];
    }
    /** Match different representations of the same number, never identical text. */
    static Round numberRound(int variant,int level,Random r) {
        if(variant<0||variant>2||level<0||level>2)throw new IllegalArgumentException("number game");
        int limit=level==0?3:level==1?5:10;
        int correct=1+r.nextInt(limit);
        int count=level==0?3:level==1?4:6;
        List<Integer> candidates=new ArrayList<>();
        for(int i=1;i<=limit;i++)if(i!=correct)candidates.add(i);
        Collections.shuffle(candidates,r);
        List<String> answers=new ArrayList<>();
        String answer=variant==1?numberWord(correct):Integer.toString(correct);
        answers.add(answer);
        for(int i=0;i<count-1;i++){
            int n=candidates.get(i);
            answers.add(variant==1?numberWord(n):Integer.toString(n));
        }
        Collections.shuffle(answers,r);
        List<Integer> colors=new ArrayList<>();
        for(int i=0;i<answers.size();i++)colors.add(0xff263b40);
        String prompt=variant==0?"Hoeveel vingers zie je?":variant==1?"Welk woord hoort bij dit getal?":"Welk getal hoort bij dit woord?";
        // The finger marker is internal (history); Android draws the actual hands.
        String display=variant==0?"hand:"+correct:variant==1?Integer.toString(correct):numberWord(correct);
        return new Round(prompt,display,0xff237c88,answers,colors,answers.indexOf(answer),variant==0?correct:0);
    }
    private void makePatterns() {
        current=PlaylumiPatterns.next(level,variant==1?1:0,false,random);
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
        int missing=variant==0?(level==2?1+pick(word.length()-2):word.length()/2):(level==0?0:word.length()-1);
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
