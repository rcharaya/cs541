package heap;

import chainexception.ChainException;

public class SpaceNotAvailableException extends ChainException {

	private static final long serialVersionUID = 1L;

	public SpaceNotAvailableException(Exception e, String message) {
		super(e, message);
	}

}
