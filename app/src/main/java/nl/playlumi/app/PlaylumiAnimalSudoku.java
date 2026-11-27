package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/** Small, deterministic animal sudokus. Every highlighted move is a naked single. */
public final class PlaylumiAnimalSudoku {
    private static final String[] ICONS={"🐱","🐶","🐰","🐸","🐻","🐼","🦊","🐨"};
    private static final String[] NAMES={"kat","hond","konijn","kikker","beer","panda","vos","koala"};
    public final int size;
    private final int[] given,solution,order,animals;

    public PlaylumiAnimalSudoku(int age,long seed){
        if(age<0||age>2)throw new IllegalArgumentException("Sudoku age");
        size=age==0?2:4;
        Random random=new Random(seed);
        animals=shuffled(ICONS.length,random);
        solution=new int[size*size];
        int[] symbols=shuffled(size,random);
        int[] rows=permutation(size,random),columns=permutation(size,random);
        boolean transpose=random.nextBoolean();
        for(int y=0;y<size;y++)for(int x=0;x<size;x++){
            int row=transpose?columns[x]:rows[y],column=transpose?rows[y]:columns[x];
            int offset=size==2?row:(row*2+row/2);
            solution[y*size+x]=symbols[(offset+column)%size];
        }
        given=solution.clone();
        int target=age==0?2:age==1?5:9,removed=0;
        for(int cell:shuffled(given.length,random)){
            int value=given[cell];given[cell]=-1;
            if(logicalOrder(given,size)!=null)removed++;else given[cell]=value;
            if(removed==target)break;
        }
        order=logicalOrder(given,size);
        if(order==null||order.length==0)throw new IllegalStateException("Unplayable Sudoku");
    }

    private static int[] shuffled(int count,Random random){
        ArrayList<Integer> values=new ArrayList<>();
        for(int i=0;i<count;i++)values.add(i);
        Collections.shuffle(values,random);
        int[] result=new int[count];for(int i=0;i<count;i++)result[i]=values.get(i);
        return result;
    }
    private static int[] permutation(int size,Random random){
        if(size==2)return shuffled(2,random);
        int[] result=new int[4],bands=shuffled(2,random);
        for(int band=0;band<2;band++){
            int[] inside=shuffled(2,random);
            for(int i=0;i<2;i++)result[band*2+i]=bands[band]*2+inside[i];
        }
        return result;
    }
    /** Candidates derived only from visible clues, never from the solution. */
    static int candidates(int[] board,int size,int cell){
        if(cell<0||cell>=board.length||board[cell]>=0)return 0;
        int mask=(1<<size)-1,row=cell/size,column=cell%size;
        for(int i=0;i<size;i++){
            int horizontal=board[row*size+i],vertical=board[i*size+column];
            if(horizontal>=0)mask&=~(1<<horizontal);
            if(vertical>=0)mask&=~(1<<vertical);
        }
        if(size==4){
            int top=row/2*2,left=column/2*2;
            for(int y=top;y<top+2;y++)for(int x=left;x<left+2;x++){
                int value=board[y*size+x];if(value>=0)mask&=~(1<<value);
            }
        }
        return mask;
    }
    /** A complete chain of singles also proves a unique completion. */
    private static int[] logicalOrder(int[] source,int size){
        int[] board=source.clone(),steps=new int[board.length];int count=0,remaining=0;
        for(int value:board)if(value<0)remaining++;
        while(remaining>0){
            int next=-1,choice=0;
            for(int cell=0;cell<board.length;cell++)if(board[cell]<0){
                int mask=candidates(board,size,cell);
                if(mask==0)return null;
                if(Integer.bitCount(mask)==1){next=cell;choice=Integer.numberOfTrailingZeros(mask);break;}
            }
            if(next<0)return null;
            board[next]=choice;steps[count++]=next;remaining--;
        }
        return Arrays.copyOf(steps,count);
    }
    public int total(){return order.length;}
    public int cellAt(int step){return step>=0&&step<order.length?order[step]:-1;}
    public int answerAt(int step){int cell=cellAt(step);return cell<0?-1:solution[cell];}
    public boolean given(int cell){return given[cell]>=0;}
    public int givenValue(int cell){return given[cell];}
    public int solutionValue(int cell){return solution[cell];}
    public String icon(int choice){return choice>=0&&choice<size?ICONS[animals[choice]]:"";}
    public String name(int choice){return choice>=0&&choice<size?NAMES[animals[choice]]:"";}
    public String clue(int cell){return given(cell)?icon(given[cell]):"·";}
    public String solvedCell(int cell){return icon(solution[cell]);}
}
