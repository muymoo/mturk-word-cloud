import static org.junit.Assert.*;

import org.junit.Test;

import services.WordValidationService;

public class WordValidationServiceTest {

	@Test
	public void WellThisWasAFunTestToWrite() {
		WordValidationService validator = new WordValidationService();
		
		assertFalse(validator.isValid("mattmomont"));
		assertTrue(validator.isValid("-is"));
		assertTrue(validator.isValid("a"));
		assertTrue(validator.isValid("creepy"));
		assertFalse(validator.isValid("dick"));
		assertFalse(validator.isValid("fucking"));
		assertFalse(validator.isValid("cunt"));
		assertFalse(validator.isValid("shitbag"));
		assertFalse(validator.isValid("penismonger"));
		assertFalse(validator.isValid("asshole"));
		assertFalse(validator.isValid("."));
		assertFalse(validator.isValid(""));
		assertTrue(validator.isValid("When"));
		assertTrue(validator.isValid("he's"));
		assertTrue(validator.isValid("near"));
		assertTrue(validator.isValid("it's"));
		assertTrue(validator.isValid("like"));
		assertFalse(validator.isValid("S#*RYFSIOF#I@FSINEIS"));
		assertFalse(validator.isValid("saweo8fhzs@$^(fhv32"));
		assertFalse(validator.isValid("!!!!!!!!!!"));
		assertTrue(validator.isValid("My"));
		assertTrue(validator.isValid("disgust"));
		assertTrue(validator.isValid("manifests"));
		assertTrue(validator.isValid("itself"));
		assertTrue(validator.isValid("in"));
		assertTrue(validator.isValid("gibberish!"));

		assertTrue(validator.isValid("ha--."));
		assertTrue(validator.isValid("game"));
		assertTrue(validator.isValid("on?"));
		assertFalse(validator.isValid("bitch"));
	}
	
}
