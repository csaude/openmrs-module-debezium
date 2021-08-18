package org.openmrs.module.debezium;

import static org.openmrs.api.context.Context.getRegisteredComponent;
import static org.openmrs.module.debezium.DebeziumConstants.DB_EVENT_LISTENER_BEAN_NAME;
import static org.openmrs.module.debezium.DebeziumConstants.GP_ENABLED;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
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
			stop();
			AdministrationService adminService = Context.getAdministrationService();
			//TODO Add GlobalPropertyListener in order to start/stop debezium on demand by the admin
			if (!Boolean.valueOf(adminService.getGlobalProperty(GP_ENABLED))) {
				log.info("Not starting debezium because it is disabled via the global property named: " + GP_ENABLED);
				return;
			}
			
			DatabaseEventListener listener = getRegisteredComponent(DB_EVENT_LISTENER_BEAN_NAME,
			    DatabaseEventListener.class);
			
			log.info("Starting OpenMRS debezium engine after context refresh");
			
			//TODO support postgres i.e. add a GP to specify the connector class
			String snapshotOnly = Utils.getSystemProperty(ConfigPropertyConstants.SYS_PROP_SNAPSHOT);
			BaseDebeziumConfig config = new MySqlDebeziumConfig(snapshotOnly != null, listener.getTablesToInclude(),
			        listener.getTablesToExclude());
			
			String userGp = adminService.getGlobalProperty(DebeziumConstants.GP_USER);
			if (StringUtils.isNotBlank(userGp)) {
				config.setUsername(userGp);
				config.setPassword(adminService.getGlobalProperty(DebeziumConstants.GP_PASSWORD));
			} else {
				config.setUsername(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME));
				config.setPassword(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_PASSWORD));
			}
			
			String jdbcUrl = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_URL);
			String host, portStr, dbName, hostPortDbNameStr;
			if (jdbcUrl.indexOf("?") > -1) {
				hostPortDbNameStr = StringUtils.substringBetween(jdbcUrl, "//", "?");
			} else {
				hostPortDbNameStr = StringUtils.substringAfter(jdbcUrl, "//");
			}
			
			String[] hostPortDbName = StringUtils.split(hostPortDbNameStr, "/");
			String[] hostAndPort = StringUtils.split(hostPortDbName[0], ":");
			host = hostAndPort[0];
			portStr = hostAndPort[1];
			dbName = hostPortDbName[1];
			
			if (log.isDebugEnabled()) {
				log.debug("Connection details used by debezium -> host=" + host + ", port=" + portStr + ", DB=" + dbName);
			}
			
			config.setHost(host);
			config.setPort(Integer.valueOf(portStr));
			config.setDatabaseName(dbName);
			config.setOffsetStorageFilename(adminService.getGlobalProperty(DebeziumConstants.GP_OFFSET_STORAGE_FILE));
			
			config.setAdditionalConfigProperties();
			
			engine = OpenmrsDebeziumEngine.getInstance();
			config.setConsumer(new DebeziumChangeConsumer(listener, engine));
			
			engine.start(config);
		}
		
	}
	
	/**
	 * Stops the OpenMRS debezium engine
	 */
	protected synchronized static void stop() {
		
		synchronized (DebeziumEngineManager.class) {
			if (engine != null) {
				log.info("Received call to stop OpenMRS debezium engine");
				
				engine.stop();
				engine = null;
			} else {
				log.info("No running OpenMRS debezium engine found");
			}
		}
		
	}
	
}
