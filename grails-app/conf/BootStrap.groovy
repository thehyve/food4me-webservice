import eu.qualify.food4me.auth.Role
import eu.qualify.food4me.auth.User
import eu.qualify.food4me.auth.UserRole

class BootStrap {
	def grailsApplication
	
    def init = { servletContext ->
		// Ensure the the role_admin exists
		def adminAuthority = "ROLE_ADMIN"
		def role = Role.findByAuthority( adminAuthority ) 
		if( !role ) {
			role = new Role( authority: adminAuthority )
			role.save()
		}
		
		// Ensure there is an admin account
		if( !User.findByUsername( "admin" ) ) {
			def password = grailsApplication.config.food4me.adminPassword
			
			def user = new User( username: "admin", password: password )
			user.save()
			
			UserRole.create( user, role )
		}
    }
		
	def destroy = {
    }
}
