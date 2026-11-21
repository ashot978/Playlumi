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
        b.setMinHeight(dp(90));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x55ffffff),bg(pastel[index%pastel.length]),null));
        b.setElevation(dp(4));
        b.setContentDescription(value);
        return b;
    }
    private void show() {
        removeAllViews();
        cards.clear();
        status=caption(question.kind==PlaylumiMiniChallenge.MEMORY?
                "Zoek twee dezelfde kaartjes!":"",17,0xff77688c);
        if(question.kind==PlaylumiMiniChallenge.DRAG) {
            status.setText("Houd vast en sleep. Tikken mag ook!");
            addView(status,new LayoutParams(-1,-2));
            TextView destination=caption("🏡\n"+question.target,48,0xff7353ae);
            destination.setBackground(bg(0xffe4f8dd));
            destination.setMinHeight(dp(175));
            LayoutParams dp=new LayoutParams(-1,dp(175));
            dp.setMargins(dp(7),dp(7),dp(7),dp(18));
            addView(destination,dp);
            destination.setOnDragListener((view,event)->{
                if(event.getAction()==DragEvent.ACTION_DRAG_STARTED)return true;
                if(event.getAction()==DragEvent.ACTION_DRAG_ENTERED) {
                    view.setScaleX(1.04f);view.setScaleY(1.04f);return true;
                }
                if(event.getAction()==DragEvent.ACTION_DRAG_EXITED ||
                   event.getAction()==DragEvent.ACTION_DRAG_ENDED) {
                    view.setScaleX(1f);view.setScaleY(1f);return true;
                }
                if(event.getAction()==DragEvent.ACTION_DROP) {
                    view.setScaleX(1f);view.setScaleY(1f);
                    if(event.getLocalState() instanceof Integer)
                        checkAnswer(((Integer)event.getLocalState()).intValue());
                    return true;
                }
                return true;
            });
            addChoices(false);
        } else if(question.kind==PlaylumiMiniChallenge.BALLOONS) {
            status.setText("🎈 "+question.question);
            addView(status,new LayoutParams(-1,-2));
            addChoices(true);
        } else {
            addView(status,new LayoutParams(-1,-2));
            addMemory();
        }
    }
    private void addChoices(boolean balloon) {
        int count=question.choices.size();
        int columns=count>4?3:2;
        for(int start=0;start<count;start+=columns) {
            LinearLayout row=new LinearLayout(getContext());
            row.setGravity(Gravity.CENTER);
            for(int idx=start;idx<Math.min(start+columns,count);idx++) {
                final int answer=idx;
                String content=balloon?"🎈\n"+question.choices.get(idx):question.choices.get(idx);
                Button b=tile(content,idx,balloon?30:38);
                LayoutParams p=new LayoutParams(0,dp(balloon?124:95),1f);
                p.setMargins(dp(5),dp(balloon?13:6),dp(5),dp(7));
                row.addView(b,p);
                cards.add(b);
                b.setOnClickListener(v->checkAnswer(answer));
                if(balloon)floatBalloon(b,idx);
                else {
                    final float[] startXY=new float[2];
                    int threshold=ViewConfiguration.get(getContext()).getScaledTouchSlop();
                    b.setOnTouchListener((view,event)->{
                        switch(event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                startXY[0]=event.getX();startXY[1]=event.getY();
                                return false;
                            case MotionEvent.ACTION_MOVE:
                                if(Math.abs(event.getX()-startXY[0])>threshold ||
                                        Math.abs(event.getY()-startXY[1])>threshold) {
                                    ClipData clip=ClipData.newPlainText("playlumi","shape");
                                    if(Build.VERSION.SDK_INT>=24)
                                        view.startDragAndDrop(clip,new View.DragShadowBuilder(view),Integer.valueOf(answer),0);
                                    else
                                        view.startDrag(clip,new View.DragShadowBuilder(view),Integer.valueOf(answer),0);
                                    return true;
                                }
                                return false;
                            default:return false;
                        }
                    });
                }
            }
            addView(row,new LayoutParams(-1,-2));
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
    private void checkAnswer(int index) {
        if(solved)return;
        if(question.choose(index)) {
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
                LayoutParams p=new LayoutParams(0,dp(90),1);
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
