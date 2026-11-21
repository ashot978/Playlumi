package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * An offline, freshly shuffled deck of 300 distinct layouts for a single
 * mini-game and difficulty. Gameplay stays in PlaylumiMiniChallenge.
 *
 * Creating a new deck on each opening means the child does not repeatedly
 * see the same fixed first puzzle. A complete 20-round session consumes
 * 20 different boards without repeats.
 */
public final class MiniChallengeDeck {
    public static final int SIZE = 300;

    private final int kind;
    private final int level;
    private final Random random;
    private final List<PlaylumiMiniChallenge> boards = new ArrayList<>(SIZE);
    private int position;

    private MiniChallengeDeck(int kind, int level, Random random) {
        this.kind = kind;
        this.level = level;
        this.random = random;
    }

    public static MiniChallengeDeck create(int kind, int level, Random random) {
        if (kind < PlaylumiMiniChallenge.DRAG ||
                kind > PlaylumiMiniChallenge.MEMORY || level < 0 || level > 2 ||
                random == null) {
            throw new IllegalArgumentException("Invalid mini-game deck settings");
        }
        MiniChallengeDeck deck = new MiniChallengeDeck(kind, level, random);
        deck.refill();
        return deck;
    }

    public int kind() { return kind; }
    public int level() { return level; }
    public int size() { return boards.size(); }
    public int remaining() { return boards.size() - position; }

    /**
     * Board identity reflects the target and the ACTUAL visible option order.
     * Memory card positions matter; so do the balloon/shape locations.
     * It is not a generated serial number or merely a different headline.
     */
    public static String identity(PlaylumiMiniChallenge board) {
        if (board == null) throw new IllegalArgumentException("board");
        StringBuilder key = new StringBuilder();
        key.append(board.kind).append('\u001f').append(board.level)
                .append('\u001f').append(board.target).append('\u001f')
                .append(board.correctIndex);
        for (String choice : board.choices) {
            key.append('\u001e').append(choice);
        }
        return key.toString();
    }

    private void refill() {
        boards.clear();
        position = 0;
        Set<String> seen = new HashSet<>(SIZE * 2);

        // Shapes at the easiest tier have seven familiar symbols but only
        // three simultaneous choices. This permits >300 true unique boards
        // without making a round harder or fabricating themed duplicates.
        // The 30k upper bound protects against a generator regression.
        for (int attempts = 0; attempts < 30000 && boards.size() < SIZE; attempts++) {
            PlaylumiMiniChallenge board =
                    PlaylumiMiniChallenge.generate(kind, level, random);
            if (seen.add(identity(board))) boards.add(board);
        }
        if (boards.size() != SIZE) {
            throw new IllegalStateException("Not enough distinct mini-game boards");
        }

        Collections.shuffle(boards, random);
    }

    public PlaylumiMiniChallenge next() {
        if (position >= boards.size()) refill();
        // Each board appears only once before the next shuffle. In particular
        // a completed memory board cannot be accidentally reused as solved.
        return boards.get(position++);
    }
}
