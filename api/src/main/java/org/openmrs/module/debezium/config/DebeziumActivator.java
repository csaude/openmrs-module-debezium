package org.openmrs.module.debezium.config;

import org.openmrs.module.BaseModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DebeziumActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumActivator.class);
	
	/**
	 * @see org.openmrs.module.BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("Debezium module started, starting OpenMRS debezium engine");
		
		DebeziumEngineManager.start();
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
	
}
