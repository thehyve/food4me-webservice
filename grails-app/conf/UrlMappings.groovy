class UrlMappings {
	static mappings = {
		name translatedAdvices: "/$language/advices(.$format)?" { 
			controller = "food4me"
			action = "advices"
			constraints {
				language inList: [ 'en', 'nl' ]
			}
		}
		"/advices(.$format)?"( controller: "food4me", action: "advices" )
		
		"/status(.$format)?"( controller: "food4me", action: "status" )
		"/references(.$format)?"( controller: "food4me", action: "references" )
		
		// Property URLs
		"/properties/$id(.$format)?"(controller: "food4me", action: "property")
		"/properties(.$format)?"( controller: "food4me", action: "properties" )

		// Unit URLs
		"/units/$id(.$format)?"(controller: "food4me", action: "unit")
		"/units(.$format)?"( controller: "food4me", action: "units" )
		
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
