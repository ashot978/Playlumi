package nl.playlumi.app;

/**
 * Optional, child-safe multiple-choice hints. The correct answer can never be
 * removed, and a hint always leaves at least two choices for the child to try.
 * Stateless Java math keeps answer/scoring logic unchanged.
 */
public final class PlaylumiHints {
    private PlaylumiHints() {}

    /**
     * @return the index of one incorrect visible answer to replace with a
     * cloud, or -1 if fewer than three answers remain.
     */
    public static int nextWrongOption(int correctIndex,int optionCount,int hiddenMask) {
        // The current game has at most ten options; limit masks to safe int shifts.
        if(optionCount<2 || optionCount>30 ||
                correctIndex<0 || correctIndex>=optionCount)
            throw new IllegalArgumentException("answers");
        int legalMask=(1<<optionCount)-1;
        if((hiddenMask&~legalMask)!=0 || (hiddenMask&(1<<correctIndex))!=0)
            throw new IllegalArgumentException("hidden answers");
        if(optionCount-Integer.bitCount(hiddenMask)<=2)return -1;
        for(int delta=1;delta<optionCount;delta++) {
            int choice=(correctIndex+delta)%optionCount;
            if((hiddenMask&(1<<choice))==0)return choice;
        }
        throw new IllegalStateException("missing incorrect answer");
    }
}
