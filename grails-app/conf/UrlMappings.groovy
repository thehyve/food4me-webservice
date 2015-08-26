class UrlMappings {
	static mappings = {
		name translatedAdvices: "/$language/advices(.$format)?" { 
			controller = "food4me"
			action = "advices"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		
		name translatedAdvice: "/$language/advices/$id(.$format)?" {
			controller = "food4me"
			action = "advice"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		
		"/status(.$format)?"( controller: "food4me", action: "status" )
		
		// Reference URLs
		"/references/$id(.$format)?"( controller: "food4me", action: "reference" )
		"/references(.$format)?"( controller: "food4me", action: "references" )
		
		// Property URLs
		"/properties/$id(.$format)?"(controller: "food4me", action: "property")
		"/properties(.$format)?"( controller: "food4me", action: "properties" )

		// Unit URLs
		"/units/$id(.$format)?"(controller: "food4me", action: "unit")
		"/units(.$format)?"( controller: "food4me", action: "units" )
		
		// Advice URLs
		"/advices/$id(.$format)?"( controller: "food4me", action: "advice" )
		"/advices(.$format)?"( controller: "food4me", action: "advices" )
		
		"/form"( controller: "food4me", action: "form" )
		"/$language/form" { 
			controller = "food4me"
			action = "form"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		"/$language" { 
			controller = "food4me"
			action = "form"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller:"food4me", action: "form")
        "500"(view:'/error')
	}
}
