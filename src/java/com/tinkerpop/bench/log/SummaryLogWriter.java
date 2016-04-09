package com.tinkerpop.bench.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tinkerpop.bench.LogUtils;
import java.util.List;

public class SummaryLogWriter {
	private final String logDelim = LogUtils.LOG_DELIMITER;
        
        public static final int TIME_SUMMARY = 1;
        public static final int MEMORY_SUMMARY = 2;
        public static final int TEPS_SUMMARY = 3;
        
        private int summaryType;
        
        public SummaryLogWriter(int summaryType) {
                this.summaryType = summaryType;
        }

	/*
	 * summaryFilePath = "path/to/summary/file.csv"
	 * 
	 * resultFiles = ["graphName"->"path/to/result/file.csv"]
	 */
	public void writeSummary(String summaryFilePath,
			Map<String, String> resultFilePaths) throws IOException {
		// summarizedResults = ["operation" -> ["graphRuntimes"]]
		LinkedHashMap<String, ArrayList<GraphRunTimes>> summarizedFiles = summarizeFiles(resultFilePaths);

		writeSummaryFile(summaryFilePath, summarizedFiles);
	}

	private LinkedHashMap<String, ArrayList<GraphRunTimes>> summarizeFiles(
			Map<String, String> resultFilePaths) {
		// summarizedResults = ["operation" -> ["graphRuntimes"]]
		LinkedHashMap<String, ArrayList<GraphRunTimes>> resultFiles = new LinkedHashMap<String, ArrayList<GraphRunTimes>>();

		// Get total time taken for each operation, for each result file
		for (Entry<String, String> fileEntry : resultFilePaths.entrySet()) {

			String graphName = fileEntry.getKey();
			String path = fileEntry.getValue();

			// Load Operations' runtimes from 1 .csv (for 1 Graph) into memory
			// fileOperationTimes= ["operation" -> "graphRuntimes"]
			LinkedHashMap<String, GraphRunTimes> fileOperationTimes = getFileOperationTimes(
					graphName, path);

			for (Entry<String, GraphRunTimes> fileOperationTimesEntry : fileOperationTimes
					.entrySet()) {
				String opType = fileOperationTimesEntry.getKey();
				GraphRunTimes opTimes = fileOperationTimesEntry.getValue();

				ArrayList<GraphRunTimes> opResults = resultFiles.get(opType);

				if (opResults == null) {
					opResults = new ArrayList<GraphRunTimes>();
					resultFiles.put(opType, opResults);
				}
				opResults.add(opTimes);
			}
		}

		return resultFiles;
	}

	private LinkedHashMap<String, GraphRunTimes> getFileOperationTimes(
			String graphName, String path) {
		// summarizedResults = ["operation" -> "graphRuntimes"]
		LinkedHashMap<String, GraphRunTimes> fileOperationTimes = new LinkedHashMap<String, GraphRunTimes>();

		OperationLogReader reader = new OperationLogReader(new File(path));

		for (OperationLogEntry opLogEntry : reader) {
                        String opName = opLogEntry.getName();
                        if (this.summaryType == TEPS_SUMMARY && !this.isTraversal(opName)) {
                                continue;
                        }
                        
			GraphRunTimes graphRunTimes = fileOperationTimes.get(opName);

			if (graphRunTimes == null)
				graphRunTimes = new GraphRunTimes(graphName);

			fileOperationTimes.put(opName, graphRunTimes);

                        switch (this.summaryType) {
                                case TIME_SUMMARY:
                                        fileOperationTimes.get(opName).add(opLogEntry.getTime());
                                        break;
                                case MEMORY_SUMMARY:
                                        fileOperationTimes.get(opName).add(opLogEntry.getMemory());
                                        break;
                                case TEPS_SUMMARY:
                                        double time = opLogEntry.getTime() > 0 ? opLogEntry.getTime() : 0.25;
                                        long teps = (long) (Long.parseLong(opLogEntry.getResult()) * 1000 / time);
                                        if (teps > 0) {
                                                fileOperationTimes.get(opName).add(teps);
                                        }
                                        break;
                        }
		}

		return fileOperationTimes;
	}

	// summarizedResults = ["operation" -> ["graphRunTime"]]
	private void writeSummaryFile(String summaryFilePath,
			LinkedHashMap<String, ArrayList<GraphRunTimes>> summarizedResults)
			throws IOException {

		File summaryFile = new File(summaryFilePath);
		(new File(summaryFile.getParent())).mkdirs();

		BufferedWriter bufferedLogWriter = new BufferedWriter(new FileWriter(
				new File(summaryFilePath)));

		// write .csv column headers
		bufferedLogWriter.write("operation");
		bufferedLogWriter.write(logDelim);

		for (Entry<String, ArrayList<GraphRunTimes>> opGraphRunTimes : summarizedResults
				.entrySet()) {
			Collections.sort(opGraphRunTimes.getValue());
			for (GraphRunTimes graphRunTimes : opGraphRunTimes.getValue()) {
				bufferedLogWriter.write(graphRunTimes.getGraphName() + "-mean");
				bufferedLogWriter.write(logDelim);
				bufferedLogWriter.write(graphRunTimes.getGraphName() + "-stdev");
				bufferedLogWriter.write(logDelim);
				bufferedLogWriter.write(graphRunTimes.getGraphName() + "-min");
				bufferedLogWriter.write(logDelim);
				bufferedLogWriter.write(graphRunTimes.getGraphName() + "-max");
				bufferedLogWriter.write(logDelim);
			}
			break;
		}

		bufferedLogWriter.newLine();

		// write .csv column data
		for (Entry<String, ArrayList<GraphRunTimes>> opGraphRunTimes : summarizedResults
				.entrySet()) {
			bufferedLogWriter.write(opGraphRunTimes.getKey());
			bufferedLogWriter.write(logDelim);

			Collections.sort(opGraphRunTimes.getValue());
			for (GraphRunTimes graphRunTimes : opGraphRunTimes.getValue()) {
                                String formattedMean = "";
                                String formattedStdev = "";
                                String formattedMin = "";
                                String formattedMax = "";
                                switch (this.summaryType) {
                                        case TIME_SUMMARY:
                                        case TEPS_SUMMARY:
                                                formattedMean = graphRunTimes.getMean().toString();
                                                formattedStdev = graphRunTimes.getStdev().toString();
                                                formattedMin = graphRunTimes.getMin().toString();
                                                formattedMax = graphRunTimes.getMax().toString();
                                                break;
                                        case MEMORY_SUMMARY:
                                                formattedMean = LogUtils.humanReadableSize(graphRunTimes.getMean());
                                                formattedStdev = LogUtils.humanReadableSize(graphRunTimes.getStdev());
                                                formattedMin = LogUtils.humanReadableSize(graphRunTimes.getMin());
                                                formattedMax = LogUtils.humanReadableSize(graphRunTimes.getMax());
                                                break;
                                } 
                                
				bufferedLogWriter.write(formattedMean);
				bufferedLogWriter.write(logDelim);
				bufferedLogWriter.write(formattedStdev);
				bufferedLogWriter.write(logDelim);
				bufferedLogWriter.write(formattedMin);
				bufferedLogWriter.write(logDelim);
				bufferedLogWriter.write(formattedMax);
				bufferedLogWriter.write(logDelim);
			}

			bufferedLogWriter.newLine();
		}

		bufferedLogWriter.flush();
		bufferedLogWriter.close();
	}

	// Encapsulates the run times for one Graph & one Operation
	private class GraphRunTimes implements Comparable<GraphRunTimes> {
		private String graphName = null;
		private ArrayList<Long> runTimes = new ArrayList<Long>();
		private Double mean = null;
		private Double stdev = null;
		private Double min = null;
		private Double max = null;

		public GraphRunTimes(String graphName) {
			this.graphName = graphName;
		}

		public void add(long runTime) {
			mean = null;
			stdev = null;
			min = null;
			max = null;
			runTimes.add(runTime);
		}

		public String getGraphName() {
			return graphName;
		}

		public Double getMean() {
			return (null == mean) ? calcMean() : mean;
		}

		public Double getStdev() {
			return (null == stdev) ? calcStdev() : stdev;
		}

		public Double getMin() {
			return (null == min) ? calcMin() : min;
		}

		public Double getMax() {
			return (null == max) ? calcMax() : max;
		}

		private double calcMean() {
                        Collections.sort(runTimes);
                        int size = runTimes.size();
                        int marginSize = (int) Math.floor(size * 0.2);
                        
                        List<Long> trimmedSample = runTimes.subList(marginSize, size - marginSize) ;
                       
			double runTimesSum = 0;
			for (Long runTime : trimmedSample)
				runTimesSum += runTime;

			return (mean = runTimesSum / (double) trimmedSample.size());
		}

		private double calcStdev() {
			mean = getMean();

			double diffFromMeanSum = 0;
			for (Long runTime : runTimes)
				diffFromMeanSum += Math.pow(runTime - mean, 2);

			return (stdev = Math.sqrt(diffFromMeanSum
					/ (double) runTimes.size()));
		}

		private double calcMin() {
			calcMinMax();
			return min;
		}

		private double calcMax() {
			calcMinMax();
			return max;
		}

		private void calcMinMax() {
			min = Double.MAX_VALUE;
			max = -1d;

			for (Long runTime : runTimes) {
				min = (runTime < min) ? runTime : min;
				max = (runTime > max) ? runTime : max;
			}
		}

		@Override
		public int compareTo(GraphRunTimes otherGraphName) {
			return this.graphName.compareTo(otherGraphName.getGraphName());
		}
	}
        
        private boolean isTraversal(String opName) {
                return opName.startsWith("Traversal");
        }

}
