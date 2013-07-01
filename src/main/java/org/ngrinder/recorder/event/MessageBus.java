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
package org.ngrinder.recorder.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * nGrinder recorder message bus class.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author AlexQin
 * @version 1.0
 */
public final class MessageBus {

	private static MessageBus instance;
	private static final Object NA = new Object();
	@SuppressWarnings("rawtypes")
	private final Map<Topic, Object> publishers;
	private final List<MessageBusConnection> connections;

	/**
	 * Get message but instance.
	 * 
	 * @return Message but instance.
	 */
	public static MessageBus getInstance() {
		if (instance == null) {
			instance = new MessageBus();
		}
		return instance;
	}

	/**
	 * Create message but connection.
	 * 
	 * @return Message but connection
	 */
	public MessageBusConnection connect() {
		MessageBusConnection connection = new MessageBusConnection(this);
		connections.add(connection);
		return connection;
	}

	/**
	 * Disconnection message but.
	 * 
	 * @param connection
	 *            Connection object
	 */
	public void disconnect(MessageBusConnection connection) {
		connections.remove(connection);
	}

	/**
	 * Get publisher.
	 * 
	 * @param topic
	 *            Topic object
	 * @param <L>
	 *            event listener type
	 * @return Publisher
	 */
	@SuppressWarnings({ "unchecked" })
	public <L> L getPublisher(final Topic<L> topic) {
		L publisher = (L) publishers.get(topic);
		if (publisher == null) {
			Class<L> listenerClass = topic.getListenerClass();
			InvocationHandler handler = new InvocationHandler() {
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					List<MessageBusConnection> copyConnections = new ArrayList<MessageBusConnection>(connections);
					for (MessageBusConnection connection : copyConnections) {
						connection.notifySubscribers(topic, method, args);
					}
					copyConnections.clear();

					String methodName = method.getName();
					if (methodName.equals("equals") || methodName.equals("hashCode") || methodName.equals("toString")) {
						return method.invoke(this, args);
					}

					return NA;
				}
			};

			publisher = (L) Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass },
							handler);
			publishers.put(topic, publisher);
		}

		return publisher;
	}

	@SuppressWarnings("rawtypes")
	private MessageBus() {
		connections = new ArrayList<MessageBusConnection>();
		publishers = new HashMap<Topic, Object>();
	}

}