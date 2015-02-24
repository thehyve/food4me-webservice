databaseChangeLog = {
	changeSet(author: "robert", id: "RH-1424706558800-1") {
		sqlFile(path:"trigger-on-advice-conditions.sql", splitStatements:false) {}
	}
}