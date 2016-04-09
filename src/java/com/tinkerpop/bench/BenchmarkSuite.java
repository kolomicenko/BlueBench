package com.tinkerpop.bench;

import cz.cuni.mff.bluebench.benchmark.IndexingBenchmark;
import cz.cuni.mff.bluebench.benchmark.LabeledGraphBenchmark;
import cz.cuni.mff.bluebench.benchmark.PropertyGraphBenchmark;
import java.lang.reflect.Field;

public class BenchmarkSuite {
	public static void main(String[] args) throws Exception {
		// BenchmarkReadWriteVersusSize.run();
		// BenchmarkEchoVersusDepth.run();
                
                // for debugging + the path seems to be ignored when this is run solely by ant
                System.setProperty("java.library.path", System.getProperty("java.library.path") + ":/opt/InfiniteGraph/3.0/lib");
                
                Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
                fieldSysPath.setAccessible( true );
                fieldSysPath.set( null, null );

                IndexingBenchmark.run();
                PropertyGraphBenchmark.run();
                LabeledGraphBenchmark.run();
	}
}
