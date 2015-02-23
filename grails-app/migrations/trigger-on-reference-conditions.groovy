databaseChangeLog = {
	changeSet(author: "robert", id: "RH-1424689802600-1") {
		sqlFile(path:"trigger-on-reference-conditions.sql", splitStatements:false) {}
	}
}