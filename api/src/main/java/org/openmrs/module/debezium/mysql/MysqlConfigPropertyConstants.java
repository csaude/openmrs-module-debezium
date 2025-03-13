package org.openmrs.module.debezium.mysql;

public class MysqlConfigPropertyConstants {
	
	protected static final String CONNECTOR_PROP_HISTORY_CLASS = "schema.history.internal";
	
	protected static final String CONNECTOR_PROP_HISTORY_FILE = "schema.history.internal.file.filename";
	
	protected static final String CONNECTOR_PROP_DB_SNAPSHOT_LOCKING_MODE = "snapshot.locking.mode";
	
	protected static final String CONNECTOR_PROP_DB_SSL_MODE = "database.ssl.mode";
	
	protected static final String CONNECTOR_PROP_INCLUDE_SCHEMA_CHANGES = "include.schema.changes";
	
	protected static final String CONNECTOR_PROP_DB_INCLUDE_LIST = "database.include.list";
	
	protected static final String CONNECTOR_PROP_TABLE_INCLUDE_LIST = "table.include.list";
	
	protected static final String CONNECTOR_PROP_TABLE_EXCLUDE_LIST = "table.exclude.list";

	protected static final String DATABASE_HISTORY = "database.history";

	protected static final String DATABASE_HISTORY_file = "database.history.file.filename";
	
}
