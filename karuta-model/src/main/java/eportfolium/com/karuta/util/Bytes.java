/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

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
