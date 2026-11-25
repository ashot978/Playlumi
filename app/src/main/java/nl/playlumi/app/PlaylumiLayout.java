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

    public static int magicSidePadding(int widthDp){
        if(widthDp<600)return 8;
        return Math.max(20,(widthDp-760)/2);
    }
    public static boolean centeredMagicQuiz(int widthDp,int heightDp){
        return widthDp>=600&&heightDp>=620;
    }
    public static int centeredQuestionHeight(int heightDp){
        return Math.max(112,Math.min(170,Math.round(heightDp*.19f)));
    }
    public static int centeredAnswerColumns(int count){
        return count>=5?3:2;
    }
    public static int centeredAnswerHeight(int heightDp,int count){
        int rows=(count+centeredAnswerColumns(count)-1)/centeredAnswerColumns(count);
        int available=heightDp-56-120-42-64-centeredQuestionHeight(heightDp)-106;
        return Math.max(50,Math.min(76,(available-rows*12)/rows));
    }
    public static int magicHeroHeight(int widthDp,int heightDp,boolean menu){
        int max=menu?220:heightDp<680?104:148;
        if(widthDp>=600)max=menu?190:heightDp<690?92:120;
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
    /** Shape board uses the available orientation, not a fixed phone layout. */
    public static boolean shapeWide(int widthDp,int heightDp){
        return widthDp>=600 || (widthDp>=520 && widthDp>heightDp);
    }
    public static int shapeColumns(int widthDp,int heightDp,int count){
        int boardWidth=shapeWide(widthDp,heightDp)?Math.round(widthDp*.52f):widthDp;
        return (count==3||count>4)&&boardWidth>=294?3:2;
    }
    public static int shapeHeroHeight(int heightDp){
        return heightDp<470?54:heightDp<640?72:98;
    }
    public static int shapeTileHeight(int widthDp,int heightDp,int count){
        boolean wide=shapeWide(widthDp,heightDp);
        int columns=shapeColumns(widthDp,heightDp,count);
        int rows=(count+columns-1)/columns;
        int boardWidth=wide?Math.round(widthDp*.52f):widthDp;
        int cardWidth=(boardWidth-32-columns*8)/columns;
        int available=heightDp-56-shapeHeroHeight(heightDp)-56-40-84-(wide?0:shapeTargetHeight(widthDp,heightDp,count));
        int cap=Math.min(Math.round(cardWidth*.90f),(available-rows*8)/rows);
        return Math.max(54,Math.min(wide?118:100,cap));
    }
    public static int shapeTargetHeight(int widthDp,int heightDp,int count){
        if(shapeWide(widthDp,heightDp)){
            int columns=shapeColumns(widthDp,heightDp,count),rows=(count+columns-1)/columns;
            // The wide board target is aligned to the actual height of its cards.
            return Math.max(112,Math.min(248,rows*(shapeTileHeight(widthDp,heightDp,count)+8)));
        }
        return Math.max(100,Math.min(168,Math.round(heightDp*.205f)));
    }
    public static int margin(int requestedDp) {
        return Math.max(2, Math.min(9, Math.round(requestedDp * .62f)));
    }
}