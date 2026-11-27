package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Seven independent, age-scaled exercises. Legacy quiz variants are unchanged. */
public final class PlaylumiSkillPractice {
    public static final int VARIANT=6;
    private static final String[] SHAPES={"●","▲","■","◆","▭"};
    private static final String[] SHAPE_NAMES={"cirkel","driehoek","vierkant","ruit","rechthoek"};
    private static final int[] CORNERS={0,3,4,4,4};
    private static final String[] NOTES={"♪","♫","♩"};
    private static final String[][] PLACES={
        {"IJsblokjes","Waar blijven ijsblokjes bevroren?","Vriezer"},
        {"Vieze kleding","Waar was je vuile kleding?","Wasmachine"},
        {"Tandenborstel","Waar poets je je tanden bij de wastafel?","Badkamer"},
        {"Brief","Waar doe je een brief om hem te versturen?","Brievenbus"},
        {"Slapen","Waar lig je als je in bed slaapt?","Slaapkamer"},
        {"Boeken lenen","Waar leen je boeken?","Bibliotheek"},
        {"Trein nemen","Waar stap je op de trein?","Station"},
        {"Brood kopen","Waar bakt en verkoopt de bakker brood?","Bakkerij"},
        {"Zwemles","Waar zwem je tijdens zwemles?","Zwembad"},
        {"Voetbalwedstrijd","Waar speel je een voetbalwedstrijd op gras?","Voetbalveld"},
        {"Vuilnis","Waar gooi je afval weg?","Vuilnisbak"},
        {"Voedsel koel bewaren","Waar blijft melk koud in huis?","Koelkast"},
        {"Film kijken","Waar kijk je een film op een groot bioscoopscherm?","Bioscoop"},
        {"Schoenen","Waar repareert de schoenmaker schoenen?","Schoenmakerij"},
        {"Bloemen","Waar koop je een boeket bij de bloemist?","Bloemenwinkel"},
        {"Schoolspullen","Waar zit je tijdens een les op school?","Klaslokaal"}
    };
    private PlaylumiSkillPractice(){}
    public static boolean supportsMode(int mode){
        return mode==LearningGame.COUNT||mode==LearningGame.NUMBERS||mode==LearningGame.LETTERS||
                mode==LearningGame.SHAPES||mode==LearningGame.PATTERNS||mode==19||mode==LearningGame.COLORS;
    }
    public static boolean supports(int mode,int variant){
        // Variant 5 is already used by counting and daily things. On other
        // skill modes it is a safe alias for internal maxVariant enumeration.
        return supportsMode(mode)&&(variant==VARIANT||variant==5&&mode!=LearningGame.COUNT&&mode!=19);
    }
    public static boolean offered(int age,int mode){
        return supportsMode(mode)&&(age>=1||mode==LearningGame.COUNT||mode==LearningGame.SHAPES||mode==LearningGame.PATTERNS);
    }
    public static boolean revealsBlank(int mode,int variant){
        return supports(mode,variant)&&(mode==LearningGame.NUMBERS||mode==LearningGame.LETTERS||mode==LearningGame.PATTERNS);
    }
    public static boolean colorChoices(int mode,int variant){return mode==LearningGame.COLORS&&supports(mode,variant);}
    private static LearningGame.Round round(String prompt,String display,String correct,List<String> choices,List<Integer> paints){
        return new LearningGame.Round(prompt,display,0xff3d195f,choices,paints,choices.indexOf(correct));
    }
    private static LearningGame.Round options(String prompt,String display,String right,List<String> pool,int count,Random r){
        ArrayList<String> others=new ArrayList<>(pool);others.remove(right);Collections.shuffle(others,r);
        ArrayList<String> choices=new ArrayList<>();choices.add(right);
        for(String candidate:others)if(!choices.contains(candidate)&&choices.size()<count)choices.add(candidate);
        if(choices.size()!=count)throw new IllegalStateException("Insufficient independent choices");
        Collections.shuffle(choices,r);ArrayList<Integer> inks=new ArrayList<>();
        for(String ignored:choices)inks.add(0xff3d195f);
        return round(prompt,display,right,choices,inks);
    }
    private static List<String> numbers(int min,int max){
        ArrayList<String> values=new ArrayList<>();for(int i=min;i<=max;i++)values.add(""+i);return values;
    }
    private static String repeated(String value,int count){
        StringBuilder text=new StringBuilder();for(int i=0;i<count;i++){if(i>0)text.append("  ");text.append(value);}return text.toString();
    }
    public static LearningGame.Round next(int mode,int variant,int level,Random r){
        if(!supports(mode,variant)||level<0||level>2)throw new IllegalArgumentException("Skill exercise");
        switch(mode){
            case LearningGame.COUNT:{
                int n=1+r.nextInt(level==0?4:level==1?9:29),step=r.nextBoolean()?1:-1;
                String prompt=step==1?"Hoeveel zijn het met eentje erbij?":"Hoeveel blijven er over als er eentje weggaat?";
                String display=level==0?repeated("●",n):""+n;
                return options(prompt,display,""+(n+step),numbers(0,level==0?5:level==1?10:30),level==0?3:4,r);
            }
            case LearningGame.NUMBERS:{
                int max=level==2?100:20,n=2+r.nextInt(max-2);
                return options("Welk getal staat tussen deze twee?",""+(n-1)+"  ?  "+(n+1),""+n,numbers(Math.max(1,n-3),Math.min(max,n+3)),4,r);
            }
            case LearningGame.LETTERS:{
                int limit=level==0?10:level==1?18:26,n=r.nextInt(limit-1);
                ArrayList<String> pool=new ArrayList<>();for(int i=0;i<limit;i++)pool.add(""+(char)('a'+i));
                return options("Welke letter komt hierna?",""+(char)('a'+n)+"  →  ?",""+(char)('a'+n+1),pool,level==2?4:3,r);
            }
            case LearningGame.SHAPES:{
                int index=r.nextInt(level==0?3:SHAPES.length);
                List<String> pool=level==0?java.util.Arrays.asList("0","3","4"):numbers(0,5);
                return options("Hoeveel hoeken heeft deze vorm?",SHAPES[index],""+CORNERS[index],pool,level==0?3:4,r);
            }
            case LearningGame.PATTERNS:{
                int a=r.nextInt(NOTES.length),b=(a+1+r.nextInt(2))%NOTES.length,c=3-a-b;
                int[] motif=level==0?new int[]{a,b}:level==1?new int[]{a,a,b}:r.nextBoolean()?new int[]{a,b,c}:new int[]{a,b,b,c};
                StringBuilder display=new StringBuilder();
                for(int i=0;i<2*motif.length;i++){if(i>0)display.append(" ");display.append(NOTES[motif[i%motif.length]]);}
                display.append("  ?");
                return options("Welke muzieknoot hoort hierna?",display.toString(),NOTES[motif[0]],java.util.Arrays.asList(NOTES),3,r);
            }
            case 19:{
                int i=r.nextInt(level==0?6:level==1?12:PLACES.length);
                ArrayList<String> pool=new ArrayList<>();for(String[] place:PLACES)pool.add(place[2]);
                return options(PLACES[i][1],PLACES[i][0],PLACES[i][2],pool,level==2?4:3,r);
            }
            case LearningGame.COLORS:{
                int limit=level==0?3:level==1?5:6,index=r.nextInt(limit);
                ArrayList<Integer> indices=new ArrayList<>();indices.add(index);
                ArrayList<Integer> others=new ArrayList<>();for(int i=0;i<limit;i++)if(i!=index)others.add(i);
                Collections.shuffle(others,r);for(int i=0;i<(level==0?2:level==1?3:4);i++)indices.add(others.get(i));
                Collections.shuffle(indices,r);ArrayList<String> choices=new ArrayList<>();ArrayList<Integer> inks=new ArrayList<>();
                for(int i:indices){choices.add(LearningGame.COLOR_NAMES[i]);inks.add(LearningGame.COLOR_VALUES[i]);}
                return round("Tik op de kleur bij dit woord",LearningGame.COLOR_NAMES[index],LearningGame.COLOR_NAMES[index],choices,inks);
            }
            default:throw new IllegalArgumentException("Unsupported skill");
        }
    }
    public static String explain(int mode,int variant,LearningGame.Round q){
        if(!supports(mode,variant)||!q.isSolved())return "";
        String right=q.answers.get(q.correctIndex);
        switch(mode){
            case LearningGame.COUNT:{
                int n=q.display.contains("●")?q.display.split("●",-1).length-1:Integer.parseInt(q.display);
                return n+(q.prompt.contains("erbij")?" + 1 = ":" − 1 = ")+right;
            }
            case LearningGame.NUMBERS:return q.display.replace("?",right);
            case LearningGame.LETTERS:return q.display.replace("?",right);
            case LearningGame.SHAPES:
                for(int i=0;i<SHAPES.length;i++)if(SHAPES[i].equals(q.display))
                    return "Een "+SHAPE_NAMES[i]+(CORNERS[i]==0?" heeft geen hoeken.":" heeft "+right+" hoeken.");
                return "Dit zijn "+right+" hoeken.";
            case LearningGame.PATTERNS:return "De noten herhalen zich: "+right+" komt hierna.";
            case 19:return "Dat doe je bij: "+right.toLowerCase(java.util.Locale.ROOT)+".";
            case LearningGame.COLORS:return "Goed! Dit is "+right+".";
            default:return "";
        }
    }
}
