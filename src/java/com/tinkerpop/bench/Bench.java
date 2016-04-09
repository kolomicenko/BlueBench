package com.tinkerpop.bench;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Bench {

	public static Logger logger = Logger.getLogger(Bench.class);
	public static Properties benchProperties = new Properties();

	// DATASETS - GraphML & Databases
	public static final String DATASETS_DIRECTORY = "bench.datasets.directory";

	// LOGS - Operation Logs
	public static final String LOGS_DELIMITER = "bench.logs.delimiter";

	// RESULTS - Logs, Summaries, Plots
	public static final String RESULTS_DIRECTORY = "bench.results.directory";
        
        // DB DATA - internal database files of the systems with persistent storage
	public static final String DB_DATA_DIRECTORY = "bench.dbdata.directory";

	// GRAPH GENERAL
	public static final String GRAPH_PROPERTY_ID = "bench.graph.property.id";
	public static final String GRAPH_LABEL = "bench.graph.label";
	public static final String GRAPH_LABEL_FAMILY = "bench.graph.label.family";
	public static final String GRAPH_LABEL_FRIEND = "bench.graph.label.friend";
        public static final String GRAPH_PROPERTY_WEIGHT = "bench.graph.property.weight";
        public static final String GRAPH_PROPERTY_COLOR = "bench.graph.property.color";
        public static final String GRAPH_PROPERTY_COLORS = "bench.graph.property.colors";
        public static final String GRAPH_PROPERTY_AGE = "bench.graph.property.age";
        public static final String GRAPH_PROPERTY_NAME = "bench.graph.property.name";
        public static final String GRAPH_PROPERTY_NAMES = "bench.graph.property.names";

	// GRAPH FILES
	public static final String GRAPHML_BARABASI = "bench.graph.barabasi.file";
        
        // DEX CFG FILE
        public static final String DEX_CFG_PATH = "bench.dex.cfgFilePath";
        
        // MySQL configuration
        public static final String MYSQL_SERVER = "bench.mysql.server";
        public static final String MYSQL_USER = "bench.mysql.user";
        public static final String MYSQL_PASSWD = "bench.mysql.passwd";
        
        // INFINITE GRAPH database name
        public static final String IG_DB_NAME = "bench.ig.dbName";
        public static final String IG_CFG_PATH = "bench.ig.cfgFilePath";
        
        public static final int REPETITIONS = 10;

	static {
		try {
			benchProperties.load(Bench.class
					.getResourceAsStream("bench.properties"));
			System.out.println(benchProperties);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(Bench.class
				.getResource("log4j.properties"));
	}

}
