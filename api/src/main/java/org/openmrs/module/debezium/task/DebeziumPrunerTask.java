package org.openmrs.module.debezium.task;

import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.service.DebeziumEventService;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebeziumPrunerTask extends AbstractTask {
	
	protected static final Logger log = LoggerFactory.getLogger(DebeziumPrunerTask.class);
	
	@Override
	public void execute() {
		if (!isExecuting()) {
			log.info("Starting execution of DebeziumPrunerTask");
			DebeziumEventService debeziumEventService = Context.getService(DebeziumEventService.class);
			Integer minFirstRead = debeziumEventService.getMinFirstRead();
			debeziumEventService.removeProcessedEvents(minFirstRead);

			log.info("Execution of DebeziumPrunerTask ended DebeziumPrunerTask");
			
		}
	}
}
