package heap;

import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;

import java.io.IOException;
import java.util.TreeSet;

import chainexception.ChainException;

public class HeapFile {
	
	TreeSet<Record> fileDir = new TreeSet<Record>();
	
	PageId dirPageId;
	
	int recCnt = 0;
	
	PageId curPageId;
	
	public HeapFile(String filename) throws IOException, ClassNotFoundException{
		HFPage hfPage = new HFPage();
		dirPageId = Minibase.DiskManager.get_file_entry(filename);
		if(dirPageId==null){
			dirPageId = Minibase.BufferManager.newPage(hfPage, 1);
			Minibase.DiskManager.add_file_entry(filename, dirPageId);
			recCnt = 0;
			fileDir.add(new Record(new Integer(hfPage.getFreeSpace()), dirPageId));
			hfPage.setPrevPage(new PageId(0));
			Minibase.BufferManager.unpinPage(dirPageId, false);
		} else{
			Minibase.BufferManager.pinPage(dirPageId, hfPage, false);
			recCnt = hfPage.getPrevPage().pid; 
			fileDir.add(new Record(new Integer(hfPage.getFreeSpace()), hfPage.getCurPage()));
			PageId nextId = hfPage.getNextPage();
			Minibase.BufferManager.unpinPage(dirPageId, false);
			while(nextId.pid!=-1){
				Minibase.BufferManager.pinPage(nextId, hfPage, false);
				hfPage.setCurPage(nextId);
				fileDir.add(new Record(new Integer(hfPage.getFreeSpace()), hfPage.getCurPage()));
				nextId = hfPage.getNextPage();
				Minibase.BufferManager.unpinPage(hfPage.getCurPage(), false);
			}
		}
		curPageId=dirPageId;
	}
	
	public RID insertRecord(byte[] record) throws ChainException{
		if(record.length>GlobalConst.MAX_TUPSIZE){
			throw new SpaceNotAvailableException(new Exception(), "HeapFile.java: insertRecord() failed");
		}
		HFPage hfPage = new HFPage();
		PageId pageId;
		int size = record.length;
		Record match = fileDir.higher(new Record(size + 4)); 
		if(match!=null){
			pageId = match.getPageId();
			Minibase.BufferManager.pinPage(pageId, hfPage, match.getSize()==GlobalConst.PAGE_SIZE);
			fileDir.remove(match);
		} else{
			pageId = Minibase.BufferManager.newPage(hfPage, 1);
			hfPage.setCurPage(pageId);
			HFPage hp = new HFPage();
			Minibase.BufferManager.pinPage(curPageId, hp, false);
			hp.setCurPage(curPageId);
			hfPage.setPrevPage(curPageId);
			curPageId=hfPage.getCurPage();
			hp.setNextPage(curPageId);
			Minibase.BufferManager.unpinPage(hp.getCurPage(), true);
		}
		try{
			RID rid = hfPage.insertRecord(record);
			fileDir.add(new Record(new Integer(hfPage.getFreeSpace()), pageId));
			HFPage hp = new HFPage();
			Minibase.BufferManager.pinPage(dirPageId, hp, false);
			recCnt++;
			hp.setPrevPage(new PageId(recCnt));
			Minibase.BufferManager.unpinPage(dirPageId, true);
			return rid;
		} catch(Exception e){
			e.printStackTrace();
			throw new ChainException(e, "HeapFile.java: insertRecord() failed");
		} finally{
			Minibase.BufferManager.unpinPage(pageId, true);
		}
	}
	
	public Tuple getRecord(RID rid){
		HFPage page = new HFPage();
		Minibase.BufferManager.pinPage(rid.pageno, page, false);
		Tuple tuple = new Tuple(page.selectRecord(rid));
		Minibase.BufferManager.unpinPage(rid.pageno, false);
		return tuple;
	}
	
	public boolean updateRecord(RID rid, Tuple record) throws InvalidUpdateException{
		HFPage page = new HFPage();
		Minibase.BufferManager.pinPage(rid.pageno, page, false);
		try{
			if(page.getSlotLength(rid.slotno)!=record.getLength()){
				throw new Exception();
			}
			page.updateRecord(rid, record);
		} catch(Exception e){
			throw new InvalidUpdateException(e, "HeapFile.java: updateRecord() failed");
		} finally{
			Minibase.BufferManager.unpinPage(rid.pageno, true);
		}
		return true;
	}
	
	public boolean deleteRecord(RID rid){
		HFPage page = new HFPage();
		Minibase.BufferManager.pinPage(rid.pageno, page, false);
		fileDir.remove(new Record(new Integer(page.getFreeSpace()), rid.pageno));
		try{
			page.deleteRecord(rid);
			fileDir.add(new Record(new Integer(page.getFreeSpace()), rid.pageno, page.firstRecord()==null));		
		} catch(Exception e){
			e.printStackTrace();
			return false;
		} finally{
			Minibase.BufferManager.unpinPage(rid.pageno, true);
		}
		Minibase.BufferManager.pinPage(dirPageId, page, false);
		recCnt--;
		page.setPrevPage(new PageId(recCnt));
		Minibase.BufferManager.unpinPage(dirPageId, true);
		return true;
	}
	
	public int getRecCnt(){
		return recCnt;
	}

	public HeapScan openScan(){
		return new HeapScan(this);
	}
	
	public PageId getIterator(){
		return dirPageId;
	}

}
