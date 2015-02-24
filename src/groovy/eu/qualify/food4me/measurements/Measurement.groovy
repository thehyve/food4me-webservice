package eu.qualify.food4me.measurements

import eu.qualify.food4me.interfaces.Measurable
import groovy.transform.Canonical


@Canonical
class Measurement {
	Measurable property
	MeasuredValue value
}
