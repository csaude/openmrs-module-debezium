package org.openmrs.module.debezium;

import java.util.Set;

/**
 * A subclass of this interface will be notified of database change events, please pay detailed
 * attention to the javadocs on {@link #getTablesToExclude(boolean)} and
 * {@link #getTablesToInclude(boolean)} methods.
 */
public interface DatabaseEventListener {
	
	/**
	 * Called whenever a database change is received
	 *
	 * @param e the DatabaseEvent object
	 */
	void onEvent(DatabaseEvent e);
	
	/**
	 * Returns a set of table names with the changes to capture, the module does not capture changes in
	 * any table not included in this set. By default, the connector captures changes in every table. If
	 * this method returns a non empty set then {@link #getTablesToExclude(boolean)} should return null
	 * or an empty set because they are mutually exclusive. The returned value translates to do not
	 * capture changes for any tables except these
	 *
	 * @param snapshotOnly specified if we are running in a snapshot only mode
	 * @return set of table names
	 */
	default Set<String> getTablesToInclude(boolean snapshotOnly) {
		return null;
	}
	
	/**
	 * Returns a set of table names to exclude when capturing changes, the module captures changes in
	 * any table not included in this set. If this method returns a non empty set then
	 * {@link #getTablesToInclude(boolean)} should return null or an empty set because they are mutually
	 * exclusive. The returned value translates to capture changes for all tables except these.
	 *
	 * @param snapshotOnly specified if we are running in a snapshot only mode
	 * @return set of tables names
	 */
	default Set<String> getTablesToExclude(boolean snapshotOnly) {
		return null;
	}
	
}
