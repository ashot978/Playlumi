package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;

/** Bounded, persisted recent-question history shared by all modes and difficulties. */
public final class QuestionHistory {
    private static final int MAX = 600;
    private final List<String> recent = new ArrayList<>();

    public QuestionHistory() {}
    public QuestionHistory(String stored) {
        if (stored == null || stored.length() == 0) return;
        for (String value : stored.split(",")) {
            if (value.matches("[0-9a-f]{16}") && !recent.contains(value))
                recent.add(value);
        }
        while (recent.size() > MAX) recent.remove(0);
    }
    public String fingerprint(LearningGame.Round round) {
        // Deliberately ignores mode, difficulty, answer ordering and superficial prompt wording;
        // the child should not see the same problem again through a different menu tile.
        String content = round.display + "\u001f" + round.answers.get(round.correctIndex);
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < content.length(); i++) {
            hash ^= content.charAt(i);
            hash *= 0x100000001b3L;
        }
        return String.format(java.util.Locale.ROOT, "%016x", hash);
    }
    /** Zero for a fresh question; otherwise larger means seen more recently. */
    public int recency(LearningGame.Round round) {
        int index = recent.indexOf(fingerprint(round));
        return index < 0 ? 0 : index + 1;
    }
    public void remember(LearningGame.Round round) {
        String key = fingerprint(round);
        recent.remove(key);
        recent.add(key);
        while (recent.size() > MAX) recent.remove(0);
    }
    public int size() { return recent.size(); }
    public String serialize() { return String.join(",", recent); }
}
