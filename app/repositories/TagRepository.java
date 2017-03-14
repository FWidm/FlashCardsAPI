package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import models.FlashCard;
import models.Tag;
import play.Logger;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;
import util.exceptions.InvalidInputException;
import util.exceptions.ParameterNotSupportedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
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
    public static List<Tag> getTags() throws ParameterNotSupportedException {
        String requestInformation = "";
        List<Tag> tagList;

        int buffer = -1;
        int start = 0;

        try {
            if (UrlParamHelper.checkForKey(RequestKeys.SIZE))
                buffer = Integer.parseInt(UrlParamHelper.getValue(RequestKeys.SIZE));

            if (UrlParamHelper.checkForKey(RequestKeys.START))
                start = Math.max(0,Integer.parseInt(UrlParamHelper.getValue(RequestKeys.START)));
        }catch (NumberFormatException e){
            throw new ParameterNotSupportedException("Invalid number for '?size=y' or '?start=x' request parameter. Please re-check your request");
        }
        Logger.debug("start="+start+", buffer="+buffer);

        if (UrlParamHelper.checkForKey(RequestKeys.STARTS_WITH)) {
            requestInformation = UrlParamHelper.getValue(RequestKeys.STARTS_WITH);
            Logger.debug("startswith=" + requestInformation);
            //match everything starting with the requestinformation -> searching for he* should return hell,help, ...
            tagList = Tag.find.where().like(JsonKeys.TAG_NAME, requestInformation + "%").findList();
        } else
            tagList = Tag.find.all();


        if (UrlParamHelper.checkForKey(RequestKeys.SORT_BY)) {
            requestInformation = UrlParamHelper.getValue(RequestKeys.SORT_BY);
            if (requestInformation.toUpperCase().contains(RequestKeys.USAGE_COUNT.toUpperCase())) {
                Logger.debug("Sortby=" + requestInformation);

                tagList.forEach(tag -> tag.updateUsageCount());
                Collections.sort(tagList);
                if (requestInformation.toUpperCase().contains(RequestKeys.DESC))
                    Collections.reverse(tagList);
            }
        }

        //.subList(Math.max(0, start), Math.min(tagList.size(), start + buffer)
        if (buffer == -1)
            buffer = tagList.size();
        return tagList.subList(Math.min(Math.max(0, start),tagList.size()), Math.min(tagList.size(), start + buffer));
    }

    /**
     * Return all attached cards of a specific tag by tag id.
     *
     * @param id of a tag
     * @return a list of FlashCards
     */
    public static List<FlashCard> getAttachedCards(long id) {
        List<FlashCard> attachedCardList = Tag.find.byId(id).getCards();
        return attachedCardList;
    }

    /**
     * Return one Tag object by id.
     *
     * @param id of the Tag
     * @return Tag object
     */
    public static Tag getTag(long id) {
        return Tag.find.byId(id);
    }

    public static List<FlashCard> getCardsByTagArray(List<Long> ids, List<String> names) {
        List<FlashCard> cardList=new ArrayList<>();
        List<Tag> tagList = retrieveTags(ids, names);

        Logger.debug("Tags found=" + tagList);
        for (int i = 0; i < tagList.size(); i++) {
            List<FlashCard> retrievedCardList = tagList.get(i).getCards();
            if(cardList.isEmpty()){
                cardList=retrievedCardList;
            }
            Logger.debug("i="+i+" | retrievedCards: ");
            FlashCard tmp = retrievedCardList.get(i);
            cardList.retainAll(retrievedCardList); // keeps only values that are both in cardList AND retrievedCardlist
            Logger.debug("retrievedList="+cardList);

        }
        return cardList;
    }

    /**
     * Parses a question from the given JsonNode node.
     *
     * @param node the json node to parse
     * @return a question object containing the information
     */
    public static Tag parseTag(JsonNode node) {
        String tagText = null;

        if (node.has(JsonKeys.TAG_NAME)) {
            tagText = node.get(JsonKeys.TAG_NAME).asText();
        }
        Tag tag = new Tag(tagText);

        return tag;
    }

    /**
     * Reads all tags in the given json - either via their id, or creates a new tag when it does not exist at the moment.
     *
     * @param json the root json object
     * @return a list of tags
     */
    public static List<Tag> retrieveOrCreateTags(JsonNode json) {
        List<Tag> tags = new ArrayList<>();
        //get the specific nods in the json
        JsonNode tagNode = json.findValue(JsonKeys.FLASHCARD_TAGS);
        // Loop through all objects in the values associated with the
        // "users" key.
        for (JsonNode node : tagNode) {
            // when a user id is found we will get the object and add them to the userList.
            Logger.debug("Node=" + node);
            if (node.has(JsonKeys.TAG_ID)) {
                Tag found = Tag.find.byId(node.get(JsonKeys.TAG_ID).asLong());
                if (found != null) {
                    System.out.println(">> tag: " + found);
                    if (!tags.contains(found))
                        tags.add(found);
                } else tags.add(null);

            } else {
                Logger.debug("got name: " + node.get(JsonKeys.TAG_NAME).asText());
                Tag lookupTag = Tag.find.where().eq(JsonKeys.TAG_NAME, node.get(JsonKeys.TAG_NAME).asText()).findUnique();
                Logger.debug("LookupTag=" + lookupTag);
                //check if the tag is unique
                if (lookupTag == null) {
                    Tag tmpT = TagRepository.parseTag(node);

                    tmpT.save();
                    System.out.println(">> found new tag: " + tmpT);
                    //save our new tag so that no foreign constraint fails
                    //((`flashcards`.`card_tag`, CONSTRAINT `fk_card_tag_tag_02` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tagId`))]]
                    tags.add(tmpT);
                } else /*Looked up Tag exists */ {
                    //check if tag name does not lead to the same tag being added twice. This would lead to a primary key constraint error.
                    boolean idExists = tags.stream()
                            .anyMatch(t -> t.getId() == lookupTag.getId());
                    //Logger.debug("TAGREPO ID="+tmpT.getId()+" EXISTS? "+idExists);
                    if (!idExists)
                        tags.add(lookupTag);
                }

            }
        }
        return tags;
    }

    /**
     * Retrieves a list of tags by iterating over the provided ids and names and retrieving the single expected Tags
     *
     * @param ids   of the tags
     * @param names of the tags
     * @return a list that contains all tags found with the given ids and names.
     */
    public static List<Tag> retrieveTags(List<Long> ids, List<String> names) {
        List<Tag> tags = new ArrayList<>();
        Tag tmpTag;
        for (Long id : ids) {
            tmpTag = Tag.find.byId(id);
            if (tmpTag != null)
                tags.add(tmpTag);
        }
        for (String tagName : names) {
            tmpTag = Tag.find.where().eq(JsonKeys.TAG_NAME, tagName).findUnique();
            Logger.debug("retrieveTags: found tmpTag="+tmpTag);

            if (tmpTag != null)
                tags.add(tmpTag);
        }
        return tags;
    }
}
