package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable;

class MeasurementStatus {
	Map<Measurable,Status> status
	
	public MeasurementStatus() {
		status = [:]
	}
	
	public Status getStatus(Measurable e) {
		return status[e]
	}
	
	public void addStatus(Measurable e, Status s) {
		status[e] = s
	}
	
	/**
	 * Retrieves all statusses from this object
	 * @return
	 */
	public Collection<Status> getAll() {
		return status.values()
	}
}
