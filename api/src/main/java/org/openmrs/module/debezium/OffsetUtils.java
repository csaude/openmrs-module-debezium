package org.openmrs.module.debezium;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OffsetUtils {
	
	private static final Logger log = LoggerFactory.getLogger(OffsetUtils.class);
	
	/**
	 * Transforms the specified offset file to match the new expected structure after the debezium and
	 * kafka upgrades in previous versions.
	 * 
	 * @param offset the offset data
	 * @throws IOException
	 */
	public static void transformOffsetIfNecessary(Map<ByteBuffer, ByteBuffer> offset) throws IOException {
		if (offset.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("No existing offset file found, skipping offset transformation check");
			}
			
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ByteBuffer keyByteBuf = offset.keySet().iterator().next();
		ByteBuffer valueByteBuf = offset.get(keyByteBuf);
		JsonNode keyNode = mapper.readValue(keyByteBuf.array(), JsonNode.class);
		if (keyNode.isObject()) {
			log.info("Transforming offset to structure that conforms to the new kafka API");
			
			offset.remove(keyByteBuf);
			byte[] newKeyBytes = mapper.writeValueAsBytes(keyNode.get("payload"));
			offset.put(ByteBuffer.wrap(newKeyBytes), valueByteBuf);
		}
	}
	
}
