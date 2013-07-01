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

/**
 * nGrinder recorder topic class.
 * 
 * @param <L>
 *            the event type
 * @author AlexQin
 * @version 1.0
 */
public class Topic<L> {

	private final Class<L> listenerClass;

	/**
	 * Constructor.
	 * 
	 * @param listenerClass
	 *            listener class
	 */
	Topic(Class<L> listenerClass) {
		if (listenerClass == null) {
			throw new IllegalArgumentException("ListenerClass cannot be null.");
		}
		this.listenerClass = listenerClass;
	}

	/**
	 * Create topic.
	 * 
	 * @param listenerClass
	 *            listener class
	 * @param <L>
	 *            event listener type
	 * @return new topic object
	 */
	static <L> Topic<L> create(Class<L> listenerClass) {
		return new Topic<L>(listenerClass);
	}

	/**
	 * Get listener class.
	 * 
	 * @return listener class
	 */
	public Class<L> getListenerClass() {
		return listenerClass;
	}
}
