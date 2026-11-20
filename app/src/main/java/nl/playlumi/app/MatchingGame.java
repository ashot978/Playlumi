package nl.playlumi.app;

import java.util.Random;

public final class MatchingGame {
    private final Random random;
    private final int size;
    private int answer = -1;
    private boolean solved;

    public MatchingGame(Random random, int size) {
        if (size < 2) throw new IllegalArgumentException("size");
        this.random = random;
        this.size = size;
        nextRound();
    }
    public int answer() { return answer; }
    public boolean isSolved() { return solved; }
    public boolean choose(int choice) {
        if (solved || choice != answer) return false;
        solved = true;
        return true;
    }
    public void nextRound() {
        int next = random.nextInt(size - 1);
        if (answer >= 0 && next >= answer) next++;
        else if (answer < 0) next = random.nextInt(size);
        answer = next;
        solved = false;
    }
    public void restore(int answer, boolean solved) {
        if (answer < 0 || answer >= size) throw new IllegalArgumentException("answer");
        this.answer = answer;
        this.solved = solved;
    }
}
