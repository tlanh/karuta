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

/**
 * @author mathieu
 *
 */
public final class Consts {

	/** Opposite of {@link #FAILS}. */
	public static final boolean PASSES = true;
	/** Opposite of {@link #PASSES}. */
	public static final boolean FAILS = false;

	/** Opposite of {@link #FAILURE}. */
	public static final boolean SUCCESS = true;
	/** Opposite of {@link #SUCCESS}. */
	public static final boolean FAILURE = false;

	/**
	 * Useful for {@link String} operations, which return an index of <tt>-1</tt>
	 * when an item is not found.
	 */
	public static final int NOT_FOUND = -1;

	public static final boolean _PS_MODE_DEV_ = false;

	/** System property - <tt>line.separator</tt> */
	public static final String NEW_LINE = System.getProperty("line.separator");
	/** System property - <tt>file.separator</tt> */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	/** System property - <tt>path.separator</tt> */
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");

	public static final String EMPTY_STRING = "";
	public static final String SPACE = " ";
	public static final String TAB = "\t";
	public static final String SINGLE_QUOTE = "'";
	public static final String PERIOD = ".";
	public static final String DOUBLE_QUOTE = "\"";

	/* Debug only */
	public static final boolean PS_MODE_DEV = false;
	public static final boolean PS_MODE_DEMO_ = false;

	/* Compatibility warning */
	public static final boolean PS_DISPLAY_COMPATIBILITY_WARNING_ = false;
	public static final boolean PS_DEBUG_SQL = false;

	public static final boolean PS_DEBUG_PROFILING_ = false;

	public final static String currentDir = Consts.class.getProtectionDomain().getCodeSource().getLocation().getPath();

	/* settings java */
	public static final String _PS_TRANS_PATTERN_ = "(.*[^\\\\])";
	public static final String _PS_MIN_TIME_GENERATE_PASSWD_ = "360";
	public static final int CAN_LOAD_FILES = 1;

	public static final int PS_ROUND_UP = 0;
	public static final int PS_ROUND_DOWN = 1;
	public static final int PS_ROUND_HALF_UP = 2;
	public static final int PS_ROUND_HALF_DOWN = 3;
	public static final int PS_ROUND_HALF_EVEN = 4;
	public static final int PS_ROUND_HALF_ODD = 5;

	/* Backward compatibility */
	public static final int PS_ROUND_HALF = PS_ROUND_HALF_UP;

	public static final int MIN_PASSWD_LENGTH = 8;

	public static final String PS_JQUERY_VERSION = "1.11.0";
	
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
	/*
	 * private static String initCurrentDirectory() { try { return new
	 * File(currentDir + FILE_SEPARATOR + "..").getCanonicalPath(); } catch
	 * (IOException e) { throw new
	 * RuntimeException("Unexpected exception in Consts.initCurrentDirectory()"); }
	 * }
	 */
}
