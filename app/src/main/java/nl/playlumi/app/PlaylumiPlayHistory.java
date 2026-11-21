package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-profile last-used standard learning activities.
 * Keeps only four game configurations. Never stores answers or personal data.
 *
 * Reopening a card begins a fresh round; no partially answered question is
 * replayed. Adventurers' story progress is managed independently.
 */
public final class PlaylumiPlayHistory {
    public static final int CAPACITY = 4;
    private static final int MAX_INPUT_LENGTH = 256;
    private final List<Entry> items = new ArrayList<>(CAPACITY);

    public static final class Entry {
        public final int mode, variant, difficulty;
        public final boolean smart;

        public Entry(int mode, int variant, int difficulty, boolean smart) {
            if (mode < 0 || mode >= LearningGame.GAME_NAMES.length ||
                    variant < 0 || variant >= 5 ||
                    difficulty < 0 || difficulty > 2)
                throw new IllegalArgumentException("Invalid learning game");
            this.mode = mode;
            this.variant = variant;
            this.difficulty = difficulty;
            this.smart = smart;
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Entry)) return false;
            Entry e=(Entry)other;
            return mode==e.mode && variant==e.variant &&
                    difficulty==e.difficulty && smart==e.smart;
        }

        @Override public int hashCode() {
            return (((mode*7+variant)*5+difficulty)*2)+(smart?1:0);
        }
    }

    public int size() { return items.size(); }

    public Entry get(int index) { return items.get(index); }

    public void record(int mode, int variant, int difficulty, boolean smart) {
        record(new Entry(mode, variant, difficulty, smart));
    }

    public void record(Entry entry) {
        if (entry == null) throw new IllegalArgumentException("Missing game");
        // The same question game and variant may only appear once, including
        // when its difficulty changes or adaptive learning is enabled.
        for (int i=items.size()-1;i>=0;i--) {
            Entry previous=items.get(i);
            if (previous.mode==entry.mode && previous.variant==entry.variant)
                items.remove(i);
        }
        items.add(0,entry);
        while(items.size()>CAPACITY)items.remove(items.size()-1);
    }

    /** Deliberately simple, bounded local preference format. */
    public String serialize() {
        StringBuilder result=new StringBuilder();
        for(Entry e:items) {
            if(result.length()>0)result.append(';');
            result.append(e.mode).append(',')
                    .append(e.variant).append(',')
                    .append(e.difficulty).append(',')
                    .append(e.smart?'1':'0');
        }
        return result.toString();
    }

    /**
     * Corrupt or outdated user preferences never crash the home screen.
     * Supports newly upgraded profiles with an empty history.
     */
    public static PlaylumiPlayHistory restore(String serialized) {
        PlaylumiPlayHistory history=new PlaylumiPlayHistory();
        if(serialized==null || serialized.isEmpty() ||
                serialized.length()>MAX_INPUT_LENGTH)return history;
        String[] records=serialized.split(";",-1);
        for(int i=records.length-1;i>=0;i--) {
            String[] fields=records[i].split(",",-1);
            if(fields.length!=4)continue;
            try {
                if(!fields[3].equals("0") && !fields[3].equals("1"))continue;
                history.record(Integer.parseInt(fields[0]),
                        Integer.parseInt(fields[1]),
                        Integer.parseInt(fields[2]),
                        fields[3].equals("1"));
            } catch(IllegalArgumentException ignored) {
                // Only the invalid item is ignored; the next one may be valid.
            }
        }
        return history;
    }
}
