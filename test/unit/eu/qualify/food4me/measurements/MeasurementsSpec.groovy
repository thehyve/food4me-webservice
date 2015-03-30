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
package eu.qualify.food4me.measurements

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock(Property)
class MeasurementsSpec extends Specification {

    def setup() {
		// Initialize properties needed
		def age = Property.findByEntity( "Age" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Age", externalId: "397669002")
			age.save(failOnError: true)
			
		def protein = Property.findByEntity( "Protein" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Protein", externalId: "123465")
			protein.save(failOnError: true)

		def carbohydrate = Property.findByEntity( "Carbohydrate" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Carbohydrate", externalId: "53451")
			carbohydrate.save(failOnError: true)

    }

    def cleanup() {
    }

    void "test getAllPropertiesForPropertyGroup"() {
		given: 
			Measurements measurements
			
			
		when: "there are no measurements"
			measurements = new Measurements()
		
		then: "no properties should be found"
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_NUTRIENT )
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_GENERIC )
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_BIOMARKER )

						
		when: "a single nutrient measurement is given"
			measurements.add( new Measurement( property: Property.findByEntity( "Protein" ), value: new MeasuredNumericValue( value: 1 ) ) )
		
		then: "property protein should be found"
			def properties = measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_NUTRIENT )
			properties.size() == 1
			properties.contains( Property.findByEntity( "Protein" ) )
			
		and: "no properties should be found for other groups"
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_GENERIC )
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_BIOMARKER )

			
		when: "a single nutrient (modifier) measurement is given"
			measurements.add( new Measurement( property: new ModifiedProperty( property: Property.findByEntity( "Protein" ), modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH ), value: new MeasuredNumericValue( value: 1 ) ) )
		
		then: "property protein should be found"
			def properties2 = measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_NUTRIENT )
			properties2.size() == 1
			properties2.contains( Property.findByEntity( "Protein" ) )
			
		and: "no properties should be found for other groups"
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_GENERIC )
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_BIOMARKER )

			
		when: "a multiple nutrient measurements are given"
			measurements.add( new Measurement( property: Property.findByEntity( "Protein" ), value: new MeasuredNumericValue( value: 1 ) ) )
			measurements.add( new Measurement( property: Property.findByEntity( "Carbohydrate" ), value: new MeasuredNumericValue( value: 1 ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: Property.findByEntity( "Protein" ), modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH ), value: new MeasuredNumericValue( value: 1 ) ) )
		
		then: "properties protein and carbohydrate should be found"
			def properties3 = measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_NUTRIENT )
			properties3.size() == 2
			properties3.contains( Property.findByEntity( "Protein" ) )
			properties3.contains( Property.findByEntity( "Carbohydrate" ) )
			
		and: "no properties should be found for other groups"
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_GENERIC )
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_BIOMARKER )

		when: "a multiple measurements for multiple groups are given"
			measurements.add( new Measurement( property: Property.findByEntity( "Protein" ), value: new MeasuredNumericValue( value: 1 ) ) )
			measurements.add( new Measurement( property: Property.findByEntity( "Carbohydrate" ), value: new MeasuredNumericValue( value: 1 ) ) )
			measurements.add( new Measurement( property: Property.findByEntity( "Age" ), value: new MeasuredNumericValue( value: 25 ) ) )
			
		then: "properties protein and carbohydrate should be found"
			def properties4 = measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_NUTRIENT )
			properties4.size() == 2
			properties4.contains( Property.findByEntity( "Protein" ) )
			properties4.contains( Property.findByEntity( "Carbohydrate" ) )

		then: "properties protein and carbohydrate should be found"
			def properties5 = measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_GENERIC )
			properties5.size() == 1
			properties5.contains( Property.findByEntity( "Age" ) )

		and: "no properties should be found for other groups"
			!measurements.getAllPropertiesForPropertyGroup( Property.PROPERTY_GROUP_BIOMARKER )

    }
}
