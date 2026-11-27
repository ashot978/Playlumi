package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/** Explicit category membership, with exactly one outsider and no unstated grouping rule. */
public final class PlaylumiOddOneOut {
    public static final int MODE=ExtraGames.CATEGORIES,VARIANT=5;
    private static final int ANIMALS=1,FRUIT=2,CLOTHES=4,VEHICLES=8,INSTRUMENTS=16,BIRDS=32,MAMMALS=64;
    private static final int INK=0xff3d195f;

    private static final class Group {
        final int membership;
        final String prompt,display,excluded;
        Group(int membership,String prompt,String display,String excluded){
            this.membership=membership;this.prompt=prompt;this.display=display;this.excluded=excluded;
        }
    }
    private static final Group[] GROUPS={
        new Group(ANIMALS,"Welk plaatje is geen dier?","🐾\nDieren","dier"),
        new Group(FRUIT,"Welk plaatje is geen fruit?","🍎\nFruit","fruit"),
        new Group(CLOTHES,"Welk plaatje toont geen kleding?","👕\nKleding","kleding"),
        new Group(VEHICLES,"Welk plaatje is geen voertuig?","🚗\nVoertuigen","voertuig"),
        new Group(INSTRUMENTS,"Welk plaatje is geen muziekinstrument?","🎵\nMuziekinstrumenten","muziekinstrument"),
        new Group(BIRDS,"Welk plaatje is geen vogel?","🐦\nVogels","vogel")
    };

    private static final class Item {
        final String symbol,subject,kind;
        final int memberships,minimum;
        Item(String symbol,String subject,String kind,int memberships,int minimum){
            this.symbol=symbol;this.subject=subject;this.kind=kind;
            this.memberships=memberships;this.minimum=minimum;
        }
    }
    // Membership is not inferred from another group: every bird is explicitly also an animal.
    // No fruit/vegetable edge cases, toy/real-vehicle ambiguity, habitat assumptions or value judgments.
    private static final Item[] ITEMS={
        new Item("🐱","Een kat is","een dier",ANIMALS|MAMMALS,0),
        new Item("🐶","Een hond is","een dier",ANIMALS|MAMMALS,0),
        new Item("🐮","Een koe is","een dier",ANIMALS|MAMMALS,0),
        new Item("🐰","Een konijn is","een dier",ANIMALS|MAMMALS,0),
        new Item("🐷","Een varken is","een dier",ANIMALS|MAMMALS,0),
        new Item("🐭","Een muis is","een dier",ANIMALS|MAMMALS,0),
        new Item("🍎","Een appel is","fruit",FRUIT,0),
        new Item("🍌","Een banaan is","fruit",FRUIT,0),
        new Item("🍇","Druiven zijn","fruit",FRUIT,0),
        new Item("🍓","Een aardbei is","fruit",FRUIT,0),
        new Item("🍊","Een sinaasappel is","fruit",FRUIT,0),
        new Item("🍐","Een peer is","fruit",FRUIT,0),
        new Item("👕","Een T-shirt is","kleding",CLOTHES,0),
        new Item("👖","Een broek is","kleding",CLOTHES,0),
        new Item("👗","Een jurk is","kleding",CLOTHES,0),
        new Item("👚","Een blouse is","kleding",CLOTHES,0),
        new Item("🚗","Een auto is","een voertuig",VEHICLES,0),
        new Item("🚌","Een bus is","een voertuig",VEHICLES,0),
        new Item("🚲","Een fiets is","een voertuig",VEHICLES,0),
        new Item("🚂","Een trein is","een voertuig",VEHICLES,0),
        new Item("🚢","Een schip is","een voertuig",VEHICLES,0),
        new Item("✈️","Een vliegtuig is","een voertuig",VEHICLES,0),
        new Item("🎸","Een gitaar is","een muziekinstrument",INSTRUMENTS,1),
        new Item("🎺","Een trompet is","een muziekinstrument",INSTRUMENTS,1),
        new Item("🎷","Een saxofoon is","een muziekinstrument",INSTRUMENTS,1),
        new Item("🎻","Een viool is","een muziekinstrument",INSTRUMENTS,1),
        new Item("🎹","Een keyboard is","een muziekinstrument",INSTRUMENTS,1),
        new Item("🐔","Een kip is","een vogel",ANIMALS|BIRDS,0),
        new Item("🐓","Een haan is","een vogel",ANIMALS|BIRDS,0),
        new Item("🐧","Een pinguïn is","een vogel",ANIMALS|BIRDS,0),
        new Item("🐦","Een vogel is","een dier",ANIMALS|BIRDS,0),
        new Item("🐤","Een kuiken is","een vogel",ANIMALS|BIRDS,0)
    };

    private PlaylumiOddOneOut(){}

    public static LearningGame.Round next(int level,Random random){
        if(level<0||level>2||random==null)throw new IllegalArgumentException("odd-one-out configuration");
        Group group=GROUPS[random.nextInt(level==0?4:level==1?5:6)];
        ArrayList<Item> members=new ArrayList<>(),outsiders=new ArrayList<>();
        for(Item item:ITEMS)if(item.minimum<=level){
            if((item.memberships&group.membership)!=0)members.add(item);else outsiders.add(item);
        }
        Collections.shuffle(members,random);
        Item answer=outsiders.get(random.nextInt(outsiders.size()));
        int count=level==0?3:4;
        ArrayList<String> choices=new ArrayList<>();choices.add(answer.symbol);
        for(int i=0;i<count-1;i++)choices.add(members.get(i).symbol);
        Collections.shuffle(choices,random);
        ArrayList<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(INK);
        return new LearningGame.Round(group.prompt,group.display,INK,choices,colors,choices.indexOf(answer.symbol));
    }

    /** Explain the exact saved category and object only after the question has been solved. */
    public static String explain(LearningGame.Round round){
        if(round==null||!round.isSolved())return "";
        Group category=null;
        for(Group group:GROUPS)if(group.prompt.equals(round.prompt)&&group.display.equals(round.display))category=group;
        if(category==null)return "";
        String answer=round.answers.get(round.correctIndex);
        for(Item item:ITEMS)if(item.symbol.equals(answer)){
            if((item.memberships&category.membership)!=0)return "";
            // Avoid implying that birds are not animals when teaching the narrower bird group.
            String kind=category.membership==BIRDS&&(item.memberships&MAMMALS)!=0?"een zoogdier":item.kind;
            return item.subject+" "+kind+", geen "+category.excluded+".";
        }
        return "";
    }
}
