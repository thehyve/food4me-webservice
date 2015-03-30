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
				[ "Glucose", "Biomarker", "67079006", "", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "286586005", "", "mg" ]
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
	
	void "test importing properties with secondary external ID"() {
		given: "a list of 2 properties to import, one without external ID and one with only a secondary ID"
			def propertiesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Glucose", "Biomarker", "", "", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "", "other ID", "mg" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(propertiesToImport)
			
			assert Property.count == 0
		
		and: "the corresponding units in the database"
			new Unit( name: "mmol/L", code: "mmol/L", externalId: "258813002").save()
			new Unit( name: "mg", code: "mg", externalId: "'258684004").save()
			
		when: "importing the properties"
			importService.loadProperties(is)
		
		then: "only the second property is imported, with the correct ID"
			Property.count == 1
			Property.findByEntity( "Vitamin C" )
			Property.findByEntity( "Vitamin C" ).externalId == "other ID"
	}

	void "test importing duplicate properties"() {
		given: "a list of 2 properties to import, with the same external ID"
			def propertiesToImport = [
				[ "headerline", "to", "be", "ignored" ],
				[ "Glucose", "Biomarker", "67079006", "", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "67079006", "", "mg" ]
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
				[ "Glucose", "Biomarker", "67079006", "", "mmol/L" ],
				[ "", "", "", "", "", "sixth column" ],
				[],
				[ "Vitamin C", "Nutrient", "286586005", "", "mg" ]
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
				[ "Glucose", "Biomarker", "67079006", "", "mmol/L" ],
				[ "Vitamin C", "Nutrient", "286586005", "", "mg" ]
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
