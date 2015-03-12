package eu.qualify.food4me.importer

import eu.qualify.food4me.Unit
import grails.test.mixin.*

/**
 * Test for importService.
 * 
 * This is an integration test instead of a unit test because the import service make use of the 
 * CSV plugin, which doesn't add the toCsvReader methods in unit tests
 */
class ImportServiceUnitsSpec extends ImportServiceIntegrationSpec {
	
    void "test importing units"() {
		given: "a list of 2 units to import, in the expected format"
			def unitsToImport = [ 
				[ "headerline", "to", "be", "ignored" ], 
				[ "Gram", "g", "258682000" ],
				[ "Centimeter", "cm", "2586821512" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(unitsToImport)
			
			assert Unit.count == 0
				
		when: "importing the units"
			importService.loadUnits(is)
		
		then: "both units are imported and the header line is discarded"
			Unit.count == 2
			Unit.findByCode( "g" )
			Unit.findByCode( "cm" )
			Unit.findByCode( "g" ).name == "Gram"
			Unit.findByCode( "g" ).externalId == "258682000"
    }
	
	void "test importing duplicate units"() {
		given: "a list of 2 units to import, with the same external ID"
			def unitsToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Gram", "g", "258682000" ],
				[ "Centimeter", "cm", "258682000" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(unitsToImport)
			
			assert Unit.count == 0
				
		when: "importing the units"
			importService.loadUnits(is)
		
		then: "only the first unit is imported"
			Unit.count == 1
			Unit.findByCode( "g" )
			!Unit.findByCode( "cm" )
	}

	void "test importing units with incorrect lines"() {
		given: "a list of 2 units to import, with the same external ID"
			def unitsToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Meters" ],
				[ "", "", "", "fourth column" ],
				[ "Gram", "g", "258682000" ],
				[],
				[ "Centimeter", "cm", "258682001" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(unitsToImport)
			
			assert Unit.count == 0
				
		when: "importing the units"
			importService.loadUnits(is)
		
		then: "gram and cm are imported"
			Unit.count == 2
			Unit.findByCode( "g" )
			Unit.findByCode( "cm" )
			
		and: "the improper lines are discarded"
			!Unit.findByName( "Meters" )
	}

	
}
