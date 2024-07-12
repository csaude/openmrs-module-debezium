package org.openmrs.module.debezium;

import static org.openmrs.api.context.Context.getRegisteredComponent;
import static org.openmrs.module.debezium.DebeziumConstants.ENGINE_CONFIG_BEAN_NAME;
import static org.openmrs.module.debezium.DebeziumConstants.GP_ENABLED;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			
			DebeziumEngineConfig engCfg = getRegisteredComponent(ENGINE_CONFIG_BEAN_NAME, DebeziumEngineConfig.class);
			BaseDebeziumConfig config = new MySqlDebeziumConfig((MySqlSnapshotMode) engCfg.getSnapshotMode(),
			        engCfg.getTablesToInclude(), engCfg.getTablesToExclude());
			Long serverId = Long.valueOf(adminService.getGlobalProperty(DebeziumConstants.GP_DB_SERVER_ID.trim()));
			config.setServerId(serverId);
			String userGp = adminService.getGlobalProperty(DebeziumConstants.GP_USER);
			if (StringUtils.isNotBlank(userGp)) {
				config.setUsername(userGp);
				config.setPassword(adminService.getGlobalProperty(DebeziumConstants.GP_PASSWORD));
			} else {
				config.setUsername(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME));
				config.setPassword(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_PASSWORD));
			}
			
			String[] creds = Utils.getConnectionDetails();
			config.setHost(creds[0]);
			config.setPort(Integer.valueOf(creds[1]));
			config.setDatabaseName(creds[2]);
			config.setOffsetStorageFilename(adminService.getGlobalProperty(DebeziumConstants.GP_OFFSET_STORAGE_FILE));
			config.setAdditionalConfigProperties();
			engine = OpenmrsDebeziumEngine.getInstance();
			config.setConsumer(new DebeziumChangeConsumer(engCfg.getEventListener()));
			
			//TODO Support postgres
			if (engCfg.getSnapshotMode() == MySqlSnapshotMode.INITIAL
			        || engCfg.getSnapshotMode() == MySqlSnapshotMode.INITIAL_ONLY) {
				
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
			engCfg.init();
			
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
