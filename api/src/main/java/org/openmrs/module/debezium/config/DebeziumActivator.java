package org.openmrs.module.debezium.config;

import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.debezium.task.DebeziumPrunerTask;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DebeziumActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumActivator.class);
	
	private static final String TASK_NAME = "DEBEZIUM EVENTS PRUNER TASK";
	
	/**
	 * @see org.openmrs.module.BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("Debezium module started, starting OpenMRS debezium engine");
		
		DebeziumEngineManager.start();
		this.registerTask();
	}
	
	/**
	 * @see org.openmrs.module.BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		log.info("Debezium module stopped");
	}
	
	/**
	 * @see org.openmrs.module.BaseModuleActivator#willStop()
	 */
	@Override
	public void willStop() {
		log.info("Stopping OpenMRS debezium engine before debezium module is stopped");
		
		DebeziumEngineManager.stop();
	}
	
	private void registerTask() {
		
		try {
			SchedulerService schedulerService = Context.getService(SchedulerService.class);
			
			// Check if exists a task running in order to avoid duplicates
			TaskDefinition existingTask = schedulerService.getTaskByName(TASK_NAME);
			if (existingTask == null) {
				TaskDefinition taskDefinition = new TaskDefinition();
				taskDefinition.setName(TASK_NAME);
				taskDefinition.setTaskClass(DebeziumPrunerTask.class.getName());
				taskDefinition.setStartTime(new Date());
				taskDefinition.setRepeatInterval(1800L);
				taskDefinition.setStartOnStartup(true);
				taskDefinition.setDescription("Task to be used to prune events read by registered applications");
				schedulerService.saveTaskDefinition(taskDefinition);
				schedulerService.scheduleTask(taskDefinition);
				log.info("Scheduled task registered and started: {}", taskDefinition.getName());
			} else {
				log.info("Task already registered: {}", TASK_NAME);
			}
		}
		catch (SchedulerException e) {
			log.error("Failed to register scheduled task", e);
		}
		
	}
	
}
