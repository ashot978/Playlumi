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
            WORLD_PICKER=40, HOME_OPTIONS=41, STAR_CATCH=42, VILLAGE_MENU=43, LIBRARY=44, INTERACTIVE=45, COLLECTIBLES=46, ACHIEVEMENTS=47, DAILY=48, CITY=49, IDEAS=50, EXPEDITION=51;
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
    private int achievementFilter=0;
    private PlaylumiMiniChallenge mini;
    private PlaylumiAvatar playPal;
    private int miniKind,miniRound,rewardReturn=PLAY;
    private boolean miniSolved;
    private PlaylumiBubbles bubbles;
    private PlaylumiMelody melody;
    private int melodyRound;
    private int melodyVariant;
    private boolean melodySolved;
    private TextView melodyStatus,melodySteps,magicHeaderProgress;
    private final Button[] melodyPads=new Button[4];
    private boolean melodyPreviewActive;
    private long melodyPreviewToken;
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
    // This Activity keeps one storage identity until it is recreated for another player.
    private int activeProfile;
    // Tracks which village building started a screen, so Back returns logically.
    private int villageLaunch=-1;
    private int libraryFilter=PlaylumiGameCatalog.OVERVIEW;
    private static int availableLibraryFilter(int filter){
        return PlaylumiWorldStyle.valid(filter)||filter==PlaylumiGameCatalog.OVERVIEW?filter:PlaylumiGameCatalog.OVERVIEW;
    }
    private TextView quizNext;
    private LinearLayout quizFeedbackRow;
    private boolean finalFeedbackPending;
    private PlaylumiPatternView questionPattern;
    private PlaylumiSkillBoard skillBoard;
    private int libraryScrollY;
    private int personalReturn=HOME,collectionReturn=HOME,closetReturn=AVATAR,achievementReturn=HOME;
    private int dailyReturn=HOME,catalogReturn=LIBRARY,friendReturn=AVATAR,ideasReturn=LIBRARY;
    private int expeditionReturn=LIBRARY,expeditionChapter=-1;
    private long renderedCalendarDay=Long.MIN_VALUE;
    private final android.os.Handler calendarHandler=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable calendarRefresh=new Runnable(){
        @Override public void run(){
            if(activeSince<=0)return;
            refreshCalendarPage();
            calendarHandler.postDelayed(this,60000L);
        }
    };
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
    private final Button[] drawingColors=new Button[PlaylumiDrawingView.PALETTE.length];
    private final Button[] drawingWidths=new Button[3];
    private Button drawingPencil,drawingEraser,drawingUndo,drawingRedo,drawingClear;
    private int drawingColor,drawingWidth;
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
        final boolean changedProfile=state!=null && state.getInt("activeProfile",activeProfile)!=activeProfile;
        if(changedProfile)
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
        if(!PlaylumiProfiles.hasChosenAge(age))age=-1;
        lifetimeCount=prefs.getInt("completed_all",0);
        int mode=prefs.getInt("game_mode",0);
        int variant=prefs.getInt("game_variant",0);
        difficulty=Math.max(0,age);
        world=Math.max(0,Math.min(2,mode/8));
        page=age<0?AGE:HOME;
        if(state!=null) {
            int restoredAge=state.getInt("age",age);
            if(PlaylumiProfiles.hasChosenAge(restoredAge))age=restoredAge;
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
            finalFeedbackPending=state.getBoolean("finalFeedbackPending",false);
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
            // A recreated Activity always requires the parent gate again.
            parentAllowed=false;
            villageLaunch=state.getInt("villageLaunch",-1);
            libraryFilter=availableLibraryFilter(state.getInt("libraryFilter",PlaylumiGameCatalog.OVERVIEW));
            libraryScrollY=Math.max(0,state.getInt("libraryScrollY",0));
            achievementReturn=state.getInt("achievementReturn",HOME);
            dailyReturn=state.getInt("dailyReturn",HOME);
            catalogReturn=state.getInt("catalogReturn",LIBRARY);
            catalogReturn=catalogReturnPage();
            expeditionReturn=state.getInt("expeditionReturn",LIBRARY);
            expeditionChapter=state.getInt("expeditionChapter",-1);
            ideasReturn=state.getInt("ideasReturn",LIBRARY)==CITY?CITY:LIBRARY;
            friendReturn=state.getInt("friendReturn",AVATAR)==CITY?CITY:AVATAR;
            achievementFilter=Math.max(0,Math.min(2,state.getInt("achievementFilter",0)));
            personalReturn=state.getInt("personalReturn",HOME);collectionReturn=state.getInt("collectionReturn",HOME);closetReturn=state.getInt("closetReturn",AVATAR);
            libraryReturn=state.getBoolean("libraryReturn",false);
            if(state.containsKey("interactiveKind"))try{
                interactive=new PlaylumiInteractive(state.getInt("interactiveKind"),state.getInt("interactiveLevel"),state.getLong("interactiveSeed"),state.getInt("interactiveProgress"),state.getInt("interactiveVersion",PlaylumiInteractive.LEGACY),state.getInt("interactiveMask",0));
            }catch(IllegalArgumentException ignored){interactive=null;}
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
                Math.max(0,Math.min(LearningGame.maxVariant(mode),variant)), Math.max(0,Math.min(2,difficulty)));
        final int restoredGamePage=page==BREAK_SCREEN?breakReturn:page;
        if(state!=null && restoredGamePage==PLAY && state.containsKey("roundDisplay")){
            try{
                if(!PlaylumiLetterChallenges.legacy(mode,variant,state.getString("roundDisplay"))&&
                        !PlaylumiWeatherPractice.legacy(mode,variant,state.getString("roundPrompt"))&&
                        !(mode==19&&variant==4&&state.getString("roundDisplay","").contains("Je steekt over bij groen licht"))){
                    game.restoreRound(state.getString("roundPrompt"),state.getString("roundDisplay"),
                            state.getInt("roundColor"),state.getStringArrayList("roundAnswers"),
                            state.getIntegerArrayList("roundColors"),state.getInt("roundCorrect"),
                            state.getInt("roundFingers"),state.getBoolean("roundSolved"),state.getInt("roundRejected",0));
                    feedback=game.round().isSolved()?state.getString("roundFeedback",""):"";
                }else{
                    // Keep earned points and completed round count; replace only the obsolete question.
                    roundHadMistake=false;feedback="";
                }
                ArrayList<Integer> savedBadges=state.getIntegerArrayList("roundBadges");
                if(savedBadges!=null)pendingBadges.addAll(savedBadges);
            }catch(IllegalArgumentException ignored){roundHadMistake=false;feedback="";}
        }
        if(restoredGamePage==MINI_PLAY){
            miniSolved=state!=null&&state.getBoolean("miniSolved",false);
            if(state!=null&&state.containsKey("miniChoices"))try{
                mini=PlaylumiMiniChallenge.restore(miniKind,state.getInt("miniLevel"),state.getString("miniTarget"),
                        state.getStringArrayList("miniChoices"),state.getInt("miniCorrect"),
                        state.getInt("miniMatched"),state.getInt("miniOpen",-1));
                if(miniKind==PlaylumiMiniChallenge.MEMORY)miniSolved=mini.solved();
            }catch(IllegalArgumentException ignored){}
            if(mini==null&&state!=null && miniKind==PlaylumiMiniChallenge.DRAG && state.containsKey("dragTarget")){
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
        if(restoredGamePage==MUSIC_PLAY && state!=null && state.getIntArray("melodyNotes")!=null) {
            try {
                melody=PlaylumiMelody.restore(state.getIntArray("melodyNotes"),
                        state.getInt("melodyProgress",0));
            } catch(IllegalArgumentException ignored) {}
        }
        if(restoredGamePage==MUSIC_PLAY){
            if(melody==null)melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
            melodySolved=melody.completed();
        }
        if(restoredGamePage==STAR_CATCH) {
            starCatchRound=state==null?0:Math.max(0,Math.min(PlaylumiStarCatch.ROUNDS,state.getInt("starCatchRound",0)));
            if(starCatchRound<PlaylumiStarCatch.ROUNDS){
                if(state!=null && state.getInt("starCatchTotal",0)>0)try {
                    starCatch=PlaylumiStarCatch.restore(state.getInt("starCatchTarget"),state.getInt("starCatchTotal"),state.getInt("starCatchTypes",0),state.getInt("starCatchMask"));
                } catch(IllegalArgumentException ignored) {}
                if(starCatch==null)starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
                starCatchSolved=starCatch.finished();
            }
        }
        if(restoredGamePage==BUBBLE_PLAY) {
            if(state!=null && state.getInt("bubbleTotal",0)>0) {
                try {
                    bubbles=PlaylumiBubbles.restore(Math.max(0,age),state.getInt("bubbleTarget",0),
                            state.getInt("bubbleTotal",0),state.getInt("bubbleMask",0));
                } catch(IllegalArgumentException ignored) {}
            }
            if(bubbles==null)bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
            bubbleSolved=bubbles.solved();
        }
        // Debug-only test entrance: isolate real library/quiz interactions from
        // emulator launcher and illustrated village navigation failures.
        // Neither entrance is available in a non-debuggable production release.
        if(state==null&&!changedProfile&&(getApplicationInfo().flags &
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0 && getIntent()!=null) {
            int ciWorld=getIntent().getIntExtra("playlumi_ci_library",-1);
            if(getIntent().getBooleanExtra("playlumi_ci_home",false)||PlaylumiWorldStyle.valid(ciWorld)){
                age=Math.max(0,Math.min(2,getIntent().getIntExtra("playlumi_ci_age",Math.max(0,age))));
                if(PlaylumiWorldStyle.valid(ciWorld)){
                    page=LIBRARY;libraryFilter=ciWorld;libraryReturn=false;
                    libraryScrollY=0;storyStage=-1;villageLaunch=ciWorld;
                }else page=HOME;
            }
        }
        showPage();
    }
    @Override protected void onDestroy() {
        autoAdvanceToken++;melodyPreviewToken++;
        calendarHandler.removeCallbacks(calendarRefresh);
        if(sound!=null)sound.close();
        super.onDestroy();
    }
    @Override protected void onResume() {
        super.onResume();
        activeSince=SystemClock.elapsedRealtime();
        if(!parentAllowed&&protectedParentPage())showPage();
        refreshCalendarPage();
        calendarHandler.removeCallbacks(calendarRefresh);
        calendarHandler.postDelayed(calendarRefresh,60000L);
        scheduleAutoAdvance();
    }
    private void refreshCalendarPage(){
        if(renderedCalendarDay!=PlaylumiPlayDays.today()&&
                (page==HOME||page==DAILY||page==CITY||page==AVATAR))showPage();
    }
    @Override protected void onPause() {
        autoAdvanceToken++;melodyPreviewToken++;
        parentAllowed=false;
        calendarHandler.removeCallbacks(calendarRefresh);
        if(melodyPreviewActive){melodyPreviewActive=false;if(melodyStatus!=null)melodyStatus.setText("Speel de melodie opnieuw af wanneer je wilt.");}
        int savedPage=page==BREAK_SCREEN?breakReturn:page;
        if(savedPage==PLAY)saveLibraryQuiz();
        if(savedPage==INTERACTIVE)saveLibraryInteractive();
        saveLibrarySpecial(savedPage);
        if(savedPage==DRAW && drawing!=null)drawing.save();
        saveUsage();
        activeSince=0;
        super.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        final int savedPage=page==BREAK_SCREEN?breakReturn:page;
        if(savedPage==DRAW && drawing!=null)drawing.save();
        out.putInt("activeProfile",activeProfile);
        out.putInt("dailyReturn",dailyReturn);out.putInt("catalogReturn",catalogReturn);out.putInt("friendReturn",friendReturn);out.putInt("ideasReturn",ideasReturn);
        out.putInt("expeditionReturn",expeditionReturn);out.putInt("expeditionChapter",expeditionChapter);
        out.putInt("age",age);out.putInt("mode",game.mode());out.putInt("variant",game.variant());
        out.putInt("difficulty",difficulty);out.putInt("world",world);
        out.putInt("categoryPage",categoryPage);out.putInt("variantPage",variantPage);
        out.putInt("badgePage",badgePage);out.putInt("sessionSolved",sessionSolved);
        out.putInt("sessionAttempts",sessionAttempts);out.putInt("page",page);
        if(savedPage==PLAY && game!=null){
            LearningGame.Round round=game.round();
            out.putString("roundPrompt",round.prompt);out.putString("roundDisplay",round.display);
            out.putInt("roundColor",round.displayColor);out.putInt("roundCorrect",round.correctIndex);
            out.putInt("roundFingers",round.fingerCount);out.putBoolean("roundSolved",round.isSolved());
            out.putStringArrayList("roundAnswers",new ArrayList<>(round.answers));
            out.putIntegerArrayList("roundColors",new ArrayList<>(round.answerColors));
            out.putString("roundFeedback",feedback);out.putInt("roundRejected",round.rejectedMask());
            out.putIntegerArrayList("roundBadges",new ArrayList<>(pendingBadges));
        }
        if(savedPage==MINI_PLAY&&mini!=null){
            out.putInt("miniLevel",mini.level);out.putString("miniTarget",mini.target);
            out.putStringArrayList("miniChoices",new ArrayList<>(mini.choices));out.putInt("miniCorrect",mini.correctIndex);
            out.putInt("miniMatched",mini.matchedMask());out.putInt("miniOpen",mini.firstOpen());
        }
        if(savedPage==MINI_PLAY && mini!=null && miniKind==PlaylumiMiniChallenge.DRAG){
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
        out.putBoolean("roundHadMistake",roundHadMistake);out.putBoolean("finalFeedbackPending",finalFeedbackPending);
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
        out.putInt("villageLaunch",villageLaunch);
        out.putInt("libraryFilter",libraryFilter);out.putBoolean("libraryReturn",libraryReturn);
        out.putInt("libraryScrollY",page==LIBRARY&&scroller!=null?scroller.getScrollY():libraryScrollY);
        out.putInt("achievementReturn",achievementReturn);out.putInt("achievementFilter",achievementFilter);
        out.putInt("personalReturn",personalReturn);out.putInt("collectionReturn",collectionReturn);out.putInt("closetReturn",closetReturn);
        if(interactive!=null){out.putInt("interactiveKind",interactive.kind);out.putInt("interactiveLevel",interactive.level);out.putLong("interactiveSeed",interactive.seed);out.putInt("interactiveProgress",interactive.progress());out.putInt("interactiveVersion",interactive.version);out.putInt("interactiveMask",interactive.mask());}
        out.putBoolean("quickCategoryPlay",quickCategoryPlay);
        out.putInt("breakReturn",breakReturn);
        out.putInt("roomPage",roomPage);
        out.putInt("roomItemPage",roomItemPage);
        out.putInt("roomSlot",roomSlot);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if(page==AGE) {
            if(PlaylumiProfiles.hasChosenAge(prefs().getInt("chosen_age",-1)))move(HOME_OPTIONS);
            else super.onBackPressed();
            return;
        }
        if(page==HOME) {super.onBackPressed();return;}
        if(page==DAILY){move(dailyReturn==CITY?CITY:dailyReturn==AVATAR?AVATAR:dailyReturn==LIBRARY?LIBRARY:HOME);return;}
        if(page==CITY){move(HOME);return;}
        if(page==EXPEDITION){
            libraryReturn=false;
            if(expeditionReturn==LIBRARY){libraryFilter=PlaylumiGameCatalog.OVERVIEW;libraryScrollY=0;}
            move(expeditionReturn==CITY?CITY:expeditionReturn==HOME?HOME:LIBRARY);return;
        }
        if(page==IDEAS){move(ideasReturn==CITY?CITY:LIBRARY);return;}
        if(page==INTERACTIVE){move(catalogReturnPage());return;}
        if(page==LIBRARY){
            if(libraryFilter!=PlaylumiGameCatalog.OVERVIEW){libraryFilter(PlaylumiGameCatalog.OVERVIEW);}
            else move(HOME);
            return;
        }
        if(libraryReturn&&storyStage<0&&(page==PLAY||page==MINI_PLAY||page==BUBBLE_PLAY||page==STAR_CATCH||page==MUSIC_PLAY||page==DRAW)){
            move(catalogReturnPage());return;
        }
        if(page==VILLAGE_MENU) {move(HOME);return;}
        if(storyStage<0 && PlaylumiWorldStyle.valid(villageLaunch) &&
                (page==PLAY || page==DETAIL || page==MINI_PLAY || page==BUBBLE_PLAY ||
                 page==STAR_CATCH || page==MUSIC_PLAY || page==MINI_MENU || page==DRAW)) {
            move(VILLAGE_MENU);return;
        }
        if(page==WORLD_PICKER || page==HOME_OPTIONS) {move(HOME);return;}
        if(page==REWARD) { continueAfterBadge();return; }
        if(page==AVATAR) {move(HOME);return;}
        if(page==CLOSET) {move(closetReturn==CITY?CITY:closetReturn==SHOP?SHOP:AVATAR);return;}
        if(page==ACHIEVEMENTS){move(achievementReturn==CITY?CITY:achievementReturn==SHOP?SHOP:achievementReturn==AVATAR?AVATAR:achievementReturn==COLLECTIBLES?COLLECTIBLES:HOME);return;}
        if(page==COLLECTIBLES) {move(collectionReturn==CITY?CITY:collectionReturn==SHOP?SHOP:collectionReturn==AVATAR?AVATAR:HOME);return;}
        if(page==SHOP) {move(HOME);return;}
        if(page==FRIEND_CARE) {move(friendReturn==CITY?CITY:AVATAR);return;}
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
            move(WORLD_HUB);return;
        }
        if(page==PARENT_GATE || page==PARENT_HOME) {parentAllowed=false;move(HOME);return;}
        if(page==PARENT_WORLDS || page==PARENT_SETTINGS) {move(PARENT_HOME);return;}
        if(page==PARENT_STATS) {move(PARENT_WORLDS);return;}
        if(page==BREAK_SCREEN) {move(HOME);return;}
        if(page==PROFILES) {move(PARENT_HOME);return;}
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
        saveUsage(false);
    }
    private boolean saveUsage(boolean synchronous) {
        if(usage==null)return true;
        int day=dayStamp();
        if(prefs().getInt("usage_day",-1)!=day)usage.startNewDay();
        if(activeSince>0) {
            long now=SystemClock.elapsedRealtime();
            long elapsed=Math.max(0,(now-activeSince)/1000L);
            usage.addSeconds((int)Math.min(86400L,elapsed));
            activeSince=now;
        }
        SharedPreferences.Editor edit=prefs().edit().putInt("usage_day",day).putInt("usage_seconds",usage.seconds())
                .putInt("usage_limit",usage.limit())
                .putBoolean("usage_break_shown",usage.breakShown());
        if(synchronous)return edit.commit();
        edit.apply();return true;
    }
    private SharedPreferences prefs() {
        return profilePrefs(activeProfile);
    }
    private SharedPreferences profilePrefs(int profile) {
        PlaylumiProfiles.validate(profile);
        return profile==0?getPreferences(MODE_PRIVATE):
                getSharedPreferences(PlaylumiProfiles.preferences(profile),MODE_PRIVATE);
    }
    private void saveRecent() { prefs().edit().putString("recent_questions",recent.serialize()).apply(); }
    private void stageAdaptive(SharedPreferences.Editor edit,int cat) {
        edit.putInt("learn_lvl_"+cat,adaptive.level(cat,Math.max(0,age)))
                .putInt("learn_seen_"+cat,adaptive.sampleCount(cat))
                .putInt("learn_good_"+cat,adaptive.sampleCorrect(cat));
    }
    private void saveScore(int mode) {
        SharedPreferences.Editor edit=prefs().edit();
        stageScore(edit,mode);
        edit.apply();
    }
    private void stageScore(SharedPreferences.Editor edit,int mode) {
        PlaylumiScores.Score s=scores.get(mode);
        String prefix="score_"+mode+"_";
        int gained=Math.max(0,s.correct-prefs().getInt(prefix+"good",0));
        edit.putInt(prefix+"good",s.correct).putInt(prefix+"tries",s.attempts)
                .putInt(prefix+"best",s.bestStreak).putInt(prefix+"streak",s.currentStreak)
                .putInt(prefix+"sessions",s.completedSessions)
                .putInt("completed_all",lifetimeCount);
        if(gained>0){
            stagePlayDay(edit);
            int w=page==MUSIC_PLAY?3:page==DRAW?5:page==BUBBLE_PLAY||page==STAR_CATCH?4:
                    page==MINI_PLAY?(miniKind==PlaylumiMiniChallenge.DRAG?2:4):
                    PlaylumiWorldStyle.valid(villageLaunch)?villageLaunch:portalForMode(mode);
            stageWorldProgress(edit,w,gained);
        }
    }
    private PlaylumiPlayDays playDays(){
        return new PlaylumiPlayDays(prefs().getLong("play_day_last",Long.MIN_VALUE),
                prefs().getInt("play_day_run",0),prefs().getInt("play_day_best",0),prefs().getInt("play_day_total",0));
    }
    private void recordPlayDay(){
        SharedPreferences.Editor edit=prefs().edit();
        stagePlayDay(edit);edit.apply();
    }
    private void stagePlayDay(SharedPreferences.Editor edit){
        PlaylumiPlayDays days=playDays();
        if(!days.record(PlaylumiPlayDays.today()))return;
        edit.putLong("play_day_last",days.last).putInt("play_day_run",days.run)
                .putInt("play_day_best",days.best).putInt("play_day_total",days.total);
    }
    private int[] achievementStats(){
        int[] stats=new int[PlaylumiAchievements.METRICS];
        long correct=0,sessions=0;int best=0;
        for(int i=0;i<PlaylumiScores.CATEGORIES;i++){
            PlaylumiScores.Score score=scores.get(i);correct+=score.correct;sessions+=score.completedSessions;
            best=Math.max(best,score.bestStreak);
        }
        stats[0]=(int)Math.min(Integer.MAX_VALUE,correct);
        stats[1]=(int)Math.min(Integer.MAX_VALUE,sessions);stats[2]=best;
        stats[3]=Math.max(0,wardrobe.allTimeStars()-PlaylumiWardrobe.STARTER_STARS);
        stats[4]=librarySet("library_played").size();
        stats[5]=PlaylumiVillageProgress.visitedCount(getSharedPreferences("playlumi-village-"+activeProfile,MODE_PRIVATE).getInt("visited",0));
        stats[6]=collectionTotal();stats[7]=Math.max(0,wardrobe.unlockedItems()-PlaylumiWardrobe.SLOTS);
        stats[8]=prefs().getBoolean("drawing_saved_once",false)?1:0;
        boolean favorite=prefs().getBoolean("achievement_favorite_ever",false)||!librarySet("library_favorites").isEmpty();
        if(favorite&&!prefs().getBoolean("achievement_favorite_ever",false))prefs().edit().putBoolean("achievement_favorite_ever",true).apply();
        // Preserve the earned legacy milestone; new players earn it by replaying a game.
        boolean replayed=false;
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age))if(roundsPlayed(entry)>=2){replayed=true;break;}
        stats[9]=favorite||replayed?1:0;
        PlaylumiPlayDays days=playDays();stats[10]=days.total;stats[11]=days.best;
        for(int i=0;i<6;i++)stats[12+i]=PlaylumiCollection.unlocked(collectionProgress(i));
        return stats;
    }
    private long achievedMask(int[] stats){
        long saved=prefs().getLong("achievements_v1",0L),next=PlaylumiAchievements.unlock(saved,stats);
        if(next!=saved)prefs().edit().putLong("achievements_v1",next).apply();
        return next;
    }
    private int achievementCount(){return Long.bitCount(achievedMask(achievementStats()));}
    private void openAchievements(int from){achievementReturn=from;achievementFilter=0;move(ACHIEVEMENTS);}
    private int roundsPlayed(PlaylumiGameCatalog.Entry e){
        return Math.max(0,prefs().getInt(PlaylumiCompletion.roundsKey(age,e.id),0));
    }
    private PlaylumiDailyChallenges dailyChallenges(){
        SharedPreferences p=prefs();
        return new PlaylumiDailyChallenges(PlaylumiPlayDays.today(),p.getLong("daily_day_v1",Long.MIN_VALUE),
                p.getInt("daily_a_v1",0),p.getInt("daily_b_v1",0),p.getInt("daily_round_v1",0),
                p.getInt("daily_claimed_v1",0),p.getInt("daily_sets_v1",0));
    }
    private int stageDaily(SharedPreferences.Editor edit,PlaylumiGameCatalog.Entry entry,boolean finished){
        if(entry==null||entry.kind!=PlaylumiGameCatalog.QUIZ)return 0;
        PlaylumiDailyChallenges daily=dailyChallenges();
        int reward=daily.correct(entry.world,finished);
        edit.putLong("daily_day_v1",daily.day).putInt("daily_a_v1",daily.progress(0))
                .putInt("daily_b_v1",daily.progress(1)).putInt("daily_round_v1",daily.progress(2))
                .putInt("daily_claimed_v1",daily.mask()).putInt("daily_sets_v1",daily.completedSets());
        if(reward>0)wardrobe.earnChallengeStars(reward);
        return reward;
    }
    private PlaylumiGameCatalog.Entry dailyRoute(PlaylumiDailyChallenges daily,int goal){
        ArrayList<String> unfinished=new ArrayList<>();
        for(PlaylumiGameCatalog.Entry entry:unfinishedGames())unfinished.add(entry.id);
        return PlaylumiDailyRoutes.choose(age,daily,goal,unfinished);
    }
    private void launchDaily(int goal){
        PlaylumiGameCatalog.Entry entry=dailyRoute(dailyChallenges(),goal);
        if(entry==null)return;
        libraryFilter=entry.world;libraryScrollY=0;quickCategoryPlay=false;
        launchCatalog(entry,libraryResume(entry)!=null,DAILY);
    }
    private int expeditionProgress(){
        return PlaylumiExpedition.safe(prefs().getInt(PlaylumiExpedition.key(age),0));
    }
    private void openExpedition(){
        expeditionReturn=page==CITY?CITY:page==HOME?HOME:LIBRARY;
        expeditionChapter=-1;libraryReturn=false;storyStage=-1;move(EXPEDITION);
    }
    private void addExpeditionShortcut(boolean magic){
        int done=expeditionProgress();
        LinearLayout card=magic?libraryPanel():personalPanel();
        if(magic)card.setBackground(libraryCardSurface(3));
        int ink=magic?PlaylumiMagicArt.CREAM:TEXT,sub=magic?PlaylumiMagicArt.GOLD:SUB;
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon=text("🗺️",30,ink);icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        top.addView(icon,new LinearLayout.LayoutParams(dp(47),dp(48)));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);
        homeAdd(labels,homeLabel("Lumi’s expeditie",19,ink,true),0,2);
        homeAdd(labels,homeLabel(done==30?"Alle zes zegels gevonden!":done+"/30 opdrachten · "+PlaylumiExpedition.stamps(done)+"/6 zegels",13,sub,false),0,1);
        top.addView(labels,new LinearLayout.LayoutParams(0,-2,1f));
        TextView go=text("›",27,sub);go.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        top.addView(go,new LinearLayout.LayoutParams(dp(26),dp(48)));card.addView(top);
        parentProgressBar(card,PlaylumiExpedition.TOTAL,done,0xff9670cf,done+" van 30 expeditieopdrachten");
        libraryClick(card,"Open Lumi’s expeditie",this::openExpedition);
        addWidget(card,2,5);
    }
    private void launchExpeditionMission(){
        if(page!=EXPEDITION||moving)return;
        PlaylumiGameCatalog.Entry entry=PlaylumiExpedition.route(age,expeditionProgress());
        if(entry==null)return;
        libraryFilter=entry.world;libraryScrollY=0;
        launchCatalog(entry,libraryResume(entry)!=null,EXPEDITION);
    }
    private void showExpedition(){
        // This is a separate progress track, not a second copy of the user's saved games.
        libraryReturn=false;
        final int done=expeditionProgress(),active=PlaylumiExpedition.currentChapter(done);
        final boolean complete=PlaylumiExpedition.finished(done);
        if(!PlaylumiExpedition.available(done,expeditionChapter))expeditionChapter=active;
        final int selected=expeditionChapter;
        header("Lumi’s expeditie","Zes hoofdstukken · één groot avontuur");
        LinearLayout passport=personalPanel();
        homeAdd(passport,homeLabel(complete?"🏆 Expeditie voltooid!":"Jouw avonturenpaspoort",23,TEXT,true),0,4);
        homeAdd(passport,homeLabel(done+" van 30 opdrachten · "+PlaylumiExpedition.stamps(done)+" van 6 zegels",16,SUB,false),0,3);
        parentProgressBar(passport,30,done,0xff9b6bd3,done+" van 30 opdrachten afgerond");
        homeAdd(passport,homeLabel("★ "+PlaylumiExpedition.starsEarned(done)+" / "+PlaylumiExpedition.totalStars()+" extra speelsterren verdiend",16,0xff715021,true),0,3);
        homeAdd(passport,homeLabel(complete?"Alle hoofdstukken zijn ontdekt. De schat is van jou!":"Geen tijdslimiet. Je mag tussendoor overal vrij spelen.",13,SUB,false),0,3);
        addWidget(passport,2,5);
        addWidget(new PlaylumiExpeditionMap(this,done),0,6);
        if(complete){
            LinearLayout certificate=personalPanel();
            homeAdd(certificate,homeLabel("🌈 De regenboogschat",24,TEXT,true),0,4);
            homeAdd(certificate,homeLabel("Lumi’s ontdekkingsdiploma · "+AGES[PlaylumiRocketCurriculum.level(age)],17,0xff27684d,true),0,4);
            homeAdd(certificate,homeLabel("30 opdrachten opgelost en alle zes zegels verzameld.",15,SUB,false),0,3);
            for(String stamp:PlaylumiExpedition.STAMPS)homeAdd(certificate,homeLabel("✓ "+stamp,15,0xff27684d,false),0,1);
            addWidget(certificate,2,5);
        }else{
            PlaylumiGameCatalog.Entry next=PlaylumiExpedition.route(age,done);
            LinearLayout mission=personalPanel();
            homeAdd(mission,homeLabel("Nu op avontuur · hoofdstuk "+(active+1),14,PlaylumiWorldStyle.deep(active),true),0,3);
            homeAdd(mission,homeLabel(PlaylumiExpedition.TITLES[active],21,TEXT,true),0,3);
            if(done>0&&done%5==0)homeAdd(mission,homeLabel(PlaylumiExpedition.FINISH[active-1]+" +15 ★ verdiend.",14,0xff27684d,true),0,4);
            homeAdd(mission,homeLabel(PlaylumiExpedition.STORIES[active],15,SUB,false),0,4);
            if(next!=null){
                org.json.JSONObject save=libraryResume(next);
                String sub=next.icon+" "+next.title+(save==null?"":" · "+save.optInt("count",0)+"/"+save.optInt("total",0)+" gespeeld");
                Button play=tile("▶ Speel opdracht "+(done+1),sub,0xffd5f5df,80);
                play.setContentDescription("Expeditie opdracht "+(done+1)+" spelen");
                mission.addView(play,new LinearLayout.LayoutParams(-1,-2));play.setOnClickListener(v->launchExpeditionMission());
            }
            homeAdd(mission,homeLabel("Vijf opdrachten per hoofdstuk: een zegel en +15 ★. Na de laatste ook +30 ★.",13,SUB,false),4,1);
            addWidget(mission,2,5);
        }
        addText("Hoofdstukken",20,TEXT,5,4);
        int width=getResources().getConfiguration().screenWidthDp;
        int columns=width>=650&&getResources().getConfiguration().fontScale<1.2f?3:2;
        for(int start=0;start<PlaylumiExpedition.CHAPTERS;start+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);row.setBaselineAligned(false);
            for(int i=start;i<Math.min(start+columns,PlaylumiExpedition.CHAPTERS);i++){
                final int chapter=i;
                int count=PlaylumiExpedition.chapterProgress(done,chapter);boolean available=PlaylumiExpedition.available(done,chapter);
                LinearLayout card=personalPanel();
                card.setBackground(rounded(available?PlaylumiWorldStyle.pale(chapter):0xffece8f0,16));
                homeAdd(card,homeLabel((count==5?"✓":available?PlaylumiExpedition.ICONS[chapter]:"🔒")+"  "+(chapter+1),18,PlaylumiWorldStyle.deep(chapter),true),0,2);
                homeAdd(card,homeLabel(PlaylumiExpedition.TITLES[chapter],16,TEXT,true),0,4);
                homeAdd(card,homeLabel(count+"/5 · "+(count==5?"Zegel verdiend":available?"Ontdekken":"Nog op slot"),13,SUB,false),0,2);
                parentProgressBar(card,5,count,PlaylumiWorldStyle.accent(chapter),count+" van 5 opdrachten");
                if(chapter==selected)homeAdd(card,homeLabel("Geselecteerd",12,0xff27684d,true),0,1);
                if(available)libraryClick(card,"Bekijk hoofdstuk "+(chapter+1)+": "+PlaylumiExpedition.TITLES[chapter],()->{
                    expeditionChapter=chapter;showPage();
                });
                else card.setContentDescription("Hoofdstuk "+(chapter+1)+": "+PlaylumiExpedition.TITLES[chapter]+", nog op slot");
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,lp);
            }
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        LinearLayout chapterCard=personalPanel();
        homeAdd(chapterCard,homeLabel(PlaylumiExpedition.ICONS[selected]+" "+PlaylumiExpedition.TITLES[selected],20,TEXT,true),0,4);
        homeAdd(chapterCard,homeLabel(PlaylumiExpedition.STORIES[selected],14,SUB,false),0,4);
        java.util.List<PlaylumiGameCatalog.Entry> entries=PlaylumiExpedition.chapter(age,selected);
        for(int i=0;i<entries.size();i++){
            int index=selected*5+i;PlaylumiGameCatalog.Entry entry=entries.get(i);
            String state=index<done?"✓":index==done?"▶":"○";
            homeAdd(chapterCard,homeLabel(state+"  "+(index+1)+". "+entry.title,16,index<done?0xff27684d:TEXT,true),0,3);
            homeAdd(chapterCard,homeLabel(PlaylumiWorldStyle.NAMES[entry.world]+(index==done?" · Jouw volgende opdracht":index<done?" · Afgerond":""),13,SUB,false),0,6);
        }
        int chapterDone=PlaylumiExpedition.chapterProgress(done,selected);
        homeAdd(chapterCard,homeLabel(chapterDone==5?"✓ "+PlaylumiExpedition.STAMPS[selected]+" verdiend":PlaylumiExpedition.STAMPS[selected]+" · voltooi alle vijf opdrachten",15,0xff715021,true),3,3);
        addWidget(chapterCard,2,5);
        Button free=tile("Vrij spelen in de werelden","Je expeditie blijft bewaard",0xffe8dcff,65);
        addTile(free,4);free.setOnClickListener(v->openLibrary(PlaylumiGameCatalog.OVERVIEW));
        Button classic=tile("Klassieke missies","Je oude avonturen blijven bestaan",0xffeaf0fb,60);
        addTile(classic,4);classic.setOnClickListener(v->{
            libraryReturn=false;villageLaunch=PlaylumiVillageView.MISSIONS;world=0;storyPage=Math.min(2,adventures.progress(world)/2);move(ADVENTURE_MAP);
        });
    }
    private void showDailyChallenges(){
        PlaylumiDailyChallenges daily=dailyChallenges();
        header("☀ Lumi's daguitdagingen","Kies een avontuur voor vandaag");
        LinearLayout summary=personalPanel();
        homeAdd(summary,text(daily.completed()+"/3 uitdagingen · ★ "+wardrobe.stars(),22,TEXT),0,5);
        LinearLayout milestones=new LinearLayout(this);milestones.setGravity(Gravity.CENTER);
        for(int i=0;i<PlaylumiDailyChallenges.COUNT;i++){
            boolean done=(daily.mask()&(1<<i))!=0;
            int color=done?0xffd3f0d7:i==2?0xffffedc5:PlaylumiWorldStyle.pale(daily.world(i));
            TextView mark=text((done?"✓":"✦")+" "+daily.progress(i)+"/"+daily.goal(i),17,TEXT);
            mark.setBackground(rounded(color,14));mark.setPadding(dp(4),dp(7),dp(4),dp(7));
            mark.setContentDescription(daily.title(i)+", "+daily.progress(i)+" van "+daily.goal(i));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);milestones.addView(mark,lp);
        }
        homeAdd(summary,milestones,0,5);
        homeAdd(summary,text("Dagbeloningen: "+daily.claimedReward()+" / "+daily.totalReward()+" sterren vrijgespeeld",15,0xff715021),0,4);
        homeAdd(summary,text("Kies een avontuur voor "+AGES[PlaylumiRocketCurriculum.level(age)]+".",14,SUB),0,3);
        homeAdd(summary,text(daily.completed()==3?"✓ Alle drie behaald · de bonus is al in je sterrenpot!":"Alle drie behaald? +"+PlaylumiDailyChallenges.BONUS+" bonussterren.",16,0xff715021),0,2);
        homeAdd(summary,text("Speel wanneer je zin hebt. Morgen zijn er nieuwe opdrachten; verdiende sterren en cadeaus blijven van jou.",14,SUB),3,2);
        addWidget(summary,3,6);
        for(int i=0;i<PlaylumiDailyChallenges.COUNT;i++){
            final int goal=i;boolean done=(daily.mask()&(1<<i))!=0;
            final PlaylumiGameCatalog.Entry selected=dailyRoute(daily,i);
            LinearLayout card=personalPanel();
            homeAdd(card,text((done?"✓ ":"✦ ")+daily.title(i),21,TEXT),0,3);
            homeAdd(card,text(daily.description(i),15,SUB),0,3);
            ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
            bar.setMax(daily.goal(i));bar.setProgress(daily.progress(i));bar.setProgressTintList(ColorStateList.valueOf(0xff9570d5));
            bar.setContentDescription(daily.title(i)+", "+daily.progress(i)+" van "+daily.goal(i));
            card.addView(bar,new LinearLayout.LayoutParams(-1,dp(10)));
            homeAdd(card,text(daily.progress(i)+"/"+daily.goal(i)+" · "+(done?"★ "+daily.reward(i)+" verdiend":"+"+daily.reward(i)+" ★"),16,0xff27684d),3,3);
            org.json.JSONObject saved=selected==null?null:libraryResume(selected);
            String title=saved!=null?"▶ Verder spelen":done?"↻ Nog een ronde":"▶ Spelen";
            Button play=tile(title,selected==null?"Kies een quiz":selected.title+(saved==null?"":" · "+saved.optInt("count",0)+"/"+saved.optInt("total",SESSION_ROUNDS)),0xffd5f5df,72);
            play.setContentDescription("Daguitdaging "+(i+1)+" spelen");
            card.addView(play,new LinearLayout.LayoutParams(-1,-2));
            play.setOnClickListener(v->launchDaily(goal));
            if(done)homeAdd(card,text("Deze bonus heb je al. Nieuwe goede antwoorden geven gewone speelsterren.",13,SUB),5,1);
            addWidget(card,2,5);
        }
        Button back=tile("⌂ Naar Lumi's stad","",0xffe8dcff,64);addTile(back,4);
        back.setContentDescription("Open Lumi's stad");back.setOnClickListener(v->move(CITY));
    }
    private void showAchievements(){
        header("Mijn prestaties","Ontdek wat je allemaal al hebt geleerd.");
        final int[] stats=achievementStats();final long mask=achievedMask(stats);
        LinearLayout summary=personalPanel();PlaylumiPlayDays days=playDays();
        int streak=days.current(PlaylumiPlayDays.today());
        PlaylumiMedal trophy=new PlaylumiMedal(this,"🏆",true,2);
        LinearLayout.LayoutParams trophyParams=new LinearLayout.LayoutParams(dp(90),dp(90));trophyParams.gravity=Gravity.CENTER;
        summary.addView(trophy,trophyParams);
        homeAdd(summary,text(Long.bitCount(mask)+" van "+PlaylumiAchievements.ALL.length+" prestaties",22,TEXT),1,5);
        ProgressBar overall=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        overall.setMax(PlaylumiAchievements.ALL.length);overall.setProgress(Long.bitCount(mask));
        overall.setProgressTintList(ColorStateList.valueOf(0xffa87724));
        summary.addView(overall,new LinearLayout.LayoutParams(-1,dp(10)));
        if(streak>0)homeAdd(summary,text("🔥 "+streak+(streak==1?" speeldag":" dagen op rij"),24,0xff774397),2,4);
        else homeAdd(summary,text("Jouw speelavonturen",19,TEXT),1,3);
        LinearLayout metrics=new LinearLayout(this);metrics.setGravity(Gravity.CENTER);
        for(String label:new String[]{"★ "+days.total+"\nspeeldagen","🏅 "+days.best+"\nlangste reeks"}){
            TextView metric=text(label,17,TEXT);metric.setPadding(dp(5),dp(7),dp(5),dp(7));
            metric.setBackground(rounded(0xffffedc8,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
            lp.setMargins(dp(3),0,dp(3),0);metrics.addView(metric,lp);
        }
        summary.addView(metrics,new LinearLayout.LayoutParams(-1,-2));
        homeAdd(summary,text("Speel wanneer je zin hebt. Je trofeeën blijven altijd van jou.",14,SUB),5,2);
        int completeCount=PlaylumiCompletion.completed(age,completedRoutes()).size();
        homeAdd(summary,text("✓ "+completeCount+" spellen met een hele ronde gespeeld",17,0xff286e54),5,3);
        homeAdd(summary,text("Elk spel kun je opnieuw spelen. Nieuwe rondes tellen mee voor meesterschap.",12,SUB),0,3);
        addWidget(summary,3,7);
        LinearLayout filters=new LinearLayout(this);filters.setGravity(Gravity.CENTER);
        String[] filterNames={"Alle ("+PlaylumiAchievements.ALL.length+")","Behaald ("+Long.bitCount(mask)+")","Nog te ontdekken"};
        for(int i=0;i<filterNames.length;i++){
            final int selected=i;boolean active=achievementFilter==i;
            TextView tab=text(filterNames[i],14,active?PlaylumiMagicArt.INK:PlaylumiMagicArt.CREAM);
            tab.setTypeface(Typeface.DEFAULT,Typeface.BOLD);tab.setMinHeight(dp(48));tab.setPadding(dp(3),dp(3),dp(3),dp(3));
            tab.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),active?PlaylumiMagicArt.creamPlate(this):rounded(0xff7653a6,13),null));
            tab.setSelected(active);libraryClick(tab,filterNames[i]+(active?", geselecteerd":""),()->{achievementFilter=selected;showPage();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);filters.addView(tab,lp);
        }
        addWidget(filters,2,5);
        if(achievementFilter==1&&mask==0)addText("Je verzameling groeit terwijl je speelt.",16,SUB,5,8);
        if(achievementFilter==2&&Long.bitCount(mask)==PlaylumiAchievements.ALL.length)addText("Je hebt alle prestaties verzameld!",17,TEXT,5,8);
        int width=getResources().getConfiguration().screenWidthDp;
        int columns=width>=1100?3:width>=600?2:1;
        for(int group=0;group<PlaylumiAchievements.GROUPS.length;group++){
            ArrayList<Integer> ids=new ArrayList<>();
            for(int i=0;i<PlaylumiAchievements.ALL.length;i++)if(PlaylumiAchievements.ALL[i].group==group&&PlaylumiPremiumLayout.showAchievement(achievementFilter,(mask&(1L<<i))!=0))ids.add(i);
            if(ids.isEmpty())continue;
            addText(PlaylumiAchievements.GROUPS[group],21,TEXT,8,3);
            for(int first=0;first<ids.size();first+=columns){
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);
                for(int j=0;j<columns;j++){
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(4),dp(4),dp(4),dp(4));
                    if(first+j>=ids.size()){row.addView(new View(this),lp);continue;}
                    int id=ids.get(first+j);
                    PlaylumiAchievements.Milestone milestone=PlaylumiAchievements.ALL[id];
                    boolean earned=(mask&(1L<<id))!=0;
                    int progress=earned?milestone.goal:Math.min(milestone.goal,Math.max(0,stats[milestone.metric]));
                    LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(dp(10),dp(8),dp(10),dp(8));card.setMinimumHeight(dp(132));
                    GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,earned?new int[]{0xfffffff0,0xffffe7b6}:new int[]{0xfffaf5ff,0xffe9dbfa});
                    bg.setCornerRadius(dp(18));bg.setStroke(dp(2),earned?0xffd9aa51:0xffc9afe8);card.setBackground(bg);
                    LinearLayout title=new LinearLayout(this);title.setGravity(Gravity.CENTER_VERTICAL);
                    PlaylumiMedal icon=new PlaylumiMedal(this,milestone.icon,earned,milestone.group);title.addView(icon,new LinearLayout.LayoutParams(dp(62),dp(62)));
                    TextView label=homeLabel(milestone.title,17,TEXT,true);title.addView(label,new LinearLayout.LayoutParams(0,-2,1f));
                    card.addView(title);
                    homeAdd(card,homeLabel(milestone.description,13,SUB,false),2,3);
                    for(PlaylumiCityRewards.Landmark landmark:PlaylumiCityRewards.ALL)if(landmark.achievement==id)
                        homeAdd(card,homeLabel("🎁 "+landmark.name+" + "+landmark.gift(),14,0xff715021,true),2,3);
                    ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
                    bar.setMax(milestone.goal);bar.setProgress(progress);
                    bar.setProgressTintList(ColorStateList.valueOf(earned?0xffa87724:0xff8555b4));
                    card.addView(bar,new LinearLayout.LayoutParams(-1,dp(8)));
                    homeAdd(card,homeLabel(earned?"✓ Behaald":progress+" / "+milestone.goal,13,earned?0xff37744a:SUB,true),3,0);
                    card.setContentDescription(milestone.title+", "+milestone.description+", "+(earned?"behaald":progress+" van "+milestone.goal));
                    row.addView(card,lp);
                }
                addWidget(row,0,0);
            }
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
        SharedPreferences.Editor edit=prefs().edit();
        stageWorldProgress(edit,world,gained);edit.apply();
    }
    private void stageWorldProgress(SharedPreferences.Editor edit,int world,int gained){
        if(!PlaylumiWorldStyle.valid(world)||gained<=0)return;
        int before=collectionProgress(world);
        int after=PlaylumiCollection.advance(before,gained);
        if(after==before)return;
        edit.putInt("collection_world_"+world,after);
        if(PlaylumiCollection.unlocked(after)>PlaylumiCollection.unlocked(before)){
            String treasure=PlaylumiCollection.NAMES[world][PlaylumiCollection.unlocked(after)-1];
            android.widget.Toast.makeText(this,"✦ Nieuwe schat: "+treasure,android.widget.Toast.LENGTH_SHORT).show();
            if(playPal!=null)PlaylumiDelight.sprinkle(stage,playPal);
        }
    }
    private void saveWardrobe() {
        SharedPreferences.Editor edit=prefs().edit();
        stageWardrobe(edit);edit.apply();
    }
    private void stageWardrobe(SharedPreferences.Editor edit) {
        edit.putBoolean("wardrobe_initialized",true)
            .putInt("wardrobe_stars",wardrobe.stars())
            .putInt("wardrobe_total_earned",wardrobe.allTimeStars());
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++) {
            edit.putInt("wardrobe_owned_"+i,wardrobe.ownedMask(i));
            edit.putInt("wardrobe_worn_"+i,wardrobe.equipped(i));
        }
    }
    private void saveStories() {
        prefs().edit().putInt("stories_completed",stories.completionMask())
                .putInt("stories_current",stories.currentStory())
                .putInt("stories_step",stories.step())
                .putInt("stories_alts",stories.secondaryChoices())
                .putInt("stories_choice",stories.choice()).apply();
    }
    private void saveFriends() {
        SharedPreferences.Editor editor=prefs().edit();
        stageFriends(editor);editor.apply();
    }
    private void stageFriends(SharedPreferences.Editor editor) {
        editor.putInt("friends_rewarded",friends.rewardedMask());
        for(int i=0;i<PlaylumiFriends.FRIENDS;i++)
            editor.putInt("friends_care_"+i,friends.mask(i));
    }
    private void saveGarden() {
        SharedPreferences.Editor e=prefs().edit();
        stageGarden(e);e.apply();
    }
    private void stageGarden(SharedPreferences.Editor e) {
        e.putInt("garden_rewarded",garden.rewardMask());
        for(int i=0;i<PlaylumiGarden.COUNT;i++)
            e.putInt("garden_stage_"+i,garden.stage(i));
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
        if(page==LIBRARY)return PlaylumiWorldStyle.valid(libraryFilter)?libraryFilter:PlaylumiVillageHit.PLAYGROUND;
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
    private int chosenAvatar() {return PlaylumiWorldStyle.LUMI;}
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
            else if(page==MINI_PLAY||page==BUBBLE_PLAY||page==STAR_CATCH||page==MUSIC_PLAY)
                height=Math.min(height,available<640?54:76);
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
    private void addPrompt(LinearLayout parent,String instruction,int size){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        int theme=worldTheme();
        row.setBackground(PlaylumiWorldStyle.valid(theme)?libraryCardSurface(theme):PlaylumiMagicArt.silkRibbon(this));
        row.setPadding(dp(9),dp(3),dp(9),dp(3));
        TextView prompt=text(instruction,size,PlaylumiMagicArt.CREAM);
        prompt.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));prompt.setPadding(dp(4),dp(5),dp(4),dp(5));prompt.setMinHeight(dp(50));
        row.addView(prompt,new LinearLayout.LayoutParams(0,-2,1f));
        addWidgetIn(parent,row,0,dp(5));
    }
    private void addQuizBanner(int theme,int mode,int variant,int height,boolean wide){
        FrameLayout banner=new FrameLayout(this);banner.setBackground(rounded(0xff4b2b75,18));banner.setClipToOutline(true);
        int h=PlaylumiPremiumLayout.quizBannerHeight(height,wide);
        View art=PlaylumiMagicArt.hero(this,theme);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams artSpace=new FrameLayout.LayoutParams(-1,-1);
        // Keep the medal in its own space, away from the illustrated world's title.
        artSpace.rightMargin=dp(h+10);banner.addView(art,artSpace);
        View shade=new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0x00332050,0x00332050,0xb5332050}));
        banner.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        PlaylumiMedal emblem=new PlaylumiMedal(this,PlaylumiPremiumLayout.emblem(mode,variant),true,theme%3);
        FrameLayout.LayoutParams icon=new FrameLayout.LayoutParams(dp(h-6),dp(h-6),Gravity.END|Gravity.CENTER_VERTICAL);
        icon.rightMargin=dp(8);banner.addView(emblem,icon);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(h));lp.bottomMargin=dp(5);root.addView(banner,lp);
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
        row.setBaselineAligned(false);
        row.setClipChildren(false);row.setClipToPadding(false);
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
        character.setContentDescription("Lumi. Tik voor een magische verrassing.");
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
        playPal.setContentDescription("Lumi. Tik om te zwaaien.");
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
        if(page!=HOME && (page!=AGE || PlaylumiProfiles.hasChosenAge(prefs().getInt("chosen_age",-1)))) {
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
                playPal.setContentDescription("Lumi reageert op je antwoord");
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
        autoAdvanceToken++;melodyPreviewToken++;autoRoundVisible=false;
        renderedCalendarDay=PlaylumiPlayDays.today();
        // Retired screens may still exist in a saved Activity after an update.
        if(page==IDEAS)page=ideasReturn==CITY?CITY:LIBRARY;
        if(page==PROFILES){page=PARENT_GATE;parentAllowed=false;}
        if(page==HOME&&!PlaylumiProfiles.hasChosenAge(age))page=AGE;
        // Restore old navigation destinations directly into the new library.
        if(page==VILLAGE_MENU){page=LIBRARY;libraryFilter=PlaylumiWorldStyle.valid(villageLaunch)?villageLaunch:PlaylumiGameCatalog.OVERVIEW;}
        // Preserve the old avatar choice in storage but always play as Lumi.
        // Previously retired friend pages now belong to Lumi's city again.
        if(wardrobe!=null&&(page==HOME||page==CITY||page==AVATAR||page==CLOSET||page==ACHIEVEMENTS||page==FRIEND_CARE||page==SHOP)){
            if(PlaylumiCityRewards.grant(wardrobe,achievedMask(achievementStats())))saveWardrobe();
        }
        if(!parentAllowed && protectedParentPage())page=PARENT_GATE;
        playPal=null;progressCounter=null;melodySteps=null;questionPicture=null;questionPattern=null;skillBoard=null;quizNext=null;quizFeedbackRow=null;magicHeaderProgress=null;roundStars=null;melodyPreviewActive=false;libraryTabs=null;drawing=null;drawingInfo=null;
        java.util.Arrays.fill(drawingColors,null);java.util.Arrays.fill(drawingWidths,null);
        drawingPencil=null;drawingEraser=null;drawingUndo=null;drawingRedo=null;drawingClear=null;eraseAsked=false;
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
                page==AVATAR || page==CITY || page==DAILY || page==EXPEDITION || page==IDEAS || page==CLOSET || page==ROOM || page==COLLECTIBLES || page==ACHIEVEMENTS || page==SHOP ||
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
                    int left=0,right=0;
                    if(Build.VERSION.SDK_INT>=30){
                        android.graphics.Insets horizontal=insets.getInsets(
                                android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());
                        left=horizontal.left;right=horizontal.right;
                    }
                    stage.setPadding(left,Math.max(0,top),right,Math.max(0,bottom));
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
                        android.util.Log.i("PlaylumiQA","gameBottomInset="+bottom+" gameReservedBottom="+safeBottom+
                                " gameSafeTop="+Math.max(0,top)+" gamePage="+page);
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
            case ACHIEVEMENTS: showAchievements();break;
            case DAILY: showDailyChallenges();break;
            case CITY: showCity();break;
            case EXPEDITION: showExpedition();break;
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
            stage.setAlpha(.82f);stage.setTranslationX(dp(page==INTERACTIVE?0:6));
            stage.animate().alpha(1).translationX(0).setDuration(165)
                    .setInterpolator(new DecelerateInterpolator()).start();
            if(page!=LIBRARY&&page!=INTERACTIVE&&page!=CLOSET&&page!=HOME&&page!=ACHIEVEMENTS&&!gameScreen())
                root.post(()->{if(root==currentRoot)PlaylumiMotion.reveal(currentRoot);});
        }
    }
    private void move(int next) {
        if(moving)return;
        if(next==IDEAS)next=page==CITY?CITY:LIBRARY;
        if(next==PROFILES)next=PARENT_GATE;
        if(next==HOME&&!PlaylumiProfiles.hasChosenAge(age))next=AGE;
        if(next==DAILY&&(page==HOME||page==CITY||page==AVATAR||page==LIBRARY))dailyReturn=page;
        autoAdvanceToken++;melodyPreviewToken++;
        if(next==EXPEDITION&&page!=EXPEDITION)expeditionChapter=-1;
        if(page==LIBRARY&&next!=LIBRARY&&scroller!=null){libraryScrollY=scroller.getScrollY();}
        if(page==PLAY&&next!=PLAY)saveLibraryQuiz();
        if(page==INTERACTIVE&&next!=INTERACTIVE)saveLibraryInteractive();
        saveLibrarySpecial(page==BREAK_SCREEN?breakReturn:page);
        // Every exit, including the header house, preserves the latest strokes.
        // Autosaving never awards the explicit first-save bonus.
        if(page==DRAW&&drawing!=null&&!drawing.save()){
            if(drawingInfo!=null)drawingInfo.setText("Bewaren lukte nog niet. Probeer het nog eens; je tekening staat nog hier.");
            return;
        }
        if(next==HOME)libraryReturn=false;
        if(next!=PARENT_GATE&&next!=PARENT_HOME&&next!=PARENT_WORLDS&&next!=PARENT_STATS&&next!=PARENT_SETTINGS&&next!=PROFILES)
            parentAllowed=false;
        saveUsage();
        if(usage!=null && page!=BREAK_SCREEN && next!=BREAK_SCREEN &&
                next!=PARENT_GATE && next!=PARENT_HOME &&
                next!=PARENT_WORLDS && next!=PARENT_STATS && next!=PARENT_SETTINGS && next!=PROFILES &&
                usage.shouldSuggestBreak()) {
            breakReturn=next;next=BREAK_SCREEN;saveUsage();
        }
        if(!advancingAutomatically&&sound!=null)sound.tap();
        // Commit navigation synchronously before Android can save the Activity on fold/rotate.
        // The existing entering animation is visual only, never responsible for committing state.
        moving=false;page=next;showPage();
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
    private void visitVillagePlace(int place){
        if(moving||page!=HOME)return;
        if(PlaylumiWorldStyle.valid(place)){
            SharedPreferences discovery=getSharedPreferences("playlumi-village-"+activeProfile,MODE_PRIVATE);
            int old=discovery.getInt("visited",0),updated=PlaylumiVillageProgress.markVisited(old,place);
            if(old!=updated)discovery.edit().putInt("visited",updated).apply();
        }
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
                requestParents();
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
                move(DAILY);break;
            case PlaylumiVillageView.PLAYER:
                move(AVATAR);break;
            case PlaylumiVillageView.HOME:
                move(CITY);
                break;
            case PlaylumiVillageView.COLLECTION:
                collectionReturn=HOME;move(COLLECTIBLES);
                break;
            case PlaylumiVillageView.PROFILE:
                move(AVATAR);
                break;
            default:break;
        }
    }
    private void showHome(){
        int width=getResources().getConfiguration().screenWidthDp;
        int height=getResources().getConfiguration().screenHeightDp;
        stage.removeView(scroller);
        if(PlaylumiHomeLayout.mode(width,height)!=PlaylumiHomeLayout.PHONE){
            showWideHome(width,height);return;
        }
        PlaylumiVillageView village=new PlaylumiVillageView(this,this::visitVillagePlace,
                activeProfile,wardrobe==null?0:wardrobe.stars(),chosenAvatar(),
                wardrobe==null?null:wardrobe.equippedItems(),playDays().current(PlaylumiPlayDays.today()),dailyChallenges().completed());
        stage.addView(new PlaylumiVillageNavigation(this,village,this::visitVillagePlace,
                wardrobe.stars(),dailyChallenges().completed()),new FrameLayout.LayoutParams(-1,-1));
    }
    private TextView homeLabel(String label,int size,int color,boolean bold){
        TextView view=text(label,size,color);view.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        if(bold)view.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        return view;
    }
    private LinearLayout homePanel(){
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10),dp(10),dp(10),dp(10));panel.setBackground(PlaylumiMagicArt.softPanel(this));return panel;
    }
    private void homeAdd(LinearLayout panel,View child,int top,int bottom){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(top),0,dp(bottom));panel.addView(child,lp);
    }
    private TextView homeAction(String label,boolean primary,Runnable action){
        TextView button=text(label,15,primary?PlaylumiMagicArt.INK:PlaylumiMagicArt.CREAM);
        button.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        button.setMinHeight(dp(48));button.setPadding(dp(7),dp(5),dp(7),dp(5));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                primary?PlaylumiMagicArt.creamPlate(this):rounded(0xff63408e,14),null));
        libraryClick(button,label,()->{if(page==HOME&&!moving)action.run();});
        return button;
    }
    private void homeMetric(LinearLayout bar,String value,String description,Runnable action){
        TextView chip=homeAction(value,false,action);chip.setTextSize(15);chip.setContentDescription(description);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-1);
        lp.setMargins(dp(3),0,dp(3),0);bar.addView(chip,lp);
    }
    private void showWideHome(int width,int height){
        final int mode=PlaylumiHomeLayout.mode(width,height);
        final java.util.List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.entries(age);
        final int visited=PlaylumiVillageProgress.sanitize(getSharedPreferences(
                "playlumi-village-"+activeProfile,MODE_PRIVATE).getInt("visited",0));
        final int stars=wardrobe==null?0:wardrobe.stars();
        final int discovered=PlaylumiVillageProgress.visitedCount(visited);

        stage.addView(PlaylumiMagicArt.backdrop(this,PlaylumiVillageView.COLORS),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(8),dp(6),dp(8),dp(6));
        stage.addView(shell,new FrameLayout.LayoutParams(-1,-1));

        // Real full-width top bar: live controls, not a stretched picture.
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(10),dp(5),dp(6),dp(5));top.setBackground(PlaylumiMagicArt.silkRibbon(this));
        TextView title=homeLabel("✦ Playlumi",width<650?21:25,PlaylumiMagicArt.CREAM,true);
        top.addView(title,new LinearLayout.LayoutParams(0,-1,1f));
        homeMetric(top,"★ "+stars,stars+" speelsterren, open beloningen",()->visitVillagePlace(PlaylumiVillageView.STARS));
        homeMetric(top,"☀ "+dailyChallenges().completed()+"/3","Dagelijkse uitdagingen, "+dailyChallenges().completed()+" van 3 behaald",()->move(DAILY));
        if(width>=800)homeMetric(top,"🏆", "Mijn prestaties",()->openAchievements(HOME));
        homeMetric(top,"⚙","Instellingen",()->visitVillagePlace(PlaylumiVillageView.SETTINGS));
        shell.addView(top,new LinearLayout.LayoutParams(-1,dp(62)));

        // Only the content scrolls on short landscape windows. Navigation stays visible.
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollParams=new LinearLayout.LayoutParams(-1,0,1f);
        scrollParams.setMargins(0,dp(8),0,dp(8));shell.addView(scroll,scrollParams);
        LinearLayout content=new LinearLayout(this);content.setGravity(Gravity.TOP);content.setOrientation(LinearLayout.HORIZONTAL);
        scroll.addView(content,new ScrollView.LayoutParams(-1,-2));

        LinearLayout profile=wideHomeProfile(width,stars,discovered);
        LinearLayout actions=wideHomeActions();
        LinearLayout worlds=new LinearLayout(this);worlds.setOrientation(LinearLayout.VERTICAL);
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView headingText=homeLabel("Lumi's magische stad",width<650?20:23,PlaylumiMagicArt.CREAM,true);
        heading.addView(headingText,new LinearLayout.LayoutParams(0,-2,1f));
        TextView total=text(entries.size()+" spellen",13,PlaylumiMagicArt.GOLD);
        heading.addView(total,new LinearLayout.LayoutParams(-2,-2));
        heading.setPadding(dp(3),dp(3),dp(3),dp(6));worlds.addView(heading);
        int columns=PlaylumiHomeLayout.worldColumns(width,height);
        int artHeight=PlaylumiHomeLayout.artHeight(width,height);
        for(int first=0;first<6;first+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);
            row.setBaselineAligned(false);row.setClipChildren(false);row.setClipToPadding(false);
            for(int j=0;j<columns;j++){
                int world=first+j;
                if(world>=6)break;
                final int place=world;
                LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x25ffffff),libraryCardSurface(world),null));
                card.setPadding(0,0,0,dp(6));card.setClipToOutline(true);
                card.addView(new PlaylumiHomeWorldArt(this,world),new LinearLayout.LayoutParams(-1,dp(artHeight)));
                TextView name=text(PlaylumiWorldStyle.NAMES[world],width<650?15:18,PlaylumiMagicArt.CREAM);
                name.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
                name.setMinHeight(dp(31));name.setMaxLines(2);
                card.addView(name,new LinearLayout.LayoutParams(-1,-2));
                int games=PlaylumiGameCatalog.filter(age,world,java.util.Collections.emptySet()).size();
                int treasures=PlaylumiCollection.unlocked(collectionProgress(world));
                TextView meta=text(games+(games==1?" spel":" spellen")+"  ·  ✦ "+treasures+"/3",12,PlaylumiMagicArt.GOLD);
                card.addView(meta,new LinearLayout.LayoutParams(-1,-2));
                if(PlaylumiVillageProgress.isVisited(visited,world)){
                    TextView badge=text("✓ Ontdekt",12,0xffc7f3d5);card.addView(badge,new LinearLayout.LayoutParams(-1,-2));
                }else{
                    TextView badge=text("Nieuw avontuur",12,0xffffe7af);card.addView(badge,new LinearLayout.LayoutParams(-1,-2));
                }
                libraryClick(card,PlaylumiWorldStyle.NAMES[world]+", open speelwereld, "+games+" spellen",()->visitVillagePlace(place));
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1f);cp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(card,cp);
            }
            worlds.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        if(mode==PlaylumiHomeLayout.EXPANDED){
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(PlaylumiHomeLayout.profileWidth(width)), -2);
            lp.rightMargin=dp(8);content.addView(profile,lp);
            content.addView(worlds,new LinearLayout.LayoutParams(0,-2,1f));
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(PlaylumiHomeLayout.actionWidth(width)), -2);
            rp.leftMargin=dp(8);content.addView(actions,rp);
        }else{
            content.addView(worlds,new LinearLayout.LayoutParams(0,-2,1f));
            LinearLayout side=new LinearLayout(this);side.setOrientation(LinearLayout.VERTICAL);
            homeAdd(side,profile,0,8);homeAdd(side,actions,0,0);
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(PlaylumiHomeLayout.actionWidth(width)),-2);
            rp.leftMargin=dp(7);content.addView(side,rp);
        }

        // Full-width native navigation with distinct 48dp+ touch targets.
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(4),dp(4),dp(4),dp(4));nav.setBackground(PlaylumiMagicArt.silkRibbon(this));
        String[] titles=width<600?new String[]{"Spellen","Cadeaus","Stad","Schatten","Lumi"}:
                new String[]{"▶ Spellen","🎁 Beloningen","⌂ Stad","✦ Collectie","★ Lumi"};
        int[] places={PlaylumiVillageView.MISSIONS,PlaylumiVillageView.REWARDS,PlaylumiVillageView.HOME,PlaylumiVillageView.COLLECTION,PlaylumiVillageView.PROFILE};
        for(int i=0;i<places.length;i++){
            final int place=places[i];TextView item=homeAction(titles[i],i==2,()->visitVillagePlace(place));
            item.setTextSize(width<650?13:16);item.setSelected(i==2);
            item.setContentDescription(i==2?"Open Lumi's stad":titles[i]);
            item.setSelected(false);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1f);lp.setMargins(dp(3),0,dp(3),0);nav.addView(item,lp);
        }
        shell.addView(nav,new LinearLayout.LayoutParams(-1,dp(64)));
    }
    private LinearLayout wideHomeProfile(int width,int stars,int discovered){
        LinearLayout panel=homePanel();panel.setGravity(Gravity.CENTER_HORIZONTAL);
        PlaylumiAvatar pal=new PlaylumiAvatar(this,chosenAvatar(),wardrobe);
        int size=width<650?80:width<1100?96:112;
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(size),dp(size));pp.gravity=Gravity.CENTER;
        panel.addView(pal,pp);libraryClick(pal,"Open Lumi",()->visitVillagePlace(PlaylumiVillageView.FRIEND));
        homeAdd(panel,text(PlaylumiWorldStyle.AVATAR_NAMES[chosenAvatar()],20,PlaylumiMagicArt.CREAM),2,4);
        homeAdd(panel,text(AGES[PlaylumiRocketCurriculum.level(age)],13,0xffe8d3ff),0,2);
        homeAdd(panel,text("★ "+stars+" sterren",17,PlaylumiMagicArt.GOLD),2,1);
        homeAdd(panel,text("✦ "+collectionTotal()+"/18 schatten",13,PlaylumiMagicArt.CREAM),0,3);
        PlaylumiPlayDays days=playDays();int current=days.current(PlaylumiPlayDays.today());
        homeAdd(panel,homeAction("🏆 Mijn prestaties",false,()->openAchievements(HOME)),1,3);
        homeAdd(panel,homeAction("Lumi",true,()->visitVillagePlace(PlaylumiVillageView.FRIEND)),3,3);
        homeAdd(panel,homeAction("Ouderzone",false,()->visitVillagePlace(PlaylumiVillageView.PARENTS)),2,0);
        return panel;
    }
    private void startFromWideHome(PlaylumiGameCatalog.Entry entry){
        if(page!=HOME||moving||entry==null)return;
        libraryFilter=PlaylumiGameCatalog.OVERVIEW;libraryScrollY=0;
        quickCategoryPlay=false;launchCatalog(entry,libraryResume(entry)!=null);
    }
    private LinearLayout wideHomeActions(){
        LinearLayout panel=homePanel();
        homeAdd(panel,homeLabel("Snel spelen",19,PlaylumiMagicArt.GOLD,true),0,5);
        homeAdd(panel,homeAction("☀ Daguitdagingen "+dailyChallenges().completed()+"/3",true,()->move(DAILY)),0,5);
        homeAdd(panel,homeAction("✦ Expeditie "+expeditionProgress()+"/30",false,this::openExpedition),3,3);
        homeAdd(panel,homeAction("Werelden",false,()->openLibrary(PlaylumiGameCatalog.OVERVIEW)),3,3);
        homeAdd(panel,homeAction("Mijn schatten",false,()->visitVillagePlace(PlaylumiVillageView.COLLECTION)),2,0);
        return panel;
    }

    private void openLibrary(int filter){
        libraryReturn=false;libraryFilter=availableLibraryFilter(filter);libraryScrollY=0;storyStage=-1;move(LIBRARY);
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
    private java.util.Set<String> completedRoutes(){
        return librarySet(PlaylumiCompletion.key(age));
    }
    private int catalogReturnPage(){
        return catalogReturn==DAILY?DAILY:catalogReturn==CITY?CITY:catalogReturn==EXPEDITION?EXPEDITION:LIBRARY;
    }
    private void describeCatalogReturn(Button button){
        if(!libraryReturn||storyStage>=0)return;
        if(catalogReturn==DAILY){button.setText("Daguitdagingen");button.setContentDescription("Terug naar daguitdagingen");}
        else if(catalogReturn==CITY){button.setText("Mijn stad");button.setContentDescription("Terug naar mijn stad");}
        else if(catalogReturn==EXPEDITION){button.setText("Naar de expeditie");button.setContentDescription("Terug naar de expeditie");}
    }
    /** One validated snapshot of the current age and profile for the visible page. */
    private static final class PlayHistory {
        final java.util.Map<String,Long> unfinished=new java.util.HashMap<>();
        final java.util.Map<String,Integer> rounds=new java.util.HashMap<>();
        final java.util.Map<String,org.json.JSONObject> snapshots=new java.util.HashMap<>();
        final java.util.Set<String> completed;
        PlayHistory(java.util.Set<String> completed){this.completed=completed;}
    }
    private PlayHistory playHistory(){
        PlayHistory history=new PlayHistory(completedRoutes());
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
            if(!PlaylumiCompletion.eligible(entry))continue;
            history.rounds.put(entry.id,roundsPlayed(entry));
            org.json.JSONObject saved=libraryResume(entry);
            if(saved!=null){
                history.unfinished.put(entry.id,saved.optLong("updated",0L));
                history.snapshots.put(entry.id,saved);
            }
        }
        return history;
    }
    private void recordCompletion(PlaylumiGameCatalog.Entry entry,int done,int total){
        SharedPreferences.Editor edit=prefs().edit();
        stageCompletion(edit,entry,done,total);edit.apply();
    }
    private void stageCompletion(SharedPreferences.Editor edit,PlaylumiGameCatalog.Entry entry,int done,int total){
        if(!libraryReturn||!PlaylumiCompletion.eligible(entry)||total<=0||done!=total||PlaylumiGameCatalog.find(age,entry.id)==null)return;
        String roundsKey=PlaylumiCompletion.roundsKey(age,entry.id);
        edit.putInt(roundsKey,PlaylumiCompletion.nextRoundCount(prefs().getInt(roundsKey,0),done,total));
        java.util.Set<String> completed=completedRoutes();
        if(PlaylumiCompletion.record(completed,age,entry,done,total))
            edit.putStringSet(PlaylumiCompletion.key(age),completed);
        PlaylumiExpedition.Advance advance=PlaylumiExpedition.complete(age,expeditionProgress(),entry.id,catalogReturn==EXPEDITION,done,total);
        if(advance.changed()){
            edit.putInt(PlaylumiExpedition.key(age),advance.after);
            if(advance.bonus>0){wardrobe.earnChallengeStars(advance.bonus);stageWardrobe(edit);}
        }
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
        SharedPreferences.Editor edit=prefs().edit();
        stageLibrarySnapshot(edit,entry,null);edit.apply();
    }
    private void writeLibrarySnapshot(PlaylumiGameCatalog.Entry entry,org.json.JSONObject saved){
        SharedPreferences.Editor edit=prefs().edit();
        stageLibrarySnapshot(edit,entry,saved);edit.apply();
    }
    private void stageLibrarySnapshot(SharedPreferences.Editor edit,PlaylumiGameCatalog.Entry entry,org.json.JSONObject saved){
        if(saved==null)edit.remove(snapshotKey(entry));
        else edit.putString(snapshotKey(entry),saved.toString());
        String legacy=legacySnapshotKey(entry);
        if(matchingSnapshot(legacy,entry)!=null)edit.remove(legacy);
    }
    private org.json.JSONObject libraryResume(PlaylumiGameCatalog.Entry entry){
        if(entry==null)return null;
        if(entry.kind==PlaylumiGameCatalog.INTERACTIVE)return libraryInteractiveResume(entry);
        if(entry.kind!=PlaylumiGameCatalog.QUIZ)return librarySpecialResume(entry);
        org.json.JSONObject saved=readLibrarySnapshot(entry);
        if(saved==null)return null;
        int level=saved.optInt("level",-1);
        if(level<0||level>2||saved.optInt("total",SESSION_ROUNDS)!=SESSION_ROUNDS)return null;
        int count=saved.optInt("count",-1),attempts=saved.optInt("attempts",-1);
        if(!PlaylumiResumeKeys.validQuizProgress(count,attempts,saved.optBoolean("solved",false),SESSION_ROUNDS))return null;
        return saved;
    }
    private org.json.JSONObject libraryInteractiveResume(PlaylumiGameCatalog.Entry entry){
        if(entry==null||entry.kind!=PlaylumiGameCatalog.INTERACTIVE)return null;
        org.json.JSONObject o=readLibrarySnapshot(entry);
        if(o==null)return null;
        try{
            int count=o.getInt("count");
            PlaylumiInteractive game=new PlaylumiInteractive(entry.mode,PlaylumiRocketCurriculum.level(age),o.getLong("seed"),count,o.optInt("engine",PlaylumiInteractive.LEGACY),o.optInt("mask",0));
            if(count<=0||count>=game.total()||game.progress()!=count||o.optInt("total",game.total())!=game.total())return null;
            return o;
        }catch(org.json.JSONException|IllegalArgumentException ignored){return null;}
    }
    private void saveLibraryInteractive(){
        SharedPreferences.Editor edit=prefs().edit();
        stageLibraryInteractive(edit);edit.apply();
    }
    private void stageLibraryInteractive(SharedPreferences.Editor edit){
        if(interactive==null)return;
        PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.route(age,villageLaunch,
                PlaylumiGameCatalog.INTERACTIVE,interactive.kind,0);
        if(entry==null)return;
        if(interactive.solved()||interactive.progress()==0){stageLibrarySnapshot(edit,entry,null);return;}
        try{
            org.json.JSONObject o=new org.json.JSONObject();
            o.put("id",entry.id);o.put("age",age);o.put("seed",interactive.seed);o.put("engine",interactive.version);
            o.put("count",interactive.progress());o.put("total",interactive.total());
            if(interactive.puzzle()!=null)o.put("mask",interactive.mask());
            o.put("updated",System.currentTimeMillis());stageLibrarySnapshot(edit,entry,o);
        }catch(org.json.JSONException ignored){}
    }
    private void saveLibraryQuiz(){
        SharedPreferences.Editor edit=prefs().edit();
        stageLibraryQuiz(edit);edit.apply();
    }
    private void stageLibraryQuiz(SharedPreferences.Editor edit){
        if(game==null||storyStage>=0||!libraryReturn)return;
        PlaylumiGameCatalog.Entry e=PlaylumiGameCatalog.route(age,villageLaunch,
                PlaylumiGameCatalog.QUIZ,game.mode(),game.variant());
        PlaylumiGameCatalog.Entry last=lastCatalog();
        if(e==null||last==null||!e.id.equals(last.id))return;
        if(sessionSolved>=sessionGoal()){stageLibrarySnapshot(edit,e,null);return;}
        if(!PlaylumiResumeKeys.validQuizProgress(sessionSolved,sessionAttempts,game.round().isSolved(),sessionGoal()))return;
        try{
            LearningGame.Round r=game.round();
            org.json.JSONObject o=new org.json.JSONObject();
            o.put("id",e.id);o.put("age",age);o.put("level",game.level());
            o.put("count",sessionSolved);o.put("total",sessionGoal());o.put("attempts",sessionAttempts);o.put("solved",r.isSolved());
            o.put("hadMistake",roundHadMistake);o.put("rejected",r.rejectedMask());o.put("feedback",feedback);
            o.put("prompt",r.prompt);o.put("display",r.display);o.put("color",r.displayColor);
            o.put("correct",r.correctIndex);o.put("fingers",r.fingerCount);
            o.put("answers",new org.json.JSONArray(r.answers));o.put("colors",new org.json.JSONArray(r.answerColors));
            o.put("updated",System.currentTimeMillis());stageLibrarySnapshot(edit,e,o);
        }catch(org.json.JSONException ignored){}
    }
    private boolean resumeLibraryQuiz(PlaylumiGameCatalog.Entry entry){
        org.json.JSONObject o=libraryResume(entry);
        if(o==null)return false;
        try{
            int level=o.getInt("level"),count=o.getInt("count"),attempts=o.getInt("attempts");
            if(level<0||level>2||!PlaylumiResumeKeys.validQuizProgress(count,attempts,o.getBoolean("solved"),SESSION_ROUNDS))return false;
            game.configure(entry.mode,entry.variant,level);
            feedback="";roundHadMistake=false;
            // If only the saved question is damaged or obsolete, keep the earned
            // score and attempts and continue with a new question at the saved level.
            try{
                org.json.JSONArray a=o.getJSONArray("answers"),c=o.getJSONArray("colors");
                if(a.length()<2||a.length()>30||a.length()!=c.length())throw new IllegalArgumentException("Invalid saved choices");
                ArrayList<String> answers=new ArrayList<>();ArrayList<Integer> colors=new ArrayList<>();
                for(int i=0;i<a.length();i++)answers.add(a.isNull(i)?null:a.getString(i));
                for(int i=0;i<c.length();i++)colors.add(c.isNull(i)?null:c.getInt(i));
                if(!PlaylumiLetterChallenges.legacy(entry.mode,entry.variant,o.getString("display"))&&
                        !PlaylumiWeatherPractice.legacy(entry.mode,entry.variant,o.getString("prompt"))&&
                        !(entry.mode==19&&entry.variant==4&&o.getString("display").contains("Je steekt over bij groen licht"))&&
                        !(entry.mode==16&&entry.variant==3&&"Hoe schrijf je deze tijd?".equals(o.getString("prompt")))&&
                        !(level==2&&entry.mode==LearningGame.PATTERNS&&PlaylumiPatterns.isVisualPattern(o.getString("display"))&&PlaylumiPatterns.tokens(o.getString("display")).length<8)&&
                        !(entry.mode==ExtraGames.SEQUENCES&&o.getString("display").split("→").length<4)&&
                        !(entry.mode==23&&(entry.variant==0||entry.variant==1||entry.variant==3)&&o.getString("display").split("→").length<4)){
                    game.restoreRound(o.getString("prompt"),o.getString("display"),o.getInt("color"),
                        answers,colors,o.getInt("correct"),o.getInt("fingers"),o.getBoolean("solved"),o.optInt("rejected",0));
                    feedback=game.round().isSolved()?o.optString("feedback",""):"";
                    roundHadMistake=o.optBoolean("hadMistake",false)||game.round().rejectedMask()!=0;
                }
            }catch(org.json.JSONException|IllegalArgumentException ignored){
                // The newly generated question is still valid; no points are awarded on restore.
                feedback="";roundHadMistake=false;
            }
            difficulty=level;sessionSolved=count;sessionAttempts=attempts;
            smartDifficulty=false;pendingLevel=-1;pendingBadges.clear();
            earnedBadge=-1;rewardReturn=PLAY;quickCategoryPlay=true;storyStage=-1;
            // A solved round has already earned its reward. Manual mode keeps its
            // feedback and Next control visible; automatic modes resume on the next question.
            finalFeedbackPending=false;
            if(PlaylumiQuizPace.advanceRestored(quizPace(),game.round().isSolved())){
                int next=catalogLevel(entry);
                if(game.level()!=next){difficulty=next;game.configure(entry.mode,entry.variant,next);}else game.nextRound();
                roundHadMistake=false;feedback="";
            }
            prefs().edit().putInt("game_mode",entry.mode).putInt("game_variant",entry.variant)
                    .putInt("last_played_category",entry.mode).putInt("last_played_variant",entry.variant).apply();
            saveRecent();saveLibraryQuiz();move(PLAY);return true;
        }catch(org.json.JSONException|IllegalArgumentException ignored){return false;}
    }
    private int specialTotal(int kind){
        return kind==PlaylumiGameCatalog.MUSIC?PlaylumiMelody.ROUNDS:kind==PlaylumiGameCatalog.STARS?PlaylumiStarCatch.ROUNDS:
                kind==PlaylumiGameCatalog.BUBBLES||kind==PlaylumiGameCatalog.MINI?20:0;
    }
    private PlaylumiGameCatalog.Entry specialEntry(int screen){
        if(!libraryReturn)return null;
        switch(screen){
            case MINI_PLAY:return PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.MINI,miniKind,0);
            case MUSIC_PLAY:return PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.MUSIC,0,melodyVariant);
            case BUBBLE_PLAY:return PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.BUBBLES,0,0);
            case STAR_CATCH:return PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.STARS,0,0);
            default:return null;
        }
    }
    private ArrayList<String> specialChoices(org.json.JSONArray array)throws org.json.JSONException{
        ArrayList<String> out=new ArrayList<>();for(int i=0;i<array.length();i++)out.add(array.getString(i));return out;
    }
    private int[] specialNotes(org.json.JSONArray array)throws org.json.JSONException{
        int[] out=new int[array.length()];for(int i=0;i<out.length;i++)out[i]=array.getInt(i);return out;
    }
    private PlaylumiMiniChallenge readMini(PlaylumiGameCatalog.Entry entry,org.json.JSONObject o)throws org.json.JSONException{
        return PlaylumiMiniChallenge.restore(entry.mode,PlaylumiRocketCurriculum.level(age),o.getString("target"),
                specialChoices(o.getJSONArray("choices")),o.getInt("correct"),o.getInt("matched"),o.getInt("open"));
    }
    private org.json.JSONObject librarySpecialResume(PlaylumiGameCatalog.Entry entry){
        int total=specialTotal(entry.kind);if(total==0)return null;
        org.json.JSONObject o=readLibrarySnapshot(entry);if(o==null)return null;
        try{
            int round=o.getInt("round"),count=o.getInt("count");boolean solved=o.getBoolean("solved");
            if(round<0||count!=round+(solved?1:0)||count>=total||o.getInt("total")!=total)return null;
            if(entry.kind==PlaylumiGameCatalog.MINI){
                PlaylumiMiniChallenge restored=readMini(entry,o);
                if(restored.kind==PlaylumiMiniChallenge.MEMORY&&restored.solved()!=solved)return null;
            }else if(entry.kind==PlaylumiGameCatalog.MUSIC){
                if(PlaylumiMelody.restore(specialNotes(o.getJSONArray("notes")),o.getInt("position")).completed()!=solved)return null;
            }else if(entry.kind==PlaylumiGameCatalog.BUBBLES){
                if(PlaylumiBubbles.restore(PlaylumiRocketCurriculum.level(age),o.getInt("target"),o.getInt("available"),o.getInt("mask")).solved()!=solved)return null;
            }else if(entry.kind==PlaylumiGameCatalog.STARS){
                if(PlaylumiStarCatch.restore(o.getInt("target"),o.getInt("available"),o.getInt("types"),o.getInt("mask")).finished()!=solved)return null;
            }
            return o;
        }catch(org.json.JSONException|IllegalArgumentException ignored){return null;}
    }
    private void saveLibrarySpecial(int screen){
        SharedPreferences.Editor edit=prefs().edit();
        stageLibrarySpecial(edit,screen);edit.apply();
    }
    private void stageLibrarySpecial(SharedPreferences.Editor edit,int screen){
        PlaylumiGameCatalog.Entry entry=specialEntry(screen);if(entry==null)return;
        int round,total=specialTotal(entry.kind);boolean solved,partial=false;
        try{
            org.json.JSONObject o=new org.json.JSONObject();
            switch(screen){
                case MINI_PLAY:
                    if(mini==null){stageLibrarySnapshot(edit,entry,null);return;}
                    round=miniRound;solved=miniSolved;
                    partial=mini.matchedPairs()>0||mini.firstOpen()>=0;
                    o.put("target",mini.target);o.put("choices",new org.json.JSONArray(mini.choices));
                    o.put("correct",mini.correctIndex);o.put("matched",mini.matchedMask());o.put("open",mini.firstOpen());break;
                case MUSIC_PLAY:
                    if(melody==null){stageLibrarySnapshot(edit,entry,null);return;}
                    round=melodyRound;solved=melodySolved;partial=melody.progress()>0;
                    org.json.JSONArray notes=new org.json.JSONArray();for(int note:melody.sequence())notes.put(note);
                    o.put("notes",notes);o.put("position",melody.progress());break;
                case BUBBLE_PLAY:
                    if(bubbles==null){stageLibrarySnapshot(edit,entry,null);return;}
                    round=bubbleRound;solved=bubbleSolved;partial=bubbles.popped()>0;
                    o.put("target",bubbles.target);o.put("available",bubbles.total);o.put("mask",bubbles.mask());break;
                case STAR_CATCH:
                    if(starCatch==null){stageLibrarySnapshot(edit,entry,null);return;}
                    round=starCatchRound;solved=starCatchSolved;partial=starCatch.collected()>0;
                    o.put("target",starCatch.target());o.put("available",starCatch.total());o.put("mask",starCatch.mask());o.put("types",starCatch.starMask());break;
                default:return;
            }
            int count=round+(solved?1:0);
            if(count>=total||count==0&&!partial){stageLibrarySnapshot(edit,entry,null);return;}
            o.put("id",entry.id);o.put("age",age);o.put("round",round);o.put("count",count);
            o.put("total",total);o.put("solved",solved);o.put("updated",System.currentTimeMillis());
            stageLibrarySnapshot(edit,entry,o);
        }catch(org.json.JSONException ignored){}
    }
    private boolean resumeLibrarySpecial(PlaylumiGameCatalog.Entry entry){
        org.json.JSONObject o=librarySpecialResume(entry);if(o==null)return false;
        try{
            int round=o.getInt("count"),level=PlaylumiRocketCurriculum.level(age),screen;
            boolean solved=o.getBoolean("solved");Random random=new Random();
            pendingBadges.clear();rewardReturn=PLAY;
            // A solved round was already credited. Resume the next one, never the reward callback.
            switch(entry.kind){
                case PlaylumiGameCatalog.MINI:
                    miniKind=entry.mode;miniRound=round;miniSolved=false;
                    mini=solved?PlaylumiMiniChallenge.generate(miniKind,level,random):readMini(entry,o);screen=MINI_PLAY;break;
                case PlaylumiGameCatalog.MUSIC:
                    melodyVariant=entry.variant;melodyRound=round;melodySolved=false;
                    melody=solved?PlaylumiMelody.generate(level,melodyVariant,random):PlaylumiMelody.restore(specialNotes(o.getJSONArray("notes")),o.getInt("position"));screen=MUSIC_PLAY;break;
                case PlaylumiGameCatalog.BUBBLES:
                    bubbleRound=round;bubbleSolved=false;
                    bubbles=solved?PlaylumiBubbles.generate(level,random):PlaylumiBubbles.restore(level,o.getInt("target"),o.getInt("available"),o.getInt("mask"));screen=BUBBLE_PLAY;break;
                case PlaylumiGameCatalog.STARS:
                    starCatchRound=round;starCatchSolved=false;
                    starCatch=solved?PlaylumiStarCatch.generate(level,random):PlaylumiStarCatch.restore(o.getInt("target"),o.getInt("available"),o.getInt("types"),o.getInt("mask"));screen=STAR_CATCH;break;
                default:return false;
            }
            saveLibrarySpecial(screen);move(screen);return true;
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
        launchCatalog(entry,resume,LIBRARY);
    }
    private void launchCatalog(PlaylumiGameCatalog.Entry entry,boolean resume,int returnPage){
        if(moving||entry==null)return;
        catalogReturn=returnPage==DAILY?DAILY:returnPage==CITY?CITY:returnPage==EXPEDITION?EXPEDITION:LIBRARY;
        libraryReturn=true;villageLaunch=entry.world;storyStage=-1;rememberCatalog(entry);
        SharedPreferences discovery=getSharedPreferences("playlumi-village-"+activeProfile,MODE_PRIVATE);
        discovery.edit().putInt("visited",PlaylumiVillageProgress.markVisited(discovery.getInt("visited",0),entry.world)).apply();
        if(entry.kind==PlaylumiGameCatalog.QUIZ){
            if(resume&&resumeLibraryQuiz(entry))return;
            startWorldQuiz(entry.mode,entry.variant);
        }else if(entry.kind==PlaylumiGameCatalog.INTERACTIVE){
            org.json.JSONObject saved=resume?libraryInteractiveResume(entry):null;
            if(saved!=null){
                interactive=new PlaylumiInteractive(entry.mode,PlaylumiRocketCurriculum.level(age),saved.optLong("seed"),saved.optInt("count"),saved.optInt("engine",PlaylumiInteractive.LEGACY),saved.optInt("mask",0));
                interactiveMessage="";move(INTERACTIVE);
            }else{rememberCatalog(entry);startInteractive(entry.mode);}
        }else if(resume&&resumeLibrarySpecial(entry))return;
        else if(entry.kind==PlaylumiGameCatalog.MINI){clearLibrarySnapshot(entry);startWorldMini(entry.mode);}
        else if(entry.kind==PlaylumiGameCatalog.MUSIC){
            clearLibrarySnapshot(entry);
            melodyVariant=entry.variant;rememberCatalog(entry);pendingBadges.clear();rewardReturn=PLAY;
            melodyRound=0;melodySolved=false;
            melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());move(MUSIC_PLAY);
        }else{if(specialTotal(entry.kind)>0)clearLibrarySnapshot(entry);startCatalogSpecial(entry.kind);}
    }
    private void startInteractive(int kind){
        interactive=PlaylumiInteractive.start(kind,PlaylumiRocketCurriculum.level(age),new Random().nextLong());
        interactiveMessage="";saveLibraryInteractive();
        move(INTERACTIVE);
    }
    private void showInteractive(){
        if(interactive==null){move(catalogReturnPage());return;}
        final PlaylumiInteractive round=interactive;
        if(round.puzzle()!=null){showPuzzle(round);return;}
        final int width=getResources().getConfiguration().screenWidthDp;
        final boolean wide=PlaylumiLayout.interactiveWide(width);
        header(PlaylumiInteractive.TITLES[round.kind],"");
        String instruction=round.instruction();
        addPrompt(root,instruction,18);
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
        if(round.wordPuzzle()&&!round.solved()){
            Button example=tile("💡 Bekijk het woord","",0xffffedc3,58);
            boardPane.addView(example,new LinearLayout.LayoutParams(-1,-2));
            example.setOnClickListener(v->{if(page==INTERACTIVE&&interactive==round&&!round.solved()){
                board.revealExample();example.setEnabled(false);
            }});
        }
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
        final LinearLayout victory=new LinearLayout(this);victory.setOrientation(LinearLayout.VERTICAL);
        addWidget(victory,3,6);
        if(round.solved())victory.addView(completionPanel(PlaylumiInteractive.TITLES[round.kind],round.kind==PlaylumiInteractive.NUMBERS?"🚀":round.kind==PlaylumiInteractive.LETTERS?"🚂":round.kind==PlaylumiInteractive.HABITATS?"🐾":"🎨",round.total(),1),new LinearLayout.LayoutParams(-1,-2));
        final LinearLayout finish=finishActions(again,back,round.solved());
        again.setOnClickListener(v->startInteractive(round.kind));back.setOnClickListener(v->move(catalogReturnPage()));
        LinearLayout row=null;
        for(int i=0;i<round.options().size();i++){
            if(i%choiceColumns==0){
                row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.setMargins(dp(2),dp(3),dp(2),dp(3));choices.addView(row,rp);
            }
            final String option=round.options().get(i);
            final Button choice=tile(option,"",COLORS[i%COLORS.length],72);
            choice.setMinHeight(dp(wide?80:66));choice.setTextSize(round.kind==PlaylumiInteractive.NUMBERS||round.kind==PlaylumiInteractive.LETTERS?25:17);
            if(round.kind==PlaylumiInteractive.MOSAIC||round.kind==PlaylumiInteractive.HABITATS){
                int paint=round.kind==PlaylumiInteractive.MOSAIC?PlaylumiHandsOn.color(option):PlaylumiHandsOn.habitatColor(option);
                GradientDrawable face=new GradientDrawable();face.setColor(paint);face.setCornerRadius(dp(18));face.setStroke(dp(2),0xffffdfa0);
                choice.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),face,null));
                choice.setTextColor(PlaylumiWorldStyle.contrastingInk(paint));
                if(round.kind==PlaylumiInteractive.MOSAIC)choice.setText(PlaylumiHandsOn.name(option));
                choice.setMaxLines(3);
            }
            choice.setContentDescription("Kies "+option);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),dp(2),dp(3),dp(2));row.addView(choice,lp);buttons.add(choice);
            if(round.kind==PlaylumiInteractive.NUMBERS)for(int n=0;n<round.progress();n++)if(option.equals(round.target(n))){
                choice.setEnabled(false);choice.setAlpha(.45f);break;
            }
            choice.setOnClickListener(v->{
                if(interactive!=round||page!=INTERACTIVE||moving||round.solved())return;
                final int previousStep=round.progress();
                if(round.choose(option)){
                    SharedPreferences.Editor progressEdit=prefs().edit();
                    stagePlayDay(progressEdit);
                    if(round.solved())sound.collect();else sound.good();
                    interactiveMessage=round.kind==PlaylumiInteractive.HABITATS&&!round.animalName(previousStep).isEmpty()?round.animalName(previousStep)+" → "+PlaylumiHandsOn.name(option):"Goed zo!";message.setTextColor(0xffc7ffd8);
                    if(playPal!=null)playPal.react(1);
                    board.refresh();count.setText(round.progress()+"/"+round.total());
                    trail.showProgress(round.progress());stageLibraryInteractive(progressEdit);
                    if(round.kind==PlaylumiInteractive.NUMBERS){choice.setEnabled(false);choice.setAlpha(.45f);}
                    else PlaylumiMotion.pop(choice);
                    if(round.solved()){
                        int category=round.kind==PlaylumiInteractive.NUMBERS?LearningGame.NUMBERS:
                                round.kind==PlaylumiInteractive.LETTERS?LearningGame.WORDS:
                                round.kind==PlaylumiInteractive.HABITATS?21:LearningGame.PATTERNS;
                        ArrayList<Integer> badges=new ArrayList<>(scores.answer(category,true));
                        lifetimeCount++;wardrobe.earnAnswer();wardrobe.earnBadges(badges.size());
                        stageWardrobe(progressEdit);stageScore(progressEdit,category);
                        stageCompletion(progressEdit,PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.INTERACTIVE,round.kind,0),round.progress(),round.total());
                        // The final board, replay count, score and wallet are one saved event.
                        progressEdit.apply();
                        interactiveMessage="🌟 Knap gedaan! +"+(PlaylumiWardrobe.STARS_FOR_ANSWER+badges.size()*PlaylumiWardrobe.STARS_FOR_BADGE)+" speelsterren";
                        for(Button b:buttons)b.setEnabled(false);
                        victory.removeAllViews();
                        victory.addView(completionPanel(PlaylumiInteractive.TITLES[round.kind],round.kind==PlaylumiInteractive.NUMBERS?"🚀":round.kind==PlaylumiInteractive.LETTERS?"🚂":round.kind==PlaylumiInteractive.HABITATS?"🐾":"🎨",round.total(),1),new LinearLayout.LayoutParams(-1,-2));
                        showFinishActions(finish,true);PlaylumiDelight.sprinkle(stage,board);
                    }else progressEdit.apply();
                }else{
                    sound.mistake();interactiveMessage=round.kind==PlaylumiInteractive.NUMBERS&&round.version>=PlaylumiInteractive.PREVIOUS&&round.level>0?
                            "Tel "+Math.abs(round.step())+(round.step()<0?" terug.":" verder."):
                            round.wordPuzzle()?"Zeg het woord langzaam. Welke letter hoor je nu?":round.kind==PlaylumiInteractive.MOSAIC?"Kijk naar het volgende vak in het voorbeeld.":round.kind==PlaylumiInteractive.HABITATS?"Kijk naar het dier. Probeer een ander huisje.":"Probeer nog eens!";
                    message.setTextColor(PlaylumiMagicArt.GOLD);PlaylumiDelight.wiggle(choice);
                }
                message.setText(interactiveMessage);
            });
        }
        if(round.solved())for(Button b:buttons)b.setEnabled(false);
    }

    private void showPuzzle(PlaylumiInteractive round){
        final PlaylumiPuzzle puzzle=round.puzzle();
        header(PlaylumiPuzzle.title(puzzle.kind),"");
        addPrompt(root,puzzle.instruction(),17);
        addMagicProgress(puzzle.progress(),puzzle.total(),false);
        final TextView count=progressCounter;final PlaylumiStarTrail trail=roundStars;
        final TextView message=text(puzzle.solved()?"Alles gelukt!":interactiveMessage.isEmpty()?"Jij bent aan de beurt!":interactiveMessage,16,PlaylumiMagicArt.CREAM);
        message.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        final LinearLayout panel=libraryPanel();panel.setBackground(libraryCardSurface(PlaylumiPuzzle.world(puzzle.kind)));
        final LinearLayout victory=new LinearLayout(this);victory.setOrientation(LinearLayout.VERTICAL);
        final Button again=tile("Nog eens spelen!","",0xffd5f5df,64),back=tile("Andere spellen","",0xffe8d9ff,64);
        final LinearLayout[] finish=new LinearLayout[1];
        final PlaylumiPuzzleBoard[] board=new PlaylumiPuzzleBoard[1];
        final TextView preview=text("",22,PlaylumiMagicArt.GOLD);preview.setVisibility(View.GONE);
        if(puzzle.kind==PlaylumiPuzzle.RHYTHM&&!puzzle.solved()){
            Button reveal=tile("Bekijk de reeks","",0xffffedc3,58);
            panel.addView(reveal,new LinearLayout.LayoutParams(-1,-2));panel.addView(preview,new LinearLayout.LayoutParams(-1,-2));
            reveal.setOnClickListener(v->{
                StringBuilder s=new StringBuilder();for(int i=0;i<puzzle.referenceCount();i++){if(i>0)s.append("  ");s.append(puzzle.reference(i));}
                preview.setText(s.toString());preview.setVisibility(View.VISIBLE);
            });
            if(sound.isEnabled()){
                Button listen=tile("Beluister de reeks","",0xffd9f3ff,58);panel.addView(listen,new LinearLayout.LayoutParams(-1,-2));
                listen.setOnClickListener(v->{
                    if(page!=INTERACTIVE||interactive!=round||round.solved()||melodyPreviewActive)return;
                    melodyPreviewActive=true;final long token=++melodyPreviewToken;final FrameLayout current=stage;
                    message.setText("Luister goed ...");
                    for(int i=0;i<puzzle.referenceCount();i++){
                        final int beat=i;
                        current.postDelayed(()->{
                            if(page!=INTERACTIVE||interactive!=round||stage!=current||token!=melodyPreviewToken||activeSince<=0||!melodyPreviewActive)return;
                            String animal=puzzle.reference(beat);
                            for(int note=0;note<PlaylumiMelody.ANIMALS.length;note++)if(PlaylumiMelody.ANIMALS[note].equals(animal))sound.note(note);
                        },i*520L);
                    }
                    current.postDelayed(()->{
                        if(page==INTERACTIVE&&interactive==round&&stage==current&&token==melodyPreviewToken){
                            melodyPreviewActive=false;message.setText("Nu jij!");
                        }
                    },puzzle.referenceCount()*520L+200L);
                });
            }
        }
        int width=Math.min(620,PlaylumiLayout.libraryContentWidth(getResources().getConfiguration().screenWidthDp)-24);
        board[0]=new PlaylumiPuzzleBoard(this,puzzle,width,(index,button)->{
            if(page!=INTERACTIVE||interactive!=round||moving||round.solved()||melodyPreviewActive)return;
            preview.setVisibility(View.GONE);
            if(!round.choose(""+index)){
                sound.mistake();interactiveMessage=puzzle.hint();message.setTextColor(PlaylumiMagicArt.GOLD);message.setText(interactiveMessage);PlaylumiDelight.wiggle(button);return;
            }
            SharedPreferences.Editor edit=prefs().edit();stagePlayDay(edit);
            if(puzzle.kind==PlaylumiPuzzle.RHYTHM)sound.note(index);else sound.good();
            board[0].refresh();count.setText(round.progress()+"/"+round.total());trail.showProgress(round.progress());
            stageLibraryInteractive(edit);
            if(round.solved()){
                int category=PlaylumiPuzzle.scoreCategory(puzzle.kind);
                ArrayList<Integer> badges=new ArrayList<>(scores.answer(category,true));lifetimeCount++;
                wardrobe.earnAnswer();wardrobe.earnBadges(badges.size());
                stageWardrobe(edit);stageScore(edit,category);
                stageCompletion(edit,PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.INTERACTIVE,round.kind,0),round.progress(),round.total());
                edit.apply();
                interactiveMessage="Knap gedaan! +"+(PlaylumiWardrobe.STARS_FOR_ANSWER+badges.size()*PlaylumiWardrobe.STARS_FOR_BADGE)+" speelsterren";
                victory.addView(completionPanel(PlaylumiPuzzle.title(puzzle.kind),PlaylumiPuzzle.icon(puzzle.kind),round.total(),1),new LinearLayout.LayoutParams(-1,-2));
                showFinishActions(finish[0],true);PlaylumiDelight.sprinkle(stage,board[0]);
            }else{edit.apply();interactiveMessage="Goed zo!";}
            message.setTextColor(0xffc7ffd8);message.setText(interactiveMessage);
        });
        panel.addView(board[0],new LinearLayout.LayoutParams(-1,-2));panel.addView(message,new LinearLayout.LayoutParams(-1,-2));
        addWidget(panel,3,4);addWidget(victory,3,6);
        if(round.solved())victory.addView(completionPanel(PlaylumiPuzzle.title(puzzle.kind),PlaylumiPuzzle.icon(puzzle.kind),round.total(),1),new LinearLayout.LayoutParams(-1,-2));
        finish[0]=finishActions(again,back,round.solved());
        again.setOnClickListener(v->startInteractive(round.kind));back.setOnClickListener(v->move(catalogReturnPage()));
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
        filter=availableLibraryFilter(filter);
        if(moving||page!=LIBRARY||libraryFilter==filter)return;
        libraryFilter=filter;libraryScrollY=0;
        if(PlaylumiWorldStyle.valid(filter)){
            SharedPreferences discovery=getSharedPreferences("playlumi-village-"+activeProfile,MODE_PRIVATE);
            int old=discovery.getInt("visited",0),updated=PlaylumiVillageProgress.markVisited(old,filter);
            if(updated!=old)discovery.edit().putInt("visited",updated).apply();
        }
        // Refresh the complete scene when changing worlds, including its backdrop.
        showPage();
    }
    private static final String[] LIBRARY_TAB_LABELS={"Dorp","Werelden"};
    private static final String[] LIBRARY_TAB_ICONS={"⌂","✦"};
    private static final int[] LIBRARY_TAB_FILTERS={Integer.MIN_VALUE,PlaylumiGameCatalog.OVERVIEW};
    private void refreshLibraryNavigation(){
        if(libraryTabs==null)return;
        for(int i=0;i<libraryTabs.length;i++){
            TextView tab=libraryTabs[i];
            boolean selected=LIBRARY_TAB_FILTERS[i]==libraryFilter||(i==1&&libraryFilter>=0);
            String label=LIBRARY_TAB_LABELS[i];
            boolean rail=PlaylumiLayout.libraryRail(getResources().getConfiguration().screenWidthDp);
            tab.setText(LIBRARY_TAB_ICONS[i]+(rail?"\n":"  ")+label);
            tab.setSelected(selected);tab.setTextColor(selected?PlaylumiMagicArt.INK:PlaylumiMagicArt.CREAM);
            tab.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                    selected?PlaylumiMagicArt.creamPlate(this):rounded(0xff573785,13),null));
            tab.setContentDescription(label+(selected?", geselecteerd":""));
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
    private void libraryWorldHeader(int world,int width){
        LinearLayout banner=new LinearLayout(this);banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackground(libraryCardSurface(world));banner.setPadding(dp(7),dp(6),dp(7),dp(6));
        banner.setMinimumHeight(dp(PlaylumiLayout.libraryBannerArtHeight(width)+12));
        View art=PlaylumiMagicArt.hero(this,world);
        art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        banner.addView(art,new LinearLayout.LayoutParams(dp(PlaylumiLayout.libraryBannerArtWidth(width)),dp(PlaylumiLayout.libraryBannerArtHeight(width))));
        LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);
        words.setGravity(Gravity.CENTER_VERTICAL);words.setPadding(dp(12),0,dp(4),0);
        TextView subtitle=homeLabel(PlaylumiWorldStyle.SUBTITLES[world],16,PlaylumiMagicArt.CREAM,true);
        subtitle.setMaxLines(3);words.addView(subtitle,new LinearLayout.LayoutParams(-1,-2));
        TextView ageLabel=homeLabel("✦ "+AGES[PlaylumiRocketCurriculum.level(age)],13,PlaylumiMagicArt.GOLD,false);
        ageLabel.setPadding(0,dp(4),0,0);words.addView(ageLabel,new LinearLayout.LayoutParams(-1,-2));
        banner.addView(words,new LinearLayout.LayoutParams(0,-2,1f));
        addWidget(banner,1,6);
    }
    private View libraryWorldCard(int world,int width){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),libraryCardSurface(world),null));
        card.setPadding(dp(5),dp(5),dp(5),dp(7));
        View art=PlaylumiMagicArt.hero(this,world);
        art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        card.addView(art,new LinearLayout.LayoutParams(-1,dp(PlaylumiLayout.libraryWorldArtHeight(width))));
        TextView title=homeLabel(PlaylumiWorldStyle.NAMES[world],17,PlaylumiMagicArt.CREAM,true);
        title.setGravity(Gravity.CENTER);title.setMinHeight(dp(34));title.setMaxLines(2);
        title.setPadding(dp(2),dp(4),dp(2),0);card.addView(title,new LinearLayout.LayoutParams(-1,-2));
        int games=PlaylumiGameCatalog.filter(age,world,java.util.Collections.emptySet()).size();
        int treasures=PlaylumiCollection.unlocked(collectionProgress(world));
        TextView info=text(games+(games==1?" spel":" spellen")+"  ·  ✦ "+treasures+"/3",13,PlaylumiMagicArt.GOLD);
        info.setPadding(dp(2),dp(2),dp(2),dp(4));card.addView(info,new LinearLayout.LayoutParams(-1,-2));
        TextView action=text("▶ Ontdek",14,PlaylumiMagicArt.INK);
        action.setTypeface(Typeface.DEFAULT,Typeface.BOLD);action.setBackground(PlaylumiMagicArt.creamPlate(this));
        action.setMinHeight(dp(36));action.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(dp(2),dp(2),dp(2),0);card.addView(action,ap);
        libraryClick(card,"Open "+PlaylumiWorldStyle.NAMES[world]+" "+games+" spellen",()->libraryFilter(world));
        return card;
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
            TextView tab=text("",width<360?14:16,PlaylumiMagicArt.CREAM);
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
        else scrollParams.bottomMargin=dp(PlaylumiLayout.libraryNavHeight()+6);
        scroller.setLayoutParams(scrollParams);
        FrameLayout.LayoutParams navParams=rail?
                new FrameLayout.LayoutParams(dp(PlaylumiLayout.LIBRARY_RAIL_WIDTH),-1,Gravity.START|Gravity.CENTER_VERTICAL):
                new FrameLayout.LayoutParams(-1,dp(PlaylumiLayout.libraryNavHeight()),Gravity.BOTTOM);
        int pad=dp(rail?8:PlaylumiLayout.librarySidePadding(width));
        navParams.setMargins(pad,rail?dp(8):0,pad,dp(3));stage.addView(nav,navParams);
    }
    private void showGameLibrary(){
        libraryFilter=availableLibraryFilter(libraryFilter);
        showLibraryNavigation();renderLibraryContents(libraryScrollY);
    }
    private void renderLibraryContents(int desiredScroll){
        final ScrollView active=scroller;final int filter=libraryFilter;
        root.removeAllViews();renderLibraryBody();refreshLibraryNavigation();
        libraryScrollY=Math.max(0,desiredScroll);
        active.post(()->{if(scroller==active&&page==LIBRARY&&libraryFilter==filter)active.scrollTo(0,Math.max(0,desiredScroll));});
    }
    private java.util.List<PlaylumiGameCatalog.Entry> unfinishedGames(){
        ArrayList<PlaylumiGameCatalog.Entry> entries=new ArrayList<>();
        final java.util.Map<String,Long> updates=new java.util.HashMap<>();
        for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age)){
            org.json.JSONObject snapshot=libraryResume(e);
            if(snapshot!=null){entries.add(e);updates.put(e.id,snapshot.optLong("updated",0));}
        }
        java.util.Collections.sort(entries,(x,y)->Long.compare(updates.get(y.id),updates.get(x.id)));
        return entries;
    }
    private void renderLibraryBody(){
        header(libraryFilter>=0?PlaylumiWorldStyle.NAMES[libraryFilter]:"Werelden","");
        java.util.Set<String> played=librarySet("library_played");
        final int width=getResources().getConfiguration().screenWidthDp;
        final float fontScale=getResources().getConfiguration().fontScale;
        final int columns=PlaylumiLayout.libraryGameColumns(PlaylumiLayout.libraryContentWidth(width),fontScale);
        if(libraryFilter==PlaylumiGameCatalog.OVERVIEW){
            addText("✦  "+AGES[PlaylumiRocketCurriculum.level(age)]+"  ·  Kies jouw avontuur",15,PlaylumiMagicArt.CREAM,2,6);
            addExpeditionShortcut(true);
            librarySection("Onze speelwerelden","6 avonturen");
            int worldColumns=PlaylumiLayout.libraryWorldColumns(PlaylumiLayout.libraryContentWidth(width),fontScale);
            for(int start=0;start<6;start+=worldColumns){
                LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);row.setBaselineAligned(false);
                for(int i=start;i<Math.min(start+worldColumns,6);i++){
                    View card=libraryWorldCard(i,width);
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
                    lp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(card,lp);
                }
                root.addView(row,new LinearLayout.LayoutParams(-1,-2));
            }
            Button daily=tile("☀ Daguitdagingen",dailyChallenges().completed()+"/3 vandaag",0xffffe4bd,72);
            addTile(daily,7);daily.setOnClickListener(v->move(DAILY));
            // The original missions remain accessible from the expedition page.
            return;
        }
        libraryWorldHeader(libraryFilter,width);
        java.util.List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.filter(age,libraryFilter,java.util.Collections.emptySet());
        librarySection("Kies een spel",entries.size()+(entries.size()==1?" spel":" spellen"));
        if(entries.isEmpty()){
            addText("Kies een nieuw avontuur.",17,PlaylumiMagicArt.CREAM,12,8);
            Button discover=tile("Ontdek de werelden","",COLORS[2],64);
            addTile(discover,4);discover.setOnClickListener(v->libraryFilter(PlaylumiGameCatalog.OVERVIEW));
            return;
        }
        for(int first=0;first<entries.size();first+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);row.setBaselineAligned(false);
            int n=Math.min(columns,entries.size()-first);
            for(int j=0;j<n;j++){
                View card=libraryGameCard(entries.get(first+j),played,columns>1);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
                lp.setMargins(dp(4),dp(3),dp(4),dp(3));row.addView(card,lp);
            }
            for(int j=n;j<columns;j++)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1f));
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
    }
    private View libraryGameCard(PlaylumiGameCatalog.Entry entry,java.util.Set<String> played,boolean wide){
        LinearLayout card=new LinearLayout(this);card.setOrientation(wide?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(9),dp(8),dp(9),dp(8));
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),libraryCardSurface(entry.world),null));
        card.setMinimumHeight(dp(wide?138:78));
        org.json.JSONObject snapshot=libraryResume(entry);
        boolean resume=snapshot!=null;
        boolean completed=completedRoutes().contains(entry.id)&&PlaylumiCompletion.eligible(entry);
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon=text(entry.icon,entry.icon.length()>3?18:26,PlaylumiWorldStyle.deep(entry.world));
        icon.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        icon.setBackground(rounded(PlaylumiWorldStyle.pale(entry.world),16));icon.setIncludeFontPadding(false);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        heading.addView(icon,new LinearLayout.LayoutParams(dp(wide?50:46),dp(wide?50:46)));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.setPadding(dp(10),0,dp(5),0);
        TextView name=homeLabel(entry.title,17,PlaylumiMagicArt.CREAM,true);
        name.setGravity(Gravity.START);labels.addView(name,new LinearLayout.LayoutParams(-1,-2));
        int playedRounds=roundsPlayed(entry);
        String progress=resume?"Ronde "+snapshot.optInt("count",0)+"/"+snapshot.optInt("total",SESSION_ROUNDS):
                playedRounds>0?PlaylumiCompletion.mastery(playedRounds)+" · "+playedRounds+(playedRounds==1?" ronde":" rondes"):"";
        if(!wide){
            TextView description=homeLabel(entry.description,13,0xfff2e4ff,false);
            description.setPadding(0,dp(3),0,0);labels.addView(description,new LinearLayout.LayoutParams(-1,-2));
        }
        if(!progress.isEmpty()){
            TextView marker=homeLabel(progress,12,0xffc9ffdc,true);
            marker.setPadding(0,dp(2),0,0);labels.addView(marker,new LinearLayout.LayoutParams(-1,-2));
        }
        heading.addView(labels,new LinearLayout.LayoutParams(0,-2,1f));
        if(wide){
            card.addView(heading,new LinearLayout.LayoutParams(-1,-2));
            TextView description=homeLabel(entry.description,13,0xfff2e4ff,false);
            description.setMinLines(2);description.setPadding(dp(3),dp(5),dp(3),dp(4));
            card.addView(description,new LinearLayout.LayoutParams(-1,-2));
            TextView play=text("▶ Spelen",15,PlaylumiMagicArt.INK);
            play.setTypeface(Typeface.DEFAULT,Typeface.BOLD);play.setBackground(PlaylumiMagicArt.creamPlate(this));
            play.setMinHeight(dp(36));play.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(4),0,0);card.addView(play,lp);
        }else{
            card.addView(heading,new LinearLayout.LayoutParams(0,-2,1f));
            TextView play=text("▶",20,PlaylumiMagicArt.GOLD);
            play.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            card.addView(play,new LinearLayout.LayoutParams(dp(28),dp(42)));
        }
        libraryClick(card,(resume?"Verder spelen ":"Speel ")+entry.title+(completed?", opnieuw spelen":""),()->launchCatalog(entry,resume));
        if(resume)card.setOnLongClickListener(v->{
            new android.app.AlertDialog.Builder(this).setTitle(entry.title).setMessage("Opnieuw beginnen met dit spel? Je verdiende sterren en schatten blijven bewaard.")
                    .setNegativeButton("Verder spelen",null).setPositiveButton("Opnieuw beginnen",(d,which)->launchCatalog(entry,false)).show();
            return true;
        });
        return card;
    }

    private void worldChoice(String title,String description,int color,Runnable action) {
        Button b=tile(title,description,color,88);
        b.setTextSize(18);
        addTile(b,5);
        b.setOnClickListener(v->action.run());
    }
    private PlaylumiGameCatalog.Entry currentLearningEntry(){
        if(!libraryReturn||storyStage>=0||game==null)return null;
        return PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.QUIZ,game.mode(),game.variant());
    }
    private String learningKey(PlaylumiGameCatalog.Entry entry){
        return "learning_v1_"+PlaylumiRocketCurriculum.level(age)+"_"+entry.id+"_";
    }
    private PlaylumiLearningProfile learningProfile(PlaylumiGameCatalog.Entry entry){
        String key=learningKey(entry);int tier=PlaylumiRocketCurriculum.level(age);
        return new PlaylumiLearningProfile(tier,PlaylumiLearningProfile.minimum(tier,entry.mode,entry.variant),
                prefs().getInt(key+"level",tier),prefs().getInt(key+"bits",0),
                prefs().getInt(key+"samples",0),prefs().getInt(key+"total",0),prefs().getInt(key+"window",0));
    }
    private boolean catalogAdaptive(){return prefs().getBoolean("catalog_adaptive",true);}
    private int catalogLevel(PlaylumiGameCatalog.Entry entry){
        return catalogAdaptive()?learningProfile(entry).level:PlaylumiRocketCurriculum.level(age);
    }
    private void stageLearning(SharedPreferences.Editor edit,PlaylumiGameCatalog.Entry entry,boolean firstTry){
        PlaylumiLearningProfile data=learningProfile(entry);
        data.record(firstTry,catalogAdaptive());
        String key=learningKey(entry);
        edit.putInt(key+"level",data.level).putInt(key+"bits",data.bits)
                .putInt(key+"samples",data.samples).putInt(key+"total",data.total)
                .putInt(key+"window",data.window);
    }
    private java.util.List<PlaylumiGameCatalog.Entry> learningSuggestions(){
        java.util.List<PlaylumiGameCatalog.Entry> candidates=new ArrayList<>();
        java.util.Set<String> played=librarySet("library_played");
        final java.util.Map<String,Integer> priorities=new java.util.HashMap<>();
        for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age)){
            if(e.kind!=PlaylumiGameCatalog.QUIZ)continue;
            PlaylumiLearningProfile profile=learningProfile(e);
            int priority=profile.priority(played.contains(e.id));
            if(priority>0){candidates.add(e);priorities.put(e.id,priority);}
        }
        java.util.Collections.sort(candidates,(x,y)->Integer.compare(priorities.get(y.id),priorities.get(x.id)));
        ArrayList<PlaylumiGameCatalog.Entry> result=new ArrayList<>();
        boolean[] worlds=new boolean[6];
        for(PlaylumiGameCatalog.Entry e:candidates){
            if(worlds[e.world])continue;
            result.add(e);worlds[e.world]=true;if(result.size()==3)return result;
        }
        for(PlaylumiGameCatalog.Entry e:candidates){
            if(!result.contains(e))result.add(e);
            if(result.size()==3)break;
        }
        return result;
    }
    private String learningSuggestionReason(PlaylumiGameCatalog.Entry entry){
        PlaylumiLearningProfile data=learningProfile(entry);
        if(data.samples>=4&&data.firstTryPercent()<75)return "Samen nog even oefenen";
        return librarySet("library_played").contains(entry.id)?"Een ander avontuur proberen":"Ontdek een nieuw spel";
    }
    private void addLearningSuggestions(){
        java.util.List<PlaylumiGameCatalog.Entry> suggestions=learningSuggestions();
        if(suggestions.isEmpty())return;
        librarySection("Speciaal voor jou","Vrij om te kiezen");
        for(PlaylumiGameCatalog.Entry entry:suggestions){
            LinearLayout card=libraryPanel();card.setBackground(libraryCardSurface(entry.world));
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
            TextView icon=text(entry.icon,27,PlaylumiMagicArt.GOLD);
            icon.setBackground(PlaylumiMagicArt.gemButton(this,entry.world));
            row.addView(icon,new LinearLayout.LayoutParams(dp(46),dp(46)));
            LinearLayout label=new LinearLayout(this);label.setOrientation(LinearLayout.VERTICAL);label.setPadding(dp(8),0,dp(5),0);
            label.addView(homeLabel(entry.title,17,PlaylumiMagicArt.CREAM,true));
            label.addView(homeLabel(learningSuggestionReason(entry),13,0xffffdf9a,false));
            row.addView(label,new LinearLayout.LayoutParams(0,-2,1f));
            row.addView(text("▶",20,PlaylumiMagicArt.GOLD),new LinearLayout.LayoutParams(dp(30),-2));
            card.addView(row);libraryClick(card,"Speel "+entry.title,()->launchCatalog(entry,libraryResume(entry)!=null));
            addWidget(card,1,3);
        }
    }
    private void startWorldQuiz(int mode,int variant) {
        storyStage=-1;quickCategoryPlay=true;smartDifficulty=true;roundHadMistake=false;finalFeedbackPending=false;
        pendingLevel=-1;sessionSolved=0;sessionAttempts=0;feedback="";pendingBadges.clear();rewardReturn=PLAY;
        boolean rocket=villageLaunch==PlaylumiVillageHit.ROCKET && PlaylumiRocketCurriculum.isMathMode(mode);
        if(rocket||libraryReturn)smartDifficulty=false;
        PlaylumiGameCatalog.Entry selected=libraryReturn?PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.QUIZ,mode,variant):null;
        difficulty=selected!=null?catalogLevel(selected):rocket||libraryReturn?PlaylumiRocketCurriculum.level(age):adaptive.level(mode,Math.max(0,age));
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
        Button ageButton=tile("🎂 Leeftijd: "+PlaylumiProfiles.ageLabel(age),"Kies je leeftijd",0xfff1e9ff,91);
        addTile(ageButton,6);
        ageButton.setOnClickListener(v->move(AGE));
        Button profile=tile("✦ Lumi de superster","Kleding, huis en vriendjes",0xffdeeeff,91);
        addTile(profile,6);
        profile.setOnClickListener(v->move(AVATAR));
        Button soundButton=tile(sound.isEnabled()?"🔊 Geluid aan":"🔇 Geluid uit",
                "Tik om geluid te veranderen",0xffe1f4ec,87);
        addTile(soundButton,6);
        soundButton.setOnClickListener(v->{
            sound.setEnabled(!sound.isEnabled());
            prefs().edit().putBoolean("sound_on",sound.isEnabled()).apply();
            showPage();
        });
        addText("Playlumi "+BuildConfig.VERSION_NAME+" · testversie",13,SUB,4,2);
        Button parents=tile("👨‍👩‍👧 Voor ouders","Voortgang en extra instellingen",
                0xffffe9ca,91);
        addTile(parents,6);
        parents.setOnClickListener(v->requestParents());
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
        if(sessionSolved>=sessionGoal()&&(!game.round().isSolved()||quizPace()!=PlaylumiQuizPace.MANUAL||!finalFeedbackPending)) {showCompleted();return;}
        autoRoundVisible=true;
        LearningGame.Round round=game.round();
        boolean wide=landscapeScreen();
        int width=getResources().getConfiguration().screenWidthDp;
        int height=getResources().getConfiguration().screenHeightDp;
        boolean centered=PlaylumiLayout.centeredMagicQuiz(width,height);
        boolean split=wide&&!centered;
        int theme=worldTheme();
        PlaylumiGameCatalog.Entry currentEntry=PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.QUIZ,mode,game.variant());
        header(currentEntry!=null?currentEntry.title:mode==16&&game.variant()==3?"Tijd van de dag":LearningGame.GAME_NAMES[mode],sessionSolved+"/"+sessionGoal());
        addQuizBanner(theme,mode,game.variant(),height,wide);
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
        addPrompt(questions,round.prompt,wide?20:19);
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
        } else if(PlaylumiSkillVisual.supported(mode,game.variant(),round)) {
            skillBoard=new PlaylumiSkillBoard(this,mode,game.variant(),round);
            skillBoard.setBackground(PlaylumiMagicArt.parchment(this));
            skillBoard.setMinimumHeight(dp(questionH));
            addWidgetIn(questions,skillBoard,0,dp(3));
        } else if(PlaylumiPatterns.isVisualPattern(round.display)&&(mode==LearningGame.PATTERNS||mode==LearningGame.COLORS&&game.variant()==3)) {
            PlaylumiPatternView pattern=new PlaylumiPatternView(this,round.display);
            questionPattern=pattern;if(round.isSolved())pattern.complete(round.answers.get(round.correctIndex));
            pattern.setBackground(wide?PlaylumiMagicArt.parchment(this):PlaylumiMagicArt.question(this,false));
            pattern.setMinimumHeight(dp(Math.max(questionH,112)));
            addWidgetIn(questions,pattern,0,dp(3));
        } else if(mode==LearningGame.COLORS && "●".equals(round.display)) {
            PlaylumiColorSwatch swatch=new PlaylumiColorSwatch(this,round.displayColor,!wide);
            swatch.setMinimumHeight(dp(questionH));
            addWidgetIn(questions,swatch,0,dp(3));
        } else {
            boolean reading=mode==PlaylumiReadingPractice.MODE&&game.variant()==PlaylumiReadingPractice.VARIANT;
            boolean countingShapes=mode==PlaylumiFocusedCounting.MODE&&game.variant()==PlaylumiFocusedCounting.VARIANT;
            boolean weekdays=mode==PlaylumiWeekdays.MODE&&game.variant()==PlaylumiWeekdays.VARIANT;
            boolean oddOneOut=mode==PlaylumiOddOneOut.MODE&&game.variant()==PlaylumiOddOneOut.VARIANT;
            boolean shortStory=reading||mode==PlaylumiThinkingPractice.DAILY_THINGS&&game.variant()==PlaylumiThinkingPractice.AFTER;
            int glyphSize=shortStory?22:countingShapes?(game.level()==0?38:30):weekdays?28:oddOneOut?(round.display.length()>15?22:26):shortDisplay?(wide?70:58):
                    mode==ExtraGames.MATH&&game.variant()!=4?(game.variant()>=PlaylumiAdvancedMath.TABLES?(wide?42:32):(wide?48:36)):
                    round.display.length()>22?25:round.display.length()>12?31:40;
            TextView picture=text(round.visibleDisplay(),glyphSize,PlaylumiMagicArt.INK);
            questionPicture=picture;
            picture.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
            picture.setBackground(wide?PlaylumiMagicArt.parchment(this):PlaylumiMagicArt.question(this,!wide && shortDisplay));
            picture.setMinHeight(dp(questionH));
            picture.setPadding(dp(12),dp(5),dp(12),dp(5));
            picture.setMaxLines(getResources().getConfiguration().fontScale>=1.2f?12:shortStory||countingShapes?6:4);
            if(reading){picture.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);picture.setPadding(dp(18),dp(10),dp(18),dp(10));}
            updateQuestionPicture(round);
            addWidgetIn(questions,picture,0,dp(3));
        }
        feedbackText=text(round.isSolved()?(feedback.isEmpty()?"⭐ Goed gedaan!":feedback):"",15,PlaylumiMagicArt.CREAM);
        feedbackText.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        feedbackText.setMinHeight(dp(40));feedbackText.setMaxLines(3);feedbackText.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedbackText.setShadowLayer(dp(1),0,dp(1),0xff2e164c);
        quizFeedbackRow=new LinearLayout(this);quizFeedbackRow.setGravity(Gravity.CENTER_VERTICAL);
        quizFeedbackRow.addView(feedbackText,new LinearLayout.LayoutParams(0,-2,1f));
        quizNext=text("Verder ›",14,PlaylumiMagicArt.INK);quizNext.setMinHeight(dp(48));quizNext.setPadding(dp(6),0,dp(6),0);
        quizNext.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),PlaylumiMagicArt.creamPlate(this),null));
        LinearLayout.LayoutParams nextParams=new LinearLayout.LayoutParams(dp(78),dp(48));nextParams.leftMargin=dp(4);
        quizFeedbackRow.addView(quizNext,nextParams);
        quizNext.setContentDescription("Volgende vraag");quizNext.setFocusable(true);
        final FrameLayout quizScreen=stage;
        quizNext.setOnClickListener(view->{
            if(page!=PLAY||stage!=quizScreen||game.round()!=round||moving||activeSince<=0||!round.isSolved())return;
            advanceSolvedRound(PLAY,quizScreen,round);
        });
        addWidgetIn(answers,quizFeedbackRow,0,dp(2));
        updateQuizFeedback(round);
        answerButtons.clear();
        boolean visualPattern=PlaylumiPatterns.isVisualPattern(round.display)&&(mode==LearningGame.PATTERNS||mode==LearningGame.COLORS&&game.variant()==3);
        answerLocked=round.isSolved();
        int columns=PlaylumiQuizCoach.columns(round.answers,width,split,centered,visualPattern,getResources().getConfiguration().fontScale);
        int rowHeight=centered?PlaylumiLayout.centeredAnswerHeight(height,round.answers.size()):PlaylumiLayout.magicAnswerHeight(height,round.answers.size(),wide);
        if(split)rowHeight=Math.min(rowHeight,64);
        if(height<690)rowHeight=Math.max(48,Math.min(rowHeight,54));
        if(mode==ExtraGames.COMPARE&&game.variant()==PlaylumiThinkingPractice.EQUAL_GROUPS)rowHeight=Math.max(68,rowHeight);
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
                if(mode==ExtraGames.COMPARE&&game.variant()==PlaylumiThinkingPractice.EQUAL_GROUPS)button.setTextSize(23);
                button.setPadding(dp(6),dp(4),dp(6),dp(4));button.setMaxLines(getResources().getConfiguration().fontScale>=1.2f?6:4);
                button.setMinHeight(dp(rowHeight));button.setEnabled(!answerLocked&&!round.isRejected(j));
                if(visualPattern&&PlaylumiPatterns.colorIndex(value)>=0){
                    int paint=PlaylumiPatterns.color(PlaylumiPatterns.colorIndex(value));
                    GradientDrawable dot=new GradientDrawable();dot.setShape(GradientDrawable.OVAL);
                    dot.setColor(paint);dot.setStroke(dp(1),0xff573785);dot.setBounds(0,0,dp(22),dp(22));
                    button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),PlaylumiMagicArt.creamPlate(this),null));
                    button.setCompoundDrawables(dot,null,null,null);button.setCompoundDrawablePadding(dp(7));
                    button.setTextColor(PlaylumiMagicArt.INK);button.setTextSize(19);
                    button.setContentDescription(value);
                }
                if(mode==LearningGame.COLORS && (game.variant()==1||PlaylumiSkillPractice.colorChoices(mode,game.variant())) && round.answerColors.get(j)!=0) {
                    int paint=round.answerColors.get(j);
                    GradientDrawable colorPlate=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{mix(paint,Color.WHITE,.18f),paint,mix(paint,0xff38204b,.13f)});
                    colorPlate.setCornerRadius(dp(25));colorPlate.setStroke(dp(2),0xffffe2a1);
                    button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x38ffffff),colorPlate,null));
                    button.setTextColor(PlaylumiWorldStyle.contrastingInk(paint));
                    if(PlaylumiSkillPractice.colorChoices(mode,game.variant())){
                        button.setText("●");button.setContentDescription(value);
                    }
                }
                if(round.isRejected(j))markRejectedAnswer(button,value);
                if(round.isSolved()&&j==round.correctIndex)markCorrectAnswer(button,value,wide);
                else if(round.isSolved()&&!round.isRejected(j))button.setAlpha(.72f);
                // Let long answers and enlarged system text grow instead of clipping inside a fixed row.
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,-2,1f);
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

    private void markCorrectAnswer(Button button,String value,boolean wide){
        button.setText("✓ "+value);button.setSelected(true);button.setAlpha(1f);
        button.setContentDescription(value+", goed gekozen");
        if(game.mode()==LearningGame.COLORS&&(game.variant()==1||PlaylumiSkillPractice.colorChoices(game.mode(),game.variant()))){
            // Keep the actual chosen color visible instead of turning every correct color green.
            int paint=game.round().answerColors.get(game.round().correctIndex);
            GradientDrawable selected=new GradientDrawable();selected.setColor(paint);selected.setCornerRadius(dp(25));selected.setStroke(dp(3),0xffffe2a1);
            button.setBackground(selected);button.setTextColor(PlaylumiWorldStyle.contrastingInk(paint));return;
        }
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x35ffffff),
                wide?PlaylumiMagicArt.gemButton(this,3):PlaylumiMagicArt.pastel(this,3),null));
        button.setTextColor(0xff174f36);
    }
    private void markRejectedAnswer(Button button,String value){
        button.setEnabled(false);button.setAlpha(.52f);
        button.setText(PlaylumiSkillPractice.colorChoices(game.mode(),game.variant())?"×":"× "+value);
        button.setContentDescription(value+", al geprobeerd");
    }
    private void updateQuizFeedback(LearningGame.Round round){
        if(quizFeedbackRow==null)return;
        boolean solved=round.isSolved(),retry=round.rejectedMask()!=0&&!solved;
        quizFeedbackRow.setVisibility(solved||retry?View.VISIBLE:View.GONE);
        quizNext.setVisibility(solved?View.VISIBLE:View.GONE);
        if(retry){
            feedbackText.setText(PlaylumiQuizCoach.RETRY_MESSAGE);
            feedbackText.setTextColor(0xffffe5b3);
        }else if(solved)feedbackText.setTextColor(0xffc6ffd6);
    }
    private int quizPace(){return PlaylumiQuizPace.safe(prefs().getInt("quiz_pace",0));}

    private void updateQuestionPicture(LearningGame.Round round){
        if(questionPicture==null)return;
        String shown=round.visibleDisplay();
        if(round.isSolved()&&PlaylumiSkillPractice.revealsBlank(game.mode(),game.variant()))shown=shown.replace("?",round.answers.get(round.correctIndex));
        if(round.isSolved()&&game.mode()==ExtraGames.SPELLING&&PlaylumiSpellingPractice.isContext(round.display))
            shown=shown.replace("…",round.answers.get(round.correctIndex));
        if(round.isSolved()&&((game.mode()==ExtraGames.MATH&&game.variant()!=4)||game.mode()==ExtraGames.SEQUENCES||
                game.mode()==23&&(game.variant()==0||game.variant()==1||game.variant()==3)))
            shown=shown.replace("?",round.answers.get(round.correctIndex));
        int blank=round.letterBlank();
        if(round.isSolved()&&game.mode()==ExtraGames.SPELLING&&PlaylumiSpellingPractice.isContext(round.display)){
            int start=round.display.indexOf('…');
            int end=start+round.answers.get(round.correctIndex).length();
            android.text.SpannableString marked=new android.text.SpannableString(shown);
            marked.setSpan(new android.text.style.ForegroundColorSpan(0xff237b49),start,end,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            marked.setSpan(new android.text.style.UnderlineSpan(),start,end,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            questionPicture.setText(marked);
        }else if(round.isSolved() && blank>=0){
            int end=blank+round.blankReplacement().length();
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
        int delay=expected==PLAY?PlaylumiQuizPace.delay(quizPace(),roundHadMistake,feedback):700;
        if(delay<0)return;
        screen.postDelayed(()->{
            if(token==autoAdvanceToken)advanceSolvedRound(expected,screen,solved);
        },delay);
    }
    private void advanceSolvedRound(int expected,FrameLayout screen,LearningGame.Round solved){
        if(isFinishing()||isDestroyed()||activeSince<=0||stage!=screen||page!=expected||moving||!solvedAutoRound()||
                (expected==PLAY&&game.round()!=solved))return;
        autoAdvanceToken++;autoRoundVisible=false;
        if(!pendingBadges.isEmpty()&&expected!=PLAY)
            android.widget.Toast.makeText(this,"🏅 Nieuwe badge verdiend!",android.widget.Toast.LENGTH_SHORT).show();
        pendingBadges.clear();advancingAutomatically=true;
        try{
            switch(expected){
                case PLAY:continueAfterAnswer();break;
                case MINI_PLAY:afterMiniSolve();break;
                case BUBBLE_PLAY:finishBubbleRound();break;
                case STAR_CATCH:finishStarCatchRound();break;
                case MUSIC_PLAY:finishMelodyRound();break;
            }
        }finally{advancingAutomatically=false;}
    }
    /** Fixed completion actions, outside the scrolling game content. */
    private LinearLayout finishActions(Button again,Button back,boolean visible){
        describeCatalogReturn(back);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER);bar.setPadding(dp(6),dp(4),dp(6),dp(4));
        bar.setBackground(PlaylumiMagicArt.primary(this));
        boolean expedition=libraryReturn&&catalogReturn==EXPEDITION&&storyStage<0;
        for(Button b:expedition?new Button[]{back}:new Button[]{again,back}){
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
        if(page!=PLAY||moving||!autoRoundVisible||answerLocked||game.round().isSolved()||game.round().isRejected(index))return;
        int mode=game.mode();
        if(index<0||index>=game.round().answers.size())return;
        // One preference transaction per answer: a crash must not persist the
        // reward without also persisting the answer and its session snapshot.
        SharedPreferences.Editor answerEdit=prefs().edit();
        sessionAttempts++;
        boolean correct=game.round().choose(index);
        ArrayList<Integer> newBadges=new ArrayList<>(scores.answer(mode,correct));
        int dailyReward=0;
        if(correct) {
            answerLocked=true;sessionSolved++;lifetimeCount++;
            updateQuizFeedback(game.round());
            if(questionPattern!=null)questionPattern.complete(game.round().answers.get(game.round().correctIndex));
            if(skillBoard!=null)skillBoard.refresh();
            if(game.round().letterBlank()>=0 || PlaylumiSkillPractice.revealsBlank(game.mode(),game.variant()) || game.mode()==ExtraGames.MATH||game.mode()==ExtraGames.SEQUENCES||game.mode()==23||
                    game.mode()==ExtraGames.SPELLING&&PlaylumiSpellingPractice.isContext(game.round().display)){
                updateQuestionPicture(game.round());
                if(questionPicture!=null)PlaylumiMotion.pop(questionPicture);
            }
            String explanation=PlaylumiQuizCoach.explain(mode,game.variant(),game.round());
            feedback=explanation.isEmpty()?"⭐ Goed gedaan! +3 🌟":"⭐ "+explanation;
            PlaylumiGameCatalog.Entry learningEntry=currentLearningEntry();
            if(learningEntry!=null){
                stageLearning(answerEdit,learningEntry,!roundHadMistake);
            }else if(smartDifficulty && storyStage<0) {
                if(adaptive.observe(mode,!roundHadMistake,Math.max(0,age))) {
                    pendingLevel=adaptive.level(mode,Math.max(0,age));
                    feedback="🌟 Wat knap! Een nieuwe uitdaging!";
                }
                stageAdaptive(answerEdit,mode);
            }
            sound.good();
            if(playPal!=null)playPal.dance();
            markCorrectAnswer(touched,game.round().answers.get(index),landscapeScreen());
            for(Button b:answerButtons){b.setEnabled(false);if(b!=touched&&b.getAlpha()>.52f)b.setAlpha(.72f);}
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
                finalFeedbackPending=true;
                if(storyStage>=0) {
                    storyNewReward=adventures.complete(world,storyStage);
                    if(storyNewReward) {
                        wardrobe.earnAdventureBonus();
                        answerEdit.putInt("adventure_progress_"+world,adventures.progress(world));
                    }
                } else newBadges.addAll(scores.finishSession(mode));
            }
            wardrobe.earnAnswer();
            wardrobe.earnBadges(newBadges.size());
            dailyReward=stageDaily(answerEdit,learningEntry,sessionSolved==sessionGoal());
            stageWardrobe(answerEdit);
            if(storyStage<0)stageCompletion(answerEdit,learningEntry,sessionSolved,sessionGoal());
            pendingBadges.addAll(newBadges);
            if(!newBadges.isEmpty())feedbackText.setText(feedback+"  🏅");
            scheduleAutoAdvance();
            feedbackText.announceForAccessibility(feedback);
        } else {
            feedback="";
            roundHadMistake=true;
            markRejectedAnswer(touched,game.round().answers.get(index));
            updateQuizFeedback(game.round());
            sound.mistake();
            if(playPal!=null)playPal.react(2);
            PlaylumiDelight.wiggle(touched);
            touched.announceForAccessibility("Probeer een ander antwoord.");
        }
        stageScore(answerEdit,mode);stageLibraryQuiz(answerEdit);answerEdit.apply();
        if(dailyReward>0)android.widget.Toast.makeText(this,"☀ Daguitdaging! +"+dailyReward+" sterren",android.widget.Toast.LENGTH_SHORT).show();
    }
    private void advanceRegularRound() {
        PlaylumiGameCatalog.Entry entry=currentLearningEntry();
        if(entry!=null){
            int next=catalogLevel(entry);
            if(game.level()!=next){difficulty=next;game.configure(entry.mode,entry.variant,next);}else game.nextRound();
            pendingLevel=-1;
        }else if(smartDifficulty && storyStage<0 && pendingLevel>=0) {
            difficulty=pendingLevel;
            game.configure(game.mode(),game.variant(),difficulty);
            pendingLevel=-1;
        } else game.nextRound();
        roundHadMistake=false;finalFeedbackPending=false;feedback="";
        saveRecent();saveLibraryQuiz();
    }
    private void continueAfterAnswer() {
        finalFeedbackPending=false;
        if(!pendingBadges.isEmpty()) {
            earnedBadge=pendingBadges.remove(0);move(REWARD);return;
        }
        if(sessionSolved<sessionGoal()) advanceRegularRound();
        feedback="";
        move(PLAY);
    }
    private PlaylumiGameCatalog.Entry completedPanelEntry(){
        if(!libraryReturn||storyStage>=0)return null;
        if(page==PLAY)return currentLearningEntry();
        if(page==INTERACTIVE&&interactive!=null)
            return PlaylumiGameCatalog.route(age,villageLaunch,PlaylumiGameCatalog.INTERACTIVE,interactive.kind,0);
        return specialEntry(page);
    }
    /** Presentation only: never award currency or increment progress while rendering. */
    private LinearLayout completionPanel(String title,String icon,int solved,int rewarded){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12),dp(12),dp(12),dp(10));
        GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xfffff9e4,0xffffe6ac,0xffeee0ff});
        gradient.setCornerRadius(dp(22));gradient.setStroke(dp(2),0xffdca953);card.setBackground(gradient);
        int width=getResources().getConfiguration().screenWidthDp,height=getResources().getConfiguration().screenHeightDp;
        boolean side=PlaylumiCompletion.sideBySide(width,height);
        LinearLayout heading=new LinearLayout(this);heading.setOrientation(side?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);heading.setGravity(Gravity.CENTER);
        PlaylumiMedal medal=new PlaylumiMedal(this,icon,true,Math.max(0,worldTheme())%3);
        int size=PlaylumiCompletion.medalSize(height);
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(size),dp(size));mp.gravity=Gravity.CENTER;
        heading.addView(medal,mp);
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setGravity(Gravity.CENTER);
        if(side)heading.addView(labels,new LinearLayout.LayoutParams(0,-2,1f));else heading.addView(labels,new LinearLayout.LayoutParams(-1,-2));
        homeAdd(labels,text("Ronde gespeeld!",21,0xff583477),0,2);
        homeAdd(labels,text(title,17,0xff583477),0,3);
        homeAdd(labels,text(solved+" / "+solved+" in deze ronde",16,0xff27684d),0,3);
        card.addView(heading,new LinearLayout.LayoutParams(-1,-2));
        PlaylumiStarTrail stars=new PlaylumiStarTrail(this,Math.max(1,Math.min(12,solved)),Math.max(1,Math.min(12,solved)));
        stars.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        card.addView(stars,new LinearLayout.LayoutParams(-1,dp(30)));
        LinearLayout totals=new LinearLayout(this);totals.setGravity(Gravity.CENTER);totals.setOrientation(LinearLayout.HORIZONTAL);
        String[] values={"+ "+PlaylumiCompletion.baseStars(rewarded)+" ★",""+wardrobe.stars()+" ★"};
        String[] labelsText={"Basisbeloning","Jouw sterrenpot"};
        for(int j=0;j<2;j++){
            LinearLayout metric=new LinearLayout(this);metric.setOrientation(LinearLayout.VERTICAL);metric.setGravity(Gravity.CENTER);
            metric.setPadding(dp(3),dp(7),dp(3),dp(7));metric.setBackground(rounded(0xfffff8e8,12));
            homeAdd(metric,text(values[j],22,0xff855b24),0,1);homeAdd(metric,text(labelsText[j],12,0xff67477c),0,0);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);totals.addView(metric,lp);
        }
        homeAdd(card,totals,4,3);
        homeAdd(card,text("Je sterren zijn al opgeslagen. Extra bonussen en badges zitten ook in je sterrenpot.",12,0xff60446f),2,1);
        PlaylumiGameCatalog.Entry entry=completedPanelEntry();
        if(entry!=null){
            int mission=expeditionProgress();
            if(catalogReturn==EXPEDITION&&mission>0&&entry.id.equals(PlaylumiExpedition.routeId(age,mission-1))){
                LinearLayout adventure=personalPanel();
                homeAdd(adventure,homeLabel("✦ Expeditieopdracht "+mission+" van 30 afgerond",18,0xff27684d,true),0,3);
                if(mission%PlaylumiExpedition.STAGES_PER_CHAPTER==0){
                    int chapter=mission/PlaylumiExpedition.STAGES_PER_CHAPTER-1;
                    homeAdd(adventure,homeLabel("✓ "+PlaylumiExpedition.STAMPS[chapter]+" verdiend",17,TEXT,true),0,2);
                    homeAdd(adventure,homeLabel(mission==30?"🏆 Alle zes zegels! +45 extra sterren.":"Hoofdstuk klaar! +15 extra sterren.",15,0xff715021,true),0,2);
                }else homeAdd(adventure,homeLabel("De volgende opdracht is vrijgespeeld.",14,SUB,false),0,2);
                parentProgressBar(adventure,30,mission,0xff9670cf,mission+" van 30 expeditieopdrachten afgerond");
                homeAdd(card,adventure,5,3);
            }
            int rounds=roundsPlayed(entry);
            if(rounds>0){
                homeAdd(card,text("🏅 "+PlaylumiCompletion.mastery(rounds)+" · "+rounds+(rounds==1?" ronde gespeeld":" rondes gespeeld"),18,0xff583477),7,3);
                if(rounds<20){
                    int goal=PlaylumiCompletion.nextMastery(rounds),left=goal-rounds;
                    ProgressBar mastery=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
                    mastery.setMax(goal);mastery.setProgress(rounds);mastery.setProgressTintList(ColorStateList.valueOf(0xffa87724));
                    mastery.setContentDescription("Meesterschap: "+rounds+" van "+goal+" rondes");
                    card.addView(mastery,new LinearLayout.LayoutParams(-1,dp(9)));
                    homeAdd(card,text("Nog "+left+(left==1?" ronde":" rondes")+" tot "+PlaylumiCompletion.mastery(goal)+". Speel gerust op je eigen tempo.",13,SUB),4,2);
                }else homeAdd(card,text("Diamant behaald! Elk nieuw rondje blijft speelsterren geven.",13,SUB),3,2);
            }
            long cityMask=achievedMask(achievementStats());
            int news=Integer.bitCount(PlaylumiCityProgress.unseen(cityMask,prefs().getInt("city_seen_v1",0)));
            if(news>0){
                Button city=tile("Je stad is gegroeid!",news+(news==1?" nieuwe plek":" nieuwe plekken")+" en een cadeau om te bekijken",0xffffe4ae,72);
                city.setContentDescription("Bekijk nieuwe plekken in Lumi's stad");
                homeAdd(card,city,6,2);city.setOnClickListener(v->move(CITY));
            }
        }
        return card;
    }
    private void showCompleted() {
        final FrameLayout finishedScreen=stage;
        finishedScreen.post(()->{
            if(stage==finishedScreen)PlaylumiMotion.celebrate(stage,10);
        });
        if(storyStage>=0) {
            header("Avontuur klaar!","");
            addWidget(completionPanel(PlaylumiAdventure.TITLES[world][storyStage],PlaylumiAdventure.ICONS[world][storyStage],sessionGoal(),sessionGoal()),3,5);
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
        header("Ronde gespeeld!","");
        PlaylumiGameCatalog.Entry entry=currentLearningEntry();
        addWidget(completionPanel(entry==null?LearningGame.GAME_NAMES[game.mode()]:entry.title,
                PlaylumiPremiumLayout.emblem(game.mode(),game.variant()),sessionGoal(),sessionGoal()),3,6);
        characterHero(102);
        addText(libraryReturn&&catalogReturn==EXPEDITION?"Keer terug naar je avonturenkaart. Je voortgang is bewaard.":"Elke nieuwe ronde heeft nieuwe vragen. Speel nog eens voor sterren en meesterschap, of kies een ander avontuur.",15,PlaylumiMagicArt.CREAM,0,8);
        Button again=tile("🔄 Opnieuw spelen","Nieuwe ronde, nieuwe sterren",0xffcbf1d9,92);
        PlaylumiDelight.invite(again);
        again.setOnClickListener(v->{
            sessionSolved=0;sessionAttempts=0;
            advanceRegularRound();move(PLAY);
        });
        Button home=tile(catalogReturn==DAILY?"☀ Daguitdagingen":catalogReturn==CITY?"⌂ Mijn stad":"🌈 Ander spelletje","",0xffeadbff,82);
        home.setOnClickListener(v->move(libraryReturn?catalogReturnPage():PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:CATEGORIES));
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
        PlaylumiMedal badgeIcon=new PlaylumiMedal(this,PlaylumiScores.BADGE_ICONS[earnedBadge],true,earnedBadge%3);
        badgeIcon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        badgeIcon.setContentDescription("Je hebt een nieuwe badge! Tik om te fonkelen.");
        badgeIcon.setFocusable(true);badgeIcon.setClickable(true);
        LinearLayout badgeHolder=new LinearLayout(this);badgeHolder.setGravity(Gravity.CENTER);
        int medalSize=PlaylumiCompletion.medalSize(getResources().getConfiguration().screenHeightDp)+20;
        badgeHolder.addView(badgeIcon,new LinearLayout.LayoutParams(dp(medalSize),dp(medalSize)));
        addWidget(badgeHolder,10,5);
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
        header("Lumi de superster","Jouw maatje in alle speelwerelden");personalSummary(false);
        long mask=achievedMask(achievementStats());
        Button city=tile("⌂ Lumi's groeiende stad",PlaylumiCityRewards.count(mask)+" van 7 plekken vrijgespeeld",0xffffe4ae,82);
        city.setContentDescription("Open Lumi's stad");addTile(city,5);city.setOnClickListener(v->move(CITY));
        Button daily=tile("☀ Daguitdagingen",dailyChallenges().completed()+"/3 vandaag behaald",0xffffe4bd,82);
        daily.setContentDescription("Dagelijkse uitdagingen");
        addTile(daily,5);daily.setOnClickListener(v->move(DAILY));
        Button style=tile("🎀 Kledingkast","Kies accessoires",0xffe9dcff,92);style.setOnClickListener(v->{closetReturn=AVATAR;move(CLOSET);});
        Button choose=tile("🐾 Vriendjespark","Ontmoet Lumi's vriendjes",0xffe4d4ff,92);choose.setOnClickListener(v->{friendReturn=AVATAR;move(FRIEND_CARE);});
        Button stageButton=tile("🎭 Danspodium","Laat Lumi bewegen",0xffffe8b8,92);stageButton.setOnClickListener(v->move(STAR_STAGE));
        Button home=tile("🏠 Mijn kamer","Versier jouw kamer",0xffd9ecff,92);home.setOnClickListener(v->move(ROOM));
        Button trophies=tile("🏆 Mijn badges","Jouw prestaties",0xffffeab9,92);
        trophies.setOnClickListener(v->{villageLaunch=PlaylumiVillageView.FRIEND;move(TROPHY_WORLDS);});
        Button album=tile("✦ Mijn schatten",collectionTotal()+" van 18 verzameld",0xffffe4bd,92);album.setOnClickListener(v->{collectionReturn=AVATAR;move(COLLECTIBLES);});
        Button achievements=tile("🏆 Mijn prestaties",achievementCount()+"/"+PlaylumiAchievements.ALL.length+" behaald",0xffffe4bd,92);
        achievements.setOnClickListener(v->openAchievements(AVATAR));
        personalActionGrid(achievements,style,choose,stageButton,home,trophies,album);
    }
    private void showCity(){
        header("Lumi's groeiende stad","Elke verdiende plek maakt jouw stad mooier");
        int[] stats=achievementStats();long mask=achievedMask(stats);
        int built=PlaylumiCityRewards.count(mask);
        LinearLayout summary=personalPanel();
        homeAdd(summary,text(built+" / "+PlaylumiCityRewards.ALL.length+" plekken · ★ "+wardrobe.stars(),21,TEXT),0,3);
        parentProgressBar(summary,PlaylumiCityRewards.ALL.length,built,0xff9670cf,built+" van "+PlaylumiCityRewards.ALL.length+" stadsplekken vrijgespeeld");
        homeAdd(summary,text("Tik op een plek om te ontdekken wat je er kunt doen en welk cadeau erbij hoort.",14,SUB),0,2);
        int unseen=PlaylumiCityProgress.unseen(mask,prefs().getInt("city_seen_v1",0));
        if(unseen!=0){
            ArrayList<String> names=new ArrayList<>();
            for(int i=0;i<PlaylumiCityRewards.ALL.length;i++)if((unseen&(1<<i))!=0)names.add(PlaylumiCityRewards.ALL[i].name);
            TextView news=text("Nieuw in je stad: "+android.text.TextUtils.join(", ",names),15,0xff27684d);
            news.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);homeAdd(summary,news,4,0);
            prefs().edit().putInt("city_seen_v1",prefs().getInt("city_seen_v1",0)|PlaylumiCityProgress.builtMask(mask)).apply();
        }
        addWidget(summary,2,5);
        addExpeditionShortcut(false);
        PlaylumiCityMapView map=new PlaylumiCityMapView(this,mask,PlaylumiMotion.isReduced(),this::showCityLandmark);
        addWidget(map,0,5);
        int next=PlaylumiCityProgress.next(mask,stats);
        if(next>=0){
            PlaylumiCityRewards.Landmark landmark=PlaylumiCityRewards.ALL[next];
            PlaylumiAchievements.Milestone milestone=PlaylumiAchievements.ALL[landmark.achievement];
            LinearLayout target=personalPanel();
            homeAdd(target,text("Een volgende ontdekking: "+landmark.name,19,TEXT),0,4);
            int progress=PlaylumiCityProgress.progress(next,mask,stats);
            homeAdd(target,text(milestone.description+" · "+progress+" / "+milestone.goal,15,SUB),0,4);
            parentProgressBar(target,milestone.goal,progress,0xff9670cf,landmark.name+", "+progress+" van "+milestone.goal);
            Button open=tile("Bekijk deze plek","Cadeau: "+landmark.gift(),0xffd5f5df,68);
            target.addView(open,new LinearLayout.LayoutParams(-1,-2));open.setOnClickListener(v->showCityLandmark(next));
            addWidget(target,3,5);
        }else addText("Alle plekken zijn ontdekt! Je kunt overal blijven spelen.",19,TEXT,3,6);
        Button closet=tile("🎀 Kledingkast","Trek je verdiende outfits aan",0xffe9dcff,80);
        closet.setContentDescription("Open kledingkast");closet.setOnClickListener(v->{closetReturn=CITY;move(CLOSET);});
        Button daily=tile("☀ Daguitdagingen",dailyChallenges().completed()+" van 3 vandaag",0xffffe8b8,80);
        daily.setContentDescription("Dagelijkse uitdagingen");daily.setOnClickListener(v->move(DAILY));
        Button achievements=tile("🏆 Mijn prestaties","Bekijk je voortgang en cadeaus",0xffd5f5df,80);
        achievements.setOnClickListener(v->openAchievements(CITY));
        Button lumi=tile("★ Naar Lumi","Kamer, dansen en vriendjes",0xffd9ecff,80);lumi.setOnClickListener(v->move(AVATAR));
        personalActionGrid(closet,daily,achievements,lumi);
    }
    private void showCityLandmark(int index){
        if(page!=CITY||index<0||index>=PlaylumiCityRewards.ALL.length)return;
        PlaylumiCityRewards.Landmark landmark=PlaylumiCityRewards.ALL[index];
        int[] stats=achievementStats();long mask=achievedMask(stats);
        boolean earned=PlaylumiCityRewards.earned(mask,landmark);
        PlaylumiAchievements.Milestone milestone=PlaylumiAchievements.ALL[landmark.achievement];
        String[] scenes={"Een groene plek voor Lumi en zijn vriendjes.","Vanuit deze sterrenkoepel gaat Lumi op rekenavontuur.",
                "Een magische toren vol letters en verhalen.","Een feestelijk podium voor melodieën en muziek.",
                "Een vrolijk park voor alle ontdekte dierenvriendjes.","Alle kleuren komen samen in deze regenboogbrug.",
                "Een gouden herinnering aan jouw achttien schatten."};
        String message=scenes[index]+"\n\n"+(earned?"✓ Vrijgespeeld! Je cadeau "+landmark.gift()+" staat in je kledingkast.":
                "Speel deze plek vrij: "+landmark.requirement()+".\n"+milestone.description+" · "+PlaylumiCityProgress.progress(index,mask,stats)+" / "+milestone.goal+
                "\n\nCadeau: "+landmark.gift()+". "+(wardrobe.owned(landmark.slot,landmark.item)?"Je hebt dit cadeau al; het blijft van jou.":"Zodra je deze plek verdient, krijg je dit cadeau erbij."));
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(this)
                .setTitle(landmark.icon+" "+landmark.name).setMessage(message)
                .setNegativeButton("Terug naar de stad",null);
        if(earned){
            dialog.setNeutralButton("Bekijk je cadeau",(d,w)->{selectedSlot=landmark.slot;closetReturn=CITY;move(CLOSET);});
            dialog.setPositiveButton(index==0||index==4?"Naar het vriendjespark":landmark.world>=0?"Spelen in deze wereld":"Bekijk je schatten",(d,w)->{
                if(index==0||index==4){friendReturn=CITY;move(FRIEND_CARE);}
                else if(landmark.world>=0)openLibrary(landmark.world);
                else{collectionReturn=CITY;move(COLLECTIBLES);}
            });
        }else dialog.setPositiveButton(index==0?"Speel een quizronde":landmark.world>=0?"Spelen in deze wereld":"Bekijk de schatten",(d,w)->{
            if(index==0){
                PlaylumiGameCatalog.Entry entry=dailyRoute(dailyChallenges(),2);
                if(entry!=null){libraryFilter=entry.world;launchCatalog(entry,libraryResume(entry)!=null,CITY);}
            }else if(landmark.world>=0)openLibrary(landmark.world);
            else{collectionReturn=CITY;move(COLLECTIBLES);}
        });
        dialog.show();
    }
    private void invalidateAvatars(View view){
        if(view instanceof PlaylumiAvatar)view.invalidate();
        if(view instanceof android.view.ViewGroup){
            android.view.ViewGroup group=(android.view.ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)invalidateAvatars(group.getChildAt(i));
        }
    }
    private void showCloset(){
        header("Mijn kledingkast","Maak Lumi helemaal uniek.");
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
        Button done=tile("Klaar!",closetReturn==SHOP?"Terug naar je beloningen":"Terug naar Lumi",0xffd5f5df,70);
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
                PlaylumiCityRewards.Landmark reward=PlaylumiCityRewards.forItem(slot,i);
                String detail=equipped?"✓ In gebruik":unlocked?"In bezit":reward!=null?"Verdien: "+reward.requirement():"★ "+wardrobe.price(slot,i)+" sterren";
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
                    if(result==PlaylumiWardrobe.ACHIEVEMENT_REQUIRED){feedback.setText("Deze beloning verdien je met een prestatie.");return;}
                    saveWardrobe();refresh.run();
                    feedback.setText(result==PlaylumiWardrobe.PURCHASED?"✨ Nieuw accessoire vrijgespeeld!":"✨ Wat staat dat mooi!");
                    sound.equip();
                    if(character!=null){character.wave();PlaylumiDelight.sprinkle(stage,character);}
                };
                b.setOnClickListener(v->{
                    if(wardrobe.owned(slot,item)){equip.run();return;}
                    PlaylumiCityRewards.Landmark reward=PlaylumiCityRewards.forItem(slot,item);
                    if(reward!=null){feedback.setText("Behaal "+reward.requirement()+" om "+reward.gift()+" vrij te spelen.");return;}
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
        Button achievements=tile("🏆 Mijn prestaties",achievementCount()+"/"+PlaylumiAchievements.ALL.length+" behaald",0xffffe4bd,76);
        addTile(achievements,4);achievements.setOnClickListener(v->openAchievements(COLLECTIBLES));
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
        Button closet=tile("🎀 Kledingkast","Kleed Lumi aan",0xffe8dcff,92);closet.setOnClickListener(v->{closetReturn=SHOP;move(CLOSET);});
        Button treasures=tile("✦ Mijn schatten",collectionTotal()+" van 18 verzameld",0xffffe4bd,92);treasures.setOnClickListener(v->{collectionReturn=SHOP;move(COLLECTIBLES);});
        Button roomButton=tile("🏠 Mijn kamer","Versier jouw kamer",0xffd9ecff,92);roomButton.setOnClickListener(v->move(ROOM));
        Button achievements=tile("🏆 Mijn prestaties",achievementCount()+"/"+PlaylumiAchievements.ALL.length+" behaald",0xffffe4bd,92);
        achievements.setOnClickListener(v->openAchievements(SHOP));
        Button badges=tile("🏅 Mijn badges","Jouw bestaande badges",0xffffe8b8,92);
        badges.setOnClickListener(v->{villageLaunch=PlaylumiVillageView.COLLECTION;move(TROPHY_WORLDS);});
        personalActionGrid(achievements,closet,treasures,roomButton,badges);
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
            addWidget(completionPanel(miniTitle,miniKind==PlaylumiMiniChallenge.MEMORY?"🐾":miniKind==PlaylumiMiniChallenge.DRAG?"◆":"🎈",20,20),3,6);
            Button again=tile("🔄 Nog eens spelen!","",0xffd4f5e0,96);
            again.setOnClickListener(v->{
                miniRound=0;miniSolved=false;
                mini=PlaylumiMiniChallenge.generate(miniKind,Math.max(0,age),new Random());
                move(MINI_PLAY);
            });
            Button back=tile("🎪 Andere minigame","",0xffe8dfff,86);
            back.setOnClickListener(v->move(libraryReturn?catalogReturnPage():PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,back,true);
            return;
        }
        autoRoundVisible=true;
        String instruction=miniKind==PlaylumiMiniChallenge.MEMORY?PlaylumiMiniPractice.theme(mini.choices)+" · vind de paren":miniKind==PlaylumiMiniChallenge.DRAG?"Sleep naar de vormpoort":mini.question;
        addPrompt(root,instruction,19);
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
            @Override public void onStateChanged(){saveLibrarySpecial(MINI_PLAY);}
        });
        LinearLayout.LayoutParams boardParams=new LinearLayout.LayoutParams(-1,-2);
        boardParams.setMargins(dp(0),dp(2),dp(0),dp(4));
        root.addView(board,boardParams);
        if(miniSolved)board.restoreSolvedShape();
        if(miniSolved)scheduleAutoAdvance();
    }
    private void recordMiniAnswer() {
        int category=miniCategory();
        ArrayList<Integer> newBadges=new ArrayList<>(scores.answer(category,true));
        lifetimeCount++;
        wardrobe.earnAnswer();
        wardrobe.earnBadges(newBadges.size());
        SharedPreferences.Editor edit=prefs().edit();
        stageWardrobe(edit);stageScore(edit,category);
        stageCompletion(edit,specialEntry(MINI_PLAY),miniRound+1,20);
        stageLibrarySpecial(edit,MINI_PLAY);edit.apply();
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
        PlaylumiRoomView view=new PlaylumiRoomView(this,room,chosenAvatar(),wardrobe);
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
        Button friend=tile("Terug naar Lumi","",0xffe8dcff,77);
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
        header("Tekenstudio","");
        addText("Teken met je vinger. Probeer, gum en probeer opnieuw.",14,PlaylumiMagicArt.CREAM,1,5);
        drawing=new PlaylumiDrawingView(this,new File(getFilesDir(),PlaylumiProfiles.artworkFile(activeProfile)));
        drawingColor=Math.max(0,Math.min(PlaylumiDrawingView.PALETTE.length-1,prefs().getInt("drawing_color_v1",0)));
        drawingWidth=Math.max(0,Math.min(2,prefs().getInt("drawing_brush_size_v1",1)));
        drawing.color(PlaylumiDrawingView.PALETTE[drawingColor]);
        drawing.setBrushSize(drawingWidth);
        drawing.setEraser(prefs().getBoolean("drawing_eraser_v1",false));
        drawing.setElevation(dp(5));
        int width=getResources().getConfiguration().screenWidthDp;
        float fontScale=getResources().getConfiguration().fontScale;
        int available=width-2*PlaylumiLayout.magicSidePadding(width);
        boolean alongside=width>=840&&fontScale<=1.25f;
        LinearLayout workspace=new LinearLayout(this);
        workspace.setOrientation(alongside?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
        workspace.setGravity(Gravity.TOP);workspace.setBaselineAligned(false);
        workspace.setClipChildren(false);workspace.setClipToPadding(false);
        int canvasHeight=alongside?Math.max(225,Math.min(360,(available-312)*3/4)):
                PlaylumiLayout.drawingHeight(landscapeScreen(),getResources().getConfiguration().screenHeightDp);
        LinearLayout.LayoutParams drawParams=alongside?new LinearLayout.LayoutParams(0,dp(canvasHeight),1f):
                new LinearLayout.LayoutParams(-1,dp(canvasHeight));
        drawParams.setMargins(dp(4),dp(8),dp(alongside?12:4),dp(12));
        workspace.addView(drawing,drawParams);
        LinearLayout controls=personalPanel();
        controls.setClipChildren(false);controls.setClipToPadding(false);
        LinearLayout.LayoutParams controlsParams=new LinearLayout.LayoutParams(alongside?dp(300):-1,-2);
        controlsParams.topMargin=dp(8);workspace.addView(controls,controlsParams);
        root.addView(workspace,new LinearLayout.LayoutParams(-1,-2));
        drawingInfo=homeLabel("Kies een kleur en teken op het witte papier.",16,TEXT,false);
        drawingInfo.setGravity(Gravity.CENTER);
        drawingInfo.setMinHeight(dp(48));
        drawingInfo.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        homeAdd(controls,drawingInfo,0,7);
        int panelWidth=alongside?280:available-20;
        String[] names={"Roze","Paars","Blauw","Groen","Geel","Donkerblauw"};
        for(int i=0;i<drawingColors.length;i++){
            final int selected=i;
            Button button=drawingChoice(names[i],mix(PlaylumiDrawingView.PALETTE[i],Color.WHITE,.84f));
            GradientDrawable swatch=rounded(PlaylumiDrawingView.PALETTE[i],20);
            swatch.setStroke(dp(1),0xff685078);swatch.setBounds(0,0,dp(18),dp(18));
            button.setCompoundDrawables(null,swatch,null,null);button.setCompoundDrawablePadding(dp(3));
            drawingColors[i]=button;
            button.setOnClickListener(v->{
                cancelDrawingClear();drawingColor=selected;
                drawing.color(PlaylumiDrawingView.PALETTE[selected]);
                rememberDrawingTools();updateDrawingTools();
                drawingInfo.setText(names[selected]+" gekozen. Teken maar!");sound.tap();
            });
        }
        // Fit complete colour names, including the selected checkmark, at the user's text size.
        float widestColor=0;
        for(int i=0;i<names.length;i++)
            widestColor=Math.max(widestColor,drawingColors[i].getPaint().measureText("✓ "+names[i]));
        int colorWidth=(int)Math.ceil(widestColor/getResources().getDisplayMetrics().density)+24;
        drawingRows(controls,Math.max(1,Math.min(3,panelWidth/Math.max(1,colorWidth))),drawingColors);
        drawingPencil=drawingChoice("✎ Potlood",0xffe6dfff);
        drawingEraser=drawingChoice("▱ Gum",0xffffe8d3);
        drawingPencil.setOnClickListener(v->{
            cancelDrawingClear();drawing.setEraser(false);rememberDrawingTools();updateDrawingTools();
            drawingInfo.setText("Potlood gekozen. Je kleur staat klaar.");
        });
        drawingEraser.setOnClickListener(v->{
            cancelDrawingClear();drawing.setEraser(true);rememberDrawingTools();updateDrawingTools();
            drawingInfo.setText("Gum gekozen. Veeg over wat je wilt weghalen.");
        });
        drawingRows(controls,2,drawingPencil,drawingEraser);
        String[] sizes={"Dun","Normaal","Dik"};
        for(int i=0;i<drawingWidths.length;i++){
            final int chosen=i;
            drawingWidths[i]=drawingChoice(sizes[i],0xffe1edf9);
            drawingWidths[i].setOnClickListener(v->{
                cancelDrawingClear();drawingWidth=chosen;drawing.setBrushSize(chosen);
                rememberDrawingTools();updateDrawingTools();
                drawingInfo.setText(sizes[chosen]+" gekozen voor je "+(drawing.isEraser()?"gum.":"potlood."));
            });
        }
        drawingRows(controls,panelWidth>=270&&fontScale<=1.3f?3:1,drawingWidths);
        drawingUndo=drawingChoice("↩  Laatste lijn weg",0xffe6dfff);
        drawingRedo=drawingChoice("↪  Laatste lijn terug",0xffe6dfff);
        drawingUndo.setOnClickListener(v->{
            cancelDrawingClear();
            if(drawing.undo())drawingInfo.setText("Laatste stap teruggedraaid. Met ‘Laatste lijn terug’ herstel je die.");
            updateDrawingTools();
        });
        drawingRedo.setOnClickListener(v->{
            cancelDrawingClear();
            if(drawing.redo())drawingInfo.setText("Je laatste stap staat weer op het papier.");
            updateDrawingTools();
        });
        drawingRows(controls,panelWidth>=270&&fontScale<=1.3f?2:1,drawingUndo,drawingRedo);
        Button save=drawingChoice("💾 Bewaar tekening",0xffd0f3dd);
        drawingRows(controls,1,save);
        save.setOnClickListener(v->{
            cancelDrawingClear();
            if(!drawing.save()) {
                drawingInfo.setText("Bewaren lukte nog niet. Je tekening staat nog hier; probeer het opnieuw.");
                return;
            }
            SharedPreferences.Editor drawingEdit=prefs().edit();
            stagePlayDay(drawingEdit);
            boolean first=!prefs().getBoolean("drawing_saved_once",false);
            if(first) {
                drawingEdit.putBoolean("drawing_saved_once",true);
                wardrobe.earnCreativeBonus();stageWardrobe(drawingEdit);stageWorldProgress(drawingEdit,5,1);
                drawingInfo.setText("🌟 Kunstwerk bewaard! +9 speelsterren!");
                FrameLayout current=stage;
                current.post(()->{if(stage==current)PlaylumiMotion.celebrate(current,10);});
            } else drawingInfo.setText("💜 Jouw kunstwerk is veilig bewaard!");
            drawingEdit.apply();
            sound.good();
        });
        drawingClear=drawingChoice("🧽 Begin met leeg papier",0xffffe9df);
        drawingRows(controls,1,drawingClear);
        drawingClear.setOnClickListener(v->{
            if(!eraseAsked) {
                eraseAsked=true;
                drawingClear.setText("🧽 Begin met leeg papier\nTik nog eens om te wissen");
                drawingInfo.setText("Tik nog eens op ‘Begin met leeg papier’. Je kunt deze stap daarna terugdraaien.");
            } else {
                drawing.clear();cancelDrawingClear();updateDrawingTools();
                drawingInfo.setText(drawing.canUndo()?"Een nieuw wit papier. ‘Laatste lijn weg’ haalt je tekening terug.":
                        "Het papier is leeg. Teken maar!");
            }
        });
        TextView history=homeLabel("Je laatste "+PlaylumiDrawingHistory.MAX_ACTIONS+" stappen kun je terugdraaien zolang dit tekenblad open is. Opnieuw openen laadt je bewaarde kunstwerk.",12,SUB,false);
        homeAdd(controls,history,7,2);
        final PlaylumiDrawingView activeDrawing=drawing;
        drawing.setOnStateChangedListener(()->{
            if(drawing!=activeDrawing)return;
            cancelDrawingClear();updateDrawingTools();
            if(drawing.wasLastStrokeSimplified())drawingInfo.setText("Je lange lijn is iets vereenvoudigd. Je kunt de hele lijn nog terugdraaien.");
        });
        updateDrawingTools();
    }
    private Button drawingChoice(String label,int color){
        Button button=tile(label,"",color,56);
        button.setTextColor(TEXT);button.setTextSize(16);button.setMinHeight(dp(48));
        button.setMaxLines(4);button.setPadding(dp(6),dp(8),dp(6),dp(8));button.setElevation(dp(2));
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{mix(color,Color.WHITE,.3f),color});
        background.setCornerRadius(dp(16));background.setStroke(dp(1),0xffbca5d0);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),background,null));
        return button;
    }
    private void drawingRows(LinearLayout parent,int columns,Button... buttons){
        for(int first=0;first<buttons.length;first+=columns){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.TOP);row.setBaselineAligned(false);
            row.setClipChildren(false);row.setClipToPadding(false);
            for(int j=0;j<columns;j++){
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1f);p.setMargins(dp(3),dp(4),dp(3),dp(4));
                int at=first+j;row.addView(at<buttons.length?buttons[at]:new View(this),p);
            }
            parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
    }
    private void cancelDrawingClear(){
        eraseAsked=false;
        if(drawingClear!=null)drawingClear.setText("🧽 Begin met leeg papier");
    }
    private void rememberDrawingTools(){
        prefs().edit().putInt("drawing_color_v1",drawingColor).putInt("drawing_brush_size_v1",drawingWidth)
                .putBoolean("drawing_eraser_v1",drawing.isEraser()).apply();
    }
    private void drawingSelected(Button button,String label,String description,boolean selected){
        if(button==null)return;
        button.setSelected(selected);button.setText((selected?"✓ ":"")+label);
        button.setContentDescription(description+(selected?", geselecteerd":""));
    }
    private void updateDrawingTools(){
        if(drawing==null)return;
        String[] names={"Roze","Paars","Blauw","Groen","Geel","Donkerblauw"};
        for(int i=0;i<drawingColors.length;i++)drawingSelected(drawingColors[i],names[i],"Tekenkleur "+names[i],i==drawingColor);
        drawingSelected(drawingPencil,"✎ Potlood","Potlood",!drawing.isEraser());
        drawingSelected(drawingEraser,"▱ Gum","Gum",drawing.isEraser());
        String[] sizes={"Dun","Normaal","Dik"};
        for(int i=0;i<drawingWidths.length;i++)drawingSelected(drawingWidths[i],sizes[i],"Lijndikte "+sizes[i],i==drawingWidth);
        if(drawingUndo!=null){drawingUndo.setEnabled(drawing.canUndo());drawingUndo.setAlpha(drawing.canUndo()?1f:.45f);}
        if(drawingRedo!=null){drawingRedo.setEnabled(drawing.canRedo());drawingRedo.setAlpha(drawing.canRedo()?1f:.45f);}
    }


    private boolean protectedParentPage(){
        return page==PARENT_HOME||page==PARENT_WORLDS||page==PARENT_STATS||page==PARENT_SETTINGS||page==PROFILES;
    }
    private void requestParents(){
        parentAllowed=false;
        adultA=10+new Random().nextInt(8);
        adultB=8+new Random().nextInt(8);
        move(PARENT_GATE);
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
    private java.util.List<PlaylumiParentProgress.World> parentProgress(){
        PlayHistory history=playHistory();
        java.util.Map<String,PlaylumiLearningProfile> profiles=new java.util.HashMap<>();
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age))
            if(entry.kind==PlaylumiGameCatalog.QUIZ)profiles.put(entry.id,learningProfile(entry));
        return PlaylumiParentProgress.summarize(age,history.unfinished,history.completed,history.rounds,profiles);
    }
    private void parentProgressBar(LinearLayout card,int max,int value,int color,String description){
        ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Math.max(1,max));bar.setProgress(Math.max(0,Math.min(max,value)));
        bar.setProgressTintList(ColorStateList.valueOf(color));
        bar.setContentDescription(description);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(9));lp.setMargins(0,dp(3),0,dp(5));
        card.addView(bar,lp);
    }
    private LinearLayout parentWorldCard(PlaylumiParentProgress.World report,boolean openDetails){
        PlaylumiPlayOverview.WorldSummary summary=report.summary;
        int world=summary.world;
        LinearLayout card=personalPanel();
        homeAdd(card,homeLabel(PlaylumiWorldStyle.NAMES[world],19,PlaylumiWorldStyle.deep(world),true),0,3);
        String counted=summary.countedRounds+(summary.countedRounds==1?" getelde ronde":" getelde rondes");
        String completed=summary.completedRoutes+(summary.completedRoutes==1?" oefening met een afgeronde ronde":" oefeningen met een afgeronde ronde");
        homeAdd(card,homeLabel(counted+" · "+completed,14,SUB,false),0,4);
        homeAdd(card,homeLabel(summary.unfinishedRoutes+(summary.unfinishedRoutes==1?" begonnen ronde om verder te spelen":" begonnen rondes om verder te spelen"),14,0xff27684d,false),0,5);
        homeAdd(card,homeLabel(summary.completedRoutes+" van "+summary.roundBasedRoutes+" rondespellen afgerond",14,SUB,false),0,1);
        parentProgressBar(card,summary.roundBasedRoutes,summary.completedRoutes,PlaylumiWorldStyle.accent(world),
                summary.completedRoutes+" van "+summary.roundBasedRoutes+" rondespellen afgerond");
        if(report.percent()<0){
            homeAdd(card,homeLabel(report.samples==0?"Nog geen nieuwe oefengegevens":"Nog te weinig gegevens voor een percentage",13,SUB,false),0,1);
            if(report.total>0)homeAdd(card,homeLabel(report.total+" opgeloste quizvragen",13,SUB,false),0,1);
        }else{
            homeAdd(card,homeLabel(report.percent()+"% in één keer goed · "+report.total+" opgeloste vragen",14,SUB,false),0,2);
            homeAdd(card,homeLabel("Gebaseerd op "+report.samples+" recente quizvragen.",12,SUB,false),0,1);
            parentProgressBar(card,100,report.percent(),0xff8057ad,"Recente eerste antwoorden "+report.percent()+" procent");
        }
        if(openDetails){
            Button details=tile("Bekijk spellen","",PlaylumiWorldStyle.pale(world),52);
            details.setContentDescription("Bekijk spellen "+PlaylumiWorldStyle.NAMES[world]);
            card.addView(details,new LinearLayout.LayoutParams(-1,-2));
            details.setOnClickListener(v->{parentWorld=world;parentStatsPage=0;move(PARENT_STATS);});
        }
        return card;
    }
    private void parentDataNote(){
        addText("Voor "+AGES[PlaylumiRocketCurriculum.level(age)]+". Rondes worden sinds versie 0.107 geteld. Oudere afgeronde oefeningen blijven zichtbaar.",14,SUB,0,4);
        addText("Percentages komen uit maximaal 12 recente antwoorden per quiz. Geen rapportcijfer of vergelijking met andere kinderen. Tekenen telt niet als ronde.",13,SUB,0,5);
    }
    private void showLearningSummary(){
        addText("Spelen en oefenen",22,TEXT,6,4);
        parentDataNote();
        for(PlaylumiParentProgress.World world:parentProgress())addWidget(parentWorldCard(world,true),2,3);
    }
    private void showParentHome() {
        header("👨‍👩‍👧 Voor ouders",PlaylumiProfiles.ageLabel(age)+". Alles blijft op dit toestel.");
        saveUsage();
        long correct=0;int badges=0;
        for(int i=0;i<PlaylumiScores.CATEGORIES;i++){
            correct+=Math.max(0,scores.get(i).correct);badges+=scores.get(i).earnedBadges();
        }
        LinearLayout overviewCard=personalPanel();
        homeAdd(overviewCard,homeLabel("Op dit toestel",17,TEXT,true),0,3);
        homeAdd(overviewCard,homeLabel("⭐ "+correct+" goede antwoorden",23,0xff34856d,true),0,4);
        homeAdd(overviewCard,homeLabel("🏅 "+badges+" van "+(PlaylumiScores.CATEGORIES*6)+" badges verdiend",17,TEXT,false),0,4);
        homeAdd(overviewCard,homeLabel("⏱️ Vandaag ongeveer "+usage.minutes()+" minuten gespeeld",15,SUB,false),0,2);
        homeAdd(overviewCard,homeLabel("Daguitdagingen vandaag: "+dailyChallenges().completed()+"/3",15,SUB,false),0,2);
        homeAdd(overviewCard,homeLabel("Expeditie: "+expeditionProgress()+"/30 opdrachten · "+PlaylumiExpedition.stamps(expeditionProgress())+"/6 hoofdstukken",15,SUB,false),0,2);
        addWidget(overviewCard,3,5);
        Button overview=tile("📊 Bekijk de voortgang","Zes speelwerelden, per spel",
                0xffd7eefe,104);
        addTile(overview,9);
        overview.setOnClickListener(v->move(PARENT_WORLDS));
        Button settings=tile("⚙️ Instellingen","Geluid, animaties en pauzeherinnering",
                0xffe8dcff,104);
        addTile(settings,9);
        settings.setOnClickListener(v->move(PARENT_SETTINGS));
        showLearningSummary();
        Button privacy=tile("🔒 Privacy en gegevens","Bekijk wat op dit toestel wordt bewaard",0xfff0e5ff,90);
        addTile(privacy,7);privacy.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
                .setTitle("Privacy en gegevens")
                .setMessage("Playlumi heeft geen account, advertenties of in-app aankopen. Leeftijdskeuze, voortgang, speelgegevens, Lumi's uiterlijk, daguitdagingen, instellingen en tekeningen blijven op dit toestel. Ook gegevens uit oudere versies worden bewaard. Er is geen Playlumi-server. Je kunt alle lokale Playlumi-gegevens wissen via Android Instellingen > Apps > Playlumi > Opslag > Gegevens wissen. Daarmee verdwijnen alle opgeslagen gegevens en beloningen.").setPositiveButton("Sluiten",null).show());
        Button back=tile("🌈 Terug naar spelen","",0xffd4f3e0,85);
        addTile(back,11);
        back.setOnClickListener(v->{parentAllowed=false;move(HOME);});
    }
    private void showParentWorlds() {
        header("📊 Voortgang","Kies een van de zes speelwerelden.");
        parentDataNote();
        for(PlaylumiParentProgress.World report:parentProgress())addWidget(parentWorldCard(report,true),2,4);
    }
    private void showParentStats() {
        if(!PlaylumiWorldStyle.valid(parentWorld))parentWorld=0;
        PlaylumiParentProgress.World report=parentProgress().get(parentWorld);
        header(PlaylumiWorldStyle.NAMES[parentWorld],"Voortgang per spel");
        addWidget(parentWorldCard(report,false),2,4);
        int pageSize=PlaylumiParentProgress.pageSize(getResources().getConfiguration().screenWidthDp,getResources().getConfiguration().fontScale);
        parentStatsPage=PlaylumiParentProgress.safePage(parentStatsPage,report.games.size(),pageSize);
        int pages=PlaylumiParentProgress.pageCount(report.games.size(),pageSize);
        addText("Spellen · pagina "+(parentStatsPage+1)+" van "+pages,17,TEXT,6,2);
        addText("Spellen met opgeslagen resultaten staan bovenaan.",13,SUB,0,4);
        int end=Math.min(report.games.size(),(parentStatsPage+1)*pageSize);
        for(int i=parentStatsPage*pageSize;i<end;i++){
            PlaylumiParentProgress.Game game=report.games.get(i);
            LinearLayout card=personalPanel();
            homeAdd(card,homeLabel(game.entry.icon+"  "+game.entry.title,18,PlaylumiWorldStyle.deep(parentWorld),true),0,3);
            String state=game.rounds>0?game.rounds+(game.rounds==1?" getelde ronde":" getelde rondes"):
                    game.completed?"Afgerond · oudere ronde, aantal onbekend":"Nog geen afgeronde ronde";
            homeAdd(card,homeLabel(state,15,TEXT,false),0,3);
            if(game.unfinished)homeAdd(card,homeLabel("Een begonnen ronde is opgeslagen.",14,0xff27684d,false),0,3);
            if(game.entry.kind==PlaylumiGameCatalog.QUIZ){
                if(game.total>0)homeAdd(card,homeLabel(game.total+" opgeloste quizvragen",14,SUB,false),0,3);
                if(game.percent()>=0){
                    homeAdd(card,homeLabel(game.percent()+"% in één keer goed · "+game.samples+" recente vragen",14,SUB,false),0,2);
                    parentProgressBar(card,100,game.percent(),0xff8057ad,"Recente eerste antwoorden "+game.percent()+" procent");
                }else homeAdd(card,homeLabel(game.samples==0?"Nog geen quizgegevens":"Nog te weinig recente vragen voor een percentage",13,SUB,false),0,2);
                if(game.total>0&&game.level>=0)homeAdd(card,homeLabel("Adaptief oefenniveau: "+AGES[game.level]+(catalogAdaptive()?"":" (adaptief oefenen staat uit)"),13,SUB,false),0,1);
            }else homeAdd(card,homeLabel("Interactief spel · voortgang per ronde",13,SUB,false),0,1);
            addWidget(card,2,3);
        }
        LinearLayout navigation=new LinearLayout(this);navigation.setGravity(Gravity.CENTER);
        Button previous=tile("‹ Vorige","",0xffe8dcff,56),next=tile("Volgende ›","",0xffd5f5df,56);
        previous.setEnabled(parentStatsPage>0);next.setEnabled(parentStatsPage+1<pages);
        previous.setContentDescription("Vorige pagina met spellen");next.setContentDescription("Volgende pagina met spellen");
        for(Button b:new Button[]{previous,next}){
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);navigation.addView(b,lp);
        }
        addWidget(navigation,4,6);
        previous.setOnClickListener(v->{parentStatsPage--;showPage();});
        next.setOnClickListener(v->{parentStatsPage++;showPage();});
    }
    private void showParentSettings() {
        header("⚙️ Instellingen","Deze instellingen blijven op dit toestel.");
        Button soundButton=tile(sound.isEnabled()?"🔊 Geluid: aan":"🔇 Geluid: uit",
                "Tik om te veranderen",0xffd6efff,90);
        addTile(soundButton,7);
        soundButton.setOnClickListener(v->{
            sound.setEnabled(!sound.isEnabled());
            prefs().edit().putBoolean("sound_on",sound.isEnabled()).apply();
            showPage();
        });
        Button learning=tile(catalogAdaptive()?"Adaptief oefenen: aan":"Adaptief oefenen: uit",
                "Past quizmoeilijkheid aan binnen de leeftijdsgroep. Voortgang blijft bewaard.",0xffd5f5df,90);
        addTile(learning,7);learning.setOnClickListener(v->{prefs().edit().putBoolean("catalog_adaptive",!catalogAdaptive()).apply();showPage();});
        addText("Speeltempo voor quizzen",22,0xff58448a,10,4);
        addText("Kies hoeveel tijd je kind krijgt voor uitleg na een goed antwoord.",15,SUB,0,5);
        for(int i=0;i<PlaylumiQuizPace.NAMES.length;i++){
            final int choice=i;boolean selected=quizPace()==i;
            Button speed=tile((selected?"✓ ":"")+PlaylumiQuizPace.NAMES[i],PlaylumiQuizPace.DETAILS[i],selected?0xffcef3d9:0xffe9dcff,78);
            addTile(speed,3);speed.setContentDescription(PlaylumiQuizPace.NAMES[i]+(selected?", geselecteerd":""));
            speed.setOnClickListener(v->{prefs().edit().putInt("quiz_pace",choice).apply();showPage();});
        }
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
            addWidget(completionPanel("Sterren vangen","⭐",PlaylumiStarCatch.ROUNDS,PlaylumiStarCatch.ROUNDS),3,6);
            Button again=tile("🌟 Speel nog eens","",0xffd4f4e2,88);
            again.setOnClickListener(v->{starCatchRound=0;starCatchSolved=false;starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());move(STAR_CATCH);});
            Button back=tile("🎪 Andere spelletjes","",0xffe8dfff,80);
            back.setOnClickListener(v->move(libraryReturn?catalogReturnPage():PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,back,true);return;
        }
        autoRoundVisible=true;
        if(starCatch==null)starCatch=PlaylumiStarCatch.generate(Math.max(0,Math.min(2,age)),new Random());
        final PlaylumiStarCatch round=starCatch;
        String instruction="Vang "+round.target()+" gouden sterren!";
        addPrompt(root,instruction,19);
        starCatchProgress=text(PlaylumiMiniPractice.trail(round.collected(),round.target(),"★","☆")+"   "+round.collected()+"/"+round.target(),20,PlaylumiMagicArt.INK);
        starCatchProgress.setContentDescription(round.collected()+" van "+round.target()+" sterren gevangen");
        starCatchProgress.setBackground(PlaylumiMagicArt.creamPlate(this));
        starCatchProgress.setPadding(dp(8),dp(8),dp(8),dp(8));
        addWidget(starCatchProgress,2,6);
        PlaylumiStarCatchBoard board=new PlaylumiStarCatchBoard(this,round,new PlaylumiStarCatchBoard.Listener(){
            @Override public void onStar(int count){starCatchProgress.setText(PlaylumiMiniPractice.trail(count,round.target(),"★","☆")+"   "+count+"/"+round.target());starCatchProgress.setContentDescription(count+" van "+round.target()+" sterren gevangen");sound.pop();if(!round.finished())saveLibrarySpecial(STAR_CATCH);}
            @Override public void onMistake(){sound.mistake();}
            @Override public void onFinished(){
                if(page!=STAR_CATCH||moving||!autoRoundVisible||starCatchSolved)return;
                starCatchSolved=true;
                int category=LearningGame.COUNT;
                ArrayList<Integer> badges=new ArrayList<>(scores.answer(category,true));
                lifetimeCount++;wardrobe.earnAnswer();wardrobe.earnBadges(badges.size());
                SharedPreferences.Editor edit=prefs().edit();
                stageWardrobe(edit);stageScore(edit,category);
                stageCompletion(edit,specialEntry(STAR_CATCH),starCatchRound+1,PlaylumiStarCatch.ROUNDS);
                stageLibrarySpecial(edit,STAR_CATCH);edit.apply();
                pendingBadges.addAll(badges);
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
            addWidget(completionPanel("Bellenfeest","🫧",20,20),3,6);
            Button again=tile("🫧 Nog meer bellen!","",0xffd3f4df,99);
            again.setOnClickListener(v->{
                bubbleRound=0;bubbleSolved=false;
                bubbles=PlaylumiBubbles.generate(Math.max(0,age),new Random());
                move(BUBBLE_PLAY);
            });
            Button back=tile("🎪 Een ander spel","",0xffe8deff,89);
            back.setOnClickListener(v->move(libraryReturn?catalogReturnPage():PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,back,true);
            return;
        }
        autoRoundVisible=true;
        String instruction="Prik "+bubbles.target+" zeepbellen!";
        addPrompt(root,instruction,19);
        PlaylumiBubbleBoard board=new PlaylumiBubbleBoard(this,bubbles,
                new PlaylumiBubbleBoard.Listener() {
            @Override public void onPop() {sound.pop();if(!bubbles.solved())saveLibrarySpecial(BUBBLE_PLAY);}
            @Override public void onSolved() {
                if(page!=BUBBLE_PLAY||moving||!autoRoundVisible||bubbleSolved)return;
                bubbleSolved=true;
                if(playPal!=null)playPal.dance();
                int category=LearningGame.COUNT;
                ArrayList<Integer> badges=new ArrayList<>(scores.answer(category,true));
                lifetimeCount++;
                wardrobe.earnAnswer();
                wardrobe.earnBadges(badges.size());
                SharedPreferences.Editor edit=prefs().edit();
                stageWardrobe(edit);stageScore(edit,category);
                stageCompletion(edit,specialEntry(BUBBLE_PLAY),bubbleRound+1,20);
                stageLibrarySpecial(edit,BUBBLE_PLAY);edit.apply();
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
        header("🐾 Vriendjespark","Lumi blijft de held. Zijn vriendjes wonen hier!");
        characterHero(139);
        for(int i=1;i<PlaylumiWardrobe.OPTIONS;i++){
            final int chosen=i;boolean owned=wardrobe.owned(PlaylumiWardrobe.FRIENDS,i);
            Button pal=tile(PlaylumiFriends.FRIEND_ICONS[i]+" "+PlaylumiWardrobe.ITEM_NAMES[PlaylumiWardrobe.FRIENDS][i],
                    owned?(friend==i?"Samen met Lumi":"Samen spelen"):"Nog vrij te spelen",owned?0xffd5f5df:0xffe8dcff,68);
            addTile(pal,3);
            pal.setOnClickListener(v->{
                if(wardrobe.owned(PlaylumiWardrobe.FRIENDS,chosen)){
                    wardrobe.buyOrEquip(PlaylumiWardrobe.FRIENDS,chosen);saveWardrobe();careMessage="";showPage();
                }else{selectedSlot=PlaylumiWardrobe.FRIENDS;closetReturn=AVATAR;move(CLOSET);}
            });
        }
        if(friend==0){
            addText("Kies een vriendje hierboven. Verdien je eerste door een quizronde te spelen!",16,SUB,4,7);return;
        }
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
                SharedPreferences.Editor careEdit=prefs().edit();
                stageFriends(careEdit);
                if(!wasDone){
                    sound.good();
                    careMessage=PlaylumiFriends.REPLIES[action];
                } else {
                    sound.hello();
                    careMessage="Wat gezellig om dit nog eens te doen!";
                }
                if(rewarded){
                    wardrobe.earnFriendBonus();
                    stageWardrobe(careEdit);
                    sound.unlock();
                    careMessage="🏅 Nieuwe vriendschapssticker! +"+
                            PlaylumiFriends.FIRST_COMPLETE_BONUS+" sterren!";
                }
                careEdit.apply();
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
        outfit.setOnClickListener(v->{selectedSlot=PlaylumiWardrobe.FRIENDS;closetReturn=AVATAR;move(CLOSET);});
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
        final long preview=++melodyPreviewToken;
        melodyStatus.setText("Luister goed ...");
        for(int i=0;i<melody.length();i++) {
            final int note=melody.noteAt(i);
            final int beat=i;
            active.postDelayed(()->{
                if(stage!=active||page!=MUSIC_PLAY||preview!=melodyPreviewToken||activeSince<=0||!melodyPreviewActive||isFinishing())return;
                sound.note(note);
                if(melodyPads[note]!=null)PlaylumiMotion.pop(melodyPads[note]);
                melodyStatus.setText("Luister goed  "+(beat+1)+"/"+melody.length());
            },i*470L);
        }
        active.postDelayed(()->{
            if(stage==active&&page==MUSIC_PLAY&&preview==melodyPreviewToken&&activeSince>0){
                melodyPreviewActive=false;
                melodyStatus.setText(melody.progress()>0?"Ga verder vanaf toon "+Math.min(melody.progress()+1,melody.length())+".":"Nu jij! Tik de dierenvriendjes aan.");
            }
        },melody.length()*470L+220L);
    }
    private void showMusicPlay() {
        if(melody==null)melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
        header(melodyVariant==2?"Geheugenconcert":melodyVariant==1?"Lange melodie":"Muziekstudio",Math.min(melodyRound,PlaylumiMelody.ROUNDS)+"/"+PlaylumiMelody.ROUNDS);
        addIllustratedPortal(PlaylumiVillageView.MUSIC,"Muziekstudio",116);
        addMagicProgress(melodyRound,PlaylumiMelody.ROUNDS,false);
        if(melodyRound>=PlaylumiMelody.ROUNDS) {
            addWidget(completionPanel(melodyVariant==2?"Geheugenconcert":melodyVariant==1?"Lange melodie":"Muziekstudio","♫",PlaylumiMelody.ROUNDS,PlaylumiMelody.ROUNDS),3,6);
            Button again=tile("🎶 Nog eens muziek maken","",0xffd4f3dd,96);
            again.setOnClickListener(v->{
                melodyRound=0;melodySolved=false;
                melody=PlaylumiMelody.generate(PlaylumiRocketCurriculum.level(age),melodyVariant,new Random());
                move(MUSIC_PLAY);
            });
            Button other=tile("🎪 Ander spelletje","",0xffe9deff,80);
            other.setOnClickListener(v->move(libraryReturn?catalogReturnPage():PlaylumiWorldStyle.valid(villageLaunch)?VILLAGE_MENU:MINI_MENU));
            finishActions(again,other,true);
            return;
        }
        autoRoundVisible=true;
        // Only the actual melody preview and playable animal buttons are shown.
        melodyStatus=magicRibbon(melody.progress()>0?"Ga verder vanaf toon "+Math.min(melody.progress()+1,melody.length())+".":sound.isEnabled()?
                "Luister en speel het deuntje na.":"Bekijk de plaatjes en tik ze na.",19);
        addWidget(melodyStatus,0,4);
        melodySteps=text("",21,PlaylumiMagicArt.INK);
        melodySteps.setBackground(PlaylumiMagicArt.creamPlate(this));
        melodySteps.setPadding(dp(5),dp(4),dp(5),dp(4));
        melodySteps.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        updateMelodySteps();addWidget(melodySteps,0,4);
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
    private void updateMelodySteps(){
        if(melody==null||melodySteps==null)return;
        melodySteps.setText(melody.playedTrail());
        melodySteps.setContentDescription(melody.progress()+" van "+melody.length()+" tonen gespeeld");
    }
    private void tapMelody(int note,Button pressed) {
        if(page!=MUSIC_PLAY||moving||!autoRoundVisible||melodySolved||melody==null||melodyPreviewActive)return;
        sound.note(note);
        PlaylumiDelight.hop(pressed);
        int status=melody.tap(note);
        updateMelodySteps();
        if(status!=2)saveLibrarySpecial(MUSIC_PLAY);
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
            SharedPreferences.Editor edit=prefs().edit();
            stageWardrobe(edit);stageScore(edit,LearningGame.PATTERNS);
            stageCompletion(edit,specialEntry(MUSIC_PLAY),melodyRound+1,PlaylumiMelody.ROUNDS);
            stageLibrarySpecial(edit,MUSIC_PLAY);edit.apply();
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
                SharedPreferences.Editor growEdit=prefs().edit();
                stageGarden(growEdit);
                if(result==1) {
                    wardrobe.earnGardenBonus();
                    stageWardrobe(growEdit);
                    sound.unlock();
                    gardenMessage="🎉 Nieuwe plant! +"+
                            PlaylumiGarden.FIRST_BLOOM_STARS+" speelsterren";
                } else {
                    sound.sparkle();
                    gardenMessage=action==PlaylumiGarden.WATER?
                            "💧 Goed gedaan! Het plantje groeit!":"☀️ Heerlijk, wat fijn zonlicht!";
                }
                growEdit.apply();
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
