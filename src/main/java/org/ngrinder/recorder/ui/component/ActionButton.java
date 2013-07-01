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
package org.ngrinder.recorder.ui.component;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Extended Button taking the action as a constructor parameter.
 * 
 * This class is from JXBrowserDemo
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ActionButton extends JButton {
	private static final long serialVersionUID = 5927012652060259296L;

	/**
	 * Constructor.
	 * 
	 * @param tooltipText
	 *            tooltip text
	 * @param action
	 *            action
	 */
	public ActionButton(String tooltipText, Action action) {
		super(action);
		setContentAreaFilled(false);
		setBorder(BorderFactory.createEmptyBorder());
		setBorderPainted(false);
		setRolloverEnabled(true);
		setToolTipText(tooltipText);
		setText(null);
		setFocusable(false);
		setDefaultCapable(false);
	}
}
