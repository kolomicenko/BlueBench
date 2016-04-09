package com.tinkerpop.bench.benchmark;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.BenchRunner;
import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.LogUtils;
import com.tinkerpop.bench.log.SummaryLogWriter;
import com.tinkerpop.bench.operationFactory.OperationFactory;
import com.tinkerpop.bench.operationFactory.OperationFactoryLog;
import cz.cuni.mff.bluebench.DB;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public abstract class Benchmark {

	private String log = null;

	private GraphDescriptor logCreator = null;

	public Benchmark(DB logCreator) {
		this.logCreator = GraphDescriptor.createDescriptorFromDbInfo(logCreator);
                
                try {
                        this.run();
                } catch (IOException ex) {
                        Logger.getLogger(Benchmark.class.getName()).log(Level.SEVERE, null, ex);
                }
	}

	protected abstract ArrayList<OperationFactory> getOperationFactories();
        
        protected abstract String getName();
        
        protected abstract List<DB> getTestedDbs();
        
        protected abstract List<String> getInputFiles();
        
        private void createOperationLogs() throws Exception {
		try {
			BenchRunner benchRunner = new BenchRunner(logCreator,
					new File(log), getOperationFactories());

			benchRunner.startBench();
		} catch (Exception e) {
			throw e;
		}
	}

	private void loadOperationLogs(GraphDescriptor graphDescriptor,
			String logOut) throws Exception {
		if (new File(log).exists() == false) {
			createOperationLogs();
                }

		OperationFactory operationFactory = new OperationFactoryLog(new File(
				log));

		BenchRunner benchRunner = new BenchRunner(graphDescriptor, new File(
				logOut), operationFactory);

		benchRunner.startBench();
	}
        
        private void run() throws IOException {
                String dirResults = Bench.benchProperties
                        .getProperty(Bench.RESULTS_DIRECTORY)
                        + this.getName() + "/";
                
                this.log = dirResults + this.getName() + ".csv";
                
                LogUtils.deleteDir(dirResults);
                
                LinkedHashMap<String, String> resultFiles = new LinkedHashMap<>();
                
                int dbCount = this.getTestedDbs().size();
                int position = 0;
                for (DB db : this.getTestedDbs()) {
                        GraphDescriptor graphDescriptor = GraphDescriptor.createDescriptorFromDbInfo(db);
                        
                        System.out.printf("%s/%s (%d/%d)", this.getName(), db.name, ++position, dbCount).println();
                        
                        try {
                                // Load operation logs with the current DB
                                this.loadOperationLogs(graphDescriptor, dirResults + this.getName() + "_" + db.name + ".csv");
                        } catch (Exception ex) {
                                Logger.getLogger(Benchmark.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        resultFiles.put(db.name, dirResults + this.getName() + '_' + db.name + ".csv");
                }
                
                // Create file with summarized results from all databases and operations
                LogUtils.makeResultsSummary(dirResults + this.getName() + "_summary.csv",
                        resultFiles, SummaryLogWriter.TIME_SUMMARY);
                LogUtils.makeResultsSummary(dirResults + this.getName() + "_memory.csv",
                        resultFiles, SummaryLogWriter.MEMORY_SUMMARY);
                LogUtils.makeResultsSummary(dirResults + this.getName() + "_teps.csv",
                        resultFiles, SummaryLogWriter.TEPS_SUMMARY);
        }

}