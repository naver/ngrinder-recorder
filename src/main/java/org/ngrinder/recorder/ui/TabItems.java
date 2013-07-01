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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.Topics;
import org.ngrinder.recorder.util.ResourceUtil;

/**
 * TabItem holder.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author JunHo Yoon (modified by)
 */
public class TabItems extends JPanel {

	/** UID. */
	private static final long serialVersionUID = 6303245940134695551L;

	private TabItem selectedTab;
	private static final Icon CLOSE_ICON = ResourceUtil.getIcon("close.png");
	private static final Icon CLOSE_ICON_OVER = ResourceUtil.getIcon("close-over.png");
	private static final Icon EXPAND_ICON = ResourceUtil.getIcon("expand.png");
	private static final Icon EXPAND_ICON_OVER = ResourceUtil.getIcon("expand-over.png");
	private static final Icon ABOUT_ICON = ResourceUtil.getIcon("about.png");
	private static final Icon ABOUT_ICON_OVER = ResourceUtil.getIcon("about-over.png");
	private static final Icon MINIMIZE_ICON = ResourceUtil.getIcon("minimize.png");
	private static final Icon MINIMIZE_ICON_OVER = ResourceUtil.getIcon("minimize-over.png");
	private JPanel tabItemsPane;
	private JPanel tabButtonsPane;
	private JPanel generalPane;
	private JPanel windowsActionPane;

	/**
	 * Constructor.
	 */
	public TabItems() {
		createUI();
	}

	private void createUI() {

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		this.generalPane = new JPanel();
		addGrid(gbl, gbc, this.generalPane, new Rectangle(0, 0, 1, 1), 1, 0);
		this.windowsActionPane = new JPanel();
		addGrid(gbl, gbc, this.windowsActionPane, new Rectangle(1, 0, 1, 1), 0, 0);
		generalPane.setLayout(new BoxLayout(generalPane, BoxLayout.X_AXIS));
		this.setBackground(Color.DARK_GRAY);
		this.generalPane.setBackground(Color.DARK_GRAY);
		this.generalPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.generalPane.add(createItemsPane());
		this.generalPane.add(createButtonsPane());
		this.generalPane.add(Box.createHorizontalGlue());

		this.windowsActionPane.setBackground(Color.DARK_GRAY);
		this.windowsActionPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.windowsActionPane.add(createMarginPanel(50));
		this.windowsActionPane.add(createTabButton(ABOUT_ICON, ABOUT_ICON_OVER, "About", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MessageBus.getInstance().getPublisher(Topics.SHOW_ABOUT_DIALOG)
								.propertyChange(new PropertyChangeEvent(this, "Show About", null, null));
			}
		}));

		this.windowsActionPane.add(createTabButton(MINIMIZE_ICON, MINIMIZE_ICON_OVER, "Minimize", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MessageBus.getInstance().getPublisher(Topics.WINDOW_MINIMIZE)
								.propertyChange(new PropertyChangeEvent(this, "Minimize Window", null, null));
			}
		}));

		this.windowsActionPane.add(createTabButton(EXPAND_ICON, EXPAND_ICON_OVER, "Expand", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MessageBus.getInstance().getPublisher(Topics.WINDOW_MAXIMIZE)
								.propertyChange(new PropertyChangeEvent(this, "Maximize Window", null, null));
			}
		}));

		this.windowsActionPane.add(createTabButton(CLOSE_ICON, CLOSE_ICON_OVER, "Close", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MessageBus.getInstance().getPublisher(Topics.APPLICATION_CLOSE)
								.propertyChange(new PropertyChangeEvent(this, "Application Close", null, null));
			}
		}));

	}

	private Component createTabButton(final Icon icon, final Icon overIcon, String title, ActionListener listener) {
		final TabButton expandButton = new TabButton(icon, title) {
			private static final long serialVersionUID = 1L;
		};
		expandButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		expandButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				expandButton.setIcon(overIcon);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				expandButton.setIcon(icon);
			}
		});
		expandButton.addActionListener(listener);
		return expandButton;
	}

	private JPanel createMarginPanel(int width) {
		JPanel marginPanel = new JPanel();
		marginPanel.setMinimumSize(new Dimension(width, 1));
		marginPanel.setOpaque(false);
		return marginPanel;
	}

	private void addGrid(GridBagLayout gbl, GridBagConstraints gbc, Component c, Rectangle rect, double weightx,
					double weighty) {
		gbc.gridx = rect.x;
		gbc.gridy = rect.y;
		gbc.gridwidth = rect.width;
		gbc.gridheight = rect.height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbl.setConstraints(c, gbc);
		add(c);
	}

	private JComponent createItemsPane() {
		tabItemsPane = new JPanel();
		tabItemsPane.setOpaque(false);
		tabItemsPane.setLayout(new BoxLayout(tabItemsPane, BoxLayout.X_AXIS));
		return tabItemsPane;
	}

	private JComponent createButtonsPane() {
		tabButtonsPane = new JPanel();
		tabButtonsPane.setOpaque(false);
		tabButtonsPane.setLayout(new BoxLayout(tabButtonsPane, BoxLayout.X_AXIS));
		return tabButtonsPane;
	}

	/**
	 * Add the given {@link TabItem}.
	 * 
	 * @param item
	 *            item to be added
	 */
	public void addTab(TabItem item) {
		tabItemsPane.add(item);
	}

	/**
	 * Remove the given {@link TabItem}.
	 * 
	 * @param item
	 *            item to be removed.
	 */
	public void removeTab(TabItem item) {
		tabItemsPane.remove(item);
	}

	/**
	 * Add the given tab button.
	 * 
	 * @param button
	 *            button to be added
	 */
	public void addTabButton(TabButton button) {
		tabButtonsPane.add(button);
	}

	/**
	 * Remove the given tab button.
	 * 
	 * @param button
	 *            btton to be removed.
	 */
	public void removeTabButton(TabButton button) {
		tabButtonsPane.remove(button);
	}

	/**
	 * Get the selected tab.
	 * 
	 * @return currently selected {@link TabItem}
	 */
	public TabItem getSelectedTab() {
		return selectedTab;
	}

	/**
	 * Select the given tab.
	 * 
	 * @param selectedTab
	 *            {@link TabItem} to be selected.
	 */
	public void setSelectedTab(TabItem selectedTab) {
		this.selectedTab = selectedTab;
		this.selectedTab.setSelected(true);
	}
}
