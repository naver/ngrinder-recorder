/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.grinder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collection Utility class.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public abstract class CollectionUtils {
	/**
	 * Convenient method to create {@link ArrayList} instance.
	 * 
	 * @param <T>
	 *            type
	 * @return new {@link ArrayList}
	 */
	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}

	/**
	 * Convenient method to create {@link HashSet} instance.
	 * 
	 * @param <T>
	 *            type
	 * @return new {@link HashSet}
	 */
	public static <T> Set<T> newHashSet() {
		return new HashSet<T>();
	}

	/**
	 * Convenient method to create {@link HashMap} instance.
	 * 
	 * @param <K>
	 *            key type
	 * @param <V>
	 *            value type
	 * @return new {@link HashMap}
	 */
	public static <K, V> Map<K, V> newHashMap() {
		return new HashMap<K, V>();
	}
	
	/**
	 * Convenient method to create {@link ConcurrentHashMap} instance.
	 * 
	 * @param <K>
	 *            key type
	 * @param <V>
	 *            value type
	 * @return new {@link HashMap}
	 */
	public static <K, V> Map<K, V> newConcurrentHashMap() {
		return new ConcurrentHashMap<K, V>();
	}

	/**
	 * Convenient method to create {@link IdentityHashMap} instance.
	 * 
	 * @param <K>
	 *            key type
	 * @param <V>
	 *            value type
	 * @return created {@link IdentityHashMap}
	 */
	public static <K, V> Map<K, V> newIdentityHashMap() {
		return new IdentityHashMap<K, V>();
	}
}
