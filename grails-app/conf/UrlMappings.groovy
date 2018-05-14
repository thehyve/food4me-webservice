class UrlMappings {
	static mappings = {
		name translatedAdvices: "/$language/advices" {
			controller = "food4me"
			action = "advices"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		
		name translatedAdvice: "/$language/advices/$id" {
			controller = "food4me"
			action = "advice"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		
		"/status"( controller: "food4me", action: "status" )
		
		// Reference URLs
		"/references/$id"( controller: "food4me", action: "reference" )
		"/references"( controller: "food4me", action: "references" )
		
		// Property URLs
		"/properties/$id"(controller: "food4me", action: "property")
		"/properties"( controller: "food4me", action: "properties" )

		// Unit URLs
		"/units/$id"(controller: "food4me", action: "unit")
		"/units"( controller: "food4me", action: "units" )
		
		// Advice URLs
		"/advices/$id"( controller: "food4me", action: "advice" )
		"/advices"( controller: "food4me", action: "advices" )
		
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
		
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller:"food4me", action: "form")
        "500"(view:'/error')
	}
}
