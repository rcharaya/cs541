package heap;

public class Tuple {
	byte[] data;
	
	public Tuple(byte[] data, int offset, int length) {
		this.data = new byte[length];
		int l = length>data.length ? data.length : length;
		System.arraycopy(data, offset, this.data, 0, l);
	}
	
	public Tuple(byte[] data) {
		this.data=data;
	}
	
	public Tuple() {
	}

	public int getLength(){
		return data.length;
	}
	
	public byte[] getTupleByteArray(){
		return data;
	}
	
}
