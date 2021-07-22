/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.debezium;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
import org.openmrs.module.debezium.mysql.MySqlSnapshotLockMode;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;
import org.openmrs.module.debezium.mysql.MySqlSslMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebeziumActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumActivator.class);
	
	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("Debezium module started");
		
		AdministrationService adminService = Context.getAdministrationService();
		//TODO support postgres
		MySqlDebeziumConfig config = new MySqlDebeziumConfig();
		
		String userGp = adminService.getGlobalProperty(DebeziumConstants.GP_USER);
		if (StringUtils.isNotBlank(userGp)) {
			config.setUsername(userGp);
		} else {
			config.setUsername(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_USERNAME));
		}
		
		String passGp = adminService.getGlobalProperty(DebeziumConstants.GP_PASSWORD);
		if (StringUtils.isNotBlank(passGp)) {
			config.setPassword(passGp);
		} else {
			config.setPassword(Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_PASSWORD));
		}
		
		String includeGp = adminService.getGlobalProperty(DebeziumConstants.GP_TABLES_TO_INCLUDE);
		if (StringUtils.isNotBlank(includeGp)) {
			config.setTablesToInclude(Arrays.stream(includeGp.split(",")).collect(toSet()));
		}
		String excludeGp = adminService.getGlobalProperty(DebeziumConstants.GP_TABLES_TO_EXCLUDE);
		if (StringUtils.isNotBlank(excludeGp)) {
			config.setTablesToExclude(Arrays.stream(excludeGp.split(",")).collect(toSet()));
		}
		
		String snapshotModeGp = adminService.getGlobalProperty(DebeziumConstants.GP_SNAPSHOT_MODE);
		if (StringUtils.isNotBlank(snapshotModeGp)) {
			config.setSnapshotMode(MySqlSnapshotMode.valueOf(snapshotModeGp));
		}
		
		String sslModeGp = adminService.getGlobalProperty(DebeziumConstants.GP_SSL_MODE);
		if (StringUtils.isNotBlank(sslModeGp)) {
			config.setSslMode(MySqlSslMode.valueOf(sslModeGp));
		}
		
		String snapshotLockGp = adminService.getGlobalProperty(DebeziumConstants.GP_SNAPSHOT_LOCK_MODE);
		if (StringUtils.isNotBlank(snapshotLockGp)) {
			config.setSnapshotLockMode(MySqlSnapshotLockMode.valueOf(snapshotLockGp));
		}
		
		String jdbcUrl = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_URL);
		config.setHost(null);
		config.setPort(null);
		config.setDatabaseName("");
		config.setHistoryFilename(adminService.getGlobalProperty(DebeziumConstants.GP_HISTORY_FILE));
		config.setOffsetStorageFilename(adminService.getGlobalProperty(DebeziumConstants.GP_OFFSET_STORAGE_FILE));
		config.setConsumer(new DebeziumChangeConsumer(null));
		
		OpenmrsDebeziumEngine.getInstance().start(config);
	}
	
	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		log.info("Debezium module stopped");
	}
	
	/**
	 * @see BaseModuleActivator#willStop()
	 */
	@Override
	public void willStop() {
		if (log.isInfoEnabled()) {
			log.info("Stopping debezium engine");
		}
		
		OpenmrsDebeziumEngine.stop();
	}
	
}
