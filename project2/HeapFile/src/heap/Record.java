package heap;

import global.PageId;

public class Record implements Comparable<Record>{
	
	PageId pageId;
	
	Integer size;
	
	boolean isEmpty;
	
	public Record(Integer size) {
		this.size=size;
	}
	
	public Record(Integer size, PageId pageId) {
		this.size=size;
		this.pageId=pageId;
	}

	public Record(Integer size, PageId pageId, boolean isEmpty) {
		this.size=size;
		this.pageId=pageId;
		this.isEmpty=isEmpty;
	}

	public PageId getPageId() {
		return pageId;
	}


	public void setPageId(PageId pageId) {
		this.pageId = pageId;
	}


	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public int compareTo(Record other) {
		int ret=0;
		if(!this.size.equals(other.getSize()))
			ret = this.size.compareTo(other.getSize());
		else{
			
			ret = new Integer(this.pageId.pid).compareTo(new Integer(other.getPageId().pid));
		}
		return ret;
	}
	
	@Override
	public boolean equals(Object obj) {
		Record other = (Record) obj;
		return new Integer(this.pageId.pid).equals(new Integer(other.getPageId().pid));
	}

	@Override
	public String toString() {
		return pageId+":"+size;
	}
	
}
