package com.tinkerpop.bench;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.tinkerpop.bench.log.OperationLogReader;
import com.tinkerpop.bench.log.OperationLogWriter;
import com.tinkerpop.bench.log.SummaryLogWriter;

public class LogUtils {

	public static final String LOG_DELIMITER = Bench.benchProperties
			.getProperty(Bench.LOGS_DELIMITER);

	public static void makeResultsSummary(String summaryFilePath,
			Map<String, String> resultFilePaths, int summaryType) throws IOException {
		SummaryLogWriter summaryLogWriter = new SummaryLogWriter(summaryType);
		summaryLogWriter.writeSummary(summaryFilePath, resultFilePaths);
	}

	public static OperationLogReader getOperationLogReader(File logFile) {
		return new OperationLogReader(logFile);
	}

	public static OperationLogWriter getOperationLogWriter(File logFile)
			throws IOException {
		return new OperationLogWriter(logFile);
	}

	// FIXME (new File(pathStr)).mkdirs(); seems to cause problems with Neo4j
	// for some reason...
	// public static void cleanDir(String pathStr) {
	// deleteDir(pathStr);
	// (new File(pathStr)).mkdirs();
	// }

	public static void deleteDir(String dirStr) {
		File dir = new File(dirStr);

		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.isDirectory())
					deleteDir(file.getAbsolutePath());
				else
					file.delete();
			}
			dir.delete();
		}
	}

	public static String pathToName(String filename) {
		int startName = (filename.lastIndexOf(File.separator) == -1) ? -1
				: filename.lastIndexOf(File.separator);
		int endName = (filename.lastIndexOf(".") == -1) ? filename.length()
				: filename.lastIndexOf(".");
		return filename.substring(startName + 1, endName);
	}

	public static String msToTimeStr(long msTotal) {
		long ms = msTotal % 1000;
		long s = (msTotal / 1000) % 60;
		long m = ((msTotal / 1000) / 60) % 60;
		long h = ((msTotal / 1000) / 60) / 60;

		return String.format("%d(h):%d(m):%d(s):%d(ms)", h, m, s, ms);
	}
        
        public static String humanReadableSize(double bytes) {
                return humanReadableSize(Math.round(bytes), false);
        }
        
        public static String humanReadableSize(long bytes) {
                return humanReadableSize(bytes, false);
        }
        
        // http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        public static String humanReadableSize(long bytes, boolean si) {
                int unit = si ? 1000 : 1024;
                if (bytes < unit) return bytes + " B";
                int exp = (int) (Math.log(bytes) / Math.log(unit));
                String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
                return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
}
