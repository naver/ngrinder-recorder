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
import net.grinder.tools.tcpproxy.TCPProxyFilter;

/**
 * {@link TCPProxyFilter} which allow to switch the nesting tcp proxy chain.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class SwitchableTcpProxyFilter implements TCPProxyFilter {

	private TCPProxyFilter proxyFilter;

	/**
	 * Constructor with nesting {@link TCPProxyFilter}.
	 * 
	 * @param proxyFilter
	 *            proxyFilter
	 */
	public SwitchableTcpProxyFilter(TCPProxyFilter proxyFilter) {
		this.proxyFilter = proxyFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.grinder.tools.tcpproxy.TCPProxyFilter#handle(net.grinder.tools.tcpproxy.ConnectionDetails
	 * , byte[], int)
	 */
	@Override
	public byte[] handle(ConnectionDetails connectionDetails, byte[] buffer, int bytesRead) throws FilterException {
		return proxyFilter.handle(connectionDetails, buffer, bytesRead);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.tools.tcpproxy.TCPProxyFilter#connectionOpened(net.grinder.tools.tcpproxy.
	 * ConnectionDetails)
	 */
	@Override
	public void connectionOpened(ConnectionDetails connectionDetails) throws FilterException {
		proxyFilter.connectionOpened(connectionDetails);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.tools.tcpproxy.TCPProxyFilter#connectionClosed(net.grinder.tools.tcpproxy.
	 * ConnectionDetails)
	 */
	@Override
	public void connectionClosed(ConnectionDetails connectionDetails) throws FilterException {
		proxyFilter.connectionClosed(connectionDetails);
	}

	/**
	 * Change the nesting {@link TCPProxyFilter} to the given proxyFilter.
	 * 
	 * @param proxyFilter
	 *            proxyFilter
	 */
	public void setTcpProxyFilter(TCPProxyFilter proxyFilter) {
		this.proxyFilter = proxyFilter;
	}
}
