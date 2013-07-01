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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Various Swing Component Utils.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public abstract class SwingUtils {
	/**
	 * Wrap the given component with {@link JScrollPane}.
	 * 
	 * @param component
	 *            component
	 * @return {@link JScrollPane} containing the given component.
	 */
	public static JScrollPane wrapScroll(Component component) {
		return new JScrollPane(component);
	}

	/**
	 * Get the full screen size recognizing multiple monitor.
	 * 
	 * @return full screen size
	 */
	public static Dimension getFullScreenSize() {
		Rectangle2D result = new Rectangle2D.Double();
		GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice gd : localGE.getScreenDevices()) {
			for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
				Rectangle2D.union(result, graphicsConfiguration.getBounds(), result);
			}
		}
		return new Dimension((int) result.getWidth(), (int) result.getHeight());
	}

	/**
	 * Create menu item.
	 * 
	 * @param caption
	 *            caption
	 * @param action
	 *            action
	 * @return {@link JMenuItem}
	 */
	public static JMenuItem createMenuItem(String caption, Action action) {
		JMenuItem menuItem = new JMenuItem(caption);
		menuItem.addActionListener(action);
		return menuItem;
	}

	/**
	 * Create simple {@link DropDownButton} button.
	 * 
	 * @param caption
	 *            caption on the button
	 * @param action
	 *            action
	 * @return {@link DropDownButton}
	 */
	public static DropDownButton createSimpleDropDownButton(String caption, Action action) {
		DropDownButton button = decoratedToSimpleButton(new DropDownButton(caption));
		button.addActionListener(action);
		return button;
	}

	/**
	 * Create simple {@link JButton}.
	 * 
	 * @param caption
	 *            caption on the button
	 * @param action
	 *            action
	 * @return {@link JButton}
	 */
	public static JButton createSimpleTextButton(String caption, Action action) {
		JButton button = decoratedToSimpleButton(new JButton(caption));
		button.addActionListener(action);
		return button;
	}

	/**
	 * Set the button to have simplified UI.
	 * 
	 * @param button
	 *            button to be modified
	 * @param <T>
	 *            type of button
	 * @return button
	 */
	public static <T extends AbstractButton> T decoratedToSimpleButton(final T button) {

		button.setForeground(Color.BLACK);
		button.setBackground(Color.LIGHT_GRAY);
		button.setBorderPainted(true);
		button.setFocusPainted(true);
		button.setContentAreaFilled(false);
		button.setOpaque(true);

		if (button instanceof JToggleButton) {
			((JToggleButton) button).addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (button.isSelected()) {
						button.setBackground(Color.WHITE);
					}
				}
			});
		}
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				button.setBackground(Color.WHITE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				if (!button.isSelected()) {
					button.setBackground(Color.LIGHT_GRAY);
				}
			}
		});

		button.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!button.isSelected()) {
					button.setBackground(Color.LIGHT_GRAY);
				}
			}
		});
		Border line = new LineBorder(Color.BLACK);
		Border margin = new EmptyBorder(5, 15, 5, 15);
		Border compound = new CompoundBorder(line, margin);
		button.setBorder(compound);
		return button;
	}

	/**
	 * Attach popup menu on the given component.
	 * 
	 * @param component
	 *            component to which the popupMenu is attached
	 * @param popupMenu
	 *            popupMenu to be attached
	 */
	public static void attachPopupMenu(final JComponent component, final JPopupMenu popupMenu) {
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});
	}
}
