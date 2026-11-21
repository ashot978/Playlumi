package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiRoomTest {
    @Test public void defaultDecorationsAreFreeAndOwned() {
        PlaylumiRoom room=new PlaylumiRoom();
        assertEquals(5,room.ownedItems());
        for(int slot=0;slot<PlaylumiRoom.SLOTS;slot++) {
            assertTrue(room.owned(slot,0));
            assertEquals(0,room.selected(slot));
            assertEquals(0,room.cost(slot,0));
        }
    }
    @Test public void unlockWithEarnedStarsOnceThenEquipForFree() {
        PlaylumiRoom room=new PlaylumiRoom();
        PlaylumiWardrobe wallet=new PlaylumiWardrobe();
        int cost=room.cost(0,1);
        assertEquals(15,cost);
        assertTrue(wallet.spendStars(cost));
        room.unlockAndSelect(0,1);
        assertEquals(3,wallet.stars());
        assertEquals(1,room.selected(0));
        assertEquals(0,room.cost(0,1));
        room.select(0,0);
        room.select(0,1);
        assertEquals(3,wallet.stars());
    }
    @Test public void purchasesPersistAndAreIndependent() {
        PlaylumiRoom before=new PlaylumiRoom();
        before.unlockAndSelect(2,2);
        before.unlockAndSelect(4,4);
        PlaylumiRoom restored=new PlaylumiRoom();
        for(int slot=0;slot<PlaylumiRoom.SLOTS;slot++)
            restored.restore(slot,before.ownedMask(slot),before.selected(slot));
        assertEquals(2,restored.selected(2));
        assertEquals(4,restored.selected(4));
        assertTrue(restored.owned(2,2));
        assertFalse(restored.owned(1,2));
    }
    @Test public void restoresInvalidSelectionsSafely() {
        PlaylumiRoom room=new PlaylumiRoom();
        room.restore(3,0,4);
        assertEquals(0,room.selected(3));
        assertEquals(1,room.ownedMask(3));
    }
    @Test public void catalogIsConsistent() {
        for(int s=0;s<PlaylumiRoom.SLOTS;s++) {
            assertEquals(5,PlaylumiRoom.NAMES[s].length);
            assertEquals(5,PlaylumiRoom.ICONS[s].length);
            assertEquals(5,PlaylumiRoom.PRICES[s].length);
            for(int i=1;i<PlaylumiRoom.OPTIONS;i++) {
                assertTrue(PlaylumiRoom.PRICES[s][i]>PlaylumiRoom.PRICES[s][i-1]);
                assertFalse(PlaylumiRoom.NAMES[s][i].isEmpty());
            }
        }
    }
    @Test(expected=IllegalStateException.class)
    public void lockedItemCannotBeEquipped() {
        new PlaylumiRoom().select(2,4);
    }
    @Test(expected=IllegalArgumentException.class)
    public void negativeWalletSpendRejected() {
        new PlaylumiWardrobe().spendStars(-1);
    }
}
