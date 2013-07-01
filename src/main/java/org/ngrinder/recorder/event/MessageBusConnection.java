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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * nGrinder recorder message bus connection.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author AlexQin
 * @version 1.0
 */
public class MessageBusConnection {

	private final MessageBus messageBus;
	@SuppressWarnings("rawtypes")
	private final Map<Topic, List<Object>> subscribers;

	/**
	 * Constructor.
	 * 
	 * @param messageBus
	 *            Message bus instance.
	 */
	@SuppressWarnings("rawtypes")
	public MessageBusConnection(MessageBus messageBus) {
		this.messageBus = messageBus;
		this.subscribers = new HashMap<Topic, List<Object>>();
	}

	/**
	 * Subscribe topic.
	 * 
	 * @param topic
	 *            Topic object
	 * @param <L>
	 *            event listener type
	 * @param handler
	 *            Handler
	 */
	public <L> void subscribe(Topic<L> topic, L handler) {
		getTopicSubscribers(topic).add(handler);
	}

	/**
	 * Disconnect message bus.
	 */
	public void disconnect() {
		subscribers.clear();
		messageBus.disconnect(this);
	}

	/**
	 * Notify subscribers.
	 * 
	 * @param topic
	 *            Topic object
	 * @param <L>
	 *            event listener type
	 * @param method
	 *            Method
	 * @param args
	 *            Arguments
	 * @throws InvocationTargetException
	 *             Invocation target exception
	 * @throws IllegalAccessException
	 *             Illegal access exception
	 */
	public <L> void notifySubscribers(Topic<L> topic, Method method, Object[] args)
					throws InvocationTargetException,
					IllegalAccessException {
		for (Object handler : getTopicSubscribers(topic)) {
			method.invoke(handler, args);
		}
	}

	private <L> List<Object> getTopicSubscribers(Topic<L> topic) {
		if (subscribers.get(topic) == null) {
			subscribers.put(topic, new ArrayList<Object>());
		}
		return subscribers.get(topic);
	}
}
