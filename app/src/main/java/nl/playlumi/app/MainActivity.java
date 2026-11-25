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
import java.io.File;
import java.util.Calendar;
import android.os.SystemClock;

/** One choice per screen, a separate learning world menu, and native Android play motion. */
public class MainActivity extends Activity {
    private static final int AGE=0, HOME=1, CATEGORIES=2, DETAIL=3, LEVEL=4,
            VARIANTS=5, PLAY=6, BADGES=7, REWARD=8, AVATAR=9, CLOSET=10, SHOP=11,
            MINI_MENU=12, MINI_PLAY=13, WORLD_HUB=14, ADVENTURE_MAP=15,
            ROOM=16, ROOM_CATS=17, ROOM_ITEMS=18,
            TROPHY_WORLDS=19, TROPHY_CATS=20,
            STORY_LIBRARY=21, STORY_PLAY=22, STORY_REACTION=23, STORY_END=24, DRAW=25,
            PARENT_GATE=26, PARENT_HOME=27, PARENT_WORLDS=28, PARENT_STATS=29,
            PARENT_SETTINGS=30, BREAK_SCREEN=31, PROFILES=32, STAR_STAGE=33,
            BUBBLE_PLAY=34, FRIEND_CARE=35, FRIEND_ALBUM=36,
            MUSIC_PLAY=37, GARDEN_MENU=38, GARDEN_PLANT=39,
            WORLD_PICKER=40, HOME_OPTIONS=41, STAR_CATCH=42;
    private static final int SESSION_ROUNDS=PlaylumiLayout.SHORT_ROUND_QUESTIONS;
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
    private boolean moving,answerLocked,greeted;
    private int mascotTapCount;
    private int starStagePage;
    private LearningGame game;
    private QuestionHistory recent;
    private PlaylumiScores scores;
    private PlaylumiWardrobe wardrobe;
    private PlaylumiFriends friends;
    private int carePage,albumPage;
    private String careMessage="";
    private PlaylumiRoom room;
    private PlaylumiGarden garden;
    private int gardenPage,gardenPlant;
    private String gardenMessage="";
    private int roomPage,roomItemPage,roomSlot;
    private String roomMessage="";
    private PlaylumiCharacterView character;
    private LinearLayout root;
    private FrameLayout stage;
    private ScrollView scroller;
    private TextView feedbackText;
    private PlaylumiStarTrail roundStars;
    private Button nextButton;
    private String feedback="";
    private PlaylumiAudio sound;
    private PlaylumiMiniChallenge mini;
    private PlaylumiCharacterView playPal;
    private int miniKind,miniRound,rewardReturn=PLAY;
    private boolean miniSolved;
    private PlaylumiBubbles bubbles;
    private PlaylumiMelody melody;
    private int melodyRound;
    private boolean melodySolved;
    private TextView melodyStatus;
    private int bubbleRound;
    private int starCatchRound;
    private PlaylumiStarCatch starCatch;
    private boolean starCatchSolved;
    private TextView starCatchProgress;
    private boolean bubbleSolved;
    private PlaylumiAdventure adventures;
    private PlaylumiAdaptive adaptive;
    private PlaylumiUsage usage;
    private long activeSince;
    private int parentWorld,parentStatsPage,adultA,adultB,breakReturn=HOME;
    private int activeProfile;
    // Tracks which village building started a screen, so Back returns logically.
    private int villageLaunch=-1;
    private boolean quickCategoryPlay;
    private boolean parentAllowed;
    private PlaylumiStories stories;
    private PlaylumiDrawingView drawing;
    private int storyLibraryPage;
    private TextView drawingInfo;
    private boolean eraseAsked;
    private boolean storyJustCompleted;
    private boolean smartDifficulty,roundHadMistake;
    private boolean badgesFromTrophy;
    private int trophyWorld,trophyPage;
    private int pendingLevel=-1;
    private int storyStage=-1,storyPage;
    private boolean storyNewReward;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xffeaf5ff);
        getWindow().setNavigationBarColor(0xfffff6e4);
        int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        activeProfile=getSharedPreferences("playlumi-global",MODE_PRIVATE)
                .getInt("active_profile",0);
        activeProfile=PlaylumiProfiles.safe(activeProfile);
        if(state!=null && state.getInt("activeProfile",activeProfile)!=activeProfile)
            state=null; // Switching children must not restore the former child's state.
        SharedPreferences prefs=prefs();
        sound=new PlaylumiAudio(this,prefs.getBoolean("sound_on",true));
        PlaylumiMotion.setReduced(prefs.getBoolean("reduced_motion",false));
        int today=dayStamp();
        if(prefs.getInt("usage_day",-1)!=today)
            prefs.edit().putInt("usage_day",today).putInt("usage_seconds",0)
                    .putBoolean("usage_break_shown",false).apply();
        usage=new PlaylumiUsage();
        usage.restore(prefs.getInt("usage_seconds",0),
                prefs.getInt("usage_limit",0),prefs.getBoolean("usage_break_shown",false));
        adultA=10+new Random().nextInt(8);
        adultB=8+new Random().nextInt(8);
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
            starStagePage=state.getInt("starStagePage",0);
            selectedSlot=state.getInt("selectedSlot",0);
            carePage=state.getInt("carePage",0);
            albumPage=state.getInt("albumPage",0);
            gardenPage=state.getInt("gardenPage",0);
            gardenPlant=state.getInt("gardenPlant",0);
            sessionSolved=state.getInt("sessionSolved",0);
            sessionAttempts=state.getInt("sessionAttempts",0);
            page=state.getInt("page",page);
            earnedBadge=state.getInt("earnedBadge",-1);
            miniKind=state.getInt("miniKind",0);
            miniRound=state.getInt("miniRound",0);
            bubbleRound=state.getInt("bubbleRound",0);
            melodyRound=state.getInt("melodyRound",0);
            melodySolved=state.getBoolean("melodySolved",false);
            bubbleSolved=state.getBoolean("bubbleSolved",false);
            rewardReturn=state.getInt("rewardReturn",PLAY);
            storyStage=state.getInt("storyStage",-1);
            storyPage=state.getInt("storyPage",0);
            roomPage=state.getInt("roomPage",0);
            roomItemPage=state.getInt("roomItemPage",0);
            roomSlot=state.getInt("roomSlot",0);
            storyNewReward=state.getBoolean("storyNewReward",false);
            smartDifficulty=state.getBoolean("smartDifficulty",smartDifficulty);
            roundHadMistake=state.getBoolean("roundHadMistake",false);
            pendingLevel=state.getInt("pendingLevel",-1);
            badgesFromTrophy=state.getBoolean("badgesFromTrophy",false);
            trophyWorld=state.getInt("trophyWorld",0);
            trophyPage=state.getInt("trophyPage",0);
            storyLibraryPage=state.getInt("storyLibraryPage",0);
            storyJustCompleted=state.getBoolean("storyJustCompleted",false);
            parentWorld=state.getInt("parentWorld",0);
            parentStatsPage=state.getInt("parentStatsPage",0);
            adultA=state.getInt("adultA",adultA);
            adultB=state.getInt("adultB",adultB);
            parentAllowed=state.getBoolean("parentAllowed",false);
            villageLaunch=state.getInt("villageLaunch",-1);
            quickCategoryPlay=state.getBoolean("quickCategoryPlay",false);
            breakReturn=state.getInt("breakReturn",HOME);
        }
        stories=new PlaylumiStories();
        stories.restore(prefs.getInt("stories_completed",0),prefs.getInt("stories_current",-1),
                prefs.getInt("stories_step",0),prefs.getInt("stories_alts",0),
                prefs.getInt("stories_choice",-1));
        adaptive=new PlaylumiAdaptive();
        for(int cat=0;cat<PlaylumiScores.CATEGORIES;cat++)
            adaptive.restore(cat,prefs.getInt("learn_lvl_"+cat,-1),
                    prefs.getInt("learn_seen_"+cat,0),prefs.getInt("learn_good_"+cat,0));
        smartDifficulty=prefs.getBoolean("smart_mode",false);
        adventures=new PlaylumiAdventure();
        for(int w=0;w<PlaylumiAdventure.WORLDS;w++)
            adventures.restore(w,prefs.getInt("adventure_progress_"+w,0));
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
        friends=new PlaylumiFriends();
        friends.restoreRewards(prefs.getInt("friends_rewarded",0));
        for(int f=0;f<PlaylumiFriends.FRIENDS;f++)
            friends.restore(f,prefs.getInt("friends_care_"+f,0));
        garden=new PlaylumiGarden();
        garden.restoreRewards(prefs.getInt("garden_rewarded",0));
        for(int p=0;p<PlaylumiGarden.COUNT;p++)
            garden.restorePlant(p,prefs.getInt("garden_stage_"+p,0));
        room=new PlaylumiRoom();
        for(int slot=0;slot<PlaylumiRoom.SLOTS;slot++)
            room.restore(slot,prefs.getInt("room_mask_"+slot,1),
                    prefs.getInt("room_item_"+slot,0));
        recent=new QuestionHistory(prefs.getString("recent_questions",""));
        game=new LearningGame(new Random());
        game.setHistory(recent);
        game.configure(Math.max(0,Math.min(LearningGame.GAME_NAMES.length-1,mode)),
                Math.max(0,Math.min(4,variant)), Math.max(0,Math.min(2,difficulty)));
        if(page==MINI_PLAY)mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
        if(page==MUSIC_PLAY && state!=null && state.getIntArray("melodyNotes")!=null) {
            try {
                melody=PlaylumiMelody.restore(state.getIntArray("melodyNotes"),
                        state.getInt("melodyProgress",0));
            } catch(IllegalArgumentException ignored) {}
        }
        if(page==MUSIC_PLAY && melody==null)
            melody=PlaylumiMelody.generate(Math.max(0,age),new Random());
        if(page==STAR_CATCH) {
            starCatchRound=state==null?0:Math.max(0,Math.min(PlaylumiStarCatch.ROUNDS,state.getInt("starCatchRound",0)));
            if(starCatchRound<PlaylumiStarCatch.ROUNDS){
                if(state!=null && state.getInt("starCatchTotal",0)>0)try {
                    starCatch=PlaylumiStarCatch.restore(state.getInt("starCatchTarget"),state.getInt("starCatchTotal"),state.getInt("starCatchMask"));
                } catch(IllegalArgumentException ignored) {}
                if(starCatch==null)starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
                starCatchSolved=starCatch.finished();
            }
        }
        if(page==BUBBLE_PLAY) {
            if(state!=null && state.getInt("bubbleTotal",0)>0) {
                try {
                    bubbles=PlaylumiBubbles.restore(Math.max(0,age),state.getInt("bubbleTarget",0),
                            state.getInt("bubbleTotal",0),state.getInt("bubbleMask",0));
                } catch(IllegalArgumentException ignored) {}
            }
            if(bubbles==null)bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
        }
        // Debug-only test entrance: emulator can capture the real village directly.
        // This is unavailable in a signed production release.
        if((getApplicationInfo().flags &
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0 &&
                getIntent()!=null && getIntent().getBooleanExtra("playlumi_ci_home",false)) {
            age=Math.max(0,age);
            page=HOME;
        }
        showPage();
    }
    @Override protected void onDestroy() {
        if(sound!=null)sound.close();
        super.onDestroy();
    }
    @Override protected void onResume() {
        super.onResume();
        activeSince=SystemClock.elapsedRealtime();
    }
    @Override protected void onPause() {
        if(page==DRAW && drawing!=null)drawing.save();
        if(sound!=null)sound.stopSpeaking();
        saveUsage();
        activeSince=0;
        super.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        if(page==DRAW && drawing!=null)drawing.save();
        out.putInt("activeProfile",activeProfile);
        out.putInt("age",age);out.putInt("mode",game.mode());out.putInt("variant",game.variant());
        out.putInt("difficulty",difficulty);out.putInt("world",world);
        out.putInt("categoryPage",categoryPage);out.putInt("variantPage",variantPage);
        out.putInt("badgePage",badgePage);out.putInt("sessionSolved",sessionSolved);
        out.putInt("sessionAttempts",sessionAttempts);out.putInt("page",page);
        out.putInt("earnedBadge",earnedBadge);
        out.putInt("closetPage",closetPage);
        out.putInt("shopPage",shopPage);
        out.putInt("starStagePage",starStagePage);
        out.putInt("selectedSlot",selectedSlot);
        out.putInt("carePage",carePage);
        out.putInt("albumPage",albumPage);
        out.putInt("gardenPage",gardenPage);
        out.putInt("gardenPlant",gardenPlant);
        out.putInt("miniKind",miniKind);
        out.putInt("miniRound",miniRound);
        out.putInt("bubbleRound",bubbleRound);
        out.putInt("starCatchRound",starCatchRound);
        if(starCatch!=null){out.putInt("starCatchTarget",starCatch.target());out.putInt("starCatchTotal",starCatch.total());out.putInt("starCatchMask",starCatch.mask());}
        out.putInt("melodyRound",melodyRound);
        out.putBoolean("melodySolved",melodySolved);
        if(melody!=null) {
            out.putIntArray("melodyNotes",melody.sequence());
            out.putInt("melodyProgress",melody.progress());
        }
        out.putBoolean("bubbleSolved",bubbleSolved);
        if(bubbles!=null) {
            out.putInt("bubbleTarget",bubbles.target);
            out.putInt("bubbleTotal",bubbles.total);
            out.putInt("bubbleMask",bubbles.mask());
        }
        out.putInt("rewardReturn",rewardReturn);
        out.putInt("storyStage",storyStage);
        out.putInt("storyPage",storyPage);
        out.putBoolean("storyNewReward",storyNewReward);
        out.putBoolean("smartDifficulty",smartDifficulty);
        out.putBoolean("roundHadMistake",roundHadMistake);
        out.putInt("pendingLevel",pendingLevel);
        out.putBoolean("badgesFromTrophy",badgesFromTrophy);
        out.putInt("trophyWorld",trophyWorld);
        out.putInt("trophyPage",trophyPage);
        out.putInt("storyLibraryPage",storyLibraryPage);
        out.putBoolean("storyJustCompleted",storyJustCompleted);
        out.putInt("parentWorld",parentWorld);
        out.putInt("parentStatsPage",parentStatsPage);
        out.putInt("adultA",adultA);
        out.putInt("adultB",adultB);
        out.putBoolean("parentAllowed",parentAllowed);
        out.putInt("villageLaunch",villageLaunch);
        out.putBoolean("quickCategoryPlay",quickCategoryPlay);
        out.putInt("breakReturn",breakReturn);
        out.putInt("roomPage",roomPage);
        out.putInt("roomItemPage",roomItemPage);
        out.putInt("roomSlot",roomSlot);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if(page==AGE) {
            if(prefs().contains("chosen_age"))move(HOME_OPTIONS);
            else super.onBackPressed();
            return;
        }
        if(page==HOME) {super.onBackPressed();return;}
        if(page==WORLD_PICKER || page==HOME_OPTIONS) {move(HOME);return;}
        if(page==REWARD) { continueAfterBadge();return; }
        if(page==AVATAR) {move(HOME);return;}
        if(page==CLOSET) {move(AVATAR);return;}
        if(page==SHOP) {move(villageLaunch==PlaylumiVillageView.REWARDS?HOME:CLOSET);return;}
        if(page==FRIEND_CARE) {move(AVATAR);return;}
        if(page==FRIEND_ALBUM) {move(FRIEND_CARE);return;}
        if(page==GARDEN_MENU) {move(ROOM);return;}
        if(page==GARDEN_PLANT) {move(GARDEN_MENU);return;}
        if(page==MINI_PLAY || page==BUBBLE_PLAY || page==MUSIC_PLAY || page==STAR_CATCH) {
            boolean launchedDirectly=(villageLaunch==PlaylumiVillageView.SHAPES&&page==MINI_PLAY)||
                    (villageLaunch==PlaylumiVillageView.MUSIC&&page==MUSIC_PLAY);
            move(launchedDirectly?HOME:MINI_MENU);return;
        }
        if(page==MINI_MENU) {move(HOME);return;}
        if(page==WORLD_HUB) {move(villageLaunch==PlaylumiVillageView.BOOKS?HOME:WORLD_PICKER);return;}
        if(page==ADVENTURE_MAP) {move(villageLaunch==PlaylumiVillageView.MISSIONS?HOME:WORLD_HUB);return;}
        if(page==STAR_STAGE) {move(AVATAR);return;}
        if(page==ROOM) {move(AVATAR);return;}
        if(page==ROOM_CATS) {move(ROOM);return;}
        if(page==ROOM_ITEMS) {move(ROOM_CATS);return;}
        if(page==TROPHY_WORLDS) {move(villageLaunch==PlaylumiVillageView.COLLECTION?HOME:AVATAR);return;}
        if(page==TROPHY_CATS) {move(TROPHY_WORLDS);return;}
        if(page==STORY_LIBRARY) {move(WORLD_HUB);return;}
        if(page==STORY_PLAY || page==STORY_REACTION || page==STORY_END) {move(STORY_LIBRARY);return;}
        if(page==DRAW) {
            if(drawing!=null)drawing.save();
            move(WORLD_HUB);return;
        }
        if(page==PARENT_GATE || page==PARENT_HOME) {parentAllowed=false;move(HOME);return;}
        if(page==PARENT_WORLDS || page==PARENT_SETTINGS) {move(PARENT_HOME);return;}
        if(page==PARENT_STATS) {move(PARENT_WORLDS);return;}
        if(page==BREAK_SCREEN) {move(HOME);return;}
        if(page==PROFILES) {move(HOME);return;}
        if(page==BADGES && badgesFromTrophy) {move(TROPHY_CATS);return;}
        if(page==PLAY && storyStage>=0) {move(ADVENTURE_MAP);return;}
        if(page==PLAY && quickCategoryPlay) {move(DETAIL);return;}
        if(page==DETAIL && (villageLaunch==PlaylumiVillageView.ROCKET ||
                villageLaunch==PlaylumiVillageView.BOOKS ||
                villageLaunch==PlaylumiVillageView.COLORS)) {move(HOME);return;}
        int back=page==CATEGORIES?WORLD_HUB:page==DETAIL?CATEGORIES:page==LEVEL?DETAIL:
                page==VARIANTS?LEVEL:page==PLAY?VARIANTS:DETAIL;
        move(back);
    }
    private int dayStamp() {
        Calendar c=Calendar.getInstance();
        return c.get(Calendar.YEAR)*10000+(c.get(Calendar.MONTH)+1)*100+c.get(Calendar.DAY_OF_MONTH);
    }
    private void saveUsage() {
        if(usage==null)return;
        int day=dayStamp();
        if(prefs().getInt("usage_day",-1)!=day)usage.startNewDay();
        if(activeSince>0) {
            long now=SystemClock.elapsedRealtime();
            long elapsed=Math.max(0,(now-activeSince)/1000L);
            usage.addSeconds((int)Math.min(86400L,elapsed));
            activeSince=now;
        }
        prefs().edit().putInt("usage_day",day).putInt("usage_seconds",usage.seconds())
                .putInt("usage_limit",usage.limit())
                .putBoolean("usage_break_shown",usage.breakShown()).apply();
    }
    private SharedPreferences prefs() {
        return activeProfile==0?getPreferences(MODE_PRIVATE):
                getSharedPreferences(PlaylumiProfiles.preferences(activeProfile),MODE_PRIVATE);
    }
    private void saveRecent() { prefs().edit().putString("recent_questions",recent.serialize()).apply(); }
    private void saveAdaptive(int cat) {
        prefs().edit().putInt("learn_lvl_"+cat,adaptive.level(cat,Math.max(0,age)))
                .putInt("learn_seen_"+cat,adaptive.sampleCount(cat))
                .putInt("learn_good_"+cat,adaptive.sampleCorrect(cat)).apply();
    }
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
    private void saveStories() {
        prefs().edit().putInt("stories_completed",stories.completionMask())
                .putInt("stories_current",stories.currentStory())
                .putInt("stories_step",stories.step())
                .putInt("stories_alts",stories.secondaryChoices())
                .putInt("stories_choice",stories.choice()).apply();
    }
    private void saveFriends() {
        SharedPreferences.Editor editor=prefs().edit().putInt("friends_rewarded",friends.rewardedMask());
        for(int i=0;i<PlaylumiFriends.FRIENDS;i++)
            editor.putInt("friends_care_"+i,friends.mask(i));
        editor.apply();
    }
    private void saveGarden() {
        SharedPreferences.Editor e=prefs().edit().putInt("garden_rewarded",garden.rewardMask());
        for(int i=0;i<PlaylumiGarden.COUNT;i++)
            e.putInt("garden_stage_"+i,garden.stage(i));
        e.apply();
    }
    private void saveRoom() {
        SharedPreferences.Editor edit=prefs().edit();
        for(int slot=0;slot<PlaylumiRoom.SLOTS;slot++) {
            edit.putInt("room_mask_"+slot,room.ownedMask(slot));
            edit.putInt("room_item_"+slot,room.selected(slot));
        }
        edit.apply();
    }
    private int dp(int x) {return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable rounded(int color,int radius) {
        GradientDrawable b=new GradientDrawable();
        b.setColor(color);b.setCornerRadius(dp(radius));return b;
    }
    private int mix(int a,int b,float amount) {
        float t=Math.max(0f,Math.min(1f,amount));
        return Color.rgb(
                Math.round(Color.red(a)*(1-t)+Color.red(b)*t),
                Math.round(Color.green(a)*(1-t)+Color.green(b)*t),
                Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));
    }
    /** A playful glossy 3D-like card, matching the purple-and-gold village. */
    private GradientDrawable gameTileBackground(int color) {
        GradientDrawable b=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mix(color,Color.WHITE,.40f),color,mix(color,0xffb299d7,.20f)});
        b.setCornerRadius(dp(21));
        b.setStroke(dp(2),mix(color,0xff8154b9,.55f));
        return b;
    }
    private void addIllustratedPortal(int place,String title,int heightDp) {
        PlaylumiPortalView art=new PlaylumiPortalView(this,place,title);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(heightDp));
        p.setMargins(dp(1),dp(8),dp(1),dp(9));
        root.addView(art,p);
    }
    private int portalForMode(int mode) {
        if(mode==LearningGame.COUNT || mode==LearningGame.NUMBERS)
            return PlaylumiVillageView.ROCKET;
        if(mode==LearningGame.LETTERS || mode==LearningGame.WORDS)
            return PlaylumiVillageView.BOOKS;
        if(mode==LearningGame.SHAPES)
            return PlaylumiVillageView.SHAPES;
        if(mode==LearningGame.COLORS)
            return PlaylumiVillageView.COLORS;
        return PlaylumiVillageView.PLAYGROUND;
    }
    private void startCategoryNow() {
        quickCategoryPlay=true;
        storyStage=-1;smartDifficulty=true;roundHadMistake=false;pendingLevel=-1;
        sessionSolved=0;sessionAttempts=0;feedback="";
        pendingBadges.clear();
        difficulty=adaptive.level(game.mode(),Math.max(0,age));
        game.configure(game.mode(),0,difficulty);
        prefs().edit().putInt("last_played_category",game.mode())
                .putInt("last_played_variant",0).apply();
        saveRecent();
        move(PLAY);
    }
    private TextView text(String value,int size,int color) {
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif-rounded",size>=19?Typeface.BOLD:Typeface.NORMAL));
        t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(true);
        t.setLineSpacing(dp(1),1.02f);return t;
    }
    private void addText(String value,int size,int color,int top,int bottom) {
        TextView t=text(value,size,color);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(PlaylumiLayout.margin(top)),
                0,dp(PlaylumiLayout.margin(bottom)));root.addView(t,p);
    }
    private Button tile(String title,String description,int color,int minHeight) {
        Button b=new Button(this);
        b.setAllCaps(false);
        b.setText(PlaylumiType.tileText(title,description));
        b.setTextColor(TEXT);
        b.setTextSize(description.isEmpty()?18:16);
        b.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        b.setLineSpacing(dp(1),1.0f);
        b.setGravity(Gravity.CENTER);
        b.setMaxLines(4);
        b.setPadding(dp(10),dp(9),dp(10),dp(9));
        b.setMinHeight(dp(PlaylumiLayout.tileHeight(minHeight,gameScreen())));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                gameTileBackground(color),null));
        b.setElevation(dp(5));
        b.setContentDescription((title+" "+description).trim());
        PlaylumiMotion.tactile(b);
        return b;
    }
    private void addTile(Button button,int margin) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        int gap=PlaylumiLayout.margin(margin);
        p.setMargins(dp(3),dp(gap),dp(3),dp(gap));
        root.addView(button,p);
    }
    private void addTwo(Button one,Button two,int height) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(0,-2,1);
        LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,-2,1);
        p1.setMargins(dp(3),dp(4),dp(3),dp(4));
        p2.setMargins(dp(3),dp(4),dp(3),dp(4));
        one.setMinHeight(dp(PlaylumiLayout.tileHeight(height,gameScreen())));
        two.setMinHeight(dp(PlaylumiLayout.tileHeight(height,gameScreen())));
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
        int actual=PlaylumiLayout.heroHeight(size,landscapeScreen(),false,
                getResources().getConfiguration().screenHeightDp);
        ImageView star=mascot(actual);
        holder.addView(star);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(actual+5));
        p.setMargins(0,dp(2),0,dp(3));
        root.addView(holder,p);
        PlaylumiMotion.floatMascot(star);
    }
    private void characterHero(int size) {
        LinearLayout holder=new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        character=new PlaylumiCharacterView(this,wardrobe);
        character.setContentDescription("Jouw stervriendje. Tik voor een magische verrassing.");
        character.setOnClickListener(v->{
            mascotTapCount++;
            switch(mascotTapCount%4){
                case 0: character.twirl();if(sound!=null)sound.sparkle();break;
                case 1: character.wave();if(sound!=null)sound.hello();break;
                case 2: character.dance();if(sound!=null)sound.dance();break;
                default: character.surprised();if(sound!=null)sound.pop();break;
            }
            PlaylumiMotion.pop(v);
            PlaylumiDelight.sprinkle(stage,character);
        });
        boolean showFull=page==AVATAR || page==STAR_STAGE || page==STORY_END ||
                page==REWARD || page==BREAK_SCREEN || page==SHOP;
        int actual=PlaylumiLayout.heroHeight(size,landscapeScreen(),showFull,
                getResources().getConfiguration().screenHeightDp);
        holder.addView(character,new LinearLayout.LayoutParams(dp(actual),dp(actual)));
        if(page==HOME && !greeted) {
            greeted=true;
            final PlaylumiCharacterView helloFriend=character;
            final FrameLayout helloStage=stage;
            holder.postDelayed(()->{
                if(stage==helloStage && character==helloFriend) {
                    helloFriend.wave();
                    if(sound!=null)sound.hello();
                    PlaylumiDelight.sprinkle(stage,helloFriend);
                }
            },550L);
        }
        LinearLayout.LayoutParams row=new LinearLayout.LayoutParams(-1,dp(actual+4));
        row.setMargins(0,dp(1),0,dp(1));
        root.addView(holder,row);
        PlaylumiMotion.floatMascot(character);
    }
    private void companion(int size) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        playPal=new PlaylumiCharacterView(this,wardrobe);
        row.addView(playPal,new LinearLayout.LayoutParams(dp(size),dp(size)));
        playPal.setContentDescription("Je stervriend. Tik om te zwaaien.");
        playPal.setOnClickListener(v->{playPal.wave();sound.tap();});
        root.addView(row,new LinearLayout.LayoutParams(-1,dp(size+4)));
    }
    private boolean landscapeScreen() {
        return PlaylumiLayout.useWideLayout(
                getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().orientation==
                        android.content.res.Configuration.ORIENTATION_LANDSCAPE);
    }
    private boolean gameScreen() {
        return page==PLAY || page==MINI_PLAY || page==BUBBLE_PLAY || page==STAR_CATCH ||
                page==MUSIC_PLAY || page==STORY_PLAY || page==STORY_REACTION ||
                page==GARDEN_PLANT || page==DRAW;
    }
    /**
     * One compact navigation row, not a large Playlumi logo followed by a duplicate title.
     * Always leaves the main part of the screen for the actual game.
     */
    private void header(String heading,String subtitle) {
        boolean gameHeader=gameScreen();
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8),dp(4),dp(8),dp(4));
        GradientDrawable barBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff8056bf,0xff58368e,0xff442879});
        barBg.setCornerRadius(dp(20));
        barBg.setStroke(dp(2),0xffd4b3f2);
        row.setBackground(barBg);
        row.setElevation(dp(5));
        if(page!=HOME && (page!=AGE || prefs().contains("chosen_age"))) {
            TextView back=text("❮",22,0xffffe6a7);
            back.setGravity(Gravity.CENTER);
            back.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    rounded(0xff7652a8,14),null));
            back.setContentDescription("Terug naar het vorige scherm");
            back.setClickable(true);back.setFocusable(true);
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(42),dp(42));
            bp.setMargins(0,0,dp(9),0);
            row.addView(back,bp);
            back.setOnClickListener(v->onBackPressed());
        }
        TextView title=text(heading,gameHeader?18:21,Color.WHITE);
        title.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        title.setMaxLines(1);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(48),1f);
        row.addView(title,tp);
        if(gameHeader) {
            if(subtitle!=null && !subtitle.isEmpty()) {
                TextView status=text(subtitle.replace("Uitdaging ",""),12,0xffffe3a0);
                status.setSingleLine(true);
                status.setPadding(dp(5),0,dp(5),0);
                row.addView(status,new LinearLayout.LayoutParams(-2,dp(45)));
            }
            if(wardrobe!=null) {
                playPal=new PlaylumiCharacterView(this,wardrobe);
                playPal.setContentDescription("Je stervriend danst als je iets goed doet");
                LinearLayout.LayoutParams tiny=new LinearLayout.LayoutParams(dp(32),dp(32));
                tiny.setMargins(dp(4),0,0,0);
                row.addView(playPal,tiny);
            }
        } else {
            View star=wardrobe==null?mascot(37):new PlaylumiCharacterView(this,wardrobe);
            LinearLayout.LayoutParams iconParams=
                    new LinearLayout.LayoutParams(dp(39),dp(39));
            iconParams.setMargins(dp(5),0,0,0);
            row.addView(star,iconParams);
        }
        // One consistent exit to the real illustrated village on every game page.
        if(page!=HOME && page!=AGE) {
            TextView village=text("⌂",28,0xffffdf86);
            village.setGravity(Gravity.CENTER);
            village.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    rounded(0xff6c469d,14),null));
            village.setContentDescription("Terug naar het magische dorp");
            village.setClickable(true);village.setFocusable(true);
            LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(42),dp(42));
            hp.setMargins(dp(5),0,0,0);
            row.addView(village,hp);
            village.setOnClickListener(v->move(HOME));
        }
        LinearLayout.LayoutParams rowp=new LinearLayout.LayoutParams(-1,dp(gameHeader?60:66));
        rowp.setMargins(0,0,0,dp(gameHeader?9:10));
        root.addView(row,rowp);
        if(!gameHeader && subtitle!=null && !subtitle.isEmpty())
            addText(subtitle,14,SUB,0,8);
    }
    private void showPage() {
        if(sound!=null)sound.stopSpeaking();
        if(!parentAllowed && (page==PARENT_HOME || page==PARENT_WORLDS ||
                page==PARENT_STATS || page==PARENT_SETTINGS))page=PARENT_GATE;
        playPal=null;
        final boolean villageScreen=page==HOME;
        // Android 15+ draws applications behind system bars by default.
        // WindowInsets (not guessed screen margins) defines the safe area.
        int appearance=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Build.VERSION.SDK_INT>=26)appearance|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if(Build.VERSION.SDK_INT>=30) {
            android.view.WindowInsetsController controller=getWindow().getInsetsController();
            if(controller!=null) {
                int mask=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(villageScreen?0:mask,mask);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(villageScreen?0:appearance);
        }
        getWindow().setStatusBarColor(villageScreen?0xff50308c:0xffeaf5ff);
        getWindow().setNavigationBarColor(villageScreen?0xff50308c:0xfffff6e4);
        if(Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
        stage=new FrameLayout(this);
        GradientDrawable bg=new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                villageScreen?new int[]{0xff50308c,0xff5b3b96,0xff50308c}:
                        new int[]{0xffeaf7ff,0xfffff3fb,0xfffff8e6});
        stage.setBackground(bg);
        if(page==WORLD_PICKER || page==WORLD_HUB ||
                page==AVATAR || page==CLOSET || page==ROOM ||
                page==MINI_MENU || page==STORY_LIBRARY || page==STORY_END ||
                page==REWARD || page==GARDEN_MENU || page==TROPHY_WORLDS) {
            stage.addView(new PlaylumiSkyView(this),
                    new FrameLayout.LayoutParams(-1,-1));
        }
        scroller=new ScrollView(this);scroller.setFillViewport(true);
        scroller.setClipToPadding(true);
        scroller.setVerticalScrollBarEnabled(false);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        int side=PlaylumiLayout.sidePadding(
                getResources().getConfiguration().screenWidthDp);
        root.setPadding(page==HOME?0:dp(side),page==HOME?0:dp(5),
                page==HOME?0:dp(side),page==HOME?0:dp(12));
        scroller.addView(root);
        stage.addView(scroller,new FrameLayout.LayoutParams(-1,-1));
        setContentView(stage);
        final LinearLayout currentRoot=root;
        final ScrollView currentScroller=scroller;
        stage.setOnApplyWindowInsetsListener((v,insets)->{
            if(scroller==currentScroller) {
                int top,bottom;
                if(Build.VERSION.SDK_INT>=30) {
                    android.graphics.Insets bars=insets.getInsets(
                            android.view.WindowInsets.Type.systemBars());
                    top=bars.top;bottom=bars.bottom;
                } else {
                    top=insets.getSystemWindowInsetTop();
                    bottom=insets.getSystemWindowInsetBottom();
                }
                if(villageScreen) {
                    // The entire illustrated world must fit BETWEEN the real
                    // status bar, camera cutout and 3-button/gesture navigation bar.
                    // Use live insets, so changing navigation mode is supported.
                    if(Build.VERSION.SDK_INT>=30) {
                        android.graphics.Insets safe=insets.getInsets(
                                android.view.WindowInsets.Type.statusBars()|
                                android.view.WindowInsets.Type.navigationBars()|
                                android.view.WindowInsets.Type.displayCutout());
                        top=Math.max(top,safe.top);
                        bottom=Math.max(bottom,safe.bottom);
                    }
                    stage.setPadding(0,Math.max(0,top),0,Math.max(0,bottom));
                    currentScroller.setPadding(0,0,0,0);
                } else {
                    stage.setPadding(0,0,0,0);
                    currentScroller.setPadding(0,top+dp(3),0,bottom+dp(20));
                }
            }
            return insets;
        });
        stage.requestApplyInsets();
        switch(page) {
            case AGE: showAge();break;
            case HOME: showHome();break;
            case WORLD_PICKER: showWorldPicker();break;
            case HOME_OPTIONS: showHomeOptions();break;
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
            case MINI_MENU: showMiniMenu();break;
            case MINI_PLAY: showMiniPlay();break;
            case WORLD_HUB: showWorldHub();break;
            case ADVENTURE_MAP: showAdventureMap();break;
            case ROOM: showRoom();break;
            case ROOM_CATS: showRoomCats();break;
            case ROOM_ITEMS: showRoomItems();break;
            case TROPHY_WORLDS: showTrophyWorlds();break;
            case TROPHY_CATS: showTrophyCats();break;
            case STORY_LIBRARY: showStoryLibrary();break;
            case STORY_PLAY: showStoryPlay();break;
            case STORY_REACTION: showStoryReaction();break;
            case STORY_END: showStoryEnd();break;
            case DRAW: showDrawing();break;
            case PARENT_GATE: showParentGate();break;
            case PARENT_HOME: showParentHome();break;
            case PARENT_WORLDS: showParentWorlds();break;
            case PARENT_STATS: showParentStats();break;
            case PARENT_SETTINGS: showParentSettings();break;
            case BREAK_SCREEN: showBreakScreen();break;
            case PROFILES: showProfiles();break;
            case STAR_STAGE: showStarStage();break;
            case FRIEND_CARE: showFriendCare();break;
            case FRIEND_ALBUM: showFriendAlbum();break;
            case MUSIC_PLAY: showMusicPlay();break;
            case GARDEN_MENU: showGardenMenu();break;
            case GARDEN_PLANT: showGardenPlant();break;
            case BUBBLE_PLAY: showBubblePlay();break;
            case STAR_CATCH: showStarCatch();break;
            default: throw new IllegalStateException("page");
        }
        if(!PlaylumiMotion.isReduced()) {
            stage.setAlpha(0f);stage.setTranslationX(dp(22));
            stage.animate().alpha(1).translationX(0).setDuration(280)
                    .setInterpolator(new DecelerateInterpolator()).start();
            root.post(()->{if(root==currentRoot)PlaylumiMotion.reveal(currentRoot);});
        }
    }
    private void move(int next) {
        if(moving)return;
        saveUsage();
        if(usage!=null && page!=BREAK_SCREEN && next!=BREAK_SCREEN &&
                next!=PARENT_GATE && next!=PARENT_HOME &&
                next!=PARENT_WORLDS && next!=PARENT_STATS && next!=PARENT_SETTINGS &&
                usage.shouldSuggestBreak()) {
            breakReturn=next;next=BREAK_SCREEN;saveUsage();
        }
        if(PlaylumiMotion.isReduced()) {
            page=next;showPage();return;
        }
        moving=true;
        if(sound!=null)sound.tap();
        FrameLayout previous=stage;
        final int destination=next;
        previous.animate().cancel();
        previous.animate().alpha(0f).translationX(-dp(23)).setDuration(165)
                .withEndAction(()->{
                    if(stage!=previous){moving=false;return;}
                    page=destination;moving=false;showPage();
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
    private void startEasyPlay() {
        int selected=PlaylumiEasyPlay.category(
                prefs().getInt("last_played_category",0));
        int variation=PlaylumiEasyPlay.variant(
                prefs().getInt("last_played_variant",0));
        storyStage=-1;smartDifficulty=true;roundHadMistake=false;
        pendingLevel=-1;sessionSolved=0;sessionAttempts=0;feedback="";
        pendingBadges.clear();
        difficulty=adaptive.level(selected,Math.max(0,age));
        game.configure(selected,variation,difficulty);
        saveRecent();
        move(PLAY);
    }
    /** Picture-driven village hub. Every building goes to a real existing game. */
    private void showHome() {
        PlaylumiVillageView village=new PlaylumiVillageView(this,place->{
            villageLaunch=place;
            quickCategoryPlay=false;
            switch(place) {
                case PlaylumiVillageView.ROCKET:
                    selectCategory(LearningGame.COUNT);
                    break;
                case PlaylumiVillageView.BOOKS:
                    selectCategory(LearningGame.LETTERS);
                    break;
                case PlaylumiVillageView.SHAPES:
                    miniKind=PlaylumiMiniChallenge.DRAG;
                    miniRound=0;miniSolved=false;pendingBadges.clear();
                    mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
                    move(MINI_PLAY);
                    break;
                case PlaylumiVillageView.MUSIC:
                    melodyRound=0;melodySolved=false;pendingBadges.clear();
                    melody=PlaylumiMelody.generate(Math.max(0,age),new Random());
                    move(MUSIC_PLAY);
                    break;
                case PlaylumiVillageView.PLAYGROUND:
                    move(MINI_MENU);
                    break;
                case PlaylumiVillageView.COLORS:
                    selectCategory(LearningGame.COLORS);
                    break;
                case PlaylumiVillageView.FRIEND:
                    move(AVATAR);
                    break;
                case PlaylumiVillageView.PARENTS:
                    parentAllowed=false;
                    adultA=10+new Random().nextInt(8);
                    adultB=8+new Random().nextInt(8);
                    move(PARENT_GATE);
                    break;
                case PlaylumiVillageView.SETTINGS:
                    move(HOME_OPTIONS);
                    break;
                case PlaylumiVillageView.MISSIONS:
                    world=0;storyPage=Math.min(2,adventures.progress(world)/2);
                    move(ADVENTURE_MAP);
                    break;
                case PlaylumiVillageView.REWARDS:
                    move(SHOP);
                    break;
                case PlaylumiVillageView.HOME:
                    // Already in the village.
                    break;
                case PlaylumiVillageView.COLLECTION:
                    move(TROPHY_WORLDS);
                    break;
                case PlaylumiVillageView.PROFILE:
                    move(PROFILES);
                    break;
                default:break;
            }
        },activeProfile,wardrobe==null?0:wardrobe.stars());
        // The world takes the complete usable screen; no ScrollView, blank strip,
        // clipped bottom buttons, or duplicate white space below the illustration.
        stage.removeView(scroller);
        stage.addView(village,new FrameLayout.LayoutParams(-1,-1));
    }
    private void showWorldPicker() {
        header("🌍 Kies een leerwereld","Elke wereld zit vol avonturen.");
        String[] worlds={"Kleuren, tellen en vormen","Letters, rijmen en verhalen",
                "Dieren, natuur en om ons heen"};
        for(int i=0;i<3;i++) {
            final int selected=i;
            Button b=tile(WORLD_ICONS[i]+"  "+WORLDS[i],worlds[i],
                    COLORS[i],114);
            addTile(b,7);
            b.setOnClickListener(v->{
                world=selected;categoryPage=0;storyPage=0;
                move(WORLD_HUB);
            });
        }
    }
    private void showHomeOptions() {
        header("⚙️ Instellingen","Kies wat bij jou past.");
        Button ageButton=tile("🎂 Leeftijd: "+AGES[age],"Kies je leeftijd",0xfff1e9ff,91);
        addTile(ageButton,6);
        ageButton.setOnClickListener(v->move(AGE));
        Button profile=tile("👧 Speler "+(activeProfile+1),"Wissel van speler",0xffdeeeff,91);
        addTile(profile,6);
        profile.setOnClickListener(v->move(PROFILES));
        Button soundButton=tile(sound.isEnabled()?"🔊 Geluid aan":"🔇 Geluid uit",
                "Tik om geluid te veranderen",0xffe1f4ec,87);
        addTile(soundButton,6);
        soundButton.setOnClickListener(v->{
            sound.setEnabled(!sound.isEnabled());
            prefs().edit().putBoolean("sound_on",sound.isEnabled()).apply();
            showPage();
        });
        Button parents=tile("👨‍👩‍👧 Voor ouders","Voortgang en extra instellingen",
                0xffffe9ca,91);
        addTile(parents,6);
        parents.setOnClickListener(v->{
            parentAllowed=false;
            adultA=10+new Random().nextInt(8);
            adultB=8+new Random().nextInt(8);
            move(PARENT_GATE);
        });
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
        storyStage=-1;
        quickCategoryPlay=false;
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
        addTile(badge,6);badge.setOnClickListener(v->{badgePage=0;badgesFromTrophy=false;move(BADGES);});
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
                smartDifficulty=false;
                prefs().edit().putBoolean("smart_mode",false).apply();
                roundHadMistake=false;pendingLevel=-1;
                difficulty=selected;
                game.configure(game.mode(),game.variant(),difficulty);
                saveRecent();variantPage=0;move(VARIANTS);
            });
        }
        Button smart=tile("✨ Slimme uitdaging","Playlumi past zich rustig aan jouw antwoorden aan",
                0xffd8f1e9,105);
        addTile(smart,11);
        smart.setOnClickListener(v->{
            smartDifficulty=true;
            prefs().edit().putBoolean("smart_mode",true).apply();
            roundHadMistake=false;pendingLevel=-1;
            difficulty=adaptive.level(game.mode(),Math.max(0,age));
            game.configure(game.mode(),game.variant(),difficulty);
            saveRecent();variantPage=0;move(VARIANTS);
        });
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
            Button b=tile(icons[i]+"  "+names[i],"8 opdrachten per speelronde",COLORS[i%6],115);
            addTile(b,11);
            b.setOnClickListener(v->{
                if(smartDifficulty)difficulty=adaptive.level(game.mode(),Math.max(0,age));
                game.configure(game.mode(),selection,difficulty);
                roundHadMistake=false;pendingLevel=-1;
                saveRecent();
                prefs().edit().putInt("game_variant",selection)
                        .putInt("last_played_category",game.mode())
                        .putInt("last_played_variant",selection).apply();
                storyStage=-1;
                quickCategoryPlay=false;
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
    private int sessionGoal() {
        return storyStage>=0?PlaylumiAdventure.QUESTIONS_PER_MISSION:SESSION_ROUNDS;
    }
    /**
     * Responsive game canvas: portrait stacks short cards; landscape splits the
     * question and answers into two columns. All answers remain tappable and the
     * ScrollView can still scroll when the user chooses large system fonts.
     */
    private boolean autoReadEnabled() {
        return sound!=null && sound.isEnabled() &&
                prefs().getBoolean("auto_read",PlaylumiEasyPlay.autoReadDefault(age));
    }
    /** Speak only while the originating page is still visible; TTS may initialize late. */
    private void readThisPage(String message) {
        if(!autoReadEnabled() || message==null || message.isEmpty())return;
        final FrameLayout originating=stage;
        final int screen=page;
        originating.postDelayed(()->{
            if(stage!=originating || page!=screen || !autoReadEnabled())return;
            if(!sound.speak(message))originating.postDelayed(()->{
                if(stage==originating && page==screen && autoReadEnabled())
                    sound.speak(message);
            },1100L);
        },500L);
    }
    private void showPlay() {
        int mode=game.mode();
        if(sessionSolved>=sessionGoal()) {showCompleted();return;}
        LearningGame.Round round=game.round();
        boolean wide=landscapeScreen();
        header(ICONS[mode]+" "+LearningGame.GAME_NAMES[mode],"");

        LinearLayout progressRow=new LinearLayout(this);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView counter=text("⭐ "+sessionSolved+" / "+sessionGoal(),15,0xff2c886f);
        counter.setGravity(Gravity.CENTER_VERTICAL);
        progressRow.addView(counter,new LinearLayout.LayoutParams(-2,dp(25)));
        roundStars=new PlaylumiStarTrail(this,sessionGoal(),sessionSolved);
        LinearLayout.LayoutParams bar=new LinearLayout.LayoutParams(0,dp(30),1f);
        bar.setMargins(dp(12),0,dp(2),0);
        progressRow.addView(roundStars,bar);
        LinearLayout.LayoutParams progressParams=new LinearLayout.LayoutParams(-1,dp(33));
        progressParams.setMargins(dp(3),0,dp(3),dp(6));
        root.addView(progressRow,progressParams);

        LinearLayout questions=root;
        LinearLayout answers=root;
        if(wide) {
            LinearLayout columns=new LinearLayout(this);
            columns.setOrientation(LinearLayout.HORIZONTAL);
            columns.setGravity(Gravity.TOP);
            root.addView(columns,new LinearLayout.LayoutParams(-1,-2));
            questions=new LinearLayout(this);
            questions.setOrientation(LinearLayout.VERTICAL);
            answers=new LinearLayout(this);
            answers.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftParams=new LinearLayout.LayoutParams(0,-2,1f);
            LinearLayout.LayoutParams rightParams=new LinearLayout.LayoutParams(0,-2,1f);
            leftParams.setMargins(0,0,dp(7),0);
            rightParams.setMargins(dp(7),0,0,0);
            columns.addView(questions,leftParams);
            columns.addView(answers,rightParams);
        }

        LinearLayout promptLine=new LinearLayout(this);
        promptLine.setOrientation(LinearLayout.HORIZONTAL);
        promptLine.setGravity(Gravity.CENTER_VERTICAL);
        TextView question=text(round.prompt,wide?18:20,0xff583b97);
        question.setPadding(dp(9),dp(9),dp(9),dp(9));
        question.setBackground(rounded(0xfff0e4ff,19));
        question.setMinHeight(dp(wide?80:68));
        question.setMaxLines(4);
        promptLine.addView(question,new LinearLayout.LayoutParams(0,-2,1f));
        if(sound.isEnabled()) {
            Button listen=tile("🔊","",0xffe7e3ff,46);
            listen.setTextSize(21);
            listen.setPadding(dp(3),dp(3),dp(3),dp(3));
            listen.setMinWidth(dp(46));
            listen.setContentDescription("Lees de vraag voor");
            LinearLayout.LayoutParams speakerP=new LinearLayout.LayoutParams(dp(49),dp(51));
            speakerP.setMargins(dp(6),0,0,0);
            promptLine.addView(listen,speakerP);
            listen.setOnClickListener(v->sound.speak(round.prompt));
        }
        addWidgetIn(questions,promptLine,0,dp(8));

        int pictureSize=round.display.length()>22?27:
                round.display.length()>12?30:wide?32:38;
        TextView picture=text(round.display,pictureSize,round.displayColor);
        picture.setPadding(dp(9),dp(10),dp(9),dp(10));
        picture.setBackground(rounded(Color.WHITE,22));
        picture.setMinHeight(dp(wide?150:112));
        picture.setMaxLines(4);
        picture.setElevation(dp(2));
        picture.setContentDescription("Tik op het plaatje om de vraag te horen");
        picture.setClickable(true);
        picture.setFocusable(true);
        picture.setOnClickListener(v->{
            if(sound!=null)sound.speak(round.prompt);
            PlaylumiDelight.hop(picture);
            PlaylumiDelight.sprinkle(stage,picture);
        });
        addWidgetIn(questions,picture,0,dp(wide?3:8));

        feedbackText=text("👇 Kies het juiste antwoord",wide?15:16,SUB);
        feedbackText.setMinHeight(dp(28));
        addWidgetIn(answers,feedbackText,0,dp(3));
        answerButtons.clear();
        answerLocked=round.isSolved();
        for(int i=0;i<round.answers.size();i+=2) {
            LinearLayout answerRow=new LinearLayout(this);
            answerRow.setGravity(Gravity.CENTER);
            for(int j=i;j<Math.min(i+2,round.answers.size());j++) {
                final int answer=j;
                String value=round.answers.get(j);
                Button button=tile(value,"",COLORS[(mode+j)%6],wide?60:70);
                button.setTextSize(value.length()>=13?15:value.length()>=9?17:
                        value.length()>=6?20:27);
                button.setPadding(dp(5),dp(7),dp(5),dp(7));
                button.setMaxLines(3);
                button.setEnabled(!answerLocked);
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,-2,1f);
                cell.setMargins(dp(3),dp(3),dp(3),dp(3));
                answerRow.addView(button,cell);
                answerButtons.add(button);
                button.setOnClickListener(v->choose(answer,button));
            }
            answers.addView(answerRow,new LinearLayout.LayoutParams(-1,-2));
        }
        nextButton=tile("🌟 Volgende!","",0xffcff4dd,66);
        LinearLayout.LayoutParams nextParams=new LinearLayout.LayoutParams(-1,-2);
        nextParams.setMargins(dp(4),dp(7),dp(4),dp(5));
        answers.addView(nextButton,nextParams);
        nextButton.setVisibility(answerLocked?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->continueAfterAnswer());
        if(!answerLocked)readThisPage(round.prompt);
    }
    private void addWidgetIn(LinearLayout parent,View widget,int top,int bottom) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(dp(3),top,dp(3),bottom);
        parent.addView(widget,p);
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
            if(smartDifficulty && storyStage<0) {
                if(adaptive.observe(mode,!roundHadMistake,Math.max(0,age))) {
                    pendingLevel=adaptive.level(mode,Math.max(0,age));
                    feedback="🌟 Wat knap! Een nieuwe uitdaging!";
                }
                saveAdaptive(mode);
            }
            sound.good();
            if(playPal!=null)playPal.dance();
            touched.setBackground(new RippleDrawable(ColorStateList.valueOf(0x35ffffff),
                    rounded(0xffb9f5d1,25),null));
            for(Button b:answerButtons)b.setEnabled(false);
            feedbackText.setText(feedback);
            feedbackText.setTextColor(0xff21836e);
            PlaylumiMotion.pop(touched);
            PlaylumiMotion.pop(feedbackText);
            PlaylumiDelight.sprinkle(stage,touched);
            if(sessionSolved==sessionGoal() ||
                    scores.get(mode).currentStreak%5==0)
                PlaylumiMotion.celebrate(stage,scores.get(mode).currentStreak);
            if(roundStars!=null)roundStars.showProgress(sessionSolved);
            if(sessionSolved==sessionGoal()) {
                if(storyStage>=0) {
                    storyNewReward=adventures.complete(world,storyStage);
                    if(storyNewReward) {
                        wardrobe.earnAdventureBonus();
                        prefs().edit().putInt("adventure_progress_"+world,adventures.progress(world)).apply();
                    }
                } else newBadges.addAll(scores.finishSession(mode));
            }
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
            roundHadMistake=true;
            sound.mistake();
            if(playPal!=null)playPal.react(2);
            feedbackText.setText(feedback);feedbackText.setTextColor(0xffa86a29);
            PlaylumiDelight.wiggle(touched);
            feedbackText.announceForAccessibility(feedback);
        }
        saveScore(mode);
    }
    private void advanceRegularRound() {
        if(smartDifficulty && storyStage<0 && pendingLevel>=0) {
            difficulty=pendingLevel;
            game.configure(game.mode(),game.variant(),difficulty);
            pendingLevel=-1;
        } else game.nextRound();
        roundHadMistake=false;
        saveRecent();
    }
    private void continueAfterAnswer() {
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);move(REWARD);return;
        }
        if(sessionSolved<sessionGoal()) advanceRegularRound();
        feedback="";
        move(PLAY);
    }
    private void showCompleted() {
        final FrameLayout finishedScreen=stage;
        finishedScreen.post(()->{
            if(stage==finishedScreen)PlaylumiMotion.celebrate(stage,10);
        });
        if(storyStage>=0) {
            header("🏆 Avontuur voltooid!","");
            characterHero(172);
            addText(PlaylumiAdventure.ICONS[world][storyStage]+"  "+
                    PlaylumiAdventure.TITLES[world][storyStage],24,0xff543c97,12,8);
            addText("Je hebt "+sessionGoal()+" uitdagingen opgelost!",17,SUB,0,10);
            if(storyNewReward)addText("🎁 +"+PlaylumiAdventure.BONUS+" extra speelsterren!",19,0xff27866b,0,12);
            Button map=tile("🗺️ Verder op avontuur!","",0xffd2f4dd,93);
            addTile(map,7);
            map.setOnClickListener(v->{
                storyPage=Math.min(2,adventures.progress(world)/2);
                storyStage=-1;
                move(ADVENTURE_MAP);
            });
            Button repeat=tile("🔄 Nog een keer oefenen","",0xffe7dcff,76);
            addTile(repeat,8);
            repeat.setOnClickListener(v->{
                sessionSolved=0;sessionAttempts=0;storyNewReward=false;
                game.nextRound();saveRecent();move(PLAY);
            });
            return;
        }
        header("🏆 Jij bent een kampioen!","");
        characterHero(145);
        addText("🌟 "+sessionGoal()+" vragen goed!",27,0xff4b3488,9,9);
        addText("Dat was knap! Zin in nog een ronde?",17,SUB,0,12);
        Button again=tile("🔄 Nog een ronde!","",0xffcbf1d9,92);
        addTile(again,6);
        PlaylumiDelight.invite(again);
        again.setOnClickListener(v->{
            sessionSolved=0;sessionAttempts=0;
            advanceRegularRound();move(PLAY);
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
            badge.setClickable(false);badge.setFocusable(false);
            addTile(badge,9);
        }
        Button more=tile(badgePage<2?"✨ Meer badges →":"↩ Eerste badges","",0xffe7ddff,74);
        addTile(more,12);
        more.setOnClickListener(v->{badgePage=badgePage<2?badgePage+1:0;showPage();});
    }
    private void showReward() {
        header("🎉 Nieuwe badge!","");
        TextView badgeIcon=text(PlaylumiScores.BADGE_ICONS[earnedBadge],
                77,0xffc68823);
        badgeIcon.setContentDescription("Je hebt een nieuwe badge! Tik om te fonkelen.");
        badgeIcon.setFocusable(true);badgeIcon.setClickable(true);
        addWidget(badgeIcon,12,5);
        PlaylumiDelight.invite(badgeIcon);
        badgeIcon.setOnClickListener(v->{
            sound.sparkle();
            PlaylumiDelight.sprinkle(stage,badgeIcon);
        });
        addText(PlaylumiScores.BADGE_NAMES[earnedBadge],30,0xff51378c,8,12);
        addText(PlaylumiScores.BADGE_HINTS[earnedBadge],18,SUB,0,10);
        addText("🎁 +12 speelsterren!",21,0xff2b9677,0,22);
        Button continueButton=tile("🌟 Hoera, verder!","",0xffd3f4dd,96);
        addTile(continueButton,7);
        continueButton.setOnClickListener(v->continueAfterBadge());
        FrameLayout active=stage;
        active.post(()->{if(stage==active){
            PlaylumiMotion.celebrate(active,10);
            sound.unlock();
        }});
    }
    private void continueAfterBadge() {
        if(rewardReturn==MUSIC_PLAY) {
            if(!pendingBadges.isEmpty()){
                earnedBadge=pendingBadges.remove(0);showPage();return;
            }
            earnedBadge=-1;rewardReturn=PLAY;
            advanceMelody();return;
        }
        if(rewardReturn==BUBBLE_PLAY) {
            if(!pendingBadges.isEmpty()) {
                earnedBadge=pendingBadges.remove(0);showPage();return;
            }
            earnedBadge=-1;rewardReturn=PLAY;
            advanceBubbles();return;
        }
        if(rewardReturn==MINI_PLAY) {
            if(!pendingBadges.isEmpty()) {
                earnedBadge=pendingBadges.remove(0);showPage();return;
            }
            earnedBadge=-1;rewardReturn=PLAY;
            advanceMini();return;
        }
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);showPage();return;
        }
        earnedBadge=-1;
        if(sessionSolved<sessionGoal()) advanceRegularRound();
        feedback="";move(PLAY);
    }

    /** A playful character preview rather than a crowded settings screen. */
    private void showAvatar() {
        header("⭐ Mijn stervriend","Jouw eigen magische maatje.");
        characterHero(194);
        addText("🌟 "+wardrobe.stars()+" sterren  •  "+
                wardrobe.unlockedItems()+" spulletjes",17,0xffa4781f,3,10);

        Button dress=tile("🎀 Kleedkamer","Kies outfits en kleuren",
                0xffd7f5e5,103);
        dress.setOnClickListener(v->{closetPage=0;move(CLOSET);});
        Button stageButton=tile("🎭 Sterrenpodium","Dans en zwaai",
                0xffffe8b8,103);
        stageButton.setOnClickListener(v->move(STAR_STAGE));
        addTwo(dress,stageButton,100);

        Button friendButton=tile("🐾 Vriendjes","Spelen en verzorgen",
                0xffd5f5e4,101);
        friendButton.setOnClickListener(v->{carePage=0;careMessage="";move(FRIEND_CARE);});
        Button home=tile("🏠 Mijn kamer","Versier jouw huis",
                0xffd9ecff,101);
        home.setOnClickListener(v->move(ROOM));
        addTwo(friendButton,home,100);

        Button trophies=tile("🏆 Trofeeënkamer","Bekijk al jouw badges",
                0xffffeab9,84);
        addTile(trophies,6);
        trophies.setOnClickListener(v->move(TROPHY_WORLDS));
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
        addText((shopPage+1)+" / "+((PlaylumiWardrobe.OPTIONS+1)/2),16,0xff9163bf,0,7);
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
                    if(result==PlaylumiWardrobe.PURCHASED)sound.unlock();
                    else sound.sparkle();
                    shopMessage=result==PlaylumiWardrobe.PURCHASED ?
                            "🎉 Nieuw! "+PlaylumiWardrobe.ITEM_NAMES[slot][selected]:
                            "✨ Superleuk! Aangetrokken.";
                    showPage();
                    if(character!=null)
                        PlaylumiDelight.sprinkle(stage,character);
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
        Button browse=tile(shopPage<(PlaylumiWardrobe.OPTIONS-1)/2?"🎨 Meer spullen →":"↩ Eerste spullen","",
                0xffe8ddff,76);
        addTile(browse,12);
        browse.setOnClickListener(v->{
            shopPage=shopPage<(PlaylumiWardrobe.OPTIONS-1)/2?shopPage+1:0;
            shopMessage="";showPage();
        });
    }


    private int miniCategory() {
        return miniKind==PlaylumiMiniChallenge.DRAG?LearningGame.SHAPES:
                miniKind==PlaylumiMiniChallenge.BALLOONS?LearningGame.NUMBERS:LearningGame.PATTERNS;
    }
    private void showMiniMenu() {
        header("🎪 Het speelplein","Tik een spelletje aan.");
        String[] names={"Sleep de vormen","Tik de ballon","Dierenmemory"};
        String[] notes={"Sleep naar het huisje","Vind de juiste ballon",
                "Zoek dezelfde diertjes"};
        Button[] first=new Button[3];
        for(int i=0;i<3;i++) {
            final int kind=i;
            first[i]=tile(PlaylumiMiniChallenge.ICONS[i]+"  "+names[i],
                    notes[i],COLORS[i],104);
            first[i].setOnClickListener(v->{
                miniKind=kind;miniRound=0;miniSolved=false;
                pendingBadges.clear();
                mini=PlaylumiMiniChallenge.generate(kind,Math.max(0,age),new Random());
                move(MINI_PLAY);
            });
        }
        Button bubblesButton=tile("🫧 Bellenfeest","Laat bellen knappen",
                0xffd4ebff,104);
        bubblesButton.setOnClickListener(v->{
            bubbleRound=0;bubbleSolved=false;
            bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
            pendingBadges.clear();
            move(BUBBLE_PLAY);
        });
        Button musicButton=tile("🎵 Muziekatelier","Speel een melodie",
                0xffefdcff,104);
        musicButton.setOnClickListener(v->{
            melodyRound=0;melodySolved=false;
            pendingBadges.clear();
            melody=PlaylumiMelody.generate(Math.max(0,age),new Random());
            move(MUSIC_PLAY);
        });
        addTwo(first[0],first[1],103);
        addTwo(first[2],bubblesButton,103);
        Button stars=tile("🌟 Vang de sterren","Tik rustig sterren aan",0xffffe8ba,104);
        stars.setOnClickListener(v->{starCatchRound=0;starCatchSolved=false;starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());move(STAR_CATCH);});
        addTwo(musicButton,stars,103);
    }

    private void showMiniPlay() {
        if(mini==null)mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
        header(PlaylumiMiniChallenge.ICONS[miniKind]+" "+PlaylumiMiniChallenge.NAMES[miniKind],
                "Uitdaging "+Math.min(miniRound+1,20)+" / 20");
        if(miniRound>=20) {
            characterHero(150);
            addText("🎉 Alle uitdagingen gespeeld!",25,0xff4e3889,13,9);
            Button again=tile("🔄 Nog eens spelen!","",0xffd4f5e0,96);
            addTile(again,9);
            again.setOnClickListener(v->{
                miniRound=0;miniSolved=false;
                mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
                move(MINI_PLAY);
            });
            Button back=tile("🎪 Andere minigame","",0xffe8dfff,86);
            addTile(back,8);back.setOnClickListener(v->move(MINI_MENU));
            return;
        }
        if(sound.isEnabled()) {
            Button listen=tile("🔊 Luister naar de opdracht","",0xffeee4ff,42);
            listen.setTextSize(15);
            listen.setPadding(dp(6),dp(5),dp(6),dp(5));
            LinearLayout.LayoutParams speakParams=new LinearLayout.LayoutParams(-1,dp(44));
            speakParams.setMargins(dp(5),0,dp(5),dp(2));
            root.addView(listen,speakParams);
            listen.setOnClickListener(v->sound.speak(mini.question));
        }
        PlaylumiMiniBoard board=new PlaylumiMiniBoard(this,mini,new PlaylumiMiniBoard.Listener() {
            @Override public void onSolved() {
                if(miniSolved)return;
                miniSolved=true;
                if(playPal!=null)playPal.dance();
                recordMiniAnswer();
                if(nextButton!=null) {
                    nextButton.setVisibility(View.VISIBLE);
                    PlaylumiMotion.pop(nextButton);
                    scroller.post(()->scroller.smoothScrollTo(0,
                            Math.max(0,root.getHeight()-scroller.getHeight())));
                }
                PlaylumiMotion.celebrate(stage,miniRound+1);
            }
            @Override public void onMistake() {
                sound.mistake();
                if(playPal!=null)playPal.react(2);
                // Memory pairs are practice, not a penalized test.
            }
            @Override public void onMatch() { sound.good(); }
        });
        LinearLayout.LayoutParams boardParams=new LinearLayout.LayoutParams(-1,-2);
        boardParams.setMargins(dp(0),dp(2),dp(0),dp(4));
        root.addView(board,boardParams);
        nextButton=tile("🌟 Verder spelen!","",0xffc9f2dc,66);
        addTile(nextButton,4);
        nextButton.setVisibility(miniSolved?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->afterMiniSolve());
        if(!miniSolved)readThisPage(mini.question);
    }
    private void recordMiniAnswer() {
        int category=miniCategory();
        ArrayList<Integer> newBadges=new ArrayList<>(scores.answer(category,true));
        lifetimeCount++;
        wardrobe.earnAnswer();
        wardrobe.earnBadges(newBadges.size());
        saveWardrobe();
        saveScore(category);
        pendingBadges.addAll(newBadges);
        sound.good();
    }
    private void afterMiniSolve() {
        miniRound++;
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);
            rewardReturn=MINI_PLAY;
            move(REWARD);
            return;
        }
        advanceMini();
    }
    private void advanceMini() {
        mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
        miniSolved=false;
        move(MINI_PLAY);
    }


    private void showWorldHub() {
        header(WORLD_ICONS[world]+" "+WORLDS[world],"Hoe wil jij vandaag spelen?");
        characterHero(141);
        addText("🌟 "+adventures.progress(world)+" van 6 avonturen ontdekt",16,0xff397f6f,4,12);
        Button story=tile("🗺️ Op avontuur!","Ontdek nieuwe plekken, één voor één",
                0xffd9efff,115);
        addTile(story,10);
        story.setOnClickListener(v->{
            storyPage=Math.min(2,adventures.progress(world)/2);
            move(ADVENTURE_MAP);
        });
        Button free=tile("🎲 Zelf een spel kiezen","Alle spellen in deze wereld",0xffffe2b8,111);
        addTile(free,10);
        free.setOnClickListener(v->{categoryPage=0;move(CATEGORIES);});
        if(world==0) {
            Button drawing=tile("🖍️ Tekenstudio","Teken je eigen kunstwerk",0xffe9d9ff,103);
            addTile(drawing,8);
            drawing.setOnClickListener(v->move(DRAW));
        } else if(world==1) {
            Button books=tile("📖 Verhalenbos","Kies je eigen avontuur in twaalf verhalen",0xffe9d9ff,103);
            addTile(books,8);
            books.setOnClickListener(v->{storyLibraryPage=0;move(STORY_LIBRARY);});
        }
    }
    private void showAdventureMap() {
        header("🗺️ Op avontuur!","Volg de sterren en ontdek nieuwe plekken");
        addText("🌟 "+adventures.progress(world)+" van 6 avonturen klaar",19,0xff35816b,0,8);
        addText(storyPage==0?"● ○ ○":storyPage==1?"● ● ○":"● ● ●",15,0xff876ac1,0,12);
        int start=storyPage*2;
        for(int i=start;i<Math.min(start+2,PlaylumiAdventure.STAGES);i++) {
            final int stageIndex=i;
            boolean open=adventures.available(world,i);
            boolean done=adventures.completed(world,i);
            String name=PlaylumiAdventure.TITLES[world][i];
            String icon=open?PlaylumiAdventure.ICONS[world][i]:"🔒";
            String detail=done?"✅ Avontuur voltooid":
                    open?"🎁 "+PlaylumiAdventure.BONUS+" sterren bij je eerste voltooiing":
                    "Speel eerst het vorige avontuur";
            Button b=tile(icon+"  "+name,detail,done?0xffdaf2e7:
                    open?COLORS[i%COLORS.length]:0xffede9f0,112);
            b.setEnabled(open);
            addTile(b,9);
            b.setOnClickListener(v->{
                storyStage=stageIndex;storyNewReward=false;
                smartDifficulty=false;roundHadMistake=false;pendingLevel=-1;
                int mode=adventures.category(world,stageIndex);
                game.configure(mode,adventures.variant(stageIndex),Math.max(0,age));
                sessionSolved=0;sessionAttempts=0;
                pendingBadges.clear();
                saveRecent();
                move(PLAY);
            });
        }
        Button more=tile(storyPage<2?"✨ Verder op het pad →":"↩ Begin van het pad","",
                0xffe9e0ff,80);
        addTile(more,14);
        more.setOnClickListener(v->{
            storyPage=storyPage<2?storyPage+1:0;
            showPage();
        });
    }


    private void roomPreview(int height) {
        PlaylumiRoomView view=new PlaylumiRoomView(this,room,wardrobe);
        int available=PlaylumiLayout.sceneHeight(height,landscapeScreen(),
                getResources().getConfiguration().screenHeightDp);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(available));
        p.setMargins(dp(3),dp(3),dp(3),dp(8));
        root.addView(view,p);
    }
    private void showRoom() {
        header("🏠 Mijn eigen kamer","Welkom thuis, stervriend!");
        roomPreview(310);
        addText("🌟 "+wardrobe.stars()+" speelsterren",20,0xffa87829,0,6);
        addText(room.ownedItems()+" van "+(PlaylumiRoom.SLOTS*PlaylumiRoom.OPTIONS)+" kamerdingen vrijgespeeld",14,SUB,0,10);
        Button decorate=tile("🎨 Richt je kamer in!","Kies kleuren, meubels en versieringen",
                0xffd5f2df,109);
        addTile(decorate,8);
        decorate.setOnClickListener(v->{roomPage=0;move(ROOM_CATS);});
        Button gardening=tile("🌱 Mijn Magische Tuin","Plant en verzorg twaalf bijzondere planten",
                0xffd5f4de,111);
        addTile(gardening,8);
        gardening.setOnClickListener(v->{gardenPage=0;move(GARDEN_MENU);});
        Button friend=tile("⭐ Terug naar je stervriend","",0xffe8dcff,77);
        addTile(friend,8);
        friend.setOnClickListener(v->move(AVATAR));
    }
    private void showRoomCats() {
        header("🎨 Versier je kamer","Wat gaan we vandaag veranderen?");
        roomPreview(184);
        addText("🌟 "+wardrobe.stars()+" speelsterren",18,0xffa87829,0,6);
        addText((roomPage+1)+" / 3",15,0xff8a63b3,0,7);
        int start=roomPage*2;
        for(int i=start;i<Math.min(start+2,PlaylumiRoom.SLOTS);i++) {
            final int slot=i;
            Button b=tile(PlaylumiRoom.SLOT_ICONS[i]+"  "+PlaylumiRoom.LABELS[i],
                    "Nu: "+PlaylumiRoom.NAMES[i][room.selected(i)],COLORS[i%6],95);
            addTile(b,7);
            b.setOnClickListener(v->{
                roomSlot=slot;roomItemPage=0;roomMessage="";
                move(ROOM_ITEMS);
            });
        }
        Button more=tile(roomPage<2?"✨ Meer kameronderdelen →":"↩ Terug naar de kleuren","",
                0xffe8deff,78);
        addTile(more,12);
        more.setOnClickListener(v->{
            roomPage=roomPage<2?roomPage+1:0;
            showPage();
        });
    }
    private void showRoomItems() {
        int slot=roomSlot;
        header(PlaylumiRoom.SLOT_ICONS[slot]+" "+PlaylumiRoom.LABELS[slot],
                "Wat past bij jouw kamer?");
        roomPreview(203);
        addText("🌟 "+wardrobe.stars()+" speelsterren",20,0xffa87829,0,5);
        if(!roomMessage.isEmpty())addText(roomMessage,16,0xff4e4291,0,8);
        addText((roomItemPage+1)+" / "+((PlaylumiRoom.OPTIONS+1)/2),15,0xff9163bd,0,6);
        for(int i=roomItemPage*2;i<Math.min(roomItemPage*2+2,PlaylumiRoom.OPTIONS);i++) {
            final int item=i;
            boolean selected=room.selected(slot)==i;
            boolean owned=room.owned(slot,i);
            String desc=selected?"✅ Heb je nu":owned?"✨ Tik om te gebruiken":
                    "🌟 "+room.cost(slot,i)+" speelsterren";
            Button b=tile(PlaylumiRoom.ICONS[slot][i]+"  "+PlaylumiRoom.NAMES[slot][i],desc,
                    selected?0xffd2f5db:COLORS[(slot+i)%6],102);
            addTile(b,8);
            b.setOnClickListener(v->{
                int price=room.cost(slot,item);
                if(price>wardrobe.stars() || !wardrobe.spendStars(price)) {
                    roomMessage="💛 Nog "+Math.max(0,price-wardrobe.stars())+" speelsterren nodig.";
                    showPage();return;
                }
                boolean first=!room.owned(slot,item);
                room.unlockAndSelect(slot,item);
                saveRoom();saveWardrobe();
                if(first)sound.unlock();else sound.sparkle();
                roomMessage=first?"🎉 Nieuw item! Je kamer ziet er anders uit.":
                        "✨ Mooi! De kamer is aangepast.";
                showPage();
                if(first) {
                    FrameLayout active=stage;
                    active.post(()->{if(stage==active)PlaylumiMotion.celebrate(active,10);});
                }
            });
        }
        Button more=tile(roomItemPage<(PlaylumiRoom.OPTIONS-1)/2?"🎁 Meer spulletjes →":"↩ Eerste spulletjes","",
                0xffe8deff,76);
        addTile(more,10);
        more.setOnClickListener(v->{
            roomItemPage=roomItemPage<(PlaylumiRoom.OPTIONS-1)/2?roomItemPage+1:0;
            roomMessage="";showPage();
        });
    }


    private int trophiesInWorld(int selectedWorld) {
        int total=0;
        for(int i=selectedWorld*8;i<selectedWorld*8+8;i++)
            total+=scores.get(i).earnedBadges();
        return total;
    }
    private void showTrophyWorlds() {
        header("🏆 Mijn trofeeënkamer","Kijk wat je allemaal al kunt!");
        characterHero(137);
        int all=0;
        for(int i=0;i<3;i++)all+=trophiesInWorld(i);
        addText("✨ "+all+" van 144 badges verzameld",20,0xffa67929,7,12);
        for(int w=0;w<3;w++) {
            final int chosen=w;
            Button b=tile(WORLD_ICONS[w]+"  "+WORLDS[w],
                    "🏅 "+trophiesInWorld(w)+" van 48 badges",COLORS[w],107);
            addTile(b,9);
            b.setOnClickListener(v->{
                trophyWorld=chosen;trophyPage=0;
                move(TROPHY_CATS);
            });
        }
    }
    private void showTrophyCats() {
        header("🏅 "+WORLDS[trophyWorld],"Kies een spel om de badges te bekijken");
        addText("✨ "+trophiesInWorld(trophyWorld)+" van 48",18,0xffa67929,2,9);
        addText((trophyPage+1)+" / 4",15,0xff946ac0,0,8);
        int from=trophyWorld*8+trophyPage*2;
        for(int category=from;category<from+2;category++) {
            final int selected=category;
            PlaylumiScores.Score score=scores.get(category);
            String label=ICONS[category]+"  "+LearningGame.GAME_NAMES[category];
            Button b=tile(label,"🏅 "+score.earnedBadges()+" van 6 verdiend",
                    COLORS[category%6],117);
            addTile(b,8);
            b.setOnClickListener(v->{
                badgesFromTrophy=true;
                badgePage=0;
                game.configure(selected,0,Math.max(0,age));
                move(BADGES);
            });
        }
        Button more=tile(trophyPage<3?"✨ Meer trofeeën →":"↩ Eerste trofeeën","",
                0xffe5daff,76);
        addTile(more,12);
        more.setOnClickListener(v->{
            trophyPage=trophyPage<3?trophyPage+1:0;
            showPage();
        });
    }


    private void showStoryLibrary() {
        header("📖 Het Verhalenbos","Jij beslist hoe het verhaal verdergaat!");
        addText("📚 "+stories.completedCount()+" van "+PlaylumiStories.NAMES.length+" verhalen ontdekt",18,0xff36836d,0,9);
        addText((storyLibraryPage+1)+" / "+((PlaylumiStories.NAMES.length+1)/2),15,0xff9771bb,0,9);
        for(int i=storyLibraryPage*2;i<Math.min(storyLibraryPage*2+2,PlaylumiStories.NAMES.length);i++) {
            final int story=i;
            String subtitle=stories.completed(i)?"🌟 Al eens gelezen":
                    "✨ Een nieuw avontuur";
            Button book=tile(PlaylumiStories.ICONS[i]+"  "+PlaylumiStories.NAMES[i],
                    subtitle,COLORS[i%6],121);
            addTile(book,10);
            book.setOnClickListener(v->{
                storyJustCompleted=false;
                if(stories.currentStory()!=story || stories.finished())
                    stories.start(story);
                saveStories();
                move(stories.choice()>=0?STORY_REACTION:STORY_PLAY);
            });
        }
        Button more=tile(storyLibraryPage<(PlaylumiStories.NAMES.length-1)/2?"📚 Meer verhalen →":"↩ Eerste verhalen","",
                0xffe8ddff,78);
        addTile(more,14);
        more.setOnClickListener(v->{
            storyLibraryPage=storyLibraryPage<(PlaylumiStories.NAMES.length-1)/2?storyLibraryPage+1:0;
            showPage();
        });
    }
    private void showStoryPlay() {
        if(stories.finished()) {showStoryEnd();return;}
        int id=stories.currentStory();
        PlaylumiStories.Scene scene=stories.scene();
        header(PlaylumiStories.ICONS[id]+" "+PlaylumiStories.NAMES[id],
                "Hoofdstuk "+(stories.step()+1)+" / 4");

        TextView illustration=text(scene.emoji,landscapeScreen()?45:61,0xff8661b6);
        illustration.setContentDescription("Tik op de plaat om te toveren");
        illustration.setClickable(true);illustration.setFocusable(true);
        addWidget(illustration,2,6);
        illustration.setOnClickListener(v->{
            PlaylumiDelight.wiggle(illustration);
            PlaylumiDelight.sprinkle(stage,illustration);
            if(sound!=null)sound.sparkle();
        });
        LinearLayout storyCard=new LinearLayout(this);
        storyCard.setGravity(Gravity.CENTER_VERTICAL);
        storyCard.setBackground(rounded(0xfff0e4ff,22));
        TextView situation=text(scene.prompt,landscapeScreen()?19:21,0xff563c8d);
        situation.setPadding(dp(12),dp(13),dp(8),dp(13));
        situation.setMinHeight(dp(90));
        situation.setMaxLines(5);
        storyCard.addView(situation,new LinearLayout.LayoutParams(0,-2,1));
        if(sound.isEnabled()) {
            Button speak=tile("🔊","",0xffe6e0ff,49);
            speak.setTextSize(22);speak.setPadding(dp(2),dp(2),dp(2),dp(2));
            speak.setContentDescription("Lees het verhaal voor");
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(53),dp(55));
            p.setMargins(0,0,dp(7),0);
            storyCard.addView(speak,p);
            speak.setOnClickListener(v->sound.speak(scene.prompt));
        }
        addWidget(storyCard,0,8);
        addText("Wat kies jij?",16,SUB,4,4);
        readThisPage(scene.prompt);
        String[] options={scene.first,scene.second};
        String[] emojis={"🌼","✨"};
        for(int i=0;i<2;i++) {
            final int choice=i;
            Button b=tile(emojis[i]+"  "+options[i],"",COLORS[(id+i)%6],91);
            addTile(b,6);
            b.setOnClickListener(v->{
                stories.choose(choice);saveStories();
                sound.good();move(STORY_REACTION);
            });
        }
    }
    private void showStoryReaction() {
        int id=stories.currentStory();
        PlaylumiStories.Scene scene=stories.scene();
        String result=stories.choice()==0?scene.firstResult:scene.secondResult;
        header("✨ Jouw avontuur","Hoofdstuk "+(stories.step()+1)+" / 4");
        TextView illustration=text(scene.emoji,landscapeScreen()?44:61,0xff7856b4);
        illustration.setContentDescription("Tik om het verhaal te laten fonkelen");
        illustration.setClickable(true);illustration.setFocusable(true);
        addWidget(illustration,3,7);
        illustration.setOnClickListener(v->{
            PlaylumiDelight.hop(illustration);
            PlaylumiDelight.sprinkle(stage,illustration);
        });
        TextView reaction=text(result,landscapeScreen()?19:21,0xff533b87);
        reaction.setPadding(dp(13),dp(14),dp(13),dp(14));
        reaction.setMinHeight(dp(106));
        reaction.setBackground(rounded(0xffffe9cf,23));
        addWidget(reaction,0,9);
        readThisPage(result);
        if(sound.isEnabled()) {
            Button speak=tile("🔊 Vertel wat er gebeurt","",0xffe6e0ff,55);
            addTile(speak,4);
            speak.setOnClickListener(v->sound.speak(result));
        }
        Button next=tile("🌟 Verder met het verhaal","",0xffd1f3dd,82);
        addTile(next,8);
        next.setOnClickListener(v->{
            stories.next();saveStories();
            move(stories.finished()?STORY_END:STORY_PLAY);
        });
    }
    private void showStoryEnd() {
        int id=stories.currentStory();
        if(stories.awardOnce()) {
            storyJustCompleted=true;
            wardrobe.earnStoryBonus();saveWardrobe();
            saveStories();
        }
        header("🎉 Het einde!","");
        characterHero(149);
        addText(PlaylumiStories.ICONS[id]+"  "+PlaylumiStories.NAMES[id],
                23,0xff654695,7,10);
        TextView ending=text(stories.ending(),23,0xff523e8b);
        ending.setPadding(dp(15),dp(20),dp(15),dp(20));
        ending.setMinHeight(dp(140));
        ending.setBackground(rounded(0xfff4e5ff,28));
        addWidget(ending,0,10);
        if(storyJustCompleted)
            addText("🎁 +15 speelsterren voor jouw nieuwe verhaal!",17,0xff31856d,0,15);
        Button back=tile("📖 Een nieuw verhaal kiezen","",0xffd2f3dd,92);
        addTile(back,10);
        back.setOnClickListener(v->move(STORY_LIBRARY));
        FrameLayout current=stage;
        current.post(()->{if(stage==current)PlaylumiMotion.celebrate(current,10);});
    }
    private void showDrawing() {
        header("🖍️ Mijn Tekenstudio","Teken met je vinger. Alles mag!");
        drawing=new PlaylumiDrawingView(this,new File(getFilesDir(),PlaylumiProfiles.artworkFile(activeProfile)));
        drawing.setElevation(dp(5));
        LinearLayout.LayoutParams drawParams=new LinearLayout.LayoutParams(-1,
                dp(PlaylumiLayout.drawingHeight(landscapeScreen(),
                        getResources().getConfiguration().screenHeightDp)));
        drawParams.setMargins(dp(4),dp(8),dp(4),dp(12));
        root.addView(drawing,drawParams);
        drawingInfo=text("🎨 Kies jouw lievelingskleur",17,0xff7157a7);
        addWidget(drawingInfo,0,9);
        String[] symbols={"🌺","💜","💙","💚","🌟","🖤"};
        for(int r=0;r<2;r++) {
            LinearLayout row=new LinearLayout(this);
            for(int c=0;c<3;c++) {
                int idx=r*3+c;
                Button button=tile(symbols[idx],"",COLORS[idx],56);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(57),1f);
                p.setMargins(dp(4),dp(4),dp(4),dp(4));
                row.addView(button,p);
                final int ink=PlaylumiDrawingView.PALETTE[idx];
                button.setOnClickListener(v->{
                    eraseAsked=false;
                    drawing.color(ink);
                    drawingInfo.setText("✨ Mooie kleur! Teken maar.");
                    sound.tap();
                });
            }
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        Button undo=tile("↩  Laatste lijn weg","",0xffe6dfff,68);
        undo.setOnClickListener(v->{
            eraseAsked=false;
            drawing.undo();
            drawingInfo.setText("Laatste lijn weggehaald");
        });
        Button save=tile("💾 Bewaar tekening","",0xffd0f3dd,77);
        addTwo(undo,save,73);
        save.setOnClickListener(v->{
            eraseAsked=false;
            if(!drawing.save()) {
                drawingInfo.setText("Oeps! Probeer nog eens te bewaren.");
                return;
            }
            boolean first=!prefs().getBoolean("drawing_saved_once",false);
            if(first) {
                prefs().edit().putBoolean("drawing_saved_once",true).apply();
                wardrobe.earnCreativeBonus();saveWardrobe();
                drawingInfo.setText("🌟 Kunstwerk bewaard! +9 speelsterren!");
                FrameLayout current=stage;
                current.post(()->{if(stage==current)PlaylumiMotion.celebrate(current,10);});
            } else drawingInfo.setText("💜 Jouw kunstwerk is veilig bewaard!");
            sound.good();
        });
        Button clear=tile("🧽 Begin met leeg papier","",0xffffe9df,65);
        addTile(clear,9);
        clear.setOnClickListener(v->{
            if(!eraseAsked) {
                eraseAsked=true;
                drawingInfo.setText("Tik nog eens op wissen als je zeker bent.");
            } else {
                drawing.clear();eraseAsked=false;
                drawingInfo.setText("🎨 Een nieuw wit papier! Veel plezier.");
            }
        });
    }


    private void showParentGate() {
        header("🔐 Alleen voor ouders","Een kleine rekensom om verder te gaan.");
        addText("Hoeveel is",22,SUB,25,7);
        addText(adultA+" + "+adultB+" = ?",34,0xff5e4098,7,20);
        java.util.List<Integer> answers=new java.util.ArrayList<>();
        answers.add(adultA+adultB-2);
        answers.add(adultA+adultB);
        answers.add(adultA+adultB+3);
        java.util.Collections.shuffle(answers,new Random());
        for(Integer value:answers) {
            int answer=value.intValue();
            Button b=tile("🔢 "+answer,"",COLORS[(value+6)%6],93);
            addTile(b,8);
            b.setOnClickListener(v->{
                if(answer==adultA+adultB) {
                    parentAllowed=true;
                    sound.good();move(PARENT_HOME);
                } else {
                    sound.mistake();
                    adultA=10+new Random().nextInt(8);
                    adultB=8+new Random().nextInt(8);
                    showPage();
                }
            });
        }
    }
    private void showParentHome() {
        header("👨‍👩‍👧 Voor ouders","Inzicht en instellingen, alles blijft op dit toestel.");
        saveUsage();
        int correct=0,attempts=0,badges=0;
        for(int i=0;i<PlaylumiScores.CATEGORIES;i++) {
            correct+=scores.get(i).correct;
            attempts+=scores.get(i).attempts;
            badges+=scores.get(i).earnedBadges();
        }
        addText("⭐ "+correct+" goede antwoorden",23,0xff34856d,10,6);
        addText("🏅 "+badges+" van 144 badges verdiend",19,0xff9166b7,2,6);
        addText("⏱️ Vandaag ongeveer "+usage.minutes()+" minuten gespeeld",16,SUB,0,12);
        Button overview=tile("📊 Bekijk de voortgang","Scores en badges per leerwereld",
                0xffd7eefe,104);
        addTile(overview,9);
        overview.setOnClickListener(v->move(PARENT_WORLDS));
        Button settings=tile("⚙️ Instellingen","Geluid, animaties en pauzeherinnering",
                0xffe8dcff,104);
        addTile(settings,9);
        settings.setOnClickListener(v->move(PARENT_SETTINGS));
        Button back=tile("🌈 Terug naar spelen","",0xffd4f3e0,85);
        addTile(back,11);
        back.setOnClickListener(v->{parentAllowed=false;move(HOME);});
    }
    private void showParentWorlds() {
        header("📊 Voortgang","Kies een leerwereld.");
        for(int w=0;w<3;w++) {
            final int index=w;
            int correct=0;
            for(int i=w*8;i<w*8+8;i++)correct+=scores.get(i).correct;
            Button b=tile(WORLD_ICONS[w]+"  "+WORLDS[w],
                    "⭐ "+correct+" goed beantwoorde opdrachten",
                    COLORS[w],104);
            addTile(b,10);
            b.setOnClickListener(v->{parentWorld=index;parentStatsPage=0;move(PARENT_STATS);});
        }
    }
    private void showParentStats() {
        header(WORLD_ICONS[parentWorld]+" "+WORLDS[parentWorld],
                "Overzicht per leeractiviteit");
        addText("Pagina "+(parentStatsPage+1)+" / 4",16,0xff8766b0,0,12);
        for(int i=parentWorld*8+parentStatsPage*2;i<parentWorld*8+parentStatsPage*2+2;i++) {
            PlaylumiScores.Score score=scores.get(i);
            String title=ICONS[i]+"  "+LearningGame.GAME_NAMES[i];
            String info="⭐ "+score.correct+" goed  •  "+score.attempts+
                    " pogingen  •  🏅 "+score.earnedBadges()+"/6 badges";
            Button b=tile(title,info,COLORS[i%6],130);
            b.setClickable(false);b.setFocusable(false);
            addTile(b,9);
        }
        Button more=tile(parentStatsPage<3?"Meer categorieën →":"↩ Eerste categorieën","",
                0xffe8dcff,80);
        addTile(more,12);
        more.setOnClickListener(v->{
            parentStatsPage=parentStatsPage<3?parentStatsPage+1:0;
            showPage();
        });
    }
    private void showParentSettings() {
        header("⚙️ Instellingen","Pas de speelervaring rustig aan.");
        Button soundButton=tile(sound.isEnabled()?"🔊 Geluid: aan":"🔇 Geluid: uit",
                "Tik om te veranderen",0xffd6efff,90);
        addTile(soundButton,7);
        soundButton.setOnClickListener(v->{
            sound.setEnabled(!sound.isEnabled());
            prefs().edit().putBoolean("sound_on",sound.isEnabled()).apply();
            showPage();
        });
        boolean narrationPref=prefs().getBoolean("auto_read",
                PlaylumiEasyPlay.autoReadDefault(age));
        Button narration=tile(narrationPref?
                "🗣️ Automatisch voorlezen: aan":"🗣️ Automatisch voorlezen: uit",
                "Werkt wanneer geluid aanstaat",0xffdef2e5,89);
        addTile(narration,7);
        narration.setOnClickListener(v->{
            boolean enabled=prefs().getBoolean("auto_read",
                    PlaylumiEasyPlay.autoReadDefault(age));
            prefs().edit().putBoolean("auto_read",!enabled).apply();
            showPage();
        });
        Button motion=tile(PlaylumiMotion.isReduced()?
                "🍃 Rustige animaties: aan":"✨ Bewegingen: aan",
                "Rustige modus beperkt beweging",0xffe6dcff,91);
        addTile(motion,7);
        motion.setOnClickListener(v->{
            PlaylumiMotion.setReduced(!PlaylumiMotion.isReduced());
            prefs().edit().putBoolean("reduced_motion",PlaylumiMotion.isReduced()).apply();
            showPage();
        });
        addText("⏰ Pauzeherinnering",22,0xff58448a,18,7);
        addText("Een vriendelijk seintje, nooit een verplichte stop.",15,SUB,0,10);
        int[] mins={0,15,30,45,60};
        String[] labels={"Geen herinnering","15 minuten","30 minuten","45 minuten","60 minuten"};
        Button[] choices=new Button[mins.length];
        for(int i=0;i<mins.length;i++) {
            final int selected=mins[i];
            boolean active=usage.limit()==selected;
            choices[i]=tile((active?"✅ ":"⏱️ ")+labels[i],"",
                    active?0xffcef3d9:COLORS[i%6],72);
            choices[i].setContentDescription(labels[i]+
                    (active?", geselecteerd":", tik om in te stellen"));
            choices[i].setOnClickListener(v->{
                usage.restore(usage.seconds(),selected,false);
                saveUsage();showPage();
            });
        }
        addTile(choices[0],4);
        addTwo(choices[1],choices[2],72);
        addTwo(choices[3],choices[4],72);
    }
    private void showBreakScreen() {
        header("🍃 Tijd voor een pauze","Even rust is ook fijn!");
        characterHero(165);
        addText("Je hebt vandaag al veel ontdekt.",22,0xff58448a,20,12);
        Button rest=tile("🌿 Even lekker uitrusten","",0xffd6f4e2,103);
        addTile(rest,10);
        rest.setOnClickListener(v->move(HOME));
        Button continueButton=tile("🌟 Nog even verder spelen","",0xffe6deff,94);
        addTile(continueButton,8);
        continueButton.setOnClickListener(v->move(breakReturn));
    }


    private void showProfiles() {
        header("👧 Kies jouw speler","Iedere speler heeft een eigen wereld.");
        String[] images=PlaylumiProfiles.ICONS;
        for(int i=0;i<PlaylumiProfiles.COUNT;i++) {
            final int slot=i;
            SharedPreferences player=i==0?getPreferences(MODE_PRIVATE):
                    getSharedPreferences(PlaylumiProfiles.preferences(i),MODE_PRIVATE);
            int selectedAge=player.getInt("chosen_age",-1);
            String ageNote=selectedAge<0?"Nog niet begonnen":AGES[Math.min(2,selectedAge)];
            String subtitle=(i==activeProfile?"✅ Nu aan het spelen  •  ":"")+
                    ageNote+"  •  ⭐ "+player.getInt("completed_all",0)+" goed";
            Button b=tile(images[i]+"  Speler "+(i+1),subtitle,
                    i==activeProfile?0xffcbf3dc:COLORS[i],118);
            addTile(b,10);
            b.setOnClickListener(v->{
                if(slot==activeProfile){move(HOME);return;}
                saveUsage();
                if(page==DRAW && drawing!=null)drawing.save();
                getSharedPreferences("playlumi-global",MODE_PRIVATE).edit()
                        .putInt("active_profile",slot).apply();
                recreate();
            });
        }
    }


    /** A toy stage: children choose a single funny movement at a time. */
    private void showStarStage() {
        header("🎭 Sterrenpodium","Tik op een plaatje!");
        characterHero(206);
        addText((starStagePage+1)+" / 2",14,0xff9b7bab,1,8);

        if(starStagePage==0) {
            Button dance=tile("💃 Dansje!","",0xffffe1bc,93);
            addTile(dance,5);
            dance.setOnClickListener(v->{
                if(character!=null) {
                    character.dance();
                    PlaylumiDelight.hop(character);
                    PlaylumiDelight.sprinkle(stage,character);
                }
                sound.dance();
            });
            Button wave=tile("👋 Zwaaien!","",0xffd6f3e1,93);
            addTile(wave,5);
            wave.setOnClickListener(v->{
                if(character!=null) {
                    character.wave();
                    PlaylumiDelight.wiggle(character);
                }
                sound.hello();
            });
            Button magic=tile("✨ Toverkunst!","",0xffe8d9ff,93);
            addTile(magic,5);
            magic.setOnClickListener(v->{
                if(character!=null)character.twirl();
                sound.sparkle();
                PlaylumiMotion.celebrate(stage,10);
            });
        } else {
            Button surprise=tile("😲 Verrassing!","",0xffffe2c7,93);
            addTile(surprise,5);
            surprise.setOnClickListener(v->{
                if(character!=null) {
                    character.surprised();
                    PlaylumiDelight.sprinkle(stage,character);
                }
                sound.pop();
            });
            Button spin=tile("🌀 Rondje draaien!","",0xffd9efff,93);
            addTile(spin,5);
            spin.setOnClickListener(v->{
                if(character!=null){
                    character.twirl();
                    PlaylumiDelight.spin(character);
                }
                sound.dance();
            });
            Button party=tile("🎉 Groot sterrenfeest!","",0xffffeabc,93);
            addTile(party,5);
            party.setOnClickListener(v->{
                if(character!=null)character.dance();
                sound.unlock();
                PlaylumiMotion.celebrate(stage,10);
            });
        }
        Button more=tile(starStagePage==0?"🌟 Nog meer trucjes →":
                "↩ Eerste trucjes","",0xffece4ff,70);
        addTile(more,8);
        more.setOnClickListener(v->{
            starStagePage=1-starStagePage;
            showPage();
        });
    }


    /** Ten calm rounds; progress is kept separate from existing score/rewards. */
    private void showStarCatch() {
        header("🌟 Vang de sterren","Ronde "+Math.min(starCatchRound+1,PlaylumiStarCatch.ROUNDS)+" / "+PlaylumiStarCatch.ROUNDS);
        if(starCatchRound>=PlaylumiStarCatch.ROUNDS){
            characterHero(140);
            addText("🏆 Alle sterren gevangen!",24,0xff59438f,8,12);
            Button again=tile("🌟 Speel nog eens","",0xffd4f4e2,88);
            addTile(again,8);
            again.setOnClickListener(v->{starCatchRound=0;starCatchSolved=false;starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());move(STAR_CATCH);});
            Button back=tile("🎪 Andere spelletjes","",0xffe8dfff,80);
            addTile(back,7);back.setOnClickListener(v->move(MINI_MENU));return;
        }
        if(starCatch==null)starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
        final PlaylumiStarCatch round=starCatch;
        addText("Tik "+round.target()+" sterren aan. Geen tijdslimiet!",20,0xff60458e,5,7);
        starCatchProgress=text("⭐ "+round.collected()+" / "+round.target(),21,0xff298064);
        addWidget(starCatchProgress,2,6);
        if(sound.isEnabled()){
            Button listen=tile("🔊 Luister","",0xffe9e0ff,51);
            addTile(listen,2);listen.setOnClickListener(v->sound.speak("Vang "+round.target()+" sterren"));
        }
        PlaylumiStarCatchBoard board=new PlaylumiStarCatchBoard(this,round,new PlaylumiStarCatchBoard.Listener(){
            @Override public void onStar(int count){starCatchProgress.setText("⭐ "+count+" / "+round.target());sound.pop();}
            @Override public void onFinished(){
                if(starCatchSolved)return;
                starCatchSolved=true;
                sound.good();
                if(playPal!=null)playPal.dance();
                PlaylumiMotion.celebrate(stage,starCatchRound+1);
                if(nextButton!=null){nextButton.setVisibility(View.VISIBLE);PlaylumiMotion.pop(nextButton);}
            }
        });
        int h=PlaylumiLayout.sceneHeight(280,landscapeScreen(),getResources().getConfiguration().screenHeightDp);
        root.addView(board,new LinearLayout.LayoutParams(-1,dp(h)));
        nextButton=tile("🌟 Volgende ronde","",0xffd4f3dd,78);
        addTile(nextButton,6);nextButton.setVisibility(starCatchSolved?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->{
            if(!starCatchSolved)return;
            starCatchRound++;starCatchSolved=false;
            starCatch=starCatchRound>=PlaylumiStarCatch.ROUNDS?null:PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
            move(STAR_CATCH);
        });
    }

    /** New sensory counting minigame, with one clear goal at a time. */
    private void showBubblePlay() {
        if(bubbles==null)bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
        header("🫧 Het Bellenfeest","Ronde "+Math.min(bubbleRound+1,20)+" van 20");
        if(bubbleRound>=20) {
            characterHero(170);
            addText("🎉 Wat een bubbelkampioen!",25,0xff63458c,10,10);
            Button again=tile("🫧 Nog meer bellen!","",0xffd3f4df,99);
            addTile(again,10);
            again.setOnClickListener(v->{
                bubbleRound=0;bubbleSolved=false;
                bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
                move(BUBBLE_PLAY);
            });
            Button back=tile("🎪 Een ander spel","",0xffe8deff,89);
            addTile(back,8);back.setOnClickListener(v->move(MINI_MENU));
            return;
        }
        // The small star in the navigation row replaces the oversized character panel.
        if(sound.isEnabled()) {
            Button speak=tile("🔊 Luister naar de opdracht","",0xffe8e1ff,49);
            addTile(speak,1);
            speak.setOnClickListener(v->sound.speak("Prik "+bubbles.target+" zeepbellen"));
        }
        PlaylumiBubbleBoard board=new PlaylumiBubbleBoard(this,bubbles,
                new PlaylumiBubbleBoard.Listener() {
            @Override public void onPop() {sound.pop();}
            @Override public void onSolved() {
                if(bubbleSolved)return;
                bubbleSolved=true;
                if(playPal!=null)playPal.dance();
                int category=LearningGame.COUNT;
                ArrayList<Integer> badges=new ArrayList<>(scores.answer(category,true));
                lifetimeCount++;
                wardrobe.earnAnswer();
                wardrobe.earnBadges(badges.size());
                saveWardrobe();saveScore(category);
                pendingBadges.addAll(badges);
                sound.good();
                PlaylumiMotion.celebrate(stage,bubbleRound+1);
                if(nextButton!=null) {
                    nextButton.setVisibility(View.VISIBLE);
                    nextButton.setAlpha(0f);
                    nextButton.animate().alpha(1f).setDuration(240).start();
                    scroller.post(()->scroller.smoothScrollTo(0,
                            Math.max(0,root.getHeight()-scroller.getHeight())));
                }
            }
        });
        LinearLayout.LayoutParams boardp=new LinearLayout.LayoutParams(-1,-2);
        boardp.setMargins(dp(2),dp(6),dp(2),dp(7));
        root.addView(board,boardp);
        nextButton=tile("🌟 Naar de volgende!","",0xffcef3d8,82);
        addTile(nextButton,6);
        nextButton.setVisibility(bubbleSolved?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->finishBubbleRound());
    }
    private void finishBubbleRound() {
        if(!bubbleSolved)return;
        bubbleRound++;
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);
            rewardReturn=BUBBLE_PLAY;
            move(REWARD);return;
        }
        advanceBubbles();
    }
    private void advanceBubbles() {
        bubbleSolved=false;
        bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
        move(BUBBLE_PLAY);
    }


    /** Four tiny pet-care activities, displayed two at a time so young kids can focus. */
    private void showFriendCare() {
        int friend=wardrobe.equipped(PlaylumiWardrobe.FRIENDS);
        header("🐾 Mijn dierenvriend","Samen zorgen en leuke dingen doen!");
        characterHero(139);
        addText(PlaylumiFriends.FRIEND_ICONS[friend]+"  "+
                (friend==0?"Mijn stervriend":PlaylumiWardrobe.ITEM_NAMES[PlaylumiWardrobe.FRIENDS][friend]),
                25,0xff6741a1,5,5);
        addText("💗 "+friends.progress(friend)+" van 4 lieve momenten",19,0xff2d8b70,4,7);
        if(!careMessage.isEmpty())addText(careMessage,17,0xff7454aa,3,9);
        addText((carePage+1)+" / 2",15,0xff9c7ab8,1,9);
        for(int i=carePage*2;i<Math.min(PlaylumiFriends.CARE_ACTIONS,carePage*2+2);i++){
            final int action=i;
            boolean done=friends.cared(friend,i);
            Button button=tile(PlaylumiFriends.ICONS[i]+"  "+PlaylumiFriends.ACTIONS[i],
                    done?"💗 Al samen gedaan!":"✨ Een nieuw momentje",
                    COLORS[i%COLORS.length],103);
            addTile(button,9);
            button.setOnClickListener(v->{
                boolean wasDone=friends.cared(friend,action);
                boolean rewarded=friends.care(friend,action);
                saveFriends();
                if(!wasDone){
                    sound.good();
                    careMessage=PlaylumiFriends.REPLIES[action];
                } else {
                    sound.hello();
                    careMessage="Wat gezellig om dit nog eens te doen!";
                }
                if(rewarded){
                    wardrobe.earnFriendBonus();
                    saveWardrobe();
                    sound.unlock();
                    careMessage="🏅 Nieuwe vriendschapssticker! +"+
                            PlaylumiFriends.FIRST_COMPLETE_BONUS+" sterren!";
                }
                showPage();
                if(character!=null){
                    character.dance();
                    PlaylumiDelight.sprinkle(stage,character);
                }
                if(rewarded) {
                    FrameLayout current=stage;
                    current.post(()->{if(stage==current)PlaylumiMotion.celebrate(current,8);});
                }
            });
        }
        Button browse=tile(carePage==0?"🐾 Nog meer samen doen →":"↩ Eerste momentjes","",
                0xffe9dfff,74);
        addTile(browse,9);
        browse.setOnClickListener(v->{carePage=carePage==0?1:0;showPage();});
        Button album=tile("📒 Mijn vriendenalbum",
                "🏅 "+friends.stickers()+" van "+PlaylumiFriends.FRIENDS+" stickers verdiend",
                0xffffe9b4,85);
        addTile(album,8);
        album.setOnClickListener(v->{albumPage=0;move(FRIEND_ALBUM);});
        Button outfit=tile("🎀 Kies een ander vriendje","",0xffd7f1f8,73);
        addTile(outfit,8);
        outfit.setOnClickListener(v->{selectedSlot=PlaylumiWardrobe.FRIENDS;shopPage=0;move(SHOP);});
    }
    private void showFriendAlbum() {
        header("📒 Mijn vriendenalbum","Iedere vriend heeft een eigen sticker!");
        addText("🏅 "+friends.stickers()+" / "+PlaylumiFriends.FRIENDS,24,0xff8a5da7,9,10);
        addText((albumPage+1)+" / "+((PlaylumiFriends.FRIENDS+1)/2),
                15,0xff9571b2,0,10);
        for(int i=albumPage*2;i<Math.min(PlaylumiFriends.FRIENDS,albumPage*2+2);i++){
            boolean earned=friends.hasReward(i);
            String name=i==0?"Mijn stervriend":PlaylumiWardrobe.ITEM_NAMES[PlaylumiWardrobe.FRIENDS][i];
            Button tile=tile((earned?PlaylumiFriends.FRIEND_ICONS[i]:"🔒")+"  "+name,
                    earned?"🏅 Vriendschapssticker verdiend!":
                            "💗 "+friends.progress(i)+" / "+PlaylumiFriends.CARE_ACTIONS+" momentjes",
                    earned?0xffffe4ae:0xffede9f1,122);
            addTile(tile,9);
            tile.setClickable(false);tile.setFocusable(false);
        }
        Button more=tile(albumPage<(PlaylumiFriends.FRIENDS-1)/2?
                "✨ Meer stickers →":"↩ Eerste stickers","",0xffe5daff,80);
        addTile(more,14);
        more.setOnClickListener(v->{
            albumPage=albumPage<(PlaylumiFriends.FRIENDS-1)/2?albumPage+1:0;
            showPage();
        });
    }


    private String melodyIcons() {
        if(melody==null)return "";
        StringBuilder b=new StringBuilder();
        for(int i=0;i<melody.length();i++){
            if(i>0)b.append("  ");
            b.append(PlaylumiMelody.ANIMALS[melody.noteAt(i)]);
        }
        return b.toString();
    }
    private void playMelodyPreview() {
        if(melody==null || melodySolved)return;
        final FrameLayout active=stage;
        melodyStatus.setText("🎶 Luister goed ...");
        for(int i=0;i<melody.length();i++) {
            final int note=melody.noteAt(i);
            final int beat=i;
            active.postDelayed(()->{
                if(stage!=active || page!=MUSIC_PLAY)return;
                sound.note(note);
                melodyStatus.setText("🎶 "+PlaylumiMelody.ANIMALS[note]+
                        "  ("+(beat+1)+" / "+melody.length()+")");
            },i*470L);
        }
        active.postDelayed(()->{
            if(stage==active && page==MUSIC_PLAY)
                melodyStatus.setText("🌟 Nu jij! Tik de dierenvriendjes aan.");
        },melody.length()*470L+220L);
    }
    private void showMusicPlay() {
        if(melody==null)melody=PlaylumiMelody.generate(Math.max(0,age),new Random());
        header("🎵 Het Muziekatelier","Melodie "+Math.min(melodyRound+1,PlaylumiMelody.ROUNDS)+
                " / "+PlaylumiMelody.ROUNDS);
        if(melodyRound>=PlaylumiMelody.ROUNDS) {
            characterHero(170);
            addText("🏆 Wat een muzikale ster!",26,0xff61418f,10,14);
            Button again=tile("🎶 Nog eens muziek maken","",0xffd4f3dd,96);
            addTile(again,9);
            again.setOnClickListener(v->{
                melodyRound=0;melodySolved=false;
                melody=PlaylumiMelody.generate(Math.max(0,age),new Random());
                move(MUSIC_PLAY);
            });
            Button other=tile("🎪 Ander spelletje","",0xffe9deff,80);
            addTile(other,7);other.setOnClickListener(v->move(MINI_MENU));
            return;
        }
        // More room for the musical instruments and large touch targets.
        addText("🐸 🐱 🐥 🦋",31,0xff7652ac,8,7);
        melodyStatus=text(sound.isEnabled()?
                "👂 Luister en speel het deuntje na.":"👀 Bekijk de plaatjes en tik ze na.",
                20,0xff65418f);
        melodyStatus.setPadding(dp(9),dp(14),dp(9),dp(14));
        melodyStatus.setBackground(rounded(0xfff1e5ff,23));
        addWidget(melodyStatus,5,11);
        Button replay=tile(sound.isEnabled()?"🔊 Speel de melodie af":"👀 Bekijk de melodie",
                "",0xffd5f0ff,87);
        addTile(replay,8);
        replay.setOnClickListener(v->{
            if(sound.isEnabled())playMelodyPreview();
            else melodyStatus.setText(melodyIcons());
        });
        Button reveal=tile("💡 Laat de volgorde zien","",0xffffedc3,61);
        addTile(reveal,5);
        reveal.setOnClickListener(v->{
            melodyStatus.setText(melodyIcons());
            sound.sparkle();
        });
        for(int i=0;i<4;i+=2) {
            final int first=i,second=i+1;
            Button one=tile(PlaylumiMelody.ANIMALS[first]+"\n"+
                    PlaylumiMelody.NAMES[first],"",COLORS[first],107);
            Button two=tile(PlaylumiMelody.ANIMALS[second]+"\n"+
                    PlaylumiMelody.NAMES[second],"",COLORS[second],107);
            addTwo(one,two,110);
            one.setOnClickListener(v->tapMelody(first,one));
            two.setOnClickListener(v->tapMelody(second,two));
        }
        if(melodySolved)melodyStatus.setText("🎉 Geweldig! Je kon de melodie spelen!");
        nextButton=tile("🌟 Volgende melodie","",0xffd2f5da,90);
        addTile(nextButton,10);
        nextButton.setVisibility(melodySolved?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->finishMelodyRound());
    }
    private void tapMelody(int note,Button pressed) {
        if(melodySolved || melody==null)return;
        sound.note(note);
        PlaylumiDelight.hop(pressed);
        int status=melody.tap(note);
        if(status==0) {
            melodyStatus.setText("💗 Oeps! Probeer de melodie nog eens.");
            if(playPal!=null)playPal.react(2);
            PlaylumiDelight.wiggle(pressed);
        } else if(status==1) {
            melodyStatus.setText("✨ Goed! "+melody.progress()+" van "+
                    melody.length()+" tonen.");
            PlaylumiMotion.pop(pressed);
        } else if(status==2) {
            melodySolved=true;
            melodyStatus.setText("🎉 Helemaal goed! +3 speelsterren");
            if(playPal!=null)playPal.dance();
            PlaylumiMotion.celebrate(stage,melodyRound+2);
            ArrayList<Integer> badges=new ArrayList<>(
                    scores.answer(LearningGame.PATTERNS,true));
            lifetimeCount++;
            wardrobe.earnAnswer();
            wardrobe.earnBadges(badges.size());
            saveWardrobe();saveScore(LearningGame.PATTERNS);
            pendingBadges.addAll(badges);
            sound.good();
            nextButton.setVisibility(View.VISIBLE);
            PlaylumiMotion.pop(nextButton);
            PlaylumiDelight.sprinkle(stage,pressed);
            scroller.post(()->scroller.smoothScrollTo(0,
                    Math.max(0,root.getHeight()-scroller.getHeight())));
        }
    }
    private void finishMelodyRound() {
        if(!melodySolved)return;
        melodyRound++;
        if(!pendingBadges.isEmpty()){
            earnedBadge=pendingBadges.remove(0);
            rewardReturn=MUSIC_PLAY;
            move(REWARD);
            return;
        }
        advanceMelody();
    }
    private void advanceMelody() {
        melodySolved=false;
        melody=PlaylumiMelody.generate(Math.max(0,age),new Random());
        move(MUSIC_PLAY);
    }


    /** Show two seeds at once rather than a crowded garden catalogue. */
    private void showGardenMenu() {
        header("🌱 Mijn Magische Tuin","Kies een plant die je wilt laten groeien.");
        addText("🌸 "+garden.bloomedCount()+" van "+PlaylumiGarden.COUNT+
                " planten in bloei",20,0xff43886e,6,8);
        addText((gardenPage+1)+" / "+((PlaylumiGarden.COUNT+1)/2),15,0xff926db4,0,6);
        for(int p=gardenPage*2;p<Math.min(PlaylumiGarden.COUNT,gardenPage*2+2);p++) {
            final int plant=p;
            int growth=garden.stage(p);
            String name=PlaylumiGarden.NAMES[p];
            String detail=growth==PlaylumiGarden.STAGES?"🏅 Helemaal in bloei!":
                    "🌱 Groeifase "+growth+" / "+PlaylumiGarden.STAGES;
            Button button=tile(garden.picture(p)+"  "+name,detail,
                    growth==PlaylumiGarden.STAGES?0xffffe5bd:COLORS[p%COLORS.length],126);
            addTile(button,12);
            button.setOnClickListener(v->{
                gardenPlant=plant;
                gardenMessage="";
                move(GARDEN_PLANT);
            });
        }
        Button more=tile(gardenPage<(PlaylumiGarden.COUNT-1)/2?
                "🌿 Meer planten ontdekken →":"↩ Eerste planten","",
                0xffe7dcff,76);
        addTile(more,13);
        more.setOnClickListener(v->{
            gardenPage=gardenPage<(PlaylumiGarden.COUNT-1)/2?gardenPage+1:0;
            showPage();
        });
    }
    private void showGardenPlant() {
        int plant=gardenPlant;
        header("🌿 "+PlaylumiGarden.NAMES[plant],"Help jouw plant groeien!");
        PlaylumiGardenView view=new PlaylumiGardenView(this,garden,plant);
        int gardenHeight=PlaylumiLayout.sceneHeight(257,landscapeScreen(),
                getResources().getConfiguration().screenHeightDp);
        LinearLayout.LayoutParams scene=new LinearLayout.LayoutParams(-1,dp(gardenHeight));
        scene.setMargins(dp(3),dp(3),dp(3),dp(7));
        root.addView(view,scene);
        view.setContentDescription("Magische plant "+PlaylumiGarden.NAMES[plant]+
                ". Tik om extra sterren te ontdekken.");
        view.setClickable(true);view.setFocusable(true);
        view.setOnClickListener(v->{
            PlaylumiDelight.wiggle(view);
            PlaylumiDelight.sprinkle(stage,view);
            if(sound!=null)sound.sparkle();
        });
        addText("💚 "+garden.stage(plant)+" van "+PlaylumiGarden.STAGES+
                " groeistapjes",21,0xff2a876a,2,8);
        if(!gardenMessage.isEmpty())addText(gardenMessage,17,0xff6a4293,0,9);
        if(garden.bloomed(plant)) {
            TextView fact=text("💡 "+PlaylumiGarden.FACTS[plant],19,0xff65458a);
            fact.setPadding(dp(13),dp(17),dp(13),dp(17));
            fact.setBackground(rounded(0xffe5f7ee,25));
            addWidget(fact,0,16);
            if(sound.isEnabled()) {
                Button listen=tile("🔊 Vertel iets over deze plant","",0xffeee2ff,61);
                addTile(listen,6);
                listen.setOnClickListener(v->sound.speak(PlaylumiGarden.FACTS[plant]));
            }
            addText("🏅 Je hebt deze plant helemaal grootgebracht!",17,0xff398a72,4,8);
        } else {
            int action=garden.nextAction(plant);
            String title=action==PlaylumiGarden.WATER?
                    "💧 Geef je plant water!":"☀️ Zet je plant in de zon!";
            Button grow=tile(title,action==PlaylumiGarden.WATER?
                    "Help het zaadje wakker te worden":"De plant heeft licht nodig",
                    action==PlaylumiGarden.WATER?0xffd6eeff:0xffffeac3,116);
            addTile(grow,11);
            grow.setOnClickListener(v->{
                int result=garden.grow(plant,action);
                saveGarden();
                if(result==1) {
                    wardrobe.earnGardenBonus();
                    saveWardrobe();
                    sound.unlock();
                    gardenMessage="🎉 Nieuwe plant! +"+
                            PlaylumiGarden.FIRST_BLOOM_STARS+" speelsterren";
                } else {
                    sound.sparkle();
                    gardenMessage=action==PlaylumiGarden.WATER?
                            "💧 Goed gedaan! Het plantje groeit!":"☀️ Heerlijk, wat fijn zonlicht!";
                }
                showPage();
                if(result==1) {
                    FrameLayout current=stage;
                    current.post(()->{
                        if(stage==current)PlaylumiMotion.celebrate(current,10);
                    });
                }
            });
        }
        Button back=tile("🌸 Kies een andere plant","",0xffe8dcff,84);
        addTile(back,10);
        back.setOnClickListener(v->move(GARDEN_MENU));
    }

}
