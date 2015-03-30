/*
 *  Copyright (C) 2015 The Hyve
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	
	void "test importing translations without text"() {
		given: "a list of advices to import without text"
			// Format: code, text
			def advicesToImport = [
				[ "L0.0.1"  ],
				[ "L0.2.5", "Some text" ],
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
		
		then: "texts for the second advice is imported, the first one is discarded"
			AdviceText.count == 1
			AdviceText.countByCode( "L0.2.5" ) == 1
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
