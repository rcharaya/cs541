package heap;

import global.Minibase;
import global.PageId;
import global.RID;

import java.util.Iterator;

import chainexception.ChainException;

public class HeapScan {

	Iterator<Record> iterator;

	HFPage curPage = new HFPage();

	RID curRid = null;

	HeapFile hf;

	protected HeapScan(HeapFile hf){
		PageId pageId = hf.getIterator();
		Minibase.BufferManager.pinPage(pageId, curPage, false);
		while(curPage.firstRecord()==null){
			Minibase.BufferManager.unpinPage(pageId, false);
			pageId=curPage.getNextPage();
			Minibase.BufferManager.pinPage(pageId, curPage, false);
		}
	}

	public boolean hasNext(){
		if(curPage==null || curRid==null){
			return false;
		}else{
			if(curPage.hasNext(curRid)){
				return true;
			} else{
				return curPage.getNextPage().pid!=-1;
			}
		}
	}

	public Tuple getNext(RID rid){
		if(curPage!=null && curRid==null){
			curRid = curPage.firstRecord();				
		}
		else if(!curPage.hasNext(curRid)) {
			Minibase.BufferManager.unpinPage(curPage.getCurPage(), false);
			if(curPage.getNextPage().pid!=-1){
				Minibase.BufferManager.pinPage(curPage.getNextPage(), curPage, false);
				curRid = curPage.firstRecord();
			} else{
				curPage=null;
				curRid=null;
				System.out.println("HeapScan.java: getNext() Returning null");
				return null;
			}
		} else{
			curRid = curPage.nextRecord(curRid);
		}	

		if(curPage!=null && curRid!=null){
			Tuple tuple = new Tuple(curPage.selectRecord(curRid));
			rid.pageno=curRid.pageno;
			rid.slotno=curRid.slotno;
			return tuple;
		}
		return null;
	}

	protected void finalize() throws Throwable{
		this.close();
	}

	public void close() throws ChainException{
		if(curPage!=null)
			Minibase.BufferManager.unpinPage(curPage.getCurPage(), false);
	}
}
