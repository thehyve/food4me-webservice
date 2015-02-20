package eu.qualify.food4me.measurements

import eu.qualify.food4me.Property
import groovy.transform.Canonical


@Canonical
class Measurement {
	Property property
	MeasuredValue value
}
