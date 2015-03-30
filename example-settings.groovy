dataSource {
	url = "jdbc:postgresql://localhost:5432/food4me"
	driverClassName = "org.postgresql.Driver"
	dialect = org.hibernate.dialect.PostgreSQLDialect
	username = "username"
	password = "password"
	pooled = false
}

log4j.external = {
	println "Extending log4j configuration"
	// Extra log4j configuration, if required
}

food4me {
	// Directory to import the data from
	importDirectory = "/tmp/input-food4me"
	
	// Administrator password when first starting the application
	// See Bootstrap.groovy
	adminPassword = 'secret'
}
