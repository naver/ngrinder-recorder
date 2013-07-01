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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * DropDown Button.
 * 
 * This button supports the sub menu binding.
 * 
 * @author JunHo Yoon (modified by)
 * @since 1.0
 * @see https
 *      ://code.google.com/p/link-collector/source/browse/ver2/trunk/common/util/src/ru/kc/util/
 *      swing/button/DropDownButton.java?spec=svn414&r=414
 */
public class DropDownButton extends JToggleButton {

	private static final long serialVersionUID = 2857744715416733620L;

	private JPopupMenu menu;

	/**
	 * Constructor.
	 * 
	 * @param text
	 *            text
	 */
	public DropDownButton(String text) {
		super(text);
		init();
	}

	private void init() {
		addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (menu == null) {
					return;
				}
				boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;
				if (isSelected) {
					menu.show(DropDownButton.this, 0, getHeight());
				}
			}
		});
	}

	public JPopupMenu getMenu() {
		return menu;
	}

	/**
	 * Attach the given menu on this button.
	 * 
	 * @param menu
	 *            menu to be attached.
	 */
	public void setMenu(JPopupMenu menu) {
		this.menu = menu;
		initMenu();
	}

	private void initMenu() {
		menu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				deselectButtonRequest();
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
				deselectButtonRequest();
			}
		});
	}

	private void deselectButtonRequest() {
		setSelected(false);
	}
}
