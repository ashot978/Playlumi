package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Local-only wardrobe and earnable star wallet. No purchases or network access.
 * The first appearance in each category is always free and owned.
 */
public final class PlaylumiWardrobe {
    public static final int COLORS = 0, HATS = 1, GLASSES = 2, FRIENDS = 3, OUTFITS = 4;
    public static final int SLOTS = 5, OPTIONS = 5;
    public static final int STARS_FOR_ANSWER = 3, STARS_FOR_BADGE = 12;
    public static final int STARTER_STARS = 18;
    public static final String[] SLOT_NAMES = {"Sterkleur", "Hoedjes", "Brillen", "Vriendjes", "Kleding"};
    public static final String[] SLOT_ICONS = {"🎨", "🎩", "🕶️", "🐾", "👗"};
    public static final String[][] ITEM_NAMES = {
            {"Zonnig geel", "Suikerspinroze", "Muntgroen", "Lila", "Hemelsblauw"},
            {"Zonder hoed", "Koningskroon", "Feesthoed", "Toverhoed", "Bloemenkrans"},
            {"Zonder bril", "Zonnebril", "Ronde bril", "Hartjesbril", "Regenboogbril"},
            {"Lekker alleen", "Vlindervriend", "Maantje", "Konijntje", "Wolkje"},
            {"Gewoon mezelf", "Strikje", "Superheldencape", "Warme sjaal", "Sterrenpak"}
    };
    public static final String[][] ITEM_ICONS = {
            {"💛","🩷","💚","💜","💙"},
            {"🌟","👑","🥳","🧙","🌸"},
            {"😊","🕶️","👓","💗","🌈"},
            {"⭐","🦋","🌙","🐰","☁️"},
            {"✨","🎀","🦸","🧣","🚀"}
    };
    public static final int[][] PRICES = {
            {0,12,24,36,50},
            {0,18,35,55,75},
            {0,15,30,48,65},
            {0,16,34,52,72},
            {0,18,38,58,78}
    };
    public static final int[] BODY_COLORS = {0xffffd557,0xffffabd8,0xff91e3ad,0xffc2a2ef,0xff8dd7f6};
    public static final int PURCHASED=2, EQUIPPED=1, NOT_ENOUGH=0;
    private final int[] ownedMasks = new int[SLOTS];
    private final int[] selected = new int[SLOTS];
    private int stars, allTimeStars;

    public PlaylumiWardrobe() {
        this(STARTER_STARS);
    }
    public PlaylumiWardrobe(int start) {
        if(start<0) throw new IllegalArgumentException("negative start");
        stars=start;
        allTimeStars=start;
        for(int slot=0;slot<SLOTS;slot++) ownedMasks[slot]=1;
    }
    private void validate(int slot,int item) {
        if(slot<0 || slot>=SLOTS || item<0 || item>=OPTIONS)
            throw new IllegalArgumentException("unknown wardrobe item");
    }
    public int stars(){return stars;}
    public int allTimeStars(){return allTimeStars;}
    public int equipped(int slot) {
        validate(slot,0);
        return selected[slot];
    }
    public int ownedMask(int slot) {
        validate(slot,0);
        return ownedMasks[slot];
    }
    public boolean owned(int slot,int item) {
        validate(slot,item);
        return (ownedMasks[slot] & (1<<item))!=0;
    }
    public int price(int slot,int item) {
        validate(slot,item);
        return PRICES[slot][item];
    }
    public void restoreBalance(int current,int total) {
        stars=Math.max(0,current);
        allTimeStars=Math.max(stars,total);
    }
    public void restoreSlot(int slot,int mask,int worn) {
        validate(slot,0);
        ownedMasks[slot]=(mask & ((1<<OPTIONS)-1))|1;
        selected[slot]=(worn>=0 && worn<OPTIONS && owned(slot,worn))?worn:0;
    }
    public void earnAnswer() { add(STARS_FOR_ANSWER); }
    public void earnBadges(int amount) {
        if(amount<0) throw new IllegalArgumentException("negative badges");
        add(STARS_FOR_BADGE*amount);
    }
    private void add(int amount) {
        if (amount<0 || stars>Integer.MAX_VALUE-amount || allTimeStars>Integer.MAX_VALUE-amount)
            throw new IllegalArgumentException("wallet full");
        stars+=amount;
        allTimeStars+=amount;
    }
    /** Buy once, equip forever; returns whether item was purchased, equipped or unaffordable. */
    public int buyOrEquip(int slot,int item) {
        validate(slot,item);
        if(owned(slot,item)) {
            selected[slot]=item;
            return EQUIPPED;
        }
        int cost=price(slot,item);
        if(stars<cost)return NOT_ENOUGH;
        stars-=cost;
        ownedMasks[slot]|=(1<<item);
        selected[slot]=item;
        return PURCHASED;
    }
    public int unlockedItems() {
        int count=0;
        for(int mask:ownedMasks)count+=Integer.bitCount(mask);
        return count;
    }
}
