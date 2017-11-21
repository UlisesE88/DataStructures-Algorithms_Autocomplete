import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * 
 * Using a sorted array of Term objects, this implementation uses binary search
 * to find the top term(s).
 * 
 * @author Austin Lu, adapted from Kevin Wayne
 * @author Jeff Forbes
 */
public class BinarySearchAutocomplete implements Autocompletor {

	Term[] myTerms;

	/**
	 * Given arrays of words and weights, initialize myTerms to a corresponding
	 * array of Terms sorted lexicographically.
	 * 
	 * This constructor is written for you, but you may make modifications to
	 * it.
	 * 
	 * @param terms
	 *            - A list of words to form terms from
	 * @param weights
	 *            - A corresponding list of weights, such that terms[i] has
	 *            weight[i].
	 * @return a BinarySearchAutocomplete whose myTerms object has myTerms[i] =
	 *         a Term with word terms[i] and weight weights[i].
	 * @throws a
	 *             NullPointerException if either argument passed in is null
	 */
	public BinarySearchAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}

		if(terms.length != weights.length) {
			throw new IllegalArgumentException();
		}
		
		myTerms = new Term[terms.length];
		for (int i = 0; i < terms.length; i++) {
			myTerms[i] = new Term(terms[i], weights[i]);
			if (weights[i] < 0) {
				throw new IllegalArgumentException("Negative weight "+ weights[i]);
			}
		}
		Arrays.sort(myTerms);
	}

	/**
	 * Uses binary search to find the index of the first Term in the passed in
	 * array which is considered equivalent by a comparator to the given key.
	 * This method should not call comparator.compare() more than 1+log n times,
	 * where n is the size of a.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the
	 *            values in a and the key.
	 * @return The first index i for which comparator considers a[i] and key as
	 *         being equal. If no such index exists, return -1 instead.
	 */
	public static int firstIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if(a == null || key == null || comparator == null) { 
			throw new NullPointerException();
		}

		int low = -1;
		int high = a.length-1;

		while (low+1 != high) {
			int mid = (low + high)/2;
			Term midVal = a[mid];
			int cmp = comparator.compare(midVal, key);

			if (cmp < 0)
				low = mid;
			else if (cmp >= 0)
				high = mid;
		}

		if (comparator.compare(a[high], key) == 0) {
			return high;
		}
		return -1;
	}

	/**
	 * The same as firstIndexOf, but instead finding the index of the last Term.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the
	 *            values in a and the key.
	 * @return The last index i for which comparator considers a[i] and key as
	 *         being equal. If no such index exists, return -1 instead.
	 */
	public static int lastIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if(a == null || key == null || comparator == null) { 
			throw new NullPointerException();
		}

		int low = 0;
		int high = a.length;

		while (low+1 != high) {
			int mid = (low + high)/2;
			Term midVal = a[mid];
			int cmp = comparator.compare(midVal,key);

			if (cmp <= 0)
				low = mid;
			else if (cmp > 0)
				high = mid;
		}

		if (comparator.compare(a[low], key) == 0) {
			return low;
		}
		return -1;
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the
	 * k words in myTerms with the largest weight which match the given prefix,
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
	 * @return An array of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k
	 *         such words exist, return an array containing all those words If
	 *         no such words exist, reutrn an empty array
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		if(prefix == null) {
			throw new NullPointerException();
		}

		int minIndex = firstIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		int maxIndex = lastIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		
		if(minIndex == -1 || maxIndex == -1) {
			return new LinkedList<String>();
		}
		
		PriorityQueue<Term> pq = new PriorityQueue<Term>(k, new Term.WeightOrder());
		for(int x = minIndex; x <= maxIndex; x++){
			pq.add(myTerms[x]);
			if (pq.size() > k)
				pq.remove();
		}
		
		LinkedList<String> list = new LinkedList<String>();
		while (pq.size() > 0) {
			list.addFirst(pq.remove().getWord());
		}
		return list;
	}

	/**
	 * Given a prefix, returns the largest-weight word in myTerms starting with
	 * that prefix. e.g. for {air:3, bat:2, bell:4, boy:1}, topMatch("b") would
	 * return "bell". If no such word exists, return an empty String.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from myTerms with the largest weight starting with
	 *         prefix, or an empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 * 
	 */
	public String topMatch(String prefix) {
		if(prefix == null) {
			throw new NullPointerException();
		}
		
		int minIndex = firstIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		int maxIndex = lastIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));

		if(minIndex == -1 || maxIndex == -1) {
			return new String();
		}
		double maxweight = 0;
		String maxTerm = "";
		for (int i = minIndex; i <= maxIndex; i++) {
			if (myTerms[i].getWeight() >= maxweight) {
				maxweight = myTerms[i].getWeight();
				maxTerm = myTerms[i].getWord();
			}
		}
		return maxTerm;
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary,
	 * return 0.0
	 */
	public double weightOf(String term) {
		if(term == null) {
			throw new NullPointerException();
		}
		
		double weight = 0.0;
		for(Term t: myTerms) {
			if(t.getWord().compareTo(term) == 0)
				weight = t.getWeight();
		}
		return weight;
	}
}
