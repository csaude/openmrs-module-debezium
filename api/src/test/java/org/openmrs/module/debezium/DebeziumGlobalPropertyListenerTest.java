package org.openmrs.module.debezium;

import static org.openmrs.module.debezium.DebeziumConstants.GP_ENABLED;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.GlobalProperty;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DebeziumEngineManager.class })
@PowerMockIgnore("javax.management.*")
public class DebeziumGlobalPropertyListenerTest {
	
	private DebeziumGlobalPropertyListener listener = new DebeziumGlobalPropertyListener();
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(DebeziumEngineManager.class);
	}
	
	@Test
	public void supportsPropertyName_shouldReturnTrueIfPropertyNameIsEngineEnabled() {
		Assert.assertTrue(listener.supportsPropertyName(GP_ENABLED));
	}
	
	@Test
	public void supportsPropertyName_shouldReturnFalseIfPropertyNameIsEngineEnabledMatchingCase() {
		Assert.assertFalse(listener.supportsPropertyName(GP_ENABLED.toUpperCase()));
	}
	
	@Test
	public void supportsPropertyName_shouldReturnFalseIfPropertyNameIsNotEngineEnabled() {
		Assert.assertFalse(listener.supportsPropertyName(DebeziumConstants.GP_USER));
	}
	
	@Test
	public void globalPropertyChanged_shouldStartTheDebeziumEngineIfThePropertyValueIsSetToTrue() {
		listener.globalPropertyChanged(new GlobalProperty(GP_ENABLED, "true"));
		PowerMockito.verifyStatic(DebeziumEngineManager.class);
		DebeziumEngineManager.start();
	}
	
	@Test
	public void globalPropertyChanged_shouldStartTheDebeziumEngineIfThePropertyValueIsSetToTrueIngoringCase() {
		listener.globalPropertyChanged(new GlobalProperty(GP_ENABLED, "TRUE"));
		PowerMockito.verifyStatic(DebeziumEngineManager.class);
		DebeziumEngineManager.start();
	}
	
	@Test
	public void globalPropertyChanged_shouldStopTheDebeziumEngineIfThePropertyValueIsNotSetToTrue() {
		listener.globalPropertyChanged(new GlobalProperty(GP_ENABLED, "false"));
		PowerMockito.verifyStatic(DebeziumEngineManager.class);
		DebeziumEngineManager.stop();
	}
	
	@Test
	public void globalPropertyDeleted_shouldStopTheDebeziumEngine() {
		listener.globalPropertyDeleted(GP_ENABLED);
		PowerMockito.verifyStatic(DebeziumEngineManager.class);
		DebeziumEngineManager.stop();
	}
	
}
