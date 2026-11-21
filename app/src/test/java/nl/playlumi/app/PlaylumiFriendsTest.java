package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiFriendsTest {
    @Test public void allActionsBuildFriendshipAndRewardExactlyOnce() {
        PlaylumiFriends friends=new PlaylumiFriends();
        for(int f=0;f<PlaylumiFriends.FRIENDS;f++){
            for(int a=0;a<4;a++){
                boolean unlocked=friends.care(f,a);
                assertEquals(a==3,unlocked);
                assertFalse(friends.care(f,a));
                assertEquals(a+1,friends.progress(f));
            }
            assertTrue(friends.hasReward(f));
        }
        assertEquals(PlaylumiFriends.FRIENDS,friends.completedFriends());
    }
    @Test public void childrenCanResumeAndKeepTheirStickers() {
        PlaylumiFriends before=new PlaylumiFriends();
        before.care(3,0);
        before.care(3,1);
        before.care(3,2);
        before.care(3,3);
        PlaylumiFriends after=new PlaylumiFriends();
        after.restoreRewards(before.rewardedMask());
        for(int f=0;f<PlaylumiFriends.FRIENDS;f++)
            after.restore(f,before.mask(f));
        assertEquals(4,after.progress(3));
        assertEquals(1,after.stickers());
        assertFalse(after.care(3,3));
        assertEquals(1,after.stickers());
    }
    @Test public void everyFriendProgressIsIndependent() {
        PlaylumiFriends friends=new PlaylumiFriends();
        friends.care(1,0);
        friends.care(4,2);
        assertEquals(1,friends.progress(1));
        assertEquals(1,friends.progress(4));
        assertEquals(0,friends.progress(0));
        assertFalse(friends.hasReward(1));
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidActionRejected(){new PlaylumiFriends().care(0,5);}
}
