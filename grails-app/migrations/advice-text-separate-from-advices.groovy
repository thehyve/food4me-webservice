/*
 *  Copyright (C) 2015 The Hyve
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
databaseChangeLog = {

	changeSet(author: "robert (generated)", id: "1426015404449-1") {
		dropForeignKeyConstraint(baseTableName: "advice_text", baseTableSchemaName: "public", constraintName: "FK_nifwnyxkbetnn485dy8x54cs0")
	}

	changeSet(author: "robert (generated)", id: "1426015404449-2") {
		createIndex(indexName: "Code_index", tableName: "advice_text") {
			column(name: "code")
		}
	}

	changeSet(author: "robert (generated)", id: "1426015404449-3") {
		dropColumn(columnName: "text", tableName: "advice")
	}

	changeSet(author: "robert (generated)", id: "1426015404449-4") {
		dropColumn(columnName: "advice_id", tableName: "advice_text")
	}
}
