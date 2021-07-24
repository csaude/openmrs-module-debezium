package org.openmrs.module.debezium;

public class DebeziumConstants {
	
	public final static String MODULE_ID = "debezium";
	
	public final static String EB_EVENT_CONSUMER_BEAN_NAME = "dbEventConsumer";
	
	public final static String PROP_DB_URL = "connection.url";
	
	public final static String PROP_DB_USERNAME = "connection.username";
	
	public final static String PROP_DB_PASSWORD = "connection.password";
	
	public final static String GP_ENABLED = MODULE_ID + ".engine.enabled";
	
	public final static String GP_USER = MODULE_ID + ".database.user";
	
	public final static String GP_PASSWORD = MODULE_ID + ".database.password";
	
	public final static String GP_TABLES_TO_INCLUDE = MODULE_ID + ".mysql.table.include.list";
	
	public final static String GP_TABLES_TO_EXCLUDE = MODULE_ID + ".mysql.table.exclude.list";
	
	public final static String GP_HISTORY_FILE = MODULE_ID + ".mysql.history.file.filename";
	
	public final static String GP_OFFSET_STORAGE_FILE = MODULE_ID + ".offset.storage.file.filename";
	
	public final static String GP_SNAPSHOT_MODE = MODULE_ID + ".snapshot.mode";
	
	public final static String GP_SNAPSHOT_LOCK_MODE = MODULE_ID + ".mysql.snapshot.locking.mode";
	
	public final static String GP_SSL_MODE = MODULE_ID + ".mysql.database.ssl.mode";
	
}
