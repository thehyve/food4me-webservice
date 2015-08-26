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
 * Serializes output data in a structured way, to be used when outputting JSON or XML
 * @author robert
 */
class StructuredSerializationService implements Serializer {

	@Override
	public List serializeStatus(MeasurementStatus measurementStatus) {
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
	public String serializeEntityList(List<Advisable> advisables) {
		return null
	}
	
	@Override
	public List serializeReferences(Map<Property,List<ReferenceValue>> references) {
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
	public List serializeAdvices(List<Advice> advices, String language = "en") {
		if( !advices ) {
			return []
		}
		
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		def texts = AdviceText.getTranslations( advices, language )
		
		// Combine the advices with texts and create a structure to serialize
		def output = advices.collect { advice -> serializeAdvice(advice, texts[ advice.code ]) }
		
		output
	}
	
	public Map serializeAdvice(Advice advice, String translation) {
		[
			code: advice.code,
			subject: serializeMeasurable(advice.subject),
			text: translation
		]
	}
	
	@Override
	public List serializeProperties(Collection<Property> properties) {
		properties.collect { serializeProperty(it) }
	}
	
	@Override
	public Map serializeProperty(Property property) {
		def measurable = serializeMeasurable(property)
		def allowedModifiers = ModifiedProperty.getAllowedModifiers(property)*.id
		
		if( allowedModifiers )
			measurable.modifiers = allowedModifiers
				 
		measurable
	}
	
	@Override
	public List serializeUnits(Collection<Unit> units) {
		units.collect { serializeUnit(it) }
	}
	
	@Override
	public Map serializeUnit( Unit unit ) {
		if( !unit )
			return null
			
		[
			id: unit.externalId,
			code: unit.code,
			name: unit.name
		]
	}
	
	/**
	 * Serializes a measurable
	 * @param measurable
	 * @return
	 */
	protected Map serializeMeasurable( Measurable measurable ) {
		if( !measurable )
			return null
			
		def output = [
			id: measurable.rootProperty.externalId,
			name: measurable.rootProperty.entity,
			group: measurable.rootProperty.propertyGroup,
			unit: serializeUnit( measurable.rootProperty.unit )
		]
		
		if( measurable instanceof ModifiedProperty ) {
			output.modifier = measurable.modifier
		}
		
		output
	}

	/**
	 * Serializes a measured value
	 * @param measurable
	 * @return
	 */
	protected Map serializeMeasuredValue( MeasuredValue value ) {
		if( !value )
			return null
			
		[
			value: value.value,
			type: value.type,
			unit: serializeUnit( value.unit )
		]
	}

	/**
	 * Serializes a reference value
	 * @param reference
	 * @return
	 */
	public Map serializeReference( ReferenceValue reference ) {
		if( !reference )
			return null
			
		def output = [
			status: reference.status,
			color: reference.color.toString(),
		]
		
		// Add boundaries
		def subjectCondition = reference.subjectCondition
		if( subjectCondition ) {
			output.value = serializeReferenceCondition(subjectCondition)
		}
		 
		def conditions = []
		reference.conditions.each { condition ->
			// Conditions for the subject iself are represented separately
			if( condition.subject == reference.subject )
				return;
			
			conditions << serializeReferenceCondition( condition, true )
		}
		
		if( conditions )
			output.conditions = conditions
			
		output
	}
	
	protected Map serializeReferenceCondition(ReferenceCondition condition, includeProperty = false ) {
		def output = [:]
		
		if( includeProperty )
			output.property = serializeMeasurable( condition.subject )
		
		if( condition.low )
			output.lower_boundary = condition.low
		
		if( condition.high )
			output.upper_boundary = condition.high

		if( condition.value )
			output.exact_match = condition.value
		
		output
	}

}
