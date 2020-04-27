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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;

public class InMemoryCache<K, T> {

	private long timeToLive;
	private LRUMap cacheMap;

	protected class CacheObject {
		public long lastAccessed = System.currentTimeMillis();
		public T value;

		protected CacheObject(T value) {
			this.value = value;
		}
	}

	public InMemoryCache(long pTimeToLive, final long pTimerInterval, int maxItems) {
		this.timeToLive = pTimeToLive * 1000;

		cacheMap = new LRUMap(maxItems);

		if (timeToLive > 0 && pTimerInterval > 0) {

			Thread t = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(pTimerInterval * 1000);
						} catch (InterruptedException ex) {
						}
						cleanup();
					}
				}
			});

			t.setDaemon(true);
			t.start();
		}
	}

	public void put(K key, T value) {
		synchronized (cacheMap) {
			cacheMap.put(key, new CacheObject(value));
		}
	}

	@SuppressWarnings("unchecked")
	public T get(K key) {
		synchronized (cacheMap) {
			CacheObject c = (CacheObject) cacheMap.get(key);

			if (c == null)
				return null;
			else {
				c.lastAccessed = System.currentTimeMillis();
				return c.value;
			}
		}
	}

	public void remove(K key) {
		synchronized (cacheMap) {
			cacheMap.remove(key);
		}
	}

	public int size() {
		synchronized (cacheMap) {
			return cacheMap.size();
		}
	}

	@SuppressWarnings("unchecked")
	public void cleanup() {

		long now = System.currentTimeMillis();
		List<K> deleteKey = null;

		synchronized (cacheMap) {
			MapIterator itr = cacheMap.mapIterator();

			deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
			K key = null;
			CacheObject c = null;

			while (itr.hasNext()) {
				key = (K) itr.next();
				c = (CacheObject) itr.getValue();

				if (c != null && (now > (timeToLive + c.lastAccessed))) {
					deleteKey.add(key);
				}
			}
		}

		for (K key : deleteKey) {
			synchronized (cacheMap) {
				cacheMap.remove(key);
			}

			Thread.yield();
		}
	}
}
