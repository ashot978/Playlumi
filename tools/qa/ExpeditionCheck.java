package nl.playlumi.app;
import java.util.*;

public final class ExpeditionCheck {
    private static void require(boolean valid,String message){if(!valid)throw new AssertionError(message);}
    public static void main(String[] args){
        int routes=0,transitions=0,rejected=0;
        for(int age=0;age<3;age++){
            Set<String> ids=new HashSet<>();Set<Integer> worlds=new HashSet<>();
            int puzzles=0;
            for(int chapter=0;chapter<6;chapter++){
                List<PlaylumiGameCatalog.Entry> entries=PlaylumiExpedition.chapter(age,chapter);
                require(entries.size()==5,"Five chapter missions");
                for(int i=0;i<5;i++){
                    int stage=chapter*5+i;PlaylumiGameCatalog.Entry entry=entries.get(i);
                    require(entry.id.equals(PlaylumiExpedition.routeId(age,stage)),"Route order changed");
                    require(PlaylumiGameCatalog.find(age,entry.id)!=null&&PlaylumiCompletion.eligible(entry),"Unavailable mission "+entry.id);
                    require(ids.add(entry.id),"Duplicate mission "+entry.id);
                    worlds.add(entry.world);
                    if(entry.kind==PlaylumiGameCatalog.INTERACTIVE&&PlaylumiPuzzle.supported(entry.mode))puzzles++;
                    routes++;
                }
            }
            require(worlds.size()==6&&puzzles>=6,"Expedition coverage");
            PlaylumiWardrobe wallet=new PlaylumiWardrobe();int progress=0;
            Random random=new Random(121+age);
            for(int mission=0;mission<30;mission++){
                String wanted=PlaylumiExpedition.routeId(age,mission);
                for(int i=0;i<50;i++){
                    String incorrect=PlaylumiExpedition.routeId(age,(mission+1+random.nextInt(29))%30);
                    int total=1+random.nextInt(20),partial=random.nextInt(total);
                    require(!PlaylumiExpedition.complete(age,progress,wanted,false,total,total).changed(),"Free play advanced expedition");
                    require(!PlaylumiExpedition.complete(age,progress,wanted,true,partial,total).changed(),"Partial round advanced expedition");
                    require(!PlaylumiExpedition.complete(age,progress,incorrect,true,total,total).changed(),"Wrong route advanced expedition");
                    require(!PlaylumiExpedition.complete(age,progress,wanted,true,total+1,total).changed(),"Over-completed round advanced expedition");
                    rejected+=4;
                }
                int total=1+random.nextInt(20);
                PlaylumiExpedition.Advance advance=PlaylumiExpedition.complete(age,progress,wanted,true,total,total);
                require(advance.before==progress&&advance.after==progress+1,"One mission per completion");
                int bonus=(mission+1)%5==0?15:0;if(mission==29)bonus+=30;
                require(advance.bonus==bonus,"Incorrect chapter reward");
                wallet.earnChallengeStars(advance.bonus);progress=advance.after;transitions++;
                require(!PlaylumiExpedition.complete(age,progress,wanted,true,total,total).changed(),"Duplicate mission paid twice");
                require(wallet.stars()==18+PlaylumiExpedition.starsEarned(progress),"Reward accounting");
                require(PlaylumiExpedition.stamps(progress)==progress/5,"Stamp count");
                for(int ch=0;ch<6;ch++){
                    require(PlaylumiExpedition.chapterProgress(progress,ch)==Math.max(0,Math.min(5,progress-ch*5)),"Chapter progress");
                    require(PlaylumiExpedition.available(progress,ch)==(ch<=Math.min(5,progress/5)),"Chapter lock");
                }
            }
            require(PlaylumiExpedition.finished(progress)&&wallet.stars()==138,"Final reward");
            require(PlaylumiExpedition.route(age,progress)==null,"Completed expedition exposes another mission");
            require(!PlaylumiExpedition.complete(age,progress,PlaylumiExpedition.routeId(age,0),true,1,1).changed(),"Finished expedition pays again");
        }
        require(!PlaylumiExpedition.key(0).equals(PlaylumiExpedition.key(1))&&!PlaylumiExpedition.key(1).equals(PlaylumiExpedition.key(2)),"Age-specific storage");
        System.out.println("PASS: "+routes+" pinned age-specific expedition stages, "+transitions+" first completions, "+rejected+" rejected/partial/free-play completions, all six chapter seals and exact once-only bonuses.");
    }
}
