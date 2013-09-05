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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.MessageBusConnection;
import org.ngrinder.recorder.event.Topics;
import org.ngrinder.recorder.ui.component.ActionButton;
import org.ngrinder.recorder.util.AsyncUtil;
import org.ngrinder.recorder.util.ResourceUtil;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.events.FailLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.events.StartLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.TitleEvent;
import com.teamdev.jxbrowser.chromium.events.TitleListener;

/**
 * Toobar located in the each {@link TabItem}.
 * 
 * This class is from JxBrowserDemo
 * 
 * @author JunHo Yoon (modified by)
 * @since 1.0
 */
public class ToolBar extends JPanel {
	/** UID. */
	private static final long serialVersionUID = 8873800890425857272L;
	public static final String ALLOW_IMAGES = "allowImages";
	public static final String ALLOW_SCRIPTS = "allowJavaScripts";
	public static final String ALLOW_PLUGINS = "allowPlugins";

	public static final String CLOSE_TAB = "closeTab";
	public static final String ADD_IE_TAB = "addIETab";
	public static final String ADD_MOZILLA_TAB = "addMozillaTab";
	public static final String ADD_SAFARI_TAB = "addSafariTab";
	public static final String ADD_SOURCE_TAB = "addSourceTab";
	public static final String SHOW_CONSOLE = "showConsole";
	public static final String HIDE_CONSOLE = "consoleClosed";

	private static final String RUN_JAVASCRIPT = "Run JavaScript...";
	private static final String CLOSE_JAVASCRIPT = "Close JavaScript console";

	private final Browser browser;
	// private final Map<String, Boolean> browserSettings = new HashMap<String,
	// Boolean>();

	private JButton backwardButton;
	private JButton forwardButton;
	private JButton refreshButton;
	private JButton stopButton;
	private ButtonGroup agentBtnGroup;

	private JMenuItem consoleMenuItem;
	private MessageBus messageBus = null;
	private MessageBusConnection connection = null;

	/**
	 * Constructor.
	 * 
	 * @param browser
	 *            browser which will react with the toolbar action.
	 */
	public ToolBar(Browser browser) {
		this.browser = browser;
		setLayout(new GridBagLayout());
		setMessageBus();
		add(createActionsPane(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(createUrlField(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(4, 0, 4, 5), 0, 0));
		add(createMenuButton(), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));

	}

	private void setMessageBus() {
		messageBus = MessageBus.getInstance();
		connection = messageBus.connect();
	}

	private JPanel createActionsPane() {
		backwardButton = createBackwardButton();
		forwardButton = createForwardButton();
		refreshButton = createRefreshButton();
		stopButton = createStopButton();
		JPanel actionsPanel = new JPanel();
		actionsPanel.add(backwardButton);
		actionsPanel.add(forwardButton);
		actionsPanel.add(refreshButton);
		actionsPanel.add(stopButton);
		actionsPanel.add(createHomeButton());
		return actionsPanel;
	}

	private JTextField createUrlField() {
		final JTextField result = new JTextField("about:blank");
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String text = ((JTextField) e.getSource()).getText();
				AsyncUtil.invokeAsync(new Runnable() {
					public void run() {
						browser.loadURL(processAddress(text));
					}
				});
			}
		});

		result.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int clickNum = e.getClickCount();
				if (clickNum == 2) {
					result.setSelectionEnd(0);
				}
				if (clickNum == 1) {
					result.selectAll();
				}
				result.requestFocusInWindow();
			}
		});
		browser.addTitleListener(new TitleListener() {
			@Override
			public void onTitleChange(TitleEvent arg0) {
				String currentLocation = arg0.getBrowser().getURL();
				result.setText(currentLocation);
			}

		});
		browser.addLoadListener(new LoadListener() {

			@Override
			public void onStartLoadingFrame(StartLoadingEvent event) {
				if (event.isMainFrame()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshButton.setEnabled(false);
							stopButton.setEnabled(true);
						}
					});
				}
			}

			@Override
			public void onFinishLoadingFrame(final FinishLoadingEvent event) {
				if (event.isMainFrame()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshButton.setEnabled(true);
							stopButton.setEnabled(false);
							AsyncUtil.invokeAsync(new Runnable() {
								public void run() {
									final boolean canGoForward = event.getBrowser().canGoForward();
									final boolean canGoBack = event.getBrowser().canGoBack();
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											forwardButton.setEnabled(canGoForward);
											backwardButton.setEnabled(canGoBack);
										}
									});
								}
							});
						}
					});
				}
			}

			@Override
			public void onFailLoadingFrame(FailLoadingEvent arg0) {
			}

			@Override
			public void onDocumentLoadedInMainFrame(LoadEvent arg0) {
			}

			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent arg0) {

			}
		});
		return result;
	}

	private String processAddress(String text) {
		return text;
	}

	private JButton createHomeButton() {
		return createButton("Home", new AbstractAction() {
			/**
			 * UUID
			 */
			private static final long serialVersionUID = -7120334988301742968L;

			public void actionPerformed(ActionEvent e) {
				messageBus.getPublisher(Topics.HOME).propertyChange(
						new PropertyChangeEvent(browser, "home", null, null));
			}
		});
	}

	private JButton createBackwardButton() {
		return createButton("Back", new AbstractAction() {
			/**
			 * UUID
			 */
			private static final long serialVersionUID = -4928817645632155565L;

			public void actionPerformed(ActionEvent e) {
				browser.goBack();
			}
		});
	}

	private JButton createForwardButton() {
		return createButton("Forward", new AbstractAction() {
			/**
			 * UUID
			 */
			private static final long serialVersionUID = -7558182378006915441L;

			public void actionPerformed(ActionEvent e) {
				browser.goForward();
			}
		});
	}

	private JButton createRefreshButton() {
		return createButton("Refresh", new AbstractAction() {
			/**
			 * UUID
			 */
			private static final long serialVersionUID = 1081813486182217372L;

			public void actionPerformed(ActionEvent e) {
				browser.reload();
			}
		});
	}

	private JButton createStopButton() {
		return createButton("Stop", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				browser.stop();
			}
		});
	}

	private JButton createButton(String caption, Action action) {
		ActionButton button = new ActionButton(caption, action);
		String imageName = caption.toLowerCase();
		button.setIcon(ResourceUtil.getIcon(imageName + ".png"));
		button.setRolloverIcon(ResourceUtil.getIcon(imageName + "Sel.png"));
		return button;
	}

	private JComponent createMenuButton() {
		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(createCloseTabMenuItem());
		popupMenu.addSeparator();
		popupMenu.add(createPreferencesMenu());
		popupMenu.add(createViewPageSourceMenuItem());
		consoleMenuItem = createConsoleMenuItem();
		popupMenu.add(consoleMenuItem);
		popupMenu.add(createUserAgentsMenuItem());
		popupMenu.addSeparator();
		popupMenu.add(createAboutMenuItem());

		final ActionButton button = new ActionButton("Preferences", null);
		button.setIcon(ResourceUtil.getIcon("gear.png"));
		button.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
					popupMenu.show(event.getComponent(), 0, button.getHeight());
				} else {
					popupMenu.setVisible(false);
				}
			}
		});
		return button;
	}

	private JMenuItem createConsoleMenuItem() {
		final JMenuItem menuItem = new JMenuItem(RUN_JAVASCRIPT);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (RUN_JAVASCRIPT.equals(menuItem.getText())) {
					menuItem.setText(CLOSE_JAVASCRIPT);
					firePropertyChange(SHOW_CONSOLE, false, true);
				} else {
					setTextForMenuItemAndHideConsole(menuItem);
				}
			}
		});

		connection.subscribe(Topics.CLOSE_CONSOLE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Browser sourceBrowser = (Browser) evt.getSource();
				if (browser.equals(sourceBrowser)) {
					setTextForMenuItemAndHideConsole(menuItem);
				}
			}
		});
		return menuItem;
	}

	private void setTextForMenuItemAndHideConsole(JMenuItem javaScriptMenuItem) {
		javaScriptMenuItem.setText(RUN_JAVASCRIPT);
		firePropertyChange(HIDE_CONSOLE, false, true);
	}

	private JMenuItem createCloseTabMenuItem() {
		JMenuItem menuItem = new JMenuItem("Close Tab");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firePropertyChange(CLOSE_TAB, false, true);
			}
		});
		return menuItem;
	}

	private JMenuItem createAboutMenuItem() {
		JMenuItem menuItem = new JMenuItem("About nGrinder Recorder");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MessageBus.getInstance().getPublisher(Topics.SHOW_ABOUT_DIALOG)
						.propertyChange(new PropertyChangeEvent(this, "", null, null));
			}
		});
		return menuItem;
	}

	private JMenuItem createViewPageSourceMenuItem() {
		JMenuItem menuItem = new JMenuItem("View Page Source");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PropertyChangeListener publisher = MessageBus.getInstance().getPublisher(Topics.ADD_SOURCE_TAB);
				publisher.propertyChange(new PropertyChangeEvent(browser, ADD_SOURCE_TAB, false, true));
			}
		});
		return menuItem;
	}

	private JMenu createPreferencesMenu() {
		JMenu settingsMenu = new JMenu("Preferences");
		settingsMenu.add(createAllowScriptsMenuItem());
		return settingsMenu;
	}

	private JRadioButtonMenuItem createUAMenuItem(JMenu menu, String itemName, final String userAgentString) {
		final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(itemName, false);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AsyncUtil.invokeAsync(new Runnable() {
					public void run() {
						BrowserPreferences.setUserAgent(userAgentString);
						// Recreate is necessary.
					}
				});
			}
		});
		agentBtnGroup.add(menuItem);
		menu.add(menuItem);

		return menuItem;
	}

	private JRadioButtonMenuItem createDefaultUserAgentMenuItem(JMenu menu) {
		JRadioButtonMenuItem defUserAgent = createUAMenuItem(menu, "Default(Auto)", null);
		defUserAgent.setSelected(true);
		return defUserAgent;
	}

	private JMenu createUserAgentsMenuItem() {
		if (agentBtnGroup == null) {
			agentBtnGroup = new ButtonGroup();
		}

		JMenu menu = new JMenu("User Agents");
		createDefaultUserAgentMenuItem(menu);
		menu.addSeparator();
		createUAMenuItem(menu, "Firefox 16.0.1",
				"Mozilla/6.0 (Windows NT 6.2; WOW64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1");
		createUAMenuItem(menu, "Firefox 15.0a2", "Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15.0a2");
		menu.addSeparator();
		createUAMenuItem(menu, "IE 10", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)");
		createUAMenuItem(menu, "IE 9", "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US)");
		createUAMenuItem(menu, "IE 8", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)");
		menu.addSeparator();
		createUAMenuItem(menu, "Safari for iPhone", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 1_2_3 like Mac OS X; en-us)"
				+ " AppleWebKit/533.17.9 (KHTML, like Gecko) " + "Version/5.0.2 Mobile/8J2 Safari/6533.18.5");
		createUAMenuItem(menu, "Safari for iPad", "Mozilla/5.0 (iPad; U; CPU OS 4_3 like Mac OS X; en-us)"
				+ " AppleWebKit/533.17.9 (KHTML, like Gecko) " + "Version/5.0.2 Mobile/8F191 Safari/6533.18.5");
		createUAMenuItem(menu, "Safari for Macintosh",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/537.13+ (KHTML, like Gecko) "
						+ "Version/5.1.7 Safari/534.57.2");
		createUAMenuItem(menu, "Safari for Windows",
				"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) "
						+ "Version/5.0.4 Safari/533.20.27");

		return menu;
	}

	private JCheckBoxMenuItem createAllowScriptsMenuItem() {
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Allow JavaScript", false);//
		// browserSettings.get(ALLOW_SCRIPTS));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final boolean selected = menuItem.isSelected();
				consoleMenuItem.setEnabled(selected);
				if (!selected) {
					setTextForMenuItemAndHideConsole(consoleMenuItem);
				}

				AsyncUtil.invokeAsync(new Runnable() {
					public void run() {
						// Configurable configurable =
						// browser.getConfigurable();
						// HashMap<Feature, Boolean> featuresMap = new
						// HashMap<Feature, Boolean>();
						// featuresMap.put(Feature.JAVASCRIPT, selected);
						// configurable.setFeatures(featuresMap);
						// browser.refresh();
					}
				});
			}
		});
		return menuItem;
	}

	private Map<String, Boolean> initBrowserSettings() {
		return null;
	}

}
