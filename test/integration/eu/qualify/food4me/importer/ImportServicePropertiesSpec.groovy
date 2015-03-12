package eu.qualify.food4me.importer

import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit


/**
 * Test for importService.
 * 
 * This is an integration test instead of a unit test because the import service make use of the 
 * CSV plugin, which doesn't add the toCsvReader methods in unit tests
 */
class ImportServicePropertiesSpec extends ImportServiceIntegrationSpec {
	
	void "test importing properties"() {
		given: "a list of 2 properties to import, in the expected format"
			def propertiesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Glucose", "Biomarker", "67079006", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "286586005", "mg" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(propertiesToImport)
			
			assert Property.count == 0
		
		and: "the corresponding units in the database"
			new Unit( name: "mmol/L", code: "mmol/L", externalId: "258813002").save()
			new Unit( name: "mg", code: "mg", externalId: "'258684004").save()
			
		when: "importing the properties"
			importService.loadProperties(is)
		
		then: "both properties are imported and the header line is discarded"
			Property.count == 2
			Property.findByEntity( "Glucose" )
			Property.findByEntity( "Vitamin C" )
			Property.findByEntity( "Glucose" ).propertyGroup == "Biomarker"
			Property.findByEntity( "Glucose" ).externalId == "67079006"
			Property.findByEntity( "Glucose" ).unit instanceof Unit
			Property.findByEntity( "Glucose" ).unit.externalId == "258813002"
	}

	void "test importing duplicate properties"() {
		given: "a list of 2 properties to import, with the same external ID"
			def propertiesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Glucose", "Biomarker", "67079006", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "67079006", "mg" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(propertiesToImport)
			
			assert Property.count == 0
		
		and: "the corresponding units in the database"
			new Unit( name: "mmol/L", code: "mmol/L", externalId: "258813002").save()
			new Unit( name: "mg", code: "mg", externalId: "'258684004").save()
			
		when: "importing the properties"
			importService.loadProperties(is)
		
		then: "only one property is imported and the header line is discarded"
			Property.count == 1
			Property.findByEntity( "Glucose" )
			!Property.findByEntity( "Vitamin C" )
	}
	
	void "test importing properties with incorrect lines"() {
		given: "a list of 2 properties to import, in the expected format with incorrect lines in  between"
			def propertiesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Cholesterol" ],
				[ "Glucose", "Biomarker", "67079006", "mmol/L" ],
				[ "", "", "", "", "", "sixth column" ],
				[],
				[ "Vitamin C", "Nutrient", "286586005", "mg" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(propertiesToImport)
			
			assert Property.count == 0
		
		and: "the corresponding units in the database"
			new Unit( name: "mmol/L", code: "mmol/L", externalId: "258813002").save()
			new Unit( name: "mg", code: "mg", externalId: "'258684004").save()
			
		when: "importing the properties"
			importService.loadProperties(is)
		
		then: "both properties are imported and the incorrect lines are discarded"
			Property.count == 2
			Property.findByEntity( "Glucose" )
			Property.findByEntity( "Vitamin C" )
			
		and: "the improper lines are discarded"
			!Property.findByEntity( "Cholesterol" )

	}
	
	void "test importing properties without proper units in the database"() {
		given: "a list of 2 properties to import, in the expected format"
			def propertiesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Glucose", "Biomarker", "67079006", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "286586005", "mg" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(propertiesToImport)
			
			assert Property.count == 0
		
		and: "no corresponding units in the database"
			assert Unit.count == 0
			
		when: "importing the properties"
			importService.loadProperties(is)
		
		then: "both properties are imported and the incorrect lines are discarded"
			Property.count == 0
	}

}
