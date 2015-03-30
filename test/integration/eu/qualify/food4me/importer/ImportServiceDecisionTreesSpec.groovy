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
import eu.qualify.food4me.decisiontree.AdviceCondition
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceValue
import grails.test.mixin.*

/**
 * Test for decision tree imports with importService.
 * 
 * This is an integration test instead of a unit test because the import service make use of the 
 * CSV plugin, which doesn't add the toCsvReader methods in unit tests
 */
class ImportServiceDecisionTreesSpec extends ImportServiceIntegrationSpec {

	void "test importing decision trees"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
		 	//			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Value" ],
				[ "Advice 1", "High", "1" ],
				[ "Advice 2", "OK", "2" ],
				[ "Advice 3", "Below OK", "3" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported (where advice 3 leads to 2 records)"
			Advice.count == 4
			Advice.countBySubject( carbohydrate ) == 4
			
		and: "the advices are properly stored"
			Advice advice1 = Advice.findByCode( "Advice 1" )
			advice1.conditions?.size() == 2
			advice1.subject == carbohydrate
			
			advice1.conditions.contains( new AdviceCondition( subject: carbohydrate, status: "High" ) )
			advice1.conditions.contains( new AdviceCondition( subject: vitaminC, value: "1", modifier: "from food" ))

			Advice advice2 = Advice.findByCode( "Advice 2" )
			advice2.conditions?.size() == 2
			advice2.subject == carbohydrate
			advice2.conditions.contains( new AdviceCondition( subject: carbohydrate, status: "OK" ))
			advice2.conditions.contains( new AdviceCondition( subject: vitaminC, value: "2", modifier: "from food" ))

			def advices3 = Advice.findAllByCode( "Advice 3" )
			advices3.size() == 2
			
			advices3[0].subject == carbohydrate
			advices3[0].conditions.contains( new AdviceCondition( subject: vitaminC, value: "3", modifier: "from food" ))
			
			advices3[1].subject == carbohydrate
			advices3[1].conditions.contains( new AdviceCondition( subject: vitaminC, value: "3", modifier: "from food" ))

			// Either one of the advices3 has Low and the other has Very Low for carbohydrate
			def statusesForCarbohydrate = advices3.collect { advice -> advice.conditions.find { it.subject == carbohydrate }.status }
			statusesForCarbohydrate.contains( Status.STATUS_LOW )
			statusesForCarbohydrate.contains( Status.STATUS_VERY_LOW )
	}
	
	void "test importing decision trees advice without conditions"() {
		given: "a list of references to import, in the expected format, but without any condition"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[],
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Value" ],
				[ "Advice 2b", "", "" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advice has been imported"
			Advice.count == 1
			Advice.countBySubject( carbohydrate ) == 1
	}

	
	void "test importing decision trees with improper lines"() {
		given: "a list of references to import, in the expected format, but with bogus lines in between"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[],
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Value" ],
				[],
				[ "Advice 1", "High", "1" ],
				[ "", "High", "2" ],
				[ "Advice 2", "OK", "2" ],
				[ "Advice 2b", "", "" ],
				[ "Advice 3", "Below OK", "3" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported (where advice 3 leads to 2 records)"
			Advice.count == 5
			Advice.countBySubject( carbohydrate ) == 5
	}

	void "test importing decision trees with improper header lines "() {
		given: "a list of references to import with improperly formatted header"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "Advice 1", "High", "1" ],
				[ "Advice 2", "OK", "2" ],
				[ "Advice 3", "Low", "3" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "Not all advices are stored, as the first two lines are taken as header"
			Advice.count == 1
			Advice.countBySubject( carbohydrate ) == 1
		
		and: "the second line is used as modifier and the third line specified uses 'status' as default"
			Advice advice = Advice.findByCode( "Advice 3" )
			advice.conditions?.size() == 2
			advice.subject == carbohydrate
			
			advice.conditions.contains( new AdviceCondition( subject: carbohydrate, modifier: "High", status: "Low" ) )
			advice.conditions.contains( new AdviceCondition( subject: vitaminC, modifier: "1", status: "3" ))
	}

	void "test importing decision trees without proper reference data"() {
		given: "a list of references to import with improperly formatted header"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
		 	//			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Value" ],
				[ "Advice 1", "High", "1" ],
				[ "Advice 2", "OK", "2" ],
				[ "Advice 3", "Below OK", "3" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "carbohydrate exists in the database, vitaminC doesn't"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			carbohydrate.save()
			
			assert !Property.findByEntity( "Vitamin C" )
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "No advices are stored, as one or more of the condition properties doesn't exist"
			Advice.count == 0
	}
	
	void "test importing decision trees without the decision tree property being available"() {
		given: "a list of references to import with improperly formatted header"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Value" ],
				[ "Advice 1", "High", "1" ],
				[ "Advice 2", "OK", "2" ],
				[ "Advice 3", "Below OK", "3" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "vitaminC exists in the database, carbohydrate doesn't"
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "2331003" )
			vitaminC.save()
			
			assert !Property.findByEntity( "Carbohydrate" )
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "No advices are stored, as the decision tree property doesn't exist"
			Advice.count == 0
	}

	
	void "test importing decision trees with sparse data"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Status" ],
				[ "Advice 1", "High", "" ],
				[ "Advice 2", "", "Low" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported"
			Advice.count == 2
			Advice.countBySubject( carbohydrate ) == 2
			
		and: "the advices are properly stored"
			Advice advice1 = Advice.findByCode( "Advice 1" )
			advice1.conditions?.size() == 1
			advice1.subject == carbohydrate
			
			advice1.conditions.contains( new AdviceCondition( subject: carbohydrate, status: "High" ) )
	
			Advice advice2 = Advice.findByCode( "Advice 2" )
			advice2.conditions?.size() == 1
			advice2.subject == carbohydrate
			advice2.conditions.contains( new AdviceCondition( subject: vitaminC, status: "Low", modifier: "from food" ))
	}

	void "test importing decision trees with status Below OK"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Status" ],
				[ "Advice 1", "Below OK", "" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported and leads to two adrives"
			Advice.count == 2
			Advice.countBySubject( carbohydrate ) == 2

		and: "the advices for carbohydrate are properly stored"
			def advices = Advice.findAllByCode( "Advice 1" )
			advices.size() == 2
			def statusesForAdvice = advices.collect { advice -> advice.conditions.find { it.subject == carbohydrate }.status }
			statusesForAdvice.contains( Status.STATUS_LOW )
			statusesForAdvice.contains( Status.STATUS_VERY_LOW )
	}
	
	
	void "test importing decision trees with status Above OK"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Status" ],
				[ "Advice 1",  "Above OK", "" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported and leads to two advices"
			Advice.count == 2
			Advice.countBySubject( carbohydrate ) == 2

		and: "the advices for carbohydrate are properly stored"
			def advices = Advice.findAllByCode( "Advice 1" )
			advices.size() == 2
			def statusesForAdvice = advices.collect { advice -> advice.conditions.find { it.subject == carbohydrate }.status }
			statusesForAdvice.contains( Status.STATUS_HIGH )
			statusesForAdvice.contains( Status.STATUS_VERY_HIGH )
	}
	
	
	void "test importing decision trees with status OK and higher"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Status" ],
				[ "Advice 1", "OK and higher", "" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported"
			Advice.count == 3
			Advice.countBySubject( carbohydrate ) == 3

		and: "the advices for carbohydrate are properly stored"
			def advices = Advice.findAllByCode( "Advice 1" )
			advices.size() == 3
			def statusesForAdvice = advices.collect { advice -> advice.conditions.find { it.subject == carbohydrate }.status }
			statusesForAdvice.contains( Status.STATUS_OK )
			statusesForAdvice.contains( Status.STATUS_HIGH )
			statusesForAdvice.contains( Status.STATUS_VERY_HIGH )
	}

	
	void "test importing decision trees with status OK and lower"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
			 //			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Status" ],
				[ "Advice 1", "OK and lower", "" ],
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported"
			Advice.count == 3
			Advice.countBySubject( carbohydrate ) == 3

		and: "the advices for carbohydrate are properly stored"
			def advices = Advice.findAllByCode( "Advice 1" )
			advices.size() == 3
			def statusesForAdvice = advices.collect { advice -> advice.conditions.find { it.subject == carbohydrate }.status }
			statusesForAdvice.contains( Status.STATUS_OK )
			statusesForAdvice.contains( Status.STATUS_LOW )
			statusesForAdvice.contains( Status.STATUS_VERY_LOW )
	}
	
	void "test importing decision trees with multiple statuses in a row"() {
		given: "a list of references to import, in the expected format"
			// Format: 	Cel A1 contains the property that this decision tree is about
			//			Row 1 (from column B) contains the properties to decide on
		 	//			Row 2 (from column B) contains optional modifiers on the properties
			//			Row 3 (from column B) contains either Status or Value
			def decisionTreeToImport = [
				[ "Carbohydrate", "Carbohydrate", "Vitamin C" ],
				[ "", "", "from food" ],
				[ "", "Status", "Status" ],
				[ "Advice 1", "Below OK", "Above OK" ],
				[ "Advice 2", "OK and higher", "" ],
				[ "Advice 3", "", "OK and lower" ]
			]
			InputStream is = getInputStreamFromImportDatastructure(decisionTreeToImport)
			
			assert Advice.count == 0
		
		and: "the corresponding properties in the database"
			def carbohydrate = new Property( entity: "Carbohydrate", propertyGroup: "Nutrient", externalId: "2331003" )
			def vitaminC = new Property( entity: "Vitamin C", propertyGroup: "Nutrient", externalId: "286586005" )
			carbohydrate.save()
			vitaminC.save()
			
		when: "importing the decision trees"
			importService.loadDecisionTrees(is)
		
		then: "the advices have been imported (where advice 1 leads to 4 records and both advices 2 and 3 lead to 3 records)"
			Advice.count == 10
			Advice.countBySubject( carbohydrate ) == 10
			
		and: "the advices for advice1 are properly stored, i.e. combinations are made properly"
			def advices1 = Advice.findAllByCode( "Advice 1" )
			advices1.size() == 4
			def statusesForAdvice1 = advices1.collect { advice -> [ 
				advice.conditions.find { it.subject == carbohydrate }.status, 
				advice.conditions.find { it.subject == vitaminC }.status 
			] }
			statusesForAdvice1.contains( [ Status.STATUS_LOW, Status.STATUS_HIGH ] )
			statusesForAdvice1.contains( [ Status.STATUS_LOW, Status.STATUS_VERY_HIGH ] )
			statusesForAdvice1.contains( [ Status.STATUS_VERY_LOW, Status.STATUS_HIGH ] )
			statusesForAdvice1.contains( [ Status.STATUS_VERY_LOW, Status.STATUS_VERY_HIGH ] )
			
		and: "the advices for advice2 are properly stored"
			def advices2 = Advice.findAllByCode( "Advice 2" )
			advices2.size() == 3
			def statusesForAdvice2 = advices2.collect { advice -> advice.conditions.find { it.subject == carbohydrate }.status }
			statusesForAdvice2.contains( Status.STATUS_OK )
			statusesForAdvice2.contains( Status.STATUS_HIGH )
			statusesForAdvice2.contains( Status.STATUS_VERY_HIGH )
			
		and: "the advices for advice3 are properly stored"
			def advices3 = Advice.findAllByCode( "Advice 3" )
			advices3.size() == 3
			def statusesForAdvice3 = advices3.collect { advice -> advice.conditions.find { it.subject == vitaminC }.status }
			statusesForAdvice3.contains( Status.STATUS_OK )
			statusesForAdvice3.contains( Status.STATUS_LOW )
			statusesForAdvice3.contains( Status.STATUS_VERY_LOW )

	}

}
