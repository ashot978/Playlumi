package nl.playlumi.app;

import java.util.Random;

/** Rules are independent of Android so they can be tested separately. */
public final class CountingGame {
    private final Random random;
    private int answer;
    private int maximum = 5;
    private boolean solved;

    public CountingGame(Random random) { this.random = random; nextRound(); }
    public int answer() { return answer; }
    public boolean isSolved() { return solved; }
    public int maximum() { return maximum; }
    public void setMaximum(int maximum) {
        if (maximum != 3 && maximum != 5 && maximum != 10) throw new IllegalArgumentException("maximum");
        this.maximum = maximum;
        nextRound();
    }
    public boolean choose(int number) {
        if (solved || number != answer) return false;
        solved = true;
        return true;
    }
    public void nextRound() {
        int next = random.nextInt(maximum) + 1;
        if (next == answer) next = next % maximum + 1;
        answer = next;
        solved = false;
    }
    public void restore(int answer, boolean solved) {
        if (answer < 1 || answer > maximum) throw new IllegalArgumentException("answer");
        this.answer = answer;
        this.solved = solved;
    }
}
