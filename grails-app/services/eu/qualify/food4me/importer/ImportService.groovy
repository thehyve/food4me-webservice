package eu.qualify.food4me.importer

import eu.qualify.food4me.Property
import eu.qualify.food4me.Unit
import grails.transaction.Transactional

@Transactional
class ImportService {
	static transactional = false
	
	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	
	int batchSize = 100
	
    def loadUnits() {
		log.info "Start loading units"
		
		importData( ~/units\.txt/, { file ->
			def units = []
			
			log.info( "Loading units from " + file )
			 
			file.toCsvReader([skipLines: 1, separatorChar: "\t"]).eachLine { line ->
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
	
	
	def loadProperties() {
		log.info "Start loading properties"
		
		importData( ~/properties\.txt/, { file ->
			def properties = []
			
			log.info( "Loading properties from " + file )
			 
			file.toCsvReader([skipLines: 1, separatorChar: "\t"]).eachLine { line ->
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
	
	protected def importData( def matcher, Closure fileHandler ) {
		def baseDir = new File("/home/robert/tmp/food4me")
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
