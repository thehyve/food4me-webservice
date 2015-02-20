package eu.qualify.food4me.algorithm.SOP3

import java.util.List;

import eu.qualify.food4me.interfaces.Advisable;
import eu.qualify.food4me.interfaces.AdvisableDeterminer
import eu.qualify.food4me.measurements.MeasurementStatus;
import eu.qualify.food4me.measurements.Measurements;
import grails.transaction.Transactional

@Transactional
class DetermineAdvisableService implements AdvisableDeterminer {

	@Override
	public List<Advisable> determineAdvisables(Measurements measurements,
			MeasurementStatus measurementStatus) {
		// TODO Auto-generated method stub
		return null;
	}
}
