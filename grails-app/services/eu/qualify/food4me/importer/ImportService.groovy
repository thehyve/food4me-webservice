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
package eu.qualify.food4me.importer

import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.decisiontree.Advice
import eu.qualify.food4me.decisiontree.AdviceCondition
import eu.qualify.food4me.decisiontree.AdviceText
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue
import groovy.sql.Sql


class ImportService {
	static transactional = false
	
	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	def grailsApplication
	def dataSource
	
	int batchSize = 50
	String separatorChar = "\t"
	
	def loadAll( String directory = null ) {
		loadUnitsFromDirectory( directory )
		loadPropertiesFromDirectory( directory )
		loadReferencesFromDirectory( directory )
		loadDecisionTreesFromDirectory( directory )
		loadAdviceTextsFromDirectory( directory )
	}
	
	/**
	 * Loads units from a CSV inputstream
	 * @param inputStream
	 * @return
	 */
	def loadUnits( InputStream inputStream ) {
		def units = []
		def alreadyImportedIds = []
		
		inputStream.toCsvReader([skipLines: 1, separatorChar: separatorChar]).eachLine { line ->
			if( !line || line.size() < 3 ) {
				log.warn "Skipping line as it has not enough columns: " + line?.size()
				return
			}
			
			if( !line[0] ) {
				log.trace "Skipping empty line"
				return
			}
			
			// Check if a unit with this externalId already exists
			def externalId = line[2] ?: line[3]
			if( Unit.countByExternalId( externalId ) > 0 || externalId in alreadyImportedIds) {
				log.info( "Skip importing unit " + line[0] + " / " + externalId + " because it already exists" )
			} else {
				units << new Unit( name: line[0], externalId: externalId, code: line[1] )
				log.trace( "Importing unit " + line[0] + " / " + externalId )
				alreadyImportedIds << externalId
			}
				 
			if( units.size() >= batchSize ) {
				saveBatch( Unit, units )
				units.clear()
			}
		}
		
		// Save all remaining units
		saveBatch( Unit, units )

	}
	
	/**
	 * Loads properties into the database from a CSV inputstream
	 * @return
	 */
	def loadProperties( InputStream inputStream ) {
		def properties = []
		def alreadyImportedIds = []
		
		inputStream.toCsvReader([skipLines: 1, separatorChar: separatorChar]).eachLine { line ->
			if( !line || line.size() < 3 ) {
				log.warn "Skipping line as it has not enough columns: " + line?.size()
				return
			}
			
			if( !line[0] ) {
				log.trace "Skipping empty line"
				return
			}
			
			// If no external ID is given, the property cannot be added
			// We use the snomedct id initially, but if it is not given, we use the EuroFIR code
			def externalId = line[2] ?: line[3]
			if( !externalId ) {
				log.warn "Skipping line with property " + line[0] + " as it has no external identifier"
				return
			}
			
			// Check if a property with this externalId already exists
			if( Property.countByExternalId( externalId ) > 0 || externalId in alreadyImportedIds ) {
				log.info( "Skip importing property " + line[0] + " (" + externalId + ") because it already exists" )
			} else {
				// Find the unit to use for this property
				def unitCode = line.size() >= 5 ? line[4] : null
				def unit
				
				if( unitCode ) {
					unit = Unit.findByCode( unitCode )
					if( !unit ) {
						log.warn "Unit " + unitCode + " for property " + line[0] + " can not be found. Consider importing units first."
						return;
					}
				}
				
				properties << new Property( entity: line[0], propertyGroup: line[1], externalId: externalId, unit: unit )
				alreadyImportedIds << externalId
				log.trace( "Importing property " + line[0] + " / " + line[1] + " with external ID " + externalId )
			}
				 
			if( properties.size() >= batchSize ) {
				saveBatch( Property, properties )
				properties.clear()
			}
		}
		
		// Save all remaining units
		saveBatch( Property, properties )
	}
	
	/**
	 * Loads generic references into the database from a CSV inputstream
	 * @return
	 */
	def loadGenericReferences( InputStream inputStream ) {
		// Retrieve objects for age and gender, as they are needed for storing referenceValues
		def ageProperty = Property.findByEntity( "Age" )
		def genderProperty = Property.findByEntity( "Gender" )
		
		def references = []
		
		// The first 2 lines contain the headers
		inputStream.toCsvReader([skipLines: 2, separatorChar: separatorChar]).eachLine { line ->
			if( !line || line.size() < 5 ) {
				log.warn "Skipping line as it has not enough columns: " + line?.size()
				return
			}
			
			if( !line[0] ) {
				log.trace "Skipping empty line"
				return
			}
			
			// Find the property that this reference refers to
			def property = Property.findByEntityAndPropertyGroup( line[0], line[1] )
			
			if( !property ) {
				log.warn "Cannot find entity " + line[0] + " / " + line[1] + " when importing reference. Skipping this line"
				return
			}
			
			// Check whether an age and/or gender are given (in columns 4, 5, 6)
			// TODO generalize this method to support more and other conditions
			def age = null
			def gender = null
			if( line[3] || line[4] ) {
				age = [ line[3] ?: null, line[4] ?: null ]
			}
			gender = line[5] ?: null
			
			// Check whether we have the properties for the requested conditions
			if( age && !ageProperty ) {
				log.error "Trying to add a reference condition on age for property " + property + " but the age property doesn't exist"
				return
			}
			if( gender && !genderProperty ) {
				log.error "Trying to add a reference condition on age for property " + property + " but the age property doesn't exist"
				return
			}
			
			// Loop through the possible references. Each reference has 2 columns:
			//		color and upper boundary. The upper boundary is also used as
			//		the lower boundary of the next
			def statusses = [ Status.STATUS_VERY_LOW, Status.STATUS_LOW, Status.STATUS_OK, Status.STATUS_HIGH, Status.STATUS_VERY_HIGH ]
			def currentLowerBoundary = null
			def currentColumnIndex = 6
			def color
			
			statusses.each { status ->
				// If no status color is given for this status, we skip this status
				if( line.size() <= currentColumnIndex || !line[ currentColumnIndex ] ) {
					log.trace "Status " + status + " not found for property " + property
					currentColumnIndex += 2
					return
				}
				
				// TODO: check for duplicates
				color = line[ currentColumnIndex ]
				def higherBoundary = ( line.size() > currentColumnIndex + 1 && line[ currentColumnIndex + 1 ].isBigDecimal() ) ? line[ currentColumnIndex + 1 ].toBigDecimal() : null
				
				def reference = new ReferenceValue(subject: property, status: status, color: Status.Color.fromString( color ) )
				
				if( age ) {
					reference.addToConditions( new ReferenceCondition( subject: ageProperty, low: age[0], high: age[1], conditionType: ReferenceCondition.TYPE_NUMERIC ) )
				}

				if( gender ) {
					reference.addToConditions( new ReferenceCondition( subject: genderProperty, value: gender, conditionType: ReferenceCondition.TYPE_TEXT ) )
				}
				
				// Add the condition on the property itself
				reference.addToConditions( new ReferenceCondition( subject: property, low: currentLowerBoundary, high: higherBoundary, conditionType: ReferenceCondition.TYPE_NUMERIC ) )

				log.trace( "Importing reference for " + property + " / " + status + " with " + reference.conditions?.size() + " conditions " + currentLowerBoundary + " / " + higherBoundary  )
				references << reference
				
				// Prepare for next iteration
				currentLowerBoundary = higherBoundary
				currentColumnIndex += 2
			}
			
			if( references.size() >= batchSize ) {
				saveBatch( ReferenceValue, references )
				references.clear()
			}
		}
		
		// Save all remaining references
		saveBatch( ReferenceValue, references )
	}
	
	/**
	 * Loads SNP references into the database from a CSV inputstream
	 * @return
	 */
	def loadSNPReferences( InputStream inputStream ) {
		def references = []
		
		// The first 2 lines
		def lineNo = 1
		def columnStatus = [:]
		inputStream.toCsvReader([separatorChar: separatorChar]).eachLine { line ->
			if( !line || line.size() < 2 ) {
				log.warn "Skipping line as it has not enough columns: " + line?.size()
				return
			}
			
			// Parse the header line, to see where the risk-alleles and non-risk alleles are
			if( lineNo++ == 1 ) {
				def columnNo = 1
				def currentStatus = ""
				while( columnNo < line.size() ) {
					if( line[ columnNo ] )
						currentStatus = line[ columnNo ]
					
					columnStatus[columnNo] = currentStatus
					columnNo++
				}
				
				lineNo++
				return
			}
			
			if( !line[0] ) {
				log.trace "Skipping empty line"
				return
			}
			
			// Find the SNP that this reference refers to
			def snp = Property.findByEntityAndPropertyGroup( line[0], Property.PROPERTY_GROUP_SNP )
			
			if( !snp ) {
				log.warn "Cannot find SNP " + line[0] + " when importing reference. Skipping this line"
				return
			}
			
			// Loop through all columns, and store a reference for the allele
			def columnNo = 1
			while( columnNo < line.size() ) {
				if( line[ columnNo ] ) {
					def status = columnStatus[columnNo]
					
					// Color is not relevant for SNPs, but we store a color anyhow
					def color = ( status == "Risk allele" ) ? Status.Color.RED : Status.Color.GREEN
					
					log.trace "Storing SNP " + line[0] + " / " + line[ columnNo ] + " as " + status
					
					def reference = new ReferenceValue(subject: snp, status: status, color: color )
					reference.addToConditions( new ReferenceCondition( subject: snp, value: line[ columnNo ], conditionType: ReferenceCondition.TYPE_TEXT ) )
					references << reference
				}
				
				columnNo++
			}
			
			if( references.size() >= batchSize ) {
				saveBatch( ReferenceValue, references )
				references.clear()
			}
		}
		
		// Save all remaining references
		saveBatch( ReferenceValue, references )
	}

	/**
	 * Loads decision trees from a CSV inputstream
	 * 
	 * Format: 	Cel A1 contains the property that this decision tree is about
	 * 			Row 1 (from column B) contains the properties to decide on
	 *			Row 2 (from column B) contains optional modifiers on the properties
	 *			Row 3 (from column B) contains either Status or Value
	 * The lines below that contain the advices, the first column contains the advice code, 
	 * the other columns contain the value or status of the given variable
	 *
	 * @param inputStream
	 * @return
	 */
	def loadDecisionTrees( InputStream inputStream ) {
		def adviceObjects = []
		
		def adviceSubject
		def conditionSubjects = []
		
		def lineNo = 1
		def headerLines = []
		def structure
		
		inputStream?.toCsvReader([skipLines: 0, separatorChar: separatorChar]).eachLine { line ->
			if( !line || line.size() < 2 ) {
				log.warn "Skipping line as it has not enough columns: " + line?.size()
				return
			}
			
			// Combine the first three header lines to be parsed separately
			if( lineNo++ < 4 ) {
				headerLines << line
				return;
			}
			
			// If we reach this point, we should first parse the header lines
			if( headerLines ) {
				structure = parseDecisionTreeHeaderLines( headerLines )
				headerLines = null
			}
			
			// If the header lines could not be properly parsed, there is no need
			// to continue, as we can't store anything
			if( !structure ) {
				return
			}
			
			if( !line[0] ) {
				log.trace "Skipping empty line"
				return
			}
			
			// As the status in the file could be 'below OK' or 'above OK',
			// it should be translated into Low and Very Low and the same for above OK.
			// That means, multiple advices could be generated for each line.
			//
			// To handle that, we create a list with a list of options for each column. That means
			// if we start with a line like [advice1, Below OK, High, Above OK], it would result in
			// a list as follows: [ [Very Low, Low], [High], [Above OK] ]. We will later on create
			// combinations of these conditions to end up with all required records in the database.
			//
			// Please note, in order to properly generate the combinations, we will insert a list with 
			// NULL value for each empty cell. These will be discarded when generating the domain objects
			// itself.
			log.trace "Generate advice combinations for advice " + structure.adviceSubject + " / " + line[0]
			
			def conditions = []
			
			def columnNo = 1
			def translationMap = [
				"below ok": [ Status.STATUS_VERY_LOW, Status.STATUS_LOW ],
				"above ok": [ Status.STATUS_VERY_HIGH, Status.STATUS_HIGH ],
				"ok and lower": [ Status.STATUS_OK, Status.STATUS_LOW, Status.STATUS_VERY_LOW ],
				"ok and higher": [ Status.STATUS_OK, Status.STATUS_HIGH, Status.STATUS_VERY_HIGH ],
			]
			
			while( columnNo < line.size() ) {
				if( line[columnNo] ) {
					def currentValue = line[columnNo]?.trim()
					
					// Check if we need to translate this property into multiple statusses
					// That is, if the column is set to filter on status and we have a translation for this status
					if( structure.conditionSubjects[columnNo].filterOnStatus && translationMap.containsKey( currentValue.toLowerCase() ) ) {
						conditions << translationMap[currentValue.toLowerCase()]
					} else {
						conditions << [currentValue]
					}
				} else {
					// Add a dummy value for this condition, in order to make the combinations
					// method work properly later on 
					conditions << [null]
				}
				
				columnNo++
			}
						
			if( !conditions || !conditions.findAll() ) {
				log.warn "No conditions are found for advice with code " + line[0]
			}
			// Generate combinations of all conditions
			def adviceConditions = conditions.combinations()
			
			// Generate objects for all advices
			adviceConditions.each { conditionSet ->
				def advice = new Advice( code: toAdviceCode(line[0]), subject: structure.adviceSubject ) 
				
				log.trace "  Generating advice with conditions + " + conditionSet
				
				conditionSet.eachWithIndex { conditionValue, index ->
					if( conditionValue ) {
						// Retrieve the parameters for this column
						def conditionParams = structure.conditionSubjects[index+1]
						
						def condition = new AdviceCondition( subject: conditionParams.property, modifier: conditionParams.modifier )
						if( conditionParams.filterOnStatus ) {
							condition.status = conditionValue
						} else {
							condition.value = conditionValue
						}
						
						advice.addToConditions condition
					}
				}
				
				adviceObjects << advice
			}
			
			if( adviceObjects.size() >= batchSize ) {
				saveBatch( Advice, adviceObjects )
				adviceObjects.clear()
			}

		}
		
		// Save all remaining objects
		saveBatch( Advice, adviceObjects )
	}
	
	protected def parseDecisionTreeHeaderLines( def headerLines ) {
		def decisionTreeStructure = [
			adviceSubject: null,
			conditionSubjects: [:]
		]
		
		if( headerLines.size() != 3 ) {
			log.error "Invalid number of header lines for decision tree: " + headerLines.size() + " lines. Skipping import of this file"
			return null
		}
		
		if( headerLines[0].size() != headerLines[1].size() || headerLines[0].size() != headerLines[2].size() ) {
			log.error "Invalid format of header lines for decision tree: all header lines should be equal length. Sizes are: " + headerLines.collect { it.size() } + ". Skipping import of this file."
			return null
		}
		
		// The first cell contains the subject of the advice
		decisionTreeStructure.adviceSubject = Property.findByEntity( headerLines[0][0].trim() )
		
		if( !decisionTreeStructure.adviceSubject ) {
			log.warn "No property could be found for advice subject " + headerLines[0][0] + ". Skipping import of this file."
			return
		}

		// The rest of the columns contain the properties
		def columnNo = 1
		def conditionProperty
		while( columnNo < headerLines[0].size() ) {
			if( headerLines[0][columnNo] ) { 
				conditionProperty = Property.findByEntity( headerLines[0][columnNo].trim() )
				
				if( !conditionProperty ) {
					log.warn "Cannot find property for column ${columnNo}: " + headerLines[0][columnNo] + ". Skipping import of this file."
					return null
				}
				
				def filterOnValue = headerLines[2][columnNo]?.trim()?.toLowerCase() == "value"
				
				// Check whether we know any references for the given property. If not, the status should be given by the user
				if( !filterOnValue && ReferenceValue.countBySubject( conditionProperty ) == 0 ) {
					log.warn "Cannot find any references for " + headerLines[0][columnNo] + ". This means that we can't determine a status for this variable automatically."
				}
				
				decisionTreeStructure.conditionSubjects[ columnNo ] = [
					property: conditionProperty,
					modifier: headerLines[1][columnNo],
					filterOnValue: filterOnValue,
					filterOnStatus: !filterOnValue
				] 
			}
			
			columnNo++
		}
		
		decisionTreeStructure
	}
	
	/**
	 * Loads text for advices from a CSV inputstream. 
	 * 
	 * The file should NOT have a header line. The first column contains the code, 
	 * the second column contains the text to be imported
	 * @param inputStream
	 * @return
	 */
	def loadAdviceTexts( InputStream inputStream, String language = "en" ) {
		def objects = []
		
		inputStream.toCsvReader([skipLines: 0, separatorChar: separatorChar]).eachLine { line ->
			if( !line || line.size() < 2 ) {
				log.warn "Skipping line as it has not enough columns: " + line?.size()
				return
			}
			
			if( !line[0] ) {
				log.trace "Skipping empty line"
				return
			}
			
			// Check if a unit with this externalId already exists
			def adviceCode = line[0].trim()
			def translation = line[1]?.trim()
			
			if( !translation ) {
				log.warn "Skipping translation for code " + adviceCode + " as it is empty"
				return
			}
			
			// Check if the translation already exists. If so, overwrite
			def adviceText = AdviceText.findByCodeAndLanguage( adviceCode, language )
			if( adviceText ) {
				log.trace "Overwriting translation for " + adviceCode + " in " + language
				adviceText.text = line[1]
			} else {
				log.trace "Importing new for " + adviceCode + " in " + language
				adviceText = new AdviceText( code: toAdviceCode(adviceCode), language: language, text: line[1] )
			}
			
			objects << adviceText
			
			if( objects.size() >= batchSize ) {
				saveBatch( AdviceText, objects )
				objects.clear()
			}

		}
		
		// Save all remaining units
		saveBatch( AdviceText, objects )
	}
	
	/**
	 * Loads units into the database from the file units*.txt in the given directory
	 * @return
	 */
	def loadUnitsFromDirectory( String directory = null ) {
		log.info "Start loading units " + ( directory ? " from " + directory : "" )
		
		importData( directory, ~/units.*\.txt/, { file ->
			log.info( "Loading units from " + file )
			file.withInputStream { is -> loadUnits( is ) }
		})
	}
	
	/**
	 * Loads properties into the database from the file properties*.txt in the given directory
	 * @return
	 */
	def loadPropertiesFromDirectory( String directory = null ) {
		log.info "Start loading properties " + ( directory ? " from " + directory : "" )
		
		importData( directory, ~/properties.*\.txt/, { file ->
			log.info( "Loading properties from " + file )
			file.withInputStream { is -> loadProperties( is ) }
		})
	}
	
	/**
	 * Loads the references into the database from the following files:
	 * 		references_generic*.txt
	 * 		references_snps*.txt
	 * @return
	 */
	def loadReferencesFromDirectory( String directory = null ) {
		loadGenericReferencesFromDirectory( directory )
		loadSNPReferencesFromDirectory( directory )
	}
	
	/**
	 * Loads generic references into the database from the file references_generic*.txt in the given directory
	 * @return
	 */
	def loadGenericReferencesFromDirectory( String directory = null ) {
		log.info "Start loading generic references " + ( directory ? " from " + directory : "" )
		
		// First disable the trigger for advice conditions, as that slows down the import heavily
		log.info "Disabling trigger on reference_condition"
		disableTriggers "reference_condition"
		
		try {
			importData( directory, ~/references-generic.*\.txt/, { file ->
				log.info( "Loading generic references from " + file )
				file.withInputStream { is -> loadGenericReferences( is ) }
			})
		} finally {
			// Re enable the trigger for advice conditions
			log.info "Re enabling trigger on reference_condition"
			enableTriggers "reference_condition"
		}
		
	}
	
	/**
	 * Loads SNP references into the database from the file references_snps*.txt in the given directory
	 * @return
	 */
	def loadSNPReferencesFromDirectory( String directory = null ) {
		log.info "Start loading SNP references " + ( directory ? " from " + directory : "" )
		
		// First disable the trigger for advice conditions, as that slows down the import heavily
		log.info "Disabling trigger on reference_conditoin"
		disableTriggers "reference_condition"
		
		try {
			importData( directory, ~/references-snps.*\.txt/, { file ->
				log.info( "Loading SNP references from " + file )
				file.withInputStream { is -> loadSNPReferences(is) }
			})
		} finally {
			// Re enable the trigger for advice conditions
			log.info "Re enabling trigger on reference_condition"
			enableTriggers "reference_condition"
		}
	}

	/**
	 * Loads advice texts into the database from the files advice_texts*.[language].txt in the given directory
	 * @return
	 */
	def loadAdviceTextsFromDirectory( String directory = null ) {
		log.info "Start loading advice texts " + ( directory ? " from " + directory : "" )
		
		importData( directory, ~/advice-texts.*\.[a-zA-Z]+\.txt$/, { file ->
			def match = file.name =~ /([a-zA-Z]+)\.txt$/
			if( !match ) {
				log.warn( "Trying to load advice texts from " + file + " but no proper language was specified" )
				return
			}
			
			def language = match[0][1]
			
			log.info( "Loading advice texts from " + file + " in language " + language )
			file.withInputStream { is -> loadAdviceTexts(is, language) }
		})
	}

	/**
	 * Loads decision trees into the database from the files decision_trees*.txt in the given directory
	 * @return
	 */
	def loadDecisionTreesFromDirectory( String directory = null ) {
		log.info "Start loading decision trees " + ( directory ? " from " + directory : "" )
		
		// First disable the trigger for advice conditions, as that slows down the import heavily
		log.info "Disabling trigger on advice_condition"
		disableTriggers "advice_condition"
		
		try {
			importData( directory, ~/decision-trees.*\.txt/, { file ->
				log.info( "Loading decision trees from " + file )
				file.withInputStream { is -> loadDecisionTrees(is) }
			})
		} finally {
			// Re enable the trigger for advice conditions
			log.info "Re enabling trigger on advice_condition"
			enableTriggers "advice_condition"
		}
	

	}

	/**
	 * Returns the default import directory to import from
	 * @return
	 */
	public String getDefaultImportDirectory() {
		grailsApplication.config.food4me.importDirectory
	}
			
	/**
	 * Import data from files in the given directory
	 * @param matcher
	 * @param fileHandler
	 * @return
	 */
	protected def importData( String directory = null, def matcher, Closure fileHandler ) {
		if( !directory ) { 
			directory = getDefaultImportDirectory()

			if( !directory ) {
				log.error "No default directory given to import data from. Please specify the configuration value food4me.importDirectory to a readable directory."
				return
			} else {
				log.info "Importing data from default directory in configuration: " + directory
			}
		}
			
		def baseDir = new File( directory )
		
		if( !baseDir.exists() || !baseDir.isDirectory() || !baseDir.canRead() ) {
			log.error "Provided directory " + directory + " is not an existing readable directory. Please check your configuration."
			return
		}
		
		baseDir.eachFileMatch matcher, fileHandler
	}
	
	/**
	 * Store a set of objects and cleanup GORM afterwards
	 * @param domainClass
	 * @param objects
	 * @return
	 */
	protected def saveBatch( def domainClass, def objects ) {
		def numSaves = 0;
		
		if( !objects ) {
			log.warn "No objects of type " + domainClass?.simpleName + " to store"
			return
		} 
		
		log.info "Batch saving " + objects.size() + " objects of type " + domainClass?.simpleName 
		
		domainClass.withTransaction {
			objects.each { object ->
				if( !object.save() ) {
					log.error "Unable to save ${domainClass} object in batch: " + object
					object?.errors?.allErrors?.each { currentError ->
						log.error "Error occured on field [${currentError?.field}] - [${currentError?.defaultMessage}] for value [${currentError?.rejectedValue}]"
					}
				} else {
					numSaves++
				}
			}
		}
		
		cleanUpGORM()
		return numSaves
	}
	
	/**
	 * Prepares an advice code for storage in the database
	 * @param code
	 * @return
	 */
	protected String toAdviceCode(code) {
		code.replaceAll( /\./, "_" )
	}
	
	protected def disableTriggers( String table ) {
		final Sql sql = new Sql(dataSource)
		sql.execute "ALTER TABLE " + table + " DISABLE TRIGGER USER"
	}
	
	protected def enableTriggers( String table ) {
		final Sql sql = new Sql(dataSource)
		sql.execute "ALTER TABLE " + table + " ENABLE TRIGGER USER"
		
		// Update rows in the table without changing the data itself
		// This will execute the trigger
		sql.execute "UPDATE " + table + " set id = id"
	}
	
	/**
	 * Cleaning up of GORM session caches, which are a performance killer if not flushed regularly
	 * @return
	 */
	protected def cleanUpGORM() {
		def session = sessionFactory.currentSession
		
		if( !session )
			log.warn "No hibernate session could be retrieved"
			
		session?.flush()
		session?.clear()
		propertyInstanceMap.get().clear()
	}
}
