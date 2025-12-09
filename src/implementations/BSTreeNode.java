package implementations;

import java.io.Serializable;

/**
 * Represents a single node in a Binary Search Tree (BST).
 * <p>
 * Each node stores an element and references to its left and right children.
 *
 * @param <E> the type of element stored in this node
 */
public class BSTreeNode<E> implements Serializable {
	private static final long serialVersionUID = 1L;

	/** The element stored in this node. */
	private E element;

	/** Reference to the left child node. */
	private BSTreeNode<E> leftChild;

	/** Reference to the right child node. */
	private BSTreeNode<E> rightChild;

	/**
	 * Constructs a new {@code BSTreeNode} with the specified element.
	 * <p>
	 * Both left and right children are initialized to {@code null}.
	 *
	 * @param element the element to be stored in this node
	 */
	public BSTreeNode(E element) {
		this.element = element;
		this.leftChild = null;
		this.rightChild = null;
	}

	/**
	 * Returns the element stored in this node.
	 *
	 * @return the element in this node
	 */
	public E getElement() {
		return element;
	}

	/**
	 * Sets the element stored in this node.
	 *
	 * @param element the new element to store in this node
	 */
	public void setElement(E element) {
		this.element = element;
	}

	/**
	 * Returns the left child of this node.
	 *
	 * @return the left child node, or {@code null} if none exists
	 */
	public BSTreeNode<E> getLeftChild() {
		return leftChild;
	}

	/**
	 * Sets the left child of this node.
	 *
	 * @param leftChild the node to assign as the left child
	 */
	public void setLeftChild(BSTreeNode<E> leftChild) {
		this.leftChild = leftChild;
	}

	/**
	 * Returns the right child of this node.
	 *
	 * @return the right child node, or {@code null} if none exists
	 */
	public BSTreeNode<E> getRightChild() {
		return rightChild;
	}

	/**
	 * Sets the right child of this node.
	 *
	 * @param rightChild the node to assign as the right child
	 */
	public void setRightChild(BSTreeNode<E> rightChild) {
		this.rightChild = rightChild;
	}

	/**
	 * Returns whether this node is a leaf node.
	 * <p>
	 * A leaf node has no left or right children.
	 *
	 * @return {@code true} if both children are {@code null}; {@code false}
	 *         otherwise
	 */
	public boolean isLeaf() {
		return leftChild == null && rightChild == null;
	}
}
