package util;

import java.util.HashMap;

import bufmgr.Frame;
public class MyHashMap extends HashMap<Integer,Frame> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1068026956445832691L;

	@Override
	public Frame put(Integer pageId, Frame frame) {
		Util.println("HM: put " + pageId);
		Util.myassert(pageId == frame.getPageId().pid, "Attempt to put different frame");
		return super.put(pageId, frame);
	}
	
	@Override
	public Frame remove(Object key) {
		Util.println("HM: remove " + key);
		return super.remove(key);
	}
	
	@Override
	public Frame get(Object key) {
		Frame ret = super.get(key);
		Util.println("HM: get " + key + " val " + ret);
		if(ret != null) {
			Util.myassert((Integer)key == ret.getPageId().pid, "Got different frame ?!");
		}
		return ret;
	}
}
