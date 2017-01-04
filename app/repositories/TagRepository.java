package repositories;

import models.FlashCard;
import models.Tag;
import play.Logger;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;

import java.util.*;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class TagRepository {
    /**
     * Retrieves all Tags.
     * Supports sorting by usage_count asc,desc -> ?sortBy=usageCount%20asc
     * Supports filtering by specifying a starting pattern that should be matched via ?startsWith=h -> h* is matched
     *
     * @return HTTPResult
     */
    public static List<Tag> getTags() {
        String requestInformation = "";
        List<Tag> tagList;

        if (UrlParamHelper.checkForKey(RequestKeys.SORT_BY)) {
            tagList = Tag.find.all();

            requestInformation = UrlParamHelper.getValue(RequestKeys.SORT_BY);
            if (requestInformation.toUpperCase().contains(RequestKeys.USAGE_COUNT.toUpperCase())) {
                Logger.debug("Sortby=" + requestInformation);

                tagList.forEach(tag -> tag.updateUsageCount());
                Collections.sort(tagList);
                if (requestInformation.toUpperCase().contains(RequestKeys.DESC))
                    Collections.reverse(tagList);
            }

            //Tag.find.where().eq(tag.key,tag.val).findRowCount();
            return tagList;
        }
        if (UrlParamHelper.checkForKey(RequestKeys.STARTS_WITH)) {
            requestInformation = UrlParamHelper.getValue(RequestKeys.STARTS_WITH);
            Logger.debug("startswith=" + requestInformation);
            //match everything starting with the requestinformation -> searching for he* should return hell,help, ...
            tagList = Tag.find.where().like(JsonKeys.TAG_NAME, requestInformation+"%").findList();
            return tagList;
        }
        return Tag.find.all();
    }

    /**
     * Return all attached cards of a specific tag by tag id.
     * @param id of a tag
     * @return a list of FlashCards
     */
    public static List<FlashCard> getAttachedCards(long id){
        List<FlashCard> attachedCardList=Tag.find.byId(id).getCards();
        return attachedCardList;
    }

    public static Tag getTag(long id){
        return Tag.find.byId(id);
    }
}
