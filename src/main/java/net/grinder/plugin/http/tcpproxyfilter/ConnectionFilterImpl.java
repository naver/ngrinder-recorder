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

import static net.grinder.util.CollectionUtils.newArrayList;
import static net.grinder.util.CollectionUtils.newConcurrentHashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.grinder.tools.tcpproxy.ConnectionDetails;
import net.grinder.tools.tcpproxy.EndPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection Filter Implementation.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ConnectionFilterImpl implements ConnectionFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFilterImpl.class);

	private Map<EndPoint, EndPointInfo> connectionDetails = newConcurrentHashMap();
	private List<EndPoint> connections = newArrayList();
	private final AtomicBoolean changed = new AtomicBoolean(false);

	/**
	 * Constructor.
	 */
	public ConnectionFilterImpl() {

	}

	@Override
	public boolean addConnectionDetails(ConnectionDetails details, boolean recodedConnection) {
		EndPoint remoteEndPoint = details.getRemoteEndPoint();
		EndPointInfo endPointRef = null;
		synchronized (this) {
			endPointRef = addEndPoint(remoteEndPoint);
			changed.set(true);
		}
		if (recodedConnection && !endPointRef.isFiltered()) {
			endPointRef.inc();
		}
		return (endPointRef.isFiltered());
	}

	private EndPointInfo addEndPoint(EndPoint remoteEndPoint) {
		EndPointInfo endPointInfo = connectionDetails.get(remoteEndPoint);
		synchronized (this) {
			if (endPointInfo == null) {
				endPointInfo = new EndPointInfo(false);
				connectionDetails.put(remoteEndPoint, endPointInfo);
				connections.add(remoteEndPoint);
				LOGGER.debug("adding remote end point : {}", remoteEndPoint);
			}
		}
		return endPointInfo;
	}

	@Override
	public void setFilter(EndPoint endPoint, boolean filter) {
		synchronized (this) {
			addEndPoint(endPoint).setFiltered(filter);
			changed.set(true);
		}
	}

	@Override
	public boolean isChanged() {
		synchronized (this) {
			return changed.compareAndSet(true, false);
		}
	}

	@Override
	public void remove(EndPoint endPoint) {
		connectionDetails.remove(endPoint);
		connections.remove(endPoint);
		changed.set(true);
	}

	@Override
	public void reset() {
		synchronized (this) {
			connectionDetails.clear();
			connections.clear();
			changed.set(true);
		}
	}

	public Map<EndPoint, EndPointInfo> getConnectionDetails() {
		return connectionDetails;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<EndPoint, EndPointInfo> each : connectionDetails.entrySet()) {
			builder.append(each.getKey().toString()).append(":").append(each.getValue()).append("\n");
		}
		return builder.toString();
	}

	@Override
	public boolean isFiltered(EndPoint endPoint) {
		EndPointInfo filterCount = connectionDetails.get(endPoint);
		if (filterCount != null && filterCount.isFiltered()) {
			return true;
		}
		return false;
	}

	@Override
	public int getSize() {
		return connections.size();
	}

	public List<EndPoint> getConnectionEndPoints() {
		return connections;
	}

	@Override
	public EndPoint getConnectionEndPoint(int index) {
		return connections.get(index);
	}

	@Override
	public EndPointInfo getEndPointInfo(EndPoint endPoint) {
		return connectionDetails.get(endPoint);
	}

	@Override
	public void remove(List<EndPoint> endPoints) {
		for (EndPoint each : endPoints) {
			remove(each);
		}
		changed.set(true);
	}

	@Override
	public void makeZeroCount() {
		for (EndPointInfo eachValue : connectionDetails.values()) {
			eachValue.zero();
		}
		changed.set(true);
	}

	@Override
	public void dec(EndPoint endPoint) {
		EndPointInfo endPointInfo = connectionDetails.get(endPoint);
		if (endPointInfo != null) {
			endPointInfo.dec();
			changed.set(true);
		}
	}
}
