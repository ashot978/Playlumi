package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiTryOnTest {
    @Test public void lockedWardrobePreviewDoesNotSpendStarsOrUnlock() {
        PlaylumiWardrobe w=new PlaylumiWardrobe(200);
        assertEquals(0,w.equipped(PlaylumiWardrobe.HATS));
        assertFalse(w.owned(PlaylumiWardrobe.HATS,6));
        int stars=w.stars();
        PlaylumiWardrobe preview=PlaylumiTryOn.previewWardrobe(
                w,PlaylumiWardrobe.HATS,6);
        assertEquals(6,preview.equipped(PlaylumiWardrobe.HATS));
        assertTrue(preview.owned(PlaylumiWardrobe.HATS,6));
        assertEquals(w.unlockedItems()+1,preview.unlockedItems()); // only preview copy changes
        assertEquals(stars,preview.stars());
        assertEquals(stars,w.stars());
        assertEquals(0,w.equipped(PlaylumiWardrobe.HATS));
        assertFalse(w.owned(PlaylumiWardrobe.HATS,6));
    }

    @Test public void previewCopiesAllOtherSlotsAndSavedSelection() {
        PlaylumiWardrobe w=new PlaylumiWardrobe(500);
        assertEquals(PlaylumiWardrobe.PURCHASED,w.buyOrEquip(0,4));
        assertEquals(PlaylumiWardrobe.PURCHASED,w.buyOrEquip(2,3));
        int wallet=w.stars();
        PlaylumiWardrobe p=PlaylumiTryOn.previewWardrobe(w,1,5);
        assertEquals(4,p.equipped(0));
        assertEquals(3,p.equipped(2));
        assertEquals(5,p.equipped(1));
        assertEquals(0,w.equipped(1));
        assertEquals(wallet,p.stars());
        assertEquals(wallet,w.stars());
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++)
            if(slot!=1)assertEquals(w.ownedMask(slot),p.ownedMask(slot));
        PlaylumiWardrobe actual=PlaylumiTryOn.previewWardrobe(w,1,-1);
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++)
            assertEquals(w.equipped(slot),actual.equipped(slot));
    }

    @Test public void roomPreviewNeverModifiesRealRoomOrWallet() {
        PlaylumiRoom room=new PlaylumiRoom();
        PlaylumiWardrobe wallet=new PlaylumiWardrobe(160);
        assertFalse(room.owned(PlaylumiRoom.WINDOW,6));
        PlaylumiRoom preview=PlaylumiTryOn.previewRoom(room,PlaylumiRoom.WINDOW,6);
        assertEquals(6,preview.selected(PlaylumiRoom.WINDOW));
        assertEquals(0,room.selected(PlaylumiRoom.WINDOW));
        assertFalse(room.owned(PlaylumiRoom.WINDOW,6));
        assertEquals(0,preview.selected(PlaylumiRoom.FLOOR));
        assertEquals(160,wallet.stars());
    }

    @Test public void filtersShowAllOwnedAndLockedConsistently() {
        PlaylumiWardrobe w=new PlaylumiWardrobe(220);
        PlaylumiRoom room=new PlaylumiRoom();
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++) {
            assertEquals(7,PlaylumiTryOn.wardrobeCount(w,slot,PlaylumiTryOn.ALL));
            assertEquals(1,PlaylumiTryOn.wardrobeCount(w,slot,PlaylumiTryOn.OWNED));
            assertEquals(6,PlaylumiTryOn.wardrobeCount(w,slot,PlaylumiTryOn.TO_UNLOCK));
        }
        for(int slot=0;slot<PlaylumiRoom.SLOTS;slot++) {
            assertEquals(7,PlaylumiTryOn.roomCount(room,slot,PlaylumiTryOn.ALL));
            assertEquals(1,PlaylumiTryOn.roomCount(room,slot,PlaylumiTryOn.OWNED));
            assertEquals(6,PlaylumiTryOn.roomCount(room,slot,PlaylumiTryOn.TO_UNLOCK));
        }
        assertEquals(PlaylumiWardrobe.PURCHASED,w.buyOrEquip(0,1));
        assertEquals(2,PlaylumiTryOn.wardrobeCount(w,0,PlaylumiTryOn.OWNED));
        assertEquals(5,PlaylumiTryOn.wardrobeCount(w,0,PlaylumiTryOn.TO_UNLOCK));
        room.unlockAndSelect(0,2);
        assertEquals(2,PlaylumiTryOn.roomCount(room,0,PlaylumiTryOn.OWNED));
        assertEquals(5,PlaylumiTryOn.roomCount(room,0,PlaylumiTryOn.TO_UNLOCK));
        for(int n=0;n<7;n++) {
            assertTrue(PlaylumiTryOn.showItem(n==0,PlaylumiTryOn.ALL));
            assertEquals(n==0,PlaylumiTryOn.showItem(n==0,PlaylumiTryOn.OWNED));
            assertEquals(n!=0,PlaylumiTryOn.showItem(n==0,PlaylumiTryOn.TO_UNLOCK));
        }
    }

    @Test public void invalidParametersFailSafely() {
        PlaylumiWardrobe w=new PlaylumiWardrobe();
        PlaylumiRoom room=new PlaylumiRoom();
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiTryOn.previewWardrobe(w,-1,0));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiTryOn.previewWardrobe(w,0,7));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiTryOn.previewRoom(room,5,1));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiTryOn.previewRoom(room,0,-2));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiTryOn.showItem(true,-1));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiTryOn.wardrobeCount(w,0,3));
    }
}
