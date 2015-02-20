package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable;

class MeasurementStatus {
	Map<Measurable,Status> status
	
	public MeasurementStatus() {
		status = [:]
	}
	
	public getStatus(Measurable e) {
		return status[e]
	}
	
	public addStatus(Measurable e, Status s) {
		status[e] = s
	}
}
