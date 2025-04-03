package org.openmrs.module.debezium.entity;

/**
 * This identify the type of event to facilitate debezium to filter when requested to share its data
 * D - Demographic F - Form G - Generic
 */
public enum EventType {
	D,
	F,
	G,
	
}
