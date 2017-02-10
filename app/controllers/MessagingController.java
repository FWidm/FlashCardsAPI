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
import util.exceptions.NotAuthorizedException;

import java.util.List;

/**
 * Created by fabianwidmann on 10/02/17.
 */
public class MessagingController extends Controller {

    @Security.Authenticated(ActionAuthenticator.class)
    public Result getMessages() {
        try {
            List<AbstractMessage> messageList=MessagingRepository.getMessages(request().username());
            return ok(JsonUtil.toJson(messageList));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage()));
        }
    }

    @Security.Authenticated(ActionAuthenticator.class)
    public Result getMessage(Long id) {
        try{
            AbstractMessage message=MessagingRepository.getMessage(id,request().username());
            return ok(JsonUtil.toJson(message));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage()));
        }
    }

    /**
     * Creates a message object via the content - the underlying object can be any descendant of AbstractMessage.
     *
     * @return either OK or badRequest if parts of the message where not found in the body.
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result createMessage() {
        try {
            AbstractMessage msg = MessagingRepository.createMessage(request().username(), request().body().asJson());
            return ok(JsonUtil.prepareJsonStatus(OK, "Message has been created.", msg.getId()));
        } catch (InvalidInputException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        }
    }
}