package edu.iastate.cs228.hw3;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
   */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;
  
  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0) throw new IllegalArgumentException();
    
    // dummy nodes
    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head;
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head
   * @param tail
   * @param nodeSize
   * @param size
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }

  @Override
  public int size()
  {
    return size;
  }
  
  @Override
  public boolean add(E item)
  {
    // TODO Auto-generated method stub
	if (item == null) {
		throw new NullPointerException();
	}
	
	if (size == 0) {
		Node n = new Node();
		n.addItem(item);
		n.next = tail;
		n.previous = head;
		size++;
	} else {
		if (tail.previous.count < nodeSize) {
			tail.previous.addItem(item);
		} else {
			Node n = new Node();
			n.addItem(item);
			n.next = tail;
			n.previous = tail.previous;
			n.previous.next = n;
		}
	}
    return true;
  }

  @Override
  public void add(int pos, E item)
  {
	//Checks if pos is within bounds
    if (pos < 0 || pos > size) {
    	throw new IndexOutOfBoundsException();
    }
    
    //Will call default add() if there are no non-dummy nodes
    if (head.next == tail) {
    	add(item);
    }
    
    NodeInfo nodeInfo = find (pos);
    Node temp = nodeInfo.node;
    int offset = nodeInfo.offset;
    
    
    if (offset == 0) {
    	
    	//If previous node is not full and not the head, add to previous
    	if (temp.previous.count < nodeSize && temp.previous != head) {
    		temp.previous.addItem(item);
    		size++;
    		return;
    	}
    	//If temp is tail
    	else if (temp == tail) {
    		add(item);
    		size++;
    		return;
    	}
    }
    
    //If there is space in temp, add it to temp and shift elements if needed
    if (temp.count < nodeSize) {
    	temp.addItem(offset, item);
    }
    
    //Else, perform split operation
    else {
    	Node newNode = new Node();
    	int halfPoint = nodeSize / 2;
    	int count = 0;
    	
    	//Move last half into new successor node
    	while(count < halfPoint) {
    		newNode.addItem(temp.data[halfPoint + count]);
    		temp.removeItem(halfPoint);
    		count++;
    	}
    	
    	Node oldSuccessor = temp.next;
    	
    	temp.next = newNode;
    	newNode.previous = temp;
    	newNode.next = oldSuccessor;
    	oldSuccessor.previous = newNode;
    	
    	//add new item based on if offset is > or < M/2
    	if (offset <= halfPoint) {
    		temp.addItem(offset, item);
    	}
    	
    	else if (offset >= halfPoint) {
    		newNode.addItem(offset - halfPoint, item);
    	}
    }
    
    size++;
  }

  @Override
  public E remove(int pos)
  {
	NodeInfo nodeInfo = find (pos);
	Node temp = nodeInfo.node;
	int offset = nodeInfo.offset;
	E val = temp.data[offset];
	
	//Check pos is within bounds
    if (pos < 0 || pos > size) {
    	throw new IndexOutOfBoundsException();
    }
    
    //If temp.next = tail and there's 1 element, remove temp entirely
    if (temp.next == tail && temp.count == 1) {
    	Node pred = temp.previous;
    	pred.next = temp.next;
    	temp.previous.next = pred;
    	temp = null;
    }
    
    //If temp.next = tail or temp.count > M/2, just remove item
    else if (temp.next == tail || temp.count > nodeSize / 2) {
    	temp.removeItem(offset);
    }
    
    //Else, perform merge
    else {
    	temp.removeItem(offset);
    	Node succ = temp.next;
    	
    	//If successor count > M/2, shift first element of successor to temp
    	if (succ.count > nodeSize / 2) {
    		temp.addItem(succ.data[0]);
    		succ.removeItem(0);
    	}
    	
    	//Else, shift all items from successor into temp, then remove successor
    	else {
    		for (int i = 0; i < succ.count; i++) {
    			temp.addItem(succ.data[i]);
    		}
    		temp.next = succ.next;
    		succ.next.previous = temp;
    		succ = null;
    	}
    }
    
    size--;
    return val;
  }

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */
  public void sort()
  {
	  E[] tempArray = (E[]) new Comparable[size];
	  
	  int index = 0;
	  Node temp = head.next;
	  
	  while(temp != tail) {
		  for (int i = 0; i < temp.count; i++) {
			  tempArray[index] = temp.data[i];
			  index++;
		  }
		  temp = temp.next;
	  }
	  
	  head.next = tail;
	  tail.previous = head;
	  
	  insertionSort(tempArray, new ElementComparator());
	  size = 0;
	  for (int i = 0; i < tempArray.length; i++) {
		  add(tempArray[i]);
	  }
  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse() 
  {
	  E[] tempArray = (E[]) new Comparable[size];
	  
	  int index = 0;
	  Node temp = head.next;
	  
	  while(temp != tail) {
		  for (int i = 0; i < temp.count; i++) {
			  tempArray[index] = temp.data[i];
			  index++;
		  }
		  temp = temp.next;
	  }
	  
	  head.next = tail;
	  tail.previous = head;
	  
	  bubbleSort(tempArray);
	  size = 0;
	  for (int i = 0; i < tempArray.length; i++) {
		  add(tempArray[i]);
	  }
  }
  
  @Override
  public Iterator<E> iterator()
  {
	  return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator()
  {
	  return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
	  return new StoutListIterator();
  }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   */
  public String toStringInternal()
  {
    return toStringInternal(null);
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter
   *            an iterator for this list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }


  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node
  {
    /**
     * Array of actual data elements.
     */
    // Unchecked warning unavoidable.
    public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        return;
      }
      data[count++] = item;
      //useful for debugging
      //      System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  return;
      }
      for (int i = count - 1; i >= offset; --i)
      {
        data[i + 1] = data[i];
      }
      ++count;
      data[offset] = item;
      //useful for debugging 
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null;
      --count;
    }    
  }
 
  private class NodeInfo{
	public Node node;
	public int offset;
	
	public NodeInfo(Node node, int offset) {
		this.node = node;
		this.offset = offset;
	}
  }
  
  public NodeInfo find(int pos) {
	  Node temp = head.next;
	  int currPos = 0;
	  
	  while (temp != tail) {
		  if (currPos + temp.count <= pos) {
			  currPos += temp.count;
			  temp = temp.next;
			  continue;
		  }
		  NodeInfo nodeInfo = new NodeInfo(temp, pos - currPos);
		  return nodeInfo;
	  }
	  
	  return null;
  }
  
  private class StoutListIterator implements ListIterator<E>
  {
	// constants you possibly use ...   
	  
	// instance variables ... 
	private int currPos;
	E[] dataList;
	
	/*
	 * follows the previous action taken by the program
	 * -1 = no action
	 * 0 = last action was previous
	 * 1 = last action was next
	 */
	private int prevAction;
	
    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	currPos = 0;
    	prevAction = -1;
    	dataList = createDataList();
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     */
    public StoutListIterator(int pos)
    {
    	currPos = pos;
    	prevAction = -1;
    	dataList = createDataList();
    }
    
    /**
     * Creates list of data in array form for easier access
     * @return dataList
     */
    public E[] createDataList() {
    	E[] tempList = (E[]) new Comparable[size];
    	int index = 0;
    	Node temp = head.next;
    	
    	while (temp != tail) {
    		for (int i = 0; i < temp.count; i++) {
    			tempList[index] = temp.data[i];
    			index++;
    		}
    		temp = temp.next;
    	}
    	return tempList;
    }

    @Override
    public boolean hasNext()
    {
    	if (currPos >= size) {
    		return false;
    	}
    	return true;
    }

    @Override
    public E next()
    {
    	if (!hasNext()) {
    		throw new NoSuchElementException();
    	}
    	prevAction = 1;
    	return dataList[currPos];
    }

    @Override
    public void remove()
    {
    	if (prevAction == 1) {
    		StoutList.this.remove(currPos - 1);
    		dataList = createDataList();
    		prevAction = -1;
    		currPos--;
    		if (currPos < 0) {
    			currPos = 0;
    		}
    	}
    	else if (prevAction == 0) {
    		StoutList.this.remove(currPos);
    		dataList = createDataList();
    		prevAction = -1;
    	}
    	else {
    		throw new IllegalStateException();
    	}
    }
    
    @Override
    public void add(E arg0) {
    	if (arg0 == null) {
    		throw new NullPointerException();
    	}
    	StoutList.this.add(currPos, arg0);
    	currPos++;
    	dataList = createDataList();
    	prevAction = -1;
    }
    
    // Other methods you may want to add or override that could possibly facilitate 
    // other operations, for instance, addition, access to the previous element, etc.
    // 
    // ...
    // 
    
    public boolean hasPrevious() {
    	if (currPos <= 0) {
    		return false;
    	}
    	return true;
    }
    
    public E previous() {
    	if(!hasPrevious()) {
    		throw new NoSuchElementException();
    	}
    	prevAction = 0;
    	currPos--;
    	return dataList[currPos];
    }
    
    public int nextIndex() {
    	return currPos + 1;
    }
    
    public int previousIndex() {
    	return currPos - 1;
    }
    
    @Override
    public void set(E item) {
    	if (prevAction == 0) {
    		NodeInfo nodeInfo = find(currPos - 1);
    		nodeInfo.node.data[nodeInfo.offset] = item;
    		dataList[currPos - 1] = item;
    	}
    	else if (prevAction == 1) {
    		NodeInfo nodeInfo = find(currPos + 1);
    		nodeInfo.node.data[nodeInfo.offset] = item;
    		dataList[currPos] = item;
    	}
    	else {
    		throw new IllegalStateException();
    	}
    }
  }
  

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
	  for (int i = 1; i < arr.length; i++) {
		  E key = arr[i];
		  int j = i - 1;
		  
		  while (j >= 0 && comp.compare(arr[j], key) > 0) {
			  arr[j + 1] = arr[j];
			  j--;
		  }
		  arr[j + 1] = key;
	  }
  }
  
  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private void bubbleSort(E[] arr)
  {
	  //Modified version of bubble sort from GeeksForGeeks.org
	  int n = arr.length;
	  for (int i = 0; i < n - 1; i++) {
		  for (int j = 0; j < n - i - 1; j++) {
			  if (arr[j].compareTo(arr[j + 1]) < 0) {
				 E temp = arr[j];
				 arr[j] = arr[j + 1];
				 arr[j + 1] = temp;
			  }
		  }
	  }
  }
  
  class ElementComparator<E extends Comparable<E>> implements Comparator<E>{
	  @Override
	  public int compare(E a, E b) {
		  return a.compareTo(b);
	  }
  }
}