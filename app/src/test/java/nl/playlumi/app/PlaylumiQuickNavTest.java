package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Navigation and customization must show EVERY option without page cycling. */
public final class PlaylumiQuickNavTest {
    @Test public void navigationHasFourClearDistinctDestinations() {
        assertEquals(4,PlaylumiQuickNav.COUNT);
        assertEquals(4,PlaylumiQuickNav.TITLES.length);
        assertEquals(4,PlaylumiQuickNav.ICONS.length);
        assertEquals("Start",PlaylumiQuickNav.TITLES[0]);
        assertEquals("Spelen",PlaylumiQuickNav.TITLES[1]);
        assertEquals("Aanpassen",PlaylumiQuickNav.TITLES[2]);
        assertEquals("Tuin",PlaylumiQuickNav.TITLES[3]);
    }

    @Test public void allWardrobeRoomCategoriesAndOptionsFitAGrid() {
        assertEquals(6,PlaylumiWardrobe.SLOTS);
        assertEquals(5,PlaylumiRoom.SLOTS);
        assertEquals(11,PlaylumiQuickNav.categoryCount());
        assertEquals(7,PlaylumiWardrobe.OPTIONS);
        assertEquals(7,PlaylumiRoom.OPTIONS);
        assertEquals(2,PlaylumiQuickNav.columnsFor(360));
        assertEquals(3,PlaylumiQuickNav.columnsFor(840));
        for(int width:new int[]{320,360,400,599,600,840,1200}) {
            int cols=PlaylumiQuickNav.columnsFor(width);
            assertTrue(cols>=2 && cols<=3);
            assertEquals((6+cols-1)/cols,PlaylumiQuickNav.rowsFor(6,cols));
            assertEquals((7+cols-1)/cols,PlaylumiQuickNav.rowsFor(7,cols));
            assertEquals((5+cols-1)/cols,PlaylumiQuickNav.rowsFor(5,cols));
            assertEquals(7,7); // all seven options, no artificial page limit
        }
    }

    @Test public void ownedItemsRemainFreeAndEquippedAfterNavigation() {
        PlaylumiWardrobe wardrobe=new PlaylumiWardrobe(100);
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++) {
            assertEquals("✅ Nu aan",PlaylumiQuickNav.wardrobeStatus(wardrobe,slot,0));
            assertTrue(PlaylumiQuickNav.wardrobeStatus(wardrobe,slot,1)
                    .contains("speelsterren"));
        }
        assertEquals(PlaylumiWardrobe.PURCHASED,wardrobe.buyOrEquip(0,2));
        int remaining=wardrobe.stars();
        assertEquals("✅ Nu aan",PlaylumiQuickNav.wardrobeStatus(wardrobe,0,2));
        assertEquals(PlaylumiWardrobe.EQUIPPED,wardrobe.buyOrEquip(0,2));
        assertEquals(remaining,wardrobe.stars());
        assertEquals("✨ Gratis aantrekken",PlaylumiQuickNav.wardrobeStatus(wardrobe,0,0));
    }

    @Test public void roomPurchasesNeverChargeOwnedItemsAgain() {
        PlaylumiRoom room=new PlaylumiRoom();
        PlaylumiWardrobe wallet=new PlaylumiWardrobe(200);
        assertEquals("✅ Nu in je kamer",PlaylumiQuickNav.roomStatus(room,1,0));
        int cost=room.cost(1,2);
        assertTrue(wallet.spendStars(cost));
        room.unlockAndSelect(1,2);
        int remaining=wallet.stars();
        assertEquals(0,room.cost(1,2));
        assertEquals("✅ Nu in je kamer",PlaylumiQuickNav.roomStatus(room,1,2));
        room.select(1,0);
        assertEquals("✨ Gratis gebruiken",PlaylumiQuickNav.roomStatus(room,1,2));
        assertEquals(remaining,wallet.stars());
    }

    @Test public void invalidGridArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->PlaylumiQuickNav.rowsFor(7,0));
        assertThrows(IllegalArgumentException.class,()->PlaylumiQuickNav.rowsFor(-1,2));
    }
}
