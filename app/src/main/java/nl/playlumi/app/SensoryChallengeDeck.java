package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** 300 offline, shuffled, distinct visible challenges for a single sensory game and level. */
public final class SensoryChallengeDeck<T> {
    public static final int SIZE=300;
    private interface Factory<T> {
        T make(Random random);
        String key(T item);
    }
    private final long seed;
    private final Random random;
    private final Factory<T> factory;
    private final List<T> cards=new ArrayList<>(SIZE);
    private int cursor;

    private SensoryChallengeDeck(long seed,Factory<T> factory){
        this.seed=seed;
        this.random=new Random(seed);
        this.factory=factory;
        refill();
    }

    public static SensoryChallengeDeck<PlaylumiBubbles> bubbles(int level,long seed){
        if(level<0 || level>2)throw new IllegalArgumentException("bubble level");
        return new SensoryChallengeDeck<>(seed,new Factory<PlaylumiBubbles>(){
            public PlaylumiBubbles make(Random random){
                return PlaylumiBubbles.generate(level,random);
            }
            public String key(PlaylumiBubbles b){return bubbleKey(b);}
        });
    }
    public static SensoryChallengeDeck<PlaylumiMelody> melodies(int level,long seed){
        if(level<0 || level>2)throw new IllegalArgumentException("melody level");
        return new SensoryChallengeDeck<>(seed,new Factory<PlaylumiMelody>(){
            public PlaylumiMelody make(Random random){
                return PlaylumiMelody.generate(level,random);
            }
            public String key(PlaylumiMelody m){return melodyKey(m);}
        });
    }

    /** Identity is based on actual bubble colors and the counting target. */
    public static String bubbleKey(PlaylumiBubbles b){
        StringBuilder s=new StringBuilder();
        s.append(b.level).append(':').append(b.target).append(':')
                .append(b.total).append(':');
        for(int i=0;i<b.total;i++)s.append((char)('0'+b.paletteIndex(i)));
        return s.toString();
    }
    /** Melody cards vary the short tune AND the location of the four animal buttons. */
    public static String melodyKey(PlaylumiMelody m){
        StringBuilder s=new StringBuilder().append(m.length()).append(':');
        for(int n:m.sequence())s.append((char)('0'+n));
        s.append(':');
        for(int n:m.buttonOrder())s.append((char)('0'+n));
        return s.toString();
    }

    private void refill(){
        cards.clear();
        cursor=0;
        Set<String> seen=new HashSet<>(SIZE*2);
        for(int tries=0;tries<30000 && cards.size()<SIZE;tries++){
            T item=factory.make(random);
            if(seen.add(factory.key(item)))cards.add(item);
        }
        if(cards.size()!=SIZE)throw new IllegalStateException("Need 300 distinct visual tasks");
        Collections.shuffle(cards,random);
    }
    public int size(){return cards.size();}
    public int remaining(){return SIZE-cursor;}
    public int dealt(){return cursor;}
    public long seed(){return seed;}
    public T next(){
        if(cursor==SIZE)refill();
        return cards.get(cursor++);
    }
    /** Restore the exact deck cursor after rotation using the saved random seed. */
    public void skip(int count){
        if(count<0 || count>SIZE)throw new IllegalArgumentException("cursor");
        for(int i=0;i<count;i++)next();
    }
}
