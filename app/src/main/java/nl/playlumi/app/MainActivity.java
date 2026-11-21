package nl.playlumi.app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;
import java.util.Random;

/** One choice per screen, a separate learning world menu, and native Android play motion. */
public class MainActivity extends Activity {
    private static final int AGE=0, HOME=1, CATEGORIES=2, DETAIL=3, LEVEL=4,
            VARIANTS=5, PLAY=6, BADGES=7, REWARD=8, AVATAR=9, CLOSET=10, SHOP=11;
    private static final int SESSION_ROUNDS=200;
    private static final int TEXT=0xff3c315d, SUB=0xff6a6390;
    private static final int[] COLORS={0xffd9f3ff,0xffffe4bd,0xffe8d9ff,
            0xffd5f5df,0xffffdded,0xfffff1b6};
    private static final String[] ICONS={"⭐","🎨","🔤","🔷","🔢","🧩","🐾",
            "✏️","🔠","🖼️","🎶","⚖️","➕","🔢","↔️","🧠",
            "🕒","🌦️","🧭","🏠","🥕","🦁","📏","💡"};
    private static final String[] WORLDS={"Ontdekken","Taal & denken","Onze wereld"};
    private static final String[] WORLD_ICONS={"🌈","🔤","🌍"};
    private static final String[] WORLD_DESCS={"Tellen, kleuren en vormen",
            "Woorden, rijmen en puzzels","Dieren, natuur en het dagelijks leven"};
    private static final String[] AGES={"3–4 jaar","5–6 jaar","7+ jaar"};
    private static final String[] LEVELS={"Makkelijk","Gemiddeld","Uitdagend"};

    private final ArrayList<Button> answerButtons=new ArrayList<>();
    private final ArrayList<Integer> pendingBadges=new ArrayList<>();
    private int page,age,world,categoryPage,variantPage,badgePage,difficulty;
    private int closetPage,shopPage,selectedSlot;
    private String shopMessage="";
    private int sessionSolved,sessionAttempts,earnedBadge=-1,lifetimeCount;
    private boolean moving,answerLocked;
    private LearningGame game;
    private QuestionHistory recent;
    private PlaylumiScores scores;
    private PlaylumiWardrobe wardrobe;
    private PlaylumiCharacterView character;
    private LinearLayout root;
    private FrameLayout stage;
    private ScrollView scroller;
    private TextView feedbackText;
    private ProgressBar roundProgress;
    private Button nextButton;
    private String feedback="";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xffeaf5ff);
        getWindow().setNavigationBarColor(0xfffff6e4);
        int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        SharedPreferences prefs=getPreferences(MODE_PRIVATE);
        age=prefs.getInt("chosen_age",-1);
        lifetimeCount=prefs.getInt("completed_all",0);
        int mode=prefs.getInt("game_mode",0);
        int variant=prefs.getInt("game_variant",0);
        difficulty=Math.max(0,age);
        world=Math.max(0,Math.min(2,mode/8));
        page=age<0?AGE:HOME;
        if(state!=null) {
            age=state.getInt("age",age);
            mode=state.getInt("mode",mode);
            variant=state.getInt("variant",variant);
            difficulty=state.getInt("difficulty",difficulty);
            world=state.getInt("world",world);
            categoryPage=state.getInt("categoryPage",0);
            variantPage=state.getInt("variantPage",0);
            badgePage=state.getInt("badgePage",0);
            closetPage=state.getInt("closetPage",0);
            shopPage=state.getInt("shopPage",0);
            selectedSlot=state.getInt("selectedSlot",0);
            sessionSolved=state.getInt("sessionSolved",0);
            sessionAttempts=state.getInt("sessionAttempts",0);
            page=state.getInt("page",page);
            earnedBadge=state.getInt("earnedBadge",-1);
        }
        scores=new PlaylumiScores();
        for(int i=0;i<PlaylumiScores.CATEGORIES;i++) {
            String prefix="score_"+i+"_";
            scores.restore(i,prefs.getInt(prefix+"good",0),prefs.getInt(prefix+"tries",0),
                    prefs.getInt(prefix+"best",0),prefs.getInt(prefix+"streak",0),
                    prefs.getInt(prefix+"sessions",0));
        }
        int starter=PlaylumiWardrobe.STARTER_STARS+Math.min(90,Math.max(0,lifetimeCount)*2);
        wardrobe=new PlaylumiWardrobe(starter);
        if(prefs.getBoolean("wardrobe_initialized",false)) {
            wardrobe.restoreBalance(prefs.getInt("wardrobe_stars",starter),
                    prefs.getInt("wardrobe_total_earned",starter));
            for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)
                wardrobe.restoreSlot(i,prefs.getInt("wardrobe_owned_"+i,1),
                        prefs.getInt("wardrobe_worn_"+i,0));
        } else saveWardrobe();
        recent=new QuestionHistory(prefs.getString("recent_questions",""));
        game=new LearningGame(new Random());
        game.setHistory(recent);
        game.configure(Math.max(0,Math.min(LearningGame.GAME_NAMES.length-1,mode)),
                Math.max(0,Math.min(4,variant)), Math.max(0,Math.min(2,difficulty)));
        showPage();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("age",age);out.putInt("mode",game.mode());out.putInt("variant",game.variant());
        out.putInt("difficulty",difficulty);out.putInt("world",world);
        out.putInt("categoryPage",categoryPage);out.putInt("variantPage",variantPage);
        out.putInt("badgePage",badgePage);out.putInt("sessionSolved",sessionSolved);
        out.putInt("sessionAttempts",sessionAttempts);out.putInt("page",page);
        out.putInt("earnedBadge",earnedBadge);
        out.putInt("closetPage",closetPage);
        out.putInt("shopPage",shopPage);
        out.putInt("selectedSlot",selectedSlot);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if(page==AGE || page==HOME) { super.onBackPressed(); return; }
        if(page==REWARD) { continueAfterBadge();return; }
        if(page==AVATAR) {move(HOME);return;}
        if(page==CLOSET) {move(AVATAR);return;}
        if(page==SHOP) {move(CLOSET);return;}
        int back=page==CATEGORIES?HOME:page==DETAIL?CATEGORIES:page==LEVEL?DETAIL:
                page==VARIANTS?LEVEL:page==PLAY?VARIANTS:DETAIL;
        move(back);
    }
    private SharedPreferences prefs() {return getPreferences(MODE_PRIVATE);}
    private void saveRecent() { prefs().edit().putString("recent_questions",recent.serialize()).apply(); }
    private void saveScore(int mode) {
        PlaylumiScores.Score s=scores.get(mode);
        String prefix="score_"+mode+"_";
        prefs().edit().putInt(prefix+"good",s.correct).putInt(prefix+"tries",s.attempts)
                .putInt(prefix+"best",s.bestStreak).putInt(prefix+"streak",s.currentStreak)
                .putInt(prefix+"sessions",s.completedSessions)
                .putInt("completed_all",lifetimeCount).apply();
    }
    private void saveWardrobe() {
        SharedPreferences.Editor edit=prefs().edit()
            .putBoolean("wardrobe_initialized",true)
            .putInt("wardrobe_stars",wardrobe.stars())
            .putInt("wardrobe_total_earned",wardrobe.allTimeStars());
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++) {
            edit.putInt("wardrobe_owned_"+i,wardrobe.ownedMask(i));
            edit.putInt("wardrobe_worn_"+i,wardrobe.equipped(i));
        }
        edit.apply();
    }
    private int dp(int x) {return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable rounded(int color,int radius) {
        GradientDrawable b=new GradientDrawable();
        b.setColor(color);b.setCornerRadius(dp(radius));return b;
    }
    private TextView text(String value,int size,int color) {
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(true);
        t.setLineSpacing(dp(2),1.06f);return t;
    }
    private void addText(String value,int size,int color,int top,int bottom) {
        TextView t=text(value,size,color);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(top),0,dp(bottom));root.addView(t,p);
    }
    private Button tile(String title,String description,int color,int minHeight) {
        Button b=new Button(this);
        b.setAllCaps(false);
        b.setText(PlaylumiType.tileText(title,description));
        b.setTextColor(TEXT);
        b.setTextSize(description.isEmpty()?20:18);
        b.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        b.setLineSpacing(dp(3),1.04f);
        b.setGravity(Gravity.CENTER);
        b.setMaxLines(5);
        b.setPadding(dp(13),dp(16),dp(13),dp(16));
        b.setMinHeight(dp(minHeight));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x35ffffff),rounded(color,25),null));
        b.setElevation(dp(4));
        b.setContentDescription((title+" "+description).trim());
        PlaylumiMotion.tactile(b);
        return b;
    }
    private void addTile(Button button,int margin) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(dp(4),dp(margin),dp(4),dp(margin));
        root.addView(button,p);
    }
    private void addTwo(Button one,Button two,int height) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(0,-2,1);
        LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,-2,1);
        p1.setMargins(dp(5),dp(7),dp(5),dp(7));
        p2.setMargins(dp(5),dp(7),dp(5),dp(7));
        one.setMinHeight(dp(height));
        two.setMinHeight(dp(height));
        row.addView(one,p1);row.addView(two,p2);
        root.addView(row);
    }
    private ImageView mascot(int size) {
        ImageView m=new ImageView(this);
        m.setImageResource(R.drawable.playlumi_icon);
        m.setScaleType(ImageView.ScaleType.FIT_CENTER);
        m.setContentDescription("Playlumi, het vrolijke sterretje");
        m.setLayoutParams(new LinearLayout.LayoutParams(dp(size),dp(size)));
        return m;
    }
    private void hero(int size) {
        LinearLayout holder=new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        ImageView star=mascot(size);
        holder.addView(star);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(size+8));
        p.setMargins(0,dp(2),0,dp(6));
        root.addView(holder,p);
        PlaylumiMotion.floatMascot(star);
    }
    private void characterHero(int size) {
        LinearLayout holder=new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        character=new PlaylumiCharacterView(this,wardrobe);
        character.setContentDescription("Jouw stervriendje. Tik voor een magische verrassing.");
        character.setOnClickListener(v->{
            PlaylumiMotion.pop(v);
            FrameLayout active=stage;
            active.post(()->{
                if(stage==active)PlaylumiMotion.celebrate(active,1);
            });
        });
        holder.addView(character,new LinearLayout.LayoutParams(dp(size),dp(size)));
        LinearLayout.LayoutParams row=new LinearLayout.LayoutParams(-1,dp(size+7));
        row.setMargins(0,dp(4),0,dp(2));
        root.addView(holder,row);
        PlaylumiMotion.floatMascot(character);
    }
    private void header(String heading,String subtitle) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        if(page!=AGE && page!=HOME) {
            Button back=tile("‹","",0xffeee4ff,46);
            back.setTextSize(27);
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(52),dp(52));
            bp.setMargins(0,0,dp(10),0);row.addView(back,bp);
            back.setOnClickListener(v->onBackPressed());
        }
        View icon=wardrobe==null?mascot(49):new PlaylumiCharacterView(this,wardrobe);
        row.addView(icon,new LinearLayout.LayoutParams(dp(49),dp(49)));
        TextView brand=text("Playlumi",23,0xff8351bb);
        LinearLayout.LayoutParams textp=new LinearLayout.LayoutParams(-2,-2);
        textp.setMargins(dp(10),0,0,0);row.addView(brand,textp);
        root.addView(row,new LinearLayout.LayoutParams(-1,dp(58)));
        addText(heading,page==PLAY?25:29,0xff52378b,9,5);
        if(subtitle!=null && !subtitle.isEmpty())addText(subtitle,14,SUB,0,16);
    }
    private void showPage() {
        stage=new FrameLayout(this);
        GradientDrawable bg=new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xffeaf7ff,0xfffff3fb,0xfffff8e6});
        stage.setBackground(bg);
        scroller=new ScrollView(this);scroller.setFillViewport(true);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(16),dp(16),dp(28));
        scroller.addView(root);
        stage.addView(scroller,new FrameLayout.LayoutParams(-1,-1));
        setContentView(stage);
        final LinearLayout currentRoot=root;
        stage.setOnApplyWindowInsetsListener((v,insets)->{
            if(root==currentRoot)root.setPadding(dp(16),dp(16)+insets.getSystemWindowInsetTop(),
                    dp(16),dp(28)+insets.getSystemWindowInsetBottom());
            return insets;
        });
        stage.requestApplyInsets();
        switch(page) {
            case AGE: showAge();break;
            case HOME: showHome();break;
            case CATEGORIES: showCategories();break;
            case DETAIL: showDetail();break;
            case LEVEL: showLevel();break;
            case VARIANTS: showVariants();break;
            case PLAY: showPlay();break;
            case BADGES: showBadges();break;
            case REWARD: showReward();break;
            case AVATAR: showAvatar();break;
            case CLOSET: showCloset();break;
            case SHOP: showShop();break;
            default: throw new IllegalStateException("page");
        }
        stage.setAlpha(0f);stage.setTranslationX(dp(22));
        stage.animate().alpha(1).translationX(0).setDuration(280)
                .setInterpolator(new DecelerateInterpolator()).start();
        root.post(()->{if(root==currentRoot)PlaylumiMotion.reveal(currentRoot);});
    }
    private void move(int next) {
        if(moving)return;
        moving=true;
        FrameLayout previous=stage;
        previous.animate().cancel();
        previous.animate().alpha(0f).translationX(-dp(23)).setDuration(165)
                .withEndAction(()->{
                    if(stage!=previous){moving=false;return;}
                    page=next;moving=false;showPage();
                }).start();
    }
    private void showAge() {
        header("🎈 Hoe oud ben jij?","Kies jouw leeftijd. We onthouden dit!");
        hero(105);
        String[] descriptions={"Ontdek vormen en kleuren","Leer woorden en tellen","Meer uitdaging en puzzels"};
        String[] emoji={"🐣","🦋","🚀"};
        for(int i=0;i<3;i++){
            final int selected=i;
            Button b=tile(emoji[i]+"  "+AGES[i],descriptions[i],COLORS[i],97);
            addTile(b,6);
            b.setOnClickListener(v->{
                age=selected;difficulty=selected;
                prefs().edit().putInt("chosen_age",age).apply();
                move(HOME);
            });
        }
    }
    private void showHome() {
        header("🌟 Kies een wereld","Waar wil je vandaag spelen?");
        characterHero(158);
        addText("✨ "+wardrobe.stars()+" speelsterren",19,0xffb87a2e,3,8);
        Button avatarButton=tile("🎁 Mijn stervriend","Kleed je eigen karakter aan",0xffffdfb4,98);
        addTile(avatarButton,4);
        avatarButton.setOnClickListener(v->move(AVATAR));
        for(int i=0;i<3;i++) {
            final int selected=i;
            Button b=tile(WORLD_ICONS[i]+"  "+WORLDS[i],WORLD_DESCS[i],COLORS[i],105);
            addTile(b,5);
            b.setOnClickListener(v->{world=selected;categoryPage=0;move(CATEGORIES);});
        }
        Button change=tile("🎂 Leeftijd: "+AGES[age],"",0xfff1e9ff,54);
        addTile(change,12);
        change.setOnClickListener(v->move(AGE));
    }
    private void showCategories() {
        header(WORLD_ICONS[world]+" "+WORLDS[world],"Kies één spelletje");
        addText(categoryPage==0?"●  ○":"○  ●",16,0xff9466c1,0,14);
        int start=world*8+categoryPage*4;
        for(int row=0;row<2;row++) {
            int left=start+row*2,right=left+1;
            Button one=tile(ICONS[left]+"\n"+LearningGame.GAME_NAMES[left],"",COLORS[left%6],144);
            Button two=tile(ICONS[right]+"\n"+LearningGame.GAME_NAMES[right],"",COLORS[right%6],144);
            one.setOnClickListener(v->selectCategory(left));
            two.setOnClickListener(v->selectCategory(right));
            addTwo(one,two,144);
        }
        Button more=tile(categoryPage==0?"✨ Meer spelletjes  →":"←  Vorige spelletjes","",
                0xffe8e2ff,68);
        addTile(more,13);
        more.setOnClickListener(v->{categoryPage=1-categoryPage;showPage();});
    }
    private void selectCategory(int category) {
        game.configure(category,0,Math.max(0,age));
        difficulty=Math.max(0,age);
        prefs().edit().putInt("game_mode",category).apply();
        saveRecent();badgePage=0;
        move(DETAIL);
    }
    private void showDetail() {
        int mode=game.mode();
        PlaylumiScores.Score s=scores.get(mode);
        header(ICONS[mode]+" "+LearningGame.GAME_NAMES[mode],"Jouw eigen avontuur");
        addText(ICONS[mode],76,0xff8a5ac6,6,4);
        addText("⭐ "+s.correct+" punten",27,0xff3d8a72,7,4);
        addText("Dit is jouw score voor "+LearningGame.GAME_NAMES[mode]+".",14,SUB,0,10);
        Button play=tile("▶  Spelen!","Kies hoe je wilt spelen",0xffcff4dd,105);
        addTile(play,6);play.setOnClickListener(v->move(LEVEL));
        Button badge=tile("🏅 Mijn badges",s.earnedBadges()+" van 6 verdiend",
                0xffffe4b9,94);
        addTile(badge,6);badge.setOnClickListener(v->{badgePage=0;move(BADGES);});
    }
    private void showLevel() {
        header("🚀 Kies je uitdaging","Hoe moeilijk wil je spelen?");
        String[] descriptions={"Rustig beginnen","Een stapje verder","Een flinke uitdaging"};
        String[] emojis={"🌱","🌼","🏆"};
        for(int i=0;i<3;i++) {
            final int selected=i;
            Button b=tile(emojis[i]+"  "+LEVELS[i],descriptions[i],COLORS[i+1],119);
            addTile(b,11);
            b.setOnClickListener(v->{
                difficulty=selected;
                game.configure(game.mode(),game.variant(),difficulty);
                saveRecent();variantPage=0;move(VARIANTS);
            });
        }
    }
    private String[] variantNames() {
        String[] first;
        if(game.mode()>=NewWorldGames.FIRST) return NewWorldGames.VARIANTS[game.mode()-NewWorldGames.FIRST];
        if(game.mode()>=ExtraGames.SPELLING)
            first=ExtraGames.VARIANT_LABELS[game.mode()-ExtraGames.SPELLING];
        else if(game.mode()==LearningGame.WORDS)
            first=new String[]{"Middelste letter","Begin- of eindletter"};
        else {
            switch(game.mode()) {
                case LearningGame.COUNT:first=new String[]{"Voorwerpen tellen","Andere voorwerpen"};break;
                case LearningGame.COLORS:first=new String[]{"Welke kleur?","Zoek de kleur"};break;
                case LearningGame.LETTERS:first=new String[]{"Zoek klein","Zoek groot"};break;
                case LearningGame.SHAPES:first=new String[]{"Zoek dezelfde vorm","Noem de vorm"};break;
                case LearningGame.NUMBERS:first=new String[]{"Herken het cijfer","Volgende cijfer"};break;
                case LearningGame.PATTERNS:first=new String[]{"Om-en-om","Dubbel patroon"};break;
                default:first=new String[]{"Spel 1","Spel 2"};break;
            }
        }
        String[] all=new String[5];
        all[0]=first[0];all[1]=first[1];
        for(int i=0;i<3;i++) all[i+2]=VariantGames.NAMES[game.mode()][i];
        return all;
    }
    private void showVariants() {
        header("🎲 Hoe wil je spelen?","Kies een spelvorm");
        String[] names=variantNames();
        addText((variantPage+1)+" / 3",17,0xff9466c1,0,14);
        String[] icons={"🎯","🧩","✨","🌟","🚀"};
        int start=variantPage*2;
        for(int i=start;i<Math.min(start+2,names.length);i++) {
            final int selection=i;
            Button b=tile(icons[i]+"  "+names[i],"200 vragen om te ontdekken",COLORS[i%6],115);
            addTile(b,11);
            b.setOnClickListener(v->{
                game.configure(game.mode(),selection,difficulty);
                saveRecent();
                prefs().edit().putInt("game_variant",selection).apply();
                sessionSolved=0;sessionAttempts=0;feedback="";
                pendingBadges.clear();
                move(PLAY);
            });
        }
        Button more=tile(variantPage<2?"Meer spelvormen →":"↩ Terug naar begin","",
                0xffe8e2ff,74);
        addTile(more,14);
        more.setOnClickListener(v->{variantPage=variantPage<2?variantPage+1:0;showPage();});
    }
    private void showPlay() {
        int mode=game.mode();
        if(sessionSolved>=SESSION_ROUNDS) {showCompleted();return;}
        LearningGame.Round round=game.round();
        header(ICONS[mode]+" "+LearningGame.GAME_NAMES[mode],"");
        addText("⭐ "+sessionSolved+" / "+SESSION_ROUNDS,18,0xff2c886f,0,7);
        roundProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        roundProgress.setMax(SESSION_ROUNDS);
        roundProgress.setProgress(sessionSolved);
        roundProgress.setProgressTintList(ColorStateList.valueOf(0xff64c7a7));
        roundProgress.setProgressBackgroundTintList(ColorStateList.valueOf(0xffe4d9f0));
        LinearLayout.LayoutParams barParams=new LinearLayout.LayoutParams(-1,dp(12));
        barParams.setMargins(dp(10),0,dp(10),dp(19));
        root.addView(roundProgress,barParams);

        TextView question=text(round.prompt,25,0xff583b97);
        question.setPadding(dp(13),dp(16),dp(13),dp(16));
        question.setBackground(rounded(0xfff0e4ff,27));
        question.setMinHeight(dp(70));
        addWidget(question,0,12);
        TextView picture=text(round.display,50,round.displayColor);
        picture.setPadding(dp(12),dp(24),dp(12),dp(24));
        picture.setBackground(rounded(Color.WHITE,32));
        picture.setMinHeight(dp(164));
        picture.setElevation(dp(3));
        picture.setContentDescription(round.prompt+" "+round.display);
        addWidget(picture,0,12);

        feedbackText=text("👇 Tik het juiste antwoord aan!",18,SUB);
        addWidget(feedbackText,0,10);
        answerButtons.clear();answerLocked=round.isSolved();
        for(int i=0;i<round.answers.size();i+=2) {
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int j=i;j<Math.min(i+2,round.answers.size());j++) {
                final int answer=j;
                String value=round.answers.get(j);
                Button b=tile(value,"",COLORS[(mode+j)%6],85);
                b.setTextSize(value.length()>=13?16:value.length()>=9?18:
                        value.length()>=6?21:29);
                b.setEnabled(!answerLocked);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
                lp.setMargins(dp(5),dp(5),dp(5),dp(5));
                row.addView(b,lp);
                answerButtons.add(b);
                b.setOnClickListener(v->choose(answer,b));
            }
            root.addView(row);
        }
        nextButton=tile("🌟 Volgende!","",0xffcff4dd,80);
        addTile(nextButton,13);
        nextButton.setVisibility(answerLocked?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->continueAfterAnswer());
    }
    private void addWidget(View widget,int top,int bottom) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(dp(4),dp(top),dp(4),dp(bottom));
        root.addView(widget,p);
    }
    private void choose(int index,Button touched) {
        if(answerLocked || game.round().isSolved())return;
        int mode=game.mode();
        sessionAttempts++;
        int oldCorrect=scores.get(mode).correct;
        boolean correct=game.round().choose(index);
        ArrayList<Integer> newBadges=new ArrayList<>(scores.answer(mode,correct));
        if(correct) {
            answerLocked=true;sessionSolved++;lifetimeCount++;
            feedback="⭐ Goed gedaan! +3 🌟";
            touched.setBackground(new RippleDrawable(ColorStateList.valueOf(0x35ffffff),
                    rounded(0xffb9f5d1,25),null));
            for(Button b:answerButtons)b.setEnabled(false);
            feedbackText.setText(feedback);
            feedbackText.setTextColor(0xff21836e);
            PlaylumiMotion.pop(touched);
            PlaylumiMotion.pop(feedbackText);
            PlaylumiMotion.celebrate(stage,scores.get(mode).currentStreak);
            ObjectAnimator.ofInt(roundProgress,"progress",Math.max(0,sessionSolved-1),sessionSolved)
                    .setDuration(420);
            ObjectAnimator animate=ObjectAnimator.ofInt(roundProgress,"progress",
                    Math.max(0,sessionSolved-1),sessionSolved);
            animate.setDuration(430);animate.start();
            if(sessionSolved==SESSION_ROUNDS)newBadges.addAll(scores.finishSession(mode));
            wardrobe.earnAnswer();
            wardrobe.earnBadges(newBadges.size());
            saveWardrobe();
            pendingBadges.addAll(newBadges);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setAlpha(0f);
            nextButton.setTranslationY(dp(20));
            nextButton.animate().alpha(1f).translationY(0f).setDuration(380).start();
            scroller.post(()->scroller.smoothScrollTo(0,Math.max(0,root.getHeight()-scroller.getHeight())));
            feedbackText.announceForAccessibility(feedback);
        } else {
            feedback="💛 Probeer nog eens!";
            feedbackText.setText(feedback);feedbackText.setTextColor(0xffa86a29);
            PlaylumiMotion.shake(touched);
            feedbackText.announceForAccessibility(feedback);
        }
        saveScore(mode);
    }
    private void continueAfterAnswer() {
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);move(REWARD);return;
        }
        if(sessionSolved<SESSION_ROUNDS) {
            game.nextRound();saveRecent();
        }
        feedback="";
        move(PLAY);
    }
    private void showCompleted() {
        header("🏆 Jij bent een kampioen!","");
        characterHero(145);
        addText("200 vragen goed!",29,0xff4b3488,10,10);
        addText("Knap gespeeld! Wil je nog een avontuur?",17,SUB,0,18);
        Button again=tile("🔄 Nog een ronde!","",0xffcbf1d9,92);
        addTile(again,6);
        again.setOnClickListener(v->{
            sessionSolved=0;sessionAttempts=0;
            game.nextRound();saveRecent();move(PLAY);
        });
        Button home=tile("🌈 Ander spelletje","",0xffeadbff,82);
        addTile(home,6);
        home.setOnClickListener(v->move(CATEGORIES));
    }
    private void showBadges() {
        PlaylumiScores.Score score=scores.get(game.mode());
        header("🏅 Mijn badges","Bij "+LearningGame.GAME_NAMES[game.mode()]);
        addText((badgePage+1)+" / 3",17,0xff9364bb,0,8);
        for(int i=badgePage*2;i<Math.min((badgePage+1)*2,PlaylumiScores.BADGES);i++) {
            boolean unlocked=score.hasBadge(i);
            String line=(unlocked?PlaylumiScores.BADGE_ICONS[i]:"🔒")+
                    "  "+PlaylumiScores.BADGE_NAMES[i];
            String detail=unlocked?"🌟 Verdiend!":
                    score.badgeProgress(i)+" / "+score.badgeGoal(i)+"  •  "+PlaylumiScores.BADGE_HINTS[i];
            Button badge=tile(line,detail,unlocked?0xffffe5b4:0xffeceaf2,130);
            badge.setEnabled(false);
            addTile(badge,12);
        }
        Button more=tile(badgePage<2?"✨ Meer badges →":"↩ Eerste badges","",0xffe7ddff,74);
        addTile(more,12);
        more.setOnClickListener(v->{badgePage=badgePage<2?badgePage+1:0;showPage();});
    }
    private void showReward() {
        header("🎉 Nieuwe badge!","");
        addText(PlaylumiScores.BADGE_ICONS[earnedBadge],100,0xffc68823,24,10);
        addText(PlaylumiScores.BADGE_NAMES[earnedBadge],30,0xff51378c,8,12);
        addText(PlaylumiScores.BADGE_HINTS[earnedBadge],18,SUB,0,10);
        addText("🎁 +12 speelsterren!",21,0xff2b9677,0,22);
        Button continueButton=tile("🌟 Hoera, verder!","",0xffd3f4dd,96);
        addTile(continueButton,10);
        continueButton.setOnClickListener(v->continueAfterBadge());
        stage.post(()->PlaylumiMotion.celebrate(stage,10));
    }
    private void continueAfterBadge() {
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);showPage();return;
        }
        earnedBadge=-1;
        if(sessionSolved<SESSION_ROUNDS) {game.nextRound();saveRecent();}
        feedback="";move(PLAY);
    }

    /** A playful character preview rather than a crowded settings screen. */
    private void showAvatar() {
        header("✨ Dit is jouw ster!","Maak je eigen magische maatje.");
        characterHero(228);
        addText("🌟 "+wardrobe.stars()+" speelsterren",24,0xffa4781f,8,6);
        addText(wardrobe.unlockedItems()+" van 25 spullen vrijgespeeld",14,SUB,0,14);
        Button dress=tile("🎀 Kleed je ster aan!","Ontdek nieuwe kleuren en accessoires",
                0xffd7f5e5,112);
        addTile(dress,8);
        dress.setOnClickListener(v->{closetPage=0;move(CLOSET);});
        Button play=tile("🎮 Verdien meer sterren","Spelen levert 3 sterren per goed antwoord op",
                0xffffe6c8,92);
        addTile(play,8);
        play.setOnClickListener(v->move(HOME));
    }

    /** Show at most two wardrobe categories on each page. */
    private void showCloset() {
        header("🎁 Kleed je ster aan","Wat wil je veranderen?");
        characterHero(170);
        addText("🌟 "+wardrobe.stars()+" speelsterren",19,0xffa4781f,5,8);
        addText((closetPage+1)+" / 3",16,0xff9163bf,0,8);
        int start=closetPage*2;
        for(int i=start;i<Math.min(start+2,PlaylumiWardrobe.SLOTS);i++) {
            final int selected=i;
            String label=PlaylumiWardrobe.SLOT_ICONS[i]+"  "+PlaylumiWardrobe.SLOT_NAMES[i];
            String worn=PlaylumiWardrobe.ITEM_NAMES[i][wardrobe.equipped(i)];
            Button b=tile(label,"Nu: "+worn,COLORS[i%COLORS.length],108);
            addTile(b,9);
            b.setOnClickListener(v->{
                selectedSlot=selected;shopPage=0;shopMessage="";
                move(SHOP);
            });
        }
        Button browse=tile(closetPage<2?"✨ Meer aankleden →":"↩ Terug naar de kleuren","",
                0xffe8ddff,76);
        addTile(browse,12);
        browse.setOnClickListener(v->{
            closetPage=closetPage<2?closetPage+1:0;
            shopMessage="";showPage();
        });
    }

    /** The wardrobe uses only stars earned by playing, with no real money. */
    private void showShop() {
        int slot=selectedSlot;
        header(PlaylumiWardrobe.SLOT_ICONS[slot]+" "+PlaylumiWardrobe.SLOT_NAMES[slot],
                "Kies een nieuwe look!");
        characterHero(194);
        addText("🌟 "+wardrobe.stars()+" speelsterren",21,0xffa4781f,0,8);
        if(!shopMessage.isEmpty()) addText(shopMessage,17,0xff5b3b90,0,10);
        addText((shopPage+1)+" / 3",16,0xff9163bf,0,7);
        int start=shopPage*2;
        for(int i=start;i<Math.min(start+2,PlaylumiWardrobe.OPTIONS);i++) {
            final int selected=i;
            boolean owned=wardrobe.owned(slot,i);
            boolean selectedNow=wardrobe.equipped(slot)==i;
            String label=PlaylumiWardrobe.ITEM_ICONS[slot][i]+"  "+
                    PlaylumiWardrobe.ITEM_NAMES[slot][i];
            String subtitle=selectedNow?"✅ Draag je nu":
                    owned?"✨ Klik om aan te trekken":
                    "🌟 "+wardrobe.price(slot,i)+" speelsterren";
            Button b=tile(label,subtitle,
                    selectedNow?0xffd0f4df:COLORS[(slot+i)%COLORS.length],102);
            addTile(b,8);
            b.setOnClickListener(v->{
                int result=wardrobe.buyOrEquip(slot,selected);
                if(result==PlaylumiWardrobe.NOT_ENOUGH) {
                    int needed=wardrobe.price(slot,selected)-wardrobe.stars();
                    shopMessage="🌼 Speel verder! Nog "+needed+" sterren nodig.";
                    showPage();
                } else {
                    saveWardrobe();
                    shopMessage=result==PlaylumiWardrobe.PURCHASED ?
                            "🎉 Nieuw! "+PlaylumiWardrobe.ITEM_NAMES[slot][selected]:
                            "✨ Superleuk! Aangetrokken.";
                    showPage();
                    if(result==PlaylumiWardrobe.PURCHASED) {
                        FrameLayout active=stage;
                        active.post(()->{
                            if(stage==active)PlaylumiMotion.celebrate(active,10);
                        });
                    } else if(character!=null) {
                        PlaylumiMotion.pop(character);
                    }
                }
            });
        }
        Button browse=tile(shopPage<2?"🎨 Meer spullen →":"↩ Eerste spullen","",
                0xffe8ddff,76);
        addTile(browse,12);
        browse.setOnClickListener(v->{
            shopPage=shopPage<2?shopPage+1:0;
            shopMessage="";showPage();
        });
    }

}
