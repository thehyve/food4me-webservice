package eu.qualify.food4me.exampledata

import eu.qualify.food4me.Property;
import eu.qualify.food4me.Unit;
import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceCondition
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue

class IntegrationTestHelper {
	static def cleanUp() {
		ReferenceCondition.executeUpdate( "DELETE FROM ReferenceCondition" )
		ReferenceValue.executeUpdate( "DELETE FROM ReferenceValue" )
		AdviceCondition.executeUpdate( "DELETE FROM AdviceCondition" )
		Advice.executeUpdate( "DELETE FROM Advice" )
		Property.executeUpdate( "DELETE FROM Property" )
		Unit.executeUpdate( "DELETE FROM Unit" )
	}
	
	static def bootStrap() {
		println "Bootstrapping test data"
		ExampleData.initializeGenericData()
		ExampleData.initializeReferences()
		ExampleData.initializeOmega3AdviceL3_4()
	}
}
