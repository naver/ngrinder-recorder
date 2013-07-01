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

import java.util.List;

import net.grinder.tools.tcpproxy.ConnectionDetails;
import net.grinder.tools.tcpproxy.EndPoint;

/**
 * Connection Filter interface.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public interface ConnectionFilter {

	/**
	 * Add a connection details into filter.
	 * 
	 * @param connectionDetails
	 *            connection details.
	 * @param b
	 * @return true if filtered.
	 */
	public boolean addConnectionDetails(ConnectionDetails connectionDetails, boolean recorded);

	/**
	 * Remove the filter entry by the given {@link EndPoint}.
	 * 
	 * @param endPoint
	 *            endPoint to be removed
	 */
	public void remove(EndPoint endPoint);

	/**
	 * Check if the connection to the given endPoint is filtered or not.
	 * 
	 * @param endPoint
	 *            {@link EndPoint}
	 * @return true if filtered
	 */
	public boolean isFiltered(EndPoint endPoint);

	/**
	 * Get the size of this filter list.
	 * 
	 * @return the size of this filter tlist
	 */
	public int getSize();

	/**
	 * Get the connection lists by insertion order.
	 * 
	 * @return all {@link EndPoint}s
	 */
	public List<EndPoint> getConnectionEndPoints();

	/**
	 * Get the connection end point on the given index.
	 * 
	 * @param index
	 *            index
	 * @return {@link EndPoint}
	 */
	public EndPoint getConnectionEndPoint(int index);

	/**
	 * Get the connection end point info on the given endPoint.
	 * 
	 * @param endPoint
	 *            endPoint
	 * @return endPointInfo containing filtering Y/N and count.
	 */
	public EndPointInfo getEndPointInfo(EndPoint endPoint);

	/**
	 * Turn on/off the filter on the given endPoint.
	 * 
	 * @param endPoint
	 *            endPoint to be set
	 * @param filter
	 *            true if filtered, false otherwise.
	 */
	void setFilter(EndPoint endPoint, boolean filter);

	/**
	 * Decrease the connection count on the given endpoint
	 * 
	 * @param endPoint
	 */
	void dec(EndPoint endPoint);

	/**
	 * Check the filter is changed recently.
	 * 
	 * @return true if changed
	 */
	public boolean isChanged();

	/**
	 * Reset all filters.
	 */
	void reset();

	/**
	 * Remove the filter info on the given {@link EndPoint}s.
	 * 
	 * @param endPoints
	 *            endPoint list
	 */
	public void remove(List<EndPoint> endPoints);

	/**
	 * Make the all connection count zero.
	 */
	public void makeZeroCount();
}
