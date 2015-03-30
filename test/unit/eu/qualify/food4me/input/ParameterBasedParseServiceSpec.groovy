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
package eu.qualify.food4me.input

import spock.lang.Specification
import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.measurements.Measurements
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ParameterBasedParseService)
@Mock(Property)
class ParameterBasedParseServiceSpec extends Specification {
	def age
	def protein
	def carbohydrate 
	def gender
	
    def setup() {
		// Initialize properties needed. Units are not needed
		age = Property.findByEntity( "Age" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Age", externalId: "397669002")
			age.save(failOnError: true)
			
		protein = Property.findByEntity( "Protein" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Protein", externalId: "123465")
			protein.save(failOnError: true)

		carbohydrate = Property.findByEntity( "Carbohydrate" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Carbohydrate", externalId: "53451")
			carbohydrate.save(failOnError: true)
			
		gender = Property.findByEntity( "Gender" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Gender", externalId: "1234")
			gender.save(failOnError: true)

    }

    def cleanup() {
		Property.list().each { it.delete() }
    }

    void "test simple measurements"() {
		given: "a set of input parameters"
			def parameters = [
				generic: [
					age: "28"
				],
				nutrient: [
					protein: [
						(ModifiedProperty.Modifier.INTAKE_DIETARY.id): "12",
						(ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id): "5"
					],
					carbohydrate: [
						(ModifiedProperty.Modifier.INTAKE_EGGS.id): "0.3",
						(ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id): "3.1"
					]
	
				]
			]
		
		when: "the parameters are parsed"
			Measurements output = service.parseMeasurements(parameters)
			
		then: "the proper measurements are stored"
			output.all.size() == 5
			output.getValueFor( age )?.value == 28
			output.getValueFor( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ) ).value == 12 
			output.getValueFor( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ) ).value == 5 
			output.getValueFor( new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_EGGS.id ) ).value == 0.3
			output.getValueFor( new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ) ).value == 3.1 
    }
	
	void "test parsing measurements without valid structure"() {
		given: "a set of input parameters"
			def parameters = [
				// Only valid groups should be allowed
				unknown_group: [
					age: "20",
					carbohydrate: "5"
				],
			
				// An empty map doesn't contain any measurements
				generic: [],
				
				// the composed string of parameter parts should be ignored
				'nutrient.protein.from food': "5",
				
				// Nutrients should always contain modifiers
				nutrient: [
					protein: "1"
				]
			]
		
		when: "the parameters are parsed"
			Measurements output = service.parseMeasurements(parameters)
			
		then: "no measurements are stored, as all items are invalid"
			output.all.size() == 0
	}

	void "test parsing measurements expressed as strings"() {
		given: "a set of input parameters"
			def parameters = [
				// Only valid groups should be allowed
				generic: [
					age: "20",
					gender: "Male"
				],
			]
		
		when: "the parameters are parsed"
			Measurements output = service.parseMeasurements(parameters)
			
		then: "The measurement for age is stored as numeric, and the measurement for gender is stored as string"
			output.all.size() == 2
			output.getValueFor( age )?.value == 20
			output.getValueFor( age )?.type == "numeric"
			output.getValueFor( gender )?.value == "Male"
			output.getValueFor( gender )?.type == "text"
	}

}
