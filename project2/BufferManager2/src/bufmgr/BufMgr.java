package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.io.IOException;
import java.util.Map;

import chainexception.ChainException;
import diskmgr.FileIOException;
import diskmgr.InvalidPageNumberException;

public class BufMgr implements GlobalConst {
	protected Page[] mPages;
	protected Frame[] mFrames;
	protected Map<Integer, Frame> mFrameMap;
	protected LIRS mLirs;

	public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy) {
		mPages = new Page[numbufs];
		mFrames = new Frame[numbufs];
		for (int i = 0; i < numbufs; i++) {
			mPages[i] = new Page();
			mFrames[i] = new Frame(i);
		}

		mFrameMap = new MyHashTable();
		mLirs = new LIRS(this);
	}

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
			throws InvalidPageNumberException, FileIOException, IOException,
			BufferPoolExceededException {
		Frame frame = mFrameMap.get(pageno.pid);
		if (frame != null) {
			frame.mPinCount += 1;
			mLirs.pinPage(frame);
			page.setPage(mPages[frame.mIndex]);
			return;
		}

		int frameno = mLirs.getEmptyFrame();
		if (frameno < 0) {
			throw new BufferPoolExceededException();
		}
		frame = mFrames[frameno];

		if (frame.mPageId.pid != -1) {
			mFrameMap.remove(frame.mPageId.pid);
			frame.flush(mPages[frame.mIndex]);
		}

		Minibase.DiskManager.read_page(pageno, mPages[frameno]);
		page.setPage(mPages[frameno]);

		frame.pin(pageno);
		mLirs.pinPage(frame);		
		mFrameMap.put((pageno.pid), frame);
	}

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
		Frame frame = (Frame) mFrameMap.get((pageno.pid));
		if (frame == null) {
			throw new HashEntryNotFoundException();
		}

		frame.unpin(dirty);
		mLirs.unpinPage(frame);
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
	public PageId newPage(Page firstpg, int run_size) throws ChainException,
			IOException {
		PageId firstid = Minibase.DiskManager.allocate_page(run_size);
		try {
			pinPage(firstid, firstpg, true);
		} catch (RuntimeException exc) {
			for (int i = 0; i < run_size; i++) {
				firstid.pid += i;
				Minibase.DiskManager.deallocate_page(firstid);
			}

			throw exc;
		}

		return firstid;
	}

	/**
	 * This method should be called to delete a page that is on disk. This
	 * routine must call the method in diskmgr package to deallocate the page.
	 *
	 * @param globalPageId
	 *            the page number in the data base.
	 * @throws ChainException
	 */
	public void freePage(PageId pageno) throws ChainException {
		Frame frame = mFrameMap.get(pageno.pid);
		if (frame != null) {
			frame.free();
			mFrameMap.remove(pageno.pid);
			mLirs.freePage(frame);
		}

		Minibase.DiskManager.deallocate_page(pageno);
	}

	/**
	 * Used to flush a particular page of the buffer pool to disk. This method
	 * calls the write_page method of the diskmgr package.
	 *
	 * @param pageid
	 *            the page number in the database.
	 */
	public void flushPage(PageId pageno) throws InvalidPageNumberException,
			FileIOException, IOException {
		Frame frame = mFrameMap.get(pageno.pid);
		if(frame == null) {
			
		} else {
			frame.flush(mPages[frame.mIndex]);
		}
	}

	/**
	 * Used to flush all dirty pages in the buffer pool to disk
	 * 
	 * @throws ChainException
	 *
	 */
	public void flushAllPages() throws InvalidPageNumberException,
			FileIOException, IOException {
		for (int i = 0; i < mPages.length; i++) {
			mFrames[i].flush(mPages[i]);
		}
	}

	/**
	 * Returns the total number of buffer frames.
	 */
	public int getNumBuffers() {
		return mPages.length;
	}

	/**
	 * Returns the total number of unpinned buffer frames.
	 */
	public int getNumUnpinned() {
		int cnt = 0;
		for (int i = 0; i < mPages.length; i++) {
			if (mFrames[i].isUnpinned()) {
				cnt++;
			}
		}
		return cnt;
	}
}