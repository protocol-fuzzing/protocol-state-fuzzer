package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierAdg.Node;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierAdg;

/**
 * Parses a DOT representation produced by {@code AdgWriter.renderDot(Node)} into
 * an {@link IdentifierAdg} tree.
 * <p>
 * The parser supports internal nodes and leaf nodes marked with
 * {@code shape=box}. Leaf nodes may contain comma-separated model names in the
 * DOT {@code label} attribute.
 */
public class IdentifierParser {

    // Node declaration: nX [label="..." ...]
    private static final Pattern NODE_DECL = Pattern.compile(
        "^\\s*(\\w+)\\s*\\[([^\\]]+)\\]");

    // Edge declaration: nX -> nY [label="..."]
    private static final Pattern EDGE_DECL = Pattern.compile(
        "^\\s*(\\w+)\\s*->\\s*(\\w+)\\s*\\[([^\\]]+)\\]");

    // Extracts label="..." value
    private static final Pattern LABEL_ATTR = Pattern.compile(
        "label\\s*=\\s*\"((?:[^\\\\\"]|\\\\.)*)\"");

    // Detects shape=box (leaf nodes)
    private static final Pattern SHAPE_BOX = Pattern.compile(
        "shape\\s*=\\s*box");

    /**
     * Creates a new parser instance.
     */
    public IdentifierParser() {}

    /**
     * Parses a DOT file from disk into an {@link IdentifierAdg}.
     *
     * @param  path        path to the DOT file
     *
     * @return             parsed ADG
     *
     * @throws IOException if the file cannot be read
     */
    public static IdentifierAdg parse(Path path) throws IOException {
        return parseString(Files.readString(path));
    }

    /**
     * Parses a DOT file from the given path string into an {@link IdentifierAdg}.
     *
     * @param  path        string path to the DOT file
     *
     * @return             parsed ADG
     *
     * @throws IOException if the file cannot be read
     */
    public static IdentifierAdg parse(String path) throws IOException {
        return parseString(Files.readString(Path.of(path)));
    }

    /**
     * Parses a DOT string into an {@link IdentifierAdg}.
     *
     * @param  dot DOT content to parse
     *
     * @return     parsed ADG
     */
    public static IdentifierAdg parseString(String dot) {
        // id -> Node
        Map<String, Node> nodes = new LinkedHashMap<>();
        // src id -> list of (edgeLabel, dst id)
        Map<String, List<String[]>> edgeMap = new LinkedHashMap<>();
        // ids that appear as a child (have an incoming edge)
        Set<String> hasParent = new LinkedHashSet<>();

        IdentifierAdg adg = new IdentifierAdg(null);

        for (String line: Splitter.on('\n').split(dot)) {
            // Try edge first (also matches node pattern, so check edge first)
            Matcher em = EDGE_DECL.matcher(line);
            if (em.find()) {
                String src = em.group(1);
                String dst = em.group(2);
                String attrs = em.group(3);

                Matcher lm = LABEL_ATTR.matcher(attrs);
                String edgeLabel = lm.find() ? unescape(lm.group(1)) : "";

                edgeMap.computeIfAbsent(src, k -> new ArrayList<>())
                    .add(new String[] {edgeLabel, dst});
                hasParent.add(dst);
                continue;
            }

            // Try node declaration
            Matcher nm = NODE_DECL.matcher(line);
            if (nm.find()) {
                String id = nm.group(1);
                String attrs = nm.group(2);

                // Skip graph-level attribute lines like: node [fontname=...]
                if (id.equals("node") || id.equals("edge") || id.equals("graph"))
                    continue;

                Matcher lm = LABEL_ATTR.matcher(attrs);
                String label = lm.find() ? unescape(lm.group(1)) : "";

                boolean isLeaf = SHAPE_BOX.matcher(attrs).find();

                Node n = new Node(null); // edgeLabel set when wiring edges
                if (isLeaf) {
                    // Parse comma-separated model names; empty label means empty set
                    Set<String> models = new LinkedHashSet<>();
                    if (!label.isBlank()) {
                        for (String m: Splitter.on(',').trimResults().omitEmptyStrings().split(label)) {
                            String trimmed = m.trim();
                            if (!trimmed.isEmpty())
                                models.add(trimmed);
                        }
                    }
                    n.updateModels(models);
                }

                nodes.put(id, n);
            }
        }

        // Wire up children and set edge labels
        for (Map.Entry<String, List<String[]>> entry: edgeMap.entrySet()) {
            Node parent = nodes.get(entry.getKey());
            if (parent == null)
                continue;
            for (String[] edge: entry.getValue()) {
                String edgeLabel = edge[0];
                String dstId = edge[1];
                Node child = nodes.get(dstId);
                if (child == null)
                    continue;
                child.updateEdgeLabel(edgeLabel);
                parent.addChild(edgeLabel, child);
            }
        }

        // Root is the only node with no incoming edge
        for (Map.Entry<String, Node> e: nodes.entrySet()) {
            if (!hasParent.contains(e.getKey())) {
                adg.updateRoot(e.getValue()); // first root found
                return adg;
            }
        }

        throw new IllegalArgumentException("No root node found — every node has a parent");
    }

    /**
     * Unescapes DOT-encoded text produced by {@code AdgWriter.esc()}.
     *
     * @param  s escaped string
     *
     * @return   unescaped string
     */
    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
