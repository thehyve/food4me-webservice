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
import eu.qualify.food4me.exampledata.IntegrationTestHelper;
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.measurements.Status
import grails.test.spock.IntegrationSpec

class GenerateAdviceServiceIntegrationSpec extends IntegrationSpec {
	def generateAdviceService
	Measurements measurements
	MeasurementStatus measurementStatus
	List<Advisable> advisables
	
	// Setup is done in bootstrap
	def setup() {
		// Make sure the database is empty
		IntegrationTestHelper.cleanUp()
		IntegrationTestHelper.bootStrap()
	}

	def cleanup() {
	}

	void "test basic advice generation"() {
		given: "a request to advise on omega3 intake"
			advisables = [ Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT) ]
			
		and: "a set of measurement statusses on omega3 related properties"
			def omega3Intake = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT)
			def omega3Biomarker = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_BIOMARKER)
			def geneFADS1 = Property.findByEntity("FADS1")
			def omega3DietaryIntake = new ModifiedProperty( property: omega3Intake, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY )
					
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( geneFADS1, new Status( entity: geneFADS1, status: Status.STATUS_RISK, color: Status.Color.RED ) )
			measurementStatus.putStatus( omega3Biomarker, new Status( entity: omega3Biomarker, status: Status.STATUS_LOW, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( omega3Intake, new Status( entity: omega3Intake, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
			measurementStatus.putStatus( omega3DietaryIntake, new Status( entity: omega3DietaryIntake, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
		
		and: "an empty list of measurements, as they are not needed in this decision tree"
			measurements = new Measurements()
			
		when: "the advice is generated"
			def advices = generateAdviceService.generateAdvice(measurements, measurementStatus, advisables)
			
		then: "the list of advices contains the proper advice codes"
			assert advices?.size() == 1
			assert advices[0].code == "L3.4.004"
	}
	
	void "test advice generation with not enough parameters"() {
		given: "a request to advise on omega3 intake"
			advisables = [ Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT) ]
			
		and: "a set of measurement statusses on omega3 related properties, but not on FADS1 gene"
			def omega3Intake = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT)
			def omega3Biomarker = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_BIOMARKER)
			def omega3DietaryIntake = new ModifiedProperty( property: omega3Intake, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY )
					
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( omega3Biomarker, new Status( entity: omega3Biomarker, status: Status.STATUS_LOW, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( omega3Intake, new Status( entity: omega3Intake, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
			measurementStatus.putStatus( omega3DietaryIntake, new Status( entity: omega3DietaryIntake, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
		
		and: "an empty list of measurements, as they are not needed in this decision tree"
			measurements = new Measurements()
			
		when: "the advice is generated"
			def advices = generateAdviceService.generateAdvice(measurements, measurementStatus, advisables)
			
		then: "the list of advices contains no advice codes"
			assert !advices
	}
	
	void "test advice generation on unknown parameter"() {
		given: "a request to advise on cholesterol"
			advisables = [ Property.findByEntity("Cholesterol") ]
			
		and: "a set of measurement statusses on omega3 related properties"
			def omega3Intake = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT)
			def omega3Biomarker = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_BIOMARKER)
			def omega3DietaryIntake = new ModifiedProperty( property: omega3Intake, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY )
					
			measurementStatus = new MeasurementStatus()
			measurementStatus.putStatus( omega3Biomarker, new Status( entity: omega3Biomarker, status: Status.STATUS_LOW, color: Status.Color.AMBER ) )
			measurementStatus.putStatus( omega3Intake, new Status( entity: omega3Intake, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
			measurementStatus.putStatus( omega3DietaryIntake, new Status( entity: omega3DietaryIntake, status: Status.STATUS_OK, color: Status.Color.GREEN ) )
		
		and: "an empty list of measurements, as they are not needed in this decision tree"
			measurements = new Measurements()
			
		when: "the advice is generated"
			def advices = generateAdviceService.generateAdvice(measurements, measurementStatus, advisables)
			
		then: "the list of advices contains no advice codes, as we don't have a decision tree for cholesterol"
			assert !advices
	}

}
