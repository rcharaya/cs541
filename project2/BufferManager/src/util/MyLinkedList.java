package util;

import java.util.LinkedList;

public class MyLinkedList<T> extends LinkedList<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2572006954020263017L;

	@Override
	public boolean add(T e) {
		Util.println("LL: add " + e);
		return super.add(e);
	}
	
	@Override
	public T remove(int index) {
		Util.println("LL: remove ");
		return super.remove(index);
	}
}
