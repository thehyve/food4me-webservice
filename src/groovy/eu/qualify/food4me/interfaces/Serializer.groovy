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
package eu.qualify.food4me.interfaces

import java.util.List;
import java.util.Map;

import eu.qualify.food4me.Property;
import eu.qualify.food4me.Unit;
import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.reference.ReferenceValue;

/**
 * This interface determines methods to serialize data within 
 * the food4me webservices. Different serializers can output different
 * types of data (e.g. a String or a Map).
 * 
 * Ideally, for each serializer implementation, there should also
 * be a Parser implementation
 * 
 * @see Parser
 * @author robert
 */
interface Serializer {
	/**
	 * Serializes a set of measurement statusses 
	 * @param status
	 * @return
	 */
	def serializeStatus(MeasurementStatus status)

	/**
	 * Serializes a set of properties
	 * @param properties
	 * @return
	 */
	def serializeProperties(Collection<Property> properties)
	
	/**
	 * Serializes a single property
	 * @param property
	 * @return
	 */
	def serializeProperty(Property property)

	/**
	 * Serializes a set of units
	 * @param units
	 * @return
	 */
	def serializeUnits(Collection<Unit> units)

	/**
	 * Serializes a single unit
	 * @param unit
	 * @return
	 */
	def serializeUnit(Unit unit)
	
	/**
	 * Serializes a list of advisables to give advice on
	 * @param advisables
	 * @return
	 */
	def serializeEntityList(List<Advisable> advisables)

	/**
	 * Serializes a list of references
	 * @param references
	 * @return
	 */
	def serializeReferences(Map<Property,List<ReferenceValue>> references)

	/**
	 * Serializes a single reference
	 * @param references
	 * @return
	 */
	def serializeReference(ReferenceValue reference)

	/**
	 * Serializes a list of advices in English
	 * @param advices
	 * @return
	 */
	def serializeAdvices(List<Advice> advices)

	/**
	 * Serializes a list of advices
	 * @param advices
	 * @return
	 */
	def serializeAdvices(List<Advice> advices, String language)

	/**
	 * Serializes a list of advices with the given translated text
	 * @param advices
	 * @return
	 */
	def serializeAdvice(Advice advice, String translatedText)

}
