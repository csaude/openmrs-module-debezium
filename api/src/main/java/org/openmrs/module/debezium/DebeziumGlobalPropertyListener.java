package org.openmrs.module.debezium;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.springframework.stereotype.Component;

@Component("dbzmGlobalPropertyListener")
public class DebeziumGlobalPropertyListener implements GlobalPropertyListener {
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return DebeziumConstants.GP_ENABLED.equals(propertyName);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		if ("true".equalsIgnoreCase(newValue.getPropertyValue())) {
			DebeziumEngineManager.start();
		} else {
			DebeziumEngineManager.stop();
		}
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		DebeziumEngineManager.stop();
	}
	
}
