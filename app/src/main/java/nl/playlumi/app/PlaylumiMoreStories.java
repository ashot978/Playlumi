package nl.playlumi.app;

/** Six additional original Dutch adventures for the expanded Verhalenbos. */
public final class PlaylumiMoreStories {
    private PlaylumiMoreStories(){}
    public static final String[] NAMES={
            "Robot Rocco ontdekt kleuren",
            "Het sneeuwvlokkenfeest",
            "De geheime schatkaart",
            "De kleine sterrenkok",
            "Het vliegende boekenhuis",
            "De wolkenpost"
    };
    public static final String[] ICONS={"🤖","❄️","🗺️","👩‍🍳","📚","☁️"};
    public static final String[] ENDINGS_A={
            "Rocco bouwt een bontgekleurde speeltuin. Iedereen mag meedoen!",
            "De sneeuwvlokken dansen boven een prachtig wit winterdorp.",
            "Op de schatplek vinden jullie een kist vol vriendschapskaarten.",
            "Jullie openen een feestelijk restaurant vol heerlijke geuren.",
            "Het boekenhuis landt bij een bibliotheek vol nieuwe avonturen.",
            "De wolken brengen lieve berichtjes naar alle dieren in het bos."
    };
    public static final String[] ENDINGS_B={
            "Rocco maakt een regenboogrobot die iedereen vrolijk begroet!",
            "Er begint een warm lichtjesfeest midden in de wintertuin.",
            "De schat is een magische kaart met nog meer onbekende plekken.",
            "Alle knuffels komen bij jullie eten op het grote picknickkleed.",
            "Het boekenhuis ontdekt een eiland waar de letters kunnen dansen.",
            "De wolken tekenen samen een grote glimlach in de blauwe lucht."
    };
    public static final PlaylumiStories.Scene[][] EXTRA={
        {
            new PlaylumiStories.Scene("🤖","Robot Rocco kent de kleuren nog niet. Wat leer je hem?",
                    "De kleur rood","De kleur blauw",
                    "Rocco ontdekt een rode ballon en zegt: wauw!",
                    "Rocco ziet de blauwe lucht en wordt heel blij."),
            new PlaylumiStories.Scene("🟡","Rocco vindt een doos met vormen. Waar begint hij?",
                    "Met een ronde bal","Met een vierkant blok",
                    "De bal rolt zachtjes naar Rocco toe.",
                    "De blokken worden een toren van drie verdiepingen."),
            new PlaylumiStories.Scene("🎨","Rocco wil iets bouwen. Wat moet het worden?",
                    "Een vrolijke glijbaan","Een kleine speelauto",
                    "De glijbaan glanst in alle kleuren.",
                    "De speelauto krijgt fonkelende wielen."),
            new PlaylumiStories.Scene("✨","Rocco roept zijn vriendjes. Hoe vieren jullie het?",
                    "Samen buiten spelen","Een grote regenboog maken",
                    "Iedereen rent en lacht in de speeltuin.",
                    "De kleuren maken een vrolijke boog over het plein.")
        },
        {
            new PlaylumiStories.Scene("❄️","De eerste sneeuw valt op het dorp. Waar gaan jullie heen?",
                    "Naar het park","Naar de wintertuin",
                    "In het park liggen zachte witte sneeuwvlokken.",
                    "De wintertuin lijkt wel een glinsterend sprookje."),
            new PlaylumiStories.Scene("⛄","Een sneeuwpop mist nog iets. Wat geven we hem?",
                    "Een vrolijke sjaal","Een grappige hoed",
                    "Met de sjaal ziet de sneeuwpop er heerlijk warm uit.",
                    "De hoed staat een beetje scheef. Wat grappig!"),
            new PlaylumiStories.Scene("🐦","Een vogeltje zoekt eten in de sneeuw. Wat doe je?",
                    "Zoek wat zaadjes","Maak een rustig plekje",
                    "Het vogeltje pikt blij de zaadjes op.",
                    "Het vogeltje kan nu rustig uitrusten."),
            new PlaylumiStories.Scene("🎉","Het sneeuwfeest begint! Wat kiezen jullie?",
                    "Een dans met sneeuwvlokken","Een lichtjeswandeling",
                    "De sneeuwvlokken draaien vrolijk in de lucht.",
                    "De bomen schitteren van de kleine lichtjes.")
        },
        {
            new PlaylumiStories.Scene("🗺️","Een oude kaart ligt onder de deur. Waar wijst hij heen?",
                    "Naar de grote boom","Naar het blauwe meer",
                    "Onder de boom ligt een klein gouden sterretje.",
                    "Aan het meer staat een bootje klaar."),
            new PlaylumiStories.Scene("🧭","Er staan twee pijlen op de kaart. Welke volg je?",
                    "De pijl naar links","De pijl naar rechts",
                    "Het linkerpad loopt langs een veld bloemen.",
                    "Het rechterpad gaat over een vrolijk bruggetje."),
            new PlaylumiStories.Scene("🐿️","Een eekhoorn heeft een aanwijzing. Wat vraag je?",
                    "Waar ligt de schat?","Mag je ons helpen?",
                    "De eekhoorn wijst naar een grote steen.",
                    "De eekhoorn loopt gezellig met jullie mee."),
            new PlaylumiStories.Scene("🔑","Er ligt een klein sleuteltje. Wat openen jullie?",
                    "De oude schatkist","De geheimzinnige poort",
                    "In de kist zitten mooie kaarten om te delen.",
                    "Achter de poort begint een heel nieuw avontuur.")
        },
        {
            new PlaylumiStories.Scene("🥕","De sterrenkok gaat vandaag koken. Wat kiezen jullie?",
                    "Een groentesoep","Een fruitsalade",
                    "De wortels en tomaten ruiken heerlijk.",
                    "Alle stukjes fruit kleuren de kom als een regenboog."),
            new PlaylumiStories.Scene("🍅","De kok heeft hulp nodig. Wat doe je?",
                    "Groenten wassen","Fruit netjes sorteren",
                    "Het water maakt kleine spettertjes.",
                    "De gele, groene en rode stukjes liggen klaar."),
            new PlaylumiStories.Scene("🥣","Je mag de tafel versieren. Wat kies je?",
                    "Vrolijke servetten","Een vaasje met bloemen",
                    "De servetten krijgen allemaal een stervorm.",
                    "De bloemen staan prachtig in het midden."),
            new PlaylumiStories.Scene("🧑‍🍳","Het eten is klaar! Wie nodig je uit?",
                    "Alle dieren uit het bos","Alle knuffels van de kok",
                    "De dieren zitten gezellig aan een grote tafel.",
                    "De knuffels krijgen allemaal hun eigen stoeltje.")
        },
        {
            new PlaylumiStories.Scene("📚","Een huisje met vleugels vliegt langs. Stap je in?",
                    "Natuurlijk, ik ga mee!","Eerst voorzichtig binnenkijken",
                    "Je ziet boeken met allerlei kleurrijke plaatjes.",
                    "Binnen is het warm en ruikt het naar papier."),
            new PlaylumiStories.Scene("🔤","Een letter springt uit een boek! Welke letter zoek je?",
                    "De letter A","De letter B",
                    "De A maakt een grappig sprongetje.",
                    "De B beweegt als twee kleine rondjes."),
            new PlaylumiStories.Scene("🪁","Het boekenhuis vliegt naar een wolk. Wat zie je?",
                    "Een vlieger","Een dansende bladzijde",
                    "De vlieger maakt een boog om het huis.",
                    "De bladzijde draait langzaam rond in de lucht."),
            new PlaylumiStories.Scene("🏡","Het huis wil ergens landen. Welke plek kies je?",
                    "De gezellige bibliotheek","Een eiland vol letters",
                    "In de bibliotheek wachten nog veel verhalen.",
                    "De letters dansen vrolijk op het eiland.")
        },
        {
            new PlaylumiStories.Scene("☁️","Wolkje Wip wil lieve berichtjes rondbrengen. Aan wie?",
                    "Aan het konijntje","Aan de kleine vos",
                    "Het konijntje huppelt blij in het gras.",
                    "De vos glimlacht en zwaait naar de wolk."),
            new PlaylumiStories.Scene("💌","Wip zoekt mooi briefpapier. Wat kiest hij?",
                    "Een blaadje met hartjes","Een blaadje met sterren",
                    "Op elk hartje past een lieve groet.",
                    "De sterren fonkelen zacht op het papier."),
            new PlaylumiStories.Scene("🌦️","De wind wordt een beetje sterk. Wat helpt?",
                    "Rustig achter een boom wachten","Hulp vragen aan de zon",
                    "Achter de boom is het lekker windstil.",
                    "De zon schijnt warm door de wolken heen."),
            new PlaylumiStories.Scene("🌈","Alle berichtjes zijn bezorgd. Wat doen jullie?",
                    "Een fijne groepsknuffel","Een glimlach in de lucht tekenen",
                    "De dieren voelen zich allemaal welkom.",
                    "Boven het bos verschijnt een grote vrolijke glimlach.")
        }
    };
}
