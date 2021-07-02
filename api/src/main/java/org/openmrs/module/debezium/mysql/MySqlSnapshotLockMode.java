package org.openmrs.module.debezium.mysql;

import org.openmrs.module.debezium.DebeziumEnumeratedPropertyValue;
import org.openmrs.module.debezium.SslMode;

/**
 * Enumeration for {@link SslMode} supported by MySQL, for more details please refer to
 * https://debezium.io/documentation/reference/1.6/connectors/mysql.html#mysql-property-snapshot-locking-mode
 */
public enum MySqlSnapshotLockMode implements DebeziumEnumeratedPropertyValue {
	
	/**
	 * Blocks all writes for the duration of the snapshot. Use this setting if there are clients that
	 * are submitting operations that MySQL excludes from REPEATABLE READ semantics.
	 */
	EXTENDED("extended");
	
	private String propertyValue;
	
	MySqlSnapshotLockMode(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public String getPropertyValue() {
		return propertyValue;
	}
	
}
