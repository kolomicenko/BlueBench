# Generate Artificial Graphs
# Alex Averbuch (alex.averbuch@gmail.com), Sidney Shaw

from igraph import *
from pyjavaproperties import Properties
from random import *

p = Properties()
p.load(open('../java/com/tinkerpop/bench/bench.properties'))

# prior to generating graphs, at least these values in /src/java/com/tinkerpop/bench/bench.properties must be configured
# bench.graph.barabasi.vertices
# bench.graph.barabasi.degree

degree = int(p['bench.graph.barabasi.degree'])
vertices = int(p['bench.graph.barabasi.vertices'])
#hasProperties = int(p['bench.graph.barabasi.has_properties'])

colors = (p['bench.graph.property.colors']).split(',');
names = (p['bench.graph.property.names']).split(',');

g = Graph.Barabasi(n=vertices, m=degree, power=1, directed=True, zero_appeal=8)

# the version of the graph without any properties
for v in g.vs:
    g.vs[v.index][p['bench.graph.property.id']] = "node:n" + str(v.index)
for e in g.es:
    g.es[e.index][p['bench.graph.property.id']] = "e" + str(e.index)
for e in g.es:
    if random() < 0.5:
        g.es[e.index][p['bench.graph.label']] = p['bench.graph.label.friend']
    else:
        g.es[e.index][p['bench.graph.label']] = p['bench.graph.label.family']

g.write_graphml('../../' + p['bench.datasets.directory'] +
                '/' + str(vertices) + 'x' + str(degree) + p['bench.graph.barabasi.no_properties.file'])

# the version of the graph with properties
for v in g.vs:
    g.vs[v.index][p['bench.graph.property.age']] = randrange(100)
    g.vs[v.index][p['bench.graph.property.name']] = names[randrange(len(names))]
for e in g.es:
    g.es[e.index][p['bench.graph.property.weight']] = randrange(1000)
    g.es[e.index][p['bench.graph.property.color']] = colors[randrange(len(colors))]

g.write_graphml('../../' + p['bench.datasets.directory'] +
                '/' + str(vertices) + 'x' + str(degree) + p['bench.graph.barabasi.with_properties.file'])

