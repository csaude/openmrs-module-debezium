package org.openmrs.module.debezium;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OffsetUtils.class)
@PowerMockIgnore("javax.management.*")
public class CustomFileOffsetBackingStoreTest {
	
	private CustomFileOffsetBackingStore store;
	
	private boolean disabledOriginal;
	
	@Before
	public void setup() {
		store = Mockito.spy(new CustomFileOffsetBackingStore());
		disabledOriginal = Whitebox.getInternalState(CustomFileOffsetBackingStore.class, "disabled");
		Whitebox.setInternalState(CustomFileOffsetBackingStore.class, "disabled", false);
	}
	
	@After
	public void tearDown() {
		Whitebox.setInternalState(CustomFileOffsetBackingStore.class, "disabled", disabledOriginal);
	}
	
	@Test
	public void start_shouldTransformAndVerifyTheExistingOffset() throws Exception {
		Mockito.doNothing().when(store).doStart();
		Map mockData = new HashMap();
		PowerMockito.mockStatic(OffsetUtils.class);
		Whitebox.setInternalState(store, Map.class, mockData);
		
		store.start();
		
		Mockito.verify(store).doStart();
		PowerMockito.verifyStatic(OffsetUtils.class);
		OffsetUtils.transformOffsetIfNecessary(mockData);
	}
	
}
