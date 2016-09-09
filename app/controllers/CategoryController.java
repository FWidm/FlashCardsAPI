package controllers;

import models.Category;
import play.mvc.Result;
import util.JsonUtil;

import java.util.List;

import static play.mvc.Results.ok;

/**
 * Created by Jonas Kraus jonas.kraus@uni-ulm.de on 09.09.2016.
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
}
