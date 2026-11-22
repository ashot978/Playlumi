package nl.playlumi.app;

/**
 * Six picture-first activities for each age, with short safe names.
 * Children choose an illustration and immediately start an eight-question
 * shuffled round. The full 24-game catalog remains accessible separately.
 */
public final class PlaylumiPicturePlay {
    public static final int COUNT=6;
    private static final int[][] GAMES={
            {LearningGame.COUNT,LearningGame.COLORS,LearningGame.SHAPES,
                    LearningGame.NUMBERS,21,17},
            {LearningGame.COUNT,LearningGame.LETTERS,LearningGame.SHAPES,
                    LearningGame.PATTERNS,21,20},
            {12,7,10,16,22,23}
    };
    private static final String[][] NAMES={
            {"Tellen","Kleuren","Vormen","Cijfers","Dieren","Weer"},
            {"Tellen","Letters","Vormen","Patronen","Dieren","Eten"},
            {"Sommen","Spelling","Rijmen","Klok","Meten","Puzzels"}
    };
    private static final String[][] PICTURES={
            {"🧮","🎨","🔷","🔢","🐶","🌦️"},
            {"🧮","🔤","🔺","🧩","🦁","🍎"},
            {"➕","✏️","🎵","🕒","📏","💡"}
    };

    private PlaylumiPicturePlay(){}

    private static int checkedAge(int age){
        if(age<0 || age>=GAMES.length)
            throw new IllegalArgumentException("age");
        return age;
    }
    private static int checkedIndex(int item){
        if(item<0 || item>=COUNT)
            throw new IllegalArgumentException("picture");
        return item;
    }
    public static int game(int age,int item){
        return GAMES[checkedAge(age)][checkedIndex(item)];
    }
    public static String label(int age,int item){
        return NAMES[checkedAge(age)][checkedIndex(item)];
    }
    public static String icon(int age,int item){
        return PICTURES[checkedAge(age)][checkedIndex(item)];
    }
    /** Friendly, local text-to-speech hint for children who cannot read yet. */
    public static String spokenLabel(int age,int item) {
        return "Dit is "+label(age,item)+". Tik om te spelen.";
    }
    public static int difficulty(int age){return checkedAge(age);}
    public static int variant(int age,int item){
        checkedAge(age);checkedIndex(item);return 0;
    }
    public static int columns(int widthDp){
        return widthDp>=600?3:2;
    }
}
