package nl.playlumi.app;

/**
 * A tiny profile-local tune made by a child. Contains no scores or account data.
 * Inputs are four safe animal-notes. At most twelve notes can be stored.
 */
public final class PlaylumiMusicComposer {
    public static final int MAX_NOTES=12;
    private final int[] notes=new int[MAX_NOTES];
    private int size;

    public static PlaylumiMusicComposer restore(String encoded) {
        PlaylumiMusicComposer result=new PlaylumiMusicComposer();
        if(encoded==null || encoded.length()>MAX_NOTES)return result;
        for(int i=0;i<encoded.length();i++) {
            char digit=encoded.charAt(i);
            if(digit<'0' || digit>='0'+PlaylumiMelody.NOTES)
                return new PlaylumiMusicComposer();
            result.notes[result.size++]=digit-'0';
        }
        return result;
    }

    public boolean add(int note) {
        if(note<0 || note>=PlaylumiMelody.NOTES)
            throw new IllegalArgumentException("invalid musical note");
        if(size==MAX_NOTES)return false;
        notes[size++]=note;
        return true;
    }
    public boolean undo() {
        if(size==0)return false;
        size--;
        return true;
    }
    public void clear(){size=0;}
    public int size(){return size;}
    public int[] snapshot(){return java.util.Arrays.copyOf(notes,size);}

    /** Compact stable representation for SharedPreferences in the child profile. */
    public String encode() {
        StringBuilder result=new StringBuilder(size);
        for(int i=0;i<size;i++)result.append((char)('0'+notes[i]));
        return result.toString();
    }
    public String description() {
        if(size==0)return "🎶 Je deuntje is nog leeg. Tik op de dieren!";
        StringBuilder result=new StringBuilder("🎼 ");
        for(int i=0;i<size;i++) {
            if(i>0)result.append(" ");
            result.append(PlaylumiMelody.ANIMALS[notes[i]]);
        }
        return result.append("  (").append(size).append(" / ")
                .append(MAX_NOTES).append(")").toString();
    }
}
