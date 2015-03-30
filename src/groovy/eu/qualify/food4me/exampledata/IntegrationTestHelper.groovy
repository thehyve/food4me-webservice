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
