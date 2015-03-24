package eu.qualify.food4me.output

import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.interfaces.Advisable
import eu.qualify.food4me.interfaces.Serializer
import eu.qualify.food4me.measurements.MeasurementStatus

class StructuredSerializationService implements Serializer {

	@Override
	public String serializeStatus(MeasurementStatus status) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String serializeEntityList(List<Advisable> advisables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List serializeAdvices(List<Advice> advices, String language = "en") {
		if( !advices ) {
			return []
		}
		
		// Find the texts for the advices given. Create a map of the texts
		// with the code being the key
		def texts = AdviceText.getTranslations( advices, language )
		
		// Combine the advices with texts and create a structure to serialize
		def output = advices.collect { advice ->
			[
				code: advice.code,
				subject: advice.subject.toString(),
				text: texts[ advice.code ]
			]
		}
		
		output
	}

}
