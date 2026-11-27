package nl.playlumi.app;

/** Per-route local first-try history. No comparison with other children. */
public final class PlaylumiLearningProfile {
    public static final int HISTORY=12, WINDOW=6;
    private final int max,min;
    public int level,bits,samples,total,window;
    public PlaylumiLearningProfile(int age,int minimum,int level,int bits,int samples,int total,int window){
        max=Math.max(0,Math.min(2,age));min=Math.max(0,Math.min(max,minimum));
        this.level=Math.max(min,Math.min(max,level));
        this.samples=Math.max(0,Math.min(HISTORY,samples));
        this.bits=bits&((1<<this.samples)-1);
        this.total=Math.max(this.samples,total);
        this.window=Math.max(0,Math.min(Math.min(this.samples,WINDOW-1),window));
    }
    /** Keep the approved advanced exercises intact even when more support is needed. */
    public static int minimum(int age,int mode,int variant){
        if(age==2&&mode==PlaylumiReadingPractice.MODE&&variant==PlaylumiReadingPractice.VARIANT)return 2;
        if(age==2&&(mode==LearningGame.PATTERNS||mode==ExtraGames.SEQUENCES||mode==ExtraGames.FIRST_LAST||mode==ExtraGames.RHYMES||mode==ExtraGames.SPELLING||mode==ExtraGames.MATH&&variant>=PlaylumiAdvancedMath.TABLES||mode==22||mode==23||mode==16&&variant==3))return 2;
        return Math.max(0,age-1);
    }
    public void record(boolean firstTry,boolean adapt){
        bits=((bits<<1)|(firstTry?1:0))&((1<<HISTORY)-1);
        samples=Math.min(HISTORY,samples+1);if(total<Integer.MAX_VALUE)total++;
        if(!adapt){window=0;return;}
        if(++window==WINDOW){
            int good=Integer.bitCount(bits&((1<<WINDOW)-1));
            if(good<=2&&level>min)level--;
            else if(good>=5&&level<max)level++;
            window=0;
        }
    }
    public int firstTryPercent(){return samples==0?0:100*Integer.bitCount(bits)/samples;}
    public int priority(boolean played){
        if(samples>=4&&firstTryPercent()<75)return 100-firstTryPercent()+50;
        if(!played)return 40;
        if(samples<4)return 20;
        return 0;
    }
}
