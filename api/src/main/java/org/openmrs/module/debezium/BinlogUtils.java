package org.openmrs.module.debezium;

import static com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY;
import static com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG;
import static org.openmrs.module.debezium.DebeziumConstants.GP_DB_SERVER_ID;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;

import io.debezium.connector.binlog.BinlogStreamingChangeEventSource.BinlogPosition;

public class BinlogUtils {
	
	/**
	 * Creates a {@link BinaryLogClient} instance to connect to the MySQL binlog at the filename and
	 * position of the specified {@link BinlogPosition}
	 *
	 * @param binlogPosition {@link BinlogPosition} instance
	 * @return BinaryLogClient
	 */
	public static BinaryLogClient createBinlogClient(BinlogPosition binlogPosition,
	                                                 BinaryLogClient.EventListener eventListener,
	                                                 BinaryLogClient.LifecycleListener lifecycleListener) {
		String username;
		String password;
		String userGp = Context.getAdministrationService().getGlobalProperty(DebeziumConstants.GP_USER);
		if (StringUtils.isNotBlank(userGp)) {
			username = userGp.trim();
			password = Context.getAdministrationService().getGlobalProperty(DebeziumConstants.GP_PASSWORD).trim();
		} else {
			username = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME).trim();
			password = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_PASSWORD).trim();
		}
		
		Long serverId = Long.valueOf(Context.getAdministrationService().getGlobalProperty(GP_DB_SERVER_ID.trim()));
		String[] creds = Utils.getConnectionDetails();
		BinaryLogClient client = new BinaryLogClient(creds[0], Integer.valueOf(creds[1]), username, password);
		client.setServerId(serverId);
		client.setBinlogFilename(binlogPosition.getFilename());
		client.setBinlogPosition(binlogPosition.getPosition());
		client.setKeepAlive(false);
		EventDeserializer eventDeserializer = new EventDeserializer();
		eventDeserializer.setCompatibilityMode(DATE_AND_TIME_AS_LONG, CHAR_AND_BINARY_AS_BYTE_ARRAY);
		client.setEventDeserializer(eventDeserializer);
		client.registerEventListener(eventListener);
		client.registerLifecycleListener(lifecycleListener);
		
		return client;
	}
	
}
