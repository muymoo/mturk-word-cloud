package services;

import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

public class WordValidationService {
	
	public WordValidationService() 
	{
		
	}
	
	public boolean isValid(String word) 
	{
		System.out.println("Checking validity of word \"" + word + "\"");
		
		if (containsWhitespace(word)) return false;
		
		WordDefinition definition;
		try {
			definition = requestDefinition(trimPunctuation(word).toLowerCase());
		}
		catch (Exception e) {
			//give the word the benefit of the doubt if request fails.
			return true;
		}
		
		if (definition.hasDefinitions() && !definition.isAbbreviation() && !definition.isOffensive())
			return true;
		else
			return false;
	}
	
	public boolean containsWhitespace(String word)
	{
		Pattern pattern = Pattern.compile("\\s");
		Matcher matcher = pattern.matcher(word);
		return matcher.find();
	}
	
	public String trimPunctuation(String word)
	{
		String frontTrimmed = word.replaceFirst("^[\\p{Punct}]+", "");
		String trimmed = frontTrimmed.replaceAll("[\\p{Punct}]+$", "");
		return trimmed;
	}
	
	private WordDefinition requestDefinition(String word) throws Exception 
	{
		return new WordDefinition(word);
	}
		
	private static final String API_KEY = "ee2b86e0-577f-44f8-97b0-16324a7049d0";
	
	private class WordDefinition 
	{		
		private Document document;
		
		public WordDefinition(String word) throws Exception
		{
			URL url = new URI("http", "www.dictionaryapi.com", "/api/v1/references/collegiate/xml/" + word, "key=" + API_KEY, null).toURL();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			document = builder.parse(url.toString());
		}
		
		public boolean hasDefinitions()
		{	
			NodeList nodes = document.getDocumentElement().getElementsByTagName(ENTRY_TAG);
			return nodes.getLength() > 0;
		}
		
		public boolean isAbbreviation()
		{
			if (!hasDefinitions()) return false;
			
			NodeList entryNodes = document.getDocumentElement().getElementsByTagName(ENTRY_TAG);
			NodeList partOfSpeechNodes = document.getDocumentElement().getElementsByTagName(FUNCTIONAL_LABEL_TAG);

			if (partOfSpeechNodes.getLength() <= 0) return false; //if no part of speech given

			if (partOfSpeechNodes.item(0).getParentNode() != entryNodes.item(0)) return false; //if no part of speech given for first entry
			
			Node primaryPartOfSpeech = partOfSpeechNodes.item(0);
			String partOfSpeech = primaryPartOfSpeech.getTextContent();
			
			if (partOfSpeech.contains("abbr")) return true;
			return false;
		}
		
		public boolean isOffensive()
		{
			for (String tag : WORD_SENSE_TAGS)
			{
				NodeList nodes = document.getDocumentElement().getElementsByTagName(tag);
				for (int i = 0; i < nodes.getLength(); ++i)
				{
					if (containsOffensiveKeywords(nodes.item(i).getTextContent()))
						return true;
				}
			}
			return false;
		}
		
		private boolean containsOffensiveKeywords(String phrase) 
		{
			for (String keyword : offensiveKeywords)
			{
				if (phrase.contains(keyword)) return true;
			}
			return false;
		}
		
		private final String FUNCTIONAL_LABEL_TAG = "fl";
		private final String ENTRY_TAG = "entry";
		private final String[] WORD_SENSE_TAGS = { "sl", "ssl", "sense", "slb" };
		
		private final String[] offensiveKeywords = {
			"vulgar",
			"offensive",
			"disparaging",
			"obscene"
		};
	}
}

