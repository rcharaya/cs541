package bufmgr;

import global.Minibase;
import global.Page;
import global.PageId;

import java.io.IOException;

import diskmgr.FileIOException;
import diskmgr.InvalidPageNumberException;

class Frame implements Comparable<Frame> {
	int mIndex;
	PageId mPageId;
	int mPinCount;
	private boolean mDirty;
	
	enum FrameState {
		FREE, PINNED, UNPINNED 
	}
	public FrameState state;
	
	// Useful for implement LIRS
	int lastUsed; // R
	int reuseDistance; // RD
	
	public Frame(int index) {
		this.mIndex = index;
		mPageId = new PageId();
		mPinCount = 0;
		mDirty = false;
		state = FrameState.FREE;
	}
	
	int getLIRSWeight() {
		if(state == FrameState.FREE)
			return Integer.MAX_VALUE;	// This will ensure that free pages come first
		if(state == FrameState.PINNED)
			return Integer.MIN_VALUE;	// This will ensure that pinned pages come last
		return Math.max(LIRS.currentTime - lastUsed, reuseDistance);
	}

	@Override
	public int compareTo(Frame o) {
		// Return according to LIRS weight
		int lirsDiff = (o.getLIRSWeight() - getLIRSWeight());
		if(lirsDiff != 0) {
			return lirsDiff;
		}
		return (o.mIndex - mIndex);
	}
	
	void pin(PageId pageno) {
		mPageId.pid = pageno.pid;
		mPinCount = 1;
		mDirty = false;
	}
	
	void unpin(boolean dirty) throws PageNotPinnedException {
		if (mPinCount == 0) {
			throw new PageNotPinnedException();
		}
		mPinCount -= 1;
		mDirty |= dirty;
	}
	
	void free() throws PagePinnedException {
		if (mPinCount > 0) {
			throw new PagePinnedException();
		}
		mPageId.pid = -1;
		mPinCount = 0;
		mDirty = false;
	}
	
	boolean isUnpinned() {
		return state == FrameState.FREE
				|| state == FrameState.UNPINNED;
	}

	void flush(Page page) throws InvalidPageNumberException, FileIOException,
			IOException {
		if (mDirty) {
			Minibase.DiskManager.write_page(mPageId, page);
			mDirty = false;
		}
	}
}