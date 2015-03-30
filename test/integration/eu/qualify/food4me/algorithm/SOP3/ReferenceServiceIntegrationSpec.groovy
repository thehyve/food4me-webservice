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
package eu.qualify.food4me.algorithm.SOP3

import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.exampledata.IntegrationTestHelper
import eu.qualify.food4me.interfaces.Measurable
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredTextValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceValue
import grails.test.spock.IntegrationSpec

class ReferenceServiceIntegrationSpec extends IntegrationSpec {
	def referenceService
	Measurements measurements
	
	// Setup is done in bootstrap
	def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()
		IntegrationTestHelper.bootStrap()
	}

	def cleanup() {
	}

	void "test basic reference retrieval"() {
		given: "an empty set of measurements"
			def protein = Property.findByEntity( "Protein" )
			measurements = new Measurements()
			
		when: "retrieving references for protein"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [protein], measurements )
		
		then: "the system returns all 4 references for protein"
			references
			references.keySet().size() == 1
			references[protein]
			references[protein].size() == 4
			references[protein]*.status.sort() == [ Status.STATUS_VERY_LOW, Status.STATUS_LOW, Status.STATUS_OK, Status.STATUS_HIGH ].sort() 
	}
	
	void "test whether non-generic measurements are ignored"() {
		given: "a set of some measurements"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def age = Property.findByEntity( "Age" )
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: carbohydrate, value: new MeasuredNumericValue( value: 50, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			
		when: "retrieving references for protein"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [protein, carbohydrate], measurements )
		
		then: "all references for protein and carbohydrate are returned"
			references
			references.keySet().size() == 2
			references[protein]
			references[protein].size() == 4
			references[carbohydrate]
			references[carbohydrate].size() == 5
	}
	
	void "test whether age and gender are taken into account"() {
		given: "a set of some measurements"
			def fibre = Property.findByEntity( "Fibre" )	
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			measurements = new Measurements()
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "Male" ) ) )
			
		when: "retrieving references for fibre"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [fibre], measurements )
		
		then: "references for fibre are returned"
			references
			references.keySet().size() == 1
			references[fibre]
			references[fibre].size() == 3
		
		and: "only the ones for 35 year old males are returned"
			references[fibre]*.status.sort() == [ Status.STATUS_VERY_LOW, Status.STATUS_LOW, Status.STATUS_OK ].sort()
			def condition = references[fibre].find { it.status == Status.STATUS_LOW }.conditions.find { it.subject == fibre }
			condition
			condition.low == 28
			condition.high == 38 
	}
	
	void "test whether only gender references are returned properly"() {
		given: "a set of age and gender"
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			def vitaminA = Property.findByEntity( "Vitamin A" )
			measurements = new Measurements()
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "Male" ) ) )
			
		when: "retrieving references for fibre"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [vitaminA], measurements )
		
		then: "references for fibre are returned"
			references
			references.keySet().size() == 1
			references[vitaminA]
			references[vitaminA].size() == 4
	}
	
	void "test whether only gender references are returned properly when only gender is provided"() {
		given: "a set of measurements with only gender"
			def gender = Property.findByEntity( "Gender" )
			def vitaminA = Property.findByEntity( "Vitamin A" )
			measurements = new Measurements()
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "Male" ) ) )
			
		when: "retrieving references for fibre"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [vitaminA], measurements )
		
		then: "references for fibre are returned"
			references
			references.keySet().size() == 1
			references[vitaminA]
			references[vitaminA].size() == 4
	}
	
	void "test whether any references are returned when age and gender are not specified"() {
		given: "a set of some measurements"
			def fibre = Property.findByEntity( "Fibre" )
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			measurements = new Measurements()
			
		when: "retrieving references for fibre"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [fibre], measurements )
		
		then: "fibre is mentioned, but no references for fibre are returned, as age and gender are required"
			references
			references.containsKey(fibre)
			!references[fibre]
	}

	void "test whether multiple references for multiple properties are returned correctly"() {
		given: "a set of some measurements"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			measurements = new Measurements()
			
		when: "retrieving references for protein"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [protein, carbohydrate], measurements )
		
		then: "all references for protein and carbohydrate are returned"
			references
			references.keySet().size() == 2
			references[protein]
			references[protein].size() == 4
			references[carbohydrate]
			references[carbohydrate].size() == 5
	}
	
	void "test whether properties without references are ignored"() {
		given: "a set of some measurements"
			def fibre = Property.findByEntity( "Fibre" )
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			measurements = new Measurements()
			
		when: "retrieving references for age"
			Map<Measurable, List<ReferenceValue>> references = referenceService.getReferences( [age], measurements )
		
		then: "no references for age are returned, as they do not exist"
			!references

	}
	
}
