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
package org.ngrinder.recorder;

import static net.grinder.util.NoOp.noOp;
import static net.grinder.util.TypeUtil.cast;
import static net.grinder.util.UrlUtil.toURL;
import static org.ngrinder.recorder.ui.component.SwingUtils.getFullScreenSize;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.grinder.tools.tcpproxy.EndPoint;
import net.grinder.util.NoOp;
import net.grinder.util.Pair;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.ngrinder.recorder.browser.BrowserFactoryEx;
import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.MessageBusConnection;
import org.ngrinder.recorder.event.Topics;
import org.ngrinder.recorder.infra.NGrinderRuntimeException;
import org.ngrinder.recorder.infra.RecorderConfig;
import org.ngrinder.recorder.proxy.ProxyEndPointPair;
import org.ngrinder.recorder.proxy.ScriptRecorderProxy;
import org.ngrinder.recorder.ui.AboutDialog;
import org.ngrinder.recorder.ui.BrowserContent;
import org.ngrinder.recorder.ui.NewBrowserButton;
import org.ngrinder.recorder.ui.RecordingControlPanel;
import org.ngrinder.recorder.ui.Tab;
import org.ngrinder.recorder.ui.TabButton;
import org.ngrinder.recorder.ui.TabButtonFactory;
import org.ngrinder.recorder.ui.TabFactory;
import org.ngrinder.recorder.ui.TabbedPane;
import org.ngrinder.recorder.ui.component.ComponentMover;
import org.ngrinder.recorder.ui.component.ComponentResizer;
import org.ngrinder.recorder.util.AsyncUtil;
import org.ngrinder.recorder.util.ResourceUtil;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.PopupContainer;
import com.teamdev.jxbrowser.chromium.PopupHandler;
import com.teamdev.jxbrowser.chromium.PopupParams;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Recorder main class.
 * 
 * @author JunHo Yoon
 * @version 1.0
 */
public class Recorder {
	private static final Logger LOGGER = LoggerFactory.getLogger(Recorder.class);

	private static final int NGRINDER_DEFAULT_PORT = 10288;
	private final JFrame frame = new JFrame();
	private ScriptRecorderProxy proxy;
	private final Gson gson = new Gson();

	/**
	 * Constructor.
	 */
	public Recorder() {
	}

	private RecorderConfig recorderConfig;

	/**
	 * Start nGrinder Recorder Instance.
	 */
	public void start() {
		initRecordConfig();
		proxy = new ScriptRecorderProxy(this.recorderConfig);
		ProxyEndPointPair endPoints = initProxy();
		TabbedPane tabbedPane = createTabbedPane();
		initFrame(tabbedPane);
		initTabbedPane(tabbedPane, endPoints);
		initialProxyCheck(endPoints);
		initShutdownHook();
	}

	/**
	 * Initialize tabbed pane.
	 * 
	 * @param tabbedPane
	 *            base tabbed pane
	 * @param proxyEndPoint
	 *            proxy endpoints which will be used to show the message how
	 *            mobile devices connect this proxy
	 */
	protected void initTabbedPane(TabbedPane tabbedPane, ProxyEndPointPair proxyEndPoint) {
		LOGGER.info("Tabbed Pane initalization is initialzed");
		initializeBrowserTab(tabbedPane, proxyEndPoint);
		initMessageHandler(tabbedPane);
		LOGGER.info("Tabbed Pane is initialzed");
	}

	/**
	 * Initialize shutdown hook. The created proxy should be stopped for the
	 * socket port reusable.
	 */
	protected void initShutdownHook() {
		Runnable shutdownHook = new Runnable() {
			public synchronized void run() {
				proxy.stopProxy();
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
	}

	/**
	 * Initialized record configuration.
	 */
	protected void initRecordConfig() {
		recorderConfig = new RecorderConfig();
		recorderConfig.init();
		BrowserFactoryEx.setRecorderConfig(recorderConfig);
	}

	/**
	 * Initialize proxy and get the created proxy. In addition, make it as a
	 * proxy for future browser invoke. If the proxy creation is failed, it
	 * exits the application.
	 * 
	 * @return generated proxy port
	 */
	protected ProxyEndPointPair initProxy() {
		ProxyEndPointPair endPoints = null;
		try {
			int port = recorderConfig.getPropertyInt("proxy.port", NGRINDER_DEFAULT_PORT);
			endPoints = proxy.startProxy(port);
			BrowserFactoryEx.setProxyEndPoints(endPoints);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Proxy initalization is failed in this system.\n\n" + e.getMessage());
			LOGGER.error("Failed to start proxy", e);
			System.exit(-1);
		}
		return endPoints;
	}

	/**
	 * Check the proxy to see it's generated with the default proxy port.
	 * 
	 * @param endPoints
	 *            endPoints
	 */
	protected void initialProxyCheck(ProxyEndPointPair endPoints) {
		int port = recorderConfig.getPropertyInt("proxy.port", NGRINDER_DEFAULT_PORT);
		if (endPoints.getHttpEndPoint().getPort() != port) {
			String msg = String.format("Already poxy port %d is used by the other recorder or processes.\n"
					+ "Recorder will use the %d port instead.", port, endPoints.getHttpEndPoint().getPort());
			JOptionPane.showMessageDialog(frame, msg);
		}
	}

	/**
	 * Create and initialized tabbed pane.
	 * 
	 * @return {@link TabbedPane}
	 */
	protected TabbedPane createTabbedPane() {
		LOGGER.info("Creating Tabbed Pane");
		TabFactory tabFactory = new TabFactory();
		final TabbedPane tabbedPane = new TabbedPane(tabFactory);
		insertSupportedBrowserTabs(tabFactory, tabbedPane);

		insertNewBrowserButton(tabFactory, tabbedPane);
		setupNewWindowsManager(tabFactory, tabbedPane);
		LOGGER.info("Tabbed Pane is created");
		return tabbedPane;
	}

	/**
	 * Initialize the frame with the given {@link TabbedPane}. It tries to get
	 * the last position and location saved in the
	 * ${NGRINDER_RECORDER_HOME}/last_frame file.
	 * 
	 * @param tabbedPane
	 *            tabbedPane
	 */
	protected void initFrame(final TabbedPane tabbedPane) {
		File frameInfoFile = recorderConfig.getHome().getFile("last_frame");
		Dimension size = new Dimension(800, 600);
		Point location = null;
		if (frameInfoFile.exists()) {
			try {
				String readFileToString = FileUtils.readFileToString(frameInfoFile);
				Gson gson = new Gson();
				TypeToken<Pair<Dimension, Point>> typeToken = new TypeToken<Pair<Dimension, Point>>() {
				};
				Pair<Dimension, Point> frameInfo = gson.fromJson(readFileToString, typeToken.getType());
				size = frameInfo.getFirst();
				location = frameInfo.getSecond();

			} catch (Exception e) {
				LOGGER.error("Failed to load the frame info", e);
			}
		}
		Dimension screenSize = getFullScreenSize();

		if (screenSize.height < size.height || screenSize.width < size.width) {
			size = screenSize;
		}
		boolean frameUse = recorderConfig.getPropertyBoolean("use.frame", false);
		RecordingControlPanel recordingControlPanel = new RecordingControlPanel(proxy.getConnectionFilter());
		frame.getContentPane().add(createSplitPane(tabbedPane, recordingControlPanel));
		if (!frameUse) {
			frame.setUndecorated(true);
			new ComponentResizer(frame).setMinimumSize(new Dimension(800, 600));
			new ComponentMover(frame, tabbedPane);
		}
		frame.setTitle("nGrinder Recorder");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setLocationByPlatform(true);
		frame.getRootPane().setMinimumSize(new Dimension(800, 600));
		frame.setLocationRelativeTo(null);
		frame.setIconImage(ResourceUtil.getIcon("recorder16x16.png").getImage());
		frame.setFocusable(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				tabbedPane.disposeAllTabs();
			}
		});

		if (location != null) {
			frame.setLocation(location);
		}

		frame.setSize(size);
		frame.setVisible(true);
		frame.requestFocusInWindow();
		MessageBus
				.getInstance()
				.getPublisher(Topics.PREPARE_TO_VIEW)
				.propertyChange(
						new PropertyChangeEvent(this, "Prepare to View", null, recorderConfig.getHome().getDirectory()));
		initDisplayDetectionScheduledTask();
	}

	protected void initDisplayDetectionScheduledTask() {
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			private Dimension prevSize;

			@Override
			public void run() {
				if (prevSize == null) {
					prevSize = getFullScreenSize();
				} else {
					Dimension currentSize = getFullScreenSize();
					if (prevSize.equals(currentSize)) {
						return;
					}
					if (prevSize.height > currentSize.height || prevSize.width > currentSize.width) {
						frame.setSize(new Dimension(600, 500));
						Point currentLocation = frame.getLocation();
						if (currentSize.width < currentLocation.x || currentSize.height < currentLocation.y) {
							frame.setLocation(200, 200);
						}
					}
					prevSize = currentSize;

				}
			}
		}, 2000, 2000);
	}

	/**
	 * Initialize global message handlers.
	 * 
	 * @param tabbedPane
	 *            tabbedPane
	 */
	protected void initMessageHandler(final TabbedPane tabbedPane) {
		final File home = recorderConfig.getHome().getDirectory();
		final MessageBus messageBusInstance = MessageBus.getInstance();
		MessageBusConnection connect = messageBusInstance.connect();
		connect.subscribe(Topics.HOME, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Browser browser = cast(evt.getSource());
				browser.loadURL(toURL(tempFile).toString());
			}
		});
		connect.subscribe(Topics.APPLICATION_CLOSE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				proxy.stopProxy();
				frame.setExtendedState(Frame.NORMAL);
				File frameInfoFile = recorderConfig.getHome().getFile("last_frame");
				try {
					Pair<Dimension, Point> pair = Pair.of(frame.getSize(), frame.getLocation());
					String frameInfo = gson.toJson(pair);
					FileUtils.writeStringToFile(frameInfoFile, frameInfo);
				} catch (Exception e) {
					LOGGER.error("Failed to save the frame info", e);
				}

				messageBusInstance.getPublisher(Topics.PREPARE_TO_CLOSE).propertyChange(
						new PropertyChangeEvent(this, "PREPARE_TO_CLOSE", null, home));

				tabbedPane.disposeAllTabs();
				frame.setVisible(false);
				frame.dispose();

				System.exit(0);
			}
		});
		connect.subscribe(Topics.WINDOW_MAXIMIZE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
					frame.setExtendedState(Frame.NORMAL);
				} else {
					frame.setExtendedState(Frame.MAXIMIZED_BOTH);
				}
			}
		});

		connect.subscribe(Topics.WINDOW_MINIMIZE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (frame.getExtendedState() == Frame.ICONIFIED) {
					frame.setExtendedState(Frame.NORMAL);
				} else {
					frame.setExtendedState(Frame.ICONIFIED);
				}
			}
		});

		connect.subscribe(Topics.SHOW_ABOUT_DIALOG, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				AboutDialog dialog = AboutDialog.getInstance(frame, recorderConfig);
				dialog.setVisible(true);
			}
		});
	}

	private StringTemplateLoader loader = new StringTemplateLoader();

	/**
	 * Prepare the initial browser content template.
	 * 
	 * @param resourceName
	 *            resource name to be loaded.
	 * @param lang
	 *            language name
	 * @return loaded template
	 */
	protected Template prepareTemplate(String resourceName, String lang) {
		String fullResourceName = "/html/" + resourceName + "_" + lang + ".html";
		InputStream htmlResourceStream = Recorder.class.getResourceAsStream(fullResourceName);
		if (htmlResourceStream == null) {
			htmlResourceStream = Recorder.class.getResourceAsStream("/html/" + resourceName + ".html");

		}
		try {
			loader.putTemplate(resourceName, IOUtils.toString(htmlResourceStream, "UTF-8"));
			Configuration freemarkerConfig = new Configuration();
			freemarkerConfig.setTemplateLoader(loader);
			DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper();
			objectWrapper.setExposureLevel(DefaultObjectWrapper.EXPOSE_ALL);
			freemarkerConfig.setObjectWrapper(objectWrapper);
			return freemarkerConfig.getTemplate(resourceName);
		} catch (IOException e) {
			NoOp.noOp();
		} finally {
			IOUtils.closeQuietly(htmlResourceStream);
		}
		return null;
	}

	private File tempFile;

	/**
	 * Initialize browser tabs with initial page.
	 * 
	 * @param tabbedPane
	 *            tabbedPane
	 * @param proxyEndPointPair
	 *            proxy endpoint info
	 */
	protected void initializeBrowserTab(TabbedPane tabbedPane, ProxyEndPointPair proxyEndPointPair) {

		Template template = prepareTemplate("initial", Locale.getDefault().getLanguage());
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		FileOutputStream fileOutputStream = null;
		OutputStreamWriter writer = null;
		try {
			tempFile = File.createTempFile("ngrinder", "init_page.html");
			tempFile.deleteOnExit();
			fileOutputStream = new FileOutputStream(tempFile);
			writer = new OutputStreamWriter(fileOutputStream, Charsets.UTF_8);

			EndPoint httpEndPoint = proxyEndPointPair.getHttpEndPoint();

			httpEndPoint = new EndPoint(InetAddress.getByName(httpEndPoint.getHost()).getHostAddress(),
					httpEndPoint.getPort());
			hashMap.put("http_port", httpEndPoint);
			hashMap.put("https_port", httpEndPoint);
			hashMap.put("embedded_browser_recoding_enabled", isInAppBrowserRecodingSupportEnv());
			template.process(hashMap, writer);
		} catch (Exception e) {
			noOp();
		} finally {
			IOUtils.closeQuietly(fileOutputStream);
			IOUtils.closeQuietly(writer);
		}
		List<Tab> tabs = tabbedPane.getTabs();
		for (Tab each : tabs) {
			if (each.getTabItemContent() instanceof BrowserContent) {
				((BrowserContent) each.getTabItemContent()).moveTo(toURL(tempFile));
			}
		}
	}

	protected boolean isInAppBrowserRecodingSupportEnv() {
		return SystemUtils.IS_OS_WINDOWS;
	}

	/**
	 * Setup new windows manager. This defines how to handle new windows in the
	 * browser.
	 * 
	 * @param tabFactory
	 *            {@link TabFactory}
	 * @param tabbedPane
	 *            {@link TabbedPane}
	 */
	protected void setupNewWindowsManager(final TabFactory tabFactory, final TabbedPane tabbedPane) {
		PopupContainer popupContainer = new PopupContainer() {

			@Override
			public void insertBrowser(Browser browser, Rectangle arg1) {
				browser.setPopupHandler(new PopupHandler() {
					@Override
					public PopupContainer handlePopup(PopupParams param) {
						return BrowserFactoryEx.getPopupContainer();
					}
				});

				final Tab createBrowserTab = tabFactory.createBrowserTab(browser);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tabbedPane.addTab(createBrowserTab);
						tabbedPane.selectTab(createBrowserTab);
					}
				});
			}
		};
		BrowserFactoryEx.setPopupContainer(popupContainer);
	}

	/**
	 * Create a split pane with the give left and right components.
	 * 
	 * @param left
	 *            component located in left
	 * @param right
	 *            component located in right
	 * @return JSplitPane instance
	 */
	protected JSplitPane createSplitPane(final JComponent left, final JComponent right) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, left, right);
		splitPane.setDividerLocation(splitPane.getSize().width - splitPane.getInsets().right
				- splitPane.getDividerSize() - 200);
		splitPane.setResizeWeight(1);
		splitPane.setMinimumSize(new Dimension(600, 600));
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(10);
		return splitPane;
	}

	/**
	 * Program entry port.
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {

		try {
			initEnvironment();
			Recorder recorder = new Recorder();
			recorder.start();
		} catch (Exception e) {
			LOGGER.error("Error while starting Recorder", e);
		}
	}

	private static void initEnvironment() throws Exception {
		if (GraphicsEnvironment.isHeadless()) {
			throw processException("nGrinder Recorder can not run in the headless environment");
		}
		System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
		System.setProperty("apple.laf.useScreenMenuBar", "false");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "nGrinder Recorder");
		System.setProperty("python.cachedir.skip", "true");
		System.setProperty("jxbrowser.ie.compatibility-disabled", "true");
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		UIManager.setLookAndFeel(getLookAndFeelClassName());
		// Make the jython is loaded in the background
		AsyncUtil.invokeAsync(new Runnable() {
			@Override
			public void run() {
				org.python.core.ParserFacade.parse("print 'hello'", CompileMode.exec, "unnamed", new CompilerFlags(
						CompilerFlags.PyCF_DONT_IMPLY_DEDENT | CompilerFlags.PyCF_ONLY_AST));
			}

		});
	}

	private static String getLookAndFeelClassName() {
		return UIManager.getSystemLookAndFeelClassName();
	}

	private void insertSupportedBrowserTabs(TabFactory tabFactory, final TabbedPane tabbedPane) {
		List<Tab> tabs = tabFactory.createSupportedBrowserTabs();
		for (Tab tab : tabs) {
			tabbedPane.addTab(tab);
			tabbedPane.selectTab(tab);
		}
	}

	private void insertNewBrowserButton(final TabFactory tabFactory, final TabbedPane tabbedPane) {
		TabButtonFactory factory = new TabButtonFactory();
		List<TabButton> buttons = factory.createNewBrowserButtons();
		for (TabButton button : buttons) {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					AsyncUtil.invokeAsync(new Runnable() {
						public void run() {
							NewBrowserButton browserButton = (NewBrowserButton) e.getSource();
							final Tab tab = tabFactory.createBrowserTab();

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									tabbedPane.addTab(tab);
									tabbedPane.selectTab(tab);
								}
							});
						}
					});
				}
			});
			tabbedPane.addTabButton(button);
		}
	}
}
