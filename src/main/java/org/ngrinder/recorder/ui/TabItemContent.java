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
package org.ngrinder.recorder.ui;

import javax.swing.JPanel;

/**
 * TabItemContent common ancestor.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author Vladimir Ikryanov
 * @since 1.0
 */
public abstract class TabItemContent extends JPanel {

	/** UID. */
	private static final long serialVersionUID = 8679532215177436575L;

	/**
	 * Dispose.
	 */
	public abstract void dispose();
}
