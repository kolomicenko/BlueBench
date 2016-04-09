package com.tinkerpop.bench.operation;

import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.StatisticsHelper;
import com.tinkerpop.blueprints.Graph;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public abstract class Operation{

	private int opId = -1;
	private String[] args = null;
	private long time = -1l;
        private long memory = -1l;
	private Object result = null;
	private GraphDescriptor graphDescriptor = null;
	private String name = null;

	/*
	 * Setter Methods
	 */

	public final void setId(int opId) {
		this.opId = opId;
	}

	public final void setArgs(String[] args) {
		this.args = args;
	}

	public final void setName(String name) {
		this.name = name;
	}

	protected final void setResult(Object result) {
		this.result = result;
	}
        
        public final void setGraphDescriptor(GraphDescriptor graphDescriptor) {
                this.graphDescriptor = graphDescriptor;
        }

	/*
	 * Getter Methods
	 */

	public final int getId() {
		return opId;
	}

	public final String[] getArgs() {
		return args;
	}

	public final long getTime() {
		return time;
	}
        
        public final long getMemory() {
		return memory;
	}

	public final Object getResult() {
		return result;
	}

	protected final GraphDescriptor getGraphDescriptor() {
		return graphDescriptor;
	}

	protected final Graph getGraph() {
		return graphDescriptor.getGraph();
	}

	public final String getName() {
		return name;
	}

	public final String getType() {
		return getClass().getName();
	}

	/*
	 * Event Methods
	 */

	public final void initialize(GraphDescriptor graphDescriptor) {
		this.graphDescriptor = graphDescriptor;
		onInitialize(args);
	}

	public final void execute() throws Exception {
                StatisticsHelper.stopMemoryWatch(StatisticsHelper.STOPWATCH_START);
		StatisticsHelper.stopWatch(StatisticsHelper.STOPWATCH_START);
		onExecute();
		time = StatisticsHelper.stopWatch(StatisticsHelper.STOPWATCH_STOP);
                memory = StatisticsHelper.stopMemoryWatch(StatisticsHelper.STOPWATCH_STOP);
	}
        
        public final void conclude() {
                onConclude();
        }

	protected abstract void onInitialize(String[] args);

	protected abstract void onExecute() throws Exception;
        
        protected void onConclude() {}
        
        public static String createTag(String graphFilename) {
                return graphFilename.split("_", 2)[0];
        }
}