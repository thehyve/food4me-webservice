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
package eu.qualify.food4me.decisiontree

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceValue
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Property,Unit,Advice,AdviceCondition])
class AdviceSpec extends Specification {

    def setup() {
		def geneFADS1 = Property.findByEntity("FADS1") ?: new Property(propertyGroup: Property.PROPERTY_GROUP_SNP, entity: "FADS1", externalId: "rs174546")
			geneFADS1.save(failOnError: true)
			
		def omega3Intake = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_NUTRIENT, entity: "Omega-3", externalId: "226332006")
			omega3Intake.save(failOnError: true)
			
		def omega3Biomarker = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_BIOMARKER) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_BIOMARKER, entity: "Omega-3", externalId: "226365003")
			omega3Biomarker.save(failOnError: true)
		
		def age = Property.findByEntity( "Age" ) ?: new Property(propertyGroup: Property.PROPERTY_GROUP_GENERIC, entity: "Age", externalId: "397669002")
			age.save(failOnError: true)
			
		if( Advice.countBySubject( omega3Intake ) == 0 ) {
			def advicesOnOmega3Intake = []
			
			def index = 1
			[ Status.STATUS_RISK, Status.STATUS_NON_RISK ].each { fads1Status ->
				[ Status.STATUS_LOW, "Intermediate", Status.STATUS_OK ].each { omega3BiomarkerStatus ->
					[ Status.STATUS_LOW, Status.STATUS_OK ].each { omega3IntakeTotalStatus ->
						[ Status.STATUS_LOW, Status.STATUS_OK ].each { omega3IntakeDietaryStatus ->
							advicesOnOmega3Intake << new Advice(subject: omega3Intake, code: sprintf( "L3.4.%03d", index ), text: sprintf( "Advice with code L3.4.%03d", index ) )
								.addToConditions( new AdviceCondition( subject: geneFADS1, status: fads1Status ) )
								.addToConditions( new AdviceCondition( subject: omega3Biomarker, status: omega3BiomarkerStatus ) )
								.addToConditions( new AdviceCondition( subject: omega3Intake, status: omega3IntakeTotalStatus ) )
								.addToConditions( new AdviceCondition( subject: omega3Intake, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY, status: omega3IntakeDietaryStatus ) )
								
							index++
						}
					}
				}
			}
				
			// Save all advices
			advicesOnOmega3Intake.each {
				it.save(failOnError: true)
			}
		}
    }

    def cleanup() {
    }

	void "test get condition properties with multiple conditions"() {
		given: "a set of properties related to omega3"
			def properties
			def fads1 = Property.findByEntity("FADS1")
			def omega3Intake = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_NUTRIENT)			
			def omega3Biomarker = Property.findByEntityAndPropertyGroup("Omega-3", Property.PROPERTY_GROUP_BIOMARKER)
			def omega3DietaryIntake = new ModifiedProperty( property: omega3Intake, modifier: ModifiedProperty.Modifier.INTAKE_DIETARY)
			
		when:
			properties = Advice.getConditionProperties( omega3Intake )
				
		then:
			properties.size() == 4
			properties.contains( fads1 )
			properties.contains( omega3Intake )
			properties.contains( omega3Biomarker )
			properties.contains( omega3DietaryIntake )
	}

		
	
	void "test condition properties without any data"() {
		given:
			def properties
			def age = Property.findByEntity( "Age" )
			
		when:
			properties = Advice.getConditionProperties( age )
			
		then:
			properties.size() == 0
	}
}
