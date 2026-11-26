package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Four hands-on activities with deterministic, restorable rounds. */
public final class PlaylumiInteractive {
    public static final int NUMBERS=0, LETTERS=1, HABITATS=2, MOSAIC=3;
    public static final String[] TITLES={"Getallenrace","Lettertrein","Dierenwereld: thuis","Kleurmozaïek"};
    public static final String[] INSTRUCTIONS={"Bouw de raket: tik de cijfers op volgorde.","Bouw de trein: tik de letters van het voorbeeld.","Help ieder dier naar zijn eigen huis.","Bouw het voorbeeld na: kies een kleur voor elk vak."};
    private static final String[][] WORDS={{"VIS","KAT","ZON"},{"BOOM","MAAN","ROOS"},{"STERREN","RAKET","BLOEMEN"}};
    private static final String[] ANIMALS={"🐠","🐄","🐒","🐬","🐑","🦜"};
    private static final String[] HOMES={"🌊 Zee","🌾 Boerderij","🌳 Bos"};
    private static final String[] COLORS={"🔴 Rood","🔵 Blauw","🟡 Geel","🟢 Groen"};
    public final int kind,level;
    public final long seed;
    private final String[] targets,answers;
    private final List<String> options;
    private int progress;
    public PlaylumiInteractive(int kind,int level,long seed,int progress){
        if(kind<0||kind>3||level<0||level>2)throw new IllegalArgumentException("game");
        this.kind=kind;this.level=level;this.seed=seed;
        Random r=new Random(seed);
        ArrayList<String> opts=new ArrayList<>();
        if(kind==NUMBERS){
            int n=3+level;int start=1+(level==0?0:r.nextInt(5));
            targets=new String[n];answers=new String[n];
            for(int i=0;i<n;i++){targets[i]=String.valueOf(start+i);answers[i]=targets[i];opts.add(targets[i]);}
            Collections.shuffle(opts,r);
        }else if(kind==LETTERS){
            String word=WORDS[level][r.nextInt(WORDS[level].length)];
            targets=new String[word.length()];answers=new String[word.length()];
            for(int i=0;i<word.length();i++){targets[i]=word.substring(i,i+1);answers[i]=targets[i];if(!opts.contains(targets[i]))opts.add(targets[i]);}
            if(!opts.contains("X"))opts.add("X");
            Collections.shuffle(opts,r);
        }else if(kind==HABITATS){
            int n=3+level;targets=new String[n];answers=new String[n];
            ArrayList<Integer> indices=new ArrayList<>();
            for(int i=0;i<ANIMALS.length;i++)indices.add(i);
            Collections.shuffle(indices,r);
            for(int i=0;i<n;i++){int index=indices.get(i);targets[i]=ANIMALS[index];answers[i]=HOMES[index%3];}
            Collections.addAll(opts,HOMES);
        }else{
            int n=4+level;targets=new String[n];answers=new String[n];
            for(int i=0;i<n;i++){targets[i]=COLORS[r.nextInt(2+level)];answers[i]=targets[i];}
            for(int i=0;i<2+level;i++)opts.add(COLORS[i]);
        }
        options=Collections.unmodifiableList(opts);
        this.progress=Math.max(0,Math.min(progress,targets.length));
    }
    public int progress(){return progress;}
    public int total(){return targets.length;}
    public boolean solved(){return progress==total();}
    public String target(int i){return targets[i];}
    public List<String> options(){return options;}
    public boolean choose(String value){
        if(solved()||!answers[progress].equals(value))return false;
        progress++;return true;
    }
    public String expected(){return solved()?"":answers[progress];}
}
