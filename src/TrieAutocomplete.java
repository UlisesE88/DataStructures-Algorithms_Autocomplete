import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * General trie/priority queue algorithm for implementing Autocompletor
 * 
 * @author Austin Lu
 * @author Jeff Forbes
 */
public class TrieAutocomplete implements Autocompletor {

	/**
	 * Root of entire trie
	 */
	protected Node myRoot;

	/**
	 * Constructor method for TrieAutocomplete. Should initialize the trie
	 * rooted at myRoot, as well as add all nodes necessary to represent the
	 * words in terms.
	 * 
	 * @param terms
	 *            - The words we will autocomplete from
	 * @param weights
	 *            - Their weights, such that terms[i] has weight weights[i].
	 * @throws NullPointerException
	 *             if either argument is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different weight
	 */
	public TrieAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}

		if (terms.length != weights.length) {
			throw new IllegalArgumentException();
		}

		myRoot = new Node('-', null, 0);
		for (int i = 0; i < terms.length; i++) {
			add(terms[i], weights[i]);
			if (weights[i] < 0) {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Add the word with given weight to the trie. If word already exists in the
	 * trie, no new nodes should be created, but the weight of word should be
	 * updated.
	 * 
	 * In adding a word, this method should do the following: Create any
	 * necessary intermediate nodes if they do not exist. Update the
	 * subtreeMaxWeight of all nodes in the path from root to the node
	 * representing word. Set the value of myWord, myWeight, isWord, and
	 * mySubtreeMaxWeight of the node corresponding to the added word to the
	 * correct values
	 * 
	 * @throws a
	 *             NullPointerException if word is null
	 * @throws an
	 *             IllegalArgumentException if weight is negative.
	 */
	private void add(String word, double weight) { //methods adds a word to the tire and adds the weight of the word along with it.
		if(word == null) throw new NullPointerException();
		if(weight < 0) 	throw new  IllegalArgumentException();

		Node current = myRoot;
		for(int k = 0; k < word.length(); k++){
			char ch = word.charAt(k);

			if(current.children.get(ch) == null){
				current.children.put(ch, new Node(ch, current, weight));
			}
			if(weight > current.mySubtreeMaxWeight){
				current.mySubtreeMaxWeight = weight;
			}
			current = current.children.get(ch);
		}
		current.isWord = true;
		current.myWord = word;
		current.myWeight = weight; 
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the
	 * k words in the trie with the largest weight which match the given prefix,
	 * in descending weight order. If less than k words exist matching the given
	 * prefix (including if no words exist), then the array instead contains all
	 * those words. e.g. If terms is {air:3, bat:2, bell:4, boy:1}, then
	 * topKMatches("b", 2) should return {"bell", "bat"}, but topKMatches("a",
	 * 2) should return {"air"}
	 * 
	 * @param prefix
	 *            - A prefix which all returned words must start with
	 * @param k
	 *            - The (maximum) number of words to be returned
	 * @return An Iterable of the k words with the largest weights among all
	 *         words starting with prefix, in descending weight order. If less
	 *         than k such words exist, return all those words. If no such words
	 *         exist, return an empty Iterable
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) { //Returns an array containing the k words from the trie with the largest weight which targeted prefix, in descending weight order.
		if(prefix == null) throw new NullPointerException();
		if(k < 0) throw new IllegalArgumentException();
		if(k == 0) return new LinkedList<String>();

		PriorityQueue<Node> nodeQueue = new PriorityQueue<Node>(k, new Node.ReverseSubtreeMaxWeightComparator());
		PriorityQueue<Term> termQueue = new PriorityQueue<Term>(k, new Term.WeightOrder());
		
		Node current = myRoot;
		for(char ch : prefix.toCharArray()) {
			current = current.getChild(ch);
			if(current==null) {
				return new LinkedList<String>();
			}
		}

		nodeQueue.add(current);

		while (nodeQueue.size() > 0) {
			if(termQueue.size() == k && nodeQueue.size() > 0 &&
					termQueue.peek().getWeight() > nodeQueue.peek().mySubtreeMaxWeight){
				break;
			}
			
			current = nodeQueue.remove();
			if (current.isWord) {
				termQueue.add(new Term(current.myWord, current.myWeight));
			}
			if (termQueue.size() > k) {
				termQueue.remove();
			}
			for(Node n : current.children.values()) {
				nodeQueue.add(n);
			}
		}

		LinkedList<String> list = new LinkedList<String>();
		while (termQueue.size() > 0) {
			list.addFirst(termQueue.remove().getWord());
		}
		return list;
	}

	/**
	 * Given a prefix, returns the largest-weight word in the trie starting with
	 * that prefix.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from with the largest weight starting with prefix, or an
	 *         empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 */
	public String topMatch(String prefix) { //Traverses through tire to to find words with targeted prefix first. Then traverse through children nodes to find max weight and returns that word.
		if(prefix == null) throw new NullPointerException();

		Node current = myRoot;
		for(char ch : prefix.toCharArray()) {
			current = current.getChild(ch);
			if(current==null) {
				return new String();
			}
		}

		double maxWeight = current.mySubtreeMaxWeight;
		while(current.myWeight != maxWeight) {
			for(Node n : current.children.values()) {
				if(n.mySubtreeMaxWeight == maxWeight) {
					current = n;
				}	
			}
		}
		return current.myWord;
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary,
	 * return 0.0
	 */
	public double weightOf(String term) { //Method traverses through tire and returns the weight of term target
		Node current = myRoot;
		for(int k = 0; k < term.length(); k++){
			if(current.children.containsKey(term.charAt(k))) {
				current = current.children.get(term.charAt(k));
			}
			else {
				return 0.0;
			}
		}
		return current.myWeight;
	}
}