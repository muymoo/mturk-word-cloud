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

import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * This class acts as an interface to Mechanical Turk
 */
public class MechanicalTurkService
{
    private RequesterService service;

    // Defining the attributes of the HIT to be created
    private String           title          = "Answer a question";
    private String           description    = "This is a HIT created by the Mechanical Turk SDK.  Please answer the provided question.";
    private int              numAssignments = 1;
    private double           reward         = 0.05;

    /**
     * Constructor
     * 
     */
    public MechanicalTurkService()
    {
        service = new RequesterService(new PropertiesClientConfig("conf/mturk.properties"));
    }

    /**
     * Check if there are enough funds in your account in order to create the HIT
     * on Mechanical Turk
     * 
     * @return true if there are sufficient funds. False if not.
     */
    public boolean hasEnoughFund()
    {
        double balance = service.getAccountBalance();
        System.out.println("Got account balance: " + RequesterService.formatCurrency(balance));
        return balance > reward;
    }

    /**
     * Creates the simple HIT.
     * 
     */
    public void createHelloWorld()
    {
        try
        {

            // The createHIT method is called using a convenience static method of
            // RequesterService.getBasicFreeTextQuestion that generates the QAP for
            // the HIT.
            HIT hit = service.createHIT(title, description, reward,
                    RequesterService.getBasicFreeTextQuestion("What is the weather like right now in Seattle, WA?"),
                    numAssignments);

            System.out.println("Created HIT: " + hit.getHITId());

            System.out.println("You may see your HIT with HITTypeId '" + hit.getHITTypeId() + "' here: ");
            System.out.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());

        }
        catch (ServiceException e)
        {
            System.err.println(e.getLocalizedMessage());
        }
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

        return hit.getHITId();
    }

    /**
     * @param hitId
     * @return
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
                String word = assigment.getAnswer();

                // Increment word count
                int count = wordCounts.containsKey(word) ? wordCounts.get(word) : 0;
                wordCounts.put(word, count);
            }
        }

        return wordCounts;
    }

}
