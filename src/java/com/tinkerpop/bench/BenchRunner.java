package com.tinkerpop.bench;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.tinkerpop.bench.log.OperationLogWriter;
import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.bench.operation.DoGC;
import com.tinkerpop.bench.operation.OpenGraph;
import com.tinkerpop.bench.operation.ShutdownGraph;
import com.tinkerpop.bench.operationFactory.OperationFactory;
import com.tinkerpop.bench.operationFactory.OperationFactoryGeneric;
import com.tinkerpop.bench.operationFactory.OperationFactoryLog;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public class BenchRunner {
	private OperationLogWriter logWriter = null;

	private GraphDescriptor graphDescriptor = null;

	private ArrayList<OperationFactory> operationFactories = null;

	public BenchRunner(GraphDescriptor graphDescriptor, File logFile,
			final OperationFactory operationFactory) throws IOException {
		this(graphDescriptor, logFile, new ArrayList<OperationFactory>() {
			private static final long serialVersionUID = -6151422065314229327L;

			{
				add(operationFactory);
			}
		});
	}

	public BenchRunner(GraphDescriptor graphDescriptor, File logFile,
			ArrayList<OperationFactory> operationFactories) throws IOException {
		this.graphDescriptor = graphDescriptor;
		this.operationFactories = operationFactories;

		logWriter = LogUtils.getOperationLogWriter(logFile);
	}

	public void startBench() throws Exception {
		try {
			int startingOpId = -1;

			OperationFactory openFactory = new OperationFactoryGeneric(
					OpenGraph.class);

			OperationFactory shutdownFactory = new OperationFactoryGeneric(
					ShutdownGraph.class);

			OperationFactory gcFactory = new OperationFactoryGeneric(
					DoGC.class);

			for (OperationFactory operationFactory : operationFactories) {
				if (operationFactory instanceof OperationFactoryLog == false) {
					// Flush cache: open/close before/after each factory
					Operation openOperation = openFactory.next();
					openOperation.setId(++startingOpId);
					openOperation.initialize(graphDescriptor);
					openOperation.execute();
					logWriter.logOperation(openOperation);
				}

				operationFactory.initialize(graphDescriptor, startingOpId);

				System.out.println(operationFactory.getClass().getSimpleName());

				for (Operation operation : operationFactory) {

					operation.initialize(graphDescriptor);
                                        
                                        if (operation.getId() % 10 == 0) {
                                                System.out.println(graphDescriptor.getGraphType().getSimpleName());
                                        }

					System.out.printf("\tOperation[%d] Type[%s]...", operation
							.getId(), operation.getName()).println();
                                        
					operation.execute();
                                        operation.conclude();

					System.out.println("\t\t...Complete");

					logWriter.logOperation(operation);
				}

				startingOpId = operationFactory.getCurrentOpId();

				if (operationFactory instanceof OperationFactoryLog == false) {
					// Flush cache: open/close before/after each factory
					Operation shutdownOperation = shutdownFactory.next();
					shutdownOperation.setId(++startingOpId);
					shutdownOperation.initialize(graphDescriptor);
					shutdownOperation.execute();
					logWriter.logOperation(shutdownOperation);

					// Try to Garbage Collect
					Operation gcOperation = gcFactory.next();
					gcOperation.setId(++startingOpId);
					gcOperation.initialize(graphDescriptor);
					gcOperation.execute();
					logWriter.logOperation(gcOperation);
				}
			}

			graphDescriptor.shutdownGraph();

			logWriter.close();
		} catch (Exception e) {
			throw e;
		}
	}

}