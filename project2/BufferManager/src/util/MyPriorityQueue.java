package util;

import java.util.PriorityQueue;

public class MyPriorityQueue<T> extends PriorityQueue<T>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9130874326568351337L;
	
	@Override
	public int size() {
		int ret = super.size();
		Util.println("PQ: size " + ret);
		return ret;
	};
	
	@Override
	public boolean add(T e) {
		Util.println("PQ: add " + e);
		return super.add(e);
	}

	@Override
	public T poll() {
		T t = super.poll();
		Util.println("PQ: poll " + t);
		return t;
	}
	
	@Override
	public boolean remove(Object o) {
		Util.println("PQ: remove " + o);
		return super.remove(o);
	}
}
