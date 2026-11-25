package nl.playlumi.app;

import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Fully-native, child-friendly interactive play surface; no HTML or web views. */
public final class PlaylumiMiniBoard extends LinearLayout {
    public interface Listener {
        void onSolved();
        void onMistake();
        void onMatch();
    }
    private final PlaylumiMiniChallenge question;
    private final Listener listener;
    private final List<Button> cards=new ArrayList<>();
    private final List<ObjectAnimator> floating=new ArrayList<>();
    private TextView status;
    private PlaylumiShapeGate shapeGate;
    private boolean solved,waiting;
    private static final class ShapeDrag {
        final PlaylumiMiniBoard owner;
        final int answerIndex;
        ShapeDrag(PlaylumiMiniBoard owner,int answerIndex) {
            this.owner=owner;this.answerIndex=answerIndex;
        }
    }
    private final int[] pastel={0xffd7efff,0xffffd9ed,0xfffce9ac,0xffd9f7e1,0xffe9dcff,0xffffe6c9};

    public PlaylumiMiniBoard(Context context,PlaylumiMiniChallenge question,Listener listener) {
        super(context);
        this.question=question;this.listener=listener;
        setOrientation(VERTICAL);
        setPadding(dp(8),dp(8),dp(8),dp(10));
        setGravity(Gravity.CENTER_HORIZONTAL);
        show();
    }
    private int dp(int value) {
        return (int)(getResources().getDisplayMetrics().density*value+.5f);
    }
    private GradientDrawable bg(int fill) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(fill);d.setCornerRadius(dp(26));
        return d;
    }
    private TextView caption(String text,int size,int color) {
        TextView view=new TextView(getContext());
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(9),dp(10),dp(9),dp(10));
        view.setMaxLines(4);
        return view;
    }
    private Button tile(String value,int index,int size) {
        Button b=new Button(getContext());
        b.setAllCaps(false);
        b.setText(value);
        b.setTextSize(size);
        b.setTextColor(PlaylumiMagicArt.INK);
        b.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4),dp(8),dp(4),dp(8));
        b.setMinHeight(dp(60));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),PlaylumiMagicArt.pastel(getContext(),index),null));
        b.setElevation(dp(4));
        b.setContentDescription(value);
        return b;
    }
    private boolean landscape() {
        return getResources().getConfiguration().orientation==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ||
                getResources().getConfiguration().screenWidthDp>=600;
    }
    private boolean shapeWide(){return PlaylumiLayout.shapeWide(getResources().getConfiguration().screenWidthDp,getResources().getConfiguration().screenHeightDp);}
    private void show() {
        removeAllViews();
        cards.clear();
        status=caption("",16,PlaylumiMagicArt.INK);
        status.setBackground(PlaylumiMagicArt.creamPlate(getContext()));
        if(question.kind==PlaylumiMiniChallenge.DRAG) {
            status.setText("Houd een vorm vast en sleep.");
            status.setPadding(dp(5),dp(4),dp(5),dp(4));
            status.setTextSize(14);
            addView(status,new LayoutParams(-1,dp(36)));

            LinearLayout board=new LinearLayout(getContext());
            boolean wide=shapeWide();
            board.setOrientation(wide?HORIZONTAL:VERTICAL);
            board.setGravity(Gravity.CENTER);
            addView(board,new LayoutParams(-1,-2));

            PlaylumiShapeGate destination=new PlaylumiShapeGate(getContext(),question.target);
            shapeGate=destination;
            int targetHeight=PlaylumiLayout.shapeTargetHeight(getResources().getConfiguration().screenWidthDp,
                    getResources().getConfiguration().screenHeightDp,question.choices.size());
            LayoutParams boxParams=wide?
                    new LayoutParams(0,dp(targetHeight),.92f):
                    new LayoutParams(-1,dp(targetHeight));
            boxParams.setMargins(dp(4),dp(3),dp(4),dp(5));
            board.addView(destination,boxParams);
            destination.setOnDragListener((view,event)->{
                switch(event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        Object token=event.getLocalState();
                        return token instanceof ShapeDrag && ((ShapeDrag)token).owner==this;
                    }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        view.setAlpha(.93f);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        view.setAlpha(1f);
                        return true;
                    case DragEvent.ACTION_DROP:
                        view.setAlpha(1f);
                        Object token=event.getLocalState();
                        if(token instanceof ShapeDrag && ((ShapeDrag)token).owner==this)
                            checkAnswer(((ShapeDrag)token).answerIndex,true);
                        return true;
                    default:return true;
                }
            });
            LinearLayout choices=new LinearLayout(getContext());
            choices.setOrientation(VERTICAL);
            choices.setGravity(Gravity.CENTER);
            LayoutParams choiceParams=wide?
                    new LayoutParams(0,-2,1.08f):new LayoutParams(-1,-2);
            choiceParams.setMargins(dp(3),0,dp(3),0);
            board.addView(choices,choiceParams);
            addChoices(choices,false);
        } else if(question.kind==PlaylumiMiniChallenge.BALLOONS) {
            status.setText("Tel rustig en kies een groep.");
            addView(status,new LayoutParams(-1,dp(45)));
            addChoices(this,true);
        } else {
            status.setText("🃏 Zoek twee dezelfde kaartjes");
            addView(status,new LayoutParams(-1,dp(45)));
            addMemory();
        }
    }
    private boolean beginShapeDrag(View view,int answer) {
        if(solved || !isAttachedToWindow())return false;
        ClipData clip=ClipData.newPlainText("playlumi-shape",question.choices.get(answer));
        ShapeDrag token=new ShapeDrag(this,answer);
        boolean started;
        if(Build.VERSION.SDK_INT>=24)
            started=view.startDragAndDrop(clip,new View.DragShadowBuilder(view),token,0);
        else
            started=view.startDrag(clip,new View.DragShadowBuilder(view),token,0);
        if(started)status.setText("Laat los in de vormpoort!");
        return started;
    }
    private void addChoices(LinearLayout parent,boolean balloon) {
        int count=question.choices.size();
        int width=getResources().getConfiguration().screenWidthDp;
        int height=getResources().getConfiguration().screenHeightDp;
        int columns=balloon?2:PlaylumiLayout.shapeColumns(width,height,count);
        int shapeHeight=PlaylumiLayout.shapeTileHeight(width,height,count);
        for(int start=0;start<count;start+=columns) {
            LinearLayout row=new LinearLayout(getContext());
            row.setGravity(Gravity.CENTER);
            for(int idx=start;idx<Math.min(start+columns,count);idx++) {
                final int answer=idx;
                String content=balloon?"":question.choices.get(idx);
                Button b=tile(content,idx,balloon?28:Math.max(28,Math.min(53,Math.round(shapeHeight*.49f))));
                if(!balloon){
                    b.setMinHeight(0);b.setMinimumHeight(0);
                    b.setPadding(dp(3),dp(3),dp(3),dp(3));
                }
                if(balloon){
                    int quantity=question.balloonCount(idx);
                    LayerDrawable art=new LayerDrawable(new android.graphics.drawable.Drawable[]{
                            PlaylumiMagicArt.pastel(getContext(),0),new PlaylumiBalloonGroup(quantity)});
                    b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),art,null));
                    b.setContentDescription("Groep met "+quantity+(quantity==1?" ballon":" ballonnen"));
                }
                int balloonHeight=landscape()?120:(getResources().getConfiguration().screenHeightDp<700?98:112);
                LayoutParams p=new LayoutParams(0,dp(balloon?balloonHeight:shapeHeight),1f);
                p.setMargins(dp(4),dp(4),dp(4),dp(4));
                row.addView(b,p);
                cards.add(b);
                if(balloon) {
                    b.setOnClickListener(v->checkAnswer(answer,false));
                    // Keep all balloons still and individually countable.
                } else {
                    // Tap provides guidance only; a real ACTION_DROP is necessary to score.
                    b.setOnClickListener(v->status.setText("Houd vast en sleep naar de poort!"));
                    b.setOnLongClickListener(v->beginShapeDrag(v,answer));
                    final float[] origin=new float[2];
                    final boolean[] dragging={false};
                    final int threshold=Math.max(dp(10),
                            ViewConfiguration.get(getContext()).getScaledTouchSlop());
                    b.setOnTouchListener((view,event)->{
                        switch(event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                origin[0]=event.getX();origin[1]=event.getY();
                                dragging[0]=false;
                                if(view.getParent()!=null)
                                    view.getParent().requestDisallowInterceptTouchEvent(true);
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                if(!dragging[0] && (Math.abs(event.getX()-origin[0])>threshold ||
                                        Math.abs(event.getY()-origin[1])>threshold)) {
                                    dragging[0]=beginShapeDrag(view,answer);
                                }
                                return true;
                            case MotionEvent.ACTION_UP:
                                if(!dragging[0])view.performClick();
                                if(view.getParent()!=null)
                                    view.getParent().requestDisallowInterceptTouchEvent(false);
                                return true;
                            case MotionEvent.ACTION_CANCEL:
                                if(view.getParent()!=null)
                                    view.getParent().requestDisallowInterceptTouchEvent(false);
                                return true;
                            default:return true;
                        }
                    });
                }
            }
            if(!balloon){
                for(int empty=Math.min(start+columns,count);empty<start+columns;empty++){
                    View spacer=new View(getContext());
                    LayoutParams p=new LayoutParams(0,dp(shapeHeight),1f);
                    p.setMargins(dp(4),dp(4),dp(4),dp(4));
                    row.addView(spacer,p);
                }
            }
            parent.addView(row,new LayoutParams(-1,-2));
        }
    }
    private void floatBalloon(Button b,int index) {
        if(PlaylumiMotion.isReduced())return;
        ObjectAnimator motion=ObjectAnimator.ofFloat(b,View.TRANSLATION_Y,0,-dp(9+index%3*4),0);
        motion.setDuration(1650+index*180);
        motion.setStartDelay(index*145);
        motion.setRepeatCount(ValueAnimator.INFINITE);
        motion.setInterpolator(new AccelerateDecelerateInterpolator());
        floating.add(motion);
        if(isAttachedToWindow())motion.start();
    }
    private void checkAnswer(int index,boolean dragged) {
        if(solved || (question.kind==PlaylumiMiniChallenge.DRAG && !dragged))return;
        if((question.kind==PlaylumiMiniChallenge.DRAG?question.drop(index):question.choose(index))) {
            solved=true;
            if(shapeGate!=null)shapeGate.setCompleted(true);
            status.setText("🌟 Joepie! Goed gedaan!");
            status.setTextColor(0xff32886b);
            for(Button b:cards)b.setEnabled(false);
            if(index<cards.size())PlaylumiMotion.pop(cards.get(index));
            listener.onSolved();
        } else {
            status.setText("💛 Probeer eens een andere!");
            if(index<cards.size())PlaylumiDelight.wiggle(cards.get(index));
            listener.onMistake();
        }
    }
    public void restoreSolvedShape(){
        if(question.kind!=PlaylumiMiniChallenge.DRAG)return;
        solved=true;
        if(shapeGate!=null)shapeGate.setCompleted(true);
        status.setText("🌟 Joepie! Goed gedaan!");
        status.setTextColor(0xff276748);
        for(Button b:cards)b.setEnabled(false);
    }
    private void addMemory() {
        int columns=landscape() && question.choices.size()>8?4:3;
        int h=getResources().getConfiguration().screenHeightDp;
        int cardHeight=landscape()?Math.max(74,Math.min(128,h/8)):h<700?60:78;
        for(int start=0;start<question.choices.size();start+=columns) {
            LinearLayout row=new LinearLayout(getContext());
            for(int idx=start;idx<Math.min(start+columns,question.choices.size());idx++) {
                final int index=idx;
                Button b=tile("",idx,36);
                LayoutParams p=new LayoutParams(0,dp(cardHeight),1);
                p.setMargins(dp(3),dp(4),dp(3),dp(4));
                row.addView(b,p);
                cards.add(b);
                b.setOnClickListener(v->flip(index));
            }
            addView(row,new LayoutParams(-1,-2));
        }
        renderMemory();
    }
    private void flip(int index) {
        if(waiting || solved)return;
        int result=question.flip(index);
        if(result==0)return;
        renderMemory();
        if(index<cards.size())PlaylumiDelight.flip(cards.get(index));
        if(result==2 || result==3) {
            listener.onMatch();
            if(index<cards.size())PlaylumiMotion.pop(cards.get(index));
            if(result==3) {
                solved=true;
                status.setText("🎉 Alle diertjes gevonden!");
                listener.onSolved();
            }
        } else if(result==-1) {
            waiting=true;
            status.setText("🌼 Kijk goed en onthoud!");
            postDelayed(()->{
                question.hideMiss();
                waiting=false;
                if(isAttachedToWindow())renderMemory();
            },950);
            listener.onMistake();
        }
    }
    private void renderMemory() {
        for(int i=0;i<cards.size();i++) {
            Button b=cards.get(i);
            boolean revealed=question.visible(i);
            b.setText(revealed?question.choices.get(i):"");
            b.setTextColor(PlaylumiMagicArt.INK);
            b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),
                    PlaylumiMagicArt.memory(getContext(),i,revealed,question.matched(i)),null));
            b.setContentDescription(revealed?question.choices.get(i):"Gesloten kaart "+(i+1));
            b.setEnabled(!question.matched(i) && !solved);
        }
        if(!solved)status.setText("🃏 "+question.matchedPairs()+" van "+question.pairCount()+" paar gevonden");
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for(ObjectAnimator m:floating) if(!m.isStarted())m.start();
    }
    @Override protected void onDetachedFromWindow() {
        for(ObjectAnimator m:floating)m.cancel();
        super.onDetachedFromWindow();
    }
}
