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
package eu.qualify.food4me.output

import org.codehaus.groovy.grails.web.mapping.LinkGenerator

import eu.qualify.food4me.ModifiedProperty
import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.interfaces.Measurable
import eu.qualify.food4me.interfaces.Serializer
import eu.qualify.food4me.measurements.MeasuredValue
import eu.qualify.food4me.measurements.Measurement
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue


/**
 * Serializes output data in a format to be used for generating JSON HAL format
 * @author robert
 */
class HALSerializationService implements Serializer {
	// Inject link generator
	LinkGenerator grailsLinkGenerator
	
	@Override
	public Map serializeStatus(MeasurementStatus measurementStatus) {
		if( !measurementStatus ) {
			return [:]
		}
		
		def element = new HALElement(generateLink( controller: "food4me", action: "status" ))
		element.addParameter "count", measurementStatus.all.size()
		
		// Combine the advices with texts and create a structure to serialize
		def output = measurementStatus.all.collect { status ->
			def statusStructure = new HALElement()
			statusStructure.addParameter("status", status.status)
			statusStructure.addParameter("propertyName", status.entity.toString())
			
			if( status.value ) {
				statusStructure.addParameter("value", status.value.value)
				
				if( status.value.unit )
					statusStructure.addParameter("unitCode", status.value.unit.code)
			}
				
			statusStructure.addEmbedded("property", serializeMeasurable(status.entity))
			statusStructure.addEmbedded("value", serializeMeasuredValue(status.value))
			
			if( status.color )
				statusStructure.addParameter("color", status.color.toString())
			
			statusStructure
		}
		
		element.addEmbedded( "status", new HALList(elements: output) )
		
		element.toHAL()
	}

	/**
	 * Serializes a list of advisables to give advice on
	 * @param advisables
	 * @return
	 */
	@Override
	public Map serializeEntityList(List<Advisable> advisables) {
		// TODO: Implement this
		return null
	}
	
	public Map serializeReferences(Map<Property,List<ReferenceValue>> references, List<Measurement> measurements = null) {
		// Determine the parameters to base the reference on. These parameters are 
		// used for generating self links
		def params = [:]
		if( measurements ) {
			measurements.each { measurement ->
				def rootProperty = measurement.property.rootProperty
				def parameterName = rootProperty.propertyGroup + "." + rootProperty.entity 
				params[ parameterName.toLowerCase() ] = measurement.value.value
			}
		}
		
		def element = new HALElement(generateLink( controller: "food4me", action: "references", params: params ))
		element.addParameter "count", references.size()
		
		// Add references to the structure
		def referenceElements  = []
		references.each { property, referenceValues ->
			if( !referenceValues ) {
				log.warn "No references found when serializing " + property
				return
			}
			
			def referenceStructure = new HALElement(generateLink( controller: "food4me", action: "references", params: params + [ property: property.entity ] ) )
			referenceStructure.addEmbedded( "property", serializeMeasurable( property ))
			referenceStructure.addEmbedded( "references", new HALList(elements: referenceValues.collect { referenceValue ->
				referenceAsHAL(referenceValue)
			}))
			referenceElements << referenceStructure
		}
		
		element.addEmbedded "references", new HALList(elements: referenceElements )
		
		element.toHAL()
	}

	@Override
	public Map serializeAdvices(List<Advice> advices, String language = "en") {
		if( !advices ) {
			return [:]
		}
		
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		def texts = AdviceText.getTranslations( advices, language )
		
		def element = new HALElement()
		element.addParameter "count", advices.size()
		
		// Combine the advices with texts and create a structure to serialize
		def output = advices.collect { advice -> adviceAsHAL(advice, texts[advice.code], language) }
		
		element.addEmbedded( "advices", new HALList(elements: output) )
		
		element.toHAL()
	}
	
	public Map serializeAdvice(Advice advice, String translatedText, String language = "en" ) {
		adviceAsHAL(advice, translatedText, language).toHAL()
	}
	
	public HALElement adviceAsHAL(Advice advice, String translatedText, String language = "en") {
		// Closure to avoid repeating the link generation logic
		def createAdviceLink = { linkLanguage ->
			generateLink( mapping: "translatedAdvice", controller: "food4me", action: "advice", id: advice.code, params: [ language: linkLanguage ] )
		} 
		
		def element = new HALElement(createAdviceLink(language))
		element.addLinks( "translations", AdviceText.getLanguagesForAdvice(advice).collect {
			 new HALLink(
				 href: createAdviceLink(it),
				 hreflang: it
			 )
		})
		
		element.addParameter( "code", advice.code)
		element.addParameter( "text", translatedText)
		element.addEmbedded( "subject", serializeMeasurable(advice.subject))
		
		element
	}
	
	@Override
	public Map serializeProperties(Collection<Property> properties) {
		def element = new HALElement(generateLink( controller: "food4me", action: "properties" ))
		element.addParameter "count", properties.size()
		
		// Add properties to the structure
		element.addEmbedded "properties", new HALList(elements: properties.collect { propertyAsHAL(it) } ) 
		
		element.toHAL()
	}
	
	@Override
	public Map serializeProperty( Property property ) {
		propertyAsHAL(property).toHAL()
	}
	
	public HALElement propertyAsHAL(Property property) {
		serializeMeasurable(property, ModifiedProperty.getAllowedModifiers(property)*.id)
	}

	@Override
	public Map serializeUnits(Collection<Unit> units) {
		def element = new HALElement(generateLink( controller: "food4me", action: "units" ))
		element.addParameter "count", units.size()
		
		// Add properties to the structure
		element.addEmbedded "units", new HALList(elements: units.collect { unitAsHAL(it) } )
		
		element.toHAL()
	}
		
	@Override
	public Map serializeUnit( Unit unit ) {
		unitAsHAL(unit).toHAL()
	}

	/**
	 * Serializes a unit
	 * @param measurable
	 * @return
	 */
	public HALElement unitAsHAL( Unit unit ) {
		if( !unit )
			return null
		
		def element = new HALElement(generateLink( controller: "food4me", action: "unit", id: unit.externalId ) )
		element.parameters = [
			code: unit.code,
			name: unit.name
		]
		
		element
	}

	
	/**
	 * Serializes a reference value
	 * @param reference
	 * @return
	 */
	@Override
	public Map serializeReference( ReferenceValue reference ) {
		referenceAsHAL(reference, true).toHAL()
	}
	
	/**
	 * Serializes a reference value
	 * @param reference
	 * @return
	 */
	public HALElement referenceAsHAL( ReferenceValue reference, boolean includeSubject = false ) {
		if( !reference )
			return null
		
		def element = new HALElement(generateLink( controller: "food4me", action: "reference", id: reference.id ) )
			
		element.parameters = [
			status: reference.status,
			color: reference.color.toString(),
		]
		
		// If subject should be added, do so as a refernece
		if( includeSubject ) {
			element.addEmbedded "subject", serializeMeasurable(reference.subject)
		}
		
		// Add boundaries
		if( reference.subjectCondition ) {
			element.addEmbedded "subjectCondition", serializeReferenceCondition(reference.subjectCondition)
		}
		 
		def conditions = []
		reference.conditions.each { condition ->
			// Conditions for the subject iself are represented separately
			if( condition.subject == reference.subject )
				return;
			
			conditions << serializeReferenceCondition( condition, true )
		}
		
		if( conditions )
			element.addEmbedded "conditions", new HALList(elements: conditions)
			
		element
	}

	/**
	 * Serializes a measurable
	 * @param measurable
	 * @return
	 */
	protected HALElement serializeMeasurable( Measurable measurable, def modifiers = null ) {
		if( !measurable )
			return null
			
		def element = new HALElement( generateLink( controller: "food4me", action: "property", id: measurable.rootProperty.externalId ) )
		
		element.addParameter "name", measurable.rootProperty.entity
		element.addParameter "group", measurable.rootProperty.propertyGroup
		
		if( measurable instanceof ModifiedProperty ) {
			element.addParameter "modifier", measurable.modifier
		}
		
		if( modifiers )
			element.addParameter "modifiers", modifiers

		element.addEmbedded( "unit", unitAsHAL( measurable.rootProperty.unit ) )
					
		element
	}

	/**
	 * Serializes a measured value
	 * @param measurable
	 * @return
	 */
	protected HALElement serializeMeasuredValue( MeasuredValue value ) {
		if( !value )
			return null
		
		def element = new HALElement()
		element.parameters = [
			value: value.value,
			type: value.type,
		]
		
		element.addEmbedded( "unit", unitAsHAL( value.unit ) )
		
		element
	}
	
	protected HALElement serializeReferenceCondition(ReferenceCondition condition, includeProperty = false ) {
		def element = new HALElement()
		
		if( includeProperty )
			element.addEmbedded "property", serializeMeasurable( condition.subject )
		
		if( condition.low )
			element.addParameter "lower_boundary", condition.low
		
		if( condition.high )
			element.addParameter "upper_boundary", condition.high

		if( condition.value )
			element.addParameter "exact_match", condition.value
		
		element
	}
	
	/** 
	 * Classes for HAL generation
	 */
	protected abstract class HALEntry {
		abstract def toHAL()
	}

	protected class HALKeyValue{
		String key
		HALEntry value
		
		def toHAL() {
			def data = [(key): value.toHAL()]
		}
	}

	protected class HALElement extends HALEntry {
		List<HALEntry> links = []
		Map parameters = [:]
		Map<String,HALEntry> embedded = [:] 
		
		public HALElement(String selfURI = null) {
			if( selfURI )
				links << new HALKeyValue( 'key': "self", 'value': new HALLink(href: selfURI))
		}
		
		def toHAL() {
			def data = [:] + parameters
			
			if(links) {
				data[ "_links" ] = links.collectEntries { it.toHAL() }
			}
				
			if(embedded) {
				data[ "_embedded" ] = embedded.collectEntries {
					[(it.key): it.value.toHAL() ]
				}
			}
			
			data
		}
		
		def addLink( String name, HALLink link ) {
			if( link ) links << new HALKeyValue( 'key': name, 'value': link )
		}
		def addLinks( String key, List<HALLink> newLinks ) {
			if( key && newLinks )
				links << new HALKeyValue( 'key': key, 'value': new HALList(elements: newLinks ) )
		}

		
		def addEmbedded( String key, HALEntry element ) {
			if( key && element ) embedded[key] = element
		}
		
		def addParameter(String key, def value) {
			parameters[key] = value
		}
	}
	
	protected class HALList extends HALEntry {
		List<HALElement> elements
		
		def add(def element) {
			elements << element
		}
		
		def toHAL() {
			elements.collect { it.toHAL() }
		}
	}

	protected class HALLink extends HALEntry {
		String href
		String hreflang
		String type
		
		def toHAL() {
			def data = ["href": href]
			
			if(hreflang)
				data.hreflang = hreflang
				
			if(type) 
				data.type = type
				
			data
		}
	}
	
	protected generateLink(Map params) {
		// Due to a bug, the format is not used in link generation
		// https://github.com/grails/grails-core/issues/589
		params.format = "hal"
		params.absolute = true
		
		def link = grailsLinkGenerator.link(params)
		def separatorIndex = link.indexOf( "?" )
		
		if( separatorIndex > -1 ) {
			link = link[0..separatorIndex-1] + ".hal" + link[separatorIndex..-1]
		} else {
			link += ".hal"
		}
		
		link
	}
	
}
