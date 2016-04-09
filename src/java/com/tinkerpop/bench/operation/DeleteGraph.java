package com.tinkerpop.bench.operation;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class DeleteGraph extends Operation {

	@Override
	protected void onInitialize(String[] args) {
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			getGraphDescriptor().deleteGraph();
			setResult("DONE");
		} catch (Exception e) {
			throw e;
		}
	}

}
