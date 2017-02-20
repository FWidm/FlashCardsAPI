package controllers;

import models.statistics.CardStatistics;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import repositories.CardStatisticsRepository;
import util.ActionAuthenticator;
import util.JsonKeys;
import util.JsonUtil;
import util.exceptions.InvalidInputException;
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
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Error while parsing count number." +
                    " Please provide a valid number."));
        }
    }


    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addCardStatistics() {
        CardStatistics cardStatistics;
        try {
            cardStatistics = CardStatisticsRepository.createCardStatistics(request().username(),  request().body().asJson());
        } catch (ParseException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Error while parsing date. " +
                    "Dates should have the format: '"+ JsonKeys.DATE_FORMAT+"'. Example: '"+JsonKeys.DATE_START+"':'2017-02-10 13:24:29 UTC'. Applies to: '"+JsonKeys.DATE_START+"' and '"+JsonKeys.DATE_END+"'."));
        } catch (InvalidInputException e) {
            e.printStackTrace();
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,e.getMessage()));
        }
        return ok(JsonUtil.toJson(cardStatistics));
    }
}