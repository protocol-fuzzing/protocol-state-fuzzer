package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintNode;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/** Handles the transformation of the result into human readable form (dot) and the printing of it */
public class FingerprintAdgWriter {

    /** Default constructor */
    public FingerprintAdgWriter() {}

    /**
     * Build, prune, condense, and write the ADG DOT file.
     *
     * @param  adg         ADG node (should be the root)
     * @param  out         output path
     *
     * @throws IOException if the write fails
     */
    public static void write(FingerprintNode adg,
        Path out) throws IOException {
        Files.writeString(out, renderDot(adg));
    }

    /**
     * Print the tree as indented outline
     *
     * @param adg ADG node (should be the root)
     * @param out the output stream
     */
    public static void printTree(FingerprintNode adg,
        PrintStream out) {
        printNode(adg, out, 0);
    }

    /**
     * Convert the {@link FingerprintNode} tree to a string for a dot file
     *
     * @param  root the root of the ADG
     *
     * @return      the String corresponding to the dot file contents
     */
    private static String renderDot(FingerprintNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph adg {\n");
        sb.append("  node [fontname=\"Helvetica\" fontsize=10]\n");
        sb.append("  edge [fontname=\"Helvetica\" fontsize=9]\n\n");

        Map<FingerprintNode, String> ids = new LinkedHashMap<>();
        Deque<FingerprintNode> bfs = new ArrayDeque<>();
        bfs.add(root);
        int counter = 0;
        while (!bfs.isEmpty()) {
            FingerprintNode n = bfs.poll();
            if (ids.containsKey(n))
                continue;
            ids.put(n, "n" + counter++);
            for (FingerprintNode c: n.getChildren().values())
                bfs.add(c);
        }

        for (Map.Entry<FingerprintNode, String> e: ids.entrySet()) {
            FingerprintNode n = e.getKey();
            String id = e.getValue();
            if (n.isLeaf()) {
                String ml = String.join(", ", new TreeSet<>(n.getModels()));
                sb.append("  ").append(id)
                    .append(" [label=\"").append(esc(ml)).append("\" shape=box models=\"").append(esc(ml))
                    .append("\"]\n");
            } else {
                sb.append("  ").append(id).append(" [label=\"\"]\n");
            }
        }
        sb.append("\n");
        for (Map.Entry<FingerprintNode, String> e: ids.entrySet()) {
            String src = e.getValue();
            for (Map.Entry<String, FingerprintNode> ce: e.getKey().getChildren().entrySet())
                sb.append("  ").append(src).append(" -> ").append(ids.get(ce.getValue()))
                    .append(" [label=\"").append(esc(ce.getKey())).append("\"]\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Pretty printing the tree
     *
     * @param n     the root of the subtree to print
     * @param out   the output stream
     * @param depth the indentation level
     */
    private static void printNode(FingerprintNode n, PrintStream out, int depth) {
        String indent = "  ".repeat(depth);
        String edge = n.getEdgeLabel() != null ? "[" + n.getEdgeLabel() + "] " : "";
        if (n.isLeaf()) {
            out.println(indent + edge + "→ " + new TreeSet<>(n.getModels()));
        } else {
            if (!edge.isEmpty())
                out.println(indent + edge);
            for (FingerprintNode c: n.getChildren().values())
                printNode(c, out, depth + 1);
        }
    }

    /**
     * Adds escape characters for print
     *
     * @param  s the string to convert
     *
     * @return   the converted string with escape characters
     */
    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
