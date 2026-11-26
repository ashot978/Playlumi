package nl.playlumi.app;

/** Short strategy hints, without giving away the correct option. */
public final class PlaylumiLearningHints {
    private PlaylumiLearningHints(){}
    public static String hint(int mode,int variant,String display,String prompt){
        switch(mode){
            case LearningGame.COUNT:return "Tel elk voorwerp één keer.";
            case LearningGame.COLORS:return "Kijk naar de kleur, niet naar de vorm.";
            case LearningGame.LETTERS:return "Vergelijk de letters en hun vorm.";
            case LearningGame.SHAPES:return "Tel de hoeken en bekijk de vorm.";
            case LearningGame.NUMBERS:return "Tel rustig of lees het getal.";
            case LearningGame.PATTERNS:return "Zoek het groepje dat zich herhaalt.";
            case LearningGame.WORDS:return "Lees het woord langzaam.";
            case ExtraGames.SPELLING:return "Kijk naar alle letters in het woord.";
            case ExtraGames.FIRST_LAST:return variant==1?"Luister naar het einde van het woord.":"Luister naar het begin van het woord.";
            case ExtraGames.PICTURES:return "Kijk naar het dier en lees het woord.";
            case ExtraGames.RHYMES:return "Zeg de woorden zachtjes; luister naar het einde.";
            case ExtraGames.COMPARE:return "Vergelijk de hoeveelheden één voor één.";
            case ExtraGames.MATH:return variant==1?"Begin bij het eerste getal en tel terug.":variant==2?"Tel verder tot het getal achter het = teken.":variant==4?"Krijgt iemand iets erbij of gaat er iets weg?":"Tel de getallen bij elkaar op.";
            case ExtraGames.SEQUENCES:return "Kijk naar de sprongen tussen de getallen.";
            case ExtraGames.OPPOSITES:return "Denk aan precies het tegenovergestelde.";
            case ExtraGames.CATEGORIES:return "Wat hebben de dingen met elkaar gemeen?";
            case 16:return variant==3?"Denk aan het dagdeel. Na twaalf uur tel je 12 bij het uur op, behalve bij twaalf uur zelf.":variant==1?"Tel één uur vooruit.":variant==2?"Tel één uur terug.":"Kijk eerst naar de korte wijzer, dan de lange.";
            case 17:return "Denk aan temperatuur, regen en wind.";
            case 18:return "Kijk naar de positie van de ster of pijl.";
            case 19:return "Denk aan wat je in het dagelijks leven doet.";
            case 20:return "Denk aan waar het eten vandaan komt.";
            case 21:return "Denk aan het dier en waar het leeft.";
            case 22:return display.contains("mm")?"1 cm is 10 mm. Vergelijk dezelfde eenheden.":variant==4?"Trek de kleinste lengte van de grootste af.":"Vergelijk beide waarden.";
            case 23:return variant==2?"Een even getal kun je verdelen in tweetallen.":"Zoek de regel tussen de getallen.";
            default:return "Kijk nog eens rustig naar de opdracht.";
        }
    }
}
