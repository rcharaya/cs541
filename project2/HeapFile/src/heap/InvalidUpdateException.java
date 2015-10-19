package heap;

import chainexception.ChainException;

public class InvalidUpdateException extends ChainException {

	private static final long serialVersionUID = 1L;

	public InvalidUpdateException(Exception e, String message) {
		super(e, message);
	}
	
}