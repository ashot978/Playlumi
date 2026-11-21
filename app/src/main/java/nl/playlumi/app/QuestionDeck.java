package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * An offline pool of exactly 300 distinct question cards for one game mode,
 * play style and difficulty. Opening a game rebuilds and shuffles its pool.
 *
 * A preschool concept is intentionally practised in several different cards:
 * e.g. there are fewer than 300 basic color names, so a short themed
 * instruction may change while the underlying skill remains age-appropriate.
 * No online AI, subscription, tracker or downloaded content.
 */
public final class QuestionDeck {
    public static final int SIZE = 300;

    public interface Source {
        LearningGame.Round next();
    }

    // Short instructions and settings give constrained beginner concepts
    // varied question cards without changing their correct answer.
    private static final String[] CUES = {
        "Kijk goed", "Jouw beurt", "Speurvraag", "Kies slim", "Zoek maar",
        "Denk even", "Wat denk jij", "Weet jij het", "Ga op zoek", "Let goed op",
        "Een nieuwe vraag", "Doe maar mee", "Kun jij dit", "Tijd om te spelen",
        "Probeer het", "Jij kunt dit", "Kijk eens", "Een klein raadsel",
        "Vind het antwoord", "Ontdek het"
    };
    private static final String[] PLACES = {
        "🌳 Bos", "🏡 Thuis", "🎪 Feest", "🐻 Dierenrijk", "🌈 Regenboog",
        "🏖️ Strand", "🚀 Ruimte", "🎨 Kleurenland", "🦋 Vlindertuin",
        "🎈 Speelplein", "🌻 Tuin", "🏰 Kasteel", "🐴 Boerderij", "⭐ Sterrenland",
        "🧸 Speelkamer", "🌊 Zee", "🌷 Bloementuin", "🍓 Picknick",
        "🛝 Speeltuin", "🎒 Avontuur"
    };

    private final Random random;
    private final List<LearningGame.Round> cards = new ArrayList<>(SIZE);
    private int position;

    private QuestionDeck(Random random) { this.random = random; }

    /** Generate from existing, already validated question factories. */
    public static QuestionDeck create(Random random, Source source) {
        if (random == null || source == null)
            throw new IllegalArgumentException("random and source are required");

        QuestionDeck deck = new QuestionDeck(random);
        Set<String> seen = new HashSet<>(SIZE * 2);

        // Prefer natural diversity: different animals, sums, words, scenes,
        // correct answers and distractor sets from the original game factories.
        // Bound this loop so beginner categories with very small pools never hang.
        for (int attempt = 0; attempt < 480 && deck.cards.size() < SIZE; attempt++) {
            LearningGame.Round next = source.next();
            if (seen.add(identity(next))) deck.cards.add(next);
        }

        // Easy age levels sometimes have only three colors or shapes. Rather
        // than raising the difficulty or inventing false facts, provide a
        // different short themed question for each remaining distinct card.
        for (int scenario = 0; deck.cards.size() < SIZE; scenario++) {
            if (scenario >= CUES.length * PLACES.length)
                throw new IllegalStateException("Question scenarios exhausted");
            LearningGame.Round base = source.next();
            String heading = PLACES[scenario / CUES.length] + " · " +
                    CUES[scenario % CUES.length] + "!";
            LearningGame.Round themed = new LearningGame.Round(
                    heading + "\n" + base.prompt,
                    base.display, base.displayColor, base.answers,
                    base.answerColors, base.correctIndex);
            if (seen.add(identity(themed))) deck.cards.add(themed);
        }

        if (deck.cards.size() != SIZE)
            throw new IllegalStateException("Expected exactly 300 questions");
        Collections.shuffle(deck.cards, random);
        return deck;
    }

    /**
     * Canonical visible card identity, not just random answer-button order.
     * Different distractor selections are distinct multiple-choice tasks.
     */
    public static String identity(LearningGame.Round round) {
        List<String> options = new ArrayList<>(round.answers);
        Collections.sort(options);
        return round.prompt + "\u001f" + round.display + "\u001f" +
                round.displayColor + "\u001f" +
                round.answers.get(round.correctIndex) + "\u001f" +
                String.join("\u001e", options);
    }

    public int size() { return cards.size(); }
    public int remaining() { return SIZE - position; }

    public LearningGame.Round next(QuestionHistory history) {
        if (position == SIZE) {
            // Already solved Round instances cannot be reused directly.
            // The child may play indefinitely; reshuffle a fresh set.
            for (int i = 0; i < cards.size(); i++) {
                LearningGame.Round old = cards.get(i);
                cards.set(i, new LearningGame.Round(old.prompt, old.display,
                        old.displayColor, old.answers, old.answerColors,
                        old.correctIndex));
            }
            Collections.shuffle(cards, random);
            position = 0;
        }

        // Protect against the same base exercise reappearing right after
        // the last session, even though the display/story may be themed.
        if (history != null) {
            int choice = position;
            int oldest = Integer.MAX_VALUE;
            for (int i = position; i < Math.min(SIZE, position + 48); i++) {
                int recency = history.recency(cards.get(i));
                if (recency == 0) { choice = i; break; }
                if (recency < oldest) { choice = i; oldest = recency; }
            }
            Collections.swap(cards, position, choice);
        }

        LearningGame.Round round = cards.get(position++);
        if (history != null) history.remember(round);
        return round;
    }
}
