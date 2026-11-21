package nl.playlumi.app;

import org.junit.Test;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;

/** Full 3 games × 3 levels × 300 unique playable board layouts. */
public final class MiniChallengeDeckTest {

    @Test public void eachGameAndDifficultyHasThreeHundredRealBoardLayouts() {
        for (int kind = 0; kind <= PlaylumiMiniChallenge.MEMORY; kind++) {
            for (int level = 0; level <= 2; level++) {
                MiniChallengeDeck deck = MiniChallengeDeck.create(
                        kind, level, new Random(20261121L + kind * 11 + level));
                assertEquals(kind, deck.kind());
                assertEquals(level, deck.level());
                assertEquals(MiniChallengeDeck.SIZE, deck.size());
                assertEquals(300, deck.remaining());
                Set<String> identities = new HashSet<>();
                for (int i = 0; i < 300; i++) {
                    PlaylumiMiniChallenge board = deck.next();
                    String info = kind + "/" + level + "/" + i;
                    assertTrue("Duplicate board: " + info,
                            identities.add(MiniChallengeDeck.identity(board)));
                    assertEquals(kind, board.kind);
                    assertEquals(level, board.level);
                    assertFalse(board.question.isEmpty());
                    assertFalse(board.choices.isEmpty());
                    assertEquals(299 - i, deck.remaining());

                    if (kind == PlaylumiMiniChallenge.MEMORY) {
                        int pairs = level == 0 ? 3 : level == 1 ? 4 : 6;
                        assertEquals(pairs, board.pairCount());
                        assertEquals(pairs * 2, board.choices.size());
                        Set<String> animals = new HashSet<>(board.choices);
                        assertEquals(pairs, animals.size());
                        for (String value : animals)
                            assertEquals(2, Collections.frequency(board.choices, value));
                        assertFalse(board.solved());
                        assertEquals(0, board.matchedPairs());
                    } else {
                        int choices = level == 0 ? 3 : level == 1 ? 4 : 6;
                        assertEquals(choices, board.choices.size());
                        assertEquals(choices, new HashSet<>(board.choices).size());
                        assertEquals(board.target, board.choices.get(board.correctIndex));
                        for (int n = 0; n < board.choices.size(); n++) {
                            boolean correct = n == board.correctIndex;
                            assertEquals(correct,
                                    kind == PlaylumiMiniChallenge.DRAG ?
                                            board.drop(n) : board.choose(n));
                        }
                    }
                }
                assertEquals(300, identities.size());
                assertEquals(0, deck.remaining());
                assertEquals(300, deck.size());
                // A finished deck automatically creates a new playable board.
                PlaylumiMiniChallenge after = deck.next();
                assertNotNull(after);
                assertEquals(299, deck.remaining());
                assertEquals(kind, after.kind);
                assertEquals(level, after.level);
                assertFalse(after.solved());
            }
        }
    }

    @Test public void memoryCanBePlayedToCompletionWithoutLeakingState() {
        for (int level = 0; level < 3; level++) {
            MiniChallengeDeck deck = MiniChallengeDeck.create(
                    PlaylumiMiniChallenge.MEMORY, level, new Random(90 + level));
            for (int round = 0; round < 10; round++) {
                PlaylumiMiniChallenge board = deck.next();
                for (int first = 0; first < board.choices.size(); first++) {
                    if (board.matched(first)) continue;
                    int twin = -1;
                    for (int other = first + 1; other < board.choices.size(); other++) {
                        if (board.choices.get(first).equals(board.choices.get(other))) {
                            twin = other;
                            break;
                        }
                    }
                    assertTrue(twin > first);
                    assertEquals(1, board.flip(first));
                    int response = board.flip(twin);
                    assertEquals(board.solved() ? 3 : 2, response);
                }
                assertTrue(board.solved());
                assertEquals(board.pairCount(), board.matchedPairs());
            }
            assertFalse(deck.next().solved());
        }
    }

    @Test public void everyOpeningShufflesTheBoardOrder() {
        for (int kind = 0; kind < 3; kind++) {
            MiniChallengeDeck a = MiniChallengeDeck.create(kind, 0, new Random(41));
            MiniChallengeDeck b = MiniChallengeDeck.create(kind, 0, new Random(9876));
            int changed = 0;
            for (int i = 0; i < 20; i++) {
                if (!MiniChallengeDeck.identity(a.next())
                        .equals(MiniChallengeDeck.identity(b.next()))) changed++;
            }
            assertTrue("Different opening should reshuffle " + kind, changed >= 16);
        }
    }

    @Test public void invalidSettingsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MiniChallengeDeck.create(3, 0, new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> MiniChallengeDeck.create(0, -1, new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> MiniChallengeDeck.create(0, 3, new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> MiniChallengeDeck.create(0, 0, null));
    }
}
