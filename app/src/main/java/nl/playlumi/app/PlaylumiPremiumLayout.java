package nl.playlumi.app;

/** Keep play space available and keep achievement filters independent of rewards. */
public final class PlaylumiPremiumLayout {
    private static final String[] EMBLEMS={"123","🎨","Aa","◇","🔢","●◆●","Ab","✎","A…Z","🐾","♫","⚖","＋","↗","↔","✦","◷","☀","➜","🏠","🍎","🐾","📏","💡"};
    private PlaylumiPremiumLayout(){}
    public static int quizBannerHeight(int height,boolean wide){
        return height<500?50:height<690?60:wide?84:74;
    }
    public static boolean showAchievement(int filter,boolean earned){
        return filter==0||(filter==1&&earned)||(filter==2&&!earned);
    }
    public static String emblem(int mode,int variant){
        if(mode==ExtraGames.MATH)return variant==PlaylumiAdvancedMath.TABLES?"×":variant==PlaylumiAdvancedMath.SHARING?"÷":variant==PlaylumiAdvancedMath.TO_HUNDRED?"100":variant==1?"−":variant==2?"?":variant==3?"2×":variant==4?"✦":"＋";
        if(mode==16)return variant==3?"☀":"◷";
        if(mode==LearningGame.PATTERNS)return "◆●◆";
        return mode>=0&&mode<EMBLEMS.length?EMBLEMS[mode]:"✦";
    }
}
