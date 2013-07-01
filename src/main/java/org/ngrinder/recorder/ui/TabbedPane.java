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

import static net.grinder.util.TypeUtil.cast;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.grinder.util.Language;
import net.grinder.util.Pair;

import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.MessageBusConnection;
import org.ngrinder.recorder.event.Topics;
import org.ngrinder.recorder.util.AsyncUtil;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserType;

/**
 * TabbedPane which includes multiple browser instances.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author Vladimir Ikryanov
 * @author JunHo Yoon (modified by)
 * @since 1.0
 */
public class TabbedPane extends JPanel {

	/** UID. */
	private static final long serialVersionUID = 1L;
	private final List<Tab> tabs;
	private final TabItems tabItems;
	private final JComponent contentContainer;
	private final TabFactory tabFactory;

	/**
	 * Constructor.
	 * 
	 * @param tabFactory
	 *            {@link TabFactory}
	 */
	public TabbedPane(TabFactory tabFactory) {
		this.tabFactory = tabFactory;
		this.tabItems = new TabItems();
		this.tabs = new ArrayList<Tab>();
		this.contentContainer = new JPanel(new BorderLayout());
		createUI();
		initializeEvents();
	}

	private void createUI() {
		setLayout(new BorderLayout());
		add(tabItems, BorderLayout.NORTH);
		add(contentContainer, BorderLayout.CENTER);
	}

	/**
	 * Initialize event handlers.
	 */
	private void initializeEvents() {
		MessageBus instance = MessageBus.getInstance();
		MessageBusConnection connection = instance.connect();

		connection.subscribe(Topics.REMOVE_TAB, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				TabItemContent itemContent = (TabItemContent) event.getSource();
				disposeTab(findTab(itemContent));
			}
		});
		connection.subscribe(Topics.ADD_BROWSER_TAB, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				BrowserType browserType = (BrowserType) event.getSource();
				Tab tab = tabFactory.createBrowserTab(browserType);
				addTab(tab);
				selectTab(tab);
			}
		});

		connection.subscribe(Topics.ADD_SOURCE_TAB, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				AsyncUtil.invokeAsync(new Runnable() {
					public void run() {
						Browser browser = (Browser) event.getSource();
						final Tab tab = tabFactory.createPageSourceTab(browser);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								addTab(tab);
								selectTab(tab);
							}
						});
					}
				});
			}
		});

		connection.subscribe(Topics.SHOW_SCRIPT, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				AsyncUtil.invokeAsync(new Runnable() {
					public void run() {
						Pair<Language, String> newValue = cast(event.getNewValue());
						final Tab tab = tabFactory.createScriptTab(newValue.getFirst(), newValue.getSecond());
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								addTab(tab);
								selectTab(tab);
							}
						});
					}
				});
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					MessageBus.getInstance().getPublisher(Topics.WINDOW_MAXIMIZE)
									.propertyChange(new PropertyChangeEvent(this, "Maximize Window", null, null));
				} else {
					super.mouseClicked(e);
				}
			}
		});
	}

	private Tab findTab(TabItemContent itemContent) {
		for (Tab tab : tabs) {
			if (tab.getTabItemContent().equals(itemContent)) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * Dispose all included tabs.
	 */
	public void disposeAllTabs() {
		ArrayList<Tab> copy = new ArrayList<Tab>(tabs);
		for (Tab tab : copy) {
			disposeTab(tab);
		}
		copy.clear();
	}

	/**
	 * Dispose tab. It detects it's the last pane and send the CloseApplication event.
	 * 
	 * @param tab
	 *            tab to be closed.
	 */
	private void disposeTab(Tab tab) {
		tab.getTabItem().setSelected(false);
		tab.getTabItemContent().dispose();
		removeTab(tab);
		if (hasTabs()) {
			getFirstTab().getTabItem().setSelected(true);
		} else {
			MessageBus messageBus = MessageBus.getInstance();
			PropertyChangeListener publisher = messageBus.getPublisher(Topics.APPLICATION_CLOSE);
			publisher.propertyChange(new PropertyChangeEvent(this, "CloseApplication", false, true));
		}
	}

	private void hideTabContent(Tab tab) {
		TabItemContent content = tab.getTabItemContent();
		contentContainer.remove(content);
		contentContainer.validate();
		contentContainer.repaint();
	}

	private void showTabContent(Tab tab) {
		final TabItemContent content = tab.getTabItemContent();
		contentContainer.add(content, BorderLayout.CENTER);
		contentContainer.validate();
		contentContainer.repaint();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				content.requestFocus();
			}
		});
	}

	private Tab findTab(TabItem item) {
		for (Tab tab : tabs) {
			if (tab.getTabItem().equals(item)) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * Add a tab.
	 * 
	 * @param tab
	 *            tab to be added
	 */
	public void addTab(Tab tab) {
		TabItem tabItem = tab.getTabItem();
		tabItem.addPropertyChangeListener("CloseButtonPressed", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				TabItem tabItem = (TabItem) evt.getSource();
				disposeTab(findTab(tabItem));
			}
		});
		tabItem.addPropertyChangeListener("Selected", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				TabItem tabItem = (TabItem) evt.getSource();
				if (tabItem.isSelected()) {
					selectTab(findTab(tabItem));
				}
				Tab selectedTab = findTab(tabItem);
				if (!tabItem.isSelected()) {
					hideTabContent(selectedTab);
				} else {
					showTabContent(selectedTab);
				}
			}
		});
		tabItems.addTab(tabItem);
		tabs.add(tab);
		validate();
		repaint();
	}

	private boolean hasTabs() {
		return !tabs.isEmpty();
	}

	private Tab getFirstTab() {
		return tabs.get(0);
	}

	/**
	 * Remove tab.
	 * 
	 * @param tab
	 *            tab to be removed.
	 */
	public void removeTab(Tab tab) {
		TabItem tabItem = tab.getTabItem();
		tabItems.removeTab(tabItem);
		tabs.remove(tab);
		validate();
		repaint();
	}

	/**
	 * Add the given tab button on the tab items.
	 * 
	 * @param button
	 *            button to be added
	 */
	public void addTabButton(TabButton button) {
		tabItems.addTabButton(button);
	}

	/**
	 * Remove the tab button.
	 * 
	 * @param button
	 *            button to be removed.
	 */
	public void removeTabButton(TabButton button) {
		tabItems.removeTabButton(button);
	}

	/**
	 * Select the given tab.
	 * 
	 * @param tab
	 *            tab to be selected
	 */
	public void selectTab(Tab tab) {
		TabItem tabItem = tab.getTabItem();
		TabItem selectedTab = tabItems.getSelectedTab();
		if (selectedTab != null && !selectedTab.equals(tabItem)) {
			selectedTab.setSelected(false);
		}
		tabItem.setFocusable(true);
		tabItem.requestFocusInWindow();
		tabItems.setSelectedTab(tabItem);
	}

	/**
	 * Get all tabs.
	 * 
	 * @return all included tabs
	 */
	public List<Tab> getTabs() {
		return tabs;
	}

	public TabFactory getTabFactory() {
		return tabFactory;
	}
}
