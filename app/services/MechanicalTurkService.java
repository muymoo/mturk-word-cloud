/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package services;

import java.util.HashMap;
import java.util.Map;

import play.Logger;

import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * This class acts as an interface to Mechanical Turk
 */
public class MechanicalTurkService
{
    private RequesterService service;
    private double           reward = 0.05;

    /**
     * Initialize the Mechanical Turk service
     */
    public MechanicalTurkService()
    {
        service = new RequesterService(new PropertiesClientConfig("conf/mturk.properties"));
    }

    /**
     * @param url URL for the turk to describe
     * @param assignments Number of assignments to run
     * @return ID of new hit
     */
    public String createHit(String url, int assignments)
    {
        Logger.debug("Creating hit: " + url + " Assignments: " + assignments);

        // Workaround for the office proxy
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");

        String question = views.html.question.render(url).body().trim();

        Logger.debug("Creating question: " + question);

        HIT hit = service.createHIT("Describe a Web Page", question, reward, question, assignments);

        Logger.debug("View HIT here: " + service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());

        // TODO: I'm not sure if this is the right ID to return to find the hit again. It doesn't seem to work. There is a different ID on the actual HIT page
        // that is used to retrieve the assignments. Could be .getHITGroupId() ??
        return hit.getHITId();
    }

    /**
     * Ex.
     * {"lovely":15, "happy":2, "nice": 3, ...}
     * 
     * @param hitId
     * @return Frequency map of words and their counts
     */
    public Map<String, Integer> getWordsFromCompletedAssignments(String hitId)
    {
        Assignment[] assignments = service.getAllAssignmentsForHIT(hitId);
        Logger.debug("Found " + assignments.length + " assignments.");
        Map<String, Integer> wordCount = parseAssignmentsForWords(assignments);
        return wordCount;
    }

    /**
     * Parses a list of assignments an pulls out the descriptive word answers.
     * 
     * @param assignments
     * @return A map of words and their counts
     */
    private Map<String, Integer> parseAssignmentsForWords(Assignment[] assignments)
    {
        Map<String, Integer> wordCounts = new HashMap<String, Integer>();

        for (Assignment assigment : assignments)
        {
            if ( assigment.getAssignmentStatus() == AssignmentStatus.Submitted )
            {
                // TODO: This is an XML string
                String word = assigment.getAnswer();
                
                // TODO: This is some parsing code that should get you close. See the Reviewer sample for more info.
                // QuestionFormAnswers questionFormAnswers = RequesterService.parseAnswers(word);
                // questionFormAnswers.getAnswer();
                
                // Increment word count
                int count = wordCounts.containsKey(word) ? wordCounts.get(word) : 0;
                wordCounts.put(word, count);
            }
        }

        return wordCounts;
    }

}
