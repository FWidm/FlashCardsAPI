package controllers;

import models.statistics.CardStatistics;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import repositories.CardStatisticsRepository;
import util.ActionAuthenticator;
import util.JsonUtil;
import util.exceptions.NotAuthorizedException;

import java.text.ParseException;
import java.util.List;

/**
 * Created by fabianwidmann on 10/02/17.
 */
public class CardStatisticsController extends Controller {

    @Security.Authenticated(ActionAuthenticator.class)
    public Result getCardStatistics() {
        try {
            List<CardStatistics> messageList = CardStatisticsRepository.getCardStatistics(request().username());
            return ok(JsonUtil.toJson(messageList));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage()));
        } catch (ParseException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Error while parsing count number. Please provide a valid number."));
        }
    }


}