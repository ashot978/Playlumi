package nl.playlumi.app;

/** Twelve original Dutch choose-your-path stories; old progress remains compatible. */
public final class PlaylumiStories {
    private static final String[] BASE_NAMES={
            "De verdwaalde vlinder","De regenboogbrug","De sterrenraket",
            "Het kleine zaadje","De vrienden in zee","De lieve draak"
    };
    private static final String[] BASE_ICONS={"🦋","🌈","🚀","🌱","🐠","🐉"};
    private static final String[] BASE_ENDINGS_A={
            "De vlinder vliegt vrolijk naar de bloemen. Jullie zijn goede vrienden!",
            "De brug schittert vol kleuren. De wolkjes maken een vrolijk dansje!",
            "De sterrenraket landt veilig op een prachtige nieuwe planeet!",
            "Uit het zaadje groeit een grote bloem. De tuin staat vol kleur!",
            "Alle zeediertjes vieren feest. Iedereen voelt zich welkom!",
            "De draak laat trots zijn zachte vuurwerk van sterren zien!"
    };
    private static final String[] BASE_ENDINGS_B={
            "De vlinder vindt de weg naar huis. Hij zwaait je nog één keer toe!",
            "Er verschijnt een geheime regenboogtuin achter de brug!",
            "De raket ontdekt een maan vol grappige, dansende kraters!",
            "Het zaadje groeit uit tot een kleine boom met een gezellig huisje!",
            "Samen ontdekken jullie een verborgen schatkist op de zeebodem!",
            "De draak nodigt alle bosdieren uit voor een warm lichtjesfeest!"
    };
    public static final class Scene {
        public final String emoji,prompt,first,second,firstResult,secondResult;
        Scene(String emoji,String prompt,String first,String second,
              String firstResult,String secondResult) {
            this.emoji=emoji;this.prompt=prompt;this.first=first;this.second=second;
            this.firstResult=firstResult;this.secondResult=secondResult;
        }
    }
    private static final Scene[][] BASE_STORIES={
        {
            new Scene("🦋","Een kleine vlinder is verdwaald. Wat doen we?",
                    "Zoek samen bloemen","Vraag de bij om hulp",
                    "De vlinder ziet een pad vol mooie bloemen.",
                    "De bij zoemt: kom maar achter mij aan!"),
            new Scene("🌼","Het pad splitst in tweeën. Waar gaan jullie heen?",
                    "Naar de gele bloemen","Naar de blauwe vijver",
                    "Er vliegt een vrolijk lieveheersbeestje voorbij.",
                    "In het water zwemt een vriendelijke vis."),
            new Scene("🌳","Er staat een grote boom. Waar zoeken we?",
                    "Onder de takken","Tussen de blaadjes",
                    "Een klein vogeltje wijst de goede richting.",
                    "Je vindt een blaadje in de vorm van een hart."),
            new Scene("🏡","Daar is het huisje van de vlinder! Wat zeg je?",
                    "Fijn dat je thuis bent!","Kom, we vieren feest!",
                    "De vlinder vliegt blij rondjes om je heen.",
                    "Alle bloemen beginnen zachtjes te dansen.")
        },
        {
            new Scene("🌈","Een regenboogbrug mist een paar kleuren!",
                    "Pak een rode verfkwast","Pak een blauwe verfkwast",
                    "Een rode streep fonkelt op de brug.",
                    "Een blauwe streep glinstert als de zee."),
            new Scene("☁️","Een wolkje wil meehelpen. Wat mag het doen?",
                    "De brug schoonpoetsen","Kleine regendruppels maken",
                    "Nu glimt de brug als een spiegel.",
                    "De druppeltjes maken kleine kleurrijke stippen."),
            new Scene("🎨","Je hebt nog een kleur over. Welke kies je?",
                    "Geel als de zon","Groen als gras",
                    "De brug straalt als de ochtendzon.",
                    "De brug voelt als een zachte lenteweide."),
            new Scene("✨","De regenboogbrug is klaar. Wat wil je doen?",
                    "Er samen overheen lopen","Een dansje op de brug maken",
                    "Aan de overkant wacht een vrolijke verrassing.",
                    "De brug begint mee te bewegen op de muziek.")
        },
        {
            new Scene("🚀","Je sterrenraket staat klaar. Wat neem je mee?",
                    "Een knuffel","Een sterrenkaart",
                    "De knuffel krijgt een eigen plekje in de raket.",
                    "Met de kaart vind je gemakkelijk de sterren."),
            new Scene("🌟","Je vliegt langs een fonkelende ster!",
                    "Zwaai naar de ster","Maak een foto",
                    "De ster zwaait met lichtstraaltjes terug.",
                    "Je maakt de mooiste foto van de ruimte."),
            new Scene("🪐","Er komen twee planeten in zicht. Waarheen?",
                    "De roze planeet","De groene planeet",
                    "De roze planeet zit vol zachte wolken.",
                    "Op de groene planeet groeien sterrenbloemen."),
            new Scene("🌙","De raket gaat landen. Wat doe je eerst?",
                    "Kijk uit het raampje","Tel rustig terug",
                    "Je ziet een prachtig licht aan de horizon.",
                    "Drie, twee, één! De raket landt heel zacht.")
        },
        {
            new Scene("🌱","Je vindt een piepklein zaadje in de tuin.",
                    "Plant het in de aarde","Zoek een mooie pot",
                    "Het zaadje ligt lekker warm in de aarde.",
                    "Het zaadje krijgt een vrolijke bloempot."),
            new Scene("💧","Het zaadje heeft dorst! Wat geef je?",
                    "Een beetje water","Een zachte regenbui",
                    "Het zaadje drinkt langzaam het water op.",
                    "De regen maakt fijne muziek op de grond."),
            new Scene("☀️","De plant groeit. Waar zet je hem neer?",
                    "Bij het zonnetje","In een lichte serre",
                    "Het plantje strekt zijn blaadjes naar de zon.",
                    "Het plantje ziet allerlei mooie bloemen."),
            new Scene("🌷","Er verschijnt een eerste bloem! Wat nu?",
                    "Teken de bloem","Laat de bij komen kijken",
                    "Je maakt een prachtige tekening.",
                    "De bij is blij met de nieuwe bloem.")
        },
        {
            new Scene("🐠","Een visje zoekt een vriend in de zee.",
                    "Zwem naar de schildpad","Zoek een zeester",
                    "De schildpad wil graag met je zwemmen.",
                    "De zeester toont een grappig dansje."),
            new Scene("🐚","Je vindt een schelp. Wat doe je ermee?",
                    "Luister naar de zee","Versier hem met zand",
                    "Je hoort een zacht geluid van golven.",
                    "De schelp schittert als een parel."),
            new Scene("🦀","Er ligt een krab op het strand. Wat nu?",
                    "Help hem naar het water","Maak ruimte voor hem",
                    "De krab loopt blij naar de zee.",
                    "De krab zwaait met zijn scharen."),
            new Scene("🐬","Een dolfijn komt langs. Wat willen jullie?",
                    "Samen spelen","Samen een schat zoeken",
                    "De dolfijn springt vrolijk uit het water.",
                    "Een lichtje wijst naar een geheim plekje.")
        },
        {
            new Scene("🐉","Een kleine draak is zijn glimlach kwijt.",
                    "Vertel een grappig verhaal","Geef hem een knuffel",
                    "De draak moet een klein beetje lachen.",
                    "De draak voelt zich meteen veel fijner."),
            new Scene("🌲","In het bos hoort de draak muziek.",
                    "Dans tussen de bomen","Volg de muziek",
                    "De bomen wiegen vrolijk met jullie mee.",
                    "Een groepje konijntjes speelt op kleine trommels."),
            new Scene("🔥","De draak kan lichtjes maken. Welke kleur?",
                    "Gouden lichtjes","Paarse lichtjes",
                    "Het hele bos schittert als de zon.",
                    "De sterren lijken op kleine bloemen."),
            new Scene("🎉","De dieren willen iets vieren. Wat kies je?",
                    "Een sterrenparade","Een gezellig picknickfeest",
                    "Iedereen loopt in een prachtige optocht.",
                    "De dieren eten samen onder de sterren.")
        }
    };

    private static String[] concat(String[] a,String[] b) {
        String[] combined=new String[a.length+b.length];
        System.arraycopy(a,0,combined,0,a.length);
        System.arraycopy(b,0,combined,a.length,b.length);
        return combined;
    }
    private static Scene[][] combineStories(Scene[][] a,Scene[][] b) {
        Scene[][] combined=new Scene[a.length+b.length][];
        System.arraycopy(a,0,combined,0,a.length);
        System.arraycopy(b,0,combined,a.length,b.length);
        return combined;
    }
    public static final String[] NAMES=concat(BASE_NAMES,PlaylumiMoreStories.NAMES);
    public static final String[] ICONS=concat(BASE_ICONS,PlaylumiMoreStories.ICONS);
    public static final String[] ENDINGS_A=concat(BASE_ENDINGS_A,PlaylumiMoreStories.ENDINGS_A);
    public static final String[] ENDINGS_B=concat(BASE_ENDINGS_B,PlaylumiMoreStories.ENDINGS_B);
    private static final Scene[][] STORIES=combineStories(BASE_STORIES,PlaylumiMoreStories.EXTRA);
    private int story=-1,step,choice=-1,secondaryChoices,completedMask;

    public void start(int index) {
        validate(index);
        story=index;step=0;choice=-1;secondaryChoices=0;
    }
    public void restore(int mask,int story,int step,int alternative,int selected) {
        completedMask=mask&((1<<STORIES.length)-1);
        if(story>=0 && story<STORIES.length && step>=0 && step<=4) {
            this.story=story;this.step=step;
            this.secondaryChoices=Math.max(0,Math.min(4,alternative));
            this.choice=(selected==0 || selected==1)?selected:-1;
        }
    }
    private void validate(int index) {
        if(index<0 || index>=STORIES.length)throw new IllegalArgumentException("story");
    }
    public int currentStory(){return story;}
    public int step(){return step;}
    public int choice(){return choice;}
    public int secondaryChoices(){return secondaryChoices;}
    public int completionMask(){return completedMask;}
    public int completedCount(){return Integer.bitCount(completedMask);}
    public boolean completed(int index) {validate(index);return (completedMask&(1<<index))!=0;}
    public Scene scene() {
        validate(story);
        if(step>=4)throw new IllegalStateException("Finished story");
        return STORIES[story][step];
    }
    public String choose(int index) {
        if(index<0 || index>1)throw new IllegalArgumentException("choice");
        if(choice>=0)throw new IllegalStateException("Already chosen");
        Scene active=scene();
        choice=index;
        if(index==1)secondaryChoices++;
        return index==0?active.firstResult:active.secondResult;
    }
    public void next() {
        if(choice<0)throw new IllegalStateException("Choose first");
        step++;
        choice=-1;
    }
    public boolean finished() {return story>=0 && step==4;}
    public String ending() {
        if(!finished())throw new IllegalStateException("Story not finished");
        return secondaryChoices>=2?ENDINGS_B[story]:ENDINGS_A[story];
    }
    /** Returns true only for the first completion of this story. */
    public boolean awardOnce() {
        if(!finished())throw new IllegalStateException("Story not finished");
        if(completed(story))return false;
        completedMask|=1<<story;
        return true;
    }
}
