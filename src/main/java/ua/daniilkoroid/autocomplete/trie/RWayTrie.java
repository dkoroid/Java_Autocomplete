package ua.daniilkoroid.autocomplete.trie;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class that represents in-memory dictionary using R-way trie.
 *
 * @author Daniil_Koroid
 *
 */
public class RWayTrie implements Trie {

    /**
     * Inner class that represents node of trie.
     * <p>
     * This node stores given value and also link to all next nodes. Size of
     * used alphabet is determined by {@link #ALPHABET_SIZE}.
     * </p>
     *
     * @author Daniil_Koroid
     *
     */
    private class Node {

        /**
         * Value to be stored.
         * <p>
         * Currently value is weight of term.
         * </p>
         */
        private int value;

        /**
         * Links to next nodes.
         */
        private Node[] next;

        /**
         * Create node with given value.
         *
         * @param value value to be stored in node
         */
        public Node(int value) {
            this();
            this.value = value;
        }

        /**
         * Create empty node.
         */
        public Node() {
            next = new Node[ALPHABET_SIZE];
        }

        /**
         * Get value that is stored in node.
         *
         * @return value that is stored in node
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Size of used alphabet.
     * <p>
     * Currently used alphabet is English alphabet which size is 26.
     * </p>
     */
    private final int ALPHABET_SIZE = 26;

    /**
     * Empty prefix.
     * <p>
     * Empty prefix is prefix for all words.
     * </p>
     */
    private final String EMPTY_PREFIX = "";

    /**
     * First letter in used alphabet.
     */
    private final char FIRST_ALPHABET_LETTER = 'a';

    /**
     * Empty node root.
     */
    private Node root;

    /**
     * Number of stored tuples.
     */
    private int size;

    /**
     * Create RWayTrie.
     * <p>
     * Currently supported alphabet size is {@link #ALPHABET_SIZE}. Created
     * in-memory dictionary is created empty.
     * </p>
     */
    public RWayTrie() {
        this.root = new Node();
        size = 0;
    }

    @Override
    public void add(Tuple tuple) {
        if (!contains(tuple.getTerm())) {
            put(root, tuple, 0);
        }
    }

    @Override
    public boolean contains(String word) {
        Node node = get(root, word, 0);
        return node != null;
    }

    @Override
    public boolean delete(String word) {
        if (!contains(word)) {
            return false;
        }
        root = delete(root, word, 0);
        size--;
        return true;
    }

    @Override
    public Iterable<String> words() {
        return wordsWithPrefix(EMPTY_PREFIX);
    }

    @Override
    public Iterable<String> wordsWithPrefix(String pref) {
        Node node = get(root, pref, 0);
        Queue<String> queue = new LinkedList<>();
        collectBFS(node, pref, queue);
        return queue;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Find node with given key.
     *
     * @param node parent node to find node with given key among parent node
     * children
     * @param key key to find node by
     * @param d number of char in key to currently watch for
     * @return node with given key if found successfully. Otherwise -
     * <code>null</code>
     */
    private Node get(Node node, String key, int d) {
        Node result;
        if (node == null) {
            result = null;
        } else if (d == key.length()) {
            result = node;
        } else {
            int c = key.charAt(d) - FIRST_ALPHABET_LETTER;
            result = get(node.next[c], key, d + 1);
        }
        return result;
    }

    /**
     * Put given tuple in given node or in one of it's children.
     * <p>
     * If given node is not correct for putting given tuple in - tries to put
     * given tuple in appropriate child of given node.
     * </p>
     *
     * @param node node to put given tuple in or parent (can be indirect) of
     * node to put given tuple in
     * @param tuple tuple to put
     * @param d number of char in word to currently watch for
     * @return
     */
    private Node put(Node node, Tuple tuple, int d) {
        if (node == null) {
            node = new Node();
        }
        String term = tuple.getTerm();
        if (d == term.length()) {
            node.value = tuple.getWeight();
            size++;
            return node;
        }
        int c = term.charAt(d) - FIRST_ALPHABET_LETTER;
        node.next[c] = put(node.next[c], tuple, d + 1);
        return node;
    }

    /**
     * Collects all stored words that start from given base prefix and start
     * search from given node.
     * <p>
     * Uses breadth-first search.
     * </p>
     *
     * @param root node to start collecting from
     * @param basePrefix base prefix of words to collect
     * @param q queue to collect words
     */
    private void collectBFS(Node root, String basePrefix, Queue<String> q) {

        /**
         * Local class used to associate node with prefix that is stored with
         * given node.
         */
        class NodePrefixTuple {

            /**
             * Node to store in tuple.
             */
            private final Node node;

            /**
             * Prefix that is associated with given node.
             */
            private final String prefix;

            /**
             * Create tuple of node and associated prefix.
             *
             * @param node node to store
             * @param prefix appropriate prefix
             */
            public NodePrefixTuple(Node node, String prefix) {
                this.node = node;
                this.prefix = prefix;
            }
        }

        if (root == null) {
            return;
        }
        Queue<NodePrefixTuple> nodeAndPrefixQueue = new LinkedList<>();
        nodeAndPrefixQueue.add(new NodePrefixTuple(root, basePrefix));
        while (!nodeAndPrefixQueue.isEmpty()) {
            NodePrefixTuple tuple = nodeAndPrefixQueue.remove();
            Node node = tuple.node;
            String prefix = tuple.prefix;
            if (node.value != 0) {
                q.offer(prefix);
            }
            for (int c = 0; c < ALPHABET_SIZE; c++) {
                if (node.next[c] != null) {
                    String newPref = new StringBuilder(prefix).append((char) (FIRST_ALPHABET_LETTER + c)).toString();
                    nodeAndPrefixQueue.offer(new NodePrefixTuple(node.next[c], newPref));
                }
            }
        }
    }

    /**
     * Deletes given key from given node.
     *
     * @param node node to delete given key
     * @param key key to delete
     * @param d index of char in key that is currently used to be pointed at
     * @return <code>null</code> if given node and all of the links in given
     * node are null. Otherwise return given node
     */
    private Node delete(Node node, String key, int d) {
        if (node == null) {
            return null;
        }
        if (d == key.length()) {
            node.value = 0;
        } else {
            int c = key.charAt(d) - FIRST_ALPHABET_LETTER;
            node.next[c] = delete(node.next[c], key, d + 1);
        }

        if (node.value != 0) {
            return node;
        }

        for (char c = 0; c < ALPHABET_SIZE; c++) {
            if (node.next[c] != null) {
                return node;
            }
        }
        return null;
    }
}
