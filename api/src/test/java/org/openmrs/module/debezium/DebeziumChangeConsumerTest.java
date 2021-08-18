package org.openmrs.module.debezium;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import io.debezium.DebeziumException;
import io.debezium.engine.ChangeEvent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CustomFileOffsetBackingStore.class, OpenmrsDebeziumEngine.class })
public class DebeziumChangeConsumerTest {
	
	private DebeziumChangeConsumer consumer;
	
	@Mock
	private Function mockFunction;
	
	@Mock
	private DatabaseEventListener mockListener;
	
	@Mock
	private ChangeEvent mockChangeEvent;
	
	@Mock
	private DatabaseEvent mockDatabaseEvent;
	
	@Mock
	private OpenmrsDebeziumEngine mockEngine;
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(CustomFileOffsetBackingStore.class);
		PowerMockito.mockStatic(OpenmrsDebeziumEngine.class);
		MockitoAnnotations.initMocks(this);
		consumer = new DebeziumChangeConsumer(mockListener, mockEngine);
		Whitebox.setInternalState(consumer, "function", mockFunction);
		Mockito.reset(mockListener);
		Mockito.reset(mockFunction);
	}
	
	@Test
	public void accept_shouldCallTheListenerIfEnabled() {
		when(mockFunction.apply(mockChangeEvent)).thenReturn(mockDatabaseEvent);
		
		consumer.accept(mockChangeEvent);
		
		verify(mockFunction).apply(mockChangeEvent);
		verify(mockListener).onEvent(mockDatabaseEvent);
	}
	
	@Test
	public void accept_shouldNotCallTheListenerIfDisabled() {
		Whitebox.setInternalState(consumer, "disabled", true);
		
		consumer.accept(mockChangeEvent);
		
		verifyZeroInteractions(mockFunction);
		verifyZeroInteractions(mockListener);
	}
	
	@Test
	public void accept_shouldDisableTheFileBackingStoreAndStopDebeziumEngineAndSetDisableToTrueWhenAnErrorOccurs() {
		Assert.assertFalse(Whitebox.getInternalState(consumer, "disabled"));
		when(mockFunction.apply(mockChangeEvent)).thenThrow(new DebeziumException());
		
		consumer.accept(mockChangeEvent);
		
		Assert.assertTrue(Whitebox.getInternalState(consumer, "disabled"));
		verify(mockEngine).stop();
		PowerMockito.verifyStatic();
		CustomFileOffsetBackingStore.disable();
	}
	
}
