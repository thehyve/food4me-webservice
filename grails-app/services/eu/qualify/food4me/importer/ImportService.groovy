package eu.qualify.food4me.importer

import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import eu.qualify.food4me.measurements.Status
import eu.qualify.food4me.reference.ReferenceCondition
import eu.qualify.food4me.reference.ReferenceValue
import grails.transaction.Transactional

@Transactional
class ImportService {
	static transactional = false
	
	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	def grailsApplication
	
	int batchSize = 50
	String separatorChar = "\t"
	
	def loadAll( String directory = null ) {
		loadUnits( directory )
		loadProperties( directory )
	}
	
	/**
	 * Loads units into the database from the file units*.txt in the given directory
	 * @return
	 */
    def loadUnits( String directory = null ) {
		log.info "Start loading units " + ( directory ? " from " + directory : "" )
		
		importData( directory, ~/units.*\.txt/, { file ->
			def units = []
			
			log.info( "Loading units from " + file )
			 
			file.toCsvReader([skipLines: 1, separatorChar: separatorChar]).eachLine { line ->
				if( !line || line.size() < 3 ) {
					log.warn "Skipping line as it has not enough columns: " + line?.size()
					return
				}
				
				if( !line[0] ) {
					log.trace "Skipping empty line"
					return
				}
				
				// Check if a unit with this externalId already exists
				if( Unit.countByExternalId( line[2] ) == 0 ) {
			    	units << new Unit( name: line[0], externalId: line[2], code: line[1] )
					log.trace( "Importing unit " + line[0] + " / " + line[2] )
				} else {
					log.info( "Skip importing unit " + line[0] + " / " + line[2] + " because it already exists" )
				}
					 
				if( units.size() >= batchSize ) {
					saveBatch( Unit, units )
					units.clear()
				}
			}
			
			// Save all remaining units
			saveBatch( Unit, units )
		})
    }
	
	/**
	 * Loads properties into the database from the file properties*.txt in the given directory
	 * @return
	 */
	def loadProperties( String directory = null ) {
		log.info "Start loading properties " + ( directory ? " from " + directory : "" )
		
		importData( directory, ~/properties.*\.txt/, { file ->
			def properties = []
			
			log.info( "Loading properties from " + file )
			 
			file.toCsvReader([skipLines: 1, separatorChar: separatorChar]).eachLine { line ->
				if( !line || line.size() < 3 ) {
					log.warn "Skipping line as it has not enough columns: " + line?.size()
					return
				}
				
				if( !line[0] ) {
					log.trace "Skipping empty line"
					return
				}
				
				// If no external ID is given, the property cannot be added
				def externalId = line[2]
				if( !externalId ) {
					log.warn "Skipping line with property " + line[0] + " as it has no external identifier"
					return
				}
				
				// Check if a property with this externalId already exists
				if( Property.countByExternalId( externalId ) == 0 ) {
					// Find the unit to use for this property
					def unitCode = line.size() >= 4 ? line[3] : null
					def unit
					
					if( unitCode ) {
						unit = Unit.findByCode( unitCode )
						if( !unit )
							log.warn "Unit " + unitCode + " for property " + line[0] + " can not be found. Consider importing units first."
					}
					
					properties << new Property( entity: line[0], propertyGroup: line[1], externalId: externalId, unit: unit )
					log.trace( "Importing property " + line[0] + " / " + line[1] )
				} else {
					log.info( "Skip importing property " + line[0] + " (" + externalId + ") because it already exists" )
				}
					 
				if( properties.size() >= batchSize ) {
					saveBatch( Property, properties )
					properties.clear()
				}
			}
			
			// Save all remaining units
			saveBatch( Property, properties )
		})
	}
	
	/**
	 * Loads the references into the database from the following files:
	 * 		references_generic*.txt
	 * 		references_snps*.txt
	 * @return
	 */
	def loadReferences( String directory = null ) {
		loadGenericReferences( directory )
		loadSNPReferences( directory )
	}
	
	/**
	 * Loads generic references into the database from the file references_generic*.txt in the given directory
	 * @return
	 */
	def loadGenericReferences( String directory = null ) {
		log.info "Start loading generic references " + ( directory ? " from " + directory : "" )
		
		// Retrieve objects for age and gender, as they are needed for storing referneceValues
		def ageProperty = Property.findByEntity( "Age" ) 
		def genderProperty = Property.findByEntity( "Gender" )
		
		importData( directory, ~/references_generic.*\.txt/, { file ->
			def references = []
			
			log.info( "Loading generic references from " + file )
						 
			// The first 2 lines 
			file.toCsvReader([skipLines: 2, separatorChar: separatorChar]).eachLine { line ->
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
					age = null
				}
				if( gender && !genderProperty ) {
					log.error "Trying to add a reference condition on age for property " + property + " but the age property doesn't exist"
					gender = null
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
					def higherBoundary = line.size() > currentColumnIndex + 1 ? line[ currentColumnIndex + 1 ] : null
					
					def reference = new ReferenceValue(subject: property, status: status, color: Status.Color.fromString( color ) )
					
					if( age ) {
						reference.addToConditions( new ReferenceCondition( subject: ageProperty, low: age[0], high: age[1] ) )
					}

					if( gender ) {
						reference.addToConditions( new ReferenceCondition( subject: genderProperty, value: gender ) )
					}
					
					// Add the condition on the property itself
					reference.addToConditions( new ReferenceCondition( subject: property, low: currentLowerBoundary, high: higherBoundary ) )

					log.trace( "Importing reference for " + property + " / " + status + " with " + reference.conditions?.size() + " conditions"  )
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
		})
	}
	
	/**
	 * Loads SNP references into the database from the file references_snps*.txt in the given directory
	 * @return
	 */
	def loadSNPReferences( String directory = null ) {
		log.info "Start loading SNP references " + ( directory ? " from " + directory : "" )
		
		importData( directory, ~/references_snps.*\.txt/, { file ->
			def references = []
			
			log.info( "Loading SNP references from " + file )
						 
			// The first 2 lines
			def lineNo = 1
			def columnStatus = [:]
			file.toCsvReader([separatorChar: separatorChar]).eachLine { line ->
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
						
						// Color is not relevant for SNPs
						def color = ( status == "Risk allele" ) ? Status.Color.RED : Status.Color.GREEN
						
						log.info "Storing SNP " + line[0] + " / " + line[ columnNo ] + " as " + status
						
						def reference = new ReferenceValue(subject: snp, status: status, color: color )
						reference.addToConditions( new ReferenceCondition( subject: snp, value: line[ columnNo ] ) )
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
		})
	}
	
	protected def importData( String directory = null, def matcher, Closure fileHandler ) {
		if( !directory ) { 
			directory = grailsApplication.config.food4me.importDirectory

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
	
	protected def saveBatch( def domainClass, def objects ) {
		def numSaves = 0;
		
		if( !objects ) {
			log.warn "No objects of type " + domainClass + " to store"
			return
		} 
		
		log.info "Batch saving " + objects.size() + " objects of type " + domainClass 
		
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
	 * Cleaning up of GORM session caches, which are a performance killer if not flushed regularly
	 * @return
	 */
	protected def cleanUpGORM() {
		def session = sessionFactory.currentSession
		session?.flush()
		session?.clear()
		propertyInstanceMap.get().clear()
	}
}
