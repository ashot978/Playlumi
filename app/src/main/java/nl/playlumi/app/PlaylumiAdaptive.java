package nl.playlumi.app;

/**
 * Optional, explainable age-aware difficulty adjustments.
 * Every eight completed questions may adjust at most one level.
 */
public final class PlaylumiAdaptive {
    public static final int SAMPLE=8;
    private final int[] levels=new int[PlaylumiScores.CATEGORIES];
    private final int[] attempts=new int[PlaylumiScores.CATEGORIES];
    private final int[] correct=new int[PlaylumiScores.CATEGORIES];
    private final boolean[] initialized=new boolean[PlaylumiScores.CATEGORIES];

    private void check(int category) {
        if(category<0 || category>=PlaylumiScores.CATEGORIES)
            throw new IllegalArgumentException("Unknown learning category");
    }
    public void restore(int category,int level,int sampleCount,int sampleCorrect) {
        check(category);
        if(level<0 || level>2) return;
        levels[category]=level;
        attempts[category]=Math.max(0,Math.min(SAMPLE-1,sampleCount));
        correct[category]=Math.max(0,Math.min(attempts[category],sampleCorrect));
        initialized[category]=true;
    }
    public int level(int category,int age) {
        check(category);
        if(age<0 || age>2)throw new IllegalArgumentException("age");
        if(!initialized[category]) {
            levels[category]=age;
            initialized[category]=true;
        }
        levels[category]=Math.min(levels[category],Math.min(2,age+1));
        return levels[category];
    }
    public int sampleCount(int category) {check(category);return attempts[category];}
    public int sampleCorrect(int category) {check(category);return correct[category];}
    /**
     * Called once per completely answered question.
     * Uses 7/8 to advance and <=2/8 to ease the challenge. All other windows hold level.
     */
    public boolean observe(int category,boolean firstTryCorrect,int age) {
        int previous=level(category,age);
        attempts[category]++;
        if(firstTryCorrect)correct[category]++;
        if(attempts[category]>=SAMPLE) {
            if(correct[category]>=7 && levels[category]<Math.min(2,age+1))
                levels[category]++;
            else if(correct[category]<=2 && levels[category]>0)
                levels[category]--;
            attempts[category]=0;
            correct[category]=0;
        }
        return levels[category]!=previous;
    }
}
