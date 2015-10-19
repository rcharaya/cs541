package bufmgr;

import global.Minibase;
import global.Page;
import global.PageId;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import util.MyHashMap;
import util.MyLinkedList;
import util.MyPriorityQueue;
import util.Util;
import chainexception.ChainException;

public class BufMgr {
	// A frame is either in the list of empty frames or is unPinned
	private List<Frame> emptyFrames;

	// Sorted by LIRS weight
	private PriorityQueue<Frame> unPinnedPages;
	
	// We don't separately maintain a list of pinned pages. 
	// pageIdIndex will hold a reference to both pinned and unpinnedpages.
	private Map<Integer, Frame> pageIdIndex;

	// Since we just care about order of time and not actual time in
	// implementing
	// LIRS, we just keep currentTime as integer
	int currentTime;
	
	private int mNumBuf;
	
	/**
	 * Create the BufMgr object. Allocate pages (frames) for the buffer pool in
	 * main memory and make the buffer manage aware that the replacement policy
	 * is* specified by replacerArg (e.g., LH, Clock, LRU, MRU, LIRS, etc.).
	 *
	 * @param numbufs
	 *            number of buffers in the buffer pool
	 * @param lookAheadSize
	 *            number of pages to be looked ahead
	 * @param replacementPolicy
	 *            Name of the replacement policy
	 */
	public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy) {
		Util.println("BM: construct " + numbufs);
		emptyFrames = new MyLinkedList<Frame>();
		// Assert.myassert(replacementPolicy.equals("LIRS"), "Replacement policy has to be LIRS");
		pageIdIndex = new MyHashMap();
		unPinnedPages = new MyPriorityQueue<Frame>();
		mNumBuf = numbufs;
		while(numbufs != 0){
			numbufs --;
			emptyFrames.add(new Frame(this));
		}
	};

	/**
	 * Pin a page. First check if this page is already in the buffer pool. If it
	 * is, increment the pin_count and return a pointer to this page. If the
	 * pin_count was 0 before the call, the page was a replacement candidate,
	 * but is no longer a candidate. If the page is not in the pool, choose a
	 * frame (from the set of replacement candidates) to hold this page, read
	 * the page (using the appropriate method from {\em diskmgr} package) and
	 * pin it. Also, must write out the old page in chosen frame if it is dirty
	 * before reading new page.__ (You can assume that emptyPage==false for this
	 * assignment.)
	 *
	 * @param pageno
	 *            page number in the Minibase.
	 * @param page
	 *            the pointer point to the page.
	 * @param emptyPage
	 *            true (empty page) false (non-empty page)
	 * @throws ChainException 
	 */
	public void pinPage(PageId pageno, Page page, boolean emptyPage)
			throws ChainException {
		Util.println("BM: pinPage " + pageno.pid);
		Frame frame = pageIdIndex.get(pageno.pid);
		if(frame == null) {
			frame = getFrame();
			frame.fetchPage(pageno, page);
			pageIdIndex.put(pageno.pid, frame);
		} else {
			unPinnedPages.remove(frame);
		}
		frame.pin();
		page.copyPage(frame.getPage());
	};

	/**
	 * Unpin a page specified by a pageId. This method should be called with
	 * dirty==true if the client has modified the page. If so, this call should
	 * set the dirty bit for this frame. Further, if pin_count>0, this method
	 * should decrement it. If pin_count=0 before this call, throw an exception
	 * to report error. (For testing purposes, we ask you to throw an exception
	 * named PageUnpinnedException in case of error.)
	 *
	 * @param pageno
	 *            page number in the Minibase.
	 * @param dirty
	 *            the dirty bit of the frame
	 */
	public void unpinPage(PageId pageno, boolean dirty) throws ChainException {
		Util.println("BM: unpinPage " + pageno.pid + " " + dirty);
		Frame frame = pageIdIndex.get(pageno.pid);
		if(frame.unPin(dirty)) {
			unPinnedPages.add(frame);
		}
	};
	
	private Frame getFrame() throws ChainException {
		Frame frame = null;
		if(! emptyFrames.isEmpty()) {
			frame = emptyFrames.remove(0);
		} else {
			frame = unPinnedPages.poll();
			if(frame == null) {
				throw new ChainException(null, "No empty pages. Can't pin new");
			} else {
				pageIdIndex.remove(frame.getPageId().pid);
				frame.flush();
				frame.empty();
			}
		}
		return frame;
	}

	/**
	 * Allocate new pages.* Call DB object to allocate a run of new pages and
	 * find a frame in the buffer pool for the first page and pin it. (This call
	 * allows a client of the Buffer Manager to allocate pages on disk.) If
	 * buffer is full, i.e., you can't find a frame for the first page, ask DB
	 * to deallocate all these pages, and return null.
	 *
	 * @param firstpage
	 *            the address of the first page.
	 * @param howmany
	 *            total number of allocated new pages.
	 *
	 * @return the first page id of the new pages.__ null, if error.
	 * @throws ChainException 
	 */
	public PageId newPage(Page firstpage, int howmany) throws ChainException {
		Util.println("BM: newPage: howmany " + howmany);
		PageId pageno = new PageId();
		Frame frame = null;
		
		try { 
			frame = getFrame();
			Minibase.DiskManager.allocate_page(pageno, howmany);
			Util.println("BM: newPage: pageno " + pageno);
			frame.fetchPage(pageno, firstpage);
			frame.pin();
			pageIdIndex.put(pageno.pid, frame);
		} catch (ChainException e) {
			if(frame != null) {
				frame.empty();
				emptyFrames.add(frame);
			}
			throw e;
		} catch (IOException e) {
			throw new ChainException(e, "new page alloc");
		}
		return pageno;
	}

	/**
	 * This method should be called to delete a page that is on disk. This
	 * routine must call the method in diskmgr package to deallocate the page.
	 *
	 * @param globalPageId
	 *            the page number in the data base.
	 * @throws ChainException 
	 */
	public void freePage(PageId globalPageId) throws ChainException {
		Util.println("BM: freePage " + globalPageId.pid);
		// Assuming that frame is already completely unpinned or empty
		
		Frame frame = pageIdIndex.get(globalPageId.pid);
		if(frame != null) {
			// It should be in unPinnedPages
			unPinnedPages.remove(frame);
			pageIdIndex.remove(globalPageId.pid);
			frame.empty();
			emptyFrames.add(frame);
		}
		Minibase.DiskManager.deallocate_page(globalPageId);
	};

	/**
	 * Used to flush a particular page of the buffer pool to disk. This method
	 * calls the write_page method of the diskmgr package.
	 *
	 * @param pageid
	 *            the page number in the database.
	 */
	public void flushPage(PageId pageid) throws ChainException{
		Util.println("BM: flushPage " + pageid.pid);
		Frame frame = pageIdIndex.get(pageid.pid);
		frame.flush();
	};

	/**
	 * Used to flush all dirty pages in the buffer pool to disk
	 * @throws ChainException 
	 *
	 */
	public void flushAllPages() throws ChainException {
		Util.println("BM: flushAllPages ");
		/*Iterator<Frame> frameIter = unPinnedPages.iterator();
		while(frameIter.hasNext()) {
			Frame frame = frameIter.next();
			frame.flush();
		}*/
	};

	/**
	 * Returns the total number of buffer frames.
	 */
	public int getNumBuffers() {
		return mNumBuf;
	};

	/**
	 * Returns the total number of unpinned buffer frames.
	 */
	public int getNumUnpinned() {
		return unPinnedPages.size() + emptyFrames.size();
	};
};
