package util;

import models.AuthToken;
import models.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */

public class ActionAuthenticator extends Security.Authenticator {

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
        return super.onUnauthorized(context);
    }

    private String getTokenFromHeader(Http.Context ctx) {
        //see rfc for oauth for info about the format: https://tools.ietf.org/html/rfc6750#section-2.1
        String[] authTokenHeaderValues = ctx.request().headers().get(UrlParamKeys.TOKEN_HEADER);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            String[] tokenHeader = authTokenHeaderValues[0].split(" ");
            if(tokenHeader.length==2){
                return tokenHeader[1];
            }

        }
        return null;
    }
}
