package com.tinkerpop.bench.evaluators;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.Direction;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public class EvaluatorInDegree extends Evaluator {

	@Override
	@SuppressWarnings("unused")
	public double evaluate(Vertex vertex) {
		double inDegree = 0;
		for (Edge edge : vertex.getEdges(Direction.IN)) {
			inDegree++;
		}
		return inDegree;
	}
}