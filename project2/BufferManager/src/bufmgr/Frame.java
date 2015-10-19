package bufmgr;

import global.Minibase;
import global.Page;
import global.PageId;
import util.Util;
import chainexception.ChainException;

public class Frame implements Comparable<Frame> {
	private BufMgr mBufMgr;
	private Page mPage;
	private PageId mPageId;

	private int mPinCount;
	private boolean mDirty;

	// Useful for implement LIRS
	int lastUsed; // R
	int reuseDistance; // RD

	Frame(BufMgr bufMgr) {
		mBufMgr = bufMgr;
		mPage = new Page();
		empty();
	}

	boolean isDirty() {
		return mDirty;
	}

	void assertNotEmpty() {
		Util.myassert(mPageId != null, "empty frame. Expected non-empty");
	}

	void assertEmpty() {
		Util.myassert(mPageId == null, "Not empty frame. Expected empty");
	}

	// Returns true if mPinCount = 0
	boolean unPin(boolean dirty) throws ChainException {
		assertNotEmpty();
		if (mPinCount == 0) {
			throw new ChainException(null, "PageUnpinnedException");
		}
		Util.myassert(mPinCount > 0, "pin count can't be negative");
		mDirty = dirty;
		mPinCount--;
		updateLIRS();
		boolean ret = (mPinCount == 0);
		Util.println("F: unPin " + ret);
		return ret;
	}

	void pin() {
		Util.println("F: pin " + mPageId.pid);
		assertNotEmpty();
		updateLIRS();
		mPinCount++;
	}

	Page getPage() {
		return mPage;
	}

	public PageId getPageId() {
		return mPageId;
	}

	private void updateLIRS() {
		if (lastUsed > 0) {
			reuseDistance = mBufMgr.currentTime - lastUsed;
		}
		lastUsed = mBufMgr.currentTime;
		mBufMgr.currentTime++;
	}

	void flush() throws ChainException {
		Util.println("F: flush " + mPageId.pid);
		assertNotEmpty();
		if (mDirty) {
			try {
				Minibase.DiskManager.write_page(mPageId, mPage);
			} catch (Exception e) {
				throw new ChainException(e, "flush failed");
			}
		}
	}

	void empty() {
		Util.myassert(mPinCount == 0,
				"Pin count should be zero when emptying frame");
		if (mPageId != null) {
			Util.println("F: empty page " + mPageId.pid);
		}
		mDirty = false;
		mPageId = null;
	}

	int getLIRSWeight() {
		return Math.max(mBufMgr.currentTime - lastUsed, reuseDistance);
	}

	@Override
	public int compareTo(Frame o) {
		// Return according to LIRS weight
		return (o.getLIRSWeight() - getLIRSWeight());
	}

	void fetchPage(PageId pageno, Page page) throws ChainException {
		Util.println("F: fetchPage " + pageno.pid);
		assertEmpty();
		try {
			Minibase.DiskManager.read_page(pageno, mPage);
		} catch (Exception e) {
			empty();
			throw new ChainException(e, "Error fetching page");
		}
		mPageId = pageno;
		page.copyPage(mPage);
	}

	@Override
	public String toString() {
		if (mPageId != null)
			return "" + mPageId.pid;
		else
			return "FEmpty";
	}
}
