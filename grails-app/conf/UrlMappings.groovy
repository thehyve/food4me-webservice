class UrlMappings {

	static mappings = {
		"/advices"( controller: "food4me", action: "advices" )
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
	}
}
