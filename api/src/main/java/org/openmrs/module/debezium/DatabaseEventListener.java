package org.openmrs.module.debezium;

import java.util.Set;

/**
 * A subclass of this interface will be notified of database change events, please pay detailed
 * attention to the javadocs on {@link #getTablesToExclude()} and {@link #getTablesToInclude()}
 * methods.
 */
public interface DatabaseEventListener {
	
	/**
	 * Called to allow a listener to initialize itself before the debezium engine is started
	 * 
	 * @param snapshotOnly specifies if the module running in snapshot only mode or not
	 */
	default void init(boolean snapshotOnly) {
	}
	
	/**
	 * Called whenever a database change is received
	 *
	 * @param e the DatabaseEvent object
	 */
	void onEvent(DatabaseEvent e);
	
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
