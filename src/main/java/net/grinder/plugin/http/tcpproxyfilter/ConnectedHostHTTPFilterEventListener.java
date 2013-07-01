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
import net.grinder.util.NoOp;

import org.picocontainer.Disposable;

/**
 * {@link HTTPFilterEventListener} impl which is aware of connection filtering.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ConnectedHostHTTPFilterEventListener implements HTTPFilterEventListener, Disposable {
	private final ConnectionFilter connectionFilter;
	private final HTTPFilterEventListener connectionCache;

	/**
	 * Constructor.
	 * 
	 * @param connectionFilter
	 *            connection filter
	 * @param connectionCache
	 *            {@link HTTPFilterEventListener} which will be delegated to
	 */
	public ConnectedHostHTTPFilterEventListener(ConnectionFilter connectionFilter, ConnectionCache connectionCache) {
		this.connectionFilter = connectionFilter;
		this.connectionCache = connectionCache;
	}

	@Override
	public void dispose() {
		if (this.connectionCache instanceof Disposable) {
			((Disposable) connectionCache).dispose();
		}
	}

	@Override
	public void open(ConnectionDetails connectionDetails) {
		if (connectionFilter.addConnectionDetails(connectionDetails, true)) {
			return;
		}
		try {
			connectionCache.open(connectionDetails);
		} catch (IllegalArgumentException e) {
			NoOp.noOp();
		}
	}

	@Override
	public void request(ConnectionDetails connectionDetails, byte[] buffer, int bytesRead) {
		if (connectionFilter.isFiltered(connectionDetails.getRemoteEndPoint())) {
			return;
		}
		try {
			connectionCache.request(connectionDetails, buffer, bytesRead);
		} catch (IllegalArgumentException e) {
			NoOp.noOp();
		}
	}

	@Override
	public void response(ConnectionDetails connectionDetails, byte[] buffer, int bytesRead) {
		if (connectionFilter.isFiltered(connectionDetails.getRemoteEndPoint())) {
			return;
		}
		try {
			connectionCache.response(connectionDetails, buffer, bytesRead);
		} catch (IllegalArgumentException e) {
			NoOp.noOp();
		}
	}

	@Override
	public void close(ConnectionDetails connectionDetails) {
		if (connectionFilter.isFiltered(connectionDetails.getRemoteEndPoint())) {
			return;
		}
		try {
			connectionCache.close(connectionDetails);
		} catch (IllegalArgumentException e) {
			NoOp.noOp();
		}
	}
}
