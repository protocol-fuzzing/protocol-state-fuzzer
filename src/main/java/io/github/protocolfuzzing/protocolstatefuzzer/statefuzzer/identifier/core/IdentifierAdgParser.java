package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core;

import net.automatalib.exception.FormatException;
import net.automatalib.graph.Graph;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.graph.impl.CompactUniversalGraph;
import net.automatalib.serialization.ModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parses a DOT representation produced by {@code AdgWriter.renderDot(Node)} into
 * an {@link IdentifierAdg} tree.
 * <p>
 * The parser supports internal nodes and leaf nodes marked with
 * {@code shape=box}. Leaf nodes may contain comma-separated model names in the
 * DOT {@code models} attribute.
 */
public class IdentifierAdgParser {

    /**
     * Default constructor for {@link IdentifierAdgParser}
     */
    public IdentifierAdgParser() {}

    private static final Pattern COMMA = Pattern.compile(",");

    /**
     * Custom Node parser to parse the "models" attribute used in the ADG DOT representation
     */
    private static Set<String> nodeModelsParser(Map<String, String> attr) {
        String raw = attr.get("models");
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        Set<String> models = new LinkedHashSet<>();
        for (String token: COMMA.split(raw, -1)) {
            String t = token.trim();
            if (!t.isEmpty()) {
                models.add(t);
            }
        }
        return models;
    }

    /**
     * Parses a DOT file from disk into an {@link IdentifierAdg}.
     *
     * @param  dotFile               the File of the DOT representation
     *
     * @return                       parsed ADG
     *
     * @throws IOException           if the file cannot be read
     * @throws FormatException       if the format of the file is wrong
     * @throws IllegalStateException if the file does not contain a tree
     */
    public static IdentifierAdg parse(File dotFile) throws IOException, FormatException, IllegalStateException {
        ModelDeserializer<CompactUniversalGraph<Set<String>, String>> parser = DOTParsers.graph(
            IdentifierAdgParser::nodeModelsParser,
            DOTParsers.DEFAULT_EDGE_PARSER);

        CompactUniversalGraph<Set<String>, String> graph = null;
        try {
            graph = parser.readModel(dotFile);
        }
        catch (IOException e) {
            throw e;
        }
        catch (FormatException e) {
            throw e;
        }

        Integer root = findRoot(graph);

        IdentifierAdg.Node rootNode = new IdentifierAdg.Node(null);
        rootNode.updateModels(graph.getNodeProperty(root)); // usually null unless root is also a leaf

        buildTree(graph, root, rootNode, new HashSet<>());
        return new IdentifierAdg(rootNode);
    }

    /**
     * Finds the root node of a directed graph. The root node is defined as the node with no incoming edges.
     *
     * @param  <N>                   Node type
     * @param  <E>                   Edge type
     * @param  graph                 the directed graph to search for the root node
     *
     * @return                       the root node of the graph
     *
     * @throws IllegalStateException if the graph contains multiple roots or no root
     */
    private static <N, E> N findRoot(Graph<N, E> graph) throws IllegalStateException {
        Set<N> hasIncoming = new HashSet<>();
        for (N n: graph.getNodes()) {
            for (E e: graph.getOutgoingEdges(n)) {
                hasIncoming.add(graph.getTarget(e));
            }
        }
        N root = null;
        for (N n: graph.getNodes()) {
            if (!hasIncoming.contains(n)) {
                if (root != null) {
                    throw new IllegalStateException("multiple candidate roots found: " + root + ", " + n);
                }
                root = n;
            }
        }
        if (root == null) {
            throw new IllegalStateException("no root found (empty graph or a cycle)");
        }
        return root;
    }

    /**
     * Recursively builds the ADG
     *
     * @param  <N>                   Type of nodes in original graph
     * @param  <E>                   Type of edges in original graph
     * @param  graph                 Original graph
     * @param  node                  The node in the original graph
     * @param  target                The ADG node matching {@code node}
     * @param  visited               Set of visited nodes in the original graph
     *
     * @throws IllegalStateException If the graph has a circle
     */
    private static <N, E> void buildTree(UniversalGraph<N, E, Set<String>, String> graph,
        N node,
        IdentifierAdg.Node target,
        Set<N> visited) throws IllegalStateException {
        if (!visited.add(node)) {
            throw new IllegalStateException("cycle detected - input DOT graph is not a tree");
        }
        for (E edge: graph.getOutgoingEdges(node)) {
            N childId = graph.getTarget(edge);
            String label = graph.getEdgeProperty(edge);

            IdentifierAdg.Node child = new IdentifierAdg.Node(null);
            child.edgeLabel = label;
            child.models = graph.getNodeProperty(childId); // non-null only at leaves

            target.children.put(label, child);
            buildTree(graph, childId, child, visited);
        }
    }

}
