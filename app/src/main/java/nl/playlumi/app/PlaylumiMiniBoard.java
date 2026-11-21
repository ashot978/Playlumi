package nl.playlumi.app;

import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
        b.setTextColor(0xff3f3166);
        b.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4),dp(8),dp(4),dp(8));
        b.setMinHeight(dp(60));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x55ffffff),bg(pastel[index%pastel.length]),null));
        b.setElevation(dp(4));
        b.setContentDescription(value);
        return b;
    }
    private boolean landscape() {
        return getResources().getConfiguration().orientation==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE;
    }
    private void show() {
        removeAllViews();
        cards.clear();
        status=caption("",16,0xff77688c);
        if(question.kind==PlaylumiMiniChallenge.DRAG) {
            status.setText("👆 Sleep de vorm naar het huisje");
            addView(status,new LayoutParams(-1,dp(landscape()?36:42)));

            LinearLayout board=new LinearLayout(getContext());
            board.setOrientation(landscape()?HORIZONTAL:VERTICAL);
            board.setGravity(Gravity.CENTER_VERTICAL);
            addView(board,new LayoutParams(-1,-2));

            TextView destination=caption("🏡\n"+question.target,landscape()?34:39,0xff7353ae);
            destination.setBackground(bg(0xffe4f8dd));
            destination.setContentDescription("Huisje. Laat hier de gekozen vorm los.");
            int targetHeight=landscape()?194:108;
            LayoutParams boxParams=landscape()?
                    new LayoutParams(0,dp(targetHeight),1f):
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
                        view.setScaleX(1.04f);view.setScaleY(1.04f);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        view.setScaleX(1f);view.setScaleY(1f);
                        return true;
                    case DragEvent.ACTION_DROP:
                        view.setScaleX(1f);view.setScaleY(1f);
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
            LayoutParams choiceParams=landscape()?
                    new LayoutParams(0,-2,1f):new LayoutParams(-1,-2);
            choiceParams.setMargins(dp(3),0,dp(3),0);
            board.addView(choices,choiceParams);
            addChoices(choices,false);
        } else if(question.kind==PlaylumiMiniChallenge.BALLOONS) {
            status.setText("🎈 "+question.question);
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
        if(started)status.setText("🏡 Laat de vorm los in het groene huisje");
        return started;
    }
    private void addChoices(LinearLayout parent,boolean balloon) {
        int count=question.choices.size();
        int columns=count==3 || count>4?3:2;
        for(int start=0;start<count;start+=columns) {
            LinearLayout row=new LinearLayout(getContext());
            row.setGravity(Gravity.CENTER);
            for(int idx=start;idx<Math.min(start+columns,count);idx++) {
                final int answer=idx;
                String content=balloon?"🎈\n"+question.choices.get(idx):question.choices.get(idx);
                Button b=tile(content,idx,balloon?28:landscape()?31:36);
                LayoutParams p=new LayoutParams(0,dp(balloon?96:76),1f);
                p.setMargins(dp(4),dp(4),dp(4),dp(4));
                row.addView(b,p);
                cards.add(b);
                if(balloon) {
                    b.setOnClickListener(v->checkAnswer(answer,false));
                    floatBalloon(b,idx);
                } else {
                    // Tap provides guidance only; a real ACTION_DROP is necessary to score.
                    b.setOnClickListener(v->status.setText("👆 Houd de vorm vast en sleep naar het huisje"));
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
            status.setText("🌟 Joepie! Goed gedaan!");
            status.setTextColor(0xff32886b);
            for(Button b:cards)b.setEnabled(false);
            if(index<cards.size())PlaylumiMotion.pop(cards.get(index));
            listener.onSolved();
        } else {
            status.setText("💛 Probeer eens een andere!");
            if(index<cards.size())PlaylumiMotion.shake(cards.get(index));
            listener.onMistake();
        }
    }
    private void addMemory() {
        int columns=question.choices.size()>8?4:3;
        for(int start=0;start<question.choices.size();start+=columns) {
            LinearLayout row=new LinearLayout(getContext());
            for(int idx=start;idx<Math.min(start+columns,question.choices.size());idx++) {
                final int index=idx;
                Button b=tile("❔",idx,28);
                LayoutParams p=new LayoutParams(0,dp(74),1);
                p.setMargins(dp(5),dp(7),dp(5),dp(7));
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
            b.setText(revealed?question.choices.get(i):"❔");
            b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    bg(question.matched(i)?0xffc7f5d5:revealed?0xffffedbc:pastel[i%pastel.length]),null));
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
