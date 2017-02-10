package controllers;

import models.msg.AbstractMessage;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import repositories.MessagingRepository;
import util.ActionAuthenticator;
import util.JsonUtil;
import util.exceptions.InvalidInputException;

import java.util.List;

/**
 * Created by fabianwidmann on 10/02/17.
 */
public class MessagingController extends Controller {

    @Security.Authenticated(ActionAuthenticator.class)
    public Result getMessages() {
        // TODO: 10/02/17 fill 
        return noContent();
    }

    /**
     * Creates a message object via the content - the underlying object can be any descendant of AbstractMessage.
     *
     * @return
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result createMessage() {
        try {
            AbstractMessage msg = MessagingRepository.createMessage(request().username(), request().body().asJson());
            msg.save();
            return ok(JsonUtil.prepareJsonStatus(OK, "Message has been created.", msg.getId()));
        } catch (InvalidInputException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        }
    }
}