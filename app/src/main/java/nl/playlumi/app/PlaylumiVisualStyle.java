package nl.playlumi.app;

/** Shared Playlumi visual palette and deterministic, platform-free color math. */
public final class PlaylumiVisualStyle {
    public static final int INK=0xff302651;
    public static final int MUTED=0xff655c7a;
    public static final int PAGE_TOP=0xfff1f9ff;
    public static final int PAGE_MIDDLE=0xfffff5f9;
    public static final int PAGE_BOTTOM=0xfff6f0ff;
    public static final int PRIMARY_START=0xff107e7a;
    public static final int PRIMARY_END=0xff3658b5;
    public static final int SUCCESS=0xff247969;
    public static final int SUCCESS_TINT=0xffd7f4e5;
    public static final int ERROR_TINT=0xffffeddc;
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
