package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable

class Status {
	static final String STATUS_VERY_LOW = "Very low"
	static final String STATUS_LOW = "Low"
	static final String STATUS_OK = "OK"
	static final String STATUS_HIGH = "High"
	static final String STATUS_VERY_HIGH = "Very high"
	static final String STATUS_UNKNOWN = "Unknown"

	String status = STATUS_UNKNOWN
	MeasuredValue value
	Measurable entity
}
