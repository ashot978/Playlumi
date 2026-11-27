package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * An offline construction game, independent of the quiz and pixel puzzle.
 * All published v1 blueprints and board encodings are stable; the three free
 * plots and each blueprint have their own bounded, persistent edit history.
 */
public final class PlaylumiBuilder {
    public static final int PLANS=12,PLOTS=3,FREE=12,HISTORY_LIMIT=16,PLAN_REWARD=6,FINAL_REWARD=18;
    public static final char EMPTY='.',HOUSE='H',STATION='S',TREE='T',FLOWER='F',WATER='W',
        ROAD_H='a',ROAD_V='b',ROAD_NE='c',ROAD_ES='d',ROAD_SW='e',ROAD_WN='f',BRIDGE_H='g',BRIDGE_V='h';
    private static final String VALID=".HSTFWabcdefgh";
    private static final char[] PALETTE={HOUSE,STATION,ROAD_H,ROAD_NE,TREE,FLOWER,WATER,BRIDGE_H,EMPTY};
    private static final String[] TITLES={
        "Lumi's eerste straat","Het bloemenpad","De bostuin","Het kleine station",
        "Het bochtenpark","De vijverroute","De groene wijk","Het tuinfeest",
        "De bruggenroute","Het eilanddorp","Het sterrenpark","De droomstad"
    };
    public static final String[] CHAPTERS={"Startbouwer","Parkontwerper","Meesterbouwer"};
    private static final int N=1,E=2,S=4,W=8;
    private static final int[] DY={-1,0,1,0},DX={0,1,0,-1},PORTS={N,E,S,W};
    private PlaylumiBuilder(){}
    public static boolean validSlot(int slot){return slot>=0&&slot<PLANS+PLOTS;}
    public static boolean free(int slot){return slot>=FREE&&slot<FREE+PLOTS;}
    private static void validate(int age,int slot){if(age<0||age>2||!validSlot(slot))throw new IllegalArgumentException("builder plot");}
    public static int size(int age,int slot){validate(age,slot);return (free(slot)?4:3)+age;}
    public static String title(int slot){
        if(!validSlot(slot))throw new IllegalArgumentException("builder plot");
        return free(slot)?"Eigen ontwerp "+(slot-FREE+1):TITLES[slot];
    }
    public static String key(int age,int slot){validate(age,slot);return "builder_v1_board_"+age+"_"+slot;}
    public static String progressKey(int age){validate(age,0);return "builder_v1_progress_"+age;}
    public static int safe(int done){return Math.max(0,Math.min(PLANS,done));}
    public static boolean available(int done,int slot){return validSlot(slot)&&(free(slot)||slot<=safe(done));}
    public static int totalReward(){return PLANS*PLAN_REWARD+FINAL_REWARD;}
    public static int rewardTotal(int done){return safe(done)*PLAN_REWARD+(safe(done)==PLANS?FINAL_REWARD:0);}
    public static char[] palette(){return PALETTE.clone();}
    public static boolean validPiece(char piece){return VALID.indexOf(piece)>=0;}
    public static boolean rotatable(char piece){return piece>='a'&&piece<='h';}
    public static char rotate(char piece){
        switch(piece){case ROAD_H:return ROAD_V;case ROAD_V:return ROAD_H;
            case ROAD_NE:return ROAD_ES;case ROAD_ES:return ROAD_SW;case ROAD_SW:return ROAD_WN;case ROAD_WN:return ROAD_NE;
            case BRIDGE_H:return BRIDGE_V;case BRIDGE_V:return BRIDGE_H;default:return piece;}
    }
    public static String name(char piece){
        switch(piece){
            case EMPTY:return "Leeg gras";case HOUSE:return "Huis";case STATION:return "Station";
            case TREE:return "Boom";case FLOWER:return "Bloemen";case WATER:return "Water";
            case ROAD_H:return "Weg horizontaal";case ROAD_V:return "Weg verticaal";
            case ROAD_NE:return "Bocht boven en rechts";case ROAD_ES:return "Bocht rechts en onder";
            case ROAD_SW:return "Bocht onder en links";case ROAD_WN:return "Bocht links en boven";
            case BRIDGE_H:return "Brug horizontaal";case BRIDGE_V:return "Brug verticaal";
            default:throw new IllegalArgumentException("piece");
        }
    }
    public static String icon(char piece){
        switch(piece){
            case EMPTY:return "·";case HOUSE:return "🏠";case STATION:return "🚉";case TREE:return "🌳";case FLOWER:return "🌷";case WATER:return "≈";
            case ROAD_H:return "━";case ROAD_V:return "┃";case ROAD_NE:return "┗";case ROAD_ES:return "┏";case ROAD_SW:return "┓";case ROAD_WN:return "┛";case BRIDGE_H:return "═";case BRIDGE_V:return "║";default:return "?";
        }
    }
    public static int color(char piece){
        if(piece==WATER||piece==BRIDGE_H||piece==BRIDGE_V)return 0xff9ed5f6;
        if(piece>=ROAD_H&&piece<=ROAD_WN)return 0xffd0c4dd;
        if(piece==HOUSE)return 0xffffe0b0;
        if(piece==STATION)return 0xffe6d3ff;
        if(piece==FLOWER)return 0xfff9d5e5;
        return piece==TREE?0xffa4d5a5:0xffe4f2d4;
    }
    public static int ports(char piece){
        switch(piece){
            case HOUSE:case STATION:return 15;
            case ROAD_H:case BRIDGE_H:return E|W;case ROAD_V:case BRIDGE_V:return N|S;
            case ROAD_NE:return N|E;case ROAD_ES:return E|S;case ROAD_SW:return S|W;case ROAD_WN:return W|N;
            default:return 0;
        }
    }
    private static char road(int ports,boolean bridge){
        switch(ports){
            case E|W:return bridge?BRIDGE_H:ROAD_H;case N|S:return bridge?BRIDGE_V:ROAD_V;
            case N|E:return ROAD_NE;case E|S:return ROAD_ES;case S|W:return ROAD_SW;case W|N:return ROAD_WN;
            default:throw new IllegalArgumentException("bad road");
        }
    }
    private static int direction(int from,int to,int n){
        if(to==from-n)return N;if(to==from+n)return S;
        if(from/n==to/n&&to==from+1)return E;
        if(from/n==to/n&&to==from-1)return W;
        throw new IllegalArgumentException("not adjacent");
    }
    private static int transformed(int x,int y,int n,int mirror){
        if((mirror&1)!=0)x=n-1-x;if((mirror&2)!=0)y=n-1-y;
        return y*n+x;
    }
    private static String empty(int n){char[] cells=new char[n*n];Arrays.fill(cells,EMPTY);return new String(cells);}
    public static String blueprint(int age,int slot){
        validate(age,slot);if(free(slot))return "";
        int n=size(age,slot);char[] cells=empty(n).toCharArray();
        if(slot==0){cells[0]=HOUSE;for(int x=1;x<n-1;x++)cells[x]=ROAD_H;cells[n-1]=STATION;return new String(cells);}
        Random r=new Random(122001L+age*1009L+slot*191L);
        List<Integer> steps=new ArrayList<>(),path=new ArrayList<>();
        int mirror=slot%4,x=0,y=0;
        for(int i=1;i<n;i++){steps.add(E);steps.add(S);}
        Collections.shuffle(steps,r);path.add(transformed(x,y,n,mirror));
        for(int step:steps){if(step==E)x++;else y++;path.add(transformed(x,y,n,mirror));}
        cells[path.get(0)]=HOUSE;cells[path.get(path.size()-1)]=STATION;
        boolean bridgeUsed=false;
        for(int i=1;i<path.size()-1;i++){
            int pos=path.get(i),ports=direction(pos,path.get(i-1),n)|direction(pos,path.get(i+1),n);
            boolean bridge=slot>=8&&!bridgeUsed&&(ports==(E|W)||ports==(N|S));
            cells[pos]=road(ports,bridge);bridgeUsed|=bridge;
        }
        // Every design retains its connected road, with an increasingly rich landscape.
        List<Integer> spare=new ArrayList<>();
        for(int i=0;i<cells.length;i++)if(cells[i]==EMPTY)spare.add(i);
        Collections.shuffle(spare,r);
        int decorations=Math.min(spare.size(),1+age+slot/3);
        char[] decor=slot<4?new char[]{TREE,FLOWER}:new char[]{TREE,FLOWER,WATER};
        for(int i=0;i<decorations;i++)cells[spare.get(i)]=decor[(i+slot)%decor.length];
        return new String(cells);
    }
    public static boolean validBoard(String value,int n){
        if(value==null||value.length()!=n*n)return false;
        for(int i=0;i<value.length();i++)if(!validPiece(value.charAt(i)))return false;
        return true;
    }
    public static final class State{
        public final int age,slot,size;
        private String cells;
        private final ArrayList<String> undo=new ArrayList<>(),redo=new ArrayList<>();
        public State(int age,int slot,String saved){
            validate(age,slot);this.age=age;this.slot=slot;size=PlaylumiBuilder.size(age,slot);cells=empty(size);
            if(saved==null||saved.length()>4096)return;
            String[] parts=saved.split("\\|",-1);
            if(parts.length!=3||!validBoard(parts[0],size))return;
            cells=parts[0];load(undo,parts[1]);load(redo,parts[2]);
        }
        private void load(ArrayList<String> list,String value){
            if(value.isEmpty())return;
            String[] states=value.split(",",-1);
            if(states.length>HISTORY_LIMIT)return;
            for(String s:states)if(!validBoard(s,size))return;
            Collections.addAll(list,states);
        }
        private static void push(ArrayList<String> list,String state){if(list.size()==HISTORY_LIMIT)list.remove(0);list.add(state);}
        public String cells(){return cells;}
        public char cell(int i){if(i<0||i>=cells.length())throw new IllegalArgumentException("cell");return cells.charAt(i);}
        public String snapshot(){return cells+"|"+String.join(",",undo)+"|"+String.join(",",redo);}
        public boolean canUndo(){return !undo.isEmpty();}
        public boolean canRedo(){return !redo.isEmpty();}
        private boolean change(String next){
            if(cells.equals(next))return false;
            push(undo,cells);cells=next;redo.clear();return true;
        }
        public boolean place(int index,char piece){
            if(index<0||index>=cells.length()||!validPiece(piece))return false;
            char[] next=cells.toCharArray();next[index]=piece;return change(new String(next));
        }
        public boolean clear(){return change(empty(size));}
        public boolean undo(){if(!canUndo())return false;push(redo,cells);cells=undo.remove(undo.size()-1);return true;}
        public boolean redo(){if(!canRedo())return false;push(undo,cells);cells=redo.remove(redo.size()-1);return true;}
        public int placed(){int count=0;for(int i=0;i<cells.length();i++)if(cell(i)!=EMPTY)count++;return count;}
        public int different(){
            if(free(slot))return 0;
            String target=blueprint(age,slot);int count=0;
            for(int i=0;i<cells.length();i++)if(cell(i)!=target.charAt(i))count++;
            return count;
        }
        public boolean matches(){return !free(slot)&&different()==0;}
        public Network network(){return inspect(cells,size);}
    }
    public static final class Network{
        public final int houses,stations,connected;
        private Network(int houses,int stations,int connected){this.houses=houses;this.stations=stations;this.connected=connected;}
        public boolean complete(){return houses>0&&stations>0&&houses==connected;}
    }
    /** A route must connect on both tile edges. A house is a destination, not a through-road. */
    public static Network inspect(String board,int size){
        if(!validBoard(board,size))throw new IllegalArgumentException("network");
        int houses=0,stations=0,connected=0;
        boolean[] seen=new boolean[board.length()];int[] queue=new int[board.length()];int tail=0,head=0;
        for(int i=0;i<board.length();i++){
            char piece=board.charAt(i);
            if(piece==HOUSE)houses++;
            if(piece==STATION){stations++;seen[i]=true;queue[tail++]=i;}
        }
        while(head<tail){
            int pos=queue[head++];char tile=board.charAt(pos);
            if(tile==HOUSE){connected++;continue;}
            for(int d=0;d<4;d++){
                int y=pos/size+DY[d],x=pos%size+DX[d];
                if(y<0||y>=size||x<0||x>=size||(ports(tile)&PORTS[d])==0)continue;
                int next=y*size+x;
                if(!seen[next]&&(ports(board.charAt(next))&PORTS[(d+2)%4])!=0){seen[next]=true;queue[tail++]=next;}
            }
        }
        return new Network(houses,stations,connected);
    }
    public static final class Completion{
        public final int after,bonus;
        private Completion(int before,int after){this.after=after;bonus=rewardTotal(after)-rewardTotal(before);}
    }
    public static Completion complete(int stored,State state){
        int done=safe(stored);
        if(state==null||free(state.slot)||state.slot!=done||done==PLANS||!state.matches()||!state.network().complete())return new Completion(done,done);
        return new Completion(done,done+1);
    }
}
