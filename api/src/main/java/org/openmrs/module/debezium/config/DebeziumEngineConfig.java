package org.openmrs.module.debezium.config;

import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.debezium.mysql.SnapshotMode;
import org.openmrs.module.debezium.entity.DatabaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Consumer;

/**
 * A subclass of this interface provides configuration options for the debezium engine, please pay
 * detailed attention to the javadocs on {@link #getTablesToExclude()} and
 * {@link #getTablesToInclude()} methods.
 */
public interface DebeziumEngineConfig {
	
	/**
	 * Called to allow a listener to initialize itself before the debezium engine is started
	 */
	default void init() {
	}
	
	/**
	 * Gets the {@link org.openmrs.module.debezium.mysql.SnapshotMode}
	 *
	 * @return SnapshotMode
	 */
	SnapshotMode getSnapshotMode();
	
	/**
	 * Gets the {@link Consumer} instance to be called whenever a database event is received.
	 * 
	 * @return a {@link Consumer} instance
	 */
	Consumer<DatabaseEvent> getEventListener();
	
	/**
	 * Returns a set of table names with the changes to capture, the module does not capture changes in
	 * any table not included in this set. By default, the connector captures changes in every table. If
	 * this method returns a non empty set then {@link #getTablesToExclude()} should return null or an
	 * empty set because they are mutually exclusive. The returned value translates to do not capture
	 * changes for any tables except these
	 *
	 * @return set of table names
	 */
	default Set<String> getTablesToInclude() {
		return null;
	}
	
	/**
	 * Returns a set of table names to exclude when capturing changes, the module captures changes in
	 * any table not included in this set. If this method returns a non empty set then
	 * {@link #getTablesToInclude()} should return null or an empty set because they are mutually
	 * exclusive. The returned value translates to capture changes for all tables except these.
	 *
	 * @return set of tables names
	 */
	default Set<String> getTablesToExclude() {
		return null;
	}

}
