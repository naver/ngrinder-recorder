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

import net.grinder.tools.tcpproxy.ConnectionDetails;
import net.grinder.tools.tcpproxy.NullFilter;

/**
 * {@link NullFilter} but be aware of connection opening status.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ConnectionAwareNullRequestFilter extends NullFilter {

	private final ConnectionFilter connectionFilter;

	/**
	 * Constructor.
	 * 
	 * @param connectionFilter
	 *            {@link ConnectionFilter}
	 */
	public ConnectionAwareNullRequestFilter(ConnectionFilter connectionFilter) {
		this.connectionFilter = connectionFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.tools.tcpproxy.NullFilter#connectionOpened(net.grinder.tools.tcpproxy.
	 * ConnectionDetails)
	 */
	@Override
	public void connectionOpened(ConnectionDetails connectionDetails) {
		connectionFilter.addConnectionDetails(connectionDetails, false);
	}

}
