package eu.qualify.food4me

class LinesTagLib {
    static defaultEncodeAs = [taglib:'none']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]
	
	def lines = { attrs, body ->
		out << attrs['string'].encodeAsHTML().replace('\n', '<br/>\n')
	}
}
