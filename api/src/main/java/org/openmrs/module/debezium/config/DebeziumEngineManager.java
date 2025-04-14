package org.openmrs.module.debezium.config;

import io.debezium.engine.ChangeEvent;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.connect.source.SourceRecord;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.entity.DatabaseEvent;
import org.openmrs.module.debezium.listener.DbChangeToEventFunction;
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;
import org.openmrs.module.debezium.service.DebeziumEventService;
import org.openmrs.module.debezium.utils.CustomFileOffsetBackingStore;
import org.openmrs.module.debezium.utils.DebeziumConstants;
import org.openmrs.module.debezium.utils.TableToWatch;
import org.openmrs.module.debezium.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.openmrs.module.debezium.utils.DebeziumConstants.GP_ENABLED;

final class DebeziumEngineManager {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumEngineManager.class);
	
	private static OpenmrsDebeziumEngine engine;
	
	/**
	 * Starts the OpenMRS debezium engine, this method will stop the engine if it's already running
	 * before restarting it.
	 */
	protected synchronized static void start() {
		
		synchronized (DebeziumEngineManager.class) {
			if (engine != null) {
				log.info("OpenMRS debezium engine is already running");
				return;
			}
			
			AdministrationService adminService = Context.getAdministrationService();
			if (!Boolean.valueOf(adminService.getGlobalProperty(GP_ENABLED))) {
				log.info("Not starting debezium because it is disabled via the global property named: " + GP_ENABLED);
				return;
			}
			
			Set<String> tablesToInclude = TableToWatch.getTablesToInclude();
			BaseDebeziumConfig config = new MySqlDebeziumConfig(MySqlSnapshotMode.SCHEMA_ONLY, tablesToInclude,
			        new HashSet<>());
			
			Long serverId = Long.valueOf(adminService.getGlobalProperty(DebeziumConstants.GP_DB_SERVER_ID.trim()));
			
			config.setServerId(serverId);
			config.setUsername(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME));
			config.setPassword(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_PASSWORD));
			
			String[] creds = Utils.getConnectionDetails();
			config.setHost(creds[0]);
			config.setPort(Integer.valueOf(creds[1]));
			config.setDatabaseName(creds[2]);
			config.setOffsetStorageFilename(adminService.getGlobalProperty(DebeziumConstants.GP_OFFSET_STORAGE_FILE));
			config.setAdditionalConfigProperties();
			engine = OpenmrsDebeziumEngine.getInstance();
			
			Consumer<ChangeEvent<SourceRecord, SourceRecord>> eventConsumer = event -> {
				Function<ChangeEvent<SourceRecord, SourceRecord>, DatabaseEvent> function = new DbChangeToEventFunction();
				
				String applicationName = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME);
				
				if (applicationName != null && !applicationName.isEmpty()) {
					DatabaseEvent dbEvent = function.apply(event);
					log.debug("Processing event {}", event);
					DebeziumEventService debeziumService = Context.getService(DebeziumEventService.class);
					debeziumService.createDebeziumEvent(Utils.convertDataBaseEvent(dbEvent));
				}
			};
			
			config.setConsumer(eventConsumer);
			
			//TODO Support postgres
			if (config.getSnapshotMode() == MySqlSnapshotMode.INITIAL
			        || config.getSnapshotMode() == MySqlSnapshotMode.INITIAL_ONLY) {
				
				File offsetFile = new File(config.getOffsetStorageFilename());
				if (offsetFile.exists()) {
					String bkpFilename = offsetFile.getName() + "." + Utils.getCurrentTimestamp();
					File bkpOffsetFile = FileUtils.getFile(offsetFile.getParentFile(), bkpFilename);
					
					log.info("Backing up existing offset file at -> " + bkpOffsetFile.getAbsolutePath());
					
					try {
						FileUtils.writeByteArrayToFile(bkpOffsetFile, FileUtils.readFileToByteArray(offsetFile));
						
						log.info("Deleting existing offset file -> " + offsetFile.getAbsolutePath());
						
						FileUtils.forceDelete(offsetFile);
					}
					catch (IOException e) {
						throw new APIException("An error occurred", e);
					}
				}
			}
			
			CustomFileOffsetBackingStore.reset();
			
			//TODO support postgres i.e. add a GP to specify the connector class
			
			log.info("Starting OpenMRS debezium engine");
			
			engine.start(config);
		}
		
	}
	
	/**
	 * Stops the OpenMRS debezium engine asynchronously
	 */
	protected synchronized static void stopAsync() {
		doStop(false);
	}
	
	/**
	 * Stops the OpenMRS debezium engine and waits for it
	 */
	public synchronized static void stop() {
		doStop(true);
	}
	
	/**
	 * Stops the OpenMRS debezium engine
	 * 
	 * @param wait if set to true the current thread will block until the debezium engine has been
	 *            stopped otherwise it will not
	 */
	private synchronized static void doStop(boolean wait) {
		if (engine != null) {
			log.info("Received call to stop OpenMRS debezium engine");
			
			engine.stop(wait);
			engine = null;
		} else {
			log.info("No running OpenMRS debezium engine found for stopping");
		}
	}
	
}
