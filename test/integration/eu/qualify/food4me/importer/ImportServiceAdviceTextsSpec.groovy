package eu.qualify.food4me.importer

import eu.qualify.food4me.Property
import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceText
import grails.test.mixin.*

/**
 * Test for importService.
 * 
 * This is an integration test instead of a unit test because the import service make use of the 
 * CSV plugin, which doesn't add the toCsvReader methods in unit tests
 */
class ImportServiceAdviceTextsSpec extends ImportServiceIntegrationSpec {
	
	void "test importing advice translations"() {
		given: "a list of advices to import, in the expected format"
			// Format: code, text
			def advicesToImport = [
				[ "L0.0.1", "Jetzt geht loss" ],
				[ "L0.2.5", "Gutentag" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(advicesToImport)
			
			assert AdviceText.count == 0
		
		and: "the corresponding advices in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			carbohydrate.save()
			def advice1 = new Advice( code: "L0.0.1", subject: carbohydrate )
			advice1.save()
			def advice2 = new Advice( code: "L0.2.5", subject: carbohydrate )
			advice2.save()
			
		when: "importing the texts in German"
			importService.loadAdviceTexts(is, "de")
		
		then: "texts for both advices are imported"
			AdviceText.count == 2
			AdviceText.countByCode( "L0.0.1" ) == 1
			AdviceText.countByCode( "L0.2.5" ) == 1
			
		and: "the advice texts"
			AdviceText text1 = AdviceText.findByCode( "L0.0.1" )
			text1.language == "de"
			text1.text == "Jetzt geht loss"
			
			AdviceText text2 = AdviceText.findByCode( "L0.2.5" )
			text2.language == "de"
			text2.text == "Gutentag"
	}
	
	void "test importing translations with improper format and data"() {
		given: "a list of advices to import, in the expected format"
			// Format: code, text
			def advicesToImport = [
				[],
				[ "L0.0.1", "Jetzt geht loss" ],
				[ "", "Test123" ],
				[ "L0.2.5", "Gutentag" ],
				[ "Non-existing-code", "Test" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(advicesToImport)
			
			assert AdviceText.count == 0
		
		and: "the corresponding advices in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			carbohydrate.save()
			def advice1 = new Advice( code: "L0.0.1", subject: carbohydrate )
			advice1.save()
			def advice2 = new Advice( code: "L0.2.5", subject: carbohydrate )
			advice2.save()
			
		when: "importing the texts in German"
			importService.loadAdviceTexts(is, "de")
		
		then: "texts for both advices are imported, other lines are discarded"
			AdviceText.count == 3
			AdviceText.countByCode( "L0.0.1" ) == 1
			AdviceText.countByCode( "L0.2.5" ) == 1
			AdviceText.countByCode( "Non-existing-code" ) == 1
	}
	
	void "test importing translations with special characters"() {
		given: "a list of advices to import, in the expected format with special characters in UTF-8"
			// Format: code, text
			def advicesToImport = [
				[ "L0.0.1", "ĂĔļ" ],
				[ "L0.2.5", "ʨΏʡ̈́" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(advicesToImport)
			
			assert AdviceText.count == 0
		
		and: "the corresponding advices in the database, without text"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			carbohydrate.save()
			def advice1 = new Advice( code: "L0.0.1", subject: carbohydrate )
			advice1.save()
			def advice2 = new Advice( code: "L0.2.5", subject: carbohydrate )
			advice2.save()
			
		when: "importing the texts in English"
			importService.loadAdviceTexts(is, "de")
		
		then: "texts for both advices are imported"
			AdviceText.count == 2
			AdviceText.findByCode( "L0.0.1" )?.text == "ĂĔļ"
			AdviceText.findByCode( "L0.2.5" )?.text == "ʨΏʡ̈́"
	}
	
	void "test importing translations that already exist"() {
		given: "a list of advices to import, in the expected format"
			// Format: code, text
			def advicesToImport = [
				[ "L0.0.1", "Jetzt geht loss" ],
				[ "L0.2.5", "Gutentag" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(advicesToImport)
		
		and: "the corresponding advices in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			carbohydrate.save()
			def advice1 = new Advice( code: "L0.0.1", subject: carbohydrate )
			advice1.save()
			def advice2 = new Advice( code: "L0.2.5", subject: carbohydrate )
			advice2.save()
		
		and: "an existing translation for one of the advices"
			new AdviceText( code: "L0.0.1", language: "de", advice: advice1, text: "to be overwritten" ).save()
			assert AdviceText.count == 1

		when: "importing the texts in German"
			importService.loadAdviceTexts(is, "de")
		
		then: "texts for both advices are imported, and the old translation has been removed"
			AdviceText.count == 2
			AdviceText.countByCode( "L0.0.1" ) == 1
			AdviceText.countByCode( "L0.2.5" ) == 1
			
		and: "the advice text for the overwritten translation is correctly imported"
			AdviceText text1 = AdviceText.findByCode( "L0.0.1" )
			text1.text == "Jetzt geht loss"
	}
	
}
