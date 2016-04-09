package cz.cuni.mff.bluebench.utils;

import com.tinkerpop.blueprints.extensions.io.GraphProgressListener;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLTokens;
import cz.cuni.mff.bluebench.operation.GraphFiller;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 * GraphMLReader writes the data from a GraphML stream to a graph. Slightly modified code from Blueprints extensions.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Peter Macko (http://eecs.harvard.edu/~pmacko)
 * @author Sidney Shaw (modified the code to reflect the needs of BlueBench)
 */
public class GraphMLReader {

        private String vertexIdKey = null;
        private String edgeIdKey = null;
        private String edgeLabelKey = null;
        private GraphProgressListener progressListener = null;
        private boolean ingestAsUndirected = false;

        /**
         * @param vertexIdKey if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
         */
        public void setVertexIdKey(String vertexIdKey) {
                this.vertexIdKey = vertexIdKey;
        }

        /**
         * @param edgeIdKey if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
         */
        public void setEdgeIdKey(String edgeIdKey) {
                this.edgeIdKey = edgeIdKey;
        }

        /**
         * @param edgeLabelKey if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
         */
        public void setEdgeLabelKey(String edgeLabelKey) {
                this.edgeLabelKey = edgeLabelKey;
        }

        /**
         * @param progressListener the progress listener.
         */
        public void setProgressListener(GraphProgressListener progressListener) {
                this.progressListener = progressListener;
        }

        /**
         * @param ingestAsUndirected if true, ingest a directed graph as an undirected graph by doubling-up all edges
         */
        public void setIngestAsUndirected(boolean ingestAsUndirected) {
                this.ingestAsUndirected = ingestAsUndirected;
        }

        /**
         * Input the GraphML stream data into the graph. In practice, usually the provided graph is empty.
         *
         * @param graphMLInputStream an InputStream of GraphML data
         * @throws IOException thrown when the GraphML data is not correctly formatted
         */
        public Map<Integer, Object> inputGraph(final InputStream graphMLInputStream, final GraphFiller filler) throws IOException {
                return inputGraph(graphMLInputStream, filler, 1000, this.vertexIdKey, this.edgeIdKey,
                        this.edgeLabelKey, this.progressListener, this.ingestAsUndirected);
        }

        /**
         * Input the GraphML stream data into the graph. In practice, usually the provided graph is empty.
         *
         * @param graphMLInputStream an InputStream of GraphML data
         * @param bufferSize the amount of elements to hold in memory before committing a transactions (only valid for
         * TransactionalGraphs)
         * @throws IOException thrown when the GraphML data is not correctly formatted
         */
        public Map<Integer, Object> inputGraph(final InputStream graphMLInputStream, final GraphFiller filler, int bufferSize) throws IOException {
                return inputGraph(graphMLInputStream, filler, bufferSize, this.vertexIdKey, this.edgeIdKey,
                        this.edgeLabelKey, this.progressListener, this.ingestAsUndirected);
        }

        /**
         * Input the GraphML stream data into the graph. More control over how data is streamed is provided by this
         * method.
         *
         * @param graphMLInputStream an InputStream of GraphML data
         * @param bufferSize the amount of elements to hold in memory before committing a transactions (only valid for
         * TransactionalGraphs)
         * @param vertexIdKey if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
         * @param edgeIdKey if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
         * @param edgeLabelKey if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
         * @throws IOException thrown when the GraphML data is not correctly formatted
         */
        public static Map<Integer, Object> inputGraph(final InputStream graphMLInputStream, final GraphFiller filler, int bufferSize, String vertexIdKey,
                String edgeIdKey, String edgeLabelKey) throws IOException {
                return inputGraph(graphMLInputStream, filler, bufferSize, vertexIdKey, edgeIdKey, edgeLabelKey, null, false);
        }

        /**
         * Input the GraphML stream data into the graph. More control over how data is streamed is provided by this
         * method.
         *
         * @param graphMLInputStream an InputStream of GraphML data
         * @param bufferSize the amount of elements to hold in memory before committing a transactions (only valid for
         * TransactionalGraphs)
         * @param vertexIdKey if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
         * @param edgeIdKey if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
         * @param edgeLabelKey if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
         * @param progressListener the progress listener
         * @param ingestAsUndirected if true, ingest a directed graph as an undirected graph by doubling-up all edges
         * @throws IOException thrown when the GraphML data is not correctly formatted
         */
        public static Map<Integer, Object> inputGraph(final InputStream graphMLInputStream, final GraphFiller filler, int bufferSize,
                String vertexIdKey, String edgeIdKey, String edgeLabelKey, GraphProgressListener progressListener,
                boolean ingestAsUndirected) throws IOException {

                XMLInputFactory inputFactory = XMLInputFactory.newInstance();

                try {
                        XMLStreamReader reader = inputFactory.createXMLStreamReader(graphMLInputStream);

                        Map<String, String> keyIdMap = new HashMap<String, String>();
                        Map<String, String> keyTypesMaps = new HashMap<String, String>();
                        // <Mapped ID String, ID Object>

                        // mapping between desired and actual IDs of the vertices
                        Map<Integer, Object> vertexIdMap = new HashMap<>();
                        
                        Map<String, Object> vertexMap = new HashMap<String, Object>();
                        // Mapping between Source/Target IDs and "Property IDs"
                        // <Default ID String, Mapped ID String>
                        Map<String, String> vertexMappedIdMap = new HashMap<String, String>();

                        // Buffered Vertex Data
                        String vertexId = null;
                        Map<String, Object> vertexProps = new HashMap<String, Object>();
                        boolean inVertex = false;

                        // Buffered Edge Data
                        String edgeId = null;
                        String edgeLabel = null;
                        Object edgeInVertex = null;
                        Object edgeOutVertex = null;
                        Map<String, Object> edgeProps = new HashMap<String, Object>();
                        boolean inEdge = false;

                        // Counts
                        int numVertices = 0;
                        int numEdges = 0;
                        
                        int numAlreadyCommited = 0;

                        while (reader.hasNext()) {

                                Integer eventType = reader.next();
                                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                                        String elementName = reader.getName().getLocalPart();
                                        switch (elementName) {
                                                case GraphMLTokens.KEY:
                                                        {
                                                                String id = reader.getAttributeValue(null, GraphMLTokens.ID);
                                                                String attributeName = reader.getAttributeValue(null, GraphMLTokens.ATTR_NAME);
                                                                String attributeType = reader.getAttributeValue(null, GraphMLTokens.ATTR_TYPE);
                                                                keyIdMap.put(id, attributeName);
                                                                keyTypesMaps.put(attributeName, attributeType);
                                                                break;
                                                        }
                                                case GraphMLTokens.NODE:
                                                        vertexId = reader.getAttributeValue(null, GraphMLTokens.ID);
                                                        if (vertexIdKey != null) {
                                                                vertexMappedIdMap.put(vertexId, vertexId);
                                                        }
                                                        inVertex = true;
                                                        vertexProps.clear();
                                                        break;
                                                case GraphMLTokens.EDGE:
                                                        edgeId = reader.getAttributeValue(null, GraphMLTokens.ID);
                                                        edgeLabel = reader.getAttributeValue(null, GraphMLTokens.LABEL);
                                                        edgeLabel = edgeLabel == null ? GraphMLTokens._DEFAULT : edgeLabel;
                                                        String outVertexId = reader.getAttributeValue(null, GraphMLTokens.SOURCE);
                                                        String inVertexId = reader.getAttributeValue(null, GraphMLTokens.TARGET);
                                                        if (vertexIdKey == null) {
                                                                edgeOutVertex = vertexMap.get(outVertexId);
                                                                edgeInVertex = vertexMap.get(inVertexId);
                                                        } else {
                                                                edgeOutVertex = vertexMap.get(vertexMappedIdMap.get(outVertexId));
                                                                edgeInVertex = vertexMap.get(vertexMappedIdMap.get(inVertexId));
                                                        }
                                                        // Automatically create vertices if they do not already exist
//                                                        if (null == edgeOutVertex) {
//                                                                edgeOutVertex = outGraph.addVertex(outVertexId);
//                                                                vertexIdMap.put(numVertices++, edgeOutVertex.getId());
//                                                                vertexMap.put(outVertexId, edgeOutVertex);
//                                                                if (vertexIdKey != null) // Default to standard ID system (in case no mapped ID is found later)
//                                                                {
//                                                                        vertexMappedIdMap.put(outVertexId, outVertexId);
//                                                                }
//                                                        }
//                                                        if (null == edgeInVertex) {
//                                                                edgeInVertex = outGraph.addVertex(inVertexId);
//                                                                vertexIdMap.put(numVertices++, edgeInVertex.getId());
//                                                                vertexMap.put(inVertexId, edgeInVertex);
//                                                                if (vertexIdKey != null) // Default to standard ID system (in case no mapped ID is found later)
//                                                                {
//                                                                        vertexMappedIdMap.put(inVertexId, inVertexId);
//                                                                }
//                                                        }
                                                        inEdge = true;
                                                        edgeProps.clear();
                                                        break;
                                                case GraphMLTokens.DATA:
                                                        {
                                                                String key = reader.getAttributeValue(null, GraphMLTokens.KEY);
                                                                String attributeName = keyIdMap.get(key);
                                                                if (attributeName != null) {
                                                                        String value = reader.getElementText();

                                                                        if (inVertex) {
                                                                                if ((vertexIdKey != null) && (key.equals(vertexIdKey))) {
                                                                                        // Should occur at most once per Vertex
                                                                                        // Assumes single ID prop per Vertex
                                                                                        vertexMappedIdMap.put(vertexId, value);
                                                                                        vertexId = value;
                                                                                } else {
                                                                                        vertexProps.put(attributeName, typeCastValue(key, value, keyTypesMaps));
                                                                                }
                                                                        } else if (inEdge) {
                                                                                if ((edgeLabelKey != null) && (key.equals(edgeLabelKey))) {
                                                                                        edgeLabel = value;
                                                                                } else if ((edgeIdKey != null) && (key.equals(edgeIdKey))) {
                                                                                        edgeId = value;
                                                                                } else {
                                                                                        edgeProps.put(attributeName, typeCastValue(key, value, keyTypesMaps));
                                                                                }
                                                                        }
                                                                }
                                                                break;
                                                        }
                                        }
                                } else if (eventType.equals(XMLEvent.END_ELEMENT)) {
                                        String elementName = reader.getName().getLocalPart();
                                        switch (elementName) {
                                                case GraphMLTokens.NODE:
                                                        Object currentVertex = vertexMap.get(vertexId);
                                                        if (currentVertex != null) {
                                                                throw new RuntimeException("Duplicate vertex with the same ID: " + vertexId);
                                                        } 
                                                        
                                                        currentVertex = filler.addVertex(vertexId, vertexProps);
                                                        
                                                        vertexIdMap.put(numVertices++, filler.resolveVertexId(currentVertex));
                                                        vertexMap.put(vertexId, currentVertex);
                                                        
                                                        vertexId = null;
                                                        vertexProps.clear();
                                                        inVertex = false;
                                                        if (progressListener != null) {
                                                                if ((numVertices + numEdges) % 1000 == 0) {
                                                                        progressListener.graphProgress(numVertices, numEdges);
                                                                }
                                                        }
                                                        break;
                                                case GraphMLTokens.EDGE:
                                                        filler.addEdge(edgeId, edgeOutVertex, edgeInVertex, edgeLabel, edgeProps);
                                                        numEdges++;
                                                        
                                                        if (ingestAsUndirected) {
                                                                // Don't check whether the edge we are about to add already exists (we might need to revisit this)
                                                                filler.addEdge(edgeId + "_opposite", edgeInVertex, edgeOutVertex, edgeLabel, edgeProps);
                                                                numEdges++;
                                                        }
                                                        edgeId = null;
                                                        edgeLabel = null;
                                                        edgeInVertex = null;
                                                        edgeOutVertex = null;
                                                        inEdge = false;
                                                        edgeProps.clear();
                                                        if (progressListener != null) {
                                                                if ((numVertices + numEdges) % 1000 == 0) {
                                                                        progressListener.graphProgress(numVertices, numEdges);
                                                                }
                                                        }
                                                        break;
                                        }
                                }
                                
                                if (numVertices + numEdges - numAlreadyCommited > bufferSize) {
                                        numAlreadyCommited += bufferSize;
                                        filler.commit();
                                }
                                
                        }

                        reader.close();

                        if (progressListener != null) {
                                progressListener.graphProgress(numVertices, numEdges);
                        }
                        
                        filler.commit();

                        return vertexIdMap;
                } catch (XMLStreamException xse) {
                        throw new IOException(xse);
                }
        }

        private static Object typeCastValue(String key, String value, Map<String, String> keyTypes) {
                String type = keyTypes.get(key);
                if (null == type || type.equals(GraphMLTokens.STRING)) {
                        return value;
                } else if (type.equals(GraphMLTokens.FLOAT)) {
                        return Float.valueOf(value);
                } else if (type.equals(GraphMLTokens.INT)) {
                        return Integer.valueOf(value);
                } else if (type.equals(GraphMLTokens.DOUBLE)) {
                        return Double.valueOf(value);
                } else if (type.equals(GraphMLTokens.BOOLEAN)) {
                        return Boolean.valueOf(value);
                } else if (type.equals(GraphMLTokens.LONG)) {
                        return Long.valueOf(value);
                } else {
                        return value;
                }
        }
}
