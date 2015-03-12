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
	
	void "test importing decision trees with improper lines"() {}
	void "test importing decision trees without proper reference data"() {}
	
	void "test importing decision trees with sparse data"() {}	
}
