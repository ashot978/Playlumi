package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Concrete addition, subtraction, missing addend, doubling, and word problems. */
public final class PlaylumiMathPractice {
    private PlaylumiMathPractice(){}
    public static int limit(int age){return age==0?5:age==1?10:20;}
    public static LearningGame.Round next(int variant,int age,Random r){
        if(variant<0||variant>4||age<0||age>2)throw new IllegalArgumentException("Math configuration");
        int max=limit(age);
        int a,b,result,answer;
        String prompt,display;
        boolean subtract=variant==1 || (variant==4&&r.nextBoolean());
        if(variant==3){
            a=1+r.nextInt(max/2);b=a;result=a+b;answer=result;
            prompt="Wat is het dubbele?";display=a+" + "+b+" = ?";
        }else if(subtract){
            a=(age==2?5:2)+r.nextInt(max-(age==2?5:2)+1);
            b=1+r.nextInt(a);
            result=a-b;answer=result;
            prompt=variant==4?"Hoeveel sterren houdt Milo over?":"Reken uit";
            display=variant==4?"Milo heeft "+a+" sterren.\nHij geeft er "+b+" weg.":a+" − "+b+" = ?";
        }else{
            a=1+r.nextInt(max-1);b=1+r.nextInt(max-a);
            result=a+b;
            answer=variant==2?b:result;
            prompt=variant==2?"Welk getal ontbreekt?":variant==4?"Hoeveel sterren heeft Fenna nu?":"Reken uit";
            display=variant==2?a+" + ? = "+result:variant==4?
                    "Fenna heeft "+a+" sterren.\nZe vindt er "+b+" bij.":a+" + "+b+" = ?";
        }
        int count=age==0?3:4;
        ArrayList<Integer> candidates=new ArrayList<>();
        for(int i=0;i<=max;i++)if(i!=answer)candidates.add(i);
        Collections.shuffle(candidates,r);
        // Prefer plausible errors nearby, then randomly position the correct answer.
        Collections.sort(candidates,(x,y)->Integer.compare(Math.abs(x-answer),Math.abs(y-answer)));
        ArrayList<String> choices=new ArrayList<>();
        choices.add(Integer.toString(answer));
        for(int i=0;i<count-1;i++)choices.add(Integer.toString(candidates.get(i)));
        Collections.shuffle(choices,r);
        List<Integer> colors=new ArrayList<>();
        for(int i=0;i<choices.size();i++)colors.add(0xff3d195f);
        return new LearningGame.Round(prompt,display,0xff3d195f,choices,colors,
                choices.indexOf(Integer.toString(answer)));
    }
}
