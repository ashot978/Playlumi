package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaylumiWardrobeTest {
    @Test public void freeOutfitsAndStarterStars() {
        PlaylumiWardrobe wallet=new PlaylumiWardrobe();
        assertEquals(18,wallet.stars());
        assertEquals(PlaylumiWardrobe.SLOTS,wallet.unlockedItems());
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++) {
            assertTrue(wallet.owned(slot,0));
            assertEquals(0,wallet.equipped(slot));
        }
    }
    @Test public void earnedStarsPayForRealItemsAndDoNotDisappearWhenEquipping() {
        PlaylumiWardrobe wallet=new PlaylumiWardrobe();
        wallet.earnAnswer(); // 21
        assertEquals(PlaylumiWardrobe.PURCHASED,
                wallet.buyOrEquip(PlaylumiWardrobe.HATS,1));
        assertEquals(3,wallet.stars()); // crown 18
        assertEquals(1,wallet.equipped(PlaylumiWardrobe.HATS));
        assertEquals(PlaylumiWardrobe.EQUIPPED,
                wallet.buyOrEquip(PlaylumiWardrobe.HATS,0));
        assertEquals(3,wallet.stars());
        assertEquals(PlaylumiWardrobe.EQUIPPED,
                wallet.buyOrEquip(PlaylumiWardrobe.HATS,1));
        assertEquals(3,wallet.stars()); // no repeated purchase
    }
    @Test public void cannotBuyWithoutEnoughStars() {
        PlaylumiWardrobe wallet=new PlaylumiWardrobe();
        assertEquals(PlaylumiWardrobe.NOT_ENOUGH,
                wallet.buyOrEquip(PlaylumiWardrobe.FRIENDS,4));
        assertFalse(wallet.owned(PlaylumiWardrobe.FRIENDS,4));
        assertEquals(18,wallet.stars());
    }
    @Test public void previousPurchasesAndAppearanceSurviveRestore() {
        PlaylumiWardrobe before=new PlaylumiWardrobe();
        before.earnBadges(2);
        before.buyOrEquip(PlaylumiWardrobe.COLORS,2);
        before.buyOrEquip(PlaylumiWardrobe.GLASSES,1);
        PlaylumiWardrobe restored=new PlaylumiWardrobe();
        restored.restoreBalance(before.stars(),before.allTimeStars());
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++)
            restored.restoreSlot(slot,before.ownedMask(slot),before.equipped(slot));
        assertEquals(before.stars(),restored.stars());
        assertEquals(2,restored.equipped(PlaylumiWardrobe.COLORS));
        assertEquals(1,restored.equipped(PlaylumiWardrobe.GLASSES));
        assertTrue(restored.owned(PlaylumiWardrobe.COLORS,2));
        assertTrue(restored.owned(PlaylumiWardrobe.GLASSES,1));
    }
    @Test public void rewardScaleIsPredictableAndNonPurchaseBased() {
        PlaylumiWardrobe wallet=new PlaylumiWardrobe(0);
        for(int i=0;i<10;i++)wallet.earnAnswer();
        wallet.earnBadges(2);
        assertEquals(54,wallet.stars());
        assertEquals(54,wallet.allTimeStars());
    }
    @Test public void allCatalogItemsHaveNamesAndStrictlyIncreasingPrices() {
        assertEquals(PlaylumiWardrobe.SLOTS,PlaylumiWardrobe.SLOT_NAMES.length);
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++) {
            assertEquals(PlaylumiWardrobe.OPTIONS,PlaylumiWardrobe.ITEM_NAMES[slot].length);
            assertEquals(PlaylumiWardrobe.OPTIONS,PlaylumiWardrobe.ITEM_ICONS[slot].length);
            assertEquals(PlaylumiWardrobe.OPTIONS,PlaylumiWardrobe.PRICES[slot].length);
            int previous=-1;
            for(int item=0;item<PlaylumiWardrobe.OPTIONS;item++) {
                assertFalse(PlaylumiWardrobe.ITEM_NAMES[slot][item].isEmpty());
                assertTrue(PlaylumiWardrobe.PRICES[slot][item]>previous);
                previous=PlaylumiWardrobe.PRICES[slot][item];
            }
        }
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidSlotFails() {new PlaylumiWardrobe().buyOrEquip(8,3);}
    @Test(expected=IllegalArgumentException.class)
    public void invalidItemFails() {new PlaylumiWardrobe().buyOrEquip(0,PlaylumiWardrobe.OPTIONS);}

    @Test public void existingReleaseInventoryRemainsCompatibleWithNewOptions() {
        PlaylumiWardrobe original=new PlaylumiWardrobe();
        original.restoreSlot(PlaylumiWardrobe.HATS,1|(1<<2)|(1<<4),4);
        original.restoreSlot(PlaylumiWardrobe.COLORS,1|(1<<1),1);
        assertEquals(4,original.equipped(PlaylumiWardrobe.HATS));
        assertTrue(original.owned(PlaylumiWardrobe.HATS,2));
        assertEquals(0,original.equipped(PlaylumiWardrobe.AURAS));
        assertTrue(original.owned(PlaylumiWardrobe.AURAS,0));
    }
    @Test public void newMagicalAurasCanBeBoughtSavedAndEquipped() {
        PlaylumiWardrobe before=new PlaylumiWardrobe(250);
        assertEquals(PlaylumiWardrobe.PURCHASED,
                before.buyOrEquip(PlaylumiWardrobe.AURAS,6));
        assertEquals(105,before.stars());
        PlaylumiWardrobe after=new PlaylumiWardrobe();
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++)
            after.restoreSlot(slot,before.ownedMask(slot),before.equipped(slot));
        assertTrue(after.owned(PlaylumiWardrobe.AURAS,6));
        assertEquals(6,after.equipped(PlaylumiWardrobe.AURAS));
        assertEquals(PlaylumiWardrobe.EQUIPPED,
                after.buyOrEquip(PlaylumiWardrobe.AURAS,6));
    }
}
