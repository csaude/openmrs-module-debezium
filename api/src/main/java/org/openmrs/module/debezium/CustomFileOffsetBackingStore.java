package org.openmrs.module.debezium;

import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Custom {@link FileOffsetBackingStore} that only saves the offset if no exception was encountered
 * while processing a source record read by debezium from the MySQL binlog to ensure no binlog entry
 * goes unprocessed.
 */
public class CustomFileOffsetBackingStore extends FileOffsetBackingStore {
	
	protected static final Logger log = LoggerFactory.getLogger(CustomFileOffsetBackingStore.class);
	
	private static boolean disabled = false;
	
	private static final JsonConverter KEY_CONVERTER = new JsonConverter();
	
	public CustomFileOffsetBackingStore() {
		//super(KEY_CONVERTER);
		super();
		KEY_CONVERTER.configure(Collections.singletonMap(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, "false"), true);
	}
	
	/**
	 * Disables offset storage
	 */
	public static void disable() {
		disabled = true;
		if (log.isDebugEnabled()) {
			log.debug("Disabled saving of offsets");
		}
	}
	
	/**
	 * Re-enables offset storage
	 */
	public static void reset() {
		disabled = false;
	}
	
	/**
	 * @see FileOffsetBackingStore#save()
	 */
	@Override
	protected void save() {
		synchronized (CustomFileOffsetBackingStore.class) {
			if (disabled) {
				log.warn("Skipping saving of offset because an error was encountered while processing a change event");
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Saving binlog offset");
			}
			
			super.save();
		}
	}
	
}
