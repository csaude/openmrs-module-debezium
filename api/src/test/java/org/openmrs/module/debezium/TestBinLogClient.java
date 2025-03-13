package org.openmrs.module.debezium;

import com.github.shyiko.mysql.binlog.event.Event;

import io.debezium.connector.mysql.legacy.BinlogReader;

public class TestBinLogClient extends BaseBinlogClient {
	
	public TestBinLogClient() {
		this(null);
	}
	
	public TestBinLogClient(BinlogReader.BinlogPosition binlogPosition) {
		super(binlogPosition);
	}
	
	@Override
	public void onEvent(Event event) {
	}
	
}
