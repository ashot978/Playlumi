package nl.playlumi.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/** Native interactive tiles with actual directional roads and bridges. */
public final class PlaylumiBuilderGrid extends LinearLayout {
    public interface Listener{void onCell(int index);}
    private final int size;
    private final Button[] cells;
    private final boolean editable;
    public PlaylumiBuilderGrid(Context c,int size,int width,String board,boolean editable,Listener listener){
        super(c);this.size=size;this.editable=editable;
        if(!PlaylumiBuilder.validBoard(board,size))throw new IllegalArgumentException("grid");
        setOrientation(VERTICAL);setGravity(Gravity.CENTER_HORIZONTAL);setPadding(dp(2),dp(2),dp(2),dp(2));
        int gap=editable?2:1,cell=Math.min(editable?70:49,Math.max(editable?28:12,(width-8-size*gap*2)/size));
        cells=new Button[size*size];
        for(int y=0;y<size;y++){
            LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER);
            for(int x=0;x<size;x++){
                final int i=y*size+x;
                Button b=new Button(c);b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);
                b.setPadding(0,0,0,0);b.setMaxLines(1);b.setGravity(Gravity.CENTER);
                b.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));b.setTextSize(Math.max(editable?15:9,Math.min(editable?29:22,cell*.50f)));
                b.setIncludeFontPadding(false);
                LayoutParams lp=new LayoutParams(dp(cell),dp(cell));lp.setMargins(dp(gap),dp(gap),dp(gap),dp(gap));row.addView(b,lp);
                if(editable)b.setOnClickListener(v->listener.onCell(i));
                else{b.setClickable(false);b.setFocusable(false);b.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
                cells[i]=b;
            }
            addView(row,new LayoutParams(-1,-2));
        }
        refresh(board);
    }
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    public void refresh(String board){
        if(!PlaylumiBuilder.validBoard(board,size))throw new IllegalArgumentException("board");
        for(int i=0;i<cells.length;i++){
            Button b=cells[i];char p=board.charAt(i);
            boolean drawn=PlaylumiBuilder.rotatable(p)||p==PlaylumiBuilder.WATER||p==PlaylumiBuilder.EMPTY;
            b.setText(drawn?"":PlaylumiBuilder.icon(p));
            int fill=PlaylumiBuilder.color(p);
            b.setTextColor(PlaylumiWorldStyle.contrastingInk(fill));
            PlaylumiBuilderTile face=new PlaylumiBuilderTile(p);
            b.setBackground(editable?new RippleDrawable(ColorStateList.valueOf(0x44ffffff),face,null):face);
            b.setContentDescription((editable?"Bouwvak ":"Voorbeeldvak ")+(i+1)+": "+PlaylumiBuilder.name(p));
        }
    }
}
