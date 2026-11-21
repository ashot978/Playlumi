package nl.playlumi.app;

/**
 * Shared layout rules for phones and folding tablets.
 * Pure Java so screen sizing can be regression-tested without an emulator.
 */
public final class PlaylumiLayout {
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

    public static int margin(int requestedDp) {
        return Math.max(2, Math.min(9, Math.round(requestedDp * .62f)));
    }
}