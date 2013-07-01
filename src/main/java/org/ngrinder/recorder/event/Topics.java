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

import java.beans.PropertyChangeListener;

/**
 * nGrinder recorder topics class.
 * 
 * @author AlexQin
 * @author JunHo Yoon
 * @version 1.0
 */
public abstract class Topics {
	/**
	 * Start Recording Event.
	 */
	public static final Topic<PropertyChangeListener> START_RECORDING = Topic.create(PropertyChangeListener.class);
	/**
	 * Stop Recording Event.
	 */
	public static final Topic<PropertyChangeListener> STOP_RECORDING = Topic.create(PropertyChangeListener.class);

	/**
	 * Tab Removal Event.
	 */
	public static final Topic<PropertyChangeListener> REMOVE_TAB = Topic.create(PropertyChangeListener.class);
	/**
	 * Browser Tab Add Event.
	 */
	public static final Topic<PropertyChangeListener> ADD_BROWSER_TAB = Topic.create(PropertyChangeListener.class);
	/**
	 * Source Tab Add Event.
	 */
	public static final Topic<PropertyChangeListener> ADD_SOURCE_TAB = Topic.create(PropertyChangeListener.class);
	/**
	 * Console Close Event.
	 */
	public static final Topic<PropertyChangeListener> CLOSE_CONSOLE = Topic.create(PropertyChangeListener.class);
	/**
	 * Application Close Event.
	 */
	public static final Topic<PropertyChangeListener> APPLICATION_CLOSE = Topic.create(PropertyChangeListener.class);
	/**
	 * Window Maximization Event.
	 */
	public static final Topic<PropertyChangeListener> WINDOW_MAXIMIZE = Topic.create(PropertyChangeListener.class);
	/**
	 * Window Minimization Event.
	 */
	public static final Topic<PropertyChangeListener> WINDOW_MINIMIZE = Topic.create(PropertyChangeListener.class);
	/**
	 * Recorded Script Show Event.
	 */
	public static final Topic<PropertyChangeListener> SHOW_SCRIPT = Topic.create(PropertyChangeListener.class);
	/**
	 * RESET Event.
	 */
	public static final Topic<PropertyChangeListener> RESET = Topic.create(PropertyChangeListener.class);
	/**
	 * Start New Page Event.
	 */
	public static final Topic<PropertyChangeListener> START_USER_PAGE = Topic.create(PropertyChangeListener.class);
	/**
	 * PreClose Event.
	 */
	public static final Topic<PropertyChangeListener> PREPARE_TO_CLOSE = Topic.create(PropertyChangeListener.class);
	/**
	 * PreView Event.
	 */
	public static final Topic<PropertyChangeListener> PREPARE_TO_VIEW = Topic.create(PropertyChangeListener.class);
	/**
	 * About Dialog Box Show Event.
	 */
	public static final Topic<PropertyChangeListener> SHOW_ABOUT_DIALOG = Topic.create(PropertyChangeListener.class);
	/**
	 * Home Event.
	 */
	public static final Topic<PropertyChangeListener> HOME = Topic.create(PropertyChangeListener.class);

}
