package nl.playlumi.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Native board with stable cells. A correct tap changes content, never reconstructs the view. */
public final class PlaylumiInteractiveBoard extends LinearLayout {
    private final PlaylumiInteractive round;
    private final TextView[] cells;
    private TextView animal;
    private final int columns;
    public PlaylumiInteractiveBoard(Context context,PlaylumiInteractive round,int panelWidth){
        super(context);this.round=round;
        setOrientation(VERTICAL);setGravity(Gravity.CENTER_HORIZONTAL);setPadding(dp(4),dp(4),dp(4),dp(4));
        columns=PlaylumiLayout.interactiveCellColumns(panelWidth,round.total());
        if(round.kind==PlaylumiInteractive.HABITATS){
            cells=null;
            addView(label("Waar woont dit dier?",18,PlaylumiMagicArt.CREAM));
            animal=label("",66,PlaylumiMagicArt.INK);animal.setMinHeight(dp(126));
            animal.setBackground(surface(0xfffff1d4,0xffffd891));
            LayoutParams ap=new LayoutParams(-1,-2);ap.setMargins(dp(8),dp(7),dp(8),dp(7));addView(animal,ap);
        }else{
            if(round.kind==PlaylumiInteractive.LETTERS||round.kind==PlaylumiInteractive.MOSAIC){
                addView(label("Voorbeeld",14,PlaylumiMagicArt.GOLD));
                addCells(true);
            }
            addView(label(round.kind==PlaylumiInteractive.NUMBERS?"🚀 Bouw jouw raket":round.kind==PlaylumiInteractive.LETTERS?"🚂 Jouw lettertrein":"Jouw mozaïek",18,PlaylumiMagicArt.CREAM));
            cells=addCells(false);
        }
        refresh();
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView label(String value,int size,int color){
        TextView t=new TextView(getContext());t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));t.setGravity(Gravity.CENTER);
        t.setIncludeFontPadding(false);t.setPadding(dp(3),dp(4),dp(3),dp(4));return t;
    }
    private GradientDrawable surface(int fill,int stroke){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(12));d.setStroke(dp(2),stroke);return d;
    }
    private String symbol(int i){String t=round.target(i);return round.kind==PlaylumiInteractive.MOSAIC?t.split(" ")[0]:t;}
    private TextView[] addCells(boolean example){
        TextView[] result=new TextView[round.total()];
        for(int first=0;first<result.length;first+=columns){
            LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER);
            for(int j=0;j<columns;j++){
                int i=first+j;
                LayoutParams lp=new LayoutParams(0,dp(example?38:54),1f);lp.setMargins(dp(3),dp(3),dp(3),dp(3));
                if(i>=result.length){row.addView(new View(getContext()),lp);continue;}
                TextView cell=label(example?symbol(i):"",example?21:25,PlaylumiMagicArt.INK);
                if(example){cell.setBackground(surface(0xfff5e7ff,0xffcba7ee));cell.setContentDescription("Voorbeeld "+round.target(i));}
                row.addView(cell,lp);result[i]=cell;
            }
            addView(row,new LayoutParams(-1,-2));
        }
        return result;
    }
    public void refresh(){
        if(animal!=null){
            animal.setText(round.solved()?"✓":round.target(round.progress()));
            animal.setContentDescription(round.solved()?"Alle dieren thuis":"Dier "+(round.progress()+1)+" van "+round.total()+": "+round.target(round.progress()));
            return;
        }
        for(int i=0;i<cells.length;i++){
            boolean complete=i<round.progress(),next=i==round.progress();
            TextView cell=cells[i];cell.setText(complete?symbol(i):next?"·":"");
            cell.setTextColor(complete?PlaylumiMagicArt.INK:PlaylumiMagicArt.GOLD);
            cell.setBackground(surface(complete?0xffd4f5da:next?0xff553681:0xff3f285e,complete?0xff85c598:next?0xffffdc88:0xff8b68b2));
            cell.setContentDescription("Vak "+(i+1)+(complete?", "+round.target(i):next?", aan de beurt":", leeg"));
        }
    }
}
