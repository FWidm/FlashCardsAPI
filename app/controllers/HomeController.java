package controllers;

import models.*;
import play.mvc.*;

import views.html.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
        return ok(index.render("Your new application is ready."));
    }

    public Result testCards() {
        User u=new User("hello","hello1@world.com","passwörd",1);
        u.save();

        List<String> tags=new ArrayList<>();
        tags.add("Tag1");
        tags.add("Tag2");

        FlashCard fc=new FlashCard(u,false,tags);
        fc.save();
        System.out.println("Flashcard created: "+fc);
        Question q = new Question("Question",u);
        q.save();
        fc.setQuestion(q);

        Answer a=new Answer("Answer","No hint available - 404",u);
        a.save();
        fc.addAnswer(a);

        System.out.println("Flashcard added Question and Answer: "+fc);
        System.out.println("_____________________");
        System.out.println("Question for Card no. "+fc.getId()+": "+fc.getQuestion());
        System.out.println("IsMultiChoice? "+fc.isMultipleChoice());
        fc.save();
        System.out.println(FlashCard.find.all());
        return ok(index.render("Card test done!"));
    }

    public Result testGroups(){
        		List<User> l=new ArrayList<User>();
		l.add(new User("a","a","a",-1));
		l.add(new User("b","b","b",-2));

		UserGroup g=new UserGroup("y","y",l);
		g.save();
		for(User u:l){
			u.setGroup(g);
			u.save();
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
}
