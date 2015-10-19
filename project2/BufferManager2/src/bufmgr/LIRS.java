package bufmgr;

import java.util.TreeSet;

import bufmgr.Frame.FrameState;

public class LIRS {
	public static int currentTime = 0;
	// Sorted by LIRS weight
	private TreeSet<Frame> unPinnedPages;

	protected LIRS(BufMgr bufmgr) {
		unPinnedPages = new TreeSet<Frame>();
		for (Frame frame : bufmgr.mFrames) {
			unPinnedPages.add(frame);
		}
	}
	
	public void freePage(Frame frame) {
		unPinnedPages.remove(frame);
		frame.state = FrameState.FREE;
		unPinnedPages.add(frame);
	}

	public void pinPage(Frame frame) {
		unPinnedPages.remove(frame);
		frame.state = FrameState.PINNED;
		unPinnedPages.add(frame);
	}

	public void unpinPage(Frame frame) {
		if (frame.mPinCount == 0) {
			unPinnedPages.remove(frame);
			frame.state = FrameState.UNPINNED;
			if (frame.lastUsed > 0) {
				frame.reuseDistance = currentTime - frame.lastUsed;
			}
			frame.lastUsed = currentTime;
			currentTime++;
			unPinnedPages.add(frame);
		}
	}

	public int getEmptyFrame() {
		Frame frame = unPinnedPages.last();
		if (frame.state == FrameState.PINNED) {
			return -1;
		}
		return frame.mIndex;
	}
}
