package org.openmrs.module.debezium;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.utils.DebeziumConstants;
import org.openmrs.module.debezium.utils.Utils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, LoggerFactory.class })
@SuppressStaticInitializationFor("org.openmrs.api.context.Context")
public class UtilsMockTest {
	
	@Mock
	private Logger mockLogger;
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(Context.class);
		PowerMockito.mockStatic(LoggerFactory.class);
		Mockito.when(LoggerFactory.getLogger(ArgumentMatchers.any(Class.class))).thenReturn(mockLogger);
	}
	
	@Test
	public void getConnectionDetails_shouldGetTheHostPostAndDb() {
		final String host = "localhost";
		final String port = "3306";
		final String db = "openmrs";
		Properties props = new Properties();
		props.put(DebeziumConstants.PROP_DB_URL, "jdbc:mysql://" + host + ":" + port + "/" + db);
		Mockito.when(Context.getRuntimeProperties()).thenReturn(props);
		
		String[] details = Utils.getConnectionDetails();
		
		assertEquals(3, details.length);
		assertEquals(host, details[0]);
		assertEquals(port, details[1]);
		assertEquals(db, details[2]);
	}
	
	@Test
	public void getConnectionDetails_shouldGetTheHostPostAndDbIfUrlContainsParameters() {
		final String host = "localhost";
		final String port = "3306";
		final String db = "openmrs";
		Properties props = new Properties();
		props.put(DebeziumConstants.PROP_DB_URL, "jdbc:mysql://" + host + ":" + port + "/" + db + "?prop=value");
		Mockito.when(Context.getRuntimeProperties()).thenReturn(props);
		
		String[] details = Utils.getConnectionDetails();
		
		assertEquals(3, details.length);
		assertEquals(host, details[0]);
		assertEquals(port, details[1]);
		assertEquals(db, details[2]);
	}
	
}
