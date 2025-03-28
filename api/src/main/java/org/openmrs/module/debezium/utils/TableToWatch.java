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
	
	concept_attribute,
	
	location_attribute,
	
	provider_attribute,
	
	visit_attribute,
	
	location,
	
	encounter_diagnosis,
	
	conditions,
	
	person_name,
	
	allergy,
	
	person_address,
	
	patient_identifier,
	
	orders,
	
	drug_order,
	
	test_order,
	
	users,
	
	relationship,
	
	provider,
	
	encounter_provider,
	
	gaac,
	
	gaac_member,
	
	gaac_family,
	
	gaac_family_member,
	
	clinicalsummary_usage_report,
	
	esaudefeatures_rps_import_log;
	
	public static Set<String> getTablesToInclude() {
		return Arrays.stream(TableToWatch.values()).map(TableToWatch::name).collect(Collectors.toSet());
	}
}
