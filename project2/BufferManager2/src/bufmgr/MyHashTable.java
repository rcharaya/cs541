package bufmgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Util;

public class MyHashTable implements Map<Integer, Frame> {
	private static final int HTSIZE = 213;
	FrameBuckets[] buckets = new FrameBuckets[HTSIZE];
	
	 public MyHashTable() {
		 for(int i = 0; i < HTSIZE; i++) {
			 buckets[i] = new FrameBuckets();
		 }
	}
	
	class FrameBuckets {
		List<Frame> frames;
		public FrameBuckets() {
			frames = new ArrayList<Frame>();
		}
		Frame getFrame(int pid) {
			for(Frame f : frames) {
				if(f.mPageId.pid == pid) {
					return f;
				}
			}
			return null;
		}
		void putFrame(int pid, Frame frame) {
			Iterator<Frame> it = frames.iterator();
			while(it.hasNext()) {
				Frame f = it.next();
				if(f.mPageId.pid == pid) {
					it.remove();
					break;
				}
			}
			frames.add(frame);
		}
		void addFrame(Frame f){
			frames.add(f);
		}
		void removeFrame(Frame f){
			frames.remove(f);
		}
		int size() {
			return frames.size();
		}
	}
	
	@Override
	public int size() {
		int size = 0;
		for(int i = 0; i < HTSIZE; i++) {
			size += buckets[i].size();
		}
		return 0;
	}

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < HTSIZE; i++) {
			if( buckets[i].size() > 0)
				return false;
		}
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		Util.myassert(false, "avoid this. just call get");
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		Util.myassert(false, "unimpl");
		return false;
	}

	@Override
	public Frame get(Object key) {
		Integer pid = (Integer)key;
		int hash = toHash(pid);
		return buckets[hash].getFrame(pid);
	}

	@Override
	public Frame put(Integer key, Frame frame) {
		Util.myassert(frame != null, "Don't insert null in map..");
		Integer pid = (Integer)key;
		int hash = toHash(pid);
		buckets[hash].putFrame(pid, frame);
		return frame;
	}

	@Override
	public Frame remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Frame> m) {
		Util.myassert(false, "unimpl");
	}

	@Override
	public void clear() {
		Util.myassert(false, "unimpl");
	}

	@Override
	public Set<Integer> keySet() {
		Util.myassert(false, "unimpl");
		return null;
	}

	@Override
	public Collection<Frame> values() {
		Util.myassert(false, "unimpl");
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Frame>> entrySet() {
		Util.myassert(false, "unimpl");
		return null;
	}
	
	private int toHash(int val) {
		return (13*val + 17) % HTSIZE;
	}
	
}
