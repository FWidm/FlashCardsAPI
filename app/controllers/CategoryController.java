package controllers;

import models.Category;
import play.mvc.Result;
import util.JsonUtil;

import java.util.List;

import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

/**
 * @author Jonas Kraus jonas.kraus@uni-ulm.de
 */
public class CategoryController {

    /**
     * Retrieves all Categories.
     *
     * @return HTTPResult
     */
    public Result getCategoriesList() {
        List<Category> categories = Category.find.all();
        return ok(JsonUtil.getJson(categories));
    }

    public Result getCategoryChildren(long parentId) {
        try {
            return ok(JsonUtil.getJson(Category.find.byId(parentId)));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "The category dosn't exist.", parentId));
        }
    }
}
