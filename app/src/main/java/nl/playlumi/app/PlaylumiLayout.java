package nl.playlumi.app;

/**
 * Shared layout rules for phones and folding tablets.
 * Pure Java so screen sizing can be regression-tested without an emulator.
 */
public final class PlaylumiLayout {
    public static final int SHORT_ROUND_QUESTIONS=8;
    private PlaylumiLayout() {}

    public static boolean useWideLayout(int widthDp, boolean landscape) {
        return landscape || widthDp >= 600;
    }

    public static int sidePadding(int widthDp) {
        if (widthDp >= 840) return 45;
        if (widthDp >= 600) return 24;
        return 12;
    }

    public static int tileHeight(int requestedDp, boolean game) {
        int minimum = game ? 56 : 60;
        int maximum = game ? 80 : 102;
        int scaled = Math.round(requestedDp * (game ? .82f : .83f));
        return Math.max(minimum, Math.min(maximum, scaled));
    }

    public static int heroHeight(int requestedDp, boolean wide, boolean heroScreen,
                                 int screenHeightDp) {
        int cap = heroScreen ? (wide ? 152 : 174) : (wide ? 84 : 108);
        if (screenHeightDp < 660) cap = Math.min(cap, heroScreen ? 124 : 76);
        return Math.max(54, Math.min(requestedDp, cap));
    }

    public static int sceneHeight(int requestedDp, boolean wide, int heightDp) {
        int cap = wide ? 218 : (heightDp < 700 ? 205 : 256);
        return Math.max(130, Math.min(requestedDp, cap));
    }

    public static int drawingHeight(boolean wide, int heightDp) {
        if (wide) return 215;
        return heightDp < 690 ? 225 : heightDp < 830 ? 265 : 310;
    }

    public static int magicHeroHeight(int widthDp,int heightDp,boolean menu){
        int max=menu?220:heightDp<680?104:148;
        if(widthDp>=600)max=menu?236:176;
        return Math.max(menu?138:85,Math.min(max,Math.round(widthDp*(menu?.39f:.30f))));
    }
    public static int magicQuestionHeight(int widthDp,int heightDp,boolean wide){
        if(wide)return Math.max(145,Math.min(252,Math.round(widthDp*.28f)));
        return Math.max(108,Math.min(heightDp<700?120:150,Math.round(widthDp*.34f)));
    }
    public static int magicAnswerHeight(int heightDp,int choices,boolean wide){
        if(wide)return Math.max(52,Math.min(74,(heightDp-220)/Math.max(4,(choices+1)/2+1)));
        return heightDp<710 || choices>8?52:60;
    }
    public static int magicAnimalHeight(int widthDp,int heightDp){
        int cap=widthDp>=600?192:heightDp<700?112:152;
        return Math.max(102,Math.min(cap,Math.round((widthDp-28)*.37f)));
    }
    public static int margin(int requestedDp) {
        return Math.max(2, Math.min(9, Math.round(requestedDp * .62f)));
    }
}