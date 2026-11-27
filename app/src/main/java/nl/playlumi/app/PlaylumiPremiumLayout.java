package nl.playlumi.app;

/** Keep play space available and keep achievement filters independent of rewards. */
public final class PlaylumiPremiumLayout {
    private static final String[] EMBLEMS={"123","🎨","Aa","◇","🔢","●◆●","Ab","✎","A…Z","🐾","♫","⚖","＋","↗","↔","✦","◷","☀","➜","🏠","🍎","🐾","📏","💡"};
    private PlaylumiPremiumLayout(){}
    public static int quizBannerHeight(int height,boolean wide){
        return height<500?50:height<690?54:wide?78:62;
    }
    public static boolean showAchievement(int filter,boolean earned){
        return filter==0||(filter==1&&earned)||(filter==2&&!earned);
    }
    public static String emblem(int mode,int variant){
        if(PlaylumiSkillPractice.supports(mode,variant)){
            switch(mode){case LearningGame.COUNT:return "±1";case LearningGame.NUMBERS:return "123";case LearningGame.LETTERS:return "A→";case LearningGame.SHAPES:return "▲";case LearningGame.PATTERNS:return "♫";case 19:return "🏠";case LearningGame.COLORS:return "●";default:break;}
        }
        if(mode==PlaylumiFocusedCounting.MODE&&variant==PlaylumiFocusedCounting.VARIANT)return "●▲";
        if(mode==PlaylumiWeekdays.MODE&&variant==PlaylumiWeekdays.VARIANT)return "7";
        if(mode==ExtraGames.MATH&&variant==PlaylumiThinkingPractice.PLACE_VALUE)return "123";
        if(mode==ExtraGames.MATH)return variant==PlaylumiAdvancedMath.TABLES?"×":variant==PlaylumiAdvancedMath.SHARING?"÷":variant==PlaylumiAdvancedMath.TO_HUNDRED?"100":variant==1?"−":variant==2?"?":variant==3?"2×":variant==4?"✦":"＋";
        if(mode==16)return variant==3?"☀":"◷";
        if(mode==LearningGame.PATTERNS)return "◆●◆";
        return mode>=0&&mode<EMBLEMS.length?EMBLEMS[mode]:"✦";
    }
}
