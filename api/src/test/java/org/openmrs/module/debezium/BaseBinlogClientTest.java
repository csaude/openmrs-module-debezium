package org.openmrs.module.debezium;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import io.debezium.connector.binlog.BinlogStreamingChangeEventSource.BinlogPosition;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BinlogUtils.class)
public class BaseBinlogClientTest {
	
	@Mock
	private BinaryLogClient mockClient;
	
	private TestBinLogClient client;
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(BinlogUtils.class);
		client = new TestBinLogClient();
		Whitebox.setInternalState(client, BinaryLogClient.class, mockClient);
	}
	
	@Test
	public void shouldCreateClientInConstructor() {
		BinlogPosition mockPosition = Mockito.mock(BinlogPosition.class);
		client = new TestBinLogClient(mockPosition);
		
		PowerMockito.verifyStatic(BinlogUtils.class);
		BinlogUtils.createBinlogClient(mockPosition, client, client);
	}
	
	@Test
	public void connect_shouldConnectTheBinLogClient() throws Exception {
		client.connect();
		
		Mockito.verify(mockClient).connect();
	}
	
	@Test
	public void connect_shouldInvokeTheFailureCallbackWhenConnectionFails() throws Exception {
		client = Mockito.spy(client);
		Mockito.doThrow(new IOException("test")).when(mockClient).connect();
		Mockito.doNothing().when(client).onConnectionFailure();
		
		client.connect();
		
		Mockito.verify(mockClient).connect();
		Mockito.verify(client).onConnectionFailure();
	}
	
	@Test
	public void disconnect_shouldDisconnectTheBinLogClient() throws Exception {
		client.disconnect();
		
		Mockito.verify(mockClient).disconnect();
	}
	
	@Test
	public void disconnect_shouldInvokeTheFailureCallbackWhenDisconnectionFails() throws Exception {
		client = Mockito.spy(client);
		Mockito.doThrow(new IOException("test")).when(mockClient).disconnect();
		Mockito.doNothing().when(client).onDisconnectionFailure();
		
		client.disconnect();
		
		Mockito.verify(mockClient).disconnect();
		Mockito.verify(client).onDisconnectionFailure();
	}
	
}
