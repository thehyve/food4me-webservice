package eu.qualify.food4me.interfaces

import eu.qualify.food4me.measurements.MeasurementStatus
import eu.qualify.food4me.measurements.Measurements

interface StatusComputer {
	/**
	 * Computes the status for a set of measurements (i.e. high, low etc.)
	 * @param measurements
	 * @return
	 */
	MeasurementStatus computeStatus(Measurements measurements)
}
