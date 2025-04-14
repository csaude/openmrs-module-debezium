package org.openmrs.module.debezium.task;

import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.service.DebeziumEventService;
import org.openmrs.module.debezium.utils.BinlogUtils;
import org.openmrs.module.debezium.utils.DebeziumConstants;
import org.openmrs.module.debezium.utils.Utils;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DebeziumPrunerTask extends AbstractTask {
	
	protected static final Logger log = LoggerFactory.getLogger(DebeziumPrunerTask.class);
	
	@Override
	public void execute() {
		if (!isExecuting()) {
			log.info("Starting execution of DebeziumPrunerTask");

			DebeziumEventService debeziumEventService = Context.getService(DebeziumEventService.class);
			Integer minFirstRead = debeziumEventService.getMinFirstRead();

            log.info("Executing prune command for debezium event with id less than {}", minFirstRead);
			if(minFirstRead != null) {
				debeziumEventService.removeProcessedEvents(minFirstRead);
				log.info("Prune finalized");
			}

			// Verify if the pruner task for binlog is activated
			String isPrunerBinlogActivated = Utils.getGlobalPropertyValue(DebeziumConstants.GP_BINLOG_PRUNE);

			if(Boolean.parseBoolean(isPrunerBinlogActivated)) {
				log.info("Prune binlog activated");
				String offsetFile = Context.getAdministrationService()
						.getGlobalProperty(DebeziumConstants.GP_OFFSET_STORAGE_FILE);
				if (offsetFile != null) {
					String binlogPosition = BinlogUtils.getMysqlBinlog(offsetFile);
					if (binlogPosition != null) {
						try {
							BinlogUtils.purgeBinLogsTo(binlogPosition);
						}
						catch (SQLException e) {
                            log.error("Could not purge binlog at {}", binlogPosition, e);
							throw new RuntimeException(e);
						}
					}
				}

				log.info("Prune binlog finalized");

			}
			log.info("Execution of DebeziumPrunerTask ended DebeziumPrunerTask");
		}
	}
}
