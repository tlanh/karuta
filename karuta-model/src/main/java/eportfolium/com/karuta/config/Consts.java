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

package eportfolium.com.karuta.config;

public final class Consts {

	/**
	 * Useful for {@link String} operations, which return an index of <tt>-1</tt>
	 * when an item is not found.
	 */
	public static final int NOT_FOUND = -1;
	
	/* Directories */
	public static final String _PS_ROOT_DIR_ = "classpath:/META-INF/assets";
	public static final String _PS_CORE_DIR_ = "classpath:/META-INF/assets/core";
	public static final String _PS_MAIL_DIR_ =  _PS_CORE_DIR_ + "/mails/";
	public static final String _PS_IMG_DIR_ = _PS_ROOT_DIR_ + "/images/";

	// PRIVATE //

	/**
	 * The caller references the constants using <tt>Consts.EMPTY_STRING</tt>, and
	 * so on. Thus, the caller should be prevented from constructing objects of this
	 * class, by declaring this private constructor.
	 */
	private Consts() {
		// this prevents even the native class from
		// calling this ctor as well :
		throw new AssertionError();
	}
}
