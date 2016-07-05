package controllers;

import models.*;
import play.mvc.*;

import util.JsonWrap;
import views.html.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
//        return ok(JsonWrap.prepareJson(map));
        return ok(index.render("Your new application is ready."));
    }

    public Result testCards() {
        User tmp=User.find.where().eq("email","hello1@world.com").findUnique();
        if(tmp==null){
            tmp=new User("hello","hello1@world.com","passwörd",1);
            tmp.save();
        }



        List<String> tags=new ArrayList<>();
        tags.add("Tag1");
        tags.add("Tag2");

        FlashCard fc=new FlashCard(tmp,false,tags);
        fc.save();
        System.out.println("Flashcard created: "+fc);
        Question q = new Question("Question",tmp);
        q.save();

        fc.setQuestion(q);

        Answer a=new Answer("Answer","No hint available - 404",tmp);
        a.save();
        fc.addAnswer(a);

        for(int i=0; i<Math.random()*10+1; i++){
            Tag t =new Tag("Tag "+i+": "+ new Date());
            t.save();
            fc.addTag(t);
        }

        //we only ned to call update from one side it'll call he other side as well.
        fc.update();

        System.out.println("Flashcard added Question and Answer: "+fc);
        System.out.println("_____________________");
        System.out.println("Question for Card no. "+fc.getId()+": "+fc.getQuestion());
        System.out.println("IsMultiChoice? "+fc.isMultipleChoice());



        System.out.println("Card tags: "+fc.getTags());
        List<Tag> fc_tags = FlashCard.find.byId(fc.getId()).getTags();
        for (Tag tmptag:fc_tags){
            tmptag.removeFlashCard(fc);
            System.out.println("Tag="+tmptag);
        }

        return ok(index.render("Card test done!"));
    }

    public Result testGroups(){
        List<User> l=new ArrayList<User>();
		l.add(new User("aaa","a@b.com","abbbbb",-1));
		l.add(new User("bbb","b@a.com","baaaaa",-2));

        for(User u:l){
            u.save();
        }

		UserGroup g=new UserGroup("y","y",l);
		g.save();

        for(User u:l){
            u.setGroup(g);
            u.update();
        }
		System.out.println("Created group g="+g+" with users"+l);
        System.out.println("__________________________________");
        System.out.println("Querying the groups");
        List<UserGroup> groups = UserGroup.find.all();
        for(UserGroup group:groups){
            List<User> users= User.find.where().eq("group_id", group.getId()).findList();
            System.out.println("Finding users for group="+group+" users are: "+users);
        }
        UserGroup tmpGroup;
        return ok(index.render("Group test done!"));
    }


    public Result test(){
        return ok(JsonWrap.prepareJsonStatus(OK, "hello world"));
    }
}
