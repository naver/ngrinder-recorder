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
package net.grinder.plugin.http.tcpproxyfilter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * EndPoint usage info class containing access count and filtering.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class EndPointInfo {
	private AtomicInteger count = new AtomicInteger(0);
	private boolean filtered = false;

	/**
	 * Constructor.
	 * 
	 * @param filtered
	 *            true if filtered
	 */
	public EndPointInfo(boolean filtered) {
		this.filtered = filtered;
	}

	public int getCount() {
		return count.get();
	}

	/**
	 * increase count.
	 */
	public void inc() {
		count.incrementAndGet();
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	/**
	 * Make the count zero.
	 */
	public void zero() {
		count.set(0);
	}

	public void dec() {
		count.decrementAndGet();
	}
}
