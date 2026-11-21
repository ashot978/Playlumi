package nl.playlumi.app;

/**
 * Persistent friendship journal. Every friend has four different caring actions.
 * A small first-time reward makes exploration fun without forcing daily logins.
 */
public final class PlaylumiFriends {
    public static final int FRIENDS=PlaylumiWardrobe.OPTIONS;
    public static final int CARE_ACTIONS=4;
    public static final int FIRST_COMPLETE_BONUS=14;
    public static final String[] ACTIONS={"Samen spelen","Lekker eten","Even knuffelen","Lekker rusten"};
    public static final String[] ICONS={"🎾","🍎","💗","🌙"};
    public static final String[] REPLIES={"Joepie! We spelen samen!","Mmm, dat was lekker!",
            "Wat een fijne knuffel!","Nu zijn we weer uitgerust!"};
    public static final String[] FRIEND_ICONS={"⭐","🦋","🌙","🐰","☁️","🦊","🐶"};
    private final int[] completed=new int[FRIENDS];
    private int rewardedMask;
    private static void checkFriend(int friend) {
        if(friend<0 || friend>=FRIENDS)throw new IllegalArgumentException("friend");
    }
    private static void checkAction(int action) {
        if(action<0 || action>=CARE_ACTIONS)throw new IllegalArgumentException("action");
    }
    public void restore(int friend,int mask) {
        checkFriend(friend);
        completed[friend]=mask & ((1<<CARE_ACTIONS)-1);
    }
    public void restoreRewards(int mask) {
        rewardedMask=mask & ((1<<FRIENDS)-1);
    }
    public int progress(int friend) {
        checkFriend(friend);return Integer.bitCount(completed[friend]);
    }
    public int mask(int friend){checkFriend(friend);return completed[friend];}
    public int rewardedMask(){return rewardedMask;}
    public boolean cared(int friend,int action) {
        checkFriend(friend);checkAction(action);
        return (completed[friend]&(1<<action))!=0;
    }
    public boolean hasReward(int friend) {
        checkFriend(friend);return (rewardedMask&(1<<friend))!=0;
    }
    /** True only for a newly completed 4-action friendship; never gives repeat bonuses. */
    public boolean care(int friend,int action) {
        checkFriend(friend);checkAction(action);
        completed[friend]|=1<<action;
        if(progress(friend)==CARE_ACTIONS && !hasReward(friend)) {
            rewardedMask|=1<<friend;
            return true;
        }
        return false;
    }
    public int completedFriends(){return Integer.bitCount(rewardedMask);}
    public int stickers(){return completedFriends();}
}
