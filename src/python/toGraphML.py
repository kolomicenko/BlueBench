# Convert an edgelist to graphML
# author Sidney Shaw

from igraph import *
from pyjavaproperties import Properties
from random import *

p = Properties()
p.load(open('../java/com/tinkerpop/bench/bench.properties'))

g = Graph.Read_Edgelist("")  # here specify the file to be converted

for v in g.vs:
    g.vs[v.index][p['bench.graph.property.id']] = "node:n" + str(v.index)
for e in g.es:
    g.es[e.index][p['bench.graph.property.id']] = "e" + str(e.index)
for e in g.es:
    g.es[e.index][p['bench.graph.label']] = p['bench.graph.label.friend']

g.write_graphml('../../' + p['bench.datasets.directory'] +
                '/amazon.graphml')
