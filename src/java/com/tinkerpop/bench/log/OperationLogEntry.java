package com.tinkerpop.bench.log;

public class OperationLogEntry {

	private int opId = -1;
	private String name = null;
	private String type = null;
	private String[] args = null;
	private long time = -1;
        private long memory = -1;
	private String result = null;

	public OperationLogEntry(int opId, String name, String type, String[] args,
			long time, long memory, String result) {
		super();
		this.opId = opId;
		this.name = name;
		this.type = type;
		this.args = args;
		this.time = time;
                this.memory = memory;
		this.result = result;
	}

	public int getOpId() {
		return opId;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String[] getArgs() {
		return args;
	}

	public long getTime() {
		return time;
	}
        
        public long getMemory() {
		return memory;
	}

	public String getResult() {
		return result;
	}

}
