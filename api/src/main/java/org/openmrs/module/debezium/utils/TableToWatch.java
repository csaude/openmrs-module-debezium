package org.openmrs.module.debezium.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TableToWatch {
	
	person,
	
	patient,
	
	visit,
	
	encounter,
	
	obs,
	
	person_attribute,
	
	patient_program,
	
	patient_state,
	
	provider_attribute,
	
	encounter_diagnosis,
	
	conditions,
	
	person_name,
	
	allergy,
	
	person_address,
	
	patient_identifier,
	
	orders,
	
	drug_order,
	
	test_order,
	
	relationship,
	
	provider,
	
	encounter_provider,
	
	clinicalsummary_usage_report,
	
	esaudefeatures_rps_import_log;
	
	public static Set<String> getTablesToInclude() {
		return Arrays.stream(TableToWatch.values()).map(TableToWatch::name).collect(Collectors.toSet());
	}
}
