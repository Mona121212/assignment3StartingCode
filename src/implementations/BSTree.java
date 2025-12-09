package implementations;

import java.io.Serializable;

import utilities.BSTreeADT;
import utilities.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * A generic Binary Search Tree (BST) implementation that stores comparable
 * elements.
 * <p>
 * The BST maintains the property that elements smaller than a node are stored
 * in its left subtree, while elements larger are stored in its right subtree.
 * Duplicate entries are not allowed.
 *
 * @param <E> the type of elements stored in the tree (must be Comparable)
 */
public class BSTree<E extends Comparable<? super E>> implements BSTreeADT<E>, Serializable {
	private static final long serialVersionUID = 1L;

	/** The root node of the BST. */
	private BSTreeNode<E> root;

	/** The total number of elements stored in the BST. */
	private int size;

	/**
	 * Constructs an empty Binary Search Tree.
	 */
	public BSTree() {
		this.root = null;
		this.size = 0;
	}

	/**
	 * Constructs a BSTree with a single root element.
	 *
	 * @param rootElement the element to be inserted as the root
	 * @throws NullPointerException if rootElement is null
	 */
	public BSTree(E rootElement) throws NullPointerException {
		if (rootElement == null) {
			throw new NullPointerException("Root element cannot be null");
		}
		this.root = new BSTreeNode<E>(rootElement);
		this.size = 1;
	}

	/**
	 * Returns the root node of this BST.
	 *
	 * @return the root node
	 * @throws NullPointerException if the tree is empty
	 */
	@Override
	public BSTreeNode<E> getRoot() throws NullPointerException {
		if (root == null) {
			throw new NullPointerException("Tree is empty");
		}
		return root;
	}

	/**
	 * Computes the height of the tree.
	 * <p>
	 * Height is defined as the number of nodes along the longest path from the root
	 * to a leaf node.
	 *
	 * @return the height of the tree; 0 if empty, 1 if only root exists
	 */
	@Override
	public int getHeight() {
		return getHeightHelper(root);
	}

	/**
	 * Recursively computes height of a subtree rooted at the given node.
	 *
	 * @param node the node whose subtree height is calculated
	 * @return the height of the subtree
	 */
	private int getHeightHelper(BSTreeNode<E> node) {
		if (node == null) {
			return 0;
		}

		int leftHeight = getHeightHelper(node.getLeftChild());
		int rightHeight = getHeightHelper(node.getRightChild());

		return 1 + Math.max(leftHeight, rightHeight);
	}

	/**
	 * Returns the number of elements currently stored in the tree.
	 *
	 * @return the size of the tree
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns whether the tree is empty.
	 *
	 * @return true if there are no elements, false otherwise
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Removes all elements from this BST.
	 * (MEMBER 2 IMPLEMENTATION)
	 */
	@Override
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * Determines whether this tree contains the specified element.
	 *
	 * @param entry the element to search for
	 * @return true if found, false otherwise
	 * @throws NullPointerException if entry is null
	 */
	@Override
	public boolean contains(E entry) throws NullPointerException {
		if (entry == null) {
			throw new NullPointerException("Entry cannot be null");
		}
		return search(entry) != null;
	}

	/**
	 * Searches this BST for a specific element.
	 *
	 * @param entry the element to find
	 * @return the node containing the element, or null if not found
	 * @throws NullPointerException if entry is null
	 */
	@Override
	public BSTreeNode<E> search(E entry) throws NullPointerException {
		if (entry == null) {
			throw new NullPointerException("Entry cannot be null");
		}
		return searchHelper(root, entry);
	}

	/**
	 * Recursive search helper.
	 *
	 * @param node  the current node
	 * @param entry the target element
	 * @return the matching node, or null if not found
	 */
	private BSTreeNode<E> searchHelper(BSTreeNode<E> node, E entry) {
		if (node == null) {
			return null;
		}

		int comparison = entry.compareTo(node.getElement());

		if (comparison == 0) {
			return node;
		} else if (comparison < 0) {
			return searchHelper(node.getLeftChild(), entry);
		} else {
			return searchHelper(node.getRightChild(), entry);
		}
	}

	/**
	 * Inserts a new element into the BST. Duplicate elements are not inserted.
	 *
	 * @param newEntry the element to insert
	 * @return true if successfully inserted, false if already exists
	 * @throws NullPointerException if newEntry is null
	 */
	@Override
	public boolean add(E newEntry) throws NullPointerException {
		if (newEntry == null) {
			throw new NullPointerException("Entry cannot be null");
		}

		if (root == null) {
			root = new BSTreeNode<E>(newEntry);
			size++;
			return true;
		}

		return addHelper(root, newEntry);
	}

	/**
	 * Recursive insertion helper.
	 *
	 * @param node     the current node
	 * @param newEntry the element to insert
	 * @return true if inserted
	 */
	private boolean addHelper(BSTreeNode<E> node, E newEntry) {
		int comparison = newEntry.compareTo(node.getElement());

		if (comparison == 0) {
			return false; // duplicate
		} else if (comparison < 0) {
			if (node.getLeftChild() == null) {
				node.setLeftChild(new BSTreeNode<E>(newEntry));
				size++;
				return true;
			}
			return addHelper(node.getLeftChild(), newEntry);
		} else {
			if (node.getRightChild() == null) {
				node.setRightChild(new BSTreeNode<E>(newEntry));
				size++;
				return true;
			}
			return addHelper(node.getRightChild(), newEntry);
		}
	}

    /**
     * Removes the first instance of the specified element from the BST.
     * (MEMBER 2 IMPLEMENTATION)
     *
     * @param entry the element to be removed.
     * @return true if the element was successfully removed, false otherwise.
     * @throws NullPointerException if entry is null.
     */

	public boolean remove(E entry) throws NullPointerException {
		if (entry == null) {
			throw new NullPointerException("Entry cannot be null");
		}

		// 1. Check if the element exists using the existing search method
		if (search(entry) == null) {
			return false; // Element not found
		}

		// 2. Element exists: Call the recursive helper to remove it and update the root
		root = removeHelper(root, entry);

		// 3. Decrement size now that we know the removal was successful
		size--;
		return true;
	}

	/**
	 * Recursively finds and removes an element from the BST.
	 * Handles the three cases of deletion (0, 1, or 2 children).
	 * (MEMBER 2 IMPLEMENTATION)
	 *
	 * @param current The current node being examined.
	 * @param entry The element to be removed.
	 * @return The node that replaces the current node (new subtree root).
	 */
	private BSTreeNode<E> removeHelper(BSTreeNode<E> current, E entry) {
		if (current == null) {
			return null; 
		}

		int compare = entry.compareTo(current.getElement());

		if (compare < 0) {
			// Element is smaller, go left
			current.setLeftChild(removeHelper(current.getLeftChild(), entry));
		} else if (compare > 0) {
			// Element is larger, go right
			current.setRightChild(removeHelper(current.getRightChild(), entry));
		} else {
			// Node found: Handle the three deletion cases

			// Case 1 & 2: 0 or 1 child
			if (current.getLeftChild() == null) {
				return current.getRightChild(); // Returns right child (or null)
			}
			if (current.getRightChild() == null) {
				return current.getLeftChild(); // Returns left child
			}

			// Case 3: Two children (Use In-Order Successor)
			// 1. Find successor (smallest node in the right subtree)
			BSTreeNode<E> successor = findMin(current.getRightChild()); 
			
			// 2. Copy the Successor's data to the current node
			current.setElement(successor.getElement());

			// 3. Recursively delete the Successor from the right subtree.
			current.setRightChild(removeHelper(current.getRightChild(), successor.getElement()));
		}
		return current;
	}
    // 

//[Image of Binary Search Tree Deletion Logic]


	/**
	 * Removes and returns the node containing the minimum element of the tree.
	 *
	 * @return the node containing the smallest element, or null if empty
	 */
	@Override
	public BSTreeNode<E> removeMin() {
		if (root == null) {
			return null;
		}

		BSTreeNode<E> minNode = findMin(root);
		root = removeMinHelper(root);
		size--;
		return minNode;
	}

	/**
	 * Returns the minimum-valued node in the subtree rooted at the given node.
	 */
	private BSTreeNode<E> findMin(BSTreeNode<E> node) {
		while (node.getLeftChild() != null) {
			node = node.getLeftChild();
		}
		return node;
	}

	/**
	 * Removes the minimum node from the subtree.
	 */
	private BSTreeNode<E> removeMinHelper(BSTreeNode<E> node) {
		if (node.getLeftChild() == null) {
			return node.getRightChild();
		}
		node.setLeftChild(removeMinHelper(node.getLeftChild()));
		return node;
	}

	/**
	 * Removes and returns the node containing the maximum element of the tree.
	 *
	 * @return the node containing the largest element, or null if empty
	 */
	@Override
	public BSTreeNode<E> removeMax() {
		if (root == null) {
			return null;
		}

		BSTreeNode<E> maxNode = findMax(root);
		root = removeMaxHelper(root);
		size--;
		return maxNode;
	}

	/**
	 * Returns the maximum-valued node in the subtree rooted at the given node.
	 */
	private BSTreeNode<E> findMax(BSTreeNode<E> node) {
		while (node.getRightChild() != null) {
			node = node.getRightChild();
		}
		return node;
	}

	/**
	 * Removes the maximum node from the subtree.
	 */
	private BSTreeNode<E> removeMaxHelper(BSTreeNode<E> node) {
		if (node.getRightChild() == null) {
			return node.getLeftChild();
		}
		node.setRightChild(removeMaxHelper(node.getRightChild()));
		return node;
	}

	/**
	 * Returns an iterator that performs in-order traversal (left-root-right).
	 * Produces elements in sorted order (alphabetical for WordTracker).
	 *
	 * @return an in-order traversal iterator
	 */
	@Override
	public Iterator<E> inorderIterator() {
		return new InorderIterator();
	}

	/**
	 * Returns an iterator that performs pre-order traversal (root-left-right).
	 *
	 * @return a pre-order traversal iterator
	 */
	@Override
	public Iterator<E> preorderIterator() {
		return new PreorderIterator();
	}

	/**
	 * Returns an iterator that performs post-order traversal (left-right-root).
	 *
	 * @return a post-order traversal iterator
	 */
	@Override
	public Iterator<E> postorderIterator() {
		return new PostorderIterator();
	}

	// ==============================
	// Iterator Classes (Snapshot Approach)
	// ==============================

	/**
	 * Iterator for in-order traversal (left-root-right). Produces elements in
	 * ascending sorted order.
	 */
	private class InorderIterator implements Iterator<E> {
		private java.util.ArrayList<E> items;
		private int index;

		public InorderIterator() {
			items = new java.util.ArrayList<E>();
			index = 0;
			inorderTraversal(root);
		}
        
        /**
         * Recursive helper to populate the list in in-order sequence.
         */
		private void inorderTraversal(BSTreeNode<E> node) {
			if (node != null) {
				inorderTraversal(node.getLeftChild());
				items.add(node.getElement());
				inorderTraversal(node.getRightChild());
			}
		}

		@Override
		public boolean hasNext() {
			return index < items.size();
		}

		@Override
		public E next() throws java.util.NoSuchElementException {
			if (!hasNext()) {
				throw new java.util.NoSuchElementException("No more elements");
			}
			return items.get(index++);
		}
	}

	/**
	 * Iterator for pre-order traversal (root-left-right).
	 */
	private class PreorderIterator implements Iterator<E> {
		private java.util.ArrayList<E> items;
		private int index;

		public PreorderIterator() {
			items = new java.util.ArrayList<E>();
			index = 0;
			preorderTraversal(root);
		}
        
        /**
         * Recursive helper to populate the list in pre-order sequence.
         */
		private void preorderTraversal(BSTreeNode<E> node) {
			if (node != null) {
				items.add(node.getElement());
				preorderTraversal(node.getLeftChild());
				preorderTraversal(node.getRightChild());
			}
		}

		@Override
		public boolean hasNext() {
			return index < items.size();
		}

		@Override
		public E next() throws java.util.NoSuchElementException {
			if (!hasNext()) {
				throw new java.util.NoSuchElementException("No more elements");
			}
			return items.get(index++);
		}
	}

	/**
	 * Iterator for post-order traversal (left-right-root).
	 */
	private class PostorderIterator implements Iterator<E> {
		private java.util.ArrayList<E> items;
		private int index;

		public PostorderIterator() {
			items = new java.util.ArrayList<E>();
			index = 0;
			postorderTraversal(root);
		}
        
        /**
         * Recursive helper to populate the list in post-order sequence.
         */
		private void postorderTraversal(BSTreeNode<E> node) {
			if (node != null) {
				postorderTraversal(node.getLeftChild());
				postorderTraversal(node.getRightChild());
				items.add(node.getElement());
			}
		}

		@Override
		public boolean hasNext() {
			return index < items.size();
		}

		@Override
		public E next() throws java.util.NoSuchElementException {
			if (!hasNext()) {
				throw new java.util.NoSuchElementException("No more elements");
			}
			return items.get(index++);
		}
	}
}
