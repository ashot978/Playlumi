package nl.playlumi.app;
import java.util.*;

/** Versioned, deterministic board games with exact saved cell selections. */
public final class PlaylumiPuzzle {
    public static final int NUMBER_HUNT=4,WORD_PATH=5,MAZE=6,RHYTHM=7,SORT=8,PIXEL=9,SUMS=10,SYMMETRY=11,MAP=12,VERSION=4,LAST=MAP;
    private static final String[] TITLES={"Getallenjacht","Woordzoeker","Sterrendoolhof","Ritmegeheugen","Sorteerfeest","Pixelpuzzel","Rekenjacht","Spiegelpuzzel","Schatkaart"};
    private static final String[] ICONS={"🔢","🔎","🏰","♫","🍎","▦","➕","◧","🗺️"};
    private static final String[] DESCRIPTIONS={"Zoek getallen op het speelbord in volgorde","Vind woorden via aangrenzende letters","Volg het doolhofpad naar de schat","Onthoud en speel een reeks dierengeluiden na","Verzamel alle voorwerpen die bij een groep horen","Herstel de verschillen tussen twee pixelplaten","Los sommen op en vind het antwoord op het bord","Herstel het spiegelbeeld","Vind schatten met letters en cijfers op een kaart"};
    private static final String[][] WORDS={{"KAT","VIS","ZON","MUS","PEN","UIL"},{"BOEK","VORK","TUIN","STER","FLES","MELK"},{"FIETS","PLANT","RAKET","SCHIP","WOLK","BLOEM","FRUIT","KONIJN"}};
    private static final String[][] GROUPS={{"🍎","🍌","🍇","🍓","🍐","🍊","🍉","🍒"},{"🐶","🐱","🐭","🐰","🐸","🐝","🦋","🐢"},{"🚗","🚌","🚲","🚂","🚁","🚀","🚢","🚕"},{"👕","👖","🧦","🧢","🧤","👗","👢","🧣"}};
    private static final String[] GROUP_NAMES={"fruit","dieren","voertuigen","kleding"};
    private static final String[] NOTES={"🐸","🐱","🐥","🦋"},PIXELS={"●","▲","■","◆","✿"};
    private static final int[] PAINTS={0xfff09ea8,0xff8fc6f2,0xffffd477,0xffa4dab5,0xffc7a6eb};
    public final int kind,level,cols,rows;public final long seed;
    private final String[] board,reference,questions;private final int[] order;
    private final boolean unordered;private final int initial,goal,allowedMask,step;private final String topic;
    private int progress,mask;
    public static boolean supported(int kind){return kind>=4&&kind<=LAST;}
    public static boolean offered(int kind,int age){return supported(kind)&&age>=0&&age<=2&&(kind<SUMS||kind<=SYMMETRY&&age>=1||kind==MAP&&age==2);}
    public static String title(int k){return TITLES[k-4];}public static String icon(int k){return ICONS[k-4];}public static String description(int k){return DESCRIPTIONS[k-4];}
    public static int world(int k){switch(k){case NUMBER_HUNT:case SUMS:return 0;case WORD_PATH:return 1;case MAZE:case SYMMETRY:case MAP:return 2;case RHYTHM:return 3;case SORT:return 4;case PIXEL:return 5;default:throw new IllegalArgumentException("puzzle");}}
    public static int scoreCategory(int k){switch(k){case NUMBER_HUNT:return LearningGame.NUMBERS;case WORD_PATH:return LearningGame.WORDS;case MAZE:case MAP:return 18;case RHYTHM:case SYMMETRY:return LearningGame.PATTERNS;case SORT:return ExtraGames.CATEGORIES;case PIXEL:return LearningGame.COLORS;case SUMS:return ExtraGames.MATH;default:throw new IllegalArgumentException("puzzle");}}
    public static PlaylumiPuzzle start(int kind,int age,long seed){return new PlaylumiPuzzle(kind,age,seed,0,0);}
    private static ArrayList<Integer> positions(int n,Random r){ArrayList<Integer> a=new ArrayList<>();for(int i=0;i<n;i++)a.add(i);Collections.shuffle(a,r);return a;}
    private static int[] first(List<Integer> a,int n){int[] b=new int[n];for(int i=0;i<n;i++)b[i]=a.get(i);return b;}
    private static int indexOf(String[] a,String s){for(int i=0;i<a.length;i++)if(s.equals(a[i]))return i;throw new IllegalStateException("missing");}
    private static void shuffle(String[] a,Random r){Collections.shuffle(Arrays.asList(a),r);}
    private static int transform(int y,int x,int n,int f){return ((f&1)==0?y:n-1-y)*n+((f&2)==0?x:n-1-x);}
    public PlaylumiPuzzle(int kind,int level,long seed,int count,int savedMask){
        if(!offered(kind,level))throw new IllegalArgumentException("puzzle age or kind");
        this.kind=kind;this.level=level;this.seed=seed;Random r=new Random(seed);
        String[] b,ref=null,qs=null;int[] sequence;boolean any=false;int start=-1,end=-1,inc=1;String subject="";
        int w=level==0?3:4,h=level==0?3:level==1?3:4;
        switch(kind){
            case NUMBER_HUNT:{
                int n=level==0?5:level==1?7:9;inc=level==2?2+r.nextInt(2):1;
                int v=level==0?1:1+r.nextInt(level==1?5:9);b=new String[w*h];String[] wanted=new String[n];
                for(int i=0;i<n;i++)b[i]=wanted[i]=""+(v+i*inc);
                for(int i=n;i<b.length;i++)b[i]=""+(v+n*inc+i-n);
                shuffle(b,r);sequence=new int[n];for(int i=0;i<n;i++)sequence[i]=indexOf(b,wanted[i]);subject=wanted[0];break;
            }
            case WORD_PATH:{
                String word=WORDS[level][r.nextInt(WORDS[level].length)];subject=word;b=new String[w*h];ArrayList<Integer> snake=new ArrayList<>();int flip=r.nextInt(4);
                for(int y=0;y<h;y++)for(int x=0;x<w;x++){int col=y%2==0?x:w-1-x;int yy=(flip&1)==0?y:h-1-y,xx=(flip&2)==0?col:w-1-col;snake.add(yy*w+xx);}
                if(r.nextBoolean())Collections.reverse(snake);int offset=r.nextInt(snake.size()-word.length()+1);
                sequence=new int[word.length()];for(int i=0;i<sequence.length;i++){sequence[i]=snake.get(offset+i);b[sequence[i]]=word.substring(i,i+1);}
                for(int i=0;i<b.length;i++)if(b[i]==null){char c;do{c=(char)('A'+r.nextInt(26));}while(word.indexOf(c)>=0);b[i]=""+c;}break;
            }
            case MAZE:{
                w=h=level+3;b=new String[w*h];Arrays.fill(b,"#");int y=0,x=0,f=r.nextInt(4),i=0;
                start=transform(0,0,w,f);b[start]="·";sequence=new int[2*(w-1)];
                while(y<w-1||x<w-1){if(y==w-1||x<w-1&&r.nextBoolean())x++;else y++;int p=transform(y,x,w,f);sequence[i++]=p;b[p]="·";}end=sequence[sequence.length-1];break;
            }
            case RHYTHM:{
                w=h=2;b=NOTES.clone();sequence=new int[3+2*level];ref=new String[sequence.length];
                for(int i=0;i<sequence.length;i++){sequence[i]=r.nextInt(4);ref[i]=b[sequence[i]];}break;
            }
            case SORT:{
                any=true;b=new String[w*h];int group=r.nextInt(GROUPS.length),n=level==0?4:level==1?5:7;subject=GROUP_NAMES[group];
                ArrayList<String> chosen=new ArrayList<>(Arrays.asList(GROUPS[group]));Collections.shuffle(chosen,r);
                ArrayList<String> decoys=new ArrayList<>();for(int i=0;i<GROUPS.length;i++)if(i!=group)Collections.addAll(decoys,GROUPS[i]);Collections.shuffle(decoys,r);
                for(int i=0;i<n;i++)b[i]=chosen.get(i);for(int i=n;i<b.length;i++)b[i]=decoys.get(i-n);
                shuffle(b,r);sequence=new int[n];for(int i=0;i<n;i++)sequence[i]=indexOf(b,chosen.get(i));break;
            }
            case PIXEL:{
                any=true;w=level==2?4:3;h=level==0?2:3;b=new String[w*h];ref=new String[b.length];int palette=level+3;
                for(int i=0;i<b.length;i++)b[i]=ref[i]=PIXELS[r.nextInt(palette)];sequence=first(positions(b.length,r),level==0?2:level==1?3:5);
                for(int pos:sequence){String s;do{s=PIXELS[r.nextInt(palette)];}while(s.equals(ref[pos]));b[pos]=s;}break;
            }
            case SUMS:{
                int n=level==1?4:6;b=new String[w*h];ArrayList<Integer> v=positions(level==1?18:32,r);
                for(int i=0;i<b.length;i++)b[i]=""+(v.get(i)+1);sequence=first(positions(b.length,r),n);qs=new String[n];
                for(int i=0;i<n;i++){int answer=Integer.parseInt(b[sequence[i]]);if(answer>1&&r.nextBoolean()){int a=1+r.nextInt(answer-1);qs[i]=a+" + "+(answer-a)+" = ?";}else{int a=1+r.nextInt(level==1?7:18);qs[i]=(answer+a)+" − "+a+" = ?";}}break;
            }
            case SYMMETRY:{
                any=true;w=4;h=level==1?3:4;b=new String[w*h];ref=new String[b.length];
                for(int y=0;y<h;y++)for(int x=0;x<w/2;x++){String s=PIXELS[r.nextInt(level+3)];int i=y*w+x,j=y*w+w-1-x;b[i]=ref[i]=b[j]=ref[j]=s;}
                ArrayList<Integer> right=new ArrayList<>();for(int y=0;y<h;y++)for(int x=w/2;x<w;x++)right.add(y*w+x);Collections.shuffle(right,r);sequence=first(right,level==1?3:5);
                for(int pos:sequence){String s;do{s=PIXELS[r.nextInt(level+3)];}while(s.equals(ref[pos]));b[pos]=s;}break;
            }
            case MAP:{w=h=4;b=new String[w*h];Arrays.fill(b,"·");sequence=first(positions(b.length,r),5);break;}
            default:throw new IllegalArgumentException("puzzle");
        }
        cols=w;rows=h;board=b;reference=ref;questions=qs;order=sequence;unordered=any;initial=start;goal=end;topic=subject;step=inc;
        int valid=0;if(kind!=RHYTHM)for(int pos:order)valid|=1<<pos;allowedMask=valid;
        if(count<0||count>total()||savedMask<0)throw new IllegalArgumentException("puzzle count");
        if(unordered){if((savedMask&~allowedMask)!=0||Integer.bitCount(savedMask)!=count)throw new IllegalArgumentException("puzzle selection");}
        else{int prefix=0;if(kind!=RHYTHM)for(int i=0;i<count;i++)prefix|=1<<order[i];if(savedMask!=prefix)throw new IllegalArgumentException("puzzle path");}
        progress=count;mask=savedMask;
    }
    public int total(){return order.length;}public int progress(){return progress;}public int mask(){return mask;}public boolean solved(){return progress==total();}
    public int cells(){return board.length;}public boolean unordered(){return unordered;}public int startCell(){return initial;}public int goalCell(){return goal;}
    public boolean selected(int i){return i>=0&&i<cells()&&(mask&(1<<i))!=0;}
    public int currentCell(){return kind==MAZE?(progress==0?initial:order[progress-1]):-1;}
    public String topic(){return topic;}public String coordinate(int i){return ""+(char)('A'+i%cols)+(1+i/cols);}
    public String original(int i){return board[i];}public String reference(int i){return reference==null?"":reference[i];}public int referenceCount(){return reference==null?0:reference.length;}
    public String display(int i){
        if(kind==MAZE){if(i==currentCell())return solved()?"🏆":"🚀";if(i==goal)return "⭐";return "#".equals(board[i])?"▧":selected(i)||i==initial?"✓":"·";}
        if(kind==MAP)return selected(i)?"💎":"·";if((kind==PIXEL||kind==SYMMETRY)&&selected(i))return reference[i];return board[i];
    }
    public boolean blocked(int i){return kind==MAZE&&"#".equals(board[i])||kind==SYMMETRY&&i%cols<cols/2;}
    public static int paint(String s){for(int i=0;i<PIXELS.length;i++)if(PIXELS[i].equals(s))return PAINTS[i];return 0xfff6e8ff;}
    public boolean accepts(int i){return !solved()&&i>=0&&i<cells()&&(unordered?(allowedMask&(1<<i))!=0&&!selected(i):i==order[progress]);}
    public int expected(){if(solved())return -1;if(!unordered)return order[progress];for(int i:order)if(!selected(i))return i;return -1;}
    public boolean tap(int i){if(!accepts(i))return false;if(kind!=RHYTHM)mask|=1<<i;progress++;return true;}
    public String instruction(){switch(kind){
        case NUMBER_HUNT:return "Begin bij "+topic+". Tel steeds "+step+" verder.";
        case WORD_PATH:return "Zoek "+topic+". Tik de letters naast elkaar.";
        case MAZE:return "Help de raket over het pad naar de ster. Tik een aangrenzend vak.";
        case RHYTHM:return "Bekijk of beluister de reeks. Speel de dieren daarna in dezelfde volgorde.";
        case SORT:return "Verzamel alle "+topic+". Kies zelf de volgorde.";
        case PIXEL:return "Tik elk vak dat anders is dan het voorbeeld.";
        case SUMS:return "Los de som op. Zoek het antwoord op het bord.";
        case SYMMETRY:return "Vergelijk links en rechts. Herstel de verkeerde vakjes rechts.";
        case MAP:return "Lees de letter en het cijfer. Vind dat vak op de schatkaart.";
        default:return "";}}
    public String prompt(){if(solved())return "Alles gelukt!";switch(kind){
        case NUMBER_HUNT:return progress==0?"Start bij "+topic:"Tel "+step+" verder vanaf "+board[order[progress-1]];
        case WORD_PATH:return topic.substring(0,progress)+"  "+topic.substring(progress);
        case MAZE:return "Op weg naar de schat!";
        case RHYTHM:return "Speel toon "+(progress+1)+" van "+total();
        case SORT:return "Nog "+(total()-progress)+" "+topic+" zoeken";
        case PIXEL:case SYMMETRY:return "Nog "+(total()-progress)+" vakjes herstellen";
        case SUMS:return questions[progress];case MAP:return "Zoek schat "+coordinate(order[progress]);default:return "";}}
    public String hint(){switch(kind){
        case MAZE:return "Kies een vak naast de raket op het pad.";
        case PIXEL:return "Vergelijk dezelfde plek in beide platen.";
        case SYMMETRY:return "Kijk naar het vak aan de overkant van de spiegel.";
        case WORD_PATH:return "Kijk naar de volgende letter van het woord.";
        case SORT:return "Kies iets dat bij "+topic+" hoort.";
        case RHYTHM:return "Bekijk of beluister de reeks opnieuw.";
        case SUMS:return "Reken rustig uit en probeer een ander vak.";
        case MAP:return "De letters staan bovenaan, de cijfers links.";
        default:return "Tel rustig verder en probeer opnieuw.";}}
    public String rhythmTrail(){if(kind!=RHYTHM)return "";StringBuilder s=new StringBuilder();for(int i=0;i<total();i++){if(i>0)s.append("  ");s.append(i<progress?reference[i]:"○");}return s.toString();}
}
