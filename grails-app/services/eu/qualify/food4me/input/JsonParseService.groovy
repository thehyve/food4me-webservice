package eu.qualify.food4me.input

import java.util.List;

import eu.qualify.food4me.interfaces.Advisable;
import eu.qualify.food4me.interfaces.Parser;
import eu.qualify.food4me.measurements.MeasurementStatus;
import eu.qualify.food4me.measurements.Measurements;
import grails.transaction.Transactional

@Transactional
class JsonParseService implements Parser {

	@Override
	public Measurements parseMeasurements(Object params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MeasurementStatus parseStatus(Object params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Advisable> parseEntityList(Object params) {
		// TODO Auto-generated method stub
		return null;
	}

}
