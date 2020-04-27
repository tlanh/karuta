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

package eportfolium.com.karuta.consumer.util.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import eportfolium.com.karuta.util.PhpUtil;


/**
 * HQL query builder
 *
 */
public class HQLQueryBuilder {

	/**
	 * List of data to build the query
	 *
	 * @var array
	 */
	protected Map<String, List<String>> query;

	public HQLQueryBuilder() {
		query = new HashMap<String, List<String>>();
		query.put("type", Arrays.asList("SELECT"));
		query.put("select", new ArrayList<String>());
		query.put("from", new ArrayList<String>());
		query.put("join", new ArrayList<String>());
		query.put("where", new ArrayList<String>());
		query.put("group", new ArrayList<String>());
		query.put("having", new ArrayList<String>());
		query.put("order", new ArrayList<String>());
	}

	/**
	 * Sets type of the query
	 *
	 * @param string $type SELECT|DELETE
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder type(String type) {
		Set<String> types = new HashSet<String>();
		types.addAll(Arrays.asList("SELECT", "DELETE"));

		if (StringUtils.isNotEmpty(type) && types.contains(type)) {
			query.put("type", Arrays.asList(type));
		}

		return this;
	}

	/**
	 * Adds fields to SELECT clause
	 *
	 * @param string fields List of fields to concat to other fields
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder select(String fields) {
		if (StringUtils.isNotEmpty(fields)) {
			query.get("select").add(fields);
		}
		return this;
	}

	/**
	 * Sets table for FROM clause
	 *
	 * @param string table Table name
	 * @param string|null alias Table alias
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder from(String table, String alias) {
		if (StringUtils.isNotEmpty(table)) {
			if (CollectionUtils.isEmpty(query.get("from"))) {
				query.put("from", new ArrayList<String>());
			}
			query.get("from").add(StringUtils.capitalize(table)
					+ (StringUtils.isNotEmpty(alias) ? " " + alias : ""));
		}
		return this;
	}

	public HQLQueryBuilder from(String table) {
		return from(table, null);
	}

	/**
	 * Adds JOIN clause E.g. join('RIGHT JOIN '+Settings._DB_PREFIX_+'product p ON ...');
	 *
	 * @param string $join Complete string
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder join(String join) {
		if (StringUtils.isNotEmpty(join)) {
			query.get("join").add(join);
		}
		return this;
	}

	public HQLQueryBuilder leftJoin(String table, String alias) {
		return leftJoin(table, alias, null);
	}

	/**
	 * Adds a LEFT JOIN clause
	 *
	 * @param string table Table name (without prefix)
	 * @param string|null alias Table alias
	 * @param string|null with ON clause
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder leftJoin(String table, String alias, String with) {
		return join("LEFT JOIN " + table + (StringUtils.isNotEmpty(alias) ? " " + alias + " " : " ")
				+ (StringUtils.isNotEmpty(with) ? " WITH " + with : ""));
	}

	public HQLQueryBuilder leftJoin(String table, String alias, boolean fetch) {
		if (fetch) {
			return join("LEFT JOIN FETCH " + table + (StringUtils.isNotEmpty(alias) ? " " + alias + " " : " "));
		}
		return leftJoin(table, alias, null);
	}

	/**
	 * Adds an INNER JOIN clause E.g. $this->innerJoin('product p ON ...')
	 *
	 * @param string table Table name (without prefix)
	 * @param string|null alias Table alias
	 * @param string|null $on ON clause
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder innerJoin(String table, String alias, String with) {
		return join("INNER JOIN " + table + (StringUtils.isNotEmpty(alias) ? " " + alias + " " : " ")
				+ (StringUtils.isNotEmpty(with) ? " WITH " + with : ""));
	}

	public HQLQueryBuilder innerJoin(String table, String alias) {
		return innerJoin(table, alias, null);
	}

	/**
	 * Adds a LEFT OUTER JOIN clause
	 *
	 * @param string table Table name (without prefix)
	 * @param string|null alias Table alias
	 * @param string|null $on ON clause
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder leftOuterJoin(String table, String alias, String with) {
		return join("LEFT OUTER JOIN " + table + "" + (StringUtils.isNotEmpty(alias) ? " " + alias : "")
				+ (StringUtils.isNotEmpty(with) ? " WITH " + with : ""));
	}

	/**
	 * Adds a NATURAL JOIN clause
	 *
	 * @param string table Table name (without prefix)
	 * @param string|null alias Table alias
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder naturalJoin(String table, String alias) {
		return join("NATURAL JOIN " + (table) + "" + (StringUtils.isNotEmpty(alias) ? " " + alias : ""));
	}

	/**
	 * Adds a RIGHT JOIN clause
	 *
	 * @param string table Table name (without prefix)
	 * @param string|null alias Table alias
	 * @param string|null $on ON clause
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder rightJoin(String table, String alias, String with) {
		return join("RIGHT JOIN " + (table) + "" + (StringUtils.isNotEmpty(alias) ? " " + alias + "" : "")
				+ (StringUtils.isNotEmpty(with) ? " WITH " + with : ""));
	}

	/**
	 * Adds a restriction in WHERE clause (each restriction will be separated by AND statement)
	 *
	 * @param string restriction
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder where(String restriction) {
		if (StringUtils.isNotEmpty(restriction)) {
			query.get("where").add(restriction);
		}

		return this;
	}

	/**
	 * Adds a restriction in HAVING clause (each restriction will be separated by AND statement)
	 *
	 * @param string restriction
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder having(String restriction) {
		if (StringUtils.isNotEmpty(restriction)) {
			query.get("having").add(restriction);
		}

		return this;
	}

	/**
	 * Adds an ORDER BY restriction
	 *
	 * @param string fields List of fields to sort. E.g. $this->order("myField, b.mySecondField DESC")
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder orderBy(String fields) {
		if (StringUtils.isNotEmpty(fields)) {
			query.get("order").add(fields);
		}

		return this;
	}

	/**
	 * Adds a GROUP BY restriction
	 *
	 * @param string fields List of fields to group. E.g. $this->group("myField1, myField2")
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder groupBy(String fields) {
		if (StringUtils.isNotEmpty(fields)) {
			query.get("group").add(fields);
		}

		return this;
	}

	/**
	 * Sets query offset and limit
	 *
	 * @param int limit
	 * @param int offset
	 *
	 * @return DbQuery
	 */
	public HQLQueryBuilder limit(int limit, int offset) {
		offset = (int) offset;
		if (offset < 0) {
			offset = 0;
		}

		query.get("limit").addAll(Arrays.asList("offset " + offset, "limit " + limit));

		return this;
	}

	/**
	 * Generates query and return SQL string
	 *
	 * @return string
	 * @throws PrestaShopException
	 */
	public String build() {
		String sql = new String();
		if (query.get("type").get(0).equals("SELECT")) {
			sql = "SELECT "
					+ (CollectionUtils.isNotEmpty(query.get("select")) ? PhpUtil.implode(",\n", query.get("select"))
							: "*")
					+ "\n";
		}
		else {
			sql = query.get("type").get(0) + " ";
		}

		if (CollectionUtils.isEmpty(query.get("from"))) {
			throw new RuntimeException("Table name not set in DbQuery object. Cannot build a valid SQL query.");
		}

		sql += "FROM " + PhpUtil.implode(", ", query.get("from")) + "\n";

		if (CollectionUtils.isNotEmpty(query.get("join"))) {
			sql += PhpUtil.implode("\n", query.get("join")) + "\n";
		}

		if (CollectionUtils.isNotEmpty(query.get("where"))) {
			sql += "WHERE (" + PhpUtil.implode(") AND (", query.get("where")) + ")\n";
		}

		if (CollectionUtils.isNotEmpty(query.get("group"))) {
			sql += "GROUP BY " + PhpUtil.implode(", ", query.get("group")) + "\n";
		}

		if (CollectionUtils.isNotEmpty(query.get("having"))) {
			sql += "HAVING (" + PhpUtil.implode(") AND (", query.get("having")) + ")\n";
		}

		if (CollectionUtils.isNotEmpty(query.get("order"))) {
			sql += "ORDER BY " + PhpUtil.implode(", ", query.get("order")) + "\n";
		}

		return sql;
	}

	/**
	 * Converts object to string
	 *
	 * @return string
	 */
	public String toString() {
		return build();
	}
}
