package eportfolium.com.karuta.util;

import java.io.Serializable;
import java.util.Arrays;

public final class Bytes implements Serializable {
	private static final long serialVersionUID = -8586686601913496649L;

	private final byte[] _bytes;

	public Bytes(byte[] bytes) {
		_bytes = bytes;
	}

	public boolean equals(Object other) {
		if (other == null || !(other instanceof Bytes))
			return false;
		return Arrays.equals(((Bytes) other)._bytes, _bytes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(_bytes);
	}

	public byte[] getBytes() {
		byte[] clone = new byte[_bytes.length];
		System.arraycopy(_bytes, 0, clone, 0, _bytes.length);
		return clone;
	}

	@Override
	public String toString() {
		return "Bytes [_bytes=" + Arrays.toString(_bytes) + "]";
	}

}
