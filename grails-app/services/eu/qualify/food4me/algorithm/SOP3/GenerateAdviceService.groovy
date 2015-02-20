package eu.qualify.food4me.algorithm.SOP3

import java.util.List;

import eu.qualify.food4me.Advice;
import eu.qualify.food4me.interfaces.AdviceGenerator
import eu.qualify.food4me.interfaces.Advisable;
import eu.qualify.food4me.measurements.MeasurementStatus;
import eu.qualify.food4me.measurements.Measurements;
import grails.transaction.Transactional

@Transactional
class GenerateAdviceService implements AdviceGenerator {

	@Override
	public List<Advice> generateAdvice(Measurements measurements,
			MeasurementStatus measurementStatus, List<Advisable> advisables) {
		// TODO Auto-generated method stub
		return null;
	}
}
