package controllers;

import static play.libs.Json.toJson;

import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import services.MechanicalTurkService;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Main entrance to the application. See conf/routes for mappings between the api and controllers.
 * 
 * @author 204054399
 */
public class Application extends Controller
{

    // So we can communicate with Mechanical Turk
    private static MechanicalTurkService turk = new MechanicalTurkService();

    /**
     * Create a hit with the given form data.
     * Expects the form to contain number_of_assignments and a url to judge.
     * 
     * @return OK if the HIT was created successfully. BAD_REQUEST otherwise.
     */
    public static Result createHit()
    {
        // Get the data from the form
        JsonNode hitRequest = request().body().asJson();
        String url = hitRequest.get("url").textValue();
        int assignments = hitRequest.get("assignments").asInt();

        Logger.debug("Got request for URL: " + url + " Assignments: " + assignments);

        String hitId = turk.createHit(url, assignments);
        return ok(toJson(hitId));
    }

    /**
     * Get all words from completed hits. Useful when creating a word cloud. The id of the HIT should be in the query parameters of the request.
     * 
     * @return Words for a hitId
     */
    public static Result getWords(String hitId)
    {
        Logger.debug("Got request for words: " + hitId);

        Map<String, Integer> wordCounts = turk.getWordsFromCompletedAssignments(hitId);

        return ok(toJson(wordCounts));
    }
}
