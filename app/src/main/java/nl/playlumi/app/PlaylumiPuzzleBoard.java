package nl.playlumi.app;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Directly playable, responsive puzzle grid with accessible labels. */
public final class PlaylumiPuzzleBoard extends LinearLayout {
    public interface Listener{void onTap(int cell,Button button);}
    private final PlaylumiPuzzle p;private final TextView[] cells;private final Button[] animalChoices;private final TextView task,trail;
    public PlaylumiPuzzleBoard(Context c,PlaylumiPuzzle p,int width,Listener listener){
        super(c);this.p=p;setOrientation(VERTICAL);setGravity(Gravity.CENTER_HORIZONTAL);setPadding(dp(6),dp(6),dp(6),dp(6));
        animalChoices=new Button[p.kind==PlaylumiPuzzle.SUDOKU?p.optionCount():0];
        task=label("",p.kind==PlaylumiPuzzle.SUDOKU?18:22,PlaylumiMagicArt.CREAM);task.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);addView(task,new LayoutParams(-1,-2));
        if(p.kind==PlaylumiPuzzle.PIXEL){
            addView(label("Voorbeeld",15,PlaylumiMagicArt.GOLD));int size=Math.min(43,Math.max(28,(width-24)/p.cols-6));
            for(int y=0;y<p.rows;y++){LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER);
                for(int x=0;x<p.cols;x++){String s=p.reference(y*p.cols+x);TextView cell=label(s,19,PlaylumiMagicArt.INK);cell.setBackground(face(PlaylumiPuzzle.paint(s),0xffffe0a1,8));cell.setContentDescription("Voorbeeld rij "+(y+1)+", vak "+(x+1)+": "+s);LayoutParams lp=new LayoutParams(dp(size),dp(size));lp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(cell,lp);}addView(row,new LayoutParams(-1,-2));}
            addView(label("Jouw pixelplaat",15,PlaylumiMagicArt.GOLD));
        }
        trail=label("",21,PlaylumiMagicArt.GOLD);if(p.kind==PlaylumiPuzzle.RHYTHM)addView(trail,new LayoutParams(-1,-2));
        if(p.kind==PlaylumiPuzzle.SYMMETRY)addView(label("Voorbeeld links  │  Spiegel rechts",15,PlaylumiMagicArt.GOLD));
        int size=Math.min(p.kind==PlaylumiPuzzle.RHYTHM?112:76,Math.max(36,(width-24-(p.kind==PlaylumiPuzzle.MAP?28:0))/p.cols-6));
        if(p.kind==PlaylumiPuzzle.MAZE&&p.cols==5)size=Math.min(size,64);
        cells=new TextView[p.cells()];
        if(p.kind==PlaylumiPuzzle.SUDOKU){
            buildSudoku(c,width,listener);
            refresh();return;
        }
        if(p.kind==PlaylumiPuzzle.MAP){LinearLayout axis=new LinearLayout(c);axis.setGravity(Gravity.CENTER);axis.addView(new View(c),new LayoutParams(dp(26),1));for(int x=0;x<p.cols;x++)axis.addView(label(""+(char)('A'+x),16,PlaylumiMagicArt.GOLD),new LayoutParams(dp(size+6),dp(28)));addView(axis,new LayoutParams(-1,-2));}
        for(int y=0;y<p.rows;y++){LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER);
            if(p.kind==PlaylumiPuzzle.MAP)row.addView(label(""+(y+1),16,PlaylumiMagicArt.GOLD),new LayoutParams(dp(26),dp(size)));
            for(int x=0;x<p.cols;x++){final int i=y*p.cols+x;Button cell=new Button(c);cell.setAllCaps(false);cell.setMinWidth(0);cell.setMinimumWidth(0);cell.setMinHeight(0);cell.setMinimumHeight(0);cell.setPadding(0,0,0,0);cell.setGravity(Gravity.CENTER);cell.setMaxLines(1);cell.setTextSize(p.kind==PlaylumiPuzzle.RHYTHM?35:p.kind==PlaylumiPuzzle.SORT?29:23);cell.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));LayoutParams lp=new LayoutParams(dp(size),dp(size));lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(cell,lp);cells[i]=cell;cell.setOnClickListener(v->listener.onTap(i,cell));}addView(row,new LayoutParams(-1,-2));}
        refresh();
    }
    private void buildSudoku(Context context,int width,Listener listener){
        int cellSize=Math.min(p.cols==2?72:54,Math.max(32,(width-40)/p.cols));
        LinearLayout grid=new LinearLayout(context);grid.setOrientation(VERTICAL);
        grid.setGravity(Gravity.CENTER);grid.setPadding(dp(4),dp(4),dp(4),dp(4));
        grid.setBackground(face(0xff49346b,0xffba98df,12));
        for(int row=0;row<p.rows;row++){
            LinearLayout line=new LinearLayout(context);line.setGravity(Gravity.CENTER);
            for(int column=0;column<p.cols;column++){
                int index=row*p.cols+column;
                TextView cell=label("",p.cols==2?32:26,PlaylumiMagicArt.INK);
                cell.setPadding(0,0,0,0);cell.setMaxLines(1);cell.setGravity(Gravity.CENTER);
                cell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                LayoutParams params=new LayoutParams(dp(cellSize),dp(cellSize));
                params.setMargins(dp(2),dp(2),dp(p.cols==4&&column==1?7:2),dp(p.rows==4&&row==1?7:2));
                line.addView(cell,params);cells[index]=cell;
            }
            grid.addView(line,new LayoutParams(-2,-2));
        }
        addView(grid,new LayoutParams(-2,-2));
        TextView instruction=label("Kies het ontbrekende dier",14,PlaylumiMagicArt.GOLD);
        addView(instruction,new LayoutParams(-1,-2));
        LinearLayout choices=new LinearLayout(context);choices.setGravity(Gravity.CENTER);
        for(int option=0;option<p.optionCount();option++){
            final int choice=option;Button button=new Button(context);
            button.setAllCaps(false);button.setMinWidth(0);button.setMinimumWidth(0);
            button.setMinHeight(dp(52));button.setMinimumHeight(dp(52));
            button.setPadding(dp(2),dp(3),dp(2),dp(3));button.setGravity(Gravity.CENTER);
            button.setMaxLines(2);button.setTextSize(14);
            button.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
            String icon=p.optionIcon(option),label=icon+"\n"+p.optionName(option);
            android.text.SpannableString caption=new android.text.SpannableString(label);
            caption.setSpan(new android.text.style.RelativeSizeSpan(1.6f),0,icon.length(),0);
            button.setText(caption);button.setTextColor(PlaylumiMagicArt.INK);
            button.setContentDescription("Kies "+p.optionName(option)+" voor het gemarkeerde vak");
            button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),face(0xfffff1d8,0xffe6bb72,12),null));
            LayoutParams params=new LayoutParams(0,-2,1);params.setMargins(dp(3),dp(2),dp(3),dp(2));
            choices.addView(button,params);animalChoices[option]=button;
            button.setOnClickListener(v->listener.onTap(choice,button));
        }
        addView(choices,new LayoutParams(-1,-2));
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView label(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));t.setPadding(dp(2),dp(4),dp(2),dp(4));return t;}
    private GradientDrawable face(int fill,int outline,int radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(2),outline);return d;}
    public void refresh(){task.setText(p.prompt());trail.setText(p.rhythmTrail());
        for(int i=0;i<cells.length;i++){TextView b=cells[i];boolean done=p.selected(i),blocked=p.blocked(i);String s=p.display(i);int fill=done?0xffc6f0cf:0xfffff1d8,stroke=done?0xff56ad78:0xffe6bb72;
            if(p.kind==PlaylumiPuzzle.PIXEL||p.kind==PlaylumiPuzzle.SYMMETRY){fill=PlaylumiPuzzle.paint(s);stroke=done?0xff328855:0xffffe0a1;}
            if(p.kind==PlaylumiPuzzle.MAZE){fill=blocked?0xff4a326c:0xfff2dcb2;if(done||i==p.startCell())fill=0xffbae6c4;if(i==p.currentCell())stroke=0xff47a470;}
            if(p.kind==PlaylumiPuzzle.MAP){fill=done?0xffb5ecd8:0xffdcecfb;stroke=0xff9caedd;}
            if(p.kind==PlaylumiPuzzle.SUDOKU){
                boolean current=i==p.currentCell();
                fill=current?0xffffd479:done?0xffbceacb:blocked?0xfff3ecff:0xffd5c4e8;
                stroke=current?0xffc18312:done?0xff399f62:0xff9d7cba;
                if(current)s="?";
            }
            b.setText(s);b.setTextColor(PlaylumiWorldStyle.contrastingInk(fill));b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),face(fill,stroke,12),null));
            String desc=p.kind==PlaylumiPuzzle.MAP?"Vak "+p.coordinate(i):"Vak "+(i+1)+": "+s;
            if(p.kind==PlaylumiPuzzle.MAZE)desc="Doolhof vak "+(i+1)+(blocked?", muur":i==p.currentCell()?", raket":i==p.goalCell()?", ster":", pad");
            if(p.kind==PlaylumiPuzzle.SUDOKU){
                desc="Sudoku rij "+(i/p.cols+1)+", kolom "+(i%p.cols+1)+": ";
                if(blocked)desc+=p.animalName(i)+", gegeven";
                else if(done)desc+=p.animalName(i)+", ingevuld";
                else desc+="leeg"+(i==p.currentCell()?", nu invullen":"");
                b.setContentDescription(desc);b.setEnabled(true);b.setClickable(false);
                continue;
            }
            b.setContentDescription(desc+(done?", gekozen":""));b.setEnabled(!p.solved()&&!blocked&&!done&&i!=p.currentCell()&&i!=p.startCell());
        }
        for(Button option:animalChoices)option.setEnabled(!p.solved());
    }
}
