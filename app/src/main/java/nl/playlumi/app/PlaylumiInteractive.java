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
    public static final int LEGACY=1,CURRENT=2;
    public final int kind,level,version;
    public final long seed;
    private String clue="",picture="";
    private int step=1;
    private boolean wordPuzzle;
    private final String[] targets,answers;
    private final List<String> options;
    private int progress;
    /** The four-argument constructor is intentionally the old deterministic generator. */
    public PlaylumiInteractive(int kind,int level,long seed,int progress){
        this(kind,level,seed,progress,LEGACY);
    }
    public PlaylumiInteractive(int kind,int level,long seed,int progress,int version){
        if(kind<0||kind>3||level<0||level>2||(version!=LEGACY&&version!=CURRENT))throw new IllegalArgumentException("game");
        this.kind=kind;this.level=level;this.seed=seed;this.version=version;
        Random r=new Random(seed);
        ArrayList<String> opts=new ArrayList<>();
        if(kind==NUMBERS){
            if(version==CURRENT&&level>0){
                int n=level==1?4:5;
                step=level==1?1:2+r.nextInt(2);
                if(level==2&&r.nextBoolean())step=-step;
                int low=1+r.nextInt(level==1?7:20-(n-1)*Math.abs(step));
                int start=step<0?low+(n-1)*Math.abs(step):low;
                targets=new String[n];answers=new String[n];
                for(int i=0;i<n;i++){targets[i]=String.valueOf(start+i*step);answers[i]=targets[i];opts.add(targets[i]);}
                ArrayList<String> decoys=new ArrayList<>();
                for(int v=1;v<=20;v++)if(!opts.contains(""+v))decoys.add(""+v);
                Collections.shuffle(decoys,r);
                for(int j=0;j<(level==1?1:2);j++)opts.add(decoys.get(j));
            }else{
                int n=3+level;int start=1+(level==0?0:r.nextInt(5));
                targets=new String[n];answers=new String[n];
                for(int i=0;i<n;i++){targets[i]=String.valueOf(start+i);answers[i]=targets[i];opts.add(targets[i]);}
            }
            Collections.shuffle(opts,r);
        }else if(kind==LETTERS){
            String word;
            if(version==CURRENT&&level>0){
                String[][] bank=level==1?PlaylumiWordTrain.MIDDLE:PlaylumiWordTrain.OLDER;
                String[] item=bank[r.nextInt(bank.length)];
                word=item[0];picture=item[1];clue=item[2];wordPuzzle=true;
            }else word=WORDS[level][r.nextInt(WORDS[level].length)];
            targets=new String[word.length()];answers=new String[word.length()];
            for(int i=0;i<word.length();i++){targets[i]=word.substring(i,i+1);answers[i]=targets[i];if(!opts.contains(targets[i]))opts.add(targets[i]);}
            if(wordPuzzle){
                ArrayList<String> decoys=new ArrayList<>();
                for(char letter='A';letter<='Z';letter++)if(!opts.contains(""+letter))decoys.add(""+letter);
                Collections.shuffle(decoys,r);
                for(int j=0;j<(level==1?1:2);j++)opts.add(decoys.get(j));
            }else if(!opts.contains("X"))opts.add("X");
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
    public static PlaylumiInteractive start(int kind,int level,long seed){
        return new PlaylumiInteractive(kind,level,seed,0,CURRENT);
    }
    public boolean wordPuzzle(){return wordPuzzle;}
    public String picture(){return picture;}
    public String clue(){return clue;}
    public int step(){return step;}
    public String instruction(){
        if(kind==NUMBERS){
            if(version==CURRENT&&level>0)return "Begin bij "+targets[0]+". Tel steeds "+Math.abs(step)+(step<0?" terug.":" verder.");
            return INSTRUCTIONS[kind]+" Begin bij "+targets[0]+".";
        }
        if(wordPuzzle)return "Bouw het woord. "+clue;
        return INSTRUCTIONS[kind];
    }
    public String example(int index,boolean revealed){
        if(!wordPuzzle||revealed||solved())return targets[index];
        // Young readers see the first and last letter; older readers get the first.
        return index==0||(level==1&&index==total()-1)?targets[index]:"?";
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
