package com.tinkerpop.bench;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.arangodb.ArangoDBGraph;
import cz.cuni.mff.bluebench.DB;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class GraphDescriptor {

	private String graphType = null;
	private String graphDir = null;
	private String graphPath = null;
	private Graph graph = null;
        private String graphCfgFile = null;
        private DB db = null;
        
        public static GraphDescriptor createDescriptorFromDbInfo(DB db) {
                GraphDescriptor graphDescriptor;
                if (db.isPersistent) {
                        graphDescriptor = new GraphDescriptor(
                                db.getGraphClass(), 
                                db.getGraphDir(), 
                                db.getGraphPath(), 
                                db.cfgPath,
                                db
                        );
                } else {
                        graphDescriptor = new GraphDescriptor(db.getGraphClass());
                }
                
                
                return graphDescriptor;
        }

	public GraphDescriptor(String graphType) {
		this(graphType, null, null, null, null);
	}
        
        public GraphDescriptor(String graphType, String graphDir, String graphPath) {
		this(graphType, graphDir, graphPath, null, null);
	}

	public GraphDescriptor(String graphType, String graphDir, String graphPath, String graphCfgFile, DB db) {
		this.graphType = graphType;
		this.graphDir = graphDir;
		this.graphPath = graphPath;
                this.graphCfgFile = graphCfgFile;
                this.db = db;
	}

	//
	// Getter Methods
	// 

	public Class<?> getGraphType() throws ClassNotFoundException {
		return Class.forName(graphType);
	}

	public Graph getGraph() {
		return graph;
	}

	public boolean getPersistent() {
		return graphPath != null;
	}

	//
	// Functionality
	//

	public Graph openGraph() throws Exception {
		if (null != graph) {
			return graph;
                }
                
                if (db != null) {
                        graph = db.createGraph(graphPath);
                        
                        if (graph != null) {
                                return graph;
                        }
                }

                ArrayList<String> args = new ArrayList<>();
                ArrayList<Class<?>> argsClasses = new ArrayList<>();
                
                if (graphPath != null) {
                        args.add(graphPath);
                        argsClasses.add(String.class);
                }
                
                if (graphCfgFile != null) {
                        args.add(graphCfgFile);
                        argsClasses.add(String.class);
                }
                
                Class<?>[] argsClassesArray     = argsClasses.toArray(new Class<?>[argsClasses.size()]);
		Constructor<?> graphConstructor = this.getGraphType().getConstructor(argsClassesArray);

		try {
			graph = (Graph) graphConstructor.newInstance(args.toArray());
		} catch (Exception e) {
                        System.out.println(graphPath);
                    
                        System.out.println(graphType);
                        
                        
                        e.printStackTrace();
                    
                    
			throw e;
		}

		return graph;
	}

	public void shutdownGraph() {
		if (null != graph) {
			graph.shutdown();
			graph = null;
		}
	}

	public void deleteGraph() {
                if (db != null) {
                        db.deleteGraph(graph);
                }
                
		shutdownGraph();
                
		if (true == getPersistent() && db != DB.Infinite) {
                        File graphFile = new File(graphDir);
                        if (graphFile.isDirectory()) {
                            deleteDir(graphDir);
                        } else {
                            graphFile.delete();
                        }
		}
	}

	private void deleteDir(String pathStr) {
		LogUtils.deleteDir(pathStr);
	}

}
