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
import java.util.List;
import java.util.Map;

import play.Logger;

import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
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
    public Map<String, String> createHit(String url, int assignments, Double reward)
    {
        Logger.debug("Creating hit: " + url + " Assignments: " + assignments);

        // Workaround for the office proxy
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");

        String question = views.html.question.render(url).body().trim();

        Logger.debug("Creating question: " + question);

        HIT hit = service.createHIT("Describe an image in 3 words", "Describe an image in 3 words", reward, question, assignments);

        String hitURL = service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId();
        Logger.debug("View HIT here: " + hitURL);

        Map<String, String> result = new HashMap<String, String>();

        // TODO: I'm not sure if this is the right ID to return to find the hit again. It doesn't seem to work. There is a different ID on the actual HIT page
        // that is used to retrieve the assignments. Could be .getHITGroupId() ??
        result.put("hitId", hit.getHITId());
        result.put("hitURL", hitURL);

        return result;
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

        for (Assignment assignment : assignments)
        {
            if ( isSubmittedOrApproved(assignment) )
            {
                String answerXML = assignment.getAnswer();

                Logger.debug("Answer as XML: " + answerXML);

                QuestionFormAnswers questionFormAnswers = RequesterService.parseAnswers(answerXML);
                questionFormAnswers.getAnswer();

                @SuppressWarnings("unchecked")
                List<QuestionFormAnswersType.AnswerType> answers 
                	= (List<QuestionFormAnswersType.AnswerType>) questionFormAnswers.getAnswer();
                
                for (QuestionFormAnswersType.AnswerType answer : answers)
                {
                    String assignmentId = assignment.getAssignmentId();
                    String word = RequesterService.getAnswerValue(assignmentId, answer);

                    // Make word lowercase & trim off spaces
                    word = word.toLowerCase();
                    word = word.trim();
                    
                    // validate words
                    WordValidationService validator = new WordValidationService();
                    
                    if(wordCounts.containsKey(word)) {
                    	// Increment word count
	                    int count = wordCounts.containsKey(word) ? wordCounts.get(word) : 0;
	                    wordCounts.put(word, count + 1);
                    }
                    else {
                    	if(validator.isValid(word)) {
                    		// Increment word count
    	                    int count = wordCounts.containsKey(word) ? wordCounts.get(word) : 0;
    	                    wordCounts.put(word, count + 1);
                    	}
                    }
                }
            }
        }

        return wordCounts;
    }

    private boolean isSubmittedOrApproved(Assignment assignment)
    {
        return assignment.getAssignmentStatus() == AssignmentStatus.Submitted
        		|| assignment.getAssignmentStatus() == AssignmentStatus.Approved;
    }

}
