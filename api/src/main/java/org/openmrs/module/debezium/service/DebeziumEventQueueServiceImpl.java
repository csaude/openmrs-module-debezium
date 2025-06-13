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

import java.time.LocalDateTime;
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
	
	private DebeziumEventQueueDAO eventQueueDAO;
	
	private DebeziumEventQueueOffsetDAO offsetDAO;
	
	@Override
	public Set<DebeziumEventQueue> getApplicationEvents(String applicationName) {
		
		Set<DebeziumEventQueue> allFetchedEvents = new HashSet<>();
		List<String> parameterizedTables = getParameterizedTables(applicationName);
		DebeziumEventQueueOffset offset = offsetDAO.getOffsetByApplicationName(applicationName);
		int fetchSize = Integer.parseInt(getFetchSize());
		Integer firstRead = 0;
		Integer starterId = offset != null ? offset.getFirstRead() : 0;
		// Fetch events
		while (allFetchedEvents.size() < fetchSize) {
			
			List<DebeziumEventQueue> lastFetchedEvents = eventQueueDAO.fetchDebeziumEvents(starterId, fetchSize);
			
			if (lastFetchedEvents == null || lastFetchedEvents.isEmpty()) {
				break;
			}
			
			for (DebeziumEventQueue debeziumEventQueue : lastFetchedEvents) {
				if (parameterizedTables.contains(debeziumEventQueue.getTableName())) {
					allFetchedEvents.add(debeziumEventQueue);
					if (allFetchedEvents.size() >= fetchSize) {
						break;
					}
				}
			}
			
			if (firstRead == 0) {
				firstRead = lastFetchedEvents.get(0).getId();
			}
			starterId = lastFetchedEvents.get(lastFetchedEvents.size() - 1).getId();
		}
		
		saveOrUpdateOffset(offset, firstRead, starterId, applicationName);
		return allFetchedEvents;
	}
	
	private void saveOrUpdateOffset(DebeziumEventQueueOffset offset, Integer firstRead, Integer lastRead,
	        String applicationName) {
		if (firstRead != 0) {
			if (offset != null) {
				offset.setLastRead(lastRead);
				offsetDAO.updateOffset(offset);
			} else {
				offset = new DebeziumEventQueueOffset();
				offset.setFirstRead(firstRead);
				offset.setLastRead(lastRead);
				offset.setApplicationName(applicationName);
				offset.setActive(Boolean.TRUE);
				offset.setCreatedAt(new Date());
				offsetDAO.saveOffset(offset);
			}
		}
	}
	
	@Override
	public void commitEventQueue(String applicationName) {
		DebeziumEventQueueOffset offset = offsetDAO.getOffsetByApplicationName(applicationName);
		if (offset != null && offset.getLastRead() != null) {
			offset.setFirstRead(offset.getLastRead());
			offset.setUpdatedAt(LocalDateTime.now());
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
	private static Map<String, List<String>> getApplicationsTableToSync() {
		String applicationNames = Utils.getGlobalPropertyValue(DebeziumConstants.GP_APPLICATION_NAME);
		
		try {
			Map<String, List<String>> applicationTableMap = new HashMap<>();
			
			if (applicationNames != null) {
				for (String applicationName : applicationNames.split(";")) {
					String[] application = applicationName.split(":");
					
					String name = application[0];
					List<String> tablesToWatch = Arrays.asList(application[1].split(","));
					applicationTableMap.put(name, tablesToWatch);
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
		return getApplicationsTableToSync().get(applicationName);
	}
}
