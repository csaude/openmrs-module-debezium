package org.openmrs.module.debezium;

public class ConfigPropertyConstants {
	
	protected static final String ENGINE_DEFAULT_NAME = "OpenMRS Debezium Engine";
	
	protected static final String ENGINE_PROP_NAME = "name";
	
	protected static final String ENGINE_PROP_CONNECT_CLASS = "connector.class";
	
	protected static final String ENGINE_PROP_OFF_SET_STORAGE_CLASS = "offset.storage";
	
	protected static final String ENGINE_PROP_OFF_SET_STORAGE_FILE = "offset.storage.file.filename";
	
	protected static final String ENGINE_PROP_OFF_SET_FLUSH_INTERVAL_MS = "offset.flush.interval.ms";
	
	protected static final String CONNECTOR_PROP_DB_HOST = "database.hostname";
	
	protected static final String CONNECTOR_PROP_DB_PORT = "database.port";
	
	protected static final String CONNECTOR_PROP_DB_USERNAME = "database.user";
	
	protected static final String CONNECTOR_PROP_DB_PASSWORD = "database.password";
	
	protected static final String CONNECTOR_PROP_HISTORY_CLASS = "database.history";
	
	protected static final String CONNECTOR_PROP_HISTORY_FILE = "database.history.file.filename";
	
	protected static final String CONNECTOR_PROP_SNAPSHOT_MODE = "snapshot.mode";
	
	protected static final String CONNECTOR_PROP_TABLES_INCLUDE_LIST = "table.include.list";
	
}
