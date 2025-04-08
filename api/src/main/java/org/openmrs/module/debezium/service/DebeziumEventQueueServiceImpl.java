package org.openmrs.module.debezium.service;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.debezium.dao.DebeziumEventQueueDAO;
import org.openmrs.module.debezium.dao.DebeziumEventQueueOffsetDAO;
import org.openmrs.module.debezium.entity.DebeziumEventQueue;
import org.openmrs.module.debezium.entity.DebeziumEventQueueOffset;
import org.openmrs.module.debezium.utils.DebeziumConstants;
import org.openmrs.module.debezium.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openmrs.module.debezium.utils.Utils.getFetchSize;

public class DebeziumEventQueueServiceImpl extends BaseOpenmrsService implements DebeziumEventQueueService {
	
	private static final Logger logger = LoggerFactory.getLogger(DebeziumEventQueueServiceImpl.class);
	
	DebeziumEventQueueDAO eventQueueDAO;
	
	DebeziumEventQueueOffsetDAO offsetDAO;
	
	@Override
	public Set<DebeziumEventQueue> getEventsByApplicationName(String applicationName) {
		
		Set<DebeziumEventQueue> eventQueues = new HashSet<>();
		List<String> parameterizedTables = getParameterizedTables(applicationName);
		DebeziumEventQueueOffset offset = offsetDAO.getOffsetByApplicationName(applicationName);
		int fetchSize = Integer.parseInt(getFetchSize());
		
		// Request new data without commit of the preview request
		if (offset != null && offset.getLastRead() != null) {
			return this.processEventWithoutCommit(offset, parameterizedTables);
		} else {
			// Request of new data without offset respecting the fetch size limit
			while (eventQueues.size() < fetchSize) {
				List<DebeziumEventQueue> eventQueue;
				if (offset != null && offset.isCreated() && offset.getLastRead() != null) {
					eventQueue = eventQueueDAO.getEventsByApplicationNameRecursive(offset.getLastRead(), fetchSize);
					
				} else {
					eventQueue = eventQueueDAO.getEventsByApplicationName(offset, fetchSize);
				}
				
				if (eventQueue == null || eventQueue.isEmpty()) {
					break;
				}
				
				for (DebeziumEventQueue debeziumEventQueue : eventQueue) {
					if (parameterizedTables.contains(debeziumEventQueue.getTableName())) {
						eventQueues.add(debeziumEventQueue);
						if (eventQueues.size() >= fetchSize) {
							break;
						}
					}
				}
				//update offset or create new one
				if (!eventQueues.isEmpty()) {
					if (offset != null) {
						offset.setLastRead(eventQueue.get(eventQueue.size() - 1).getId());
					} else {
						offset = new DebeziumEventQueueOffset();
						offset.setFirstRead(eventQueue.get(0).getId());
						offset.setLastRead(eventQueue.get(eventQueue.size() - 1).getId());
						offset.setApplicationName(applicationName);
						offset.setActive(Boolean.TRUE);
						offset.setCreatedAt(new Date());
					}
				}
			}
		}
		
		if (!eventQueues.isEmpty()) {
			if (offset.isCreated()) {
				offsetDAO.updateOffset(offset);
			} else {
				offsetDAO.saveOffset(offset);
			}
		}
		
		return eventQueues;
	}
	
	private Set<DebeziumEventQueue> processEventWithoutCommit(DebeziumEventQueueOffset offset,
	        List<String> parameterizedTables) {
		Set<DebeziumEventQueue> previousEventQueues = new HashSet<>();
		List<DebeziumEventQueue> alreadyProcessedEvents = eventQueueDAO.getEventsByApplicationName(offset, null);
		
		for (DebeziumEventQueue debeziumEventQueue : alreadyProcessedEvents) {
			if (parameterizedTables.contains(debeziumEventQueue.getTableName())) {
				previousEventQueues.add(debeziumEventQueue);
			}
		}
		return previousEventQueues;
	}
	
	@Override
	public void commitEventQueue(String applicationName) {
		DebeziumEventQueueOffset offset = offsetDAO.getOffsetByApplicationName(applicationName);
		if(offset.getLastRead() != null) {
			offset.setFirstRead(offset.getLastRead());
			offset.setLastRead(null);
			offsetDAO.updateOffset(offset);
		}
	}
	
	public DebeziumEventQueueDAO getEventQueueDAO() {
		return eventQueueDAO;
	}
	
	public void setEventQueueDAO(DebeziumEventQueueDAO eventQueueDAO) {
		this.eventQueueDAO = eventQueueDAO;
	}
	
	public DebeziumEventQueueOffsetDAO getOffsetDAO() {
		return offsetDAO;
	}
	
	public void setOffsetDAO(DebeziumEventQueueOffsetDAO offsetDAO) {
		this.offsetDAO = offsetDAO;
	}
	
	/**
	 * Get application name and the table to be listened
	 *
	 * @return
	 */
	private static Map<String, List<String>> getParameterizedApplicationName() {
		String applicationNames = Utils.getGlobalPropertyValue(DebeziumConstants.GP_APPLICATION_NAME);
		
		try {
			Map<String, List<String>> applicationTableMap = new HashMap<>();
			
			if (applicationNames != null) {
				for (String applicationName : applicationNames.split(";")) {
					String[] application = applicationName.split(":");
					
					String name = application[0];
					List<String> TablesToWatch = Arrays.asList(application[1].split(","));
					applicationTableMap.put(name, TablesToWatch);
				}
			}
			return applicationTableMap;
		}
		catch (Exception e) {
			logger.error("An error occurred trying to get parameterized application : {}", e.getMessage());
			throw new RuntimeException(e);
		}
		
	}
	
	private static List<String> getParameterizedTables(String applicationName) {
		return getParameterizedApplicationName().get(applicationName);
	}
}
