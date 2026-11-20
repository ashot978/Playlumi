package nl.playlumi.app;

import java.util.Random;

/** Rules are independent of Android so they can be tested separately. */
public final class CountingGame {
    private final Random random;
    private int answer;
    private boolean solved;

    public CountingGame(Random random) { this.random = random; nextRound(); }
    public int answer() { return answer; }
    public boolean isSolved() { return solved; }
    public boolean choose(int number) {
        if (solved || number != answer) return false;
        solved = true;
        return true;
    }
    public void nextRound() {
        int next = random.nextInt(5) + 1;
        if (next == answer) next = next % 5 + 1;
        answer = next;
        solved = false;
    }
    public void restore(int answer, boolean solved) {
        if (answer < 1 || answer > 5) throw new IllegalArgumentException("answer");
        this.answer = answer;
        this.solved = solved;
    }
}
