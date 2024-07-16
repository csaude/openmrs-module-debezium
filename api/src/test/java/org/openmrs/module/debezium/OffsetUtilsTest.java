package org.openmrs.module.debezium;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerFactory.class })
@PowerMockIgnore("javax.management.*")
public class OffsetUtilsTest {
	
	public static final String OFFSET_PROP_FILE = "file";
	
	public static final String OFFSET_PROP_POSITION = "pos";
	
	public static final String OFFSET_PROP_ROW = "row";
	
	public static final String OFFSET_PROP_EVENT = "event";
	
	@Mock
	private Logger mockLogger;
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(LoggerFactory.class);
		Mockito.when(LoggerFactory.getLogger(ArgumentMatchers.any(Class.class))).thenReturn(mockLogger);
	}
	
	@Test
	public void transformOffsetIfNecessary_shouldTransformTheOffsetFileKeyItIsAMap() throws Exception {
		File file = new File(ClassUtils.getDefaultClassLoader().getResource("old_offset.txt").getFile());
		CustomFileOffsetBackingStore store = new CustomFileOffsetBackingStore();
		Utils.setFieldValue(store, FileOffsetBackingStore.class.getDeclaredField("file"), file);
		Utils.invokeMethod(store, FileOffsetBackingStore.class.getDeclaredMethod("load"));
		Map<ByteBuffer, ByteBuffer> offset = Utils.getFieldValue(store,
		    MemoryOffsetBackingStore.class.getDeclaredField("data"));
		ObjectMapper mapper = new ObjectMapper();
		Map keyMap = mapper.readValue(offset.keySet().iterator().next().array(), Map.class);
		Assert.assertTrue(keyMap.containsKey("schema"));
		final String extract = "extract";
		final String server = "Nsambya";
		assertEquals(extract, ((List) keyMap.get("payload")).get(0));
		assertEquals(server, ((Map) ((List) keyMap.get("payload")).get(1)).get("server"));
		final int ts = 1697696629;
		final String binlogFile = "bin-log.000012";
		final int position = 1192;
		final int row = 1;
		final int event = 2;
		final int serverId = 2;
		Map valueMap = mapper.readValue(offset.values().iterator().next().array(), Map.class);
		assertEquals(ts, valueMap.get("ts_sec"));
		assertEquals(binlogFile, valueMap.get(OFFSET_PROP_FILE));
		assertEquals(position, valueMap.get(OFFSET_PROP_POSITION));
		assertEquals(row, valueMap.get(OFFSET_PROP_ROW));
		assertEquals(event, valueMap.get(OFFSET_PROP_EVENT));
		assertEquals(serverId, valueMap.get("server_id"));
		
		OffsetUtils.transformOffsetIfNecessary(offset);
		
		List keyList = mapper.readValue(offset.keySet().iterator().next().array(), List.class);
		assertEquals(extract, keyList.get(0));
		assertEquals(server, ((Map) keyList.get(1)).get("server"));
		valueMap = mapper.readValue(offset.values().iterator().next().array(), Map.class);
		assertEquals(ts, valueMap.get("ts_sec"));
		assertEquals(binlogFile, valueMap.get(OFFSET_PROP_FILE));
		assertEquals(position, valueMap.get(OFFSET_PROP_POSITION));
		assertEquals(row, valueMap.get(OFFSET_PROP_ROW));
		assertEquals(event, valueMap.get(OFFSET_PROP_EVENT));
		assertEquals(serverId, valueMap.get("server_id"));
	}
	
	@Test
	public void transformOffsetIfNecessary_shouldSkipIfOffsetIsEmpty() throws Exception {
		Map mockOffset = Mockito.mock(Map.class);
		Mockito.when(mockOffset.isEmpty()).thenReturn(true);
		
		OffsetUtils.transformOffsetIfNecessary(mockOffset);
		
		Mockito.verify(mockOffset, Mockito.never()).keySet();
	}
	
	@Test
	public void transformOffsetIfNecessary_shouldNotTransformTheOffsetFileKeyIfItIsAList() throws Exception {
		File file = new File(ClassUtils.getDefaultClassLoader().getResource("offset.txt").getFile());
		CustomFileOffsetBackingStore store = new CustomFileOffsetBackingStore();
		Utils.setFieldValue(store, FileOffsetBackingStore.class.getDeclaredField("file"), file);
		Utils.invokeMethod(store, FileOffsetBackingStore.class.getDeclaredMethod("load"));
		Map<ByteBuffer, ByteBuffer> offset = Utils.getFieldValue(store,
		    MemoryOffsetBackingStore.class.getDeclaredField("data"));
		ObjectMapper mapper = new ObjectMapper();
		final String extract = "extract";
		final String server = "Nsambya";
		List keyList = mapper.readValue(offset.keySet().iterator().next().array(), List.class);
		assertEquals(extract, keyList.get(0));
		assertEquals(server, ((Map) keyList.get(1)).get("server"));
		final int ts = 1701953000;
		final String binlogFile = "bin-log.000005";
		final int position = 4660;
		final int row = 1;
		final int event = 2;
		final int serverId = 2;
		Map valueMap = mapper.readValue(offset.values().iterator().next().array(), Map.class);
		assertEquals(ts, valueMap.get("ts_sec"));
		assertEquals(binlogFile, valueMap.get(OFFSET_PROP_FILE));
		assertEquals(position, valueMap.get(OFFSET_PROP_POSITION));
		assertEquals(row, valueMap.get(OFFSET_PROP_ROW));
		assertEquals(event, valueMap.get(OFFSET_PROP_EVENT));
		assertEquals(serverId, valueMap.get("server_id"));
		
		OffsetUtils.transformOffsetIfNecessary(offset);
		
		keyList = mapper.readValue(offset.keySet().iterator().next().array(), List.class);
		assertEquals(extract, keyList.get(0));
		assertEquals(server, ((Map) keyList.get(1)).get("server"));
		valueMap = mapper.readValue(offset.values().iterator().next().array(), Map.class);
		assertEquals(ts, valueMap.get("ts_sec"));
		assertEquals(binlogFile, valueMap.get(OFFSET_PROP_FILE));
		assertEquals(position, valueMap.get(OFFSET_PROP_POSITION));
		assertEquals(row, valueMap.get(OFFSET_PROP_ROW));
		assertEquals(event, valueMap.get(OFFSET_PROP_EVENT));
		assertEquals(serverId, valueMap.get("server_id"));
	}
	
}
