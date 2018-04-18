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
package eu.qualify.food4me

import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.interfaces.*
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements
import eu.qualify.food4me.reference.ReferenceValue
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class Food4meController {
	
	// Services where the real action happens. See conf/spring/resources.groovy
	StatusComputer statusComputer
	AdvisableDeterminer advisableDeterminer
	AdviceGenerator adviceGenerator
	
	// Services for input and output. See conf/spring/resources.groovy
	Parser parser
	Serializer jsonSerializer
	Serializer halSerializer
	
	def derivedMeasurementsService
	
	def referenceService
	
	/**
	 * Form that the user can use to explore the API 
	 * @return
	 */
	def form() {
		// Find all properties grouped by propertygroup
		def groupedProperties = [:]
		def nutrients = []
		Property.list( sort: 'entity' ).each {
			if( it.propertyGroup == Property.PROPERTY_GROUP_NUTRIENT ) {
				nutrients << it
			} else {
				if(!groupedProperties[it.propertyGroup])
					groupedProperties[it.propertyGroup] = []
					
				groupedProperties[it.propertyGroup] << it
			}
		}
		
		// Determine the modifiers to allow the user to enter through the form
		def nutrientModifiers = [
			ModifiedProperty.Modifier.INTAKE_MEAT_FISH,
			ModifiedProperty.Modifier.INTAKE_DAIRY,
			ModifiedProperty.Modifier.INTAKE_SOUP_SAUCES,
			ModifiedProperty.Modifier.INTAKE_SWEETS_SNACKS,
			ModifiedProperty.Modifier.INTAKE_FATS_SPREADS,
			ModifiedProperty.Modifier.INTAKE_POTATOES_RICE_PASTA,
			ModifiedProperty.Modifier.INTAKE_EGGS,
			ModifiedProperty.Modifier.INTAKE_SUPPLEMENTS,
		]
		
		// Convert names of the property groups into names of the parameter (groups) in the URL
		def conversionMap = [
			(Property.PROPERTY_GROUP_GENERIC): 'generic',
			(Property.PROPERTY_GROUP_NUTRIENT): 'nutrient',
			(Property.PROPERTY_GROUP_BIOMARKER): 'biomarker',
			(Property.PROPERTY_GROUP_SNP): 'snp',
			(Property.PROPERTY_GROUP_PHYSICAL): 'physical',
			(Property.PROPERTY_GROUP_FOODGROUP): 'foodgroup'
		]
		
		// Send output to the view
		[
			nutrients: nutrients,
			nutrientModifiers: nutrientModifiers, 
			properties: groupedProperties, 
			conversionMap: conversionMap,
			language: params.language ?: "en"
		]
	}

	/**
	 * Webservice that returns a list of supported properties 
	 * 
	 * The list of properties depends on the data that was loaded
	 * @return
	 */
	def properties() {
		def criteria = Property.createCriteria()
		
		def properties = criteria.list {
			and {
				order('propertyGroup')
				order('entity')
			}
		}
		
		// Use content negotiation to output the data
		withFormat {
			html { 
				def propertyModifiers = [:]
				properties.each { property -> propertyModifiers[ property ] = ModifiedProperty.getAllowedModifiers(property) }
				[ properties: properties, propertyModifiers: propertyModifiers ]  
			}
			json { render jsonSerializer.serializeProperties( properties ) as JSON }
			hal {
				render text: halSerializer.serializeProperties( properties ) as JSON, contentType: "application/hal+json"
			}
		}
	}
	
	/**
	 * Webservice that returns details about a single property.
	 * 
	 * This method is used to have the URL for a property in HAL resolve to a page
	 *
	 * The list of properties depends on the data that was loaded
	 * @return
	 */
	def property() {
		def property = Property.findByExternalId(params.id)
		
		if( !property ) {
			response.status = 404
			render "Property not found"
			return
		}
		
		// Use content negotiation to output the data
		withFormat {
			html {
				def propertyModifiers = [(property): ModifiedProperty.getAllowedModifiers(property) ]
				render view: "properties", model: [ properties: [property], propertyModifiers: propertyModifiers ] 
			}
			json { render jsonSerializer.serializeProperty( property ) as JSON }
			hal {
				render text: halSerializer.serializeProperty( property ) as JSON, contentType: "application/hal+json"
			}
		}
	}
	
	
	/**
	 * Webservice that returns details about a single unit.
	 *
	 * This method is used to have the URL for a property in HAL resolve to a page
	 *
	 * @return
	 */
	def units() {
		def criteria = Unit.createCriteria()
		
		def units = criteria.list {
			and {
				order('name')
			}
		}
		
		// Use content negotiation to output the data
		withFormat {
			html {
				[ units: units ]
			}
			json { render jsonSerializer.serializeUnits( units ) as JSON }
			hal {
				render text: halSerializer.serializeUnits( units ) as JSON, contentType: "application/hal+json"
			}
		}
	}
	
	/**
	 * Webservice that returns details about a single unit.
	 *
	 * This method is used to have the URL for a property in HAL resolve to a page
	 *
	 * @return
	 */
	def unit() {
		def unit = Unit.findByExternalId(params.id)
		
		if( !unit) {
			response.status = 404
			render "Unit not found"
			return
		}
		
		// Use content negotiation to output the data
		withFormat {
			html {
				render view: "units", [ units: [unit] ]
			}
			json { render jsonSerializer.serializeUnit( unit ) as JSON }
			hal {
				render text: halSerializer.serializeUnit( unit ) as JSON, contentType: "application/hal+json"
			}
		}
	}
	
	/**
	 * Webservice to return status of the provided raw measurements
	 * 
	 * @see Parser.parseMeasurements()
	 * @return
	 */
	def status() {
		Measurements measurements = parser.parseMeasurements(params)
		derivedMeasurementsService.deriveMeasurements(measurements)
		MeasurementStatus status = statusComputer.computeStatus(measurements)

		// Use content negotiation to output the data
		withFormat {
			html { [ measurements: measurements, status: status, references: referenceService.getReferences( measurements.all*.property, measurements ) ] }
			json { render jsonSerializer.serializeStatus( status ) as JSON }
			hal { render text: halSerializer.serializeStatus( status ) as JSON, contentType: "application/hal+json" }
		}
	}

	/**
	 * Webservice to return references
	 * @return
	 */
	def references() {
		// Parse a list of entities to return the references for
		List<Measurable> entities = parser.parseEntityList(params)
		
		// Parse the list of measurements to find the references. Only gender and age 
		// are used, the rest of the measurements are discarded
		Measurements measurements = parser.parseMeasurements(params)
		
		// Retrieve the references that apply
		Map<Property,List<ReferenceValue>> references = referenceService.getReferences( entities, measurements )

		// Use content negotiation to output the data
		withFormat {
			html entities: entities, references: references, measurements: measurements, secondaryConditions: [ "age", "gender" ]
			json { render jsonSerializer.serializeReferences( references ) as JSON }
			hal { render text: halSerializer.serializeReferences( references, measurements.all.findAll { it.property.rootProperty.entity in [ "Age", "Gender" ] } ) as JSON, contentType: "application/hal+json" }
		}
	}
	
	/**
	 * Webservice to return a single reference
	 * @return
	 */
	def reference() {
		def reference = ReferenceValue.get(params.long("id"))
		
		if( !reference ) {
			response.status = 404
			render "Reference not found"
			return
		}
		
		// Use content negotiation to output the data
		withFormat {
			html {
				[reference: reference]
			}
			json { render jsonSerializer.serializeReference( reference ) as JSON }
			hal {
				render text: halSerializer.serializeReference( reference ) as JSON, contentType: "application/hal+json"
			}
		}
	}
			
	/**
	 * Webservice to return advices based on the raw measurements
	 * @return
	 */
	def advices() {
		Measurements measurements = parser.parseMeasurements(params)
		derivedMeasurementsService.deriveMeasurements(measurements)
		
		MeasurementStatus status = statusComputer.computeStatus(measurements)
		List<Advisable> advisables = advisableDeterminer.determineAdvisables(status, measurements )
		List<Advice> advices = adviceGenerator.generateAdvice( measurements, status, advisables )

		def language = getLanguageFromRequest()
		if( !language ) return
		
		// Use content negotiation to output the data
		withFormat {
			html advices: advices, measurements: measurements, status: status, translations: AdviceText.getTranslations( advices, language )
			json { render jsonSerializer.serializeAdvices( advices, language ) as JSON }
			hal { render text: halSerializer.serializeAdvices( advices, language ) as JSON, contentType: "application/hal+json" }
		}
	}
	
	
	def advice() {
		def advice = Advice.findByCode(params.id)
		
		log.debug "Finding advice for code [" + params.id + "]: " + advice
		
		if( !advice) {
			response.status = 404
			render "Advice not found"
			return
		}
		
		// Determine language
		def language = getLanguageFromRequest()
		if( !language ) return
		
		// Use content negotiation to output the data
		withFormat {
			html {
				def advices = [advice]
				render view: "advices", [ advices: advices, translations: AdviceText.getTranslations( advices, language )  ]
			}
			json { render jsonSerializer.serializeAdvice( advice, advice.getTranslation(language)) as JSON }
			hal {
				render text: halSerializer.serializeAdvice( advice, advice.getTranslation(language) ) as JSON, contentType: "application/hal+json"
			}
		}
	}
	
	protected String getLanguageFromRequest() {
		// Determine output language. Defaults to English
		def language = params.language
		if( !language )
			language = "en"

		// If the language is not supported, return 404
		if( !AdviceText.isLanguageSupported( language ) ) {
			response.status = 404
			render ""
			return null
		}

		return language
	}
}
