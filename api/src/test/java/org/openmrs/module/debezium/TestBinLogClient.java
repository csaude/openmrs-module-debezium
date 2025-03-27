package org.openmrs.module.debezium;

import com.github.shyiko.mysql.binlog.event.Event;

import io.debezium.connector.binlog.BinlogStreamingChangeEventSource.BinlogPosition;
import org.openmrs.module.debezium.listener.BaseBinlogClient;

public class TestBinLogClient extends BaseBinlogClient {
	
	public TestBinLogClient() {
		this(null);
	}
	
	public TestBinLogClient(BinlogPosition binlogPosition) {
		super(binlogPosition);
	}
	
	@Override
	public void onEvent(Event event) {
	}
	
}
