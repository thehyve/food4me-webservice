package eu.qualify.food4me.interfaces

import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.measurements.MeasurementStatus

/**
 * This interface determines methods to serialize data within 
 * the food4me webservices.
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
	String serializeStatus(MeasurementStatus status)

	/**
	 * Serializes a list of advisables to give advice on
	 * @param advisables
	 * @return
	 */
	String serializeEntityList(List<Advisable> advisables)

	/**
	 * Serializes a list of advices in English
	 * @param advices
	 * @return
	 */
	String serializeAdvices(List<Advice> advices)

	/**
	 * Serializes a list of advices
	 * @param advices
	 * @return
	 */
	String serializeAdvices(List<Advice> advices, String language)

}
