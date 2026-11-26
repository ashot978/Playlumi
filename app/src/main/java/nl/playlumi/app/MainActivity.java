package nl.playlumi.app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
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
            WORLD_PICKER=40, HOME_OPTIONS=41, STAR_CATCH=42, VILLAGE_MENU=43, LIBRARY=44, INTERACTIVE=45, COLLECTIBLES=46;
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
    private PlaylumiAvatar character;
    private LinearLayout root;
    private FrameLayout stage;
    private ScrollView scroller;
    private TextView feedbackText,progressCounter,questionPicture;
    private PlaylumiStarTrail roundStars;
    private long autoAdvanceToken;
    private boolean autoRoundVisible,advancingAutomatically;
    private String feedback="";
    private PlaylumiAudio sound;
    private PlaylumiMiniChallenge mini;
    private PlaylumiAvatar playPal;
    private int miniKind,miniRound,rewardReturn=PLAY;
    private boolean miniSolved;
    private PlaylumiBubbles bubbles;
    private PlaylumiMelody melody;
    private int melodyRound;
    private int melodyVariant;
    private boolean melodySolved;
    private TextView melodyStatus,magicHeaderProgress;
    private final Button[] melodyPads=new Button[4];
    private boolean melodyPreviewActive;
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
    // Keep the last-used legacy storage slot; changing a character never switches data.
    private int activeProfile;
    // Tracks which village building started a screen, so Back returns logically.
    private int villageLaunch=-1;
    private int libraryFilter=PlaylumiGameCatalog.OVERVIEW;
    private int libraryScrollY;
    private int personalReturn=HOME,collectionReturn=HOME,closetReturn=AVATAR;
    private TextView[] libraryTabs;
    private boolean libraryReturn;
    private PlaylumiInteractive interactive;
    private String interactiveMessage="";
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
            melodyVariant=Math.max(0,Math.min(PlaylumiRocketCurriculum.level(age),state.getInt("melodyVariant",0)));
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
            libraryFilter=state.getInt("libraryFilter",PlaylumiGameCatalog.OVERVIEW);
            libraryScrollY=Math.max(0,state.getInt("libraryScrollY",0));
            personalReturn=state.getInt("personalReturn",HOME);collectionReturn=state.getInt("collectionReturn",HOME);closetReturn=state.getInt("closetReturn",AVATAR);
            libraryReturn=state.getBoolean("libraryReturn",false);
            if(state.containsKey("interactiveKind"))interactive=new PlaylumiInteractive(state.getInt("interactiveKind"),state.getInt("interactiveLevel"),state.getLong("interactiveSeed"),state.getInt("interactiveProgress"));
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
        if(page==PLAY&&(libraryReturn||(villageLaunch==PlaylumiVillageHit.ROCKET&&PlaylumiRocketCurriculum.isMathMode(mode))))smartDifficulty=false;
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
        if(state!=null && page==PLAY && state.containsKey("roundDisplay")){
            try{
                game.restoreRound(state.getString("roundPrompt"),state.getString("roundDisplay"),
                        state.getInt("roundColor"),state.getStringArrayList("roundAnswers"),
                        state.getIntegerArrayList("roundColors"),state.getInt("roundCorrect"),
                        state.getInt("roundFingers"),state.getBoolean("roundSolved"));
                feedback=state.getString("roundFeedback","");
                ArrayList<Integer> savedBadges=state.getIntegerArrayList("roundBadges");
                if(savedBadges!=null)pendingBadges.addAll(savedBadges);
            }catch(IllegalArgumentException ignored){}
        }
        if(page==MINI_PLAY){
            miniSolved=state!=null&&state.getBoolean("miniSolved",false);
            if(state!=null && miniKind==PlaylumiMiniChallenge.DRAG && state.containsKey("dragTarget")){
                try {
                    mini=PlaylumiMiniChallenge.restoreDrag(state.getInt("dragLevel"),
                            state.getString("dragTarget"),state.getStringArrayList("dragChoices"),
                            state.getInt("dragCorrect"));
                    miniSolved=state.getBoolean("dragSolved");
                    ArrayList<Integer> savedBadges=state.getIntegerArrayList("dragBadges");
                    if(savedBadges!=null)pendingBadges.addAll(savedBadges);
                }catch(IllegalArgumentException ignored){}
            }
            if(mini==null)mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
        }
        if(page==MUSIC_PLAY && state!=null && state.getIntArray("melodyNotes")!=null) {
            try {
                melody=PlaylumiMelody.restore(state.getIntArray("melodyNotes"),
                        state.getInt("melodyProgress",0));
            } catch(IllegalArgumentException ignored) {}
        }
        if(page==MUSIC_PLAY && melody==null)
            melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
        if(page==STAR_CATCH) {
            starCatchRound=state==null?0:Math.max(0,Math.min(PlaylumiStarCatch.ROUNDS,state.getInt("starCatchRound",0)));
            if(starCatchRound<PlaylumiStarCatch.ROUNDS){
                if(state!=null && state.getInt("starCatchTotal",0)>0)try {
                    starCatch=PlaylumiStarCatch.restore(state.getInt("starCatchTarget"),state.getInt("starCatchTotal"),state.getInt("starCatchTypes",0),state.getInt("starCatchMask"));
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
            age=Math.max(0,Math.min(2,getIntent().getIntExtra("playlumi_ci_age",Math.max(0,age))));
            page=HOME;
        }
        showPage();
    }
    @Override protected void onDestroy() {
        autoAdvanceToken++;
        if(sound!=null)sound.close();
        super.onDestroy();
    }
    @Override protected void onResume() {
        super.onResume();
        activeSince=SystemClock.elapsedRealtime();
        scheduleAutoAdvance();
    }
    @Override protected void onPause() {
        autoAdvanceToken++;
        if(page==PLAY)saveLibraryQuiz();
        if(page==INTERACTIVE)saveLibraryInteractive();
        if(page==DRAW && drawing!=null)drawing.save();
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
        if(page==PLAY && game!=null){
            LearningGame.Round round=game.round();
            out.putString("roundPrompt",round.prompt);out.putString("roundDisplay",round.display);
            out.putInt("roundColor",round.displayColor);out.putInt("roundCorrect",round.correctIndex);
            out.putInt("roundFingers",round.fingerCount);out.putBoolean("roundSolved",round.isSolved());
            out.putStringArrayList("roundAnswers",new ArrayList<>(round.answers));
            out.putIntegerArrayList("roundColors",new ArrayList<>(round.answerColors));
            out.putString("roundFeedback",feedback);
            out.putIntegerArrayList("roundBadges",new ArrayList<>(pendingBadges));
        }
        if(page==MINI_PLAY && mini!=null && miniKind==PlaylumiMiniChallenge.DRAG){
            out.putInt("dragLevel",mini.level);out.putString("dragTarget",mini.target);
            out.putStringArrayList("dragChoices",new ArrayList<>(mini.choices));
            out.putInt("dragCorrect",mini.correctIndex);out.putBoolean("dragSolved",miniSolved);
            out.putIntegerArrayList("dragBadges",new ArrayList<>(pendingBadges));
        }
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
        out.putInt("miniRound",miniRound);out.putBoolean("miniSolved",miniSolved);
        out.putInt("bubbleRound",bubbleRound);
        out.putInt("starCatchRound",starCatchRound);
        if(starCatch!=null){out.putInt("starCatchTarget",starCatch.target());out.putInt("starCatchTotal",starCatch.total());out.putInt("starCatchMask",starCatch.mask());out.putInt("starCatchTypes",starCatch.starMask());}
        out.putInt("melodyRound",melodyRound);out.putInt("melodyVariant",melodyVariant);
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
        out.putInt("libraryFilter",libraryFilter);out.putBoolean("libraryReturn",libraryReturn);
        out.putInt("libraryScrollY",page==LIBRARY&&scroller!=null?scroller.getScrollY():libraryScrollY);
        out.putInt("personalReturn",personalReturn);out.putInt("collectionReturn",collectionReturn);out.putInt("closetReturn",closetReturn);
        if(interactive!=null){out.putInt("interactiveKind",interactive.kind);out.putInt("interactiveLevel",interactive.level);out.putLong("interactiveSeed",interactive.seed);out.putInt("interactiveProgress",interactive.progress());}
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
        if(page==INTERACTIVE){move(LIBRARY);return;}
        if(page==LIBRARY){
            if(libraryFilter!=PlaylumiGameCatalog.OVERVIEW){libraryFilter(PlaylumiGameCatalog.OVERVIEW);}
            else move(HOME);
            return;
        }
        if(libraryReturn&&storyStage<0&&(page==PLAY||page==MINI_PLAY||page==BUBBLE_PLAY||page==STAR_CATCH||page==MUSIC_PLAY||page==DRAW)){
            if(page==DRAW&&drawing!=null)drawing.save();
            move(LIBRARY);return;
        }
        if(page==VILLAGE_MENU) {move(HOME);return;}
        if(storyStage<0 && PlaylumiWorldStyle.valid(villageLaunch) &&
                (page==PLAY || page==DETAIL || page==MINI_PLAY || page==BUBBLE_PLAY ||
                 page==STAR_CATCH || page==MUSIC_PLAY || page==MINI_MENU || page==DRAW)) {
            if(page==DRAW && drawing!=null)drawing.save();
            move(VILLAGE_MENU);return;
        }
        if(page==WORLD_PICKER || page==HOME_OPTIONS) {move(HOME);return;}
        if(page==REWARD) { continueAfterBadge();return; }
        if(page==AVATAR) {move(HOME);return;}
        if(page==CLOSET) {move(closetReturn==SHOP?SHOP:AVATAR);return;}
        if(page==COLLECTIBLES) {move(collectionReturn==SHOP?SHOP:collectionReturn==AVATAR?AVATAR:HOME);return;}
        if(page==SHOP) {move(HOME);return;}
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
        if(page==TROPHY_WORLDS) {move(villageLaunch==PlaylumiVillageView.COLLECTION?COLLECTIBLES:AVATAR);return;}
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
        if(page==PROFILES) {move(personalReturn==AVATAR?AVATAR:HOME);return;}
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
        int gained=Math.max(0,s.correct-prefs().getInt(prefix+"good",0));
        prefs().edit().putInt(prefix+"good",s.correct).putInt(prefix+"tries",s.attempts)
                .putInt(prefix+"best",s.bestStreak).putInt(prefix+"streak",s.currentStreak)
                .putInt(prefix+"sessions",s.completedSessions)
                .putInt("completed_all",lifetimeCount).apply();
        if(gained>0){
            int w=page==MUSIC_PLAY?3:page==DRAW?5:page==BUBBLE_PLAY||page==STAR_CATCH?4:
                    page==MINI_PLAY?(miniKind==PlaylumiMiniChallenge.DRAG?2:4):
                    PlaylumiWorldStyle.valid(villageLaunch)?villageLaunch:portalForMode(mode);
            recordWorldProgress(w,gained);
        }
    }
    private int collectionProgress(int world){
        return PlaylumiCollection.clamp(prefs().getInt("collection_world_"+world,0));
    }
    private int collectionTotal(){
        int total=0;
        for(int i=0;i<6;i++)total+=PlaylumiCollection.unlocked(collectionProgress(i));
        return total;
    }
    private void recordWorldProgress(int world,int gained){
        if(!PlaylumiWorldStyle.valid(world)||gained<=0)return;
        int before=collectionProgress(world);
        int after=PlaylumiCollection.advance(before,gained);
        if(after==before)return;
        prefs().edit().putInt("collection_world_"+world,after).apply();
        if(PlaylumiCollection.unlocked(after)>PlaylumiCollection.unlocked(before)){
            String treasure=PlaylumiCollection.NAMES[world][PlaylumiCollection.unlocked(after)-1];
            android.widget.Toast.makeText(this,"✦ Nieuwe schat: "+treasure,android.widget.Toast.LENGTH_SHORT).show();
            if(playPal!=null)PlaylumiDelight.sprinkle(stage,playPal);
        }
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
    private int worldTheme() {
        if(page==LIBRARY)return PlaylumiVillageHit.PLAYGROUND;
        if(page==INTERACTIVE)return PlaylumiWorldStyle.valid(villageLaunch)?villageLaunch:PlaylumiVillageHit.PLAYGROUND;
        if(page==VILLAGE_MENU || page==PLAY || page==DETAIL || page==LEVEL || page==VARIANTS ||
                page==MINI_MENU || page==MINI_PLAY || page==MUSIC_PLAY || page==BUBBLE_PLAY ||
                page==STAR_CATCH || page==DRAW || page==BADGES) {
            if(PlaylumiWorldStyle.valid(villageLaunch))return villageLaunch;
            if(page==MUSIC_PLAY)return PlaylumiVillageHit.MUSIC;
            if(page==MINI_MENU || page==MINI_PLAY || page==BUBBLE_PLAY || page==STAR_CATCH)
                return PlaylumiVillageHit.PLAYGROUND;
            if(page==DRAW)return PlaylumiVillageHit.COLORS;
            if(game!=null)return portalForMode(game.mode());
        }
        return -1;
    }
    private int chosenAvatar() {return PlaylumiWorldStyle.safeAvatar(prefs().getInt("chosen_avatar",PlaylumiWorldStyle.FIRST_AVATAR));}
    private GradientDrawable worldPlate(int world,int color) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{PlaylumiWorldStyle.plateTop(world),
                        PlaylumiWorldStyle.plateMiddle(world),PlaylumiWorldStyle.deep(world)});
        d.setCornerRadius(dp(19));d.setStroke(dp(2),0xffffdfa0);return d;
    }
    private Drawable gameTileBackground(int color) {
        int theme=worldTheme();
        if(theme>=0){
            int index=PlaylumiMagicArt.nearestPastel(color);
            return landscapeScreen()?PlaylumiMagicArt.gemButton(this,index):PlaylumiMagicArt.pastel(this,index);
        }
        GradientDrawable b=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mix(color,Color.WHITE,.40f),color,mix(color,0xffb299d7,.20f)});
        b.setCornerRadius(dp(21));
        b.setStroke(dp(2),mix(color,0xff8154b9,.55f));
        return b;
    }
    private void addIllustratedPortal(int place,String title,int heightDp) {
        if(worldTheme()>=0 && PlaylumiWorldStyle.valid(place)) {
            int width=getResources().getConfiguration().screenWidthDp;
            int available=getResources().getConfiguration().screenHeightDp;
            int height=PlaylumiLayout.magicHeroHeight(width,available,page==VILLAGE_MENU);
            if(page==PLAY)height=Math.min(height,PlaylumiLayout.fastQuizHeroHeight(available,landscapeScreen()));
            if(page==VILLAGE_MENU && place==PlaylumiVillageView.ROCKET)height=Math.min(height,landscapeScreen()?160:118);
            if(page==MINI_PLAY && miniKind==PlaylumiMiniChallenge.DRAG)
                height=PlaylumiLayout.shapeHeroHeight(available);
            View art=PlaylumiMagicArt.hero(this,place);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(height));
            p.setMargins(0,0,0,dp(landscapeScreen()?10:6));root.addView(art,p);
        } else {
            PlaylumiPortalView art=new PlaylumiPortalView(this,place,title);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(heightDp));
            p.setMargins(dp(1),dp(8),dp(1),dp(9));
            root.addView(art,p);
        }
    }
    private TextView magicRibbon(String value,int size) {
        TextView t=text(value,size,PlaylumiMagicArt.CREAM);
        t.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        t.setPadding(dp(20),dp(8),dp(20),dp(4));
        t.setMinHeight(dp(56));
        t.setMaxLines(3);
        t.setBackground(landscapeScreen()?PlaylumiMagicArt.silkRibbon(this):PlaylumiMagicArt.ribbon(this));
        t.setShadowLayer(dp(1),0,dp(1),0xff301449);
        return t;
    }
    private void addMagicProgress(int done,int total,boolean quiz) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8),dp(2),dp(8),dp(2));
        row.setBackground(PlaylumiMagicArt.creamPlate(this));
        PlaylumiStarTrail stars=new PlaylumiStarTrail(this,total,done);
        row.addView(stars,new LinearLayout.LayoutParams(0,dp(30),1f));
        TextView count=text(done+"/"+total,15,PlaylumiMagicArt.INK);
        count.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        count.setPadding(dp(5),0,dp(3),0);
        row.addView(count,new LinearLayout.LayoutParams(-2,dp(32)));
        roundStars=stars;progressCounter=count;
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(37));
        p.setMargins(dp(5),dp(1),dp(5),dp(6));
        root.addView(row,p);
    }
    private int portalForMode(int mode) {
        if(PlaylumiRocketCurriculum.isMathMode(mode))
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
        TextView t=text(value,size,worldTheme()>=0?PlaylumiMagicArt.CREAM:color);
        if(worldTheme()>=0)t.setShadowLayer(dp(1),0,dp(1),0xff342052);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(PlaylumiLayout.margin(top)),
                0,dp(PlaylumiLayout.margin(bottom)));root.addView(t,p);
    }
    private Button tile(String title,String description,int color,int minHeight) {
        Button b=new Button(this);
        b.setAllCaps(false);
        boolean magic=worldTheme()>=0;
        boolean primary=magic && description.isEmpty() &&
                (title.contains("Volgende") || title.contains("Verder spelen") || title.contains("Nog eens"));
        b.setText(PlaylumiType.tileText(title,description,primary));
        b.setTextColor(primary?PlaylumiMagicArt.CREAM:magic?PlaylumiMagicArt.INK:TEXT);
        b.setTextSize(description.isEmpty()?18:16);
        b.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        b.setLineSpacing(dp(1),1.0f);
        b.setGravity(Gravity.CENTER);
        b.setMaxLines(4);
        b.setPadding(dp(10),dp(9),dp(10),dp(9));
        b.setMinHeight(dp(PlaylumiLayout.tileHeight(minHeight,gameScreen())));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),
                primary?PlaylumiMagicArt.primary(this):gameTileBackground(color),null));
        b.setElevation(dp(magic?4:5));
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
        PlaylumiAvatar star=new PlaylumiAvatar(this,chosenAvatar(),wardrobe);
        holder.addView(star,new LinearLayout.LayoutParams(dp(actual),dp(actual)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(actual+5));
        p.setMargins(0,dp(2),0,dp(3));
        root.addView(holder,p);
        PlaylumiMotion.floatMascot(star);
    }
    private void characterHero(int size) {
        LinearLayout holder=new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        character=new PlaylumiAvatar(this,chosenAvatar(),wardrobe);
        character.setContentDescription("Jouw maatje. Tik voor een magische verrassing.");
        character.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
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
        boolean showFull=page==AVATAR || page==CLOSET || page==COLLECTIBLES || page==STAR_STAGE || page==STORY_END ||
                page==REWARD || page==BREAK_SCREEN || page==SHOP;
        int actual=PlaylumiLayout.heroHeight(size,landscapeScreen(),showFull,
                getResources().getConfiguration().screenHeightDp);
        holder.addView(character,new LinearLayout.LayoutParams(dp(actual),dp(actual)));
        if(page==HOME && !greeted) {
            greeted=true;
            final PlaylumiAvatar helloFriend=character;
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
        playPal=new PlaylumiAvatar(this,chosenAvatar(),wardrobe);
        row.addView(playPal,new LinearLayout.LayoutParams(dp(size),dp(size)));
        playPal.setContentDescription("Je maatje. Tik om te zwaaien.");
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
        return page==INTERACTIVE || page==PLAY || page==MINI_PLAY || page==BUBBLE_PLAY || page==STAR_CATCH ||
                page==MUSIC_PLAY || page==STORY_PLAY || page==STORY_REACTION ||
                page==GARDEN_PLANT || page==DRAW;
    }
    /**
     * One compact navigation row, not a large Playlumi logo followed by a duplicate title.
     * Always leaves the main part of the screen for the actual game.
     */
    private void header(String heading,String subtitle) {
        boolean gameHeader=gameScreen();
        int theme=worldTheme();
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8),dp(4),dp(8),dp(4));
        GradientDrawable barBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff8056bf,0xff58368e,0xff442879});
        barBg.setCornerRadius(dp(20));
        barBg.setStroke(dp(2),0xffd4b3f2);
        row.setBackground(theme>=0?PlaylumiMagicArt.primary(this):barBg);
        row.setElevation(dp(5));
        if(page!=HOME && (page!=AGE || prefs().contains("chosen_age"))) {
            TextView back=text("❮",22,0xffffe6a7);
            back.setGravity(Gravity.CENTER);
            back.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    theme>=0?PlaylumiMagicArt.nav(this,false):rounded(0xff7652a8,14),null));
            if(theme>=0)back.setText("");
            back.setContentDescription("Terug naar het vorige scherm");
            back.setClickable(true);back.setFocusable(true);
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(48),dp(48));
            bp.setMargins(0,0,dp(9),0);
            row.addView(back,bp);
            back.setOnClickListener(v->onBackPressed());
        }
        TextView title=text(heading,gameHeader?17:20,Color.WHITE);
        if(theme>=0){title.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));title.setShadowLayer(dp(1),0,dp(1),0xff391452);}
        title.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        title.setSingleLine(false);
        title.setHorizontallyScrolling(false);
        title.setMaxLines(2);
        title.setEllipsize(null);
        title.setIncludeFontPadding(false);
        title.setLineSpacing(0,1f);
        if(Build.VERSION.SDK_INT>=26)
            title.setAutoSizeTextTypeUniformWithConfiguration(11,gameHeader?19:20,1,android.util.TypedValue.COMPLEX_UNIT_SP);
        if((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)
            title.addOnLayoutChangeListener((view,l,t,r,b,ol,ot,or,ob)->{
                android.text.Layout layout=title.getLayout();
                if(layout!=null&&layout.getLineCount()>0){
                    int last=layout.getLineCount()-1;
                    boolean fit=last<2 && layout.getLineEnd(last)>=title.length() &&
                            layout.getEllipsisCount(last)==0 && layout.getLineBottom(last)<=title.getHeight()+2;
                    android.util.Log.i("PlaylumiQA","headerFit="+fit+" title="+heading);
                }
            });
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(48),1f);
        row.addView(title,tp);
        if(gameHeader) {
            if(subtitle!=null && !subtitle.isEmpty()) {
                TextView status=text(subtitle.replace("Uitdaging ",""),12,0xffffe3a0);
                status.setSingleLine(true);
                status.setPadding(dp(5),0,dp(5),0);
                if(theme>=0){status.setText("★ "+subtitle.replace("Uitdaging ","").replace("Melodie ",""));status.setTextColor(0xffffe1a0);}
                if(theme>=0)magicHeaderProgress=status;
                row.addView(status,new LinearLayout.LayoutParams(-2,dp(45)));
            }
            if(wardrobe!=null) {
                playPal=new PlaylumiAvatar(this,chosenAvatar(),wardrobe);
                playPal.setContentDescription("Je maatje reageert op je antwoord");
                LinearLayout.LayoutParams tiny=new LinearLayout.LayoutParams(dp(32),dp(32));
                tiny.setMargins(dp(4),0,0,0);
                row.addView(playPal,tiny);
            }
        } else {
            View star=wardrobe==null?mascot(37):new PlaylumiAvatar(this,chosenAvatar(),wardrobe);
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
                    theme>=0?PlaylumiMagicArt.nav(this,true):rounded(0xff6c469d,14),null));
            if(theme>=0)village.setText("");
            village.setContentDescription("Terug naar het magische dorp");
            village.setClickable(true);village.setFocusable(true);
            LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(42),dp(42));
            hp.setMargins(dp(5),0,0,0);
            row.addView(village,hp);
            village.setOnClickListener(v->move(HOME));
        }
        LinearLayout.LayoutParams rowp=new LinearLayout.LayoutParams(-1,dp(theme>=0?56:gameHeader?60:66));
        rowp.setMargins(0,0,0,dp(theme>=0?5:gameHeader?9:10));
        root.addView(row,rowp);
        if(!gameHeader && subtitle!=null && !subtitle.isEmpty())
            addText(subtitle,14,SUB,0,8);
    }
    private void showPage() {
        autoAdvanceToken++;autoRoundVisible=false;
        // Restore old navigation destinations directly into the new library.
        if(page==VILLAGE_MENU){page=LIBRARY;libraryFilter=PlaylumiWorldStyle.valid(villageLaunch)?villageLaunch:PlaylumiGameCatalog.OVERVIEW;}
        // Retire old star wardrobe and pet pages without deleting their saved data.
        if(page==FRIEND_CARE || page==FRIEND_ALBUM)page=AVATAR;
        if(!parentAllowed && (page==PARENT_HOME || page==PARENT_WORLDS ||
                page==PARENT_STATS || page==PARENT_SETTINGS))page=PARENT_GATE;
        playPal=null;progressCounter=null;questionPicture=null;magicHeaderProgress=null;roundStars=null;melodyPreviewActive=false;libraryTabs=null;
        final boolean villageScreen=page==HOME;
        final boolean magicScreen=worldTheme()>=0;
        final boolean darkBars=villageScreen || magicScreen;
        // Android 15+ draws applications behind system bars by default.
        // WindowInsets (not guessed screen margins) defines the safe area.
        int appearance=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Build.VERSION.SDK_INT>=26)appearance|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if(Build.VERSION.SDK_INT>=30) {
            android.view.WindowInsetsController controller=getWindow().getInsetsController();
            if(controller!=null) {
                int mask=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(darkBars?0:mask,mask);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(darkBars?0:appearance);
        }
        getWindow().setStatusBarColor(darkBars?0xff50308c:0xffeaf5ff);
        getWindow().setNavigationBarColor(darkBars?0xff50308c:0xfffff6e4);
        if(Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
        stage=new FrameLayout(this);
        GradientDrawable bg=new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                darkBars?new int[]{0xff30194f,0xff50308c,0xff30194f}:
                        new int[]{0xffeaf7ff,0xfffff3fb,0xfffff8e6});
        stage.setBackground(bg);
        int theme=worldTheme();
        if(theme>=0) {
            stage.addView(PlaylumiMagicArt.backdrop(this,theme),new FrameLayout.LayoutParams(-1,-1));
        } else if(page==WORLD_PICKER || page==WORLD_HUB ||
                page==AVATAR || page==CLOSET || page==ROOM || page==COLLECTIBLES || page==SHOP ||
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
        if(magicScreen)side=page==LIBRARY||page==INTERACTIVE?PlaylumiLayout.librarySidePadding(getResources().getConfiguration().screenWidthDp):PlaylumiLayout.magicSidePadding(getResources().getConfiguration().screenWidthDp);
        root.setPadding(page==HOME?0:dp(side),page==HOME?0:dp(5),
                page==HOME?0:dp(side),page==HOME?0:dp(16));
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
                    if((getApplicationInfo().flags &
                            android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)
                        android.util.Log.i("PlaylumiQA","safeTop="+Math.max(0,top)+
                                " safeBottom="+Math.max(0,bottom));
                } else {
                    int left=0,right=0;
                    if(Build.VERSION.SDK_INT>=30){
                        android.graphics.Insets stable=insets.getInsetsIgnoringVisibility(
                                android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());
                        android.graphics.Insets interactive=insets.getInsets(
                                android.view.WindowInsets.Type.tappableElement()|android.view.WindowInsets.Type.mandatorySystemGestures());
                        top=Math.max(top,stable.top);
                        bottom=Math.max(bottom,Math.max(stable.bottom,interactive.bottom));
                        left=stable.left;right=stable.right;
                    }
                    // Some foldable taskbars and emulators report zero during transitions.
                    // Reserve a conservative minimum only on non-village screens.
                    int safeBottom=Math.max(bottom,dp(landscapeScreen()?56:24));
                    stage.setPadding(left,Math.max(0,top),right,safeBottom);
                    currentScroller.setPadding(0,dp(3),0,dp(12));
                    if((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)
                        android.util.Log.i("PlaylumiQA","gameBottomInset="+bottom+" gameReservedBottom="+safeBottom);
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
            case VILLAGE_MENU: showVillageMenu();break;
            case LIBRARY: showGameLibrary();break;
            case INTERACTIVE: showInteractive();break;
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
            case COLLECTIBLES: showCollectibles();break;
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
        if((getApplicationInfo().flags &
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)
            android.util.Log.i("PlaylumiQA","screen="+page+" villageLaunch="+villageLaunch);
        if(!PlaylumiMotion.isReduced()&&!advancingAutomatically) {
            stage.setAlpha(0f);stage.setTranslationX(dp(page==INTERACTIVE?0:12));
            stage.animate().alpha(1).translationX(0).setDuration(200)
                    .setInterpolator(new DecelerateInterpolator()).start();
            if(page!=LIBRARY&&page!=INTERACTIVE&&page!=CLOSET)
                root.post(()->{if(root==currentRoot)PlaylumiMotion.reveal(currentRoot);});
        }
    }
    private void move(int next) {
        if(moving)return;
        autoAdvanceToken++;
        if(page==LIBRARY&&next!=LIBRARY&&scroller!=null)libraryScrollY=scroller.getScrollY();
        if(page==PLAY&&next!=PLAY)saveLibraryQuiz();
        if(page==INTERACTIVE&&next!=INTERACTIVE)saveLibraryInteractive();
        if(next==HOME)libraryReturn=false;
        saveUsage();
        if(usage!=null && page!=BREAK_SCREEN && next!=BREAK_SCREEN &&
                next!=PARENT_GATE && next!=PARENT_HOME &&
                next!=PARENT_WORLDS && next!=PARENT_STATS && next!=PARENT_SETTINGS &&
                usage.shouldSuggestBreak()) {
            breakReturn=next;next=BREAK_SCREEN;saveUsage();
        }
        if(advancingAutomatically&&next==page){showPage();return;}
        if(sound!=null)sound.tap();
        if(PlaylumiMotion.isReduced()) {
            page=next;showPage();return;
        }
        moving=true;
        FrameLayout previous=stage;
        final int destination=next;
        previous.animate().cancel();
        previous.animate().alpha(0f).translationX(-dp(12)).setDuration(115)
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
            libraryReturn=false;
            quickCategoryPlay=false;
            switch(place) {
                case PlaylumiVillageView.ROCKET:
                case PlaylumiVillageView.BOOKS:
                case PlaylumiVillageView.SHAPES:
                case PlaylumiVillageView.MUSIC:
                case PlaylumiVillageView.PLAYGROUND:
                case PlaylumiVillageView.COLORS:
                    openLibrary(place);
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
                    openLibrary(PlaylumiGameCatalog.OVERVIEW);
                    break;
                case PlaylumiVillageView.REWARDS:
                    move(SHOP);
                    break;
                case PlaylumiVillageView.STARS:
                    villageLaunch=PlaylumiVillageView.REWARDS;
                    move(SHOP);
                    break;
                case PlaylumiVillageView.DISCOVERY:
                    if(sound!=null)sound.tap();
                    int mask=getSharedPreferences("playlumi-village-"+activeProfile,
                            MODE_PRIVATE).getInt("visited",0);
                    int count=PlaylumiVillageProgress.visitedCount(mask);
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Mijn ontdekkingen")
                            .setMessage(count+" van de 6 speelplekken bezocht.")
                            .setPositiveButton("Verder spelen",null)
                            .show();
                    break;
                case PlaylumiVillageView.PLAYER:
                    villageLaunch=PlaylumiVillageView.PROFILE;
                    personalReturn=HOME;move(PROFILES);
                    break;
                case PlaylumiVillageView.HOME:
                    if(sound!=null)sound.tap();
                    // Already in the village.
                    break;
                case PlaylumiVillageView.COLLECTION:
                    collectionReturn=HOME;move(COLLECTIBLES);
                    break;
                case PlaylumiVillageView.PROFILE:
                    personalReturn=HOME;move(PROFILES);
                    break;
                default:break;
            }
        },activeProfile,wardrobe==null?0:wardrobe.stars(),chosenAvatar(),wardrobe==null?null:wardrobe.equippedItems());
        // The world takes the complete usable screen; no ScrollView, blank strip,
        // clipped bottom buttons, or duplicate white space below the illustration.
        stage.removeView(scroller);
        stage.addView(village,new FrameLayout.LayoutParams(-1,-1));
    }
    private void openLibrary(int filter){
        libraryReturn=false;libraryFilter=filter;libraryScrollY=0;storyStage=-1;move(LIBRARY);
    }
    private java.util.Set<String> librarySet(String key){
        return new java.util.HashSet<>(prefs().getStringSet(key,java.util.Collections.emptySet()));
    }
    private String snapshotKey(PlaylumiGameCatalog.Entry entry){
        return PlaylumiResumeKeys.session(age,entry.id);
    }
    private String legacySnapshotKey(PlaylumiGameCatalog.Entry entry){
        return entry.kind==PlaylumiGameCatalog.QUIZ?"library_quiz_resume":
                entry.kind==PlaylumiGameCatalog.INTERACTIVE?"library_interactive_resume":null;
    }
    private void rememberCatalog(PlaylumiGameCatalog.Entry entry){
        if(entry==null)return;
        java.util.Set<String> played=librarySet("library_played");played.add(entry.id);
        prefs().edit().putStringSet("library_played",played)
                .putString("library_last",entry.id).putInt("library_last_age",age)
                .putString(PlaylumiResumeKeys.last(age),entry.id).apply();
    }
    private PlaylumiGameCatalog.Entry lastCatalog(){
        String id=prefs().getString(PlaylumiResumeKeys.last(age),"");
        if(id.isEmpty()&&prefs().getInt("library_last_age",-1)==age)id=prefs().getString("library_last","");
        return PlaylumiGameCatalog.find(age,id);
    }
    private org.json.JSONObject matchingSnapshot(String key,PlaylumiGameCatalog.Entry entry){
        if(key==null)return null;
        try{
            org.json.JSONObject saved=new org.json.JSONObject(prefs().getString(key,""));
            return saved.getInt("age")==age&&entry.id.equals(saved.getString("id"))?saved:null;
        }catch(org.json.JSONException|ClassCastException ignored){return null;}
    }
    /** Move the matching old single-session record into the per-age/per-game slot only once. */
    private org.json.JSONObject readLibrarySnapshot(PlaylumiGameCatalog.Entry entry){
        org.json.JSONObject saved=matchingSnapshot(snapshotKey(entry),entry);
        if(saved!=null)return saved;
        String legacy=legacySnapshotKey(entry);
        saved=matchingSnapshot(legacy,entry);
        if(saved!=null){
            prefs().edit().putString(snapshotKey(entry),saved.toString()).remove(legacy).apply();
        }
        return saved;
    }
    private void clearLibrarySnapshot(PlaylumiGameCatalog.Entry entry){
        SharedPreferences.Editor edit=prefs().edit().remove(snapshotKey(entry));
        String legacy=legacySnapshotKey(entry);
        if(matchingSnapshot(legacy,entry)!=null)edit.remove(legacy);
        edit.apply();
    }
    private void writeLibrarySnapshot(PlaylumiGameCatalog.Entry entry,org.json.JSONObject saved){
        SharedPreferences.Editor edit=prefs().edit().putString(snapshotKey(entry),saved.toString());
        String legacy=legacySnapshotKey(entry);
        if(matchingSnapshot(legacy,entry)!=null)edit.remove(legacy);
        edit.apply();
    }
    private org.json.JSONObject libraryResume(PlaylumiGameCatalog.Entry entry){
        if(entry==null)return null;
        if(entry.kind==PlaylumiGameCatalog.INTERACTIVE)return libraryInteractiveResume(entry);
        if(entry.kind!=PlaylumiGameCatalog.QUIZ)return null;
        org.json.JSONObject saved=readLibrarySnapshot(entry);
        if(saved==null)return null;
        int count=saved.optInt("count",-1),attempts=saved.optInt("attempts",-1);
        if(count<0||count>=SESSION_ROUNDS||attempts<count||attempts==0)return null;
        return saved;
    }
    private org.json.JSONObject libraryInteractiveResume(PlaylumiGameCatalog.Entry entry){
        if(entry==null||entry.kind!=PlaylumiGameCatalog.INTERACTIVE)return null;
        org.json.JSONObject o=readLibrarySnapshot(entry);
        if(o==null)return null;
        try{
            int count=o.getInt("count");
            PlaylumiInteractive game=new PlaylumiInteractive(entry.mode,PlaylumiRocketCurriculum.level(age),o.getLong("seed"),count);
            if(count<=0||count>=game.total()||game.progress()!=count)return null;
            return o;
        }catch(org.json.JSONException|IllegalArgumentException ignored){return null;}
    }
    private void saveLibraryInteractive(){
        if(interactive==null)return;
        PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.route(age,villageLaunch,
                PlaylumiGameCatalog.INTERACTIVE,interactive.kind,0);
        if(entry==null)return;
        if(interactive.solved()||interactive.progress()==0){clearLibrarySnapshot(entry);return;}
        try{
            org.json.JSONObject o=new org.json.JSONObject();
            o.put("id",entry.id);o.put("age",age);o.put("seed",interactive.seed);
            o.put("count",interactive.progress());o.put("total",interactive.total());
            o.put("updated",System.currentTimeMillis());writeLibrarySnapshot(entry,o);
        }catch(org.json.JSONException ignored){}
    }
    private void saveLibraryQuiz(){
        if(game==null||storyStage>=0)return;
        PlaylumiGameCatalog.Entry e=PlaylumiGameCatalog.route(age,villageLaunch,
                PlaylumiGameCatalog.QUIZ,game.mode(),game.variant());
        PlaylumiGameCatalog.Entry last=lastCatalog();
        if(e==null||last==null||!e.id.equals(last.id))return;
        if(sessionSolved>=sessionGoal()||sessionAttempts==0){clearLibrarySnapshot(e);return;}
        try{
            LearningGame.Round r=game.round();
            org.json.JSONObject o=new org.json.JSONObject();
            o.put("id",e.id);o.put("age",age);o.put("level",game.level());
            o.put("count",sessionSolved);o.put("total",sessionGoal());o.put("attempts",sessionAttempts);o.put("solved",r.isSolved());
            o.put("prompt",r.prompt);o.put("display",r.display);o.put("color",r.displayColor);
            o.put("correct",r.correctIndex);o.put("fingers",r.fingerCount);
            o.put("answers",new org.json.JSONArray(r.answers));o.put("colors",new org.json.JSONArray(r.answerColors));
            o.put("updated",System.currentTimeMillis());writeLibrarySnapshot(e,o);
        }catch(org.json.JSONException ignored){}
    }
    private boolean resumeLibraryQuiz(PlaylumiGameCatalog.Entry entry){
        org.json.JSONObject o=libraryResume(entry);
        if(o==null)return false;
        try{
            int level=o.getInt("level"),count=o.getInt("count"),attempts=o.getInt("attempts");
            org.json.JSONArray a=o.getJSONArray("answers"),c=o.getJSONArray("colors");
            ArrayList<String> answers=new ArrayList<>();ArrayList<Integer> colors=new ArrayList<>();
            for(int i=0;i<a.length();i++)answers.add(a.getString(i));
            for(int i=0;i<c.length();i++)colors.add(c.getInt(i));
            if(level<0||level>2||count<0||count>=SESSION_ROUNDS||attempts<count)return false;
            game.configure(entry.mode,entry.variant,level);
            game.restoreRound(o.getString("prompt"),o.getString("display"),o.getInt("color"),
                    answers,colors,o.getInt("correct"),o.getInt("fingers"),o.getBoolean("solved"));
            difficulty=level;sessionSolved=count;sessionAttempts=attempts;feedback="";
            smartDifficulty=false;roundHadMistake=false;pendingLevel=-1;pendingBadges.clear();
            earnedBadge=-1;rewardReturn=PLAY;quickCategoryPlay=true;storyStage=-1;
            // The solved round already earned points. Resume at the next question, never credit it twice.
            if(game.round().isSolved())game.nextRound();
            saveRecent();saveLibraryQuiz();move(PLAY);return true;
        }catch(org.json.JSONException|IllegalArgumentException ignored){return false;}
    }
    private void startCatalogSpecial(int kind){
        rememberCatalog(PlaylumiGameCatalog.route(age,villageLaunch,kind,0,0));
        pendingBadges.clear();rewardReturn=PLAY;
        if(kind==PlaylumiGameCatalog.MUSIC){
            melodyVariant=0;melodyRound=0;melodySolved=false;
            melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());move(MUSIC_PLAY);
        }else if(kind==PlaylumiGameCatalog.BUBBLES){
            bubbleRound=0;bubbleSolved=false;
            bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());move(BUBBLE_PLAY);
        }else if(kind==PlaylumiGameCatalog.STARS){
            starCatchRound=0;starCatchSolved=false;
            starCatch=PlaylumiStarCatch.generate(Math.max(0,age),new Random());move(STAR_CATCH);
        }else if(kind==PlaylumiGameCatalog.DRAW)move(DRAW);
    }
    private void launchCatalog(PlaylumiGameCatalog.Entry entry,boolean resume){
        if(moving||entry==null)return;
        libraryReturn=true;villageLaunch=entry.world;storyStage=-1;rememberCatalog(entry);
        SharedPreferences discovery=getSharedPreferences("playlumi-village-"+activeProfile,MODE_PRIVATE);
        discovery.edit().putInt("visited",PlaylumiVillageProgress.markVisited(discovery.getInt("visited",0),entry.world)).apply();
        if(entry.kind==PlaylumiGameCatalog.QUIZ){
            if(resume&&resumeLibraryQuiz(entry))return;
            startWorldQuiz(entry.mode,entry.variant);
        }else if(entry.kind==PlaylumiGameCatalog.INTERACTIVE){
            org.json.JSONObject saved=resume?libraryInteractiveResume(entry):null;
            if(saved!=null){
                interactive=new PlaylumiInteractive(entry.mode,PlaylumiRocketCurriculum.level(age),saved.optLong("seed"),saved.optInt("count"));
                interactiveMessage="";move(INTERACTIVE);
            }else{rememberCatalog(entry);startInteractive(entry.mode);}
        }else if(entry.kind==PlaylumiGameCatalog.MINI)startWorldMini(entry.mode);
        else if(entry.kind==PlaylumiGameCatalog.MUSIC){
            melodyVariant=entry.variant;rememberCatalog(entry);pendingBadges.clear();rewardReturn=PLAY;
            melodyRound=0;melodySolved=false;
            melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());move(MUSIC_PLAY);
        }else startCatalogSpecial(entry.kind);
    }
    private void startInteractive(int kind){
        interactive=new PlaylumiInteractive(kind,PlaylumiRocketCurriculum.level(age),new Random().nextLong(),0);
        interactiveMessage="";saveLibraryInteractive();
        move(INTERACTIVE);
    }
    private void showInteractive(){
        if(interactive==null){move(LIBRARY);return;}
        final PlaylumiInteractive round=interactive;
        final int width=getResources().getConfiguration().screenWidthDp;
        final boolean wide=PlaylumiLayout.interactiveWide(width);
        header(PlaylumiInteractive.TITLES[round.kind],"");
        addText(round.kind==PlaylumiInteractive.NUMBERS?PlaylumiInteractive.INSTRUCTIONS[round.kind]+" Begin bij "+round.target(0)+".":PlaylumiInteractive.INSTRUCTIONS[round.kind],18,PlaylumiMagicArt.CREAM,4,5);
        addMagicProgress(round.progress(),round.total(),false);
        final PlaylumiStarTrail trail=roundStars;
        final TextView count=progressCounter;
        LinearLayout workspace=new LinearLayout(this);workspace.setOrientation(wide?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);workspace.setGravity(Gravity.TOP);
        LinearLayout boardPane=libraryPanel(),answerPane=libraryPanel();
        boardPane.setBackground(libraryCardSurface(PlaylumiWorldStyle.valid(villageLaunch)?villageLaunch:4));
        answerPane.setBackground(PlaylumiMagicArt.softPanel(this));
        if(wide){
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1f);bp.setMargins(dp(3),dp(3),dp(5),dp(3));
            LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,-2,1f);ap.setMargins(dp(5),dp(3),dp(3),dp(3));
            workspace.addView(boardPane,bp);workspace.addView(answerPane,ap);
        }else{
            workspace.addView(boardPane,new LinearLayout.LayoutParams(-1,-2));
            LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.topMargin=dp(6);workspace.addView(answerPane,ap);
        }
        addWidget(workspace,3,4);
        final PlaylumiInteractiveBoard board=new PlaylumiInteractiveBoard(this,round,PlaylumiLayout.interactiveBoardWidth(width));
        boardPane.addView(board,new LinearLayout.LayoutParams(-1,-2));
        final TextView message=text(round.solved()?"🌟 Knap gedaan! Alles klaar!":interactiveMessage.isEmpty()?"Jij bent aan de beurt!":interactiveMessage,16,PlaylumiMagicArt.CREAM);
        message.setMinHeight(dp(48));message.setMaxLines(2);message.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        boardPane.addView(message,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView answerTitle=text(round.kind==PlaylumiInteractive.HABITATS?"Kies een huisje":"Kies een tegel",17,PlaylumiMagicArt.CREAM);
        answerTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);answerPane.addView(answerTitle);
        final int choiceColumns=PlaylumiLayout.interactiveChoiceColumns(width,round.options().size());
        final LinearLayout choices=new LinearLayout(this);choices.setOrientation(LinearLayout.VERTICAL);answerPane.addView(choices);
        final ArrayList<Button> buttons=new ArrayList<>();
        final Button again=tile("Nog eens spelen!","",0xffd5f5df,64);
        final Button back=tile("Andere spellen","",0xffe8d9ff,64);
        final LinearLayout finish=finishActions(again,back,round.solved());
        again.setOnClickListener(v->startInteractive(round.kind));back.setOnClickListener(v->move(LIBRARY));
        LinearLayout row=null;
        for(int i=0;i<round.options().size();i++){
            if(i%choiceColumns==0){
                row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.setMargins(dp(2),dp(3),dp(2),dp(3));choices.addView(row,rp);
            }
            final String option=round.options().get(i);
            final Button choice=tile(option,"",COLORS[i%COLORS.length],72);
            choice.setMinHeight(dp(wide?80:66));choice.setTextSize(round.kind==PlaylumiInteractive.NUMBERS||round.kind==PlaylumiInteractive.LETTERS?25:17);
            choice.setContentDescription("Kies "+option);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),dp(2),dp(3),dp(2));row.addView(choice,lp);buttons.add(choice);
            if(round.kind==PlaylumiInteractive.NUMBERS)for(int n=0;n<round.progress();n++)if(option.equals(round.target(n))){
                choice.setEnabled(false);choice.setAlpha(.45f);break;
            }
            choice.setOnClickListener(v->{
                if(interactive!=round||page!=INTERACTIVE||moving||round.solved())return;
                if(round.choose(option)){
                    if(round.solved())sound.collect();else sound.good();
                    interactiveMessage="Goed zo!";message.setTextColor(0xffc7ffd8);
                    if(playPal!=null)playPal.react(1);
                    board.refresh();count.setText(round.progress()+"/"+round.total());
                    trail.showProgress(round.progress());saveLibraryInteractive();
                    if(round.kind==PlaylumiInteractive.NUMBERS){choice.setEnabled(false);choice.setAlpha(.45f);}
                    else PlaylumiMotion.pop(choice);
                    if(round.solved()){
                        int category=round.kind==PlaylumiInteractive.NUMBERS?LearningGame.NUMBERS:
                                round.kind==PlaylumiInteractive.LETTERS?LearningGame.WORDS:
                                round.kind==PlaylumiInteractive.HABITATS?21:LearningGame.PATTERNS;
                        ArrayList<Integer> badges=new ArrayList<>(scores.answer(category,true));
                        lifetimeCount++;wardrobe.earnAnswer();wardrobe.earnBadges(badges.size());
                        saveWardrobe();saveScore(category);
                        interactiveMessage="🌟 Knap gedaan! +"+(PlaylumiWardrobe.STARS_FOR_ANSWER+badges.size()*PlaylumiWardrobe.STARS_FOR_BADGE)+" speelsterren";
                        for(Button b:buttons)b.setEnabled(false);
                        showFinishActions(finish,true);PlaylumiDelight.sprinkle(stage,board);
                    }
                }else{
                    sound.mistake();interactiveMessage="Probeer nog eens!";message.setTextColor(PlaylumiMagicArt.GOLD);PlaylumiDelight.wiggle(choice);
                }
                message.setText(interactiveMessage);
            });
        }
        if(round.solved())for(Button b:buttons)b.setEnabled(false);
    }

    private void libraryClick(View view,String description,Runnable action){
        view.setContentDescription(description);view.setClickable(true);view.setFocusable(true);
        view.setOnClickListener(v->{if(sound!=null)sound.tap();action.run();});
    }
    private LinearLayout libraryPanel(){
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(8),dp(8),dp(8),dp(8));p.setBackground(PlaylumiMagicArt.softPanel(this));
        return p;
    }
    private void libraryFilter(int filter){
        if(moving||page!=LIBRARY||libraryFilter==filter)return;
        libraryFilter=filter;libraryScrollY=0;
        renderLibraryContents(0);
    }
    private static final String[] LIBRARY_TAB_LABELS={"Dorp","Werelden","Spellen","Favorieten"};
    private static final String[] LIBRARY_TAB_ICONS={"⌂","✦","▶","★"};
    private static final int[] LIBRARY_TAB_FILTERS={Integer.MIN_VALUE,PlaylumiGameCatalog.OVERVIEW,PlaylumiGameCatalog.ALL,PlaylumiGameCatalog.FAVORITES};
    private void refreshLibraryNavigation(){
        if(libraryTabs==null)return;
        int favorites=PlaylumiGameCatalog.filter(age,PlaylumiGameCatalog.FAVORITES,librarySet("library_favorites")).size();
        for(int i=0;i<libraryTabs.length;i++){
            TextView tab=libraryTabs[i];
            boolean selected=LIBRARY_TAB_FILTERS[i]==libraryFilter||(i==1&&libraryFilter>=0);
            String label=LIBRARY_TAB_LABELS[i]+(i==3&&favorites>0?" "+favorites:"");
            tab.setText(LIBRARY_TAB_ICONS[i]+"\n"+label);
            tab.setSelected(selected);tab.setTextColor(selected?PlaylumiMagicArt.INK:PlaylumiMagicArt.CREAM);
            tab.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                    selected?PlaylumiMagicArt.creamPlate(this):rounded(0xff573785,13),null));
            tab.setContentDescription((i==2?"Alle spellen":label)+(selected?", geselecteerd":""));
        }
    }
    private Drawable libraryCardSurface(int world){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{PlaylumiWorldStyle.plateTop(world),PlaylumiWorldStyle.plateMiddle(world),0xff3d235c});
        d.setCornerRadius(dp(19));d.setStroke(dp(1),0xffd8b67d);
        return d;
    }
    private void librarySection(String title,String detail){
        LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);
        TextView name=text(title,19,PlaylumiMagicArt.CREAM);name.setGravity(Gravity.START);
        name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        line.addView(name,new LinearLayout.LayoutParams(0,-2,1f));
        TextView info=text(detail,13,PlaylumiMagicArt.GOLD);info.setGravity(Gravity.END);
        line.addView(info,new LinearLayout.LayoutParams(-2,-2));
        addWidget(line,7,4);
    }
    private void libraryWorldStrip(){
        android.widget.HorizontalScrollView strip=new android.widget.HorizontalScrollView(this);
        strip.setHorizontalScrollBarEnabled(false);
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        for(int w=0;w<6;w++){
            final int index=w;
            boolean selected=libraryFilter==w;
            TextView chip=text(PlaylumiWorldStyle.NAMES[w],14,selected?PlaylumiMagicArt.INK:PlaylumiMagicArt.CREAM);
            chip.setPadding(dp(11),dp(7),dp(11),dp(7));chip.setMinHeight(dp(48));
            chip.setBackground(selected?PlaylumiMagicArt.creamPlate(this):libraryCardSurface(w));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.setMargins(dp(3),0,dp(3),0);row.addView(chip,lp);
            libraryClick(chip,"Open "+PlaylumiWorldStyle.NAMES[w],()->libraryFilter(index));
            if(selected)strip.post(()->strip.smoothScrollTo(Math.max(0,chip.getLeft()-dp(8)),0));
        }
        strip.addView(row);addWidget(strip,2,5);
    }
    private void showLibraryNavigation(){
        final int width=getResources().getConfiguration().screenWidthDp;
        final boolean rail=PlaylumiLayout.libraryRail(width);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);
        nav.setOrientation(rail?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        nav.setPadding(dp(5),dp(5),dp(5),dp(5));nav.setBackground(PlaylumiMagicArt.primary(this));
        libraryTabs=new TextView[LIBRARY_TAB_FILTERS.length];
        for(int i=0;i<LIBRARY_TAB_FILTERS.length;i++){
            final int filter=LIBRARY_TAB_FILTERS[i];
            TextView tab=text("",width<360?11:13,PlaylumiMagicArt.CREAM);
            tab.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            tab.setPadding(dp(2),dp(2),dp(2),dp(2));tab.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp=rail?new LinearLayout.LayoutParams(-1,dp(68)):new LinearLayout.LayoutParams(0,-1,1f);
            lp.setMargins(dp(2),dp(2),dp(2),dp(2));nav.addView(tab,lp);libraryTabs[i]=tab;
            libraryClick(tab,LIBRARY_TAB_LABELS[i],()->{
                if(filter==Integer.MIN_VALUE)move(HOME);else libraryFilter(filter);
            });
        }
        refreshLibraryNavigation();
        FrameLayout.LayoutParams scrollParams=new FrameLayout.LayoutParams(-1,-1);
        if(rail)scrollParams.leftMargin=dp(PlaylumiLayout.LIBRARY_RAIL_WIDTH+14);
        else scrollParams.bottomMargin=dp(74);
        scroller.setLayoutParams(scrollParams);
        FrameLayout.LayoutParams navParams=rail?
                new FrameLayout.LayoutParams(dp(PlaylumiLayout.LIBRARY_RAIL_WIDTH),-1,Gravity.START|Gravity.CENTER_VERTICAL):
                new FrameLayout.LayoutParams(-1,dp(68),Gravity.BOTTOM);
        int pad=dp(rail?8:PlaylumiLayout.librarySidePadding(width));
        navParams.setMargins(pad,rail?dp(8):0,pad,dp(3));stage.addView(nav,navParams);
    }
    private void showGameLibrary(){
        showLibraryNavigation();renderLibraryContents(libraryScrollY);
    }
    private void renderLibraryContents(int desiredScroll){
        final ScrollView active=scroller;final int filter=libraryFilter;
        root.removeAllViews();renderLibraryBody();refreshLibraryNavigation();
        libraryScrollY=Math.max(0,desiredScroll);
        active.post(()->{if(scroller==active&&page==LIBRARY&&libraryFilter==filter)active.scrollTo(0,Math.max(0,desiredScroll));});
    }
    private void renderLibraryBody(){
        header(libraryFilter>=0?PlaylumiWorldStyle.NAMES[libraryFilter]:
                libraryFilter==PlaylumiGameCatalog.FAVORITES?"Mijn favorieten":
                libraryFilter==PlaylumiGameCatalog.ALL?"Alle spellen":"Spellen","");
        addText("✦  "+AGES[PlaylumiRocketCurriculum.level(age)]+"  ·  Kies jouw avontuur",15,PlaylumiMagicArt.CREAM,2,6);
        java.util.Set<String> favorites=librarySet("library_favorites");
        java.util.Set<String> played=librarySet("library_played");
        final int width=getResources().getConfiguration().screenWidthDp;
        final int columns=PlaylumiLayout.libraryColumns(PlaylumiLayout.libraryContentWidth(width));
        if(libraryFilter==PlaylumiGameCatalog.OVERVIEW){
            PlaylumiGameCatalog.Entry last=lastCatalog();
            java.util.List<PlaylumiGameCatalog.Entry> inProgress=new ArrayList<>();
            for(PlaylumiGameCatalog.Entry candidate:PlaylumiGameCatalog.entries(age))if(libraryResume(candidate)!=null)inProgress.add(candidate);
            java.util.Collections.sort(inProgress,(left,right)->{
                org.json.JSONObject x=libraryResume(left),y=libraryResume(right);
                long lx=x==null?0:x.optLong("updated",0),ry=y==null?0:y.optLong("updated",0);
                return Long.compare(ry,lx);
            });
            if(!inProgress.isEmpty()){
                librarySection("Verder spelen",inProgress.size()+(inProgress.size()==1?" spel":" spellen"));
                for(int i=0;i<Math.min(3,inProgress.size());i++)addWidget(libraryGameCard(inProgress.get(i),favorites,played,false),1,3);
                if(inProgress.size()>3){Button more=tile("Bekijk alle spellen","Ook jouw andere bewaarde rondes",COLORS[2],60);addTile(more,3);more.setOnClickListener(v->libraryFilter(PlaylumiGameCatalog.ALL));}
            }
            if(last!=null&&inProgress.isEmpty()){
                org.json.JSONObject snapshot=libraryResume(last);
                boolean resume=snapshot!=null;
                int completed=resume?snapshot.optInt("count",0):0;
                LinearLayout recentCard=libraryPanel();
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
                row.addView(PlaylumiMagicArt.hero(this,last.world),new LinearLayout.LayoutParams(dp(96),dp(76)));
                LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);
                info.setPadding(dp(10),0,dp(4),0);
                TextView label=text(resume?"Verder spelen":"Opnieuw spelen",14,0xffffdc85);
                label.setGravity(Gravity.START);info.addView(label);
                TextView name=text(last.title,19,PlaylumiMagicArt.CREAM);
                name.setGravity(Gravity.START);info.addView(name);
                TextView details=text(resume?completed+" van "+snapshot.optInt("total",SESSION_ROUNDS)+" klaar":PlaylumiWorldStyle.NAMES[last.world],13,0xffe6ccff);
                details.setGravity(Gravity.START);info.addView(details);
                row.addView(info,new LinearLayout.LayoutParams(0,-2,1f));
                row.addView(text("▶",22,0xffffd47a),new LinearLayout.LayoutParams(dp(32),-1));
                recentCard.addView(row);libraryClick(recentCard,(resume?"Verder spelen ":"Opnieuw spelen ")+last.title,()->launchCatalog(last,resume));addWidget(recentCard,3,6);
            }
            Button all=tile("Alle spellen",""+PlaylumiGameCatalog.entries(age).size()+" avonturen",COLORS[2],58);
            Button fav=tile("★ Favorieten",""+PlaylumiGameCatalog.filter(age,PlaylumiGameCatalog.FAVORITES,favorites).size()+" bewaard",COLORS[1],58);
            all.setOnClickListener(v->libraryFilter(PlaylumiGameCatalog.ALL));
            fav.setOnClickListener(v->libraryFilter(PlaylumiGameCatalog.FAVORITES));
            addTwo(all,fav,58);
            librarySection("Onze speelwerelden","6 avonturen");
            int worldColumns=PlaylumiLayout.libraryContentWidth(width)>=600?3:2;
            for(int start=0;start<6;start+=worldColumns){
                LinearLayout row=new LinearLayout(this);
                for(int i=start;i<Math.min(start+worldColumns,6);i++){
                    final int worldIndex=i;
                    LinearLayout card=libraryPanel();card.setBackground(libraryCardSurface(worldIndex));
                    card.addView(PlaylumiMagicArt.hero(this,worldIndex),new LinearLayout.LayoutParams(-1,dp(PlaylumiLayout.libraryWorldArtHeight(width))));
                    TextView title=text(PlaylumiWorldStyle.NAMES[worldIndex],16,PlaylumiMagicArt.CREAM);
                    title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setMinHeight(dp(28));card.addView(title);
                    int n=PlaylumiGameCatalog.filter(age,worldIndex,favorites).size();
                    card.addView(text(n+(n==1?" spel":" spellen")+"  ›",14,PlaylumiMagicArt.GOLD));
                    card.addView(text("✦ "+PlaylumiCollection.unlocked(collectionProgress(worldIndex))+"/3 schatten",12,0xfff2e2ff));
                    libraryClick(card,"Open "+PlaylumiWorldStyle.NAMES[worldIndex]+" "+n+" spellen",()->libraryFilter(worldIndex));
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
                    lp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(card,lp);
                }
                root.addView(row,new LinearLayout.LayoutParams(-1,-2));
            }
            Button missions=tile("Missies","Speel je bestaande avonturen",COLORS[3],64);
            addTile(missions,7);missions.setOnClickListener(v->{
                libraryReturn=false;villageLaunch=PlaylumiVillageView.MISSIONS;
                world=0;storyPage=Math.min(2,adventures.progress(world)/2);move(ADVENTURE_MAP);
            });
            return;
        }
        if(libraryFilter>=0){
            View art=PlaylumiMagicArt.hero(this,libraryFilter);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(width>=600?140:94));
            lp.setMargins(dp(4),dp(2),dp(4),dp(6));root.addView(art,lp);
            addText(PlaylumiWorldStyle.SUBTITLES[libraryFilter],15,PlaylumiMagicArt.CREAM,2,4);
            libraryWorldStrip();
        }else if(libraryFilter==PlaylumiGameCatalog.ALL)libraryWorldStrip();
        java.util.List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.filter(age,libraryFilter,favorites);
        librarySection(libraryFilter==PlaylumiGameCatalog.FAVORITES?"Jouw lievelingsspellen":"Kies een spel",entries.size()+(entries.size()==1?" spel":" spellen"));
        if(entries.isEmpty()){
            addText("Nog geen favorieten. Tik op een ster naast je lievelingsspel!",17,PlaylumiMagicArt.CREAM,12,8);
            Button discover=tile("Ontdek alle spellen","",COLORS[2],64);addTile(discover,4);discover.setOnClickListener(v->libraryFilter(PlaylumiGameCatalog.ALL));
            return;
        }
        for(int w=0;w<6;w++){
            java.util.List<PlaylumiGameCatalog.Entry> group=new ArrayList<>();
            for(PlaylumiGameCatalog.Entry e:entries)if(e.world==w)group.add(e);
            if(group.isEmpty())continue;
            if(libraryFilter<0)librarySection(PlaylumiWorldStyle.NAMES[w],group.size()+" spellen");
            for(int first=0;first<group.size();first+=columns){
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);
                int n=Math.min(columns,group.size()-first);
                for(int j=0;j<n;j++){
                    View card=libraryGameCard(group.get(first+j),favorites,played,columns>1);
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
                    lp.setMargins(dp(4),dp(3),dp(4),dp(3));row.addView(card,lp);
                }
                for(int j=n;j<columns;j++)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1f));
                root.addView(row,new LinearLayout.LayoutParams(-1,-2));
            }
        }
    }
    private View libraryGameCard(PlaylumiGameCatalog.Entry entry,java.util.Set<String> favorites,java.util.Set<String> played,boolean wide){
        LinearLayout card=new LinearLayout(this);card.setOrientation(wide?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(8),dp(7),dp(8),dp(7));
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x25ffffff),libraryCardSurface(entry.world),null));
        card.setMinimumHeight(dp(wide?138:82));
        org.json.JSONObject snapshot=libraryResume(entry);
        boolean resume=snapshot!=null;
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon=text(entry.icon,wide?29:25,PlaylumiMagicArt.INK);
        icon.setBackground(PlaylumiMagicArt.gemButton(this,entry.world));icon.setIncludeFontPadding(false);
        heading.addView(icon,new LinearLayout.LayoutParams(dp(wide?49:46),dp(wide?49:46)));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.setPadding(dp(8),0,dp(3),0);
        TextView name=text(entry.title,17,PlaylumiMagicArt.CREAM);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        name.setGravity(Gravity.START);labels.addView(name,new LinearLayout.LayoutParams(-1,-2));
        if(!wide){
            TextView description=text(entry.description,13,0xfff2e4ff);description.setGravity(Gravity.START);labels.addView(description);
        }
        heading.addView(labels,new LinearLayout.LayoutParams(0,-2,1f));
        TextView star=text(favorites.contains(entry.id)?"★":"☆",26,0xffffdf85);
        updateFavoriteLabel(star,entry,favorites.contains(entry.id));
        heading.addView(star,new LinearLayout.LayoutParams(dp(48),dp(48)));
        if(wide){
            card.addView(heading,new LinearLayout.LayoutParams(-1,-2));
            TextView description=text(entry.description,13,0xfff2e4ff);description.setGravity(Gravity.START);
            description.setMinLines(2);description.setPadding(dp(3),dp(4),dp(3),0);
            card.addView(description,new LinearLayout.LayoutParams(-1,-2));
        }else{
            card.addView(heading,new LinearLayout.LayoutParams(0,-2,1f));
        }
        String resumeLabel=resume?"▶ Verder "+snapshot.optInt("count",0)+"/"+snapshot.optInt("total",SESSION_ROUNDS):"▶ Spelen";
        TextView play=text(resume?resumeLabel:played.contains(entry.id)?"▶ Opnieuw":"▶ Spelen",wide?15:13,PlaylumiMagicArt.INK);
        play.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        play.setBackground(PlaylumiMagicArt.creamPlate(this));play.setPadding(dp(6),dp(3),dp(6),dp(3));play.setMinHeight(dp(34));
        if(wide){
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(38));lp.setMargins(0,dp(4),0,0);card.addView(play,lp);
        }else{
            // Keep the title readable on narrow devices; the whole tile launches the game.
            TextView action=text(resume?resumeLabel:played.contains(entry.id)?"▶ Opnieuw spelen":"▶ Spelen",12,PlaylumiMagicArt.GOLD);
            action.setGravity(Gravity.START);labels.addView(action);
        }
        libraryClick(card,(resume?"Verder spelen ":"Speel ")+entry.title,()->launchCatalog(entry,resume));
        if(resume)card.setOnLongClickListener(v->{
            new android.app.AlertDialog.Builder(this).setTitle(entry.title).setMessage("Opnieuw beginnen met dit spel? Je verdiende sterren en schatten blijven bewaard.")
                    .setNegativeButton("Verder spelen",null).setPositiveButton("Opnieuw beginnen",(d,which)->launchCatalog(entry,false)).show();
            return true;
        });
        star.setClickable(true);star.setFocusable(true);
        star.setOnClickListener(v->{
            java.util.Set<String> saved=librarySet("library_favorites");
            boolean now=!saved.contains(entry.id);
            if(now)saved.add(entry.id);else saved.remove(entry.id);
            prefs().edit().putStringSet("library_favorites",saved).apply();
            if(sound!=null)sound.tap();
            updateFavoriteLabel(star,entry,now);refreshLibraryNavigation();
            if(now)PlaylumiMotion.pop(star);
            if(libraryFilter==PlaylumiGameCatalog.FAVORITES&&!now)renderLibraryContents(scroller.getScrollY());
        });
        return card;
    }
    private void updateFavoriteLabel(TextView star,PlaylumiGameCatalog.Entry entry,boolean saved){
        star.setText(saved?"★":"☆");star.setSelected(saved);
        star.setBackground(saved?PlaylumiMagicArt.silkRibbon(this):rounded(0xff3b245d,13));
        star.setContentDescription((saved?"Verwijder favoriet ":"Bewaar favoriet ")+entry.title);
    }

    private void worldChoice(String title,String description,int color,Runnable action) {
        Button b=tile(title,description,color,88);
        b.setTextSize(18);
        addTile(b,5);
        b.setOnClickListener(v->action.run());
    }
    private void startWorldQuiz(int mode,int variant) {
        storyStage=-1;quickCategoryPlay=true;smartDifficulty=true;roundHadMistake=false;
        pendingLevel=-1;sessionSolved=0;sessionAttempts=0;feedback="";pendingBadges.clear();rewardReturn=PLAY;
        boolean rocket=villageLaunch==PlaylumiVillageHit.ROCKET && PlaylumiRocketCurriculum.isMathMode(mode);
        if(rocket||libraryReturn)smartDifficulty=false;
        difficulty=rocket||libraryReturn?PlaylumiRocketCurriculum.level(age):adaptive.level(mode,Math.max(0,age));
        game.configure(mode,variant,difficulty);
        prefs().edit().putInt("game_mode",mode).putInt("game_variant",variant)
                .putInt("last_played_category",mode).putInt("last_played_variant",variant).apply();
        rememberCatalog(PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.QUIZ,mode,variant));
        saveRecent();saveLibraryQuiz();move(PLAY);
    }
    private void startWorldMini(int kind) {
        miniKind=kind;miniRound=0;miniSolved=false;pendingBadges.clear();
        mini=PlaylumiMiniChallenge.generate(kind,Math.max(0,age),new Random());
        rememberCatalog(PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.MINI,kind,0));
        move(MINI_PLAY);
    }
    private void showVillageMenu() {
        if(!PlaylumiWorldStyle.valid(villageLaunch)) {villageLaunch=PlaylumiVillageHit.ROCKET;}
        int w=villageLaunch;
        header(PlaylumiWorldStyle.NAMES[w],"");
        addIllustratedPortal(w,PlaylumiWorldStyle.NAMES[w],landscapeScreen()?154:211);
        addWidget(magicRibbon(PlaylumiWorldStyle.SUBTITLES[w],20),0,5);
        switch(w) {
            case PlaylumiVillageHit.ROCKET:
                addText("✦  "+AGES[PlaylumiRocketCurriculum.level(age)]+"  ✦",16,PlaylumiMagicArt.CREAM,1,4);
                int missionColor=0;
                for(PlaylumiRocketCurriculum.Mission mission:PlaylumiRocketCurriculum.missions(age)){
                    worldChoice(mission.title,mission.description,COLORS[missionColor++%COLORS.length],
                            ()->startWorldQuiz(mission.mode,mission.variant));
                }
                break;
            case PlaylumiVillageHit.BOOKS:
                worldChoice("Kleine letters","Vind de kleine letter",COLORS[0],()->startWorldQuiz(LearningGame.LETTERS,0));
                worldChoice("Hoofdletters","Vind de grote letter",COLORS[1],()->startWorldQuiz(LearningGame.LETTERS,1));
                worldChoice("Woorden maken","Vul de ontbrekende letter in",COLORS[2],()->startWorldQuiz(LearningGame.WORDS,0));
                worldChoice("Meer lettermissies","Kies niveau, spelvorm en badges",COLORS[3],()->selectCategory(LearningGame.LETTERS));
                break;
            case PlaylumiVillageHit.SHAPES:
                worldChoice("Sleep de vormen","Breng vormen naar hun huisje",COLORS[0],()->startWorldMini(PlaylumiMiniChallenge.DRAG));
                worldChoice("Vormen zoeken","Vind dezelfde vorm",COLORS[1],()->startWorldQuiz(LearningGame.SHAPES,0));
                worldChoice("Vormen benoemen","Herken de vorm",COLORS[2],()->startWorldQuiz(LearningGame.SHAPES,1));
                break;
            case PlaylumiVillageHit.MUSIC:
                worldChoice("Speel het deuntje","Luister en speel de melodie na",COLORS[0],()->{
                    startCatalogSpecial(PlaylumiGameCatalog.MUSIC);
                });
                addText("Vier dierenvriendjes maken elk een eigen toon. Luister goed en speel in dezelfde volgorde.",16,PlaylumiWorldStyle.deep(w),4,8);
                break;
            case PlaylumiVillageHit.PLAYGROUND:
                worldChoice("Dierenmemory","Zoek bij elkaar passende dieren",COLORS[0],()->startWorldMini(PlaylumiMiniChallenge.MEMORY));
                worldChoice("Tel de ballonnen","Leer hoeveelheden tellen",COLORS[1],()->startWorldMini(PlaylumiMiniChallenge.BALLOONS));
                worldChoice("Bellenfeest","Laat zeepbellen knappen",COLORS[2],()->{
                    startCatalogSpecial(PlaylumiGameCatalog.BUBBLES);
                });
                worldChoice("Vang de sterren","Verzamel de gevraagde sterren",COLORS[3],()->{
                    startCatalogSpecial(PlaylumiGameCatalog.STARS);
                });
                break;
            case PlaylumiVillageHit.COLORS:
                worldChoice("Kleuren herkennen","Welke kleur zie je?",COLORS[0],()->startWorldQuiz(LearningGame.COLORS,0));
                worldChoice("Kleuren zoeken","Vind de gevraagde kleur",COLORS[1],()->startWorldQuiz(LearningGame.COLORS,1));
                worldChoice("Mijn kunstwerk","Teken en kleur vrij",COLORS[2],()->startCatalogSpecial(PlaylumiGameCatalog.DRAW));
                worldChoice("Meer kleurmissies","Kies niveau, spelvorm en badges",COLORS[3],()->selectCategory(LearningGame.COLORS));
                break;
            default:break;
        }
        Button more=tile("✦ Meer spellen","Bekijk alle spellen van deze wereld",COLORS[2],76);
        addTile(more,6);more.setOnClickListener(v->openLibrary(w));
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
        Button profile=tile("✦ Mijn maatje","Verander je karakter",0xffdeeeff,91);
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
        PlaylumiScores.Score score=scores.get(mode);
        String name=LearningGame.GAME_NAMES[mode];
        header(ICONS[mode]+" "+name,"Jouw magische avontuur");
        addIllustratedPortal(portalForMode(mode),name,240);
        TextView progress=text("★  "+score.correct+" punten    ✦    "+
                score.earnedBadges()+"/6 badges",20,0xffffe4a1);
        GradientDrawable badgeBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff8052b8,0xff503180});
        badgeBg.setCornerRadius(dp(23));
        badgeBg.setStroke(dp(2),0xffffd49a);
        progress.setPadding(dp(8),dp(11),dp(8),dp(11));
        progress.setBackground(badgeBg);
        addWidget(progress,3,9);
        Button play=tile("▶  START AVONTUUR!","Direct spelen op jouw niveau",
                0xffbdeecf,105);
        play.setTextSize(21);
        addTile(play,7);play.setOnClickListener(v->startCategoryNow());
        Button level=tile("🎯  Niveau en spelvorm kiezen",
                "Pas het spel aan zoals jij wilt",0xffe7d8fb,82);
        addTile(level,4);level.setOnClickListener(v->move(LEVEL));
        Button badge=tile("🏅  Mijn badges",
                score.earnedBadges()+" van 6 verdiend",0xffffe4b9,76);
        addTile(badge,4);
        badge.setOnClickListener(v->{badgePage=0;badgesFromTrophy=false;move(BADGES);});
    }
    private void showLevel() {
        header("🚀 Kies je uitdaging","Hoe moeilijk wil je spelen?");
        addIllustratedPortal(portalForMode(game.mode()),"Kies je missie",134);
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
                case LearningGame.NUMBERS:first=new String[]{"Vingers tellen","Cijfer naar woord"};break;
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
        addIllustratedPortal(portalForMode(game.mode()),"Speel jouw avontuur",118);
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
    private void showPlay() {
        int mode=game.mode();
        if(sessionSolved>=sessionGoal()) {showCompleted();return;}
        autoRoundVisible=true;
        LearningGame.Round round=game.round();
        boolean wide=landscapeScreen();
        int width=getResources().getConfiguration().screenWidthDp;
        int height=getResources().getConfiguration().screenHeightDp;
        boolean centered=PlaylumiLayout.centeredMagicQuiz(width,height);
        boolean split=wide&&!centered;
        int theme=worldTheme();
        header(LearningGame.GAME_NAMES[mode],sessionSolved+"/"+sessionGoal());
        addIllustratedPortal(theme,PlaylumiWorldStyle.NAMES[theme],118);
        addMagicProgress(sessionSolved,sessionGoal(),true);
        LinearLayout questions=root,answers=root;
        if(centered) {
            LinearLayout canvas=new LinearLayout(this);
            canvas.setOrientation(LinearLayout.VERTICAL);
            canvas.setGravity(Gravity.CENTER_HORIZONTAL);
            canvas.setPadding(dp(12),dp(8),dp(12),dp(10));
            canvas.setBackground(PlaylumiMagicArt.softPanel(this));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
            cp.setMargins(dp(4),dp(5),dp(4),dp(8));root.addView(canvas,cp);
            questions=canvas;answers=canvas;
        }else if(split) {
            LinearLayout columns=new LinearLayout(this);
            columns.setOrientation(LinearLayout.HORIZONTAL);
            columns.setGravity(Gravity.TOP);
            root.addView(columns,new LinearLayout.LayoutParams(-1,-2));
            questions=new LinearLayout(this);questions.setOrientation(LinearLayout.VERTICAL);
            answers=new LinearLayout(this);answers.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1f);
            LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1f);
            left.setMargins(0,0,dp(8),0);right.setMargins(dp(8),0,0,0);
            questions.setPadding(dp(6),dp(6),dp(6),dp(6));
            answers.setPadding(dp(6),dp(6),dp(6),dp(6));
            questions.setBackground(PlaylumiMagicArt.softPanel(this));
            answers.setBackground(PlaylumiMagicArt.softPanel(this));
            columns.addView(questions,left);columns.addView(answers,right);
        }
        TextView prompt=magicRibbon(round.prompt,wide?20:19);
        addWidgetIn(questions,prompt,0,dp(5));
        boolean shortDisplay=round.display.codePointCount(0,round.display.length())<=3;
        int questionH=centered?PlaylumiLayout.centeredQuestionHeight(height):PlaylumiLayout.magicQuestionHeight(width,height,wide);
        if(!wide&&height<690)questionH=Math.min(questionH,110);
        if(round.fingerCount>0){
            PlaylumiNumberHands hands=new PlaylumiNumberHands(this,round.fingerCount);
            hands.setBackground(wide?PlaylumiMagicArt.parchment(this):PlaylumiMagicArt.question(this,false));
            hands.setMinimumHeight(dp(wide?Math.min(210,questionH):Math.max(145,questionH)));
            addWidgetIn(questions,hands,0,dp(3));
        } else if(mode==16 && round.display.startsWith("clock:")) {
            String[] time=round.display.substring(6).split(":");
            PlaylumiClockFace clock=new PlaylumiClockFace(this,Integer.parseInt(time[0]),Integer.parseInt(time[1]));
            clock.setBackground(PlaylumiMagicArt.parchment(this));clock.setMinimumHeight(dp(Math.max(148,questionH)));
            addWidgetIn(questions,clock,0,dp(3));
        } else if(mode==LearningGame.COLORS && "●".equals(round.display)) {
            PlaylumiColorSwatch swatch=new PlaylumiColorSwatch(this,round.displayColor,!wide);
            swatch.setMinimumHeight(dp(questionH));
            addWidgetIn(questions,swatch,0,dp(3));
        } else {
            int glyphSize=shortDisplay?(wide?70:58):
                    mode==ExtraGames.MATH&&game.variant()!=4?(wide?48:36):
                    round.display.length()>22?25:round.display.length()>12?31:40;
            TextView picture=text(round.visibleDisplay(),glyphSize,PlaylumiMagicArt.INK);
            questionPicture=picture;
            picture.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
            picture.setBackground(wide?PlaylumiMagicArt.parchment(this):PlaylumiMagicArt.question(this,!wide && shortDisplay));
            picture.setMinHeight(dp(questionH));
            picture.setPadding(dp(12),dp(5),dp(12),dp(5));
            picture.setMaxLines(4);
            updateQuestionPicture(round);
            addWidgetIn(questions,picture,0,dp(3));
        }
        feedbackText=text(round.isSolved()?(feedback.isEmpty()?"⭐ Goed gedaan!":feedback):"Kies het juiste antwoord",15,PlaylumiMagicArt.CREAM);
        feedbackText.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        feedbackText.setMinHeight(dp(23));
        feedbackText.setShadowLayer(dp(1),0,dp(1),0xff2e164c);
        addWidgetIn(answers,feedbackText,0,dp(2));
        answerButtons.clear();
        answerLocked=round.isSolved();
        int columns=PlaylumiAnswerOrder.verticalPair(round.answers)?1:PlaylumiAnswerOrder.horizontalPair(round.answers)?2:centered?PlaylumiLayout.centeredAnswerColumns(round.answers.size()):2;
        int rowHeight=centered?PlaylumiLayout.centeredAnswerHeight(height,round.answers.size()):PlaylumiLayout.magicAnswerHeight(height,round.answers.size(),wide);
        if(split)rowHeight=Math.min(rowHeight,64);
        if(height<690)rowHeight=Math.max(48,Math.min(rowHeight,54));
        for(int i=0;i<round.answers.size();i+=columns) {
            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER);row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            for(int j=i;j<Math.min(i+columns,round.answers.size());j++) {
                final int answer=j;
                String value=round.answers.get(j);
                Button button=tile(value,"",COLORS[j%COLORS.length],rowHeight);
                button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),
                        wide?PlaylumiMagicArt.gemButton(this,(j+2)%6):PlaylumiMagicArt.pastel(this,(j+2)%6),null));
                button.setTextColor(PlaylumiMagicArt.INK);
                button.setTextSize(value.length()>=13?15:value.length()>=9?17:value.length()>=6?21:29);
                button.setPadding(dp(6),dp(4),dp(6),dp(4));button.setMaxLines(3);
                button.setMinHeight(dp(rowHeight));button.setEnabled(!answerLocked);
                if(mode==LearningGame.COLORS && game.variant()==1 && round.answerColors.get(j)!=0) {
                    int paint=round.answerColors.get(j);
                    GradientDrawable colorPlate=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{mix(paint,Color.WHITE,.18f),paint,mix(paint,0xff38204b,.13f)});
                    colorPlate.setCornerRadius(dp(25));colorPlate.setStroke(dp(2),0xffffe2a1);
                    button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),colorPlate,null));
                    button.setTextColor(Color.WHITE);
                }
                if(round.isSolved() && j==round.correctIndex){
                    button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x35ffffff),
                            wide?PlaylumiMagicArt.gemButton(this,3):PlaylumiMagicArt.pastel(this,3),null));
                    button.setTextColor(0xff174f36);
                }
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,dp(rowHeight),1f);
                int gap=centered?6:3;
                cell.setMargins(dp(gap),dp(gap),dp(gap),dp(gap));row.addView(button,cell);
                answerButtons.add(button);
                button.setOnClickListener(v->choose(answer,button));
            }
            if(centered&&!PlaylumiAnswerOrder.horizontalPair(round.answers)){
                for(int j=Math.min(i+columns,round.answers.size());j<i+columns;j++){
                    View spacer=new View(this);
                    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(rowHeight),1f);
                    p.setMargins(dp(6),dp(6),dp(6),dp(6));row.addView(spacer,p);
                }
            }
            answers.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        if(answerLocked)scheduleAutoAdvance();
    }

    private void updateQuestionPicture(LearningGame.Round round){
        if(questionPicture==null)return;
        String shown=round.visibleDisplay();
        if(game.mode()==ExtraGames.MATH && game.variant()!=4 && round.isSolved())
            shown=shown.replace("?",round.answers.get(round.correctIndex));
        int blank=round.letterBlank();
        if(round.isSolved() && blank>=0){
            int end=blank+round.answers.get(round.correctIndex).length();
            android.text.SpannableString marked=new android.text.SpannableString(shown);
            marked.setSpan(new android.text.style.ForegroundColorSpan(0xff237b49),blank,end,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            marked.setSpan(new android.text.style.UnderlineSpan(),blank,end,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            questionPicture.setText(marked);
        }else questionPicture.setText(shown);
        questionPicture.setContentDescription(shown);
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
    private boolean solvedAutoRound(){
        if(!autoRoundVisible)return false;
        switch(page){
            case PLAY:return game!=null&&game.round().isSolved();
            case MINI_PLAY:return miniSolved;
            case BUBBLE_PLAY:return bubbleSolved;
            case STAR_CATCH:return starCatchSolved;
            case MUSIC_PLAY:return melodySolved;
            default:return false;
        }
    }
    /** A single cancellable continuation; never fires on a different screen or while paused. */
    private void scheduleAutoAdvance(){
        if(!solvedAutoRound()||stage==null||moving)return;
        final long token=++autoAdvanceToken;
        final int expected=page;
        final FrameLayout screen=stage;
        final LearningGame.Round solved=expected==PLAY?game.round():null;
        screen.postDelayed(()->{
            if(token!=autoAdvanceToken||isFinishing()||isDestroyed()||activeSince<=0||
                    stage!=screen||page!=expected||moving||!solvedAutoRound()||
                    (expected==PLAY&&game.round()!=solved))return;
            autoRoundVisible=false;
            // Awards are already persisted; no interstitial badge screen or extra tap.
            if(!pendingBadges.isEmpty()&&expected!=PLAY)
                android.widget.Toast.makeText(this,"🏅 Nieuwe badge verdiend!",android.widget.Toast.LENGTH_SHORT).show();
            pendingBadges.clear();
            advancingAutomatically=true;
            try{
                switch(expected){
                    case PLAY:continueAfterAnswer();break;
                    case MINI_PLAY:afterMiniSolve();break;
                    case BUBBLE_PLAY:finishBubbleRound();break;
                    case STAR_CATCH:finishStarCatchRound();break;
                    case MUSIC_PLAY:finishMelodyRound();break;
                }
            }finally{advancingAutomatically=false;}
        },expected==PLAY?500:700);
    }
    /** Fixed completion actions, outside the scrolling game content. */
    private LinearLayout finishActions(Button again,Button back,boolean visible){
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER);bar.setPadding(dp(6),dp(4),dp(6),dp(4));
        bar.setBackground(PlaylumiMagicArt.primary(this));
        for(Button b:new Button[]{again,back}){
            b.setMinHeight(dp(56));b.setMaxLines(2);b.setTextSize(15);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1f);
            lp.setMargins(dp(3),0,dp(3),0);bar.addView(b,lp);
        }
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(70),Gravity.BOTTOM);
        int pad=dp(PlaylumiLayout.librarySidePadding(getResources().getConfiguration().screenWidthDp));
        bp.setMargins(pad,0,pad,dp(3));stage.addView(bar,bp);
        showFinishActions(bar,visible);return bar;
    }
    private void showFinishActions(LinearLayout bar,boolean visible){
        bar.setVisibility(visible?View.VISIBLE:View.GONE);
        FrameLayout.LayoutParams sp=(FrameLayout.LayoutParams)scroller.getLayoutParams();
        sp.bottomMargin=visible?dp(78):0;scroller.setLayoutParams(sp);
    }

    private void choose(int index,Button touched) {
        if(moving||!autoRoundVisible||answerLocked||game.round().isSolved())return;
        int mode=game.mode();
        sessionAttempts++;
        int oldCorrect=scores.get(mode).correct;
        boolean correct=game.round().choose(index);
        ArrayList<Integer> newBadges=new ArrayList<>(scores.answer(mode,correct));
        if(correct) {
            answerLocked=true;sessionSolved++;lifetimeCount++;
            if(game.round().letterBlank()>=0 || game.mode()==ExtraGames.MATH){
                updateQuestionPicture(game.round());
                if(questionPicture!=null)PlaylumiMotion.pop(questionPicture);
            }
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
                    landscapeScreen()?PlaylumiMagicArt.gemButton(this,3):PlaylumiMagicArt.pastel(this,3),null));
            touched.setTextColor(0xff174f36);
            for(Button b:answerButtons)b.setEnabled(false);
            feedbackText.setText(feedback);
            feedbackText.setTextColor(0xffc6ffd6);
            PlaylumiMotion.pop(touched);
            PlaylumiMotion.pop(feedbackText);
            PlaylumiDelight.sprinkle(stage,touched);
            if(sessionSolved==sessionGoal() ||
                    scores.get(mode).currentStreak%5==0)
                PlaylumiMotion.celebrate(stage,scores.get(mode).currentStreak);
            if(roundStars!=null)roundStars.showProgress(sessionSolved);
            if(progressCounter!=null)progressCounter.setText(sessionSolved+"/"+sessionGoal());
            if(magicHeaderProgress!=null)magicHeaderProgress.setText("★ "+sessionSolved+"/"+sessionGoal());
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
            if(!newBadges.isEmpty())feedbackText.setText(feedback+"  🏅");
            scheduleAutoAdvance();
            feedbackText.announceForAccessibility(feedback);
        } else {
            feedback="💛 Probeer nog eens!";
            roundHadMistake=true;
            sound.mistake();
            if(playPal!=null)playPal.react(2);
            feedbackText.setText(feedback);feedbackText.setTextColor(0xffffdf9a);
            PlaylumiDelight.wiggle(touched);
            feedbackText.announceForAccessibility(feedback);
        }
        saveScore(mode);saveLibraryQuiz();
    }
    private void advanceRegularRound() {
        if(smartDifficulty && storyStage<0 && pendingLevel>=0) {
            difficulty=pendingLevel;
            game.configure(game.mode(),game.variant(),difficulty);
            pendingLevel=-1;
        } else game.nextRound();
        roundHadMistake=false;
        saveRecent();saveLibraryQuiz();
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
            header("Avontuur klaar!","");
            characterHero(172);
            addText(PlaylumiAdventure.ICONS[world][storyStage]+"  "+
                    PlaylumiAdventure.TITLES[world][storyStage],24,0xff543c97,12,8);
            addText("Je hebt "+sessionGoal()+" uitdagingen opgelost!",17,SUB,0,10);
            if(storyNewReward)addText("🎁 +"+PlaylumiAdventure.BONUS+" extra speelsterren!",19,0xff27866b,0,12);
            Button map=tile("🗺️ Verder op avontuur!","",0xffd2f4dd,93);
            map.setOnClickListener(v->{
                storyPage=Math.min(2,adventures.progress(world)/2);
                storyStage=-1;
                move(ADVENTURE_MAP);
            });
            Button repeat=tile("🔄 Nog een keer oefenen","",0xffe7dcff,76);
            repeat.setOnClickListener(v->{
                sessionSolved=0;sessionAttempts=0;storyNewReward=false;
                game.nextRound();saveRecent();move(PLAY);
            });
            finishActions(map,repeat,true);
            return;
        }
        header("Knap gedaan!","");
        characterHero(145);
        addText("🌟 "+sessionGoal()+" vragen goed!",27,0xff4b3488,9,9);
        addText("Dat was knap! Zin in nog een ronde?",17,SUB,0,12);
        Button again=tile("🔄 Nog een ronde!","",0xffcbf1d9,92);
        PlaylumiDelight.invite(again);
        again.setOnClickListener(v->{
            sessionSolved=0;sessionAttempts=0;
            advanceRegularRound();move(PLAY);
        });
        Button home=tile("🌈 Ander spelletje","",0xffeadbff,82);
        home.setOnClickListener(v->move(libraryReturn?LIBRARY:PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:CATEGORIES));
        finishActions(again,home,true);
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
    private void personalActionGrid(Button... actions){
        int width=getResources().getConfiguration().screenWidthDp;
        int columns=width>=900?3:width>=360?2:1;
        for(int first=0;first<actions.length;first+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);
            for(int j=0;j<columns;j++){
                int i=first+j;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(4),dp(4),dp(4),dp(4));
                if(i<actions.length){actions[i].setMinHeight(dp(94));row.addView(actions[i],lp);}
                else row.addView(new View(this),lp);
            }
            addWidget(row,1,2);
        }
    }
    private LinearLayout personalPanel(){
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(10),dp(10),dp(10),dp(10));
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xfffff7e5,0xfff3e7ff,0xffe8dcff});
        bg.setCornerRadius(dp(22));bg.setStroke(dp(1),0xffc5a4e3);panel.setBackground(bg);
        return panel;
    }
    private void personalSummary(boolean rewards){
        LinearLayout panel=personalPanel();
        LinearLayout savedRoot=root;root=panel;
        try{characterHero(194);}finally{root=savedRoot;}
        panel.addView(text(rewards?"Jouw speelsterren":PlaylumiWorldStyle.AVATAR_NAMES[chosenAvatar()],23,TEXT));
        LinearLayout metrics=new LinearLayout(this);metrics.setGravity(Gravity.CENTER);
        TextView stars=text("★ "+wardrobe.stars()+"\nspeelsterren",18,0xff715021);
        TextView treasure=text("✦ "+collectionTotal()+"/18\nschatten",18,0xff5c3688);
        stars.setBackground(rounded(0xffffedc8,14));treasure.setBackground(rounded(0xffe5d6ff,14));
        for(TextView metric:new TextView[]{stars,treasure}){
            metric.setPadding(dp(4),dp(7),dp(4),dp(7));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
            lp.setMargins(dp(4),dp(5),dp(4),dp(3));metrics.addView(metric,lp);
        }
        panel.addView(metrics);addWidget(panel,4,7);
    }
    private void showAvatar() {
        header("Mijn maatje","Samen op magisch avontuur.");personalSummary(false);
        Button style=tile("🎀 Kledingkast","Kies accessoires",0xffe9dcff,92);style.setOnClickListener(v->{closetReturn=AVATAR;move(CLOSET);});
        Button choose=tile("🐾 Kies je maatje","Verander je karakter",0xffe4d4ff,92);choose.setOnClickListener(v->{personalReturn=AVATAR;move(PROFILES);});
        Button stageButton=tile("🎭 Danspodium","Laat je maatje bewegen",0xffffe8b8,92);stageButton.setOnClickListener(v->move(STAR_STAGE));
        Button home=tile("🏠 Mijn kamer","Versier jouw kamer",0xffd9ecff,92);home.setOnClickListener(v->move(ROOM));
        Button trophies=tile("🏆 Mijn badges","Jouw prestaties",0xffffeab9,92);
        trophies.setOnClickListener(v->{villageLaunch=PlaylumiVillageView.FRIEND;move(TROPHY_WORLDS);});
        Button album=tile("✦ Mijn schatten",collectionTotal()+" van 18 verzameld",0xffffe4bd,92);album.setOnClickListener(v->{collectionReturn=AVATAR;move(COLLECTIBLES);});
        personalActionGrid(style,choose,stageButton,home,trophies,album);
    }
    private void invalidateAvatars(View view){
        if(view instanceof PlaylumiAvatar)view.invalidate();
        if(view instanceof android.view.ViewGroup){
            android.view.ViewGroup group=(android.view.ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)invalidateAvatars(group.getChildAt(i));
        }
    }
    private void showCloset(){
        header("Mijn kledingkast","Maak jouw maatje helemaal uniek.");
        int width=getResources().getConfiguration().screenWidthDp;
        boolean wide=PlaylumiLayout.interactiveWide(width);
        LinearLayout outfit=new LinearLayout(this);outfit.setOrientation(wide?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);outfit.setGravity(Gravity.TOP);
        LinearLayout preview=personalPanel(),controls=personalPanel();
        if(wide){
            LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,-2,1f);pp.setMargins(dp(3),0,dp(5),0);outfit.addView(preview,pp);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1.65f);cp.setMargins(dp(5),0,dp(3),0);outfit.addView(controls,cp);
        }else{
            outfit.addView(preview,new LinearLayout.LayoutParams(-1,-2));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(7);outfit.addView(controls,cp);
        }
        addWidget(outfit,3,5);
        LinearLayout savedRoot=root;root=preview;
        try{characterHero(180);}finally{root=savedRoot;}
        final TextView balance=text("★ "+wardrobe.stars()+" speelsterren",22,0xff715021);preview.addView(balance);
        final TextView owned=text((wardrobe.unlockedItems()-PlaylumiWardrobe.SLOTS)+" van 36 accessoires vrijgespeeld",14,SUB);preview.addView(owned);
        final TextView feedback=text("Kies een accessoire. Gratis wisselen als je het al hebt!",15,SUB);
        feedback.setMinHeight(dp(44));feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);controls.addView(feedback);
        final LinearLayout items=new LinearLayout(this);items.setOrientation(LinearLayout.VERTICAL);
        final Button[] tabs=new Button[PlaylumiWardrobe.SLOTS];
        final Runnable render=()->{
            for(int i=0;i<tabs.length;i++)if(tabs[i]!=null){
                boolean selected=i==selectedSlot;
                tabs[i].setAlpha(selected?1f:.85f);tabs[i].setSelected(selected);
                tabs[i].setText(PlaylumiWardrobe.SLOT_ICONS[i]+" "+PlaylumiWardrobe.SLOT_NAMES[i]+(selected?" ✓":""));
                tabs[i].setContentDescription(PlaylumiWardrobe.SLOT_NAMES[i]+(selected?", geselecteerd":""));
            }
            renderClosetItems(items,balance,owned,feedback);
        };
        final int tabColumns=wide&&width<900?2:3;
        for(int first=0;first<PlaylumiWardrobe.SLOTS;first+=tabColumns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int i=first;i<Math.min(first+tabColumns,PlaylumiWardrobe.SLOTS);i++){
                final int slot=i;
                Button tab=tile(PlaylumiWardrobe.SLOT_ICONS[i]+" "+PlaylumiWardrobe.SLOT_NAMES[i],"",COLORS[i],60);
                tabs[i]=tab;tab.setTextSize(14);tab.setMinHeight(dp(54));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tab,lp);
                tab.setOnClickListener(v->{selectedSlot=slot;sound.tap();feedback.setText("Kies jouw favoriet.");render.run();});
            }
            controls.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        addWidgetIn(controls,items,dp(5),dp(6));
        selectedSlot=Math.max(0,Math.min(PlaylumiWardrobe.SLOTS-1,selectedSlot));render.run();
        Button done=tile("Klaar!",closetReturn==SHOP?"Terug naar je beloningen":"Terug naar je maatje",0xffd5f5df,70);
        addWidgetIn(controls,done,dp(4),dp(4));done.setOnClickListener(v->onBackPressed());
    }
    private void renderClosetItems(LinearLayout items,TextView balance,TextView owned,TextView feedback){
        items.removeAllViews();
        final int slot=selectedSlot,columns=PlaylumiLayout.closetItemColumns(getResources().getConfiguration().screenWidthDp);
        final Button[] choices=new Button[PlaylumiWardrobe.OPTIONS];
        final Runnable refresh=()->{
            balance.setText("★ "+wardrobe.stars()+" speelsterren");
            owned.setText((wardrobe.unlockedItems()-PlaylumiWardrobe.SLOTS)+" van 36 accessoires vrijgespeeld");
            for(int i=0;i<choices.length;i++){
                Button button=choices[i];if(button==null)continue;
                String label=PlaylumiWardrobe.ITEM_ICONS[slot][i]+" "+PlaylumiWardrobe.ITEM_NAMES[slot][i];
                boolean equipped=wardrobe.equipped(slot)==i,unlocked=wardrobe.owned(slot,i);
                String detail=equipped?"✓ In gebruik":unlocked?"In bezit":"★ "+wardrobe.price(slot,i)+" sterren";
                button.setText(PlaylumiType.tileText(label,detail,false));
                button.setContentDescription(label+" "+detail);
                button.setSelected(equipped);button.setAlpha(equipped?1f:unlocked?.95f:.84f);
            }
            invalidateAvatars(root);
        };
        for(int first=0;first<PlaylumiWardrobe.OPTIONS;first+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);
            for(int j=0;j<columns;j++){
                int i=first+j;
                if(i>=PlaylumiWardrobe.OPTIONS){row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1f));continue;}
                final int item=i;
                Button b=tile(PlaylumiWardrobe.ITEM_NAMES[slot][item],"",COLORS[slot],86);
                b.setMinHeight(dp(88));b.setTextSize(16);choices[item]=b;
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(b,lp);
                final Runnable equip=()->{
                    if(page!=CLOSET||selectedSlot!=slot)return;
                    int result=wardrobe.buyOrEquip(slot,item);
                    if(result==PlaylumiWardrobe.NOT_ENOUGH){feedback.setText("Nog niet genoeg speelsterren. Speel rustig verder!");return;}
                    saveWardrobe();refresh.run();
                    feedback.setText(result==PlaylumiWardrobe.PURCHASED?"✨ Nieuw accessoire vrijgespeeld!":"✨ Wat staat dat mooi!");
                    sound.equip();
                    if(character!=null){character.wave();PlaylumiDelight.sprinkle(stage,character);}
                };
                b.setOnClickListener(v->{
                    if(wardrobe.owned(slot,item)){equip.run();return;}
                    int price=wardrobe.price(slot,item);
                    if(wardrobe.stars()<price){feedback.setText("Je hebt nog "+(price-wardrobe.stars())+" speelsterren nodig.");return;}
                    new android.app.AlertDialog.Builder(this).setTitle(PlaylumiWardrobe.ITEM_NAMES[slot][item])
                            .setMessage("Vrijspelen voor "+price+" speelsterren?").setNegativeButton("Nog niet",null)
                            .setPositiveButton("Vrijspelen",(d,which)->equip.run()).show();
                });
            }
            items.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        refresh.run();
    }
    private void showCollectibles(){
        header("Mijn schatten","Ontdek de zes werelden en verzamel magische schatten.");
        addText("✦ "+collectionTotal()+" van 18 schatten",23,0xff715021,6,5);
        addText("Verdien ze met goede antwoorden en voltooide opdrachten. Geen sterren uitgeven!",15,SUB,0,8);
        int columns=getResources().getConfiguration().screenWidthDp>=600?2:1;
        for(int first=0;first<6;first+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);
            for(int i=first;i<Math.min(first+columns,6);i++){
                final int w=i,progress=collectionProgress(w);
                LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);
                panel.setPadding(dp(9),dp(7),dp(9),dp(8));
                GradientDrawable bg=rounded(0xfffff6ed,18);bg.setStroke(dp(2),0xffc5a4e3);panel.setBackground(bg);
                panel.addView(PlaylumiMagicArt.hero(this,w),new LinearLayout.LayoutParams(-1,dp(86)));
                panel.addView(text(PlaylumiWorldStyle.NAMES[w],19,TEXT));
                panel.addView(text(PlaylumiCollection.unlocked(progress)+" van 3 schatten",14,0xff7651a4));
                LinearLayout treasures=new LinearLayout(this);treasures.setGravity(Gravity.CENTER);
                for(int n=0;n<3;n++){
                    boolean unlocked=progress>=PlaylumiCollection.GOALS[n];
                    LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);
                    tile.setPadding(dp(3),dp(5),dp(3),dp(5));
                    tile.setBackground(rounded(unlocked?0xffffe6b2:0xffeee7f5,12));
                    tile.addView(text(unlocked?PlaylumiCollection.ICONS[w][n]:"◇",29,unlocked?0xff9c651c:0xffb4a5c3));
                    tile.addView(text(unlocked?PlaylumiCollection.NAMES[w][n]:progress+"/"+PlaylumiCollection.GOALS[n],12,TEXT));
                    tile.setContentDescription(PlaylumiCollection.NAMES[w][n]+(unlocked?", verzameld":", nog "+(PlaylumiCollection.GOALS[n]-progress)+" goede antwoorden"));
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);treasures.addView(tile,lp);
                    if(unlocked)tile.setOnClickListener(v->{sound.collect();PlaylumiDelight.sprinkle(stage,v);});
                }
                panel.addView(treasures,new LinearLayout.LayoutParams(-1,-2));
                Button play=tile("Speel in deze wereld","",COLORS[w],58);panel.addView(play,new LinearLayout.LayoutParams(-1,-2));
                play.setOnClickListener(v->openLibrary(w));
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1f);cp.setMargins(dp(4),dp(5),dp(4),dp(5));row.addView(panel,cp);
            }
            addWidget(row,0,0);
        }
        Button badges=tile("🏆 Mijn badges","Bekijk jouw bestaande prestaties",0xffffe8b8,78);
        addTile(badges,5);badges.setOnClickListener(v->{villageLaunch=PlaylumiVillageView.COLLECTION;move(TROPHY_WORLDS);});
    }
    private void showShop() {
        header("Mijn beloningen","Speel en verzamel sterren!");personalSummary(true);
        Button closet=tile("🎀 Kledingkast","Kleed je maatje aan",0xffe8dcff,92);closet.setOnClickListener(v->{closetReturn=SHOP;move(CLOSET);});
        Button treasures=tile("✦ Mijn schatten",collectionTotal()+" van 18 verzameld",0xffffe4bd,92);treasures.setOnClickListener(v->{collectionReturn=SHOP;move(COLLECTIBLES);});
        Button roomButton=tile("🏠 Mijn kamer","Versier jouw kamer",0xffd9ecff,92);roomButton.setOnClickListener(v->move(ROOM));
        Button badges=tile("🏆 Mijn badges","Jouw prestaties",0xffffe8b8,92);
        badges.setOnClickListener(v->{villageLaunch=PlaylumiVillageView.COLLECTION;move(TROPHY_WORLDS);});
        personalActionGrid(closet,treasures,roomButton,badges);
        Button done=tile("Verder spelen","Terug naar het dorp",0xffd9f2d8,82);
        addTile(done,6);done.setOnClickListener(v->move(HOME));
    }

    private int miniCategory() {
        return miniKind==PlaylumiMiniChallenge.DRAG?LearningGame.SHAPES:
                miniKind==PlaylumiMiniChallenge.BALLOONS?LearningGame.COUNT:LearningGame.PATTERNS;
    }
    private void showMiniMenu() {
        header("🎪 Het speelplein","Tik een spelletje aan.");
        addIllustratedPortal(PlaylumiVillageView.PLAYGROUND,"Het Speelplein",164);
        String[] names={"Sleep de vormen","Tel de ballonnen","Dierenmemory"};
        String[] notes={"Sleep naar het huisje","Leer hoeveelheden tellen",
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
            melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
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
        String miniTitle=miniKind==PlaylumiMiniChallenge.MEMORY?"Dierenmemory":
                miniKind==PlaylumiMiniChallenge.DRAG?"Vormen slepen":PlaylumiMiniChallenge.NAMES[miniKind];
        header(miniRound>=20?"Knap gedaan!":miniTitle,miniRound>=20?"":Math.min(miniRound+1,20)+"/20");
        addIllustratedPortal(worldTheme(),PlaylumiWorldStyle.NAMES[worldTheme()],116);
        if(miniRound>=20) {
            characterHero(150);
            addText("🎉 Alle uitdagingen gespeeld!",25,0xff4e3889,13,9);
            Button again=tile("🔄 Nog eens spelen!","",0xffd4f5e0,96);
            again.setOnClickListener(v->{
                miniRound=0;miniSolved=false;
                mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
                move(MINI_PLAY);
            });
            Button back=tile("🎪 Andere minigame","",0xffe8dfff,86);
            back.setOnClickListener(v->move(libraryReturn?LIBRARY:PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,back,true);
            return;
        }
        autoRoundVisible=true;
        addWidget(magicRibbon(miniKind==PlaylumiMiniChallenge.MEMORY?"Vind de gelijke dieren":
                miniKind==PlaylumiMiniChallenge.DRAG?"Sleep naar de vormpoort":mini.question,19),0,5);
        PlaylumiMiniBoard board=new PlaylumiMiniBoard(this,mini,new PlaylumiMiniBoard.Listener() {
            @Override public void onSolved() {
                if(page!=MINI_PLAY||moving||!autoRoundVisible||miniSolved)return;
                miniSolved=true;
                if(playPal!=null)playPal.dance();
                recordMiniAnswer();
                scheduleAutoAdvance();
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
        if(miniSolved && miniKind==PlaylumiMiniChallenge.DRAG)board.restoreSolvedShape();
        if(miniSolved)scheduleAutoAdvance();
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
        if(!miniSolved)return;
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
        PlaylumiRoomView view=new PlaylumiRoomView(this,room,chosenAvatar());
        int available=PlaylumiLayout.sceneHeight(height,landscapeScreen(),
                getResources().getConfiguration().screenHeightDp);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(available));
        p.setMargins(dp(3),dp(3),dp(3),dp(8));
        root.addView(view,p);
    }
    private void showRoom() {
        header("🏠 Mijn eigen kamer","Welkom thuis!");
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
        Button friend=tile("Terug naar je maatje","",0xffe8dcff,77);
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
        addWidget(storyCard,0,8);
        addText("Wat kies jij?",16,SUB,4,4);
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
                wardrobe.earnCreativeBonus();saveWardrobe();recordWorldProgress(5,1);
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
        addText("Spraak is uitgeschakeld. Geluidseffecten en muziek blijven beschikbaar.",14,SUB,2,7);
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
        header("Mijn maatje","Kies wie je meeneemt op avontuur.");
        int current=chosenAvatar();
        PlaylumiAvatar preview=new PlaylumiAvatar(this,current,wardrobe);
        LinearLayout.LayoutParams hero=new LinearLayout.LayoutParams(dp(162),dp(162));
        hero.gravity=Gravity.CENTER;hero.setMargins(0,dp(4),0,dp(4));
        root.addView(preview,hero);
        addText(PlaylumiWorldStyle.AVATAR_NAMES[current],24,0xff57308c,1,5);
        addText("Je sterren, badges en voortgang blijven bij jou.",15,SUB,0,7);
        int columns=landscapeScreen()?3:2;
        for(int i=PlaylumiWorldStyle.FIRST_AVATAR;i<PlaylumiWorldStyle.AVATAR_NAMES.length;i+=columns) {
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int j=i;j<Math.min(i+columns,PlaylumiWorldStyle.AVATAR_NAMES.length);j++) {
                final int choice=j;
                LinearLayout card=new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);
                card.setPadding(dp(8),dp(8),dp(8),dp(8));
                GradientDrawable cardBg=rounded(choice==current?0xffd7f2d9:0xfff2e7ff,19);
                cardBg.setStroke(dp(2),choice==current?0xff46845a:0xff8e66bd);
                card.setBackground(cardBg);
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);
                cp.setMargins(dp(4),dp(4),dp(4),dp(4));
                row.addView(card,cp);
                PlaylumiAvatar icon=new PlaylumiAvatar(this,choice,wardrobe);
                card.addView(icon,new LinearLayout.LayoutParams(dp(92),dp(92)));
                TextView name=text(PlaylumiWorldStyle.AVATAR_NAMES[choice],18,TEXT);
                card.addView(name,new LinearLayout.LayoutParams(-1,-2));
                TextView hint=text(choice==current?"Gekozen":"Kies",13,SUB);
                card.addView(hint,new LinearLayout.LayoutParams(-1,-2));
                card.setContentDescription(PlaylumiWorldStyle.AVATAR_NAMES[choice]+(choice==current?", gekozen":", kies maatje"));
                card.setFocusable(true);card.setClickable(true);
                card.setOnClickListener(v->{
                    prefs().edit().putInt("chosen_avatar",choice).apply();
                    if(sound!=null)sound.tap();
                    showPage();
                });
            }
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        Button done=tile("Klaar!",personalReturn==AVATAR?"Terug naar je maatje":"Terug naar het magische dorp",0xffd9f2d8,82);
        addTile(done,8);done.setOnClickListener(v->onBackPressed());
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
        header("Sterren vangen",Math.min(starCatchRound+1,PlaylumiStarCatch.ROUNDS)+"/"+PlaylumiStarCatch.ROUNDS);
        addIllustratedPortal(PlaylumiVillageView.PLAYGROUND,"Vang de sterren",98);
        if(starCatchRound>=PlaylumiStarCatch.ROUNDS){
            characterHero(140);
            addText("🏆 Alle sterren gevangen!",24,0xff59438f,8,12);
            Button again=tile("🌟 Speel nog eens","",0xffd4f4e2,88);
            again.setOnClickListener(v->{starCatchRound=0;starCatchSolved=false;starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());move(STAR_CATCH);});
            Button back=tile("🎪 Andere spelletjes","",0xffe8dfff,80);
            back.setOnClickListener(v->move(libraryReturn?LIBRARY:PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,back,true);return;
        }
        autoRoundVisible=true;
        if(starCatch==null)starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
        final PlaylumiStarCatch round=starCatch;
        addWidget(magicRibbon("Vang "+round.target()+" gouden sterren!",19),0,5);
        starCatchProgress=text("⭐ "+round.collected()+" / "+round.target(),21,PlaylumiMagicArt.INK);
        starCatchProgress.setBackground(PlaylumiMagicArt.creamPlate(this));
        starCatchProgress.setPadding(dp(8),dp(8),dp(8),dp(8));
        addWidget(starCatchProgress,2,6);
        PlaylumiStarCatchBoard board=new PlaylumiStarCatchBoard(this,round,new PlaylumiStarCatchBoard.Listener(){
            @Override public void onStar(int count){starCatchProgress.setText("⭐ "+count+" / "+round.target());sound.pop();}
            @Override public void onMistake(){sound.mistake();}
            @Override public void onFinished(){
                if(page!=STAR_CATCH||moving||!autoRoundVisible||starCatchSolved)return;
                starCatchSolved=true;
                sound.good();
                if(playPal!=null)playPal.dance();
                PlaylumiMotion.celebrate(stage,starCatchRound+1);
                scheduleAutoAdvance();
            }
        });
        int h=PlaylumiLayout.sceneHeight(280,landscapeScreen(),getResources().getConfiguration().screenHeightDp);
        root.addView(board,new LinearLayout.LayoutParams(-1,dp(h)));
        if(starCatchSolved)scheduleAutoAdvance();
    }
    private void finishStarCatchRound(){
        if(!starCatchSolved)return;
        starCatchRound++;starCatchSolved=false;
        starCatch=starCatchRound>=PlaylumiStarCatch.ROUNDS?null:PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
        move(STAR_CATCH);
    }

    /** New sensory counting minigame, with one clear goal at a time. */
    private void showBubblePlay() {
        if(bubbles==null)bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
        header("🫧 Het Bellenfeest","Ronde "+Math.min(bubbleRound+1,20)+" van 20");
        addIllustratedPortal(PlaylumiVillageView.PLAYGROUND,"Bellenfeest",96);
        if(bubbleRound>=20) {
            characterHero(170);
            addText("🎉 Wat een bubbelkampioen!",25,0xff63458c,10,10);
            Button again=tile("🫧 Nog meer bellen!","",0xffd3f4df,99);
            again.setOnClickListener(v->{
                bubbleRound=0;bubbleSolved=false;
                bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
                move(BUBBLE_PLAY);
            });
            Button back=tile("🎪 Een ander spel","",0xffe8deff,89);
            back.setOnClickListener(v->move(libraryReturn?LIBRARY:PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,back,true);
            return;
        }
        autoRoundVisible=true;
        addText("Prik "+bubbles.target+" zeepbellen!",20,PlaylumiWorldStyle.deep(worldTheme()),3,5);
        PlaylumiBubbleBoard board=new PlaylumiBubbleBoard(this,bubbles,
                new PlaylumiBubbleBoard.Listener() {
            @Override public void onPop() {sound.pop();}
            @Override public void onSolved() {
                if(page!=BUBBLE_PLAY||moving||!autoRoundVisible||bubbleSolved)return;
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
                scheduleAutoAdvance();
            }
        });
        LinearLayout.LayoutParams boardp=new LinearLayout.LayoutParams(-1,-2);
        boardp.setMargins(dp(2),dp(6),dp(2),dp(7));
        root.addView(board,boardp);
        if(bubbleSolved)scheduleAutoAdvance();
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
        if(melodyPreviewActive)return;
        melodyPreviewActive=true;
        melodyStatus.setText("Luister goed ...");
        for(int i=0;i<melody.length();i++) {
            final int note=melody.noteAt(i);
            final int beat=i;
            active.postDelayed(()->{
                if(stage!=active || page!=MUSIC_PLAY)return;
                sound.note(note);
                if(melodyPads[note]!=null)PlaylumiMotion.pop(melodyPads[note]);
                melodyStatus.setText("Luister goed  "+(beat+1)+"/"+melody.length());
            },i*470L);
        }
        active.postDelayed(()->{
            if(stage==active && page==MUSIC_PLAY){
                melodyPreviewActive=false;
                melodyStatus.setText("Nu jij! Tik de dierenvriendjes aan.");
            }
        },melody.length()*470L+220L);
    }
    private void showMusicPlay() {
        if(melody==null)melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
        header(melodyVariant==2?"Geheugenconcert":melodyVariant==1?"Lange melodie":"Muziekstudio",Math.min(melodyRound,PlaylumiMelody.ROUNDS)+"/"+PlaylumiMelody.ROUNDS);
        addIllustratedPortal(PlaylumiVillageView.MUSIC,"Muziekstudio",116);
        addMagicProgress(melodyRound,PlaylumiMelody.ROUNDS,false);
        if(melodyRound>=PlaylumiMelody.ROUNDS) {
            characterHero(170);
            addText("🏆 Wat een muzikale ster!",26,0xff61418f,10,14);
            Button again=tile("🎶 Nog eens muziek maken","",0xffd4f3dd,96);
            again.setOnClickListener(v->{
                melodyRound=0;melodySolved=false;
                melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
                move(MUSIC_PLAY);
            });
            Button other=tile("🎪 Ander spelletje","",0xffe9deff,80);
            other.setOnClickListener(v->move(libraryReturn?LIBRARY:PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,other,true);
            return;
        }
        autoRoundVisible=true;
        // Only the actual melody preview and playable animal buttons are shown.
        melodyStatus=magicRibbon(sound.isEnabled()?
                "Luister en speel het deuntje na.":"Bekijk de plaatjes en tik ze na.",19);
        addWidget(melodyStatus,0,5);
        Button replay=tile(sound.isEnabled()?"▶  Speel de melodie af":"Bekijk de melodie",
                "",0xffd9f3ff,66);
        addTile(replay,3);
        replay.setOnClickListener(v->{
            if(sound.isEnabled())playMelodyPreview();
            else melodyStatus.setText(melodyIcons());
        });
        Button reveal=tile("💡 Laat de volgorde zien","",0xffffedc3,61);
        addTile(reveal,3);
        reveal.setOnClickListener(v->{
            melodyStatus.setText(melodyIcons());
            sound.sparkle();
        });
        int padHeight=PlaylumiLayout.magicAnimalHeight(getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().screenHeightDp);
        for(int i=0;i<4;i+=2) {
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for(int j=i;j<i+2;j++) {
                final int note=j;
                Button pad=tile(PlaylumiMelody.NAMES[j],"",COLORS[j],padHeight);
                pad.setText(PlaylumiMelody.NAMES[j]);pad.setTextColor(PlaylumiMagicArt.INK);
                pad.setTextSize(19);pad.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                pad.setPadding(dp(6),0,dp(6),dp(16));
                pad.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                        PlaylumiMagicArt.animal(this,j),null));
                pad.setContentDescription(PlaylumiMelody.NAMES[j]);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(padHeight),1f);
                p.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(pad,p);
                melodyPads[j]=pad;pad.setOnClickListener(v->tapMelody(note,pad));
            }
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        if(melodySolved)melodyStatus.setText("🎉 Geweldig! Je kon de melodie spelen!");
        if(melodySolved)scheduleAutoAdvance();
    }
    private void tapMelody(int note,Button pressed) {
        if(page!=MUSIC_PLAY||moving||!autoRoundVisible||melodySolved||melody==null||melodyPreviewActive)return;
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
            if(roundStars!=null)roundStars.showProgress(melodyRound+1);
            if(progressCounter!=null)progressCounter.setText((melodyRound+1)+"/"+PlaylumiMelody.ROUNDS);
            if(magicHeaderProgress!=null)magicHeaderProgress.setText("★ "+(melodyRound+1)+"/"+PlaylumiMelody.ROUNDS);
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
            PlaylumiDelight.sprinkle(stage,pressed);
            scheduleAutoAdvance();
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
        melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
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
