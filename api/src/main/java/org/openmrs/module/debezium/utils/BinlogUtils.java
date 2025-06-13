package org.openmrs.module.debezium.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import io.debezium.connector.binlog.BinlogStreamingChangeEventSource.BinlogPosition;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY;
import static com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG;
import static org.openmrs.module.debezium.utils.DebeziumConstants.GP_DB_SERVER_ID;

public class BinlogUtils {
	
	private static final Logger log = LoggerFactory.getLogger(BinlogUtils.class);
	
	protected static final String URL_QUERY = "?autoReconnect=true&sessionVariables=default_storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8";
	
	protected static final String FILE_PLACEHOLDER = "{TO_BIN_FILE}";
	
	protected static final String QUERY_PURGE_LOGS = "PURGE BINARY LOGS TO '" + FILE_PLACEHOLDER + "'";
	
	/**
	 * Creates a {@link BinaryLogClient} instance to connect to the MySQL binlog at the filename and
	 * position of the specified {@link BinlogPosition}
	 *
	 * @param binlogPosition {@link BinlogPosition} instance
	 * @return BinaryLogClient
	 */
	
	public static BinaryLogClient createBinlogClient(BinlogPosition binlogPosition,
	        BinaryLogClient.EventListener eventListener, BinaryLogClient.LifecycleListener lifecycleListener) {
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
	
	/**
	 * Extract the binlog file name from a given offset file path
	 * 
	 * @param offsetFilePath
	 * @return
	 */
	public static String getMysqlBinlog(String offsetFilePath) {
		if (offsetFilePath == null || offsetFilePath.trim().isEmpty()) {
			log.error("Offset file path is null or empty");
			throw new IllegalArgumentException("Invalid offset file path");
		}
		
		Path path = Paths.get(offsetFilePath);
		
		if (!Files.exists(path)) {
			log.error("Offset file path does not exist");
			throw new IllegalArgumentException("Offset file path does not exist: " + offsetFilePath);
		}
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(offsetFilePath))) {
			Object obj = ois.readObject();
			
			if (!(obj instanceof Map)) {
				throw new IllegalStateException("Unexpected object in offset file: not a Map");
			}
			
			Map<?, ?> offsetMap = (Map<?, ?>) obj;
			ObjectMapper objectMapper = new ObjectMapper();
			
			for (Map.Entry<?, ?> entry : offsetMap.entrySet()) {
				Object value = entry.getValue();
				
				if (value instanceof byte[]) {
					String json = new String((byte[]) value, StandardCharsets.UTF_8);
					Map<?, ?> innerMap = objectMapper.readValue(json, Map.class);
					Object file = innerMap.get("file");
					
					if (file != null) {
						return file.toString();
					}
				}
			}
			return null;
		}
		catch (Exception e) {
			log.error("Failed to extract binlog file name from offset: {}", offsetFilePath, e);
			throw new RuntimeException("Offset file parsing failed", e);
		}
	}
	
	/**
	 * Deletes all the processed binary log files up to the file that precedes the specified binary log
	 * file name.
	 *
	 * @param toBinLogFile the name of the last file to keep
	 * @throws SQLException
	 */
	public static void purgeBinLogsTo(String toBinLogFile) throws SQLException {
		try (Connection c = getConnectionToBinaryLogs(); Statement s = c.createStatement()) {
			log.info("Purging binlog files up to {}", toBinLogFile);
			s.executeUpdate(QUERY_PURGE_LOGS.replace(FILE_PLACEHOLDER, toBinLogFile));
			log.info("Successfully purged");
		}
	}
	
	protected static Connection getConnectionToBinaryLogs() throws SQLException {
		String[] connectionDetails = Utils.getConnectionDetails();
		
		String user = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME).trim();
		String password = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_PASSWORD).trim();
		
		final String host = connectionDetails[0];
		final String port = connectionDetails[1];
		final String url = "jdbc:mysql://" + host + ":" + port + URL_QUERY;
		
		return getConnection(url, user, password);
	}
	
	public static Connection getConnection(String url, String user, String password) throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
}
