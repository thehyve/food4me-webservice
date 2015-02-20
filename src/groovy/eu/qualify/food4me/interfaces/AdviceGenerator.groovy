package eu.qualify.food4me.interfaces

import eu.qualify.food4me.Advice
import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements

interface AdviceGenerator {
	/**
	 * Generates a personalized advice for the given advisables, based on the measurements on the person
	 * @param measurements
	 * @param measurementStatus
	 * @param advisables
	 * @return
	 */
	List<Advice> generateAdvice(Measurements measurements, MeasurementStatus measurementStatus, List<Advisable> advisables)
}
