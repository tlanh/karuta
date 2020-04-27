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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class StrToTime {

	private static final List<Matcher> matchers;

	static {
		matchers = new LinkedList<Matcher>();
		matchers.add(new NowMatcher());
		matchers.add(new TomorrowMatcher());
		matchers.add(new DayMatcher());
		matchers.add(new DateFormatMatcher(new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")));
		matchers.add(new DateFormatMatcher(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")));
		matchers.add(new DateFormatMatcher(new SimpleDateFormat("yyyy MM dd")));
		matchers.add(new DateFormatMatcher(new SimpleDateFormat("yyyy-MM-dd")));
		// add as many format as you want
	}

	// not thread-safe
	public static void registerMatcher(Matcher matcher) {
		matchers.add(matcher);
	}

	public static interface Matcher {
		public Date tryConvert(String input);
	}

	private static class DateFormatMatcher implements Matcher {

		private final DateFormat dateFormat;

		public DateFormatMatcher(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
		}

		public Date tryConvert(String input) {
			try {
				return dateFormat.parse(input);
			} catch (ParseException ex) {
				return null;
			}
		}
	}

	private static class NowMatcher implements Matcher {

		private final Pattern now = Pattern.compile("now");

		public Date tryConvert(String input) {
			if (now.matcher(input).matches()) {
				return new Date();
			} else {
				return null;
			}
		}
	}

	private static class TomorrowMatcher implements Matcher {

		private final Pattern tomorrow = Pattern.compile("tomorrow");

		public Date tryConvert(String input) {
			if (tomorrow.matcher(input).matches()) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_YEAR, +1);
				return calendar.getTime();
			} else {
				return null;
			}
		}
	}

	private static class DayMatcher implements Matcher {

		private final Pattern days = Pattern.compile("[\\-\\+]?\\d+ days");

		public Date tryConvert(String input) {

			if (days.matcher(input).matches()) {
				int d = Integer.parseInt(input.split(" ")[0]);
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_YEAR, d);
				return calendar.getTime();
			}

			return null;
		}
	}

	public static Date convert(String input) {
		for (Matcher matcher : matchers) {
			Date date = matcher.tryConvert(input);

			if (date != null) {
				return date;
			}
		}

		return null;
	}

	private StrToTime() {
		throw new UnsupportedOperationException();
	}
}
