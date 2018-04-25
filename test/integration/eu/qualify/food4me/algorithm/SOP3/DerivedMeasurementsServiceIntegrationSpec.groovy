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

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.exampledata.IntegrationTestHelper
import eu.qualify.food4me.measurements.MeasuredNumericValue
import eu.qualify.food4me.measurements.MeasuredTextValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.Measurements
import grails.test.spock.IntegrationSpec
import org.hibernate.type.descriptor.java.BigDecimalTypeDescriptor

class DerivedMeasurementsServiceIntegrationSpec extends IntegrationSpec {
	def derivedMeasurementsService
	Measurements measurements
	
	// Setup is done in bootstrap
	def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()
		IntegrationTestHelper.bootStrap()
		
		// Insert properties for omega-3 and total carotenoids calculations
		new Property( entity: "Omega-3 index", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "123" ).save()
		new Property( entity: "Docosapentaenoic acid", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "1235" ).save()
		new Property( entity: "Eicosapentanoic acid", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "1234" ).save()
		new Property( entity: "Docosahexaenoic acid", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "1236" ).save()
		
		new Property( entity: "Carotenoids", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "001" ).save()
		new Property( entity: "Alpha-carotene", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "0012" ).save()
		new Property( entity: "Beta-carotene", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "0013" ).save()
		new Property( entity: "Lutein", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "0014" ).save()
		new Property( entity: "Zeaxanthin", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "0015" ).save()
		new Property( entity: "Beta-cryptoxanthin", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "0016" ).save()
		new Property( entity: "Lycopene", propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, externalId: "0017" ).save()
	}

	def cleanup() {
	}

	void "test basic nutrient value derivation"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def age = Property.findByEntity( "Age" )
			def fibre = Property.findByEntity( "Fibre" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_EGGS.id ), value: new MeasuredNumericValue( value: 2, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_SWEETS_SNACKS.id ), value: new MeasuredNumericValue( value: 12, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 4, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "the total and intake from food are determined for protein "
			measurements
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 1.3, unit: Unit.findByCode( "g/kg bw" ) ) ) ) 
			measurements.all.contains( new Measurement( derived: true, property: protein, value: new MeasuredNumericValue( value: 1.8, unit: Unit.findByCode( "g/kg bw" ) ) ) )

		and: "the total and intake from food are determined for carbohydrate"
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 18, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.all.contains( new Measurement( derived: true, property: carbohydrate, value: new MeasuredNumericValue( value: 18, unit: Unit.findByCode( "% energy intake" ) ) ) )

		and: "no measurements are derived for age"
			measurements.getValuesFor(age)?.size() == 1
	}

	void "test nutrient value derivation when units don't match"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "mg" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "ug" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no derived measurements are determined for protein, as one or more units don't match"
			measurements
			measurements.getValuesFor(protein)?.size() == 4
			!measurements.getValueFor(protein)
	}
	
	void "test nutrient value derivation when total is already known"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		and: "a measurement already exists for the total intake of protein"
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no total intake is determined for protein, as is was already provided"
			measurements
			measurements.all.contains( new Measurement( derived: false, property: protein, value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		and: "the total food intake is computed"
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 1.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
		
	}
	
	void "test nutrient value derivation when intake from food is already known"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		and: "a measurement already exists for the intake from food of protein"
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 1.0, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no intake from food is determined for protein, as is was already provided"
			measurements
			measurements.all.contains( new Measurement( derived: false, property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 1.0, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		and: "the total intake is computed"
			measurements.all.contains( new Measurement( derived: true, property: protein, value: new MeasuredNumericValue( value: 1.8, unit: Unit.findByCode( "g/kg bw" ) ) ) )
	}
	
	void "test nutrient value derivation for non nutrient values"() {
		given: "a set of textual measurements for nutrients"
			def age = Property.findByEntity( "Age" )
			def gender = Property.findByEntity( "Gender" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			measurements.add( new Measurement( property: gender, value: new MeasuredTextValue( value: "Male" ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no measurements are derived for gender"
			measurements.getValuesFor(gender)?.size() == 1
			
		and: "no measurements are derived for age"
			measurements.getValuesFor(age)?.size() == 1
	}
	
	void "test nutrient value derivation with only supplement intake"() {
		given: "a set of nutrient measurements for nutrients from supplements"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 12, unit: Unit.findByCode( "% energy intake" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "the total and intake from food are determined for protein, with 0 for dietary intake"
			measurements
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 0, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.all.contains( new Measurement( derived: true, property: protein, value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
	
		and: "the total and intake from food are determined for carbohydrate, with 0 for dietary intake"
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 0, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.all.contains( new Measurement( derived: true, property: carbohydrate, value: new MeasuredNumericValue( value: 12, unit: Unit.findByCode( "% energy intake" ) ) ) )
	}
	
	void "test nutrient value derivation for textual nutrient values"() {
		given: "a set of nutrient measurements for non-nutrients"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def age = Property.findByEntity( "Age" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredTextValue( value: "unknown", unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredTextValue( value: "optimal", unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_EGGS.id ), value: new MeasuredTextValue( value: "none", unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 12, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 31, unit: Unit.findByCode( "years" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "the total and intake from food are determined for protein, ignoring the text values"
			measurements
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.all.contains( new Measurement( derived: true, property: protein, value: new MeasuredNumericValue( value: 0.9, unit: Unit.findByCode( "g/kg bw" ) ) ) )
	
		and: "the total and intake from food are determined for carbohydrate, ignoring text values"
			measurements.all.contains( new Measurement( derived: true, property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY.id ), value: new MeasuredNumericValue( value: 0, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.all.contains( new Measurement( derived: true, property: carbohydrate, value: new MeasuredNumericValue( value: 12, unit: Unit.findByCode( "% energy intake" ) ) ) )
	
		and: "no measurements are derived for age"
			measurements.getValuesFor(age)?.size() == 1

	}
	
	void "test nutrient contributing foodgroup derivation"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			def carbohydrate = Property.findByEntity( "Carbohydrate" )
			def age = Property.findByEntity( "Age" )
			def fibre = Property.findByEntity( "Fibre" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.6, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_EGGS.id ), value: new MeasuredNumericValue( value: 2, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_SWEETS_SNACKS.id ), value: new MeasuredNumericValue( value: 12, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.INTAKE_DAIRY.id ), value: new MeasuredNumericValue( value: 4, unit: Unit.findByCode( "% energy intake" ) ) ) )
			measurements.add( new Measurement( property: age, value: new MeasuredNumericValue( value: 35, unit: Unit.findByCode( "years" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "the two contributing food groups for protein are determined"
			measurements
			measurements.all.contains( new Measurement( 
				derived: true,
				property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP.id ), 
				value: new MeasuredTextValue( value: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ) ) )
			measurements.all.contains( new Measurement( 
				derived: true,
				property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.SECOND_CONTRIBUTING_FOOD_GROUP.id ), 
				value: new MeasuredTextValue( value: ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA.id ) ) )
	
		and: "the two contributing food groups for carbohydrate are determined"
			measurements
			measurements.all.contains( new Measurement( 
				derived: true,
				property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP.id ), 
				value: new MeasuredTextValue( value: ModifiedProperty.Modifier.INTAKE_SWEETS_SNACKS.id ) ) )
			measurements.all.contains( new Measurement( 
				derived: true,
				property: new ModifiedProperty( property: carbohydrate, modifier: ModifiedProperty.Modifier.SECOND_CONTRIBUTING_FOOD_GROUP.id ), 
				value: new MeasuredTextValue( value: ModifiedProperty.Modifier.INTAKE_DAIRY.id ) ) )
	
		and: "no food groups are derived for age"
			measurements.getValuesFor(age)?.size() == 1
	}
	
	void "test nutrient contributing foodgroup derivation without intake from food"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: protein, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: "unknown" ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 0.5, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no contributing food groups for protein are determined, as no foodgroups are specified"
			measurements
			!measurements.all*.property.contains( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP.id ) )
			!measurements.all*.property.contains( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.SECOND_CONTRIBUTING_FOOD_GROUP.id ) )
	}
	
	void "test nutrient contributing foodgroup derivation with intake from only one foodgroup"() {
		given: "a set of only one nutrient measurement"
			def protein = Property.findByEntity( "Protein" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no contributing food groups for protein are determined, as no foodgroups are specified"
			measurements
			measurements.all.contains( new Measurement( 
				derived: true,
				property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP.id ), 
				value: new MeasuredTextValue( value: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ) ) )
			!measurements.all*.property.contains( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.SECOND_CONTRIBUTING_FOOD_GROUP.id ) )
	}
	
	void "test nutrient contributing foodgroup derivation with high intake from supplements"() {
		given: "a set of nutrient measurements"
			def protein = Property.findByEntity( "Protein" )
			
			measurements = new Measurements()
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ), value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			measurements.add( new Measurement( property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS.id ), value: new MeasuredNumericValue( value: 1.0, unit: Unit.findByCode( "g/kg bw" ) ) ) )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no contributing food groups for protein are determined, as no foodgroups are specified"
			measurements
			measurements.all.contains( new Measurement(
				derived: true,
				property: new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.FIRST_CONTRIBUTING_FOOD_GROUP.id ),
				value: new MeasuredTextValue( value: ModifiedProperty.Modifier.INTAKE_MEAT_FISH.id ) ) )
			!measurements.all*.property.contains( new ModifiedProperty( property: protein, modifier: ModifiedProperty.Modifier.SECOND_CONTRIBUTING_FOOD_GROUP.id ) )
	}
	
	void "test omega3 index computation"() {
		given: 
			def n3Index = Property.findByEntity( "Omega-3 index" )
			def dpa = Property.findByEntity( "Docosapentaenoic acid" )
			def epa = Property.findByEntity( "Eicosapentanoic acid" )
			def dha = Property.findByEntity( "Docosahexaenoic acid" )
			
		and: "the appropriate set of measurements is given"
			measurements = new Measurements()
			measurements.add( new Measurement( property: dpa, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "%" ) ) ) )
			measurements.add( new Measurement( property: epa, value: new MeasuredNumericValue( value: 0.85, unit: Unit.findByCode( "%" ) ) ) )
			measurements.add( new Measurement( property: dha, value: new MeasuredNumericValue( value: 0.7, unit: Unit.findByCode( "%" ) ) ) )
			
			assert !measurements.getValueFor( n3Index )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "omega 3 index is present"
			measurements
			measurements.getValueFor( n3Index ) == new MeasuredNumericValue( value: 3.066385, unit: Unit.findByCode( "%" ) )
	}
	
	void "test omega3 index computation without all ingredients"() {
		given:
			def n3Index = Property.findByEntity( "Omega-3 index" )
			def dpa = Property.findByEntity( "Docosapentaenoic acid" )
			def epa = Property.findByEntity( "Eicosapentanoic acid" )
			def dha = Property.findByEntity( "Docosahexaenoic acid" )
			
		and: "most measurements are given, but not for EPA"
			measurements = new Measurements()
			measurements.add( new Measurement( property: dpa, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "%" ) ) ) )
			measurements.add( new Measurement( property: dha, value: new MeasuredNumericValue( value: 0.7, unit: Unit.findByCode( "%" ) ) ) )
			
			assert !measurements.getValueFor( n3Index )
			assert !measurements.getValueFor( epa )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no omega 3 index is computed"
			measurements
			!measurements.getValueFor( n3Index )
	}
	
	
	void "test omega3 index computation with text measurements"() {
		given:
			def n3Index = Property.findByEntity( "Omega-3 index" )
			def dpa = Property.findByEntity( "Docosapentaenoic acid" )
			def epa = Property.findByEntity( "Eicosapentanoic acid" )
			def dha = Property.findByEntity( "Docosahexaenoic acid" )
			
		and: "a set of measurements is given with a textual value for EPA"
			measurements = new Measurements()
			measurements.add( new Measurement( property: dpa, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "%" ) ) ) )
			measurements.add( new Measurement( property: epa, value: new MeasuredTextValue( value: "unknown", unit: Unit.findByCode( "%" ) ) ) )
			measurements.add( new Measurement( property: dha, value: new MeasuredNumericValue( value: 0.7, unit: Unit.findByCode( "%" ) ) ) )
			
			assert !measurements.getValueFor( n3Index )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no omega 3 index is present"
			measurements
			!measurements.getValueFor( n3Index )
	}

	void "test total carotenoids computation"() {
		given:
			def carotenoids = Property.findByEntity( "Carotenoids" )
			def ac = Property.findByEntity("Alpha-carotene" )
			def bc = Property.findByEntity("Beta-carotene" )
			def lu = Property.findByEntity("Lutein" )
			def zx = Property.findByEntity("Zeaxanthin" )
			def bcr = Property.findByEntity("Beta-cryptoxanthin" )
			def lp  = Property.findByEntity("Lycopene" )
			
		and: "the appropriate set of measurements is given"
			measurements = new Measurements()
			measurements.add( new Measurement( property: ac, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: bc, value: new MeasuredNumericValue( value: 0.85, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: lu, value: new MeasuredNumericValue( value: 0.7, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: zx, value: new MeasuredNumericValue( value: 2, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: bcr, value: new MeasuredNumericValue( value: 1.6, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: lp, value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "mcg" ) ) ) )

			assert !measurements.getValueFor( carotenoids )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "the value for total carotenoids is present"
			measurements
			measurements.getValueFor( carotenoids ) == new MeasuredNumericValue( value: 0.4 + 0.85 + 0.7 + 2 + 1.6 + 0.3, unit: Unit.findByCode( "mcg" ) )
	}
	
	void "test total carotenoids computation without all ingredients"() {
		given:
			def carotenoids = Property.findByEntity( "Carotenoids" )
			def ac = Property.findByEntity("Alpha-carotene" )
			def bc = Property.findByEntity("Beta-carotene" )
			def lu = Property.findByEntity("Lutein" )
			def zx = Property.findByEntity("Zeaxanthin" )
			def bcr = Property.findByEntity("Beta-cryptoxanthin" )
			def lp  = Property.findByEntity("Lycopene" )
			
		and: "most measurements are given, but not for lutein"
			measurements = new Measurements()
			measurements.add( new Measurement( property: ac, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: bc, value: new MeasuredNumericValue( value: 0.85, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: zx, value: new MeasuredNumericValue( value: 2, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: bcr, value: new MeasuredNumericValue( value: 1.6, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: lp, value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "mcg" ) ) ) )
	
			assert !measurements.getValueFor( carotenoids )
			assert !measurements.getValueFor( lu )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no value for total carotenoids is present"
			measurements
			!measurements.getValueFor( carotenoids )
	}
	
	void "test total carotenoids computation with textual values"() {
		given:
			def carotenoids = Property.findByEntity( "Carotenoids" )
			def ac = Property.findByEntity("Alpha-carotene" )
			def bc = Property.findByEntity("Beta-carotene" )
			def lu = Property.findByEntity("Lutein" )
			def zx = Property.findByEntity("Zeaxanthin" )
			def bcr = Property.findByEntity("Beta-cryptoxanthin" )
			def lp  = Property.findByEntity("Lycopene" )
			
		and: "most measurements are given, but a textual one for lutein"
			measurements = new Measurements()
			measurements.add( new Measurement( property: ac, value: new MeasuredNumericValue( value: 0.4, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: bc, value: new MeasuredNumericValue( value: 0.85, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: lu, value: new MeasuredTextValue( value: "very high", unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: zx, value: new MeasuredNumericValue( value: 2, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: bcr, value: new MeasuredNumericValue( value: 1.6, unit: Unit.findByCode( "mcg" ) ) ) )
			measurements.add( new Measurement( property: lp, value: new MeasuredNumericValue( value: 0.3, unit: Unit.findByCode( "mcg" ) ) ) )
	
			assert !measurements.getValueFor( carotenoids )
			
		when: "the derived measurements are computed"
			derivedMeasurementsService.deriveMeasurements( measurements )
		
		then: "no value for total carotenoids is present"
			measurements
			!measurements.getValueFor( carotenoids )
	}
	
}
