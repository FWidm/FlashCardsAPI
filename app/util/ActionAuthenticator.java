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
/*        if (token != null) {
            User user = User.find.where().eq("authToken", token).findUnique();
            if (user != null) {
                return user.getName();
            }
        }*/
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
        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            return authTokenHeaderValues[0];
        }
        return null;
    }
}
