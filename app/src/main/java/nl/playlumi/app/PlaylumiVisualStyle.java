package nl.playlumi.app;

/** Shared Playlumi visual palette and deterministic, platform-free color math. */
public final class PlaylumiVisualStyle {
    // Calm, editorial colors: warm ivory surfaces and a deep forest-green action.
    public static final int INK=0xff253647;
    public static final int MUTED=0xff586b78;
    public static final int PAGE_TOP=0xfffbf9f3;
    public static final int PAGE_MIDDLE=0xfff5f6f2;
    public static final int PAGE_BOTTOM=0xffedf3ef;
    public static final int PRIMARY_START=0xff176457;
    public static final int PRIMARY_END=0xff184d59;
    public static final int SUCCESS=0xff216a53;
    public static final int SUCCESS_TINT=0xffe0f3e9;
    public static final int ERROR_TINT=0xffffeee9;
    public static final int SURFACE=0xffffffff;
    public static final int BORDER=0xffe0e9e7;
    public static final int MINT=0xffe2f2e9;
    public static final int PEACH=0xffffecdc;
    public static final int LAVENDER=0xffede8f9;
    public static final int SKY=0xffe4f1f7;
    private PlaylumiVisualStyle() {}

    /** Opaque weighted mix in sRGB, 0 means only a and 1000 means only b. */
    public static int blend(int a,int b,int weight) {
        int w=Math.max(0,Math.min(1000,weight)),inv=1000-w;
        int r=(((a>>>16)&255)*inv+((b>>>16)&255)*w+500)/1000;
        int g=(((a>>>8)&255)*inv+((b>>>8)&255)*w+500)/1000;
        int blue=((a&255)*inv+(b&255)*w+500)/1000;
        return 0xff000000 | (r<<16) | (g<<8) | blue;
    }
    public static int lightened(int color,int amount) {
        return blend(color,0xffffffff,amount);
    }
    public static double contrastRatio(int a,int b) {
        double first=luminance(a),second=luminance(b);
        return (Math.max(first,second)+.05)/(Math.min(first,second)+.05);
    }
    private static double luminance(int c) {
        return .2126*channel((c>>>16)&255)+
                .7152*channel((c>>>8)&255)+.0722*channel(c&255);
    }
    private static double channel(int component) {
        double v=component/255.0;
        return v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4);
    }
}
