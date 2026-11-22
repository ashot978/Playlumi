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
import android.widget.HorizontalScrollView;
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
            WORLD_PICKER=40, HOME_OPTIONS=41, PICTURE_PLAY=42, MUSIC_BOX=43;
    private static final int SESSION_ROUNDS=PlaylumiLayout.SHORT_ROUND_QUESTIONS;
    private static final int TEXT=PlaylumiVisualStyle.INK, SUB=PlaylumiVisualStyle.MUTED;
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
    // Gentle hints belong only to the visible question, never to a saved profile.
    private LearningGame.Round hintRound;
    private int hintHiddenMask;
    private Button hintButton;
    private final ArrayList<Integer> pendingBadges=new ArrayList<>();
    private int page,age,world,categoryPage,variantPage,badgePage,difficulty;
    private int closetPage,shopPage,selectedSlot;
    private String shopMessage="";
    private int outfitPreview=-1, roomPreview=-1;
    private int outfitFilter=PlaylumiTryOn.ALL, roomFilter=PlaylumiTryOn.ALL;
    private int sessionSolved,sessionAttempts,earnedBadge=-1,lifetimeCount;
    private boolean moving,answerLocked,greeted;
    private int mascotTapCount;
    private int starStagePage;
    private LearningGame game;
    private QuestionHistory recent;
    private PlaylumiPlayHistory playHistory;
    private PlaylumiFavorites favorites;
    private PlaylumiSavedLooks savedLooks;
    private PlaylumiScores scores;
    private PlaylumiWardrobe wardrobe;
    private PlaylumiFriends friends;
    private int carePage,albumPage;
    private String careMessage="";
    private PlaylumiRoom room;
    private PlaylumiGarden garden;
    private int gardenPage,gardenPlant;
    private String gardenMessage="";
    private String gardenRoundNotice="";
    private PlaylumiGardenView visibleGarden;
    private int roomPage,roomItemPage,roomSlot;
    private String roomMessage="";
    private PlaylumiCharacterView character;
    private LinearLayout root;
    private FrameLayout stage;
    private ScrollView scroller;
    private LinearLayout quickNavBar;
    private TextView feedbackText;
    private PlaylumiStarTrail roundStars;
    private TextView sessionCounter;
    private Button nextButton;
    private String feedback="";
    private PlaylumiAudio sound;
    private PlaylumiMiniChallenge mini;
    private MiniChallengeDeck miniDeck;
    private PlaylumiCharacterView playPal;
    private int miniKind,miniRound,rewardReturn=PLAY;
    private boolean miniSolved;
    private PlaylumiBubbles bubbles;
    private SensoryChallengeDeck<PlaylumiBubbles> bubbleDeck;
    private long bubbleDeckSeed;
    private PlaylumiMelody melody;
    private final PlaylumiMelodyPlayback melodyPlayback=new PlaylumiMelodyPlayback();
    private SensoryChallengeDeck<PlaylumiMelody> melodyDeck;
    private long melodyDeckSeed;
    private int melodyRound;
    private boolean melodySolved;
    private int musicBoxPage,musicBoxTrack=-1;
    private boolean musicBoxFavoritesOnly;
    private PlaylumiMusicFavorites musicBoxFavorites=new PlaylumiMusicFavorites(0);
    private PlaylumiMelodyAlbum melodyAlbum=new PlaylumiMelodyAlbum();
    private int composerTempo=PlaylumiComposerTempo.NORMAL;
    private int rhythmIdeaTurn;
    private TextView melodyStatus;
    private int bubbleRound;
    private boolean bubbleSolved;
    private PlaylumiAdventure adventures;
    private PlaylumiAdaptive adaptive;
    private PlaylumiUsage usage;
    private long activeSince;
    private int parentWorld,parentStatsPage,adultA,adultB,breakReturn=HOME;
    private int activeProfile;
    private boolean parentAllowed;
    private PlaylumiStories stories;
    private PlaylumiDrawingView drawing;
    private int storyLibraryPage;
    private TextView drawingInfo;
    private boolean eraseAsked;
    private boolean storyJustCompleted;
    private boolean smartDifficulty,roundHadMistake;
    private boolean pictureLaunch;
    private boolean badgesFromTrophy;
    private int trophyWorld,trophyPage;
    private int pendingLevel=-1;
    private int storyStage=-1,storyPage;
    private boolean storyNewReward;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(PlaylumiVisualStyle.PAGE_TOP);
        getWindow().setNavigationBarColor(PlaylumiVisualStyle.PAGE_BOTTOM);
        int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        activeProfile=getSharedPreferences("playlumi-global",MODE_PRIVATE)
                .getInt("active_profile",0);
        activeProfile=PlaylumiProfiles.safe(activeProfile);
        if(state!=null && state.getInt("activeProfile",activeProfile)!=activeProfile)
            state=null; // Switching children must not restore the former child's state.
        SharedPreferences prefs=prefs();
        musicBoxFavorites=new PlaylumiMusicFavorites(
                prefs.getInt(PlaylumiMusicFavorites.PREF_KEY,0));
        // Reuse the 0.49 key as album slot 1; older melodies are never lost.
        melodyAlbum=PlaylumiMelodyAlbum.restore(
                prefs.getString(PlaylumiMelodyAlbum.key(0),""),
                prefs.getString(PlaylumiMelodyAlbum.key(1),""),
                prefs.getString(PlaylumiMelodyAlbum.key(2),""),
                prefs.getInt("music_box_selected_slot",0));
        composerTempo=PlaylumiComposerTempo.safe(
                prefs.getInt("music_box_tempo",PlaylumiComposerTempo.NORMAL));
        sound=new PlaylumiAudio(this,prefs.getBoolean("sound_on",true));
        // Playful music is enabled by default, unless a parent stored a mute.
        sound.setEffectsEnabled(prefs.getBoolean("effects_on",true));
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
            musicBoxPage=PlaylumiMusicBox.safePage(state.getInt("musicBoxPage",0));
            musicBoxTrack=PlaylumiMusicBox.safeSelected(state.getInt("musicBoxTrack",-1));
            musicBoxFavoritesOnly=state.getBoolean("musicBoxFavoritesOnly",false);
            selectedSlot=state.getInt("selectedSlot",0);
            outfitPreview=state.getInt("outfitPreview",-1);
            roomPreview=state.getInt("roomPreview",-1);
            outfitFilter=state.getInt("outfitFilter",PlaylumiTryOn.ALL);
            roomFilter=state.getInt("roomFilter",PlaylumiTryOn.ALL);
            carePage=state.getInt("carePage",0);
            albumPage=state.getInt("albumPage",0);
            gardenPage=state.getInt("gardenPage",0);
            gardenPlant=state.getInt("gardenPlant",0);
            gardenRoundNotice=state.getString("gardenRoundNotice","");
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
            pictureLaunch=state.getBoolean("pictureLaunch",false);
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
        playHistory=PlaylumiPlayHistory.restore(prefs.getString("recent_games",""));
        favorites=PlaylumiFavorites.restore(prefs.getString("favorite_games",""));
        savedLooks=PlaylumiSavedLooks.restore(prefs.getString("outfit_presets",""));
        game=new LearningGame(new Random());
        game.setHistory(recent);
        game.configure(Math.max(0,Math.min(LearningGame.GAME_NAMES.length-1,mode)),
                Math.max(0,Math.min(4,variant)), Math.max(0,Math.min(2,difficulty)));
        if(page==MINI_PLAY)startMiniDeck();
        if(page==MUSIC_PLAY || (page==REWARD && rewardReturn==MUSIC_PLAY)) {
            restoreMelodyDeck(state);
            if(state!=null && state.getIntArray("melodyNotes")!=null) {
                try {
                    int[] order=state.getIntArray("melodyButtonOrder");
                    melody=order==null?
                            PlaylumiMelody.restore(state.getIntArray("melodyNotes"),
                                    state.getInt("melodyProgress",0)):
                            PlaylumiMelody.restore(state.getIntArray("melodyNotes"),
                                    state.getInt("melodyProgress",0),order);
                } catch(IllegalArgumentException ignored) {}
            }
        }
        if(page==BUBBLE_PLAY || (page==REWARD && rewardReturn==BUBBLE_PLAY)) {
            restoreBubbleDeck(state);
            if(state!=null && state.getInt("bubbleTotal",0)>0) {
                try {
                    bubbles=PlaylumiBubbles.restore(Math.max(0,Math.min(2,age)),
                            state.getInt("bubbleTarget",0),
                            state.getInt("bubbleTotal",0),
                            state.getInt("bubbleMask",0),
                            state.getInt("bubbleLayout",0));
                } catch(IllegalArgumentException ignored) {}
            }
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
        melodyPlayback.stop();
        if(sound!=null) {
            sound.stopNotes();
        }
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
        out.putInt("musicBoxPage",musicBoxPage);
        out.putInt("musicBoxTrack",musicBoxTrack);
        out.putBoolean("musicBoxFavoritesOnly",musicBoxFavoritesOnly);
        out.putInt("selectedSlot",selectedSlot);
        out.putInt("outfitPreview",outfitPreview);
        out.putInt("roomPreview",roomPreview);
        out.putInt("outfitFilter",outfitFilter);
        out.putInt("roomFilter",roomFilter);
        out.putInt("carePage",carePage);
        out.putInt("albumPage",albumPage);
        out.putInt("gardenPage",gardenPage);
        out.putInt("gardenPlant",gardenPlant);
        out.putString("gardenRoundNotice",gardenRoundNotice);
        out.putInt("miniKind",miniKind);
        out.putInt("miniRound",miniRound);
        out.putInt("bubbleRound",bubbleRound);
        out.putInt("melodyRound",melodyRound);
        out.putBoolean("melodySolved",melodySolved);
        if(melody!=null) {
            out.putIntArray("melodyNotes",melody.sequence());
            out.putIntArray("melodyButtonOrder",melody.buttonOrder());
            out.putInt("melodyProgress",melody.progress());
        }
        if(melodyDeck!=null) {
            out.putLong("melodyDeckSeed",melodyDeckSeed);
            out.putInt("melodyDeckConsumed",melodyDeck.dealt());
        }
        out.putBoolean("bubbleSolved",bubbleSolved);
        if(bubbles!=null) {
            out.putInt("bubbleTarget",bubbles.target);
            out.putInt("bubbleTotal",bubbles.total);
            out.putInt("bubbleMask",bubbles.mask());
            out.putInt("bubbleLayout",bubbles.layoutCode());
        }
        if(bubbleDeck!=null) {
            out.putLong("bubbleDeckSeed",bubbleDeckSeed);
            out.putInt("bubbleDeckConsumed",bubbleDeck.dealt());
        }
        out.putInt("rewardReturn",rewardReturn);
        out.putInt("storyStage",storyStage);
        out.putInt("storyPage",storyPage);
        out.putBoolean("storyNewReward",storyNewReward);
        out.putBoolean("smartDifficulty",smartDifficulty);
        out.putBoolean("roundHadMistake",roundHadMistake);
        out.putBoolean("pictureLaunch",pictureLaunch);
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
        if(page==WORLD_PICKER || page==HOME_OPTIONS || page==PICTURE_PLAY ||
                page==MUSIC_BOX) {move(HOME);return;}
        if(page==REWARD) { continueAfterBadge();return; }
        if(page==AVATAR) {move(HOME);return;}
        if(page==CLOSET) {move(AVATAR);return;}
        if(page==SHOP) {
            if(outfitPreview>=0) {
                outfitPreview=-1;shopMessage="";showPage();
            } else move(CLOSET);
            return;
        }
        if(page==FRIEND_CARE) {move(AVATAR);return;}
        if(page==FRIEND_ALBUM) {move(FRIEND_CARE);return;}
        if(page==GARDEN_MENU) {move(HOME);return;}
        if(page==GARDEN_PLANT) {move(GARDEN_MENU);return;}
        if(page==MINI_PLAY || page==BUBBLE_PLAY || page==MUSIC_PLAY) {move(MINI_MENU);return;}
        if(page==MINI_MENU) {move(HOME);return;}
        if(page==WORLD_HUB) {move(WORLD_PICKER);return;}
        if(page==ADVENTURE_MAP) {move(WORLD_HUB);return;}
        if(page==STAR_STAGE) {move(AVATAR);return;}
        if(page==ROOM) {move(AVATAR);return;}
        if(page==ROOM_CATS) {move(AVATAR);return;}
        if(page==ROOM_ITEMS) {
            if(roomPreview>=0) {
                roomPreview=-1;roomMessage="";showPage();
            } else move(ROOM_CATS);
            return;
        }
        if(page==TROPHY_WORLDS) {move(AVATAR);return;}
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
        if(page==PLAY && pictureLaunch) {move(PICTURE_PLAY);return;}
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

    /** Only activity choices, never answers, are saved per player. */
    private void rememberPlay() {
        playHistory.record(game.mode(),game.variant(),difficulty,smartDifficulty);
        prefs().edit().putString("recent_games",playHistory.serialize()).apply();
    }

    /** Start a NEW shuffled round from a previously chosen configuration. */
    private void resumeRecent(PlaylumiPlayHistory.Entry item) {
        pictureLaunch=false;
        storyStage=-1;storyNewReward=false;roundHadMistake=false;pendingLevel=-1;
        sessionSolved=0;sessionAttempts=0;feedback="";
        pendingBadges.clear();
        smartDifficulty=item.smart;
        difficulty=item.smart?adaptive.level(item.mode,Math.max(0,age)):item.difficulty;
        game.configure(item.mode,item.variant,difficulty);
        world=Math.max(0,Math.min(2,item.mode/8));
        saveRecent();rememberPlay();
        prefs().edit().putInt("game_mode",item.mode)
                .putInt("game_variant",item.variant)
                .putInt("last_played_category",item.mode)
                .putInt("last_played_variant",item.variant)
                .putBoolean("smart_mode",item.smart).apply();
        move(PLAY);
    }

    private void saveFavorites() {
        prefs().edit().putString("favorite_games",favorites.serialize()).apply();
    }
    private void showFavoriteGames() {
        if(favorites.size()==0)return;
        addText("💛 Mijn favoriete spellen",20,TEXT,7,5);
        java.util.List<Button> buttons=new java.util.ArrayList<>();
        for(int i=0;i<favorites.size();i++) {
            final PlaylumiPlayHistory.Entry item=favorites.get(i);
            String title="⭐ "+LearningGame.GAME_NAMES[item.mode];
            String description="▶ Spelvorm "+(item.variant+1)+" · "+
                    (item.smart?"Slim niveau":LEVELS[item.difficulty]);
            Button button=tile(title,description,0xffffeaca,99);
            button.setOnClickListener(v->resumeRecent(item));
            buttons.add(button);
        }
        addOptionGrid(buttons,105);
    }
    private void toggleFavorite(PlaylumiPlayHistory.Entry item) {
        boolean pinned=favorites.toggle(item);
        saveFavorites();
        sound.sparkle();
        // A filled list leaves the game unpinned; explain the limit in UI.
        if(!pinned && !favorites.contains(item.mode,item.variant) &&
                favorites.size()==PlaylumiFavorites.CAPACITY) {
            // The user can remove a favorite through the star next to a recent game.
        }
        redrawKeepingScroll();
    }

    /** Compact recent-game cards appear on the Home and Play menus. */
    private void showRecentGames(boolean atHome) {
        if(playHistory.size()==0)return;
        addText(atHome?"🕹️ Verder spelen":"🕹️ Onlangs gespeeld",20,TEXT,6,5);
        java.util.List<Button> options=new java.util.ArrayList<>();
        for(int i=0;i<playHistory.size();i++) {
            final PlaylumiPlayHistory.Entry item=playHistory.get(i);
            String subtitle="▶ Spelvorm "+(item.variant+1)+" · "+
                    (item.smart?"Slim niveau":LEVELS[item.difficulty]);
            Button button=tile(ICONS[item.mode]+" "+
                    LearningGame.GAME_NAMES[item.mode],subtitle,
                    COLORS[item.mode%COLORS.length],93);
            button.setOnClickListener(v->resumeRecent(item));
            options.add(button);
            Button star=tile(favorites.contains(item.mode,item.variant)?
                    "💛 Uit favorieten":"🤍 Favoriet",
                    "Tik om dit spel vast te zetten",0xfff6e9c9,78);
            star.setOnClickListener(v->toggleFavorite(item));
            options.add(star);
        }
        addOptionGrid(options,105);
    }

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
    /** One rounded surface language shared by navigation, tiles and questions. */
    private GradientDrawable softCard(int color,int radius) {
        GradientDrawable shape=new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{PlaylumiVisualStyle.lightened(color,100),
                        PlaylumiVisualStyle.lightened(color,470)});
        shape.setCornerRadius(dp(radius));
        shape.setStroke(dp(1),PlaylumiVisualStyle.BORDER);
        return shape;
    }
    private RippleDrawable primaryAction() {
        GradientDrawable shape=new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{PlaylumiVisualStyle.PRIMARY_START,
                        PlaylumiVisualStyle.PRIMARY_END});
        shape.setCornerRadius(dp(24));
        shape.setStroke(dp(1),0x60ffffff);
        return new RippleDrawable(ColorStateList.valueOf(0x30ffffff),shape,null);
    }
    private TextView text(String value,int size,int color) {
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create(size>=19?"sans-serif-medium":"sans-serif",
                size>=19?Typeface.BOLD:Typeface.NORMAL));
        t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(true);
        t.setLineSpacing(dp(2),1.06f);return t;
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
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        b.setLineSpacing(dp(2),1.07f);
        b.setGravity(description.isEmpty()?Gravity.CENTER:(Gravity.CENTER_VERTICAL|Gravity.START));
        b.setMaxLines(4);
        b.setPadding(dp(description.isEmpty()?17:20),dp(13),
                dp(description.isEmpty()?17:19),dp(13));
        b.setMinHeight(dp(PlaylumiLayout.tileHeight(minHeight,gameScreen())));
        // Consistent elevated cards instead of the former wobbly cloud buttons.
        GradientDrawable card=new GradientDrawable();
        card.setColor(PlaylumiVisualStyle.lightened(color,270));
        card.setCornerRadius(dp(22));
        card.setStroke(dp(1),PlaylumiVisualStyle.BORDER);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x1a176457),
                card,null));
        b.setElevation(dp(1));
        b.setContentDescription((title+" "+description).trim());
        PlaylumiMotion.tactile(b);
        return b;
    }
    private void addTile(Button button,int margin) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        int gap=PlaylumiLayout.margin(margin);
        p.setMargins(dp(2),dp(gap+2),dp(2),dp(gap+2));
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
        m.setImageResource(R.drawable.playlumi_brand_icon);
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
        boolean welcomeCard=page==HOME;
        holder.setGravity(welcomeCard?Gravity.CENTER_VERTICAL:Gravity.CENTER);
        if(welcomeCard) {
            holder.setPadding(dp(14),dp(10),dp(14),dp(10));
            holder.setBackground(softCard(PlaylumiVisualStyle.MINT,26));
            holder.setElevation(dp(1));
        }
        PlaylumiWardrobe display=page==SHOP && outfitPreview>=0 ?
                PlaylumiTryOn.previewWardrobe(wardrobe,selectedSlot,outfitPreview) : wardrobe;
        character=new PlaylumiCharacterView(this,display);
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
        if(welcomeCard) {
            LinearLayout welcome=new LinearLayout(this);
            welcome.setOrientation(LinearLayout.VERTICAL);
            welcome.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
            welcome.setPadding(dp(12),0,0,0);
            TextView greet=text("Hoi, avonturier!",21,TEXT);
            greet.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            greet.setMaxLines(2);
            welcome.addView(greet,new LinearLayout.LayoutParams(-1,-2));
            TextView subtitle=text("Jouw eigen speelwereld",14,SUB);
            subtitle.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            subtitle.setMaxLines(2);
            welcome.addView(subtitle,new LinearLayout.LayoutParams(-1,-2));
            TextView stars=text("  ⭐ "+wardrobe.stars()+" sterren  ",14,PlaylumiVisualStyle.SUCCESS);
            stars.setGravity(Gravity.CENTER);
            stars.setBackground(rounded(0xdfffffff,15));
            stars.setPadding(dp(4),dp(4),dp(4),dp(4));
            LinearLayout.LayoutParams badge=new LinearLayout.LayoutParams(-2,-2);
            badge.setMargins(0,dp(8),0,0);
            welcome.addView(stars,badge);
            holder.addView(welcome,new LinearLayout.LayoutParams(0,-2,1f));
        }
        if(page==HOME && !greeted) {
            greeted=true;
            final PlaylumiCharacterView helloFriend=character;
            final FrameLayout helloStage=stage;
            holder.postDelayed(()->{
                if(stage==helloStage && character==helloFriend) {
                    helloFriend.wave();
                    // A visual greeting, without an unsolicited sound.
                    PlaylumiDelight.sprinkle(stage,helloFriend);
                }
            },550L);
        }
        LinearLayout.LayoutParams row=new LinearLayout.LayoutParams(
                -1,dp(actual+(welcomeCard?16:4)));
        row.setMargins(dp(welcomeCard?3:0),dp(welcomeCard?6:1),
                dp(welcomeCard?3:0),dp(welcomeCard?9:1));
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
        return page==PLAY || page==MINI_PLAY || page==BUBBLE_PLAY ||
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
        row.setPadding(dp(9),dp(3),dp(9),dp(3));
        row.setBackground(softCard(0xffffffff,22));
        row.setElevation(dp(2));
        if(page!=HOME && (page!=AGE || prefs().contains("chosen_age"))) {
            TextView back=text("←",27,0xff60448e);
            back.setGravity(Gravity.CENTER);
            back.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                    softCard(0xffe0daff,14),null));
            back.setContentDescription("Terug naar het vorige scherm");
            back.setClickable(true);back.setFocusable(true);
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(42),dp(42));
            bp.setMargins(0,0,dp(9),0);
            row.addView(back,bp);
            back.setOnClickListener(v->onBackPressed());
        }
        TextView title=text(heading,gameHeader?19:21,TEXT);
        title.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        title.setMaxLines(1);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(48),1f);
        row.addView(title,tp);
        if(gameHeader) {
            if(subtitle!=null && !subtitle.isEmpty()) {
                TextView status=text(subtitle.replace("Uitdaging ",""),12,SUB);
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
            TextView homeShortcut=text("⌂",23,PlaylumiVisualStyle.INK);
            homeShortcut.setContentDescription("Ga naar start");
            homeShortcut.setBackground(softCard(0xffdceeff,13));
            homeShortcut.setClickable(true);homeShortcut.setFocusable(true);
            LinearLayout.LayoutParams shortcut=new LinearLayout.LayoutParams(
                    dp(36),dp(39));
            shortcut.setMargins(dp(5),0,0,0);
            row.addView(homeShortcut,shortcut);
            homeShortcut.setOnClickListener(v->move(HOME));
        } else if(page!=HOME) {
            View star=wardrobe==null?mascot(37):new PlaylumiCharacterView(this,wardrobe);
            LinearLayout.LayoutParams iconParams=
                    new LinearLayout.LayoutParams(dp(39),dp(39));
            iconParams.setMargins(dp(5),0,0,0);
            row.addView(star,iconParams);
        }
        LinearLayout.LayoutParams rowp=new LinearLayout.LayoutParams(-1,dp(57));
        rowp.setMargins(dp(3),dp(2),dp(3),dp(gameHeader?7:8));
        root.addView(row,rowp);
        if(!gameHeader && subtitle!=null && !subtitle.isEmpty())
            addText(subtitle,14,SUB,0,8);
    }
    /** A preview always appears at the TOP so the child actually sees it. */
    private void showPreview() {
        showPage();
        final ScrollView previewScroller=scroller;
        previewScroller.post(()->{
            if(scroller==previewScroller)previewScroller.scrollTo(0,0);
        });
    }

    /** A stable bottom navigation bar on menus; gameplay keeps maximum room. */
    private boolean showQuickNavigation() {
        return page!=AGE && page!=BREAK_SCREEN && page!=PARENT_GATE &&
                page!=PARENT_HOME && page!=PARENT_WORLDS && page!=PARENT_STATS &&
                page!=PARENT_SETTINGS && page!=PLAY && page!=MINI_PLAY &&
                page!=BUBBLE_PLAY && page!=MUSIC_PLAY && page!=STORY_PLAY &&
                page!=STORY_REACTION && page!=DRAW && page!=REWARD;
    }

    private int selectedQuickDestination() {
        switch(page) {
            case AVATAR:case CLOSET:case SHOP:case ROOM:case ROOM_CATS:
            case ROOM_ITEMS:case STAR_STAGE:case FRIEND_CARE:case FRIEND_ALBUM:
            case TROPHY_WORLDS:case TROPHY_CATS:
                return PlaylumiQuickNav.CUSTOMIZE;
            case GARDEN_MENU:case GARDEN_PLANT:
                return PlaylumiQuickNav.GARDEN;
            case MINI_MENU:case PICTURE_PLAY:case MUSIC_BOX:case WORLD_PICKER:
            case WORLD_HUB:case CATEGORIES:
            case DETAIL:case LEVEL:case VARIANTS:case ADVENTURE_MAP:
            case STORY_LIBRARY:case BADGES:
                return PlaylumiQuickNav.PLAY;
            default:return PlaylumiQuickNav.START;
        }
    }

    private void renderQuickNavigation() {
        quickNavBar=null;
        if(!showQuickNavigation())return;
        int current=selectedQuickDestination();
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(4),dp(4),dp(4),dp(4));
        nav.setBackground(softCard(0xffeff7ff,24));
        nav.setElevation(dp(12));
        for(int tab=0;tab<PlaylumiQuickNav.COUNT;tab++) {
            final int destination=tab;
            TextView button=text(PlaylumiQuickNav.ICONS[tab]+"\n"+
                    PlaylumiQuickNav.TITLES[tab],12,
                    tab==current?PlaylumiVisualStyle.INK:SUB);
            button.setGravity(Gravity.CENTER);
            button.setMaxLines(2);
            button.setLineSpacing(0,1f);
            button.setContentDescription(PlaylumiQuickNav.TITLES[tab]+
                    (tab==current?", geselecteerd":""));
            button.setClickable(true);
            button.setFocusable(true);
            button.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(0x22ffffff),
                    softCard(tab==current?0xffcde9ff:0xffffffff,18),null));
            LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,-1,1f);
            cell.setMargins(dp(2),0,dp(2),0);
            nav.addView(button,cell);
            button.setOnClickListener(v->{
                int target=destination==PlaylumiQuickNav.START?HOME:
                        destination==PlaylumiQuickNav.PLAY?PICTURE_PLAY:
                        destination==PlaylumiQuickNav.CUSTOMIZE?AVATAR:GARDEN_MENU;
                if(page!=target)move(target);
            });
        }
        FrameLayout.LayoutParams bar=new FrameLayout.LayoutParams(
                -1,dp(66),Gravity.BOTTOM);
        bar.setMargins(dp(10),0,dp(10),dp(8));
        stage.addView(nav,bar);
        quickNavBar=nav;
    }

    /** Horizontal, always visible shortcuts between the six/five customization sections. */
    private void addCategoryStrip(boolean outfitMode) {
        int slots=outfitMode?PlaylumiWardrobe.SLOTS:PlaylumiRoom.SLOTS;
        int selected=outfitMode?selectedSlot:roomSlot;
        HorizontalScrollView scroll=new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2),dp(3),dp(2),dp(3));
        for(int i=0;i<slots;i++) {
            final int target=i;
            String icon=outfitMode?PlaylumiWardrobe.SLOT_ICONS[i]:
                    PlaylumiRoom.SLOT_ICONS[i];
            String title=outfitMode?PlaylumiWardrobe.SLOT_NAMES[i]:
                    PlaylumiRoom.LABELS[i];
            Button tab=tile(icon+" "+title,"",i==selected?0xffc8f2e2:0xffeaf1ff,57);
            tab.setTextSize(13);
            tab.setMaxLines(2);
            tab.setGravity(Gravity.CENTER);
            tab.setMinWidth(dp(116));
            LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(dp(121),dp(60));
            cell.setMargins(dp(2),0,dp(2),0);
            row.addView(tab,cell);
            tab.setOnClickListener(v->{
                if(outfitMode) {
                    selectedSlot=target;outfitPreview=-1;shopMessage="";
                    outfitFilter=PlaylumiTryOn.ALL;
                } else {
                    roomSlot=target;roomPreview=-1;roomMessage="";
                    roomFilter=PlaylumiTryOn.ALL;
                }
                showPage();
            });
        }
        scroll.addView(row);
        LinearLayout.LayoutParams position=new LinearLayout.LayoutParams(-1,dp(67));
        position.setMargins(0,dp(2),0,dp(3));
        root.addView(scroll,position);
        scroll.post(()->scroll.smoothScrollTo(Math.max(0,selected-1)*dp(121),0));
    }

    /** See only owned items, or items still to unlock, without extra pages. */
    private void addItemFilter(boolean outfitMode,int slot) {
        LinearLayout row=new LinearLayout(this);
        for(int filter=0;filter<PlaylumiTryOn.FILTERS;filter++) {
            final int target=filter;
            int active=outfitMode?outfitFilter:roomFilter;
            int count=outfitMode?PlaylumiTryOn.wardrobeCount(wardrobe,slot,filter):
                    PlaylumiTryOn.roomCount(room,slot,filter);
            Button tab=tile(PlaylumiTryOn.FILTER_LABELS[filter]+" ("+count+")","",
                    filter==active?0xffd2f3df:0xffecf0fc,53);
            tab.setTextSize(12);
            tab.setPadding(dp(2),dp(4),dp(2),dp(4));
            LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,dp(55),1f);
            cell.setMargins(dp(2),0,dp(2),0);
            row.addView(tab,cell);
            tab.setOnClickListener(v->{
                if(outfitMode) outfitFilter=target;
                else roomFilter=target;
                redrawKeepingScroll();
            });
        }
        root.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }

    /** Compact, touch-friendly grid that shows every item without page cycling. */
    private void addOptionGrid(java.util.List<Button> options,int height) {
        int columns=PlaylumiQuickNav.columnsFor(
                getResources().getConfiguration().screenWidthDp);
        for(int start=0;start<options.size();start+=columns) {
            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.TOP);
            for(int column=0;column<columns;column++) {
                int index=start+column;
                View item=index<options.size()?options.get(index):new View(this);
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(
                        0,-2,1f);
                cell.setMargins(dp(3),dp(4),dp(3),dp(4));
                if(item instanceof Button)
                    ((Button)item).setMinHeight(dp(height));
                row.addView(item,cell);
            }
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
    }

    /**
     * The picture chooser uses true round animated shapes, not Button tiles.
     * Each remains a focusable, accessible click target with >140dp height.
     */
    private void addBubbleChoiceGrid(java.util.List<View> choices) {
        int columns=PlaylumiPicturePlay.columns(
                getResources().getConfiguration().screenWidthDp);
        for(int start=0;start<choices.size();start+=columns) {
            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            for(int column=0;column<columns;column++) {
                int index=start+column;
                View choice=index<choices.size()?choices.get(index):new View(this);
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,
                        dp(179),1f);
                cell.setMargins(dp(1),dp(3),dp(1),dp(3));
                row.addView(choice,cell);
            }
            root.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
    }

    /** Stay at the selected wardrobe / room option after changing an item. */
    private void redrawKeepingScroll() {
        final int position=scroller==null?0:scroller.getScrollY();
        showPage();
        final ScrollView visible=scroller;
        visible.post(()->{if(scroller==visible)visible.scrollTo(0,position);});
    }

    private void showPage() {
        showPage(true);
    }

    /** Rebuild music-box controls without cutting off the instrumental tune. */
    private void showMusicBoxWithoutStopping() {
        showPage(PlaylumiMusicBox.shouldStopForRefresh(page==MUSIC_BOX,true));
    }

    private void showPage(boolean stopAudio) {
        if(stopAudio) {
            melodyPlayback.stop();
            if(sound!=null) {
                sound.stopNotes();
            }
        }
        if(!parentAllowed && (page==PARENT_HOME || page==PARENT_WORLDS ||
                page==PARENT_STATS || page==PARENT_SETTINGS))page=PARENT_GATE;
        playPal=null;
        quickNavBar=null;
        stage=new FrameLayout(this);
        GradientDrawable bg=new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{PlaylumiVisualStyle.PAGE_TOP,
                        PlaylumiVisualStyle.PAGE_MIDDLE,
                        PlaylumiVisualStyle.PAGE_BOTTOM});
        stage.setBackground(bg);
        boolean wonderland=page==PICTURE_PLAY ||
                page==WORLD_PICKER || page==WORLD_HUB || page==CATEGORIES ||
                page==MINI_MENU || page==MUSIC_BOX || page==AVATAR || page==CLOSET ||
                page==SHOP || page==ROOM || page==ROOM_CATS ||
                page==ROOM_ITEMS || page==GARDEN_MENU || page==STAR_STAGE;
        if(wonderland) {
            stage.addView(new PlaylumiWonderlandView(this),
                    new FrameLayout.LayoutParams(-1,-1));
        } else if(page==STORY_LIBRARY || page==STORY_END ||
                page==REWARD || page==TROPHY_WORLDS) {
            stage.addView(new PlaylumiSkyView(this),
                    new FrameLayout.LayoutParams(-1,-1));
        }
        scroller=new ScrollView(this);scroller.setFillViewport(true);
        scroller.setClipToPadding(true);
        scroller.setVerticalScrollBarEnabled(false);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        int side=PlaylumiLayout.sidePadding(
                getResources().getConfiguration().screenWidthDp);
        root.setPadding(dp(side),dp(8),dp(side),dp(14));
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
                // Reserve an actual safe, scrollable viewport above system navigation.
                currentScroller.setPadding(0,top+dp(3),0,
                        bottom+dp(showQuickNavigation()?88:20));
                if(quickNavBar!=null && quickNavBar.getParent()==stage) {
                    FrameLayout.LayoutParams navPosition=
                            (FrameLayout.LayoutParams)quickNavBar.getLayoutParams();
                    navPosition.bottomMargin=bottom+dp(8);
                    quickNavBar.setLayoutParams(navPosition);
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
            case PICTURE_PLAY: showPicturePlay();break;
            case MUSIC_BOX: showMusicBox();break;
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
            default: throw new IllegalStateException("page");
        }
        renderQuickNavigation();
        stage.requestApplyInsets(); // Recheck system-bar clearance after adding tabs.
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
    /** Calm onboarding with no oversized sticker art or emoji-heavy controls. */
    private void showAge() {
        root.setGravity(Gravity.CENTER_VERTICAL);
        TextView eyebrow=text("P L A Y L U M I",14,
                PlaylumiVisualStyle.PRIMARY_START);
        eyebrow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams eyebrowParams=new LinearLayout.LayoutParams(-1,-2);
        eyebrowParams.setMargins(0,dp(8),0,dp(17));
        root.addView(eyebrow,eyebrowParams);

        TextView heading=text("Klaar om te ontdekken?",29,TEXT);
        heading.setGravity(Gravity.CENTER);
        heading.setMaxLines(2);
        root.addView(heading,new LinearLayout.LayoutParams(-1,-2));

        TextView intro=text("Kies de leeftijd die bij jou past.",16,SUB);
        LinearLayout.LayoutParams introParams=new LinearLayout.LayoutParams(-1,-2);
        introParams.setMargins(dp(12),dp(9),dp(12),dp(22));
        root.addView(intro,introParams);

        String[] descriptions={
                "Kleuren, vormen en eerste cijfers",
                "Woorden, tellen en puzzels",
                "Uitdagende spelletjes en raadsels"
        };
        int[] colors={PlaylumiVisualStyle.MINT,
                PlaylumiVisualStyle.SKY,PlaylumiVisualStyle.LAVENDER};
        for(int i=0;i<AGES.length;i++) {
            final int selected=i;
            Button b=tile(AGES[i]+"   →",descriptions[i],colors[i],97);
            b.setTextSize(18);
            addTile(b,6);
            b.setOnClickListener(v->{
                age=selected;difficulty=selected;
                prefs().edit().putInt("chosen_age",age).apply();
                move(HOME);
            });
        }
        addText("Je kunt dit later altijd veranderen.",13,SUB,17,8);
    }
    private void startEasyPlay() {
        int selected=PlaylumiEasyPlay.category(
                prefs().getInt("last_played_category",0));
        int variation=PlaylumiEasyPlay.variant(
                prefs().getInt("last_played_variant",0));
        pictureLaunch=false;
        storyStage=-1;smartDifficulty=true;roundHadMistake=false;
        pendingLevel=-1;sessionSolved=0;sessionAttempts=0;feedback="";
        pendingBadges.clear();
        difficulty=adaptive.level(selected,Math.max(0,age));
        game.configure(selected,variation,difficulty);
        saveRecent();
        rememberPlay();
        move(PLAY);
    }
    /** A focused, calm home screen: primary play first, three clear destinations. */
    private void showHome() {
        header("Playlumi","Spelen, ontdekken en groeien");
        characterHero(106);

        TextView section=text("Klaar voor een avontuur?",21,TEXT);
        section.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headingParams=new LinearLayout.LayoutParams(-1,-2);
        headingParams.setMargins(dp(6),dp(17),dp(6),dp(5));
        root.addView(section,headingParams);

        Button quick=tile("Speel meteen  →",
                "",PlaylumiVisualStyle.MINT,98);
        quick.setBackground(primaryAction());
        quick.setTextColor(Color.WHITE);
        quick.setTextSize(19);
        quick.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        quick.setContentDescription("Speel meteen een korte ronde van acht vragen");
        addTile(quick,6);
        quick.setOnClickListener(v->startEasyPlay());

        Button games=tile("Ontdek spelletjes",
                "Kies samen een nieuwe uitdaging",PlaylumiVisualStyle.SKY,100);
        games.setOnClickListener(v->move(PICTURE_PLAY));
        addTile(games,5);

        Button music=tile("De Muziekdoos",
                "Luister, speel en bewaar melodieën",PlaylumiVisualStyle.PEACH,100);
        music.setOnClickListener(v->move(MUSIC_BOX));
        addTile(music,5);

        TextView explore=text("Nog meer ontdekken",18,TEXT);
        explore.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams moreParams=new LinearLayout.LayoutParams(-1,-2);
        moreParams.setMargins(dp(6),dp(20),dp(6),dp(4));
        root.addView(explore,moreParams);

        Button worlds=tile("Leerwerelden",
                "Kleuren, letters en natuur",PlaylumiVisualStyle.LAVENDER,88);
        worlds.setOnClickListener(v->move(WORLD_PICKER));
        Button mini=tile("Beweeg & speel",
                "Kleine interactieve spelletjes",PlaylumiVisualStyle.MINT,88);
        mini.setOnClickListener(v->move(MINI_MENU));
        addTwo(worlds,mini,88);

        Button friend=tile("Mijn speelwereld",
                "Kleed je vriendje aan en versier je kamer",
                PlaylumiVisualStyle.LAVENDER,87);
        friend.setOnClickListener(v->move(AVATAR));
        addTile(friend,4);

        // Keep shortcuts and the child's history, but put them after the core flow.
        showFavoriteGames();
        showRecentGames(true);

        Button options=tile("Leeftijd en instellingen",
                "Voor ouders en de keuze van je leeftijd",
                PlaylumiVisualStyle.SURFACE,70);
        options.setOnClickListener(v->move(HOME_OPTIONS));
        addTile(options,8);
    }
    /**
     * The picture-first path uses just one tap after choosing a simple icon.
     * No forced level, world or variant screens. Older children retain all
     * advanced modes via the separate full-catalog button.
     */
    private void showPicturePlay() {
        header("🧸 Kies een plaatje",
                "Tik om te spelen • Houd vast voor een vrolijk deuntje!");
        java.util.List<View> choices=new java.util.ArrayList<>();
        int chosenAge=Math.max(0,Math.min(2,age));
        for(int i=0;i<PlaylumiPicturePlay.COUNT;i++) {
            final int selected=i;
            PlaylumiBubbleChoiceView bubble=new PlaylumiBubbleChoiceView(this,
                    PlaylumiPicturePlay.icon(chosenAge,i),
                    PlaylumiPicturePlay.label(chosenAge,i),
                    COLORS[i%COLORS.length],i);
            bubble.setOnClickListener(v->{sound.tap();startPictureGame(selected);});
            bubble.setOnLongClickListener(v->{sound.sample();return true;});
            choices.add(bubble);
        }
        addBubbleChoiceGrid(choices);
        Button all=tile("🎮 Alle spellen bekijken",
                "Meer spelletjes en moeilijkheidsgraden",0xffe5e3ff,79);
        addTile(all,7);
        all.setOnClickListener(v->move(MINI_MENU));
    }

    private void startPictureGame(int selection) {
        int chosenAge=Math.max(0,Math.min(2,age));
        int mode=PlaylumiPicturePlay.game(chosenAge,selection);
        int variation=PlaylumiPicturePlay.variant(chosenAge,selection);
        pictureLaunch=true;
        storyStage=-1;
        storyNewReward=false;
        smartDifficulty=false;
        roundHadMistake=false;
        pendingLevel=-1;
        sessionSolved=0;sessionAttempts=0;feedback="";
        pendingBadges.clear();
        difficulty=PlaylumiPicturePlay.difficulty(chosenAge);
        world=Math.min(2,mode/8);
        game.configure(mode,variation,difficulty);
        saveRecent();
        rememberPlay();
        prefs().edit().putInt("game_mode",mode)
                .putInt("game_variant",variation)
                .putInt("last_played_category",mode)
                .putInt("last_played_variant",variation)
                .putBoolean("smart_mode",false).apply();
        move(PLAY);
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
    /** All eight activities are directly visible per learning world. */
    private void showCategories() {
        header(WORLD_ICONS[world]+" "+WORLDS[world],
                "Kies een van de acht spellen.");
        java.util.List<Button> options=new java.util.ArrayList<>();
        for(int mode=world*8;mode<world*8+8;mode++) {
            final int chosen=mode;
            Button b=tile(ICONS[mode]+" "+
                    LearningGame.GAME_NAMES[mode],"",COLORS[mode%COLORS.length],111);
            b.setOnClickListener(v->selectCategory(chosen));
            options.add(b);
        }
        addOptionGrid(options,117);
    }
    private void selectCategory(int category) {
        pictureLaunch=false;
        storyStage=-1;
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
    /** All five play variants available without an extra paging screen. */
    private void showVariants() {
        header("🎲 Hoe wil je spelen?","Alle vijf spelvormen staan hieronder.");
        String[] names=variantNames();
        String[] icons={"🎯","🧩","✨","🌟","🚀"};
        java.util.List<Button> variants=new java.util.ArrayList<>();
        for(int i=0;i<names.length;i++) {
            final int selection=i;
            Button b=tile(icons[i]+" "+names[i],
                    "8 per ronde · 300 vragen op dit niveau",
                    COLORS[i%COLORS.length],109);
            b.setOnClickListener(v->{
                if(smartDifficulty)
                    difficulty=adaptive.level(game.mode(),Math.max(0,age));
                game.configure(game.mode(),selection,difficulty);
                roundHadMistake=false;pendingLevel=-1;
                saveRecent();
                prefs().edit().putInt("game_variant",selection)
                        .putInt("last_played_category",game.mode())
                        .putInt("last_played_variant",selection).apply();
                storyStage=-1;
                pictureLaunch=false;
                sessionSolved=0;sessionAttempts=0;feedback="";
                pendingBadges.clear();
                rememberPlay();
                move(PLAY);
            });
            variants.add(b);
        }
        addOptionGrid(variants,120);
    }
    private int sessionGoal() {
        return storyStage>=0?PlaylumiAdventure.QUESTIONS_PER_MISSION:SESSION_ROUNDS;
    }
    /**
     * Responsive game canvas: portrait stacks short cards; landscape splits the
     * question and answers into two columns. All answers remain tappable and the
     * ScrollView can still scroll when the user chooses large system fonts.
     */
    // Questions are only spoken by explicit actions on the speaker buttons.
    private void showPlay() {
        int mode=game.mode();
        if(sessionSolved>=sessionGoal()) {showCompleted();return;}
        LearningGame.Round round=game.round();
        if(hintRound!=round) {
            hintRound=round;
            hintHiddenMask=0;
        }
        hintButton=null;
        boolean wide=landscapeScreen();
        header(ICONS[mode]+" "+LearningGame.GAME_NAMES[mode],"");

        LinearLayout progressRow=new LinearLayout(this);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView counter=text("⭐ "+sessionSolved+" / "+sessionGoal(),15,
                PlaylumiVisualStyle.SUCCESS);
        sessionCounter=counter;
        counter.setGravity(Gravity.CENTER_VERTICAL);
        progressRow.setPadding(dp(10),dp(5),dp(10),dp(5));
        progressRow.setBackground(softCard(0xffffffff,18));
        progressRow.setElevation(dp(2));
        progressRow.addView(counter,new LinearLayout.LayoutParams(-2,dp(28)));
        roundStars=new PlaylumiStarTrail(this,sessionGoal(),sessionSolved);
        LinearLayout.LayoutParams bar=new LinearLayout.LayoutParams(0,dp(30),1f);
        bar.setMargins(dp(12),0,dp(2),0);
        progressRow.addView(roundStars,bar);
        LinearLayout.LayoutParams progressParams=new LinearLayout.LayoutParams(-1,dp(46));
        progressParams.setMargins(dp(3),0,dp(3),dp(10));
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
        TextView question=text(round.prompt,wide?18:20,TEXT);
        question.setText(PlaylumiType.emphasizedQuestion(round.prompt));
        question.setPadding(dp(14),dp(11),dp(14),dp(11));
        question.setBackground(new PlaylumiCloudDrawable(0xffe7e0fc));
        question.setElevation(dp(1));
        question.setMinHeight(dp(wide?80:68));
        question.setMaxLines(4);
        promptLine.addView(question,new LinearLayout.LayoutParams(0,-2,1f));
        addWidgetIn(questions,promptLine,0,dp(8));

        int pictureSize=round.display.length()>22?27:
                round.display.length()>12?30:wide?32:38;
        TextView picture=text(round.display,pictureSize,round.displayColor);
        picture.setPadding(dp(13),dp(12),dp(13),dp(12));
        picture.setBackground(new PlaylumiCloudDrawable(0xffe6f7fa));
        picture.setMinHeight(dp(wide?150:112));
        picture.setMaxLines(4);
        picture.setElevation(dp(2));
        picture.setContentDescription("Plaatje bij de opdracht: "+
                round.display.replace('\n',' '));
        // This image does not trigger human speech.
        addWidgetIn(questions,picture,0,dp(wide?3:8));

        feedbackText=text("👇 Kies het juiste antwoord",wide?15:16,SUB);
        feedbackText.setMinHeight(dp(38));
        feedbackText.setPadding(dp(10),dp(6),dp(10),dp(6));
        feedbackText.setBackground(softCard(0xfff1ebff,17));
        addWidgetIn(answers,feedbackText,0,dp(3));
        answerButtons.clear();
        answerLocked=round.isSolved();
        if(!answerLocked && PlaylumiHints.nextWrongOption(
                round.correctIndex,round.answers.size(),hintHiddenMask)>=0) {
            hintButton=tile("💡 Help mij","Maak de vraag makkelijker",0xffffedcf,60);
            hintButton.setTextSize(16);
            hintButton.setContentDescription("Help mij: haal een fout antwoord weg");
            hintButton.setOnClickListener(v->showGentleHint(round));
            addWidgetIn(answers,hintButton,dp(2),dp(5));
        }
        for(int i=0;i<round.answers.size();i+=2) {
            LinearLayout answerRow=new LinearLayout(this);
            answerRow.setGravity(Gravity.CENTER);
            for(int j=i;j<Math.min(i+2,round.answers.size());j++) {
                final int answer=j;
                String value=round.answers.get(j);
                Button button=tile(value,"",COLORS[(mode+j)%6],wide?60:70);
                button.setBackground(new RippleDrawable(
                        ColorStateList.valueOf(0x22ffffff),
                        new PlaylumiCloudDrawable(COLORS[(mode+j)%6]),null));
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
                if((hintHiddenMask&(1<<answer))!=0)
                    markHintedAnswer(button);
            }
            answers.addView(answerRow,new LinearLayout.LayoutParams(-1,-2));
        }
        // An optional way out of a hard question. Skipping cannot grant rewards.
        if(!answerLocked) {
            Button skip=tile("🍃 Andere vraag",
                    "Deze is lastig? Zonder straf verder spelen",
                    0xffe8f5eb,61);
            skip.setTextSize(16);
            skip.setContentDescription(
                    "Sla deze vraag over, zonder punten of straf. Probeer een andere vraag.");
            skip.setOnClickListener(v->skipDifficultQuestion(round));
            addWidgetIn(answers,skip,dp(5),dp(5));
        }
        nextButton=tile("🌟 Volgende!","",0xffcff4dd,66);
        nextButton.setBackground(primaryAction());
        nextButton.setTextColor(Color.WHITE);
        nextButton.setElevation(dp(4));
        LinearLayout.LayoutParams nextParams=new LinearLayout.LayoutParams(-1,-2);
        nextParams.setMargins(dp(4),dp(7),dp(4),dp(5));
        answers.addView(nextButton,nextParams);
        nextButton.setVisibility(answerLocked?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->continueAfterAnswer());
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
    /** Show a friendly cloud without touching the round's scoring or answers. */
    private void markHintedAnswer(Button button) {
        button.setEnabled(false);
        button.setFocusable(false);
        button.setText("☁️");
        button.setTextSize(26);
        button.setContentDescription("Deze keuze is weg");
        button.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        button.setAlpha(.44f);
    }
    private void showGentleHint(LearningGame.Round round) {
        if(answerLocked || game.round()!=round || round.isSolved())return;
        int choice=PlaylumiHints.nextWrongOption(
                round.correctIndex,round.answers.size(),hintHiddenMask);
        if(choice<0)return;
        hintHiddenMask|=1<<choice;
        markHintedAnswer(answerButtons.get(choice));
        String message="💛 Kijk eens! Nog "+
                (round.answers.size()-Integer.bitCount(hintHiddenMask))+
                " antwoorden om uit te kiezen.";
        feedbackText.setText(message);
        feedbackText.setTextColor(PlaylumiVisualStyle.SUCCESS);
        feedbackText.setBackground(softCard(PlaylumiVisualStyle.SUCCESS_TINT,17));
        feedbackText.announceForAccessibility(message);
        if(hintButton!=null && PlaylumiHints.nextWrongOption(
                round.correctIndex,round.answers.size(),hintHiddenMask)<0)
            hintButton.setVisibility(View.GONE);
    }
    /** A skipped question neither spends nor awards anything; it simply moves on. */
    private void skipDifficultQuestion(LearningGame.Round displayedRound) {
        if(answerLocked || game==null || game.round()!=displayedRound)return;
        if(!PlaylumiQuestionSkip.skip(game))return;
        hintRound=null;
        hintHiddenMask=0;
        feedback="";
        roundHadMistake=false;
        // Deliberately do not touch sessionSolved, scores, progress or badges.
        saveRecent();
        move(PLAY);
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
            if(hintButton!=null)hintButton.setVisibility(View.GONE);
            feedback="⭐ Goed gedaan! +3 🌟";
            if(smartDifficulty && storyStage<0) {
                if(adaptive.observe(mode,!roundHadMistake,Math.max(0,age))) {
                    pendingLevel=adaptive.level(mode,Math.max(0,age));
                    feedback="🌟 Wat knap! Een nieuwe uitdaging!";
                }
                saveAdaptive(mode);
            }
            if(sessionSolved==sessionGoal())sound.party();
            else sound.good();
            final int answerStreak = scores.get(mode).currentStreak;
            final boolean isSessionLast = sessionSolved == sessionGoal();
            final boolean highFive = HighFiveMotion.shouldPlay(answerStreak, isSessionLast);
            if (playPal != null) {
                if (highFive) playPal.highFive();
                else playPal.dance();
            }
            touched.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(0x35ffffff),
                    new PlaylumiCloudDrawable(0xffb9f5d1),null));
            for(Button b:answerButtons)b.setEnabled(false);
            feedbackText.setText(feedback);
            feedbackText.setTextColor(PlaylumiVisualStyle.SUCCESS);
            feedbackText.setBackground(softCard(PlaylumiVisualStyle.SUCCESS_TINT,17));
            PlaylumiMotion.pop(touched);
            PlaylumiMotion.pop(feedbackText);
            if(roundStars!=null)roundStars.showProgress(sessionSolved);
            if(sessionCounter!=null)
                sessionCounter.setText("⭐ "+sessionSolved+" / "+sessionGoal());
            // Exactly one visual celebration; scoring and progress are already saved.
            if(isSessionLast || answerStreak % 5 == 0) {
                PlaylumiMotion.celebrate(stage,answerStreak);
            } else if (highFive) {
                PlaylumiHighFiveView.show(stage,playPal);
            } else {
                PlaylumiFlyingStar.launch(stage,touched,roundStars,
                        sessionSolved,sessionGoal());
            }
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
            if(sessionSolved==sessionGoal()) {
                // The exact session-completion event advances ONE garden stage.
                // Existing manual-care controls and first-bloom reward remain valid.
                PlaylumiGardenJourney.Growth growth=
                        PlaylumiGardenJourney.afterCompletedRound(garden);
                if(growth!=null) {
                    saveGarden();
                    gardenRoundNotice="🌱 "+PlaylumiGarden.NAMES[growth.plant]+
                            " groeit! "+growth.toStage+" / "+PlaylumiGarden.STAGES;
                    if(growth.firstBloom) {
                        wardrobe.earnGardenBonus();
                        gardenRoundNotice="🌸 "+PlaylumiGarden.NAMES[growth.plant]+
                                " staat in bloei! +"+
                                PlaylumiGarden.FIRST_BLOOM_STARS+" sterren";
                    }
                } else gardenRoundNotice="🌸 Je hele tuin staat in bloei!";
            }
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
            feedbackText.setText(feedback);feedbackText.setTextColor(0xff915526);
            feedbackText.setBackground(softCard(PlaylumiVisualStyle.ERROR_TINT,17));
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
            if(!gardenRoundNotice.isEmpty())
                addText(gardenRoundNotice,18,PlaylumiVisualStyle.SUCCESS,4,9);
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
        if(!gardenRoundNotice.isEmpty())
            addText(gardenRoundNotice,18,PlaylumiVisualStyle.SUCCESS,4,9);
        addText("🌟 "+sessionGoal()+" vragen goed!",27,0xff4b3488,9,9);
        addText("Dat was knap! Zin in nog een ronde?",17,SUB,0,12);
        Button again=tile("🔄 Nog een ronde!","",0xffcbf1d9,92);
        addTile(again,6);
        PlaylumiDelight.invite(again);
        again.setOnClickListener(v->{
            sessionSolved=0;sessionAttempts=0;
            if(smartDifficulty && pendingLevel>=0) {
                difficulty=pendingLevel;
                pendingLevel=-1;
            }
            game.configure(game.mode(),game.variant(),difficulty);
            saveRecent();
            move(PLAY);
        });
        Button home=tile("🌈 Ander spelletje","",0xffeadbff,82);
        addTile(home,6);
        home.setOnClickListener(v->move(pictureLaunch?PICTURE_PLAY:CATEGORIES));
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
        active.post(()->{
            if(stage==active)PlaylumiMotion.celebrate(active,10);
        });
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

    /** A customization hub where every destination is obvious at first glance. */
    private void showAvatar() {
        header("🎨 Aanpassen","Maak je eigen Playlumi-wereld.");
        characterHero(143);
        addText("⭐ "+wardrobe.stars()+" speelsterren  ·  "+
                wardrobe.unlockedItems()+" vrijgespeelde spullen",17,
                PlaylumiVisualStyle.SUCCESS,1,7);

        Button dress=tile("🎀 Mijn ster aankleden",
                "Kleur, bril, hoed, kleding, vriendjes en gloed",
                0xffd7f5e5,99);
        dress.setOnClickListener(v->move(CLOSET));
        Button roomButton=tile("🏠 Mijn kamer",
                "Muren, vloeren, meubels en versiering",
                0xffd9ecff,99);
        roomButton.setOnClickListener(v->move(ROOM_CATS));
        addTile(dress,5);
        addTile(roomButton,5);

        Button gardenButton=tile("🌱 Magische Tuin",
                "Bekijk en verzorg je twaalf planten",0xffdff3cf,94);
        gardenButton.setOnClickListener(v->move(GARDEN_MENU));
        Button friendButton=tile("🐾 Vriendjes",
                "Spelen en verzorgen",0xffffe7c4,94);
        friendButton.setOnClickListener(v->{
            carePage=0;careMessage="";move(FRIEND_CARE);
        });
        addTwo(gardenButton,friendButton,94);

        Button stageButton=tile("🎭 Sterrenpodium","Dans en zwaai",
                0xffe8ddff,90);
        stageButton.setOnClickListener(v->move(STAR_STAGE));
        Button trophies=tile("🏆 Trofeeën","Bekijk je badges",
                0xffffe8b8,90);
        trophies.setOnClickListener(v->move(TROPHY_WORLDS));
        addTwo(stageButton,trophies,90);
    }

    /** One-tap outfit presets, never bought, never equipped from a preview. */
    private void addSavedLooks() {
        addText("🎀 Mijn bewaarde outfits",20,TEXT,4,4);
        for(int i=0;i<PlaylumiSavedLooks.SLOTS;i++) {
            final int slot=i;
            String current=savedLooks.describe(slot);
            addText("Outfit "+(slot+1)+" · "+current,14,SUB,2,3);
            Button save=tile("💾 Bewaar look "+(slot+1),"Huidige kleding bewaren",
                    0xffd9eaff,76);
            Button wear=tile(savedLooks.isFilled(slot)?
                    "✨ Draag look "+(slot+1):"🔒 Nog leeg",
                    savedLooks.isFilled(slot)?"Gratis aantrekken":"Bewaar eerst een look",
                    0xffd7f4e7,76);
            save.setOnClickListener(v->{
                savedLooks.save(slot,wardrobe);
                prefs().edit().putString("outfit_presets",savedLooks.serialize()).apply();
                sound.sparkle();redrawKeepingScroll();
            });
            wear.setEnabled(savedLooks.isFilled(slot));
            wear.setOnClickListener(v->{
                if(savedLooks.wear(slot,wardrobe)) {
                    saveWardrobe();
                    sound.sparkle();
                    redrawKeepingScroll();
                }
            });
            addTwo(save,wear,80);
        }
        addText("Bewaarde looks gebruiken alleen spullen die je bezit.",
                13,SUB,2,6);
    }

    /** All six wardrobe categories at once, without page arrows. */
    private void showCloset() {
        header("🎀 Mijn kledingkast","Tik wat je wilt veranderen.");
        characterHero(117);
        addText("⭐ "+wardrobe.stars()+" speelsterren",18,
                PlaylumiVisualStyle.SUCCESS,0,8);
        addSavedLooks();
        java.util.List<Button> options=new java.util.ArrayList<>();
        for(int slot=0;slot<PlaylumiWardrobe.SLOTS;slot++) {
            final int selected=slot;
            String title=PlaylumiWardrobe.SLOT_ICONS[slot]+" "+
                    PlaylumiWardrobe.SLOT_NAMES[slot];
            String equipped=PlaylumiWardrobe.ITEM_NAMES[slot][wardrobe.equipped(slot)];
            Button b=tile(title,"Nu: "+equipped,COLORS[slot%COLORS.length],89);
            b.setOnClickListener(v->{
                selectedSlot=selected;shopMessage="";outfitPreview=-1;
                outfitFilter=PlaylumiTryOn.ALL;move(SHOP);
            });
            options.add(b);
        }
        addOptionGrid(options,98);
    }

    /**
     * First tap tries a locked item on the star without buying it.
     * Only the large, clearly priced confirmation spends earned stars.
     */
    private void showShop() {
        final int slot=selectedSlot;
        header("🎀 Stervriend aanpassen","Probeer alles eerst rustig uit.");
        addCategoryStrip(true);
        characterHero(169);
        addText("⭐ "+wardrobe.stars()+" speelsterren",18,
                PlaylumiVisualStyle.SUCCESS,0,5);
        if(outfitPreview>=0 && !wardrobe.owned(slot,outfitPreview)) {
            final int item=outfitPreview;
            addText(PlaylumiTryOn.previewLabel(false,
                    PlaylumiWardrobe.ITEM_NAMES[slot][item]),17,TEXT,1,6);
            int cost=wardrobe.price(slot,item);
            Button confirm=tile("⭐ Vrijspelen voor "+cost+" sterren",
                    "Pas na deze knop gebruik je sterren",0xffc8efd9,79);
            addTile(confirm,4);
            confirm.setOnClickListener(v->{
                int result=wardrobe.buyOrEquip(slot,item);
                if(result==PlaylumiWardrobe.NOT_ENOUGH) {
                    shopMessage="💛 Nog "+Math.max(0,cost-wardrobe.stars())+
                            " speelsterren nodig.";
                } else {
                    saveWardrobe();
                    sound.unlock();
                    outfitPreview=-1;
                    shopMessage="🎉 Nieuw item! Je ster draagt het nu.";
                }
                redrawKeepingScroll();
            });
        } else {
            addText("👀 Tik op een item om het te zien. Wat je bezit is gratis.",
                    14,SUB,0,5);
        }
        if(!shopMessage.isEmpty())addText(shopMessage,16,0xff5b3b90,0,6);
        addItemFilter(true,slot);
        java.util.List<Button> items=new java.util.ArrayList<>();
        for(int item=0;item<PlaylumiWardrobe.OPTIONS;item++) {
            final int chosen=item;
            boolean owned=wardrobe.owned(slot,item);
            if(!PlaylumiTryOn.showItem(owned,outfitFilter))continue;
            String title=PlaylumiWardrobe.ITEM_ICONS[slot][item]+" "+
                    PlaylumiWardrobe.ITEM_NAMES[slot][item];
            String status=wardrobe.equipped(slot)==item?"✅ Nu aan":
                    outfitPreview==item?"👀 Bekijk hierboven":
                    PlaylumiQuickNav.wardrobeStatus(wardrobe,slot,item);
            Button button=tile(title,status,
                    outfitPreview==item?0xfffae4b5:
                    wardrobe.equipped(slot)==item?0xffc8f1d8:
                    owned?0xffe6f8f1:COLORS[(slot+item)%COLORS.length],93);
            button.setOnClickListener(v->{
                if(wardrobe.owned(slot,chosen)) {
                    wardrobe.buyOrEquip(slot,chosen); // free for an owned item
                    saveWardrobe();
                    outfitPreview=-1;
                    shopMessage="✨ Aangetrokken!";
                    sound.sparkle();
                } else {
                    outfitPreview=chosen; // no mutation of real wardrobe/wallet
                    shopMessage="";
                    sound.tap();
                    showPreview();
                    return;
                }
                redrawKeepingScroll();
            });
            items.add(button);
        }
        addOptionGrid(items,102);
        Button back=tile("🎀 Terug naar alle categorieën","",0xffe7dfff,65);
        addTile(back,5);back.setOnClickListener(v->move(CLOSET));
    }


    private int miniCategory() {
        return miniKind==PlaylumiMiniChallenge.DRAG?LearningGame.SHAPES:
                miniKind==PlaylumiMiniChallenge.BALLOONS?LearningGame.NUMBERS:LearningGame.PATTERNS;
    }
    private void showMiniMenu() {
        header("🎮 Spelen","Kies hoe jij wilt spelen.");
        Button simple=tile("🧸 Spelen met plaatjes","Groot, duidelijk en meteen starten",
                0xffffdfb3,113);
        addTile(simple,4);
        simple.setOnClickListener(v->move(PICTURE_PLAY));
        showFavoriteGames();
        showRecentGames(false);
        Button learn=tile("🌈 Leerwerelden","Tellen, taal, vormen en dieren",
                0xffe5e3ff,84);
        addTile(learn,5);
        learn.setOnClickListener(v->move(WORLD_PICKER));
        addText("🎪 Minispellen",18,TEXT,4,5);
        String[] names={"Sleep de vormen","Tik de ballon","Dierenmemory"};
        String[] notes={"300 geschudde vormborden","300 geschudde ballonborden",
                "300 geschudde memoryborden"};
        Button[] first=new Button[3];
        for(int i=0;i<3;i++) {
            final int kind=i;
            first[i]=tile(PlaylumiMiniChallenge.ICONS[i]+"  "+names[i],
                    notes[i],COLORS[i],104);
            first[i].setOnClickListener(v->{
                miniKind=kind;miniRound=0;miniSolved=false;
                pendingBadges.clear();
                startMiniDeck();
                move(MINI_PLAY);
            });
        }
        Button bubblesButton=tile("🫧 Bellenfeest","Laat bellen knappen",
                0xffd4ebff,104);
        bubblesButton.setOnClickListener(v->{
            bubbleRound=0;bubbleSolved=false;
            startBubbleDeck();
            pendingBadges.clear();
            move(BUBBLE_PLAY);
        });
        Button musicButton=tile("🎵 Muziekatelier","Speel een melodie",
                0xffefdcff,104);
        musicButton.setOnClickListener(v->{
            melodyRound=0;melodySolved=false;
            pendingBadges.clear();
            startMelodyDeck();
            move(MUSIC_PLAY);
        });
        addTwo(first[0],first[1],103);
        addTwo(first[2],bubblesButton,103);
        addTile(musicButton,7);
        Button musicBox=tile("🎼 De Muziekdoos",
                "Achttien instrumentale melodietjes om zelf te kiezen",
                0xffffe7b5,88);
        addTile(musicBox,7);
        musicBox.setOnClickListener(v->move(MUSIC_BOX));
    }

    /** Rebuild a 300-board deck on every entry, using a fresh shuffle. */
    /** Child-controlled instrumental music and profile-local favorites. */
    private void showMusicBox() {
        header("🎼 De Muziekdoos","Kies muziek of bewaar een deuntje met een ster!");
        Button filter=tile(musicBoxFavoritesOnly?
                "🎼 Bekijk alle 18 deuntjes":
                "⭐ Mijn favorieten ("+musicBoxFavorites.count()+")",
                musicBoxFavoritesOnly?"Terug naar alle melodietjes":
                        "Bekijk alleen jouw favoriete muziek",0xffffe7b5,78);
        addTile(filter,5);
        filter.setOnClickListener(v->{
            musicBoxFavoritesOnly=!musicBoxFavoritesOnly;
            musicBoxPage=0;
            showMusicBoxWithoutStopping();
        });
        addText(musicBoxFavoritesOnly?
                "⭐ Jouw favoriete deuntjes":"🎵 Tik op een deuntje om te luisteren",
                18,SUB,4,7);
        TextView status=text(musicBoxTrack<0?
                "✨ Welke melodie kies jij?":
                "🎼 Laatst gekozen: "+PlaylumiMusicBox.name(musicBoxTrack),
                17,0xff5b4287);
        status.setBackground(softCard(0xfff1e7ff,20));
        status.setPadding(dp(11),dp(13),dp(11),dp(13));
        status.setMinHeight(dp(56));
        addWidget(status,2,10);

        // The star button is explicit; a long press on a tune offers the
        // same shortcut without playing sound or changing rewards.
        Button favoriteAction=tile("⭐ Kies eerst een deuntje",
                "Tik op een melodie en bewaar hem als favoriet",
                0xffffeed6,69);
        int numberOfPages=musicBoxFavorites.pages(musicBoxFavoritesOnly);
        if(musicBoxPage>=numberOfPages)musicBoxPage=0;
        int[] visible=musicBoxFavorites.onPage(musicBoxFavoritesOnly,musicBoxPage);
        Button[] visibleTrackTiles=new Button[PlaylumiMusicBox.COUNT];
        if(visible.length==0)
            addText("⭐ Nog geen favorieten. Bekijk alle deuntjes en geef er een ster!",
                    16,SUB,7,9);
        for(int i=0;i<visible.length;i+=2) {
            final int left=visible[i];
            Button one=musicBoxTile(left,status,favoriteAction,visibleTrackTiles);
            if(i+1<visible.length) {
                final int right=visible[i+1];
                Button two=musicBoxTile(right,status,favoriteAction,visibleTrackTiles);
                addTwo(one,two,109);
            } else addTile(one,6);
        }
        addTile(favoriteAction,6);
        refreshFavoriteAction(favoriteAction);
        refreshMusicBoxSelection(visibleTrackTiles);
        favoriteAction.setOnClickListener(v->{
            if(musicBoxTrack>=0)toggleMusicFavorite(musicBoxTrack);
        });

        Button findChosen=tile("🎯 Zoek gekozen deuntje",
                "Ga naar de pagina met jouw laatst gekozen muziek",
                0xfff1e7ff,72);
        addTile(findChosen,5);
        findChosen.setEnabled(musicBoxTrack>=0);
        findChosen.setOnClickListener(v->{
            int target=musicBoxFavorites.pageOf(musicBoxFavoritesOnly,
                    musicBoxTrack);
            if(target<0 && musicBoxFavoritesOnly) {
                // A chosen tune can be outside the favorite filter.
                musicBoxFavoritesOnly=false;
                target=musicBoxFavorites.pageOf(false,musicBoxTrack);
            }
            if(target>=0) {
                musicBoxPage=target;
                showMusicBoxWithoutStopping();
            }
        });

        Button surprise=tile(musicBoxFavoritesOnly?
                "🎲 Verras me met een favoriet":"🎲 Verrassingsdeuntje",
                musicBoxFavoritesOnly && musicBoxFavorites.count()==0?
                        "Bewaar eerst een deuntje met een ster":
                        "Speel iets anders dan de vorige keer",0xffe4dcff,82);
        Button stop=tile("⏹ Stop muziek",
                "Stop het deuntje meteen",0xffdaf2e7,82);
        addTwo(surprise,stop,87);
        Button previousTune=tile("⬅ Vorig deuntje",
                "Luister naar het vorige deuntje",0xffe5ecff,78);
        Button nextTune=tile("Volgend deuntje ➡",
                "Luister naar het volgende deuntje",0xffdff4ec,78);
        addTwo(previousTune,nextTune,84);
        if(musicBoxFavoritesOnly && musicBoxFavorites.count()==0) {
            previousTune.setEnabled(false);
            nextTune.setEnabled(false);
        }
        previousTune.setOnClickListener(v->
                playAdjacentMusicBox(-1,status,favoriteAction,visibleTrackTiles));
        nextTune.setOnClickListener(v->
                playAdjacentMusicBox(1,status,favoriteAction,visibleTrackTiles));
        surprise.setOnClickListener(v->{
            int chosen=musicBoxFavorites.surprise(musicBoxFavoritesOnly,
                    musicBoxTrack,new Random().nextInt());
            if(chosen<0) {
                status.setText("⭐ Bewaar eerst een deuntje bij alle melodieën!");
                return;
            }
            chooseMusicBox(chosen,status);
            refreshMusicBoxAfterSelection(favoriteAction,visibleTrackTiles);
        });
        stop.setOnClickListener(v->{
            melodyPlayback.stop();
            sound.stopNotes();
            status.setText("🍃 Gestopt. Kies een nieuw deuntje!");
        });
        if(numberOfPages>1) {
            Button previousPage=tile("⬅ Vorige pagina",
                    "Bekijk de vorige zes deuntjes",0xffe5ecff,70);
            Button nextPage=tile("Volgende pagina ➡",
                    "Bekijk de volgende zes deuntjes",0xffdff4ec,70);
            addTwo(previousPage,nextPage,78);
            addText("Pagina "+(musicBoxPage+1)+" van "+numberOfPages,
                    15,SUB,5,6);
            previousPage.setOnClickListener(v->{
                musicBoxPage=musicBoxFavorites.movePage(musicBoxFavoritesOnly,
                        musicBoxPage,-1);
                showMusicBoxWithoutStopping();
            });
            nextPage.setOnClickListener(v->{
                musicBoxPage=musicBoxFavorites.movePage(musicBoxFavoritesOnly,
                        musicBoxPage,1);
                showMusicBoxWithoutStopping();
            });
        }
        showMusicComposer();
        if(!sound.isEnabled())
            addText("🔇 Geluid staat uit. Zet geluid aan bij Instellingen.",
                    15,SUB,5,8);
    }

    private void playAdjacentMusicBox(int direction,TextView status,
                                      Button starAction,Button[] visibleTrackTiles) {
        int chosen=musicBoxFavorites.adjacent(musicBoxFavoritesOnly,
                musicBoxTrack,direction);
        if(chosen<0) {
            status.setText("⭐ Bewaar eerst een favoriet om te bladeren!");
            return;
        }
        chooseMusicBox(chosen,status);
        refreshMusicBoxAfterSelection(starAction,visibleTrackTiles);
    }

    private Button musicBoxTile(int index,TextView status,Button starAction,
                                Button[] visibleTrackTiles) {
        Button tile=tile(musicBoxTrackLabel(index),"",
                COLORS[index%COLORS.length],104);
        tile.setTextSize(17);
        tile.setContentDescription("Speel de melodie "+PlaylumiMusicBox.name(index)+
                ". Houd ingedrukt om favoriet te maken of te verwijderen.");
        tile.setOnClickListener(v->{
            chooseMusicBox(index,status);
            refreshMusicBoxAfterSelection(starAction,visibleTrackTiles);
        });
        visibleTrackTiles[index]=tile;
        tile.setOnLongClickListener(v->{
            toggleMusicFavorite(index);
            return true;
        });
        return tile;
    }

    private String musicBoxTrackLabel(int index) {
        return PlaylumiMusicBox.displayTitle(index,musicBoxTrack==index,
                musicBoxFavorites.isFavorite(index));
    }

    private void refreshMusicBoxSelection(Button[] visibleTrackTiles) {
        for(int track=0;track<visibleTrackTiles.length;track++) {
            if(visibleTrackTiles[track]!=null)
                visibleTrackTiles[track].setText(musicBoxTrackLabel(track));
        }
    }

    /** Keep the chosen tune visible without interrupting its playback. */
    private void refreshMusicBoxAfterSelection(Button starAction,
                                              Button[] visibleTrackTiles) {
        int pageOfTrack=musicBoxFavorites.pageOf(musicBoxFavoritesOnly,
                musicBoxTrack);
        if(pageOfTrack>=0 && pageOfTrack!=musicBoxPage) {
            musicBoxPage=pageOfTrack;
            showMusicBoxWithoutStopping();
            return;
        }
        refreshFavoriteAction(starAction);
        refreshMusicBoxSelection(visibleTrackTiles);
    }

    private void refreshFavoriteAction(Button starAction) {
        if(musicBoxTrack<0) {
            starAction.setText("⭐ Kies eerst een deuntje");
            starAction.setEnabled(false);
        } else {
            starAction.setEnabled(true);
            starAction.setText(musicBoxFavorites.isFavorite(musicBoxTrack)?
                    "💛 Verwijder gekozen favoriet":"⭐ Bewaar gekozen deuntje");
        }
    }

    private void toggleMusicFavorite(int index) {
        musicBoxFavorites.toggle(index);
        prefs().edit().putInt(PlaylumiMusicFavorites.PREF_KEY,
                musicBoxFavorites.mask()).apply();
        // Keep the current instrumental tune playing while updating star icons.
        showMusicBoxWithoutStopping();
    }

    private void chooseMusicBox(int index,TextView status) {
        melodyPlayback.stop(); // Stop any queued notes from a custom composition.
        if(sound.playMusicBox(index)) {
            musicBoxTrack=index;
            status.setText("🎶 Gekozen: "+PlaylumiMusicBox.title(index));
            PlaylumiDelight.hop(status);
        } else if(!sound.isEnabled()) {
            // A child can still select and bookmark a song when muted.
            musicBoxTrack=index;
            status.setText("🔇 Deuntje gekozen. Zet geluid aan om te luisteren.");
        } else {
            status.setText("🎵 Deuntje wordt geladen. Probeer nog eens!");
        }
    }


    /** Four playful animal notes, locally saved per child, without rewards. */
    private void showMusicComposer() {
        addText("🎹 Mijn drie deuntjes",21,TEXT,13,6);
        addText("Kies een plekje om jouw eigen muziek te bewaren.",15,SUB,3,6);
        Button[] songSlots=new Button[PlaylumiMelodyAlbum.SLOTS];
        for(int i=0;i<songSlots.length;i++) {
            final int index=i;
            songSlots[i]=tile(PlaylumiMelodyAlbum.title(i)+
                            (melodyAlbum.selected()==i?" ✓":""),
                    melodyAlbum.noteCount(i)+" van 12 tonen",
                    0xffe5ecff,82);
            songSlots[i].setContentDescription(
                    PlaylumiMelodyAlbum.title(i)+", "+
                    melodyAlbum.noteCount(i)+" noten. Tik om dit deuntje te kiezen.");
            songSlots[i].setOnClickListener(v->selectMyMelody(index));
        }
        addTwo(songSlots[0],songSlots[1],85);
        addTile(songSlots[2],5);
        addText("🎵 Tik op de dieren om maximaal twaalf tonen te combineren.",
                15,SUB,4,7);
        TextView composition=text(melodyAlbum.current().description(),17,0xff65418f);
        composition.setBackground(softCard(0xffe9f3ff,20));
        composition.setPadding(dp(12),dp(14),dp(12),dp(14));
        composition.setMinHeight(dp(72));
        addWidget(composition,5,9);
        Button[] instruments=new Button[PlaylumiMelody.NOTES];
        for(int i=0;i<PlaylumiMelody.NOTES;i++) {
            final int note=i;
            instruments[i]=tile(PlaylumiMelody.ANIMALS[i]+" "+
                    PlaylumiMelody.NAMES[i],"",COLORS[i],108);
            instruments[i].setTextSize(18);
            instruments[i].setContentDescription(
                    "Voeg de toon "+PlaylumiMelody.NAMES[i]+" toe aan jouw deuntje");
            instruments[i].setOnClickListener(v->recordMusicNote(note,composition));
        }
        addTwo(instruments[0],instruments[1],110);
        addTwo(instruments[2],instruments[3],110);
        Button pause=tile("🤫 Rust",
                "Voeg een korte stille tel toe aan jouw melodie",0xfff0e9fa,75);
        addTile(pause,5);
        pause.setContentDescription("Voeg een rustmoment zonder geluid toe");
        pause.setOnClickListener(v->recordMusicNote(PlaylumiMusicComposer.REST,
                composition));
        Button idea=tile("🎲 Probeer een ritme-idee",
                "Voeg vier vrolijke tonen met een pauze toe",0xffdff4ec,76);
        addTile(idea,5);
        idea.setOnClickListener(v->{
            melodyPlayback.stop();
            sound.stopNotes();
            int ideaIndex=(rhythmIdeaTurn++ & Integer.MAX_VALUE)%
                    PlaylumiRhythmIdea.COUNT;
            if(!PlaylumiRhythmIdea.append(melodyAlbum.current(),ideaIndex)) {
                composition.setText("🎶 Geen plek voor vier tonen. Haal een noot weg!");
            } else {
                saveMyMelody();
                composition.setText("✨ Nieuw ritme! "+
                        melodyAlbum.current().description());
            }
        });
        Button tempo=tile("🥁 Tempo: "+PlaylumiComposerTempo.label(composerTempo),
                "Tik voor rustig, normaal of snel",0xffe5eeff,70);
        addTile(tempo,5);
        tempo.setOnClickListener(v->{
            melodyPlayback.stop();
            sound.stopNotes();
            composerTempo=PlaylumiComposerTempo.next(composerTempo);
            prefs().edit().putInt("music_box_tempo",composerTempo).apply();
            showPage();
        });
        Button replay=tile("▶ Speel mijn deuntje",
                "Luister naar je zelfgemaakte melodie",0xffd5f0df,82);
        Button stopOwn=tile("⏹ Stop",
                "Stop mijn deuntje meteen",0xffffe6dc,82);
        addTwo(replay,stopOwn,85);
        replay.setOnClickListener(v->playMyMelody(composition));
        stopOwn.setOnClickListener(v->{
            melodyPlayback.stop();
            sound.stopNotes();
            composition.setText("🍃 Muziek gestopt. "+melodyAlbum.current().description());
        });
        Button undo=tile("↩ Laatste noot weg","",0xffe4eaff,72);
        Button erase=tile("🗑 Begin opnieuw","",0xffffe3db,72);
        addTwo(undo,erase,77);
        undo.setOnClickListener(v->{
            melodyPlayback.stop();
            sound.stopNotes();
            if(melodyAlbum.current().undo()) {
                saveMyMelody();
                composition.setText(melodyAlbum.current().description());
            } else composition.setText("🎶 Kies eerst een dier om muziek te maken.");
        });
        erase.setOnClickListener(v->{
            melodyPlayback.stop();
            sound.stopNotes();
            melodyAlbum.current().clear();
            saveMyMelody();
            composition.setText(melodyAlbum.current().description());
        });
        addText("💾 Jouw drie deuntjes worden apart bewaard voor dit kind.",
                13,SUB,4,10);
    }

    private void selectMyMelody(int slot) {
        if(slot==melodyAlbum.selected())return;
        melodyPlayback.stop();
        sound.stopNotes();
        melodyAlbum.select(slot);
        prefs().edit().putInt("music_box_selected_slot",slot).apply();
        showPage(); // No sound starts when switching songs.
    }

    private void saveMyMelody() {
        prefs().edit().putString(
                PlaylumiMelodyAlbum.key(melodyAlbum.selected()),
                melodyAlbum.current().encode()).apply();
    }

    private void recordMusicNote(int note,TextView status) {
        melodyPlayback.stop();
        sound.stopNotes();
        if(!melodyAlbum.current().add(note)) {
            status.setText("🎶 Je hebt twaalf tonen! Speel ze af of haal er een weg.");
            return;
        }
        saveMyMelody();
        status.setText(melodyAlbum.current().description());
        if(sound.isEnabled() && note!=PlaylumiMusicComposer.REST)
            sound.note(note);
    }

    /** The child explicitly starts playback; old queued notes are token-guarded. */
    private void playMyMelody(TextView status) {
        if(melodyAlbum.current().size()==0) {
            status.setText("🎵 Tik eerst op een paar dieren om een deuntje te maken!");
            return;
        }
        if(!sound.isEnabled()) {
            status.setText("🔇 Zet het geluid aan om jouw deuntje te horen.");
            return;
        }
        final FrameLayout screen=stage;
        final int[] notes=melodyAlbum.current().snapshot();
        final long token=melodyPlayback.begin();
        sound.stopNotes();
        status.setText("🎶 Daar komt jouw eigen deuntje!");
        final int beatMillis=PlaylumiComposerTempo.delayMillis(composerTempo);
        for(int i=0;i<notes.length;i++) {
            final int index=i;
            screen.postDelayed(()->{
                if(stage!=screen || page!=MUSIC_BOX ||
                        !melodyPlayback.isActive(token) || !sound.isEnabled())return;
                if(notes[index]==PlaylumiMusicComposer.REST)
                    sound.stopNotes(); // A real silent beat.
                else sound.note(notes[index]);
                status.setText("🎵 "+PlaylumiMusicComposer.symbolFor(notes[index])+
                        " ("+(index+1)+" / "+notes.length+")");
            },i*(long)beatMillis);
        }
        screen.postDelayed(()->{
            if(stage==screen && page==MUSIC_BOX &&
                    melodyPlayback.isActive(token)) {
                melodyPlayback.stop();
                status.setText("🌟 Goed gedaan! "+melodyAlbum.current().description());
            }
        },notes.length*(long)beatMillis+160L);
    }

    private void startMiniDeck() {
        miniDeck=MiniChallengeDeck.create(miniKind,Math.max(0,Math.min(2,age)),new Random());
        mini=miniDeck.next();
    }
    private void showMiniPlay() {
        if(mini==null) {
            if(miniDeck==null)startMiniDeck();
            else mini=miniDeck.next();
        }
        header(PlaylumiMiniChallenge.ICONS[miniKind]+" "+PlaylumiMiniChallenge.NAMES[miniKind],
                "Uitdaging "+Math.min(miniRound+1,20)+" / 20");
        if(miniRound>=20) {
            characterHero(150);
            addText("🎉 Alle uitdagingen gespeeld!",25,0xff4e3889,13,9);
            Button again=tile("🔄 Nog eens spelen!","",0xffd4f5e0,96);
            addTile(again,9);
            again.setOnClickListener(v->{
                miniRound=0;miniSolved=false;
                startMiniDeck();
                move(MINI_PLAY);
            });
            Button back=tile("🎪 Andere minigame","",0xffe8dfff,86);
            addTile(back,8);back.setOnClickListener(v->move(MINI_MENU));
            return;
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
            @Override public void onMatch() { sound.match(); }
        });
        LinearLayout.LayoutParams boardParams=new LinearLayout.LayoutParams(-1,-2);
        boardParams.setMargins(dp(0),dp(2),dp(0),dp(4));
        root.addView(board,boardParams);
        nextButton=tile("🌟 Verder spelen!","",0xffc9f2dc,66);
        addTile(nextButton,4);
        nextButton.setVisibility(miniSolved?View.VISIBLE:View.GONE);
        nextButton.setOnClickListener(v->afterMiniSolve());
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
        if(miniDeck==null)startMiniDeck();
        else mini=miniDeck.next();
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
        PlaylumiRoom display=page==ROOM_ITEMS && roomPreview>=0 ?
                PlaylumiTryOn.previewRoom(room,roomSlot,roomPreview) : room;
        PlaylumiRoomView view=new PlaylumiRoomView(this,display,wardrobe);
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
    /** All five room decoration categories at once. */
    private void showRoomCats() {
        header("🏠 Mijn kamer aanpassen","Kies wat je wilt veranderen.");
        roomPreview(150);
        addText("⭐ "+wardrobe.stars()+" speelsterren",18,
                PlaylumiVisualStyle.SUCCESS,0,7);
        java.util.List<Button> options=new java.util.ArrayList<>();
        for(int slot=0;slot<PlaylumiRoom.SLOTS;slot++) {
            final int selected=slot;
            Button b=tile(PlaylumiRoom.SLOT_ICONS[slot]+" "+
                    PlaylumiRoom.LABELS[slot],
                    "Nu: "+PlaylumiRoom.NAMES[slot][room.selected(slot)],
                    COLORS[slot%COLORS.length],95);
            b.setOnClickListener(v->{
                roomSlot=selected;roomMessage="";roomPreview=-1;
                roomFilter=PlaylumiTryOn.ALL;move(ROOM_ITEMS);
            });
            options.add(b);
        }
        addOptionGrid(options,100);
        Button gardenButton=tile("🌱 Ook de tuin bekijken","",0xffd9f3dd,66);
        addTile(gardenButton,6);
        gardenButton.setOnClickListener(v->move(GARDEN_MENU));
    }

    /** See the room change BEFORE buying a new decoration. */
    private void showRoomItems() {
        final int slot=roomSlot;
        header("🏠 Kamer aanpassen","Bekijk eerst je nieuwe inrichting.");
        addCategoryStrip(false);
        roomPreview(195);
        addText("⭐ "+wardrobe.stars()+" speelsterren",18,
                PlaylumiVisualStyle.SUCCESS,0,5);
        if(roomPreview>=0 && !room.owned(slot,roomPreview)) {
            final int item=roomPreview;
            int price=room.cost(slot,item);
            addText(PlaylumiTryOn.previewLabel(false,
                    PlaylumiRoom.NAMES[slot][item]),17,TEXT,1,6);
            Button confirm=tile("⭐ Plaatsen voor "+price+" sterren",
                    "Pas na deze knop gebruik je sterren",0xffc8efd9,79);
            addTile(confirm,4);
            confirm.setOnClickListener(v->{
                int cost=room.cost(slot,item);
                if(!wardrobe.spendStars(cost)) {
                    roomMessage="💛 Nog "+
                            Math.max(0,cost-wardrobe.stars())+" sterren nodig.";
                } else {
                    room.unlockAndSelect(slot,item);
                    saveRoom();saveWardrobe();
                    roomPreview=-1;
                    sound.unlock();
                    roomMessage="🎉 Vrijgespeeld en geplaatst!";
                }
                redrawKeepingScroll();
            });
        } else {
            addText("👀 Tik op een item: bekijk eerst hoe je kamer eruitziet.",
                    14,SUB,0,5);
        }
        if(!roomMessage.isEmpty())addText(roomMessage,16,0xff4e4291,0,7);
        addItemFilter(false,slot);
        java.util.List<Button> options=new java.util.ArrayList<>();
        for(int item=0;item<PlaylumiRoom.OPTIONS;item++) {
            final int chosen=item;
            boolean owned=room.owned(slot,item);
            if(!PlaylumiTryOn.showItem(owned,roomFilter))continue;
            boolean equipped=room.selected(slot)==item;
            Button button=tile(PlaylumiRoom.ICONS[slot][item]+" "+
                    PlaylumiRoom.NAMES[slot][item],
                    equipped?"✅ Nu in je kamer":
                    roomPreview==item?"👀 Bekijk hierboven":
                    PlaylumiQuickNav.roomStatus(room,slot,item),
                    roomPreview==item?0xffffe8b9:
                    equipped?0xffd2f5db:
                    owned?0xffe4f4e9:COLORS[(slot+item)%6],96);
            button.setOnClickListener(v->{
                if(room.owned(slot,chosen)) {
                    room.select(slot,chosen); // free to use already-owned items
                    saveRoom();
                    roomPreview=-1;
                    roomMessage="✨ Je kamer is aangepast!";
                    sound.sparkle();
                } else {
                    roomPreview=chosen;
                    roomMessage="";
                    sound.tap();
                    showPreview();
                    return;
                }
                redrawKeepingScroll();
            });
            options.add(button);
        }
        addOptionGrid(options,105);
        Button back=tile("🏠 Andere kameronderdelen","",0xffe7dfff,65);
        addTile(back,5);back.setOnClickListener(v->move(ROOM_CATS));
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
                sound.story();move(STORY_REACTION);
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
        addText("🎼 Muziekplezier",21,TEXT,8,5);
        addText("Vrolijke deuntjes en spelgeluiden, zonder computerstem.",
                15,SUB,2,8);
        Button sample=tile("🎶 Speel een verrassingsdeuntje",
                "Tik opnieuw voor een ander origineel melodietje",0xffdcf3e6,81);
        addTile(sample,6);
        sample.setOnClickListener(v->sound.sample());
        Button effects=tile(sound.effectsEnabled()?
                "🎵 Deuntjes en geluidjes: aan":"🤫 Deuntjes en geluidjes: uit",
                "Vrolijke muziek bij tikken, spelen en beloningen",0xffe6f0ff,89);
        addTile(effects,7);
        effects.setOnClickListener(v->{
            sound.setEffectsEnabled(!sound.effectsEnabled());
            prefs().edit().putBoolean("effects_on",sound.effectsEnabled()).apply();
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


    private void startBubbleDeck() {
        bubbleDeckSeed=new Random().nextLong();
        bubbleDeck=SensoryChallengeDeck.bubbles(Math.max(0,Math.min(2,age)),bubbleDeckSeed);
        bubbles=bubbleDeck.next();
    }
    private void restoreBubbleDeck(Bundle state) {
        if(state==null || !state.containsKey("bubbleDeckSeed")) {
            startBubbleDeck();return;
        }
        bubbleDeckSeed=state.getLong("bubbleDeckSeed");
        bubbleDeck=SensoryChallengeDeck.bubbles(Math.max(0,Math.min(2,age)),bubbleDeckSeed);
        try{bubbleDeck.skip(state.getInt("bubbleDeckConsumed",0));}
        catch(IllegalArgumentException invalid){startBubbleDeck();}
    }
    private void startMelodyDeck() {
        melodyDeckSeed=new Random().nextLong();
        melodyDeck=SensoryChallengeDeck.melodies(Math.max(0,Math.min(2,age)),melodyDeckSeed);
        melody=melodyDeck.next();
    }
    private void restoreMelodyDeck(Bundle state) {
        if(state==null || !state.containsKey("melodyDeckSeed")) {
            startMelodyDeck();return;
        }
        melodyDeckSeed=state.getLong("melodyDeckSeed");
        melodyDeck=SensoryChallengeDeck.melodies(Math.max(0,Math.min(2,age)),melodyDeckSeed);
        try{melodyDeck.skip(state.getInt("melodyDeckConsumed",0));}
        catch(IllegalArgumentException invalid){startMelodyDeck();}
    }
    /** New sensory counting minigame, with one clear goal at a time. */
    private void showBubblePlay() {
        if(bubbles==null) {
            if(bubbleDeck==null)startBubbleDeck();
            else bubbles=bubbleDeck.next();
        }
        header("🫧 Het Bellenfeest","Ronde "+Math.min(bubbleRound+1,20)+" van 20");
        if(bubbleRound>=20) {
            characterHero(170);
            addText("🎉 Wat een bubbelkampioen!",25,0xff63458c,10,10);
            Button again=tile("🫧 Nog meer bellen!","",0xffd3f4df,99);
            addTile(again,10);
            again.setOnClickListener(v->{
                bubbleRound=0;bubbleSolved=false;
                startBubbleDeck();
                move(BUBBLE_PLAY);
            });
            Button back=tile("🎪 Een ander spel","",0xffe8deff,89);
            addTile(back,8);back.setOnClickListener(v->move(MINI_MENU));
            return;
        }
        // The small star in the navigation row replaces the oversized character panel.
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
        if(bubbleDeck==null)startBubbleDeck();
        else bubbles=bubbleDeck.next();
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
    /** Every new replay invalidates the previous delayed notes, without auto-play. */
    private void playMelodyPreview() {
        if(melody==null || melodySolved || sound==null || !sound.isEnabled())return;
        final FrameLayout active=stage;
        final PlaylumiMelody shownMelody=melody;
        final long playbackId=melodyPlayback.begin();
        sound.stopNotes();
        melodyStatus.setText("🎶 Luister goed ...");
        for(int i=0;i<shownMelody.length();i++) {
            final int note=shownMelody.noteAt(i);
            final int beat=i;
            active.postDelayed(()->{
                if(stage!=active || page!=MUSIC_PLAY || melody!=shownMelody ||
                        !melodyPlayback.isActive(playbackId) || !sound.isEnabled())return;
                sound.note(note);
                melodyStatus.setText("🎶 "+PlaylumiMelody.ANIMALS[note]+
                        "  ("+(beat+1)+" / "+shownMelody.length()+")");
            },i*470L);
        }
        active.postDelayed(()->{
            if(stage==active && page==MUSIC_PLAY && melody==shownMelody &&
                    melodyPlayback.isActive(playbackId)) {
                melodyPlayback.stop();
                melodyStatus.setText("🌟 Nu jij! Tik de dierenvriendjes aan.");
            }
        },shownMelody.length()*470L+220L);
    }
    private void showMusicPlay() {
        if(melody==null) {
            if(melodyDeck==null)startMelodyDeck();
            else melody=melodyDeck.next();
        }
        header("🎵 Het Muziekatelier","Melodie "+Math.min(melodyRound+1,PlaylumiMelody.ROUNDS)+
                " / "+PlaylumiMelody.ROUNDS);
        if(melodyRound>=PlaylumiMelody.ROUNDS) {
            characterHero(170);
            addText("🏆 Wat een muzikale ster!",26,0xff61418f,10,14);
            Button again=tile("🎶 Nog eens muziek maken","",0xffd4f3dd,96);
            addTile(again,9);
            again.setOnClickListener(v->{
                melodyRound=0;melodySolved=false;
                startMelodyDeck();
                move(MUSIC_PLAY);
            });
            Button other=tile("🎪 Ander spelletje","",0xffe9deff,80);
            addTile(other,7);other.setOnClickListener(v->move(MINI_MENU));
            return;
        }
        // Fresh order of the four familiar animals on each musical board.
        int[] instrumentOrder=melody.buttonOrder();
        StringBuilder instruments=new StringBuilder();
        for(int note:instrumentOrder) {
            if(instruments.length()>0)instruments.append("  ");
            instruments.append(PlaylumiMelody.ANIMALS[note]);
        }
        addText(instruments.toString(),31,0xff7652ac,8,7);
        melodyStatus=text(sound.isEnabled()?
                "👂 Tik op de luisterknop en speel het deuntje na.":
                "👀 Bekijk de plaatjes en tik ze na.",
                20,0xff65418f);
        melodyStatus.setPadding(dp(9),dp(14),dp(9),dp(14));
        melodyStatus.setBackground(rounded(0xfff1e5ff,23));
        addWidget(melodyStatus,5,11);
        Button replay=tile(sound.isEnabled()?"🔊 Speel de melodie af":"👀 Bekijk de melodie",
                "Start alleen als je op deze knop tikt",0xffd5f0ff,87);
        addTile(replay,8);
        replay.setOnClickListener(v->{
            if(sound.isEnabled())playMelodyPreview();
            else melodyStatus.setText(melodyIcons());
        });
        if(sound.isEnabled()) {
            Button stop=tile("⏹ Stop de melodie",
                    "Onderbreek het luisteren meteen",0xffe8e6f7,62);
            stop.setContentDescription("Stop de melodie die nu wordt afgespeeld");
            addTile(stop,4);
            stop.setOnClickListener(v->{
                melodyPlayback.stop();
                sound.stopNotes();
                melodyStatus.setText("🌟 Klaar om zelf te spelen of opnieuw te luisteren.");
            });
        }
        Button reveal=tile("💡 Laat de volgorde zien","",0xffffedc3,61);
        addTile(reveal,5);
        reveal.setOnClickListener(v->{
            melodyStatus.setText(melodyIcons());
            sound.sparkle();
        });
        for(int i=0;i<4;i+=2) {
            final int first=instrumentOrder[i],second=instrumentOrder[i+1];
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
        // A child's instrument tap interrupts any preview, never overlaps it.
        melodyPlayback.stop();
        sound.stopNotes();
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
        if(melodyDeck==null)startMelodyDeck();
        else melody=melodyDeck.next();
        move(MUSIC_PLAY);
    }


    /** Show two seeds at once rather than a crowded garden catalogue. */
    private void showGardenMenu() {
        header("🌱 Mijn Magische Tuin","Jouw speelrondes laten de planten groeien!");
        addText("🌸 "+garden.bloomedCount()+" van "+PlaylumiGarden.COUNT+
                " planten in bloei",20,PlaylumiVisualStyle.SUCCESS,6,8);
        addText("✨ Iedere afgeronde speelronde helpt een plant een stapje!",
                16,SUB,1,7);
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
        visibleGarden=view;
        view.setContentDescription("Magische plant "+PlaylumiGarden.NAMES[plant]+
                ". Tik om extra sterren te ontdekken.");
        view.setClickable(true);view.setFocusable(true);
        view.setOnClickListener(v->{
            PlaylumiDelight.wiggle(view);
            PlaylumiDelight.sprinkle(stage,view);
            if(sound!=null)sound.garden();
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
                int before=garden.stage(plant);
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
                if(result>=0 && visibleGarden!=null)
                    visibleGarden.animateGrowth(before,action);
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
