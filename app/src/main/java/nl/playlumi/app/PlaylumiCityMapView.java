package nl.playlumi.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A growing, offline city scene. Each native button owns a whole illustrated
 * site, so touch, keyboard and TalkBack all activate the same action. Empty
 * plots become buildings only from the supplied, persisted achievement mask.
 * This view does not change the wallet, gifts or achievement data.
 *
 * Add at MATCH_PARENT width and WRAP_CONTENT height inside a scrolling page.
 * The existing approved illustrated home is deliberately a separate view.
 */
public final class PlaylumiCityMapView extends ViewGroup {
    public interface Listener { void onLandmark(int index); }
    private static final int INK=0xff46245f,GOLD=0xffffd780;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF rect=new RectF();
    private final Button[] sites=new Button[PlaylumiCityLayout.SITES];
    private final float density;
    private final boolean reducedMotion;
    private long achievements;
    private PlaylumiCityLayout geometry;

    public PlaylumiCityMapView(Context context,long achievementMask,
                               boolean reducedMotion,Listener listener){
        super(context);
        density=context.getResources().getDisplayMetrics().density;
        this.reducedMotion=reducedMotion;
        achievements=achievementMask;
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        // The real child controls supply the spoken information and focus order.
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        for(int i=0;i<sites.length;i++){
            final int index=i;
            Button b=new Button(context);
            b.setId(View.generateViewId());
            b.setAllCaps(false);
            b.setTextSize(12f);
            b.setTextColor(INK);
            b.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
            b.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            b.setIncludeFontPadding(false);
            b.setMaxLines(3);
            b.setEllipsize(TextUtils.TruncateAt.END);
            b.setMinWidth(0);b.setMinimumWidth(0);
            b.setMinHeight(0);b.setMinimumHeight(Math.round(PlaylumiCityLayout.MIN_TARGET*density));
            b.setPadding(Math.round(4*density),0,Math.round(4*density),Math.round(11*density));
            b.setBackground(siteFeedback());
            b.setStateListAnimator(null);
            b.setOnClickListener(v->{if(listener!=null)listener.onLandmark(index);});
            sites[i]=b;
            addView(b);
        }
        updateSites();
    }

    private android.graphics.drawable.Drawable siteFeedback(){
        GradientDrawable normal=new GradientDrawable();
        normal.setColor(Color.TRANSPARENT);normal.setCornerRadius(22*density);
        GradientDrawable focused=new GradientDrawable();
        focused.setColor(0x18fff2c1);focused.setCornerRadius(22*density);
        focused.setStroke(Math.max(2,Math.round(3*density)),GOLD);
        StateListDrawable state=new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_focused},focused);
        state.addState(new int[]{},normal);
        if(reducedMotion)return state;
        GradientDrawable mask=new GradientDrawable();
        mask.setColor(Color.WHITE);mask.setCornerRadius(22*density);
        return new RippleDrawable(ColorStateList.valueOf(0x30ffe9a1),state,mask);
    }

    public void setAchievementMask(long achievementMask){
        if(achievements==achievementMask)return;
        achievements=achievementMask;updateSites();invalidate();
    }

    private void updateSites(){
        for(int i=0;i<sites.length;i++){
            PlaylumiCityRewards.Landmark landmark=PlaylumiCityRewards.ALL[i];
            boolean unlocked=PlaylumiCityRewards.earned(achievements,landmark);
            sites[i].setText(landmark.name+"\n"+(unlocked?"Vrijgespeeld":"Nog te ontdekken"));
            sites[i].setContentDescription(landmark.name+(unlocked?
                    ", vrijgespeeld. Bekijk deze plek en je cadeau: "+landmark.gift()+".":
                    ", nog te ontdekken. Speel vrij met: "+landmark.requirement()+"."));
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int suggested=Math.round(380*density);
        int width=resolveSize(suggested,widthMeasureSpec);
        if(MeasureSpec.getMode(widthMeasureSpec)!=MeasureSpec.UNSPECIFIED)
            width=MeasureSpec.getSize(widthMeasureSpec);
        geometry=PlaylumiCityLayout.forWidth(width/density,
                getResources().getConfiguration().fontScale);
        int desiredHeight=Math.round(geometry.height*density);
        setMeasuredDimension(width,resolveSize(desiredHeight,heightMeasureSpec));
        for(int i=0;i<sites.length;i++){
            PlaylumiCityLayout.Plot q=geometry.plot(i);
            sites[i].measure(MeasureSpec.makeMeasureSpec(Math.round(q.width*density),MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(Math.round(q.height*density),MeasureSpec.EXACTLY));
        }
    }

    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
        if(geometry==null)return;
        for(int i=0;i<sites.length;i++){
            PlaylumiCityLayout.Plot q=geometry.plot(i);
            int x=Math.round(q.left*density),y=Math.round(q.top*density);
            sites[i].layout(x,y,x+sites[i].getMeasuredWidth(),y+sites[i].getMeasuredHeight());
        }
    }

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(geometry==null)return;
        canvas.save();canvas.scale(density,density);
        float width=geometry.width,height=getHeight()/density;
        path.reset();rect.set(0,0,width,height);
        path.addRoundRect(rect,24,24,Path.Direction.CW);canvas.clipPath(path);
        paint(0xff63468b);
        p.setShader(new LinearGradient(0,0,width,height,
                new int[]{0xff7568af,0xff546a91,0xff3b6d78,0xff4c665c},
                new float[]{0,.25f,.65f,1},Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,width,height,p);p.setShader(null);
        scenery(canvas,width,height);
        roads(canvas);
        for(int i=0;i<sites.length;i++){
            PlaylumiCityLayout.Plot q=geometry.plot(i);
            boolean unlocked=PlaylumiCityRewards.earned(achievements,PlaylumiCityRewards.ALL[i]);
            float scale=Math.min(q.width/220f,q.artHeight/190f);
            canvas.save();canvas.translate(q.centerX()-110*scale,q.top+q.artHeight-190*scale);
            canvas.scale(scale,scale);
            island(canvas,i,unlocked);
            if(unlocked){
                switch(i){
                    case 0:friendPark(canvas);break;
                    case 1:observatory(canvas);break;
                    case 2:library(canvas);break;
                    case 3:stage(canvas);break;
                    case 4:animalPark(canvas);break;
                    case 5:bridge(canvas);break;
                    default:monument(canvas);break;
                }
                badge(canvas,177,140);
            }else{
                emptyPlot(canvas,i);
            }
            canvas.restore();
            nameplate(canvas,q,unlocked);
        }
        canvas.restore();
        // No continuous animation: reduced-motion users get exactly the same
        // readable scene and the map never schedules background redraws.
    }

    private void scenery(Canvas c,float w,float h){
        // Depth comes from layered hills, a winding stream, rim lighting and
        // small fixed highlights; none of these decorations represents progress.
        paint(0xff7687a1);path.reset();path.moveTo(0,56);
        path.cubicTo(w*.20f,15,w*.30f,94,w*.58f,48);
        path.cubicTo(w*.78f,13,w*.86f,36,w,22);path.lineTo(w,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        paint(0xff6b927d);path.reset();path.moveTo(0,100);
        path.cubicTo(w*.20f,52,w*.30f,130,w*.57f,96);
        path.cubicTo(w*.80f,66,w*.9f,104,w,92);path.lineTo(w,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        paint(0xff4a786b);path.reset();path.moveTo(0,h*.47f);
        path.cubicTo(w*.25f,h*.35f,w*.48f,h*.48f,w,h*.33f);
        path.lineTo(w,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        paint(0xff41675e);path.reset();path.moveTo(0,h*.83f);
        path.cubicTo(w*.23f,h*.70f,w*.65f,h*.87f,w,h*.74f);
        path.lineTo(w,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        path.reset();path.moveTo(w*.88f,-20);
        path.cubicTo(w*.72f,h*.23f,w*.91f,h*.42f,w*.73f,h*.61f);
        path.cubicTo(w*.63f,h*.78f,w*.81f,h*.88f,w*.66f,h+30);
        stroke(0x7773dbdd,18);c.drawPath(path,p);
        stroke(0xff8cd0d2,11);c.drawPath(path,p);
        stroke(0x66ecffff,2);c.drawPath(path,p);
        for(int i=0;i<26;i++){
            float x=8+(i*73%997)/997f*(w-16),y=18+(i*139%971)/971f*(h-36);
            ellipse(c,x-4,y-1,x+4,y+1,0x32b6e7ae);
            if(i%4==0)star(c,x,y,2.5f,0xc9ffffc3);
        }
        // Small atmospheric lights are static, including reduced-motion mode.
        for(int i=0;i<5;i++){
            float x=(i+1)*w/6f,y=11+(i%2)*8;
            star(c,x,y,i%2==0?2.8f:1.6f,0xdffff2d2);
        }
    }

    private void roads(Canvas c){
        if(geometry==null)return;
        path.reset();
        PlaylumiCityLayout.Plot first=geometry.plot(0);
        path.moveTo(first.centerX(),first.groundY());
        for(int i=1;i<sites.length;i++){
            PlaylumiCityLayout.Plot a=geometry.plot(i-1),b=geometry.plot(i);
            float mid=(a.groundY()+b.groundY())/2f;
            path.cubicTo(a.centerX(),mid,b.centerX(),mid,b.centerX(),b.groundY());
        }
        stroke(0x764b5446,15);c.drawPath(path,p);
        stroke(0xffd8c5a3,10);c.drawPath(path,p);
        stroke(0xffeddec0,4);c.drawPath(path,p);
    }

    private void island(Canvas c,int index,boolean unlocked){
        ellipse(c,12,156,209,188,0x48323738);
        gradientOval(c,12,140,208,182,0xffb6a37b,0xff6c7453);
        gradientOval(c,10,132,210,173,unlocked?0xffcae99d:0xffadc8a3,0xff749b6e);
        stroke(0xffd6e8b1,2);rect.set(16,135,205,168);c.drawArc(rect,190,125,false,p);
        ellipse(c,48,145,171,169,0x28646243);
        for(int i=0;i<10;i++){
            float x=20+(i*43+index*17)%177,y=146+(i*11)%14;
            ellipse(c,x-2,y,x+2,y+1.3f,0x70f7f1bb);
        }
    }

    private void emptyPlot(Canvas c,int index){
        // A planted clearing and a sign, never a ghosted finished building.
        gradientOval(c,50,116,174,153,0xffc4c2a0,0xff929d7c);
        for(int i=0;i<8;i++){
            float angle=(float)(i*Math.PI/4),x=112+(float)Math.cos(angle)*60,y=135+(float)Math.sin(angle)*16;
            gradientOval(c,x-5,y-3,x+5,y+3,0xfff3e5c5,0xffb1a88e);
        }
        tree(c,35,125,.50f,0xff8aa993,0xff527f74);
        tree(c,187,115,.43f,0xff93baa2,0xff608a7a);
        round(c,105,100,112,141,3,0xff9a7956);
        gradientRound(c,76,72,144,115,9,0xfff4e7c5,0xffd6c39d);
        stroke(0xffaa8e66,2);rect.set(77,73,143,114);c.drawRoundRect(rect,8,8,p);
        // A keyhole distinguishes a future site even without reading the label.
        circle(c,110,88,6,0xff78696a);
        polygon(c,0xff78696a,108,90,112,90,116,103,104,103);
        flower(c,53,151,0xffe3c1e0,.65f);
        flower(c,168,146,0xffead5a3,.50f);
        ellipse(c,79,146,87,150,0xffe9e6c8);
    }

    private void friendPark(Canvas c){
        tree(c,36,131,.78f,0xff96cf9e,0xff3f8d79);
        tree(c,185,124,.67f,0xffb7d58b,0xff699d68);
        // Curved rose arch with a small tiled footpath.
        stroke(0xff7862a0,8);rect.set(64,50,154,144);c.drawArc(rect,180,180,false,p);
        line(c,64,97,64,150,7,0xff7862a0);line(c,154,97,154,150,7,0xff7862a0);
        stroke(0xffe5c6f6,3);rect.set(64,50,154,144);c.drawArc(rect,180,180,false,p);
        for(int i=0;i<9;i++){
            double a=Math.PI+(i/8f)*Math.PI;
            float x=109+(float)Math.cos(a)*45,y=97+(float)Math.sin(a)*47;
            leaf(c,x,y,.78f);flower(c,x+3,y-3,i%2==0?0xffffb6d5:0xffffe0a4,.62f);
        }
        for(int i=0;i<3;i++)gradientOval(c,83-i*4,131+i*8,134+i*4,139+i*8,0xfff9eac8,0xffc9b78f);
        bench(c,155,135,.62f);
        butterfly(c,111,88,1.15f,0xffedd0ff);
        butterfly(c,53,91,.55f,0xffffd899);
        flower(c,41,151,0xffffb8db,.8f);flower(c,176,156,0xfff7c681,.7f);
    }

    private void observatory(Canvas c){
        tree(c,31,133,.50f,0xff8db8c1,0xff4f8b92);
        ellipse(c,55,145,174,167,0x544d5a69);
        gradientRound(c,64,89,166,154,12,0xfff6e9c4,0xffc8aa85);
        polygon(c,0xffb48d7d,143,93,166,89,166,146,143,155);
        // Raised dome, meridian seams and an offset telescope.
        gradientOval(c,60,49,169,116,0xffb4e9ef,0xff5c86b8);
        polygon(c,0xff84bdcc,60,84,169,84,169,106,60,106);
        ellipse(c,60,97,169,114,0xff5c75a5);
        line(c,66,102,164,102,4,0xffdae8df);
        stroke(0xffd4f5f0,2.5f);rect.set(80,49,149,109);c.drawArc(rect,182,175,false,p);
        line(c,116,52,116,99,2,0xffb2d8e6);
        round(c,109,34,124,65,4,0xffdfd5bd);
        c.save();c.rotate(-28,122,44);
        gradientRound(c,111,35,155,53,4,0xfffcdf8c,0xffb4935c);
        round(c,150,33,160,55,3,0xffd4e7ef);ellipse(c,153,36,161,52,0xff405c94);c.restore();
        domeDoor(c,99,119,24,37,0xff626299);
        window(c,74,120,15,19);window(c,139,120,14,18);
        star(c,118,78,9,0xffffe398);
        step(c,90,154,43);
        flower(c,183,148,0xffb6e8f5,.65f);
    }

    private void library(Canvas c){
        tree(c,32,131,.62f,0xffc49cd6,0xff86639f);
        ellipse(c,48,149,176,171,0x47494067);
        gradientRound(c,55,86,157,155,9,0xfff9e3bf,0xffe0b18f);
        polygon(c,0xffc69b99,145,87,170,99,170,155,145,163);
        polygon(c,0xff8b609f,43,91,108,49,174,91,150,103,108,71,65,102);
        polygon(c,0xffd5a4df,44,90,108,48,108,60,58,97);
        line(c,65,99,108,72,3,0xffefd1f5);line(c,108,72,151,100,3,0xffd4b1df);
        gradientRound(c,121,49,151,105,5,0xffe4bbd2,0xffb688b9);
        polygon(c,0xff845aa4,113,55,136,20,160,55);
        line(c,118,55,156,55,4,0xffe2b9e6);
        star(c,136,36,5,0xffffdf9e);
        window(c,129,66,13,21);
        domeDoor(c,93,121,27,36,0xff786296);
        window(c,65,118,17,23);window(c,131,121,15,22);
        // Book-shaped sign on the facade.
        polygon(c,0xffffefc5,86,88,104,91,104,108,86,104);
        polygon(c,0xfffae2a9,104,91,122,88,122,104,104,108);
        line(c,104,92,104,106,1.5f,0xffbe9073);
        step(c,84,156,45);
        books(c,160,142,.65f);flower(c,39,153,0xffffd3ed,.65f);
    }

    private void stage(Canvas c){
        tree(c,30,130,.50f,0xffb7b4d8,0xff7c759e);
        ellipse(c,45,149,180,174,0x50433c59);
        gradientOval(c,44,133,178,170,0xffa476aa,0xff644a7d);
        gradientOval(c,44,126,178,157,0xffffdfb2,0xffddb187);
        gradientRound(c,60,62,163,137,9,0xff71417b,0xff392d60);
        polygon(c,0xffbe789c,54,65,111,30,169,65);
        polygon(c,0xffe7afc4,54,65,111,30,111,43,67,72);
        line(c,53,65,170,65,5,0xffffd28a);
        for(int i=0;i<4;i++)round(c,61+i*6,69,69+i*5,136-i*5,4,i%2==0?0xffe08dad:0xffb96599);
        for(int i=0;i<4;i++)round(c,141+i*6,69,150+i*4,119+i*5,4,i%2==0?0xffd47fa8:0xffad548a);
        line(c,58,67,58,140,5,0xffe9c392);line(c,165,67,165,140,5,0xffe9c392);
        circle(c,111,85,20,0x25ffdaa2);
        musicNote(c,103,95,.90f,0xffffe7a7);musicNote(c,127,111,.60f,0xffc2def4);
        star(c,111,49,8,0xffffe29e);
        for(int i=0;i<5;i++)circle(c,62+i*24,146,2.5f,0xffffefbe);
        step(c,84,157,57);flower(c,188,149,0xffefbbdc,.65f);
    }

    private void animalPark(Canvas c){
        tree(c,36,121,.87f,0xffc7d595,0xff83a769);
        tree(c,186,119,.83f,0xffa1d4a1,0xff4c9274);
        fence(c,48,137,118);
        gradientRound(c,60,86,77,147,5,0xffefe0ba,0xffc5b38d);
        gradientRound(c,149,86,166,147,5,0xffeee1bd,0xffc2ae88);
        gradientRound(c,54,65,172,93,8,0xffb1ba8b,0xff7f976c);
        line(c,58,68,169,68,3,0xffd9dba7);
        paw(c,112,79,.50f,0xfff8e6b4);
        gradientOval(c,81,147,150,162,0xffe8d8ae,0xffc6b291);
        // A small sculpted fox ornament, not a second player avatar.
        ellipse(c,96,128,140,151,0xffd88f5f);
        polygon(c,0xffdf9b6c,99,132,95,108,109,117,128,114,140,103,139,130);
        polygon(c,0xffffe2ba,103,129,116,135,135,123,132,140,116,146,105,138);
        circle(c,110,126,2,0xff654451);circle(c,129,123,2,0xff654451);
        circle(c,119,136,2.8f,0xff654451);
        flower(c,52,155,0xfff8dc94,.75f);flower(c,170,153,0xfff1bdcb,.65f);
    }

    private void bridge(Canvas c){
        tree(c,29,125,.55f,0xffb7d3b3,0xff649c8a);
        // The stream passes through this island, beneath the earned bridge.
        gradientOval(c,15,129,208,166,0xffbce6ed,0xff6bbbc9);
        for(int i=0;i<4;i++)line(c,30+i*42,145+(i%2)*6,48+i*42,145+(i%2)*6,1.5f,0xffd6f6ef);
        gradientRound(c,46,114,62,157,3,0xffe3d3bd,0xffbda9a5);
        gradientRound(c,167,114,183,157,3,0xffe3d3bd,0xffbda9a5);
        path.reset();path.moveTo(51,134);path.cubicTo(79,92,142,92,178,133);
        stroke(0xffb3a3b6,14);c.drawPath(path,p);
        stroke(0xfff5d8b6,9);c.drawPath(path,p);
        for(int i=0;i<9;i++){
            float x=56+i*14.4f,u=(x-116)/63f,y=104+28*u*u;
            line(c,x,y-2,x+2,y+7,1.5f,0xffbe9b8e);
        }
        // Seven coloured rails form an actual traversable arch, not a badge.
        int[] colors={0xffe3a2c8,0xffe5b69c,0xfff4d98d,0xffb6d797,0xff8fcdd3,0xffa8b4e3,0xffc9a9df};
        for(int i=0;i<colors.length;i++){
            path.reset();path.moveTo(48,123-i*2);
            path.cubicTo(74,36+i*3,151,36+i*3,182,123-i*2);
            stroke(colors[i],4.5f);c.drawPath(path,p);
        }
        for(int i=0;i<5;i++){
            float x=54+i*30,u=(x-114)/61f,y=56+49*u*u;
            line(c,x,y+15,x,108+25*u*u,2.5f,0xffe8d2bc);
        }
        circle(c,48,116,4,0xffffeac1);circle(c,182,116,4,0xffffeac1);
        flower(c,31,153,0xffecc0dd,.7f);flower(c,194,149,0xfff5d499,.65f);
    }

    private void monument(Canvas c){
        tree(c,34,128,.65f,0xffd6c994,0xff99956a);
        tree(c,188,124,.58f,0xffacc7b1,0xff688f7e);
        ellipse(c,63,149,161,169,0x66595561);
        gradientOval(c,57,138,164,162,0xfff1e2bd,0xffbdb09b);
        gradientRound(c,75,122,147,150,4,0xfff7e6c2,0xffbcaaa1);
        gradientRound(c,85,101,137,129,5,0xffe9d6b0,0xffc9b7a2);
        round(c,102,111,121,122,2,0xffb19880);
        // An inert, faceless golden sculpture. Lumi remains the only hero.
        glow(c,110,71,45,0x6dfff3bd);
        star(c,111,72,37,0xffc39458);
        c.save();c.translate(-2,-4);star(c,111,72,36,0xffffd989);c.restore();
        polygon(c,0xffffedbc,109,32,109,68,75,57,96,53);
        polygon(c,0xffe3af67,109,68,131,99,125,74,145,57);
        star(c,69,92,3.5f,0xffffe6aa);star(c,158,55,4,0xffffe8b5);
        flower(c,52,151,0xffffd899,.70f);flower(c,174,151,0xffedc4df,.65f);
    }

    private void nameplate(Canvas c,PlaylumiCityLayout.Plot q,boolean unlocked){
        float l=q.left+2,t=q.top+q.artHeight+1,r=q.right()-2,b=q.bottom()-3;
        round(c,l,t+3,r,b+3,16,0x4e37453b);
        gradientRound(c,l,t,r,b,16,unlocked?0xfffff5d8:0xffe8ecd9,unlocked?0xffefdab4:0xffd5ddc8);
        stroke(unlocked?0xffe6be72:0xffa9b49b,1.2f);rect.set(l+.7f,t+.7f,r-.7f,b-.7f);c.drawRoundRect(rect,15,15,p);
        line(c,l+16,t+3,r-16,t+3,1,0xafffffff);
    }

    private void tree(Canvas c,float x,float y,float s,int light,int dark){
        c.save();c.translate(x,y);c.scale(s,s);
        ellipse(c,-25,-3,30,11,0x343a4f45);
        gradientRound(c,-5,-41,5,6,3,0xffcba77d,0xff8f725c);
        line(c,0,-28,-15,-47,4,0xff9d7d63);line(c,0,-23,16,-44,3,0xff9d7d63);
        gradientOval(c,-32,-75,31,-20,light,dark);
        gradientOval(c,-22,-91,18,-43,light,dark);
        gradientOval(c,-38,-60,-4,-21,light,dark);
        gradientOval(c,3,-64,37,-21,light,dark);
        ellipse(c,-21,-74,-9,-66,0x46ffffd7);ellipse(c,3,-52,13,-44,0x37ffffd8);
        c.restore();
    }
    private void flower(Canvas c,float x,float y,int color,float scale){
        line(c,x,y-6*scale,x,y+2*scale,1.5f*scale,0xff698d5a);
        for(int i=0;i<5;i++){
            double a=i*Math.PI*2/5;
            circle(c,x+(float)Math.cos(a)*3.4f*scale,y-7*scale+(float)Math.sin(a)*3.4f*scale,2.8f*scale,color);
        }
        circle(c,x,y-7*scale,2*scale,0xffffe6a4);
    }
    private void leaf(Canvas c,float x,float y,float s){
        ellipse(c,x-7*s,y-2*s,x+2*s,y+3*s,0xff79ac76);
        ellipse(c,x+1*s,y-5*s,x+9*s,y,0xffa5c985);
    }
    private void butterfly(Canvas c,float x,float y,float s,int color){
        c.save();c.translate(x,y);c.rotate(-12);c.scale(s,s);
        gradientOval(c,-15,-12,-1,4,0xfffff3df,color);
        gradientOval(c,1,-12,15,4,0xfffff3df,color);
        ellipse(c,-11,0,-1,10,color);ellipse(c,1,0,11,10,color);
        line(c,0,-6,0,9,3,0xff765c86);
        line(c,-1,-7,-4,-13,1,0xff765c86);line(c,1,-7,4,-13,1,0xff765c86);c.restore();
    }
    private void bench(Canvas c,float x,float y,float s){
        c.save();c.translate(x,y);c.scale(s,s);
        line(c,-18,0,-18,15,3,0xff776f75);line(c,20,0,20,15,3,0xff776f75);
        round(c,-24,-9,26,0,3,0xffecc393);round(c,-24,3,26,9,2,0xffe0b384);c.restore();
    }
    private void fence(Canvas c,float x,float y,float w){
        line(c,x,y-10,x+w,y-10,4,0xffdec897);line(c,x,y,x+w,y,3,0xffbbab84);
        for(int i=0;i<=8;i++)round(c,x+i*w/8-2,y-20,x+i*w/8+2,y+5,2,0xffebd5a4);
    }
    private void window(Canvas c,float x,float y,float w,float h){
        gradientRound(c,x-2,y-2,x+w+2,y+h+2,4,0xffe9c394,0xffb58e79);
        gradientRound(c,x,y,x+w,y+h,3,0xffffeeb3,0xffdcad74);
        line(c,x+w/2,y,x+w/2,y+h,1.6f,0xffb38f7c);
        line(c,x,y+h*.45f,x+w,y+h*.45f,1.6f,0xffb38f7c);
    }
    private void domeDoor(Canvas c,float x,float y,float w,float h,int color){
        gradientRound(c,x-3,y-3,x+w+3,y+h+1,w/2,0xfff6dbaa,0xffc8a68f);
        gradientRound(c,x,y,x+w,y+h,w/2,color,0xff4d4268);
        round(c,x,y+h/2,x+w,y+h,0,0xff685778);
        line(c,x+w/2,y+5,x+w/2,y+h,1,0xff3f3c5e);
        circle(c,x+w*.70f,y+h*.70f,1.7f,0xffffd58b);
    }
    private void step(Canvas c,float x,float y,float w){
        gradientRound(c,x,y,x+w,y+5,2,0xfff8e5be,0xffcbb799);
        gradientRound(c,x-4,y+4,x+w+4,y+9,2,0xfff1deba,0xffbdaa94);
    }
    private void books(Canvas c,float x,float y,float s){
        c.save();c.translate(x,y);c.scale(s,s);
        round(c,-12,-4,14,2,2,0xffd49ac2);round(c,-10,-10,16,-5,2,0xffb8cfb5);
        round(c,-14,-16,13,-11,2,0xffe7c08d);c.restore();
    }
    private void paw(Canvas c,float x,float y,float s,int color){
        ellipse(c,x-9*s,y-1*s,x+9*s,y+9*s,color);
        circle(c,x-10*s,y-6*s,4*s,color);circle(c,x-3*s,y-11*s,4*s,color);
        circle(c,x+5*s,y-10*s,4*s,color);circle(c,x+12*s,y-4*s,4*s,color);
    }
    private void musicNote(Canvas c,float x,float y,float s,int color){
        ellipse(c,x-8*s,y-2*s,x+4*s,y+5*s,color);
        line(c,x+3*s,y,x+3*s,y-24*s,3*s,color);
        path.reset();path.moveTo(x+3*s,y-24*s);path.cubicTo(x+17*s,y-17*s,x+13*s,y-10*s,x+6*s,y-10*s);
        stroke(color,3*s);c.drawPath(path,p);
    }
    private void badge(Canvas c,float x,float y){
        circle(c,x,y,13,0xffe5bc75);circle(c,x,y-1,10.5f,0xffffe4a7);
        line(c,x-5,y,x-1,y+4,2.6f,0xff697a5b);line(c,x-1,y+4,x+6,y-5,2.6f,0xff697a5b);
    }
    private void glow(Canvas c,float x,float y,float radius,int color){
        paint(color);p.setShader(new RadialGradient(x,y,radius,new int[]{color,color&0x00ffffff},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,radius,p);p.setShader(null);
    }
    private void star(Canvas c,float x,float y,float radius,int color){
        path.reset();
        for(int i=0;i<10;i++){
            double a=-Math.PI/2+i*Math.PI/5;float r=i%2==0?radius:radius*.46f;
            float px=x+(float)Math.cos(a)*r,py=y+(float)Math.sin(a)*r;
            if(i==0)path.moveTo(px,py);else path.lineTo(px,py);
        }
        path.close();paint(color);c.drawPath(path,p);
    }
    private void paint(int color){
        p.reset();p.setAntiAlias(true);p.setColor(color);
        p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
    }
    private void stroke(int color,float width){paint(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);}
    private void line(Canvas c,float x1,float y1,float x2,float y2,float width,int color){stroke(color,width);c.drawLine(x1,y1,x2,y2,p);}
    private void circle(Canvas c,float x,float y,float radius,int color){paint(color);c.drawCircle(x,y,radius,p);}
    private void ellipse(Canvas c,float l,float t,float r,float b,int color){paint(color);rect.set(l,t,r,b);c.drawOval(rect,p);}
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color){paint(color);rect.set(l,t,r,b);c.drawRoundRect(rect,radius,radius,p);}
    private void gradientOval(Canvas c,float l,float t,float r,float b,int top,int bottom){
        paint(top);p.setShader(new LinearGradient(l,t,r,b,top,bottom,Shader.TileMode.CLAMP));
        rect.set(l,t,r,b);c.drawOval(rect,p);p.setShader(null);
    }
    private void gradientRound(Canvas c,float l,float t,float r,float b,float radius,int top,int bottom){
        paint(top);p.setShader(new LinearGradient(l,t,r,b,top,bottom,Shader.TileMode.CLAMP));
        rect.set(l,t,r,b);c.drawRoundRect(rect,radius,radius,p);p.setShader(null);
    }
    private void polygon(Canvas c,int color,float... points){
        path.reset();path.moveTo(points[0],points[1]);
        for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);
        path.close();paint(color);c.drawPath(path,p);
    }
}
