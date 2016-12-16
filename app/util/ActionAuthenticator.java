package util;

import models.AuthToken;
import models.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import static play.mvc.Http.Status.UNAUTHORIZED;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */

public class ActionAuthenticator extends Security.Authenticator {

    /**
     * Returns the email (unique) of the user despite it's name to identify the user.
     * @param ctx
     * @return
     */
    @Override
    public String getUsername(Http.Context ctx) {
        String token = getTokenFromHeader(ctx);
        System.out.println("Token=" + token);

        if (token != null) {
            AuthToken authToken = AuthToken.find.where().eq(JsonKeys.TOKEN, token).findUnique();
            if (authToken != null) {
                User user = authToken.getUser();
                return user.getEmail();
            }
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED,"Please provide a valid token via the header field 'Authorization':'Bearer {{token}}' to authenticate before sending requests."));
    }

    /**
     * Performs operations to get the tokenHeader from the context.
     * @param ctx
     * @return
     */
    private String getTokenFromHeader(Http.Context ctx) {
        //see rfc for oauth for info about the format: https://tools.ietf.org/html/rfc6750#section-2.1
        String[] authTokenHeaderValues = ctx.request().headers().get(RequestKeys.TOKEN_HEADER);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            String[] tokenHeader = authTokenHeaderValues[0].split(" ");
            if(tokenHeader.length==2){
                return tokenHeader[1];
            }

        }
        return null;
    }
}
