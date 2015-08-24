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

import java.util.Collection;

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
	public HALElement serializeStatus(MeasurementStatus measurementStatus) {
		if( !measurementStatus ) {
			return []
		}
		
		// Combine the advices with texts and create a structure to serialize
		def output = measurementStatus.all.collect { status ->
			def statusStructure = [
				property: serializeMeasurable(status.entity),
				value: serializeMeasuredValue(status.value),
				status: status.status,
			]
			
			if( status.color )
				statusStructure.color = status.color.toString()
			
			statusStructure
		}
		
		output
	}

	/**
	 * Serializes a list of advisables to give advice on
	 * @param advisables
	 * @return
	 */
	@Override
	public HALElement serializeEntityList(List<Advisable> advisables) {
		// TODO: Implement this
		return null
	}
	
	@Override
	public HALElement serializeReferences(Map<Property,List<ReferenceValue>> references) {
		def output  = []
		references.each { property, referenceValues ->
			if( !referenceValues ) {
				log.warn "No references found when serializing " + property
				return
			}
			
			def referenceStructure = [:]
			referenceStructure.property = serializeMeasurable( property )
			referenceStructure.references = referenceValues.collect { referenceValue ->
				serializeReference(referenceValue)
			}
			 
			output << referenceStructure
		}
		
		output
	}

	@Override
	public HALElement serializeAdvices(List<Advice> advices, String language = "en") {
		if( !advices ) {
			return []
		}
		
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		def texts = AdviceText.getTranslations( advices, language )
		
		// Combine the advices with texts and create a structure to serialize
		def output = advices.collect { advice ->
			[
				code: advice.code,
				subject: serializeMeasurable(advice.subject),
				text: texts[ advice.code ]
			]
		}
		
		output
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
	}

	/**
	 * Serializes a reference value
	 * @param reference
	 * @return
	 */
	protected HALElement serializeReference( ReferenceValue reference ) {
		if( !reference )
			return null
		
		def element = new HALElement(generateLink( controller: "food4me", action: "references", id: reference.id ) )
			
		element.parameters = [
			status: reference.status,
			color: reference.color.toString(),
		]
		
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
	
	protected Map serializeReferenceCondition(ReferenceCondition condition, includeProperty = false ) {
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
	
	protected class HALElement extends HALEntry {
		List<HALLink> links = []
		Map parameters = [:]
		Map<String,HALEntry> embedded = [:] 
		
		public HALElement(String selfURI) {
			links << new HALLink(name: "self", href: selfURI )
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
		
		def addLink( HALLink link ) {
			if( link ) links << link
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
		String name
		String href
		String hreflang
		String type
		
		def toHAL() {
			def data = ["href": href]
			
			if(hreflang)
				data.hreflang = hreflang
				
			if(type) 
				data.type = type
				
			[ (name): data ]
		}
	}
	
	protected generateLink(Map params) {
		// Due to a bug, the format is not used in link generation
		// https://github.com/grails/grails-core/issues/589
		params.format = "hal"
		params.absolute = true
		
		grailsLinkGenerator.link(params) + ".hal"
	}
	
}
