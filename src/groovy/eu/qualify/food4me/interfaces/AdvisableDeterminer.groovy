package eu.qualify.food4me.interfaces

import java.util.List;

import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements

interface AdvisableDeterminer {
	/**
	 * Determines which advisables the system should give advice on
	 * @param measurements
	 * @param measurementStatus
	 * @return
	 */
	List<Advisable> determineAdvisables(Measurements measurements, MeasurementStatus measurementStatus)
}
