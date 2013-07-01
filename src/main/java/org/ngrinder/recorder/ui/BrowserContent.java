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

/*
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.Topics;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.events.StatusChangedEvent;
import com.teamdev.jxbrowser.events.StatusListener;
import com.teamdev.jxbrowser.events.TitleChangedEvent;
import com.teamdev.jxbrowser.events.TitleListener;

/**
 * Browser Content which includes a {@link Browser} messageBus.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class BrowserContent extends TabItemContent {

	/** UUID. */
	private static final long serialVersionUID = -7638205410270930491L;
	private final Browser browser;
	private final JComponent container;
	private final JComponent browserContainer;
	private final JComponent javaScriptConsole;
	private MessageBus messageBus = MessageBus.getInstance();

	/**
	 * Constructor.
	 * 
	 * @param browser
	 *            browser messageBus.
	 */
	public BrowserContent(final Browser browser) {
		this.browser = browser;
		this.browser.addTitleListener(new TitleListener() {
			public void titleChanged(final TitleChangedEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						firePropertyChange("PageTitleChanged", null, event.getTitle());
					}
				});

			}
		});
		this.browser.getComponent().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				PropertyChangeListener publisher = messageBus.getPublisher(Topics.START_USER_PAGE);
				publisher.propertyChange(new PropertyChangeEvent(BrowserContent.this, "Start User Page", false, false));

			}
		});

		browserContainer = createBrowserContainer();
		javaScriptConsole = createJavaScriptConsole();

		container = new JPanel(new BorderLayout());
		container.add(browserContainer, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(container, BorderLayout.CENTER);
		add(createToolBar(browser), BorderLayout.NORTH);
	}

	/**
	 * Set HTML content with the given title.
	 * 
	 * @param url
	 *            content the browser will show.
	 * @param title
	 *            browser title
	 */
	public void setHtml(String url, String title) {
		this.browser.navigate(url);
	}

	/**
	 * Create the tool bar.
	 * 
	 * @param browser
	 *            browser
	 * @return generated toolbar.
	 */
	private JComponent createToolBar(Browser browser) {
		ToolBar toolBar = new ToolBar(browser);
		toolBar.addPropertyChangeListener(ToolBar.CLOSE_TAB, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				PropertyChangeListener publisher = MessageBus.getInstance().getPublisher(Topics.REMOVE_TAB);
				publisher.propertyChange(new PropertyChangeEvent(BrowserContent.this, ToolBar.CLOSE_TAB, true, false));
			}
		});
		toolBar.addPropertyChangeListener(ToolBar.SHOW_CONSOLE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				showConsole();
			}
		});
		toolBar.addPropertyChangeListener(ToolBar.HIDE_CONSOLE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				hideConsole();
			}
		});
		return toolBar;
	}

	private void hideConsole() {
		showComponent(browserContainer);
	}

	private void showComponent(JComponent component) {
		container.removeAll();
		container.add(component, BorderLayout.CENTER);
		validate();
	}

	private void showConsole() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(browserContainer, JSplitPane.TOP);
		splitPane.add(javaScriptConsole, JSplitPane.BOTTOM);
		splitPane.setResizeWeight(0.8);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		showComponent(splitPane);
	}

	private JComponent createJavaScriptConsole() {
		JavaScriptConsole result = new JavaScriptConsole(browser);
		result.addPropertyChangeListener(JavaScriptConsole.CONSOLE_CLOSED, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				MessageBus messageBus = MessageBus.getInstance();
				PropertyChangeListener publisher = messageBus.getPublisher(Topics.CLOSE_CONSOLE);
				publisher.propertyChange(new PropertyChangeEvent(browser, "ConsoleClosed", true, false));
			}
		});
		return result;
	}

	private JComponent createBrowserContainer() {
		JPanel browserContainer = new JPanel(new BorderLayout());
		browserContainer.add(browser.getComponent(), BorderLayout.CENTER);
		browserContainer.add(createStatusBar(), BorderLayout.SOUTH);
		return browserContainer;
	}

	private JComponent createStatusBar() {
		final JLabel result = new JLabel();
		result.setPreferredSize(new Dimension(100, 16));
		result.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		result.setHorizontalAlignment(SwingConstants.LEFT);
		StatusListener paramStatusListener = new StatusListener() {
			public void statusChanged(StatusChangedEvent event) {
				final String statusText = processStatusText(event.getStatusText());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						result.setText(statusText);
					}
				});
			}

			private String processStatusText(String statusText) {
				return statusText.length() == 0 ? "Done" : statusText;
			}
		};
		browser.addStatusListener(paramStatusListener);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.recorder.ui.TabItemContent#dispose()
	 */
	@Override
	public void dispose() {
		// It's very important to remove all listener.
		// Because if it's not removed, the JVM crash will be occurred.
		for (StatusListener each : browser.getStatusListeners()) {
			browser.removeStatusListener(each);
		}
		for (TitleListener each : browser.getTitleListeners()) {
			browser.removeTitleListener(each);
		}
		browser.dispose();
	}

	/**
	 * Move to url in the browser.
	 * 
	 * @param url
	 *            page url
	 */
	public void moveTo(URL url) {
		if (url == null) {
			return;
		}
		this.browser.navigate(url.toString());
	}
}
