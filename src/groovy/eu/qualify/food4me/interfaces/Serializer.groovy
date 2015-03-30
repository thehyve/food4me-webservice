package eu.qualify.food4me.interfaces

import java.util.List;

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
	def serializeReferences(List<ReferenceValue> references)
	
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

}
