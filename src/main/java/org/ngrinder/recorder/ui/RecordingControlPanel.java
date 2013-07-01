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

import static net.grinder.util.CollectionUtils.newArrayList;
import static net.grinder.util.CollectionUtils.newHashSet;
import static org.ngrinder.recorder.ui.component.SwingUtils.attachPopupMenu;
import static org.ngrinder.recorder.ui.component.SwingUtils.createMenuItem;
import static org.ngrinder.recorder.ui.component.SwingUtils.createSimpleDropDownButton;
import static org.ngrinder.recorder.ui.component.SwingUtils.createSimpleTextButton;
import static org.ngrinder.recorder.ui.component.SwingUtils.decoratedToSimpleButton;
import static org.ngrinder.recorder.ui.component.SwingUtils.wrapScroll;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.grinder.plugin.http.tcpproxyfilter.ConnectionFilter;
import net.grinder.plugin.http.tcpproxyfilter.options.FileTypeCategory;
import net.grinder.plugin.http.tcpproxyfilter.options.GenerationOption;
import net.grinder.tools.tcpproxy.EndPoint;
import net.grinder.util.CollectionUtils;
import net.grinder.util.NoOp;
import net.grinder.util.Pair;

import org.apache.commons.io.FileUtils;
import org.ngrinder.recorder.browser.BrowserFactoryEx;
import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.Topics;
import org.ngrinder.recorder.ui.component.DropDownButton;
import org.ngrinder.recorder.ui.component.JExtendedCheckBox;

import com.google.gson.Gson;
import com.teamdev.jxbrowser.BrowserServices;
import com.teamdev.jxbrowser.BrowserType;
import com.teamdev.jxbrowser.cookie.HttpCookie;
import com.teamdev.jxbrowser.cookie.HttpCookieStorage;

/**
 * UI Component which contains the recoding options controller.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class RecordingControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Timer timer;
	private MessageBus messageBus;
	private ConnectionFilter connectionFilter;
	private JComponent typeFilter;
	private JComponent generationOptions;
	private OptionPersistencyHandler typeFilterPersistentHandler;
	private OptionPersistencyHandler generationOptionsPersistentHandler;
	private JPopupMenu createFilterTablePopUp;
	private JTable createFilterTables;

	/**
	 * Constructor.
	 * 
	 * @param connectionFilter
	 *            {@link ConnectionFilter}
	 */
	public RecordingControlPanel(ConnectionFilter connectionFilter) {
		this.connectionFilter = connectionFilter;
		initUI(connectionFilter);
		initEventHandler();
	}

	/**
	 * Initialize event handler.
	 */
	protected void initEventHandler() {
		// Global Event
		messageBus = MessageBus.getInstance();
		messageBus.connect().subscribe(Topics.PREPARE_TO_CLOSE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				File home = (File) evt.getNewValue();
				typeFilterPersistentHandler.save(home);
				generationOptionsPersistentHandler.save(home);
			}

		});
		messageBus.connect().subscribe(Topics.PREPARE_TO_VIEW, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				File home = (File) evt.getNewValue();
				typeFilterPersistentHandler.load(home);
				generationOptionsPersistentHandler.load(home);
			}

		});

		// Attach event.
		createFilterTablePopUp = createFilterTablePopUp();
		attachPopupMenu(createFilterTables, createFilterTablePopUp);
	}

	/**
	 * Initialize UI.
	 * 
	 * @param connectionFilter
	 *            connection filter
	 */
	protected void initUI(ConnectionFilter connectionFilter) {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;

		addGrid(gbl, gbc, createRecordingButton(), new Rectangle(0, 0, 1, 1), 0, 0);
		createFilterTables = createFilterTables(connectionFilter);
		addGrid(gbl, gbc, wrapScroll(createFilterTables), new Rectangle(0, 1, 1, 5), 1, 0.7);
		addGrid(gbl, gbc, createFilterButtonPanel(), new Rectangle(0, 7, 1, 1), 0, 0);

		typeFilter = createTypeFilterPanel();
		typeFilterPersistentHandler = new OptionPersistencyHandler(typeFilter, "type_filter");
		addGrid(gbl, gbc, typeFilter, new Rectangle(0, 8, 1, 3), 0, 0);
		generationOptions = createGenerationOptionsPanel();
		generationOptionsPersistentHandler = new OptionPersistencyHandler(generationOptions, "generation_options");
		addGrid(gbl, gbc, generationOptions, new Rectangle(0, 11, 1, 1), 0, 0);
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

	private JPopupMenu createFilterTablePopUp() {
		final JPopupMenu result = new JPopupMenu();
		JMenuItem excludeSelectedHosts = new JMenuItem("Exclude selected hosts", KeyEvent.VK_U);
		result.add(excludeSelectedHosts);
		excludeSelectedHosts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = createFilterTables.getSelectedRows();
				((FilterTableModel) createFilterTables.getModel()).setSelection(selectedRows, false);
			}
		});

		JMenuItem includeSelectedHost = new JMenuItem("Include selected hosts", KeyEvent.VK_I);
		result.add(includeSelectedHost);
		includeSelectedHost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = createFilterTables.getSelectedRows();
				((FilterTableModel) createFilterTables.getModel()).setSelection(selectedRows, true);
			}
		});
		result.addSeparator();

		JMenuItem deleteSelectedHost = new JMenuItem("Deleted selected hosts", KeyEvent.VK_D);
		result.add(deleteSelectedHost);
		deleteSelectedHost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = createFilterTables.getSelectedRows();
				((FilterTableModel) createFilterTables.getModel()).removeRows(selectedRows);
			}
		});
		return result;
	}

	/**
	 * Create filter by type panel.
	 * 
	 * @return created panel
	 */
	protected JPanel createTypeFilterPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Recorded Type"));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (FileTypeCategory each : FileTypeCategory.values()) {
			panel.add(new JExtendedCheckBox(each.name(), each.getDisplayName(), true));
		}
		return panel;
	}

	/**
	 * {@link JExtendedCheckBox} save / load handler.
	 * 
	 * @author JunHo Yoon
	 * @since 1.0
	 */
	static class OptionPersistencyHandler {
		private final Container container;
		private final String fileName;

		private static final Gson GSON = new Gson();

		/**
		 * Constructor.
		 * 
		 * @param container
		 *            container of the {@link JExtendedCheckBox} components.
		 * @param fileName
		 *            file to be saved or loaded
		 */
		public OptionPersistencyHandler(Container container, String fileName) {
			this.container = container;
			this.fileName = fileName;
		}

		/**
		 * Save the selected components in the given home folder.
		 * 
		 * @param home
		 *            home
		 */
		public void save(File home) {
			try {
				List<String> list = CollectionUtils.newArrayList();
				for (Component each : container.getComponents()) {
					if (each instanceof JExtendedCheckBox) {
						JExtendedCheckBox checkBox = (JExtendedCheckBox) each;
						if (checkBox.isSelected()) {
							list.add(checkBox.getId());
						}
					}

					if (each instanceof JPanel) {
						for (Component subEach : ((JPanel) each).getComponents()) {
							if (subEach instanceof JComboBox) {
								JComboBox comboBox = (JComboBox) subEach;
								list.add(((GenerationOption) comboBox.getSelectedItem()).name());
							}
						}
					}
				}
				String json = GSON.toJson(list);
				File recodingTypeFile = new File(home, fileName);

				FileUtils.write(recodingTypeFile, json);
			} catch (Exception e) {
				NoOp.noOp();
			}
		}

		/**
		 * Load the selected components in the given home folder.
		 * 
		 * @param home
		 *            home
		 */
		@SuppressWarnings("unchecked")
		public void load(File home) {
			File savedFile = new File(home, fileName);
			if (savedFile.exists()) {
				ArrayList<String> savedData;
				try {
					savedData = (ArrayList<String>) GSON.fromJson(FileUtils.readFileToString(savedFile),
									ArrayList.class);

					for (Component each : container.getComponents()) {
						if (each instanceof JExtendedCheckBox) {
							JExtendedCheckBox checkBox = (JExtendedCheckBox) each;
							checkBox.setSelected(savedData.contains(checkBox.getId()));
						}
						if (each instanceof JPanel) {
							for (Component subEach : ((JPanel) each).getComponents()) {
								if (subEach instanceof JComboBox) {
									JComboBox comboBox = (JComboBox) subEach;
									if (savedData.contains("Jython")) {
										comboBox.setSelectedItem(GenerationOption.Jython);
									} else {
										comboBox.setSelectedItem(GenerationOption.Groovy);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					NoOp.noOp();
				}
			}

		}
	}

	/**
	 * Get FileTypeCategory list which will be filtered.
	 * 
	 * @return {@link FileTypeCategory} list
	 */
	private List<FileTypeCategory> getFilteredFileTypes() {
		List<FileTypeCategory> list = newArrayList();
		for (Component each : this.typeFilter.getComponents()) {
			if (each instanceof JCheckBox) {
				JExtendedCheckBox checkBox = (JExtendedCheckBox) each;
				if (!checkBox.isSelected()) {
					list.add(FileTypeCategory.valueOf(checkBox.getId()));
				}
			}
		}
		return list;
	}

	/**
	 * Get the selected {@link GenerationOption} list.
	 * 
	 * @return set of selected {@link GenerationOption}
	 */
	private Set<GenerationOption> getGenerationOption() {
		Set<GenerationOption> list = newHashSet();
		for (Component each : this.generationOptions.getComponents()) {
			if (each instanceof JCheckBox) {
				JExtendedCheckBox checkBox = (JExtendedCheckBox) each;
				if (checkBox.isSelected()) {
					list.add(GenerationOption.valueOf(checkBox.getId()));
				}
			}
			if (each instanceof JPanel) {
				for (Component subEach : ((JPanel) each).getComponents()) {
					if (subEach instanceof JComboBox) {
						JComboBox comboBox = (JComboBox) subEach;
						list.add((GenerationOption) comboBox.getSelectedItem());
					}
				}
			}
		}
		return list;
	}

	/**
	 * Create "Start Recording" Button and attach the event handler.
	 * 
	 * @return created Button.
	 */
	private JToggleButton createRecordingButton() {
		JToggleButton button = decoratedToSimpleButton(new JToggleButton("Start Recording"));
		button.setText("Start Recording");
		button.setSelected(false);
		button.setMinimumSize(new Dimension(100, 30));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton) e.getSource();
				if (button.isSelected()) {
					button.setText("Stop Recording");
					button.setSelected(true);
					messageBus.getPublisher(Topics.START_RECORDING).propertyChange(
									new PropertyChangeEvent(RecordingControlPanel.this, "Start Recording", null,
													getFilteredFileTypes()));
				} else {
					if (!stopConfirm()) {
						button.setSelected(true);
						return;
					}
					messageBus.getPublisher(Topics.STOP_RECORDING).propertyChange(
									new PropertyChangeEvent(RecordingControlPanel.this, "Stop Recording", null, Pair
													.of(getFilteredFileTypes(), getGenerationOption())));
					button.setText("Start Recording");
				}
			}
		});
		return button;
	}

	private boolean stopConfirm() {
		JOptionPane pane = new JOptionPane("Do you want to stop recording?", JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_OPTION, null);
		JDialog dialog = pane.createDialog(SwingUtilities.getWindowAncestor(this), "Stop recording");
		dialog.setVisible(true);
		Object selectedValue = pane.getValue();
		if (selectedValue == null) {
			return false;
		}
		return JOptionPane.YES_OPTION == (Integer) selectedValue;
	}

	/**
	 * Create Generation Options Panel.
	 * 
	 * @return generated panel
	 */
	protected JPanel createGenerationOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Script Generation Options"));
		GridLayout mgr = new GridLayout(3, 1);
		panel.setLayout(mgr);
		for (GenerationOption each : GenerationOption.getOptions(null)) {
			JExtendedCheckBox comp = new JExtendedCheckBox(each.name(), each.getDisplayName(), true);
			panel.add(comp);

		}
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(1, 2));
		JLabel languageLabel = new JLabel("Language");
		subPanel.add(languageLabel);
		JComboBox languageCombo = new JComboBox();
		for (GenerationOption each : GenerationOption.getOptions("Language")) {
			languageCombo.addItem(each);
		}
		subPanel.add(languageCombo);
		panel.add(subPanel);
		return panel;
	}

	/**
	 * Create Filter Button Panel.
	 * 
	 * @return filter button panel
	 */
	protected JPanel createFilterButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(createSimpleTextButton("Unselect All", new AbstractAction() {
			/** UUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				for (EndPoint each : connectionFilter.getConnectionEndPoints()) {
					connectionFilter.setFilter(each, true);
				}
			}
		}));
		DropDownButton resetDropDownButton = createSimpleDropDownButton("Reset", null);
		panel.add(resetDropDownButton);
		JPopupMenu menu = new JPopupMenu();
		menu.add(createMenuItem("Clear all recording", new AbstractAction() {
			/** UUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				messageBus.getPublisher(Topics.RESET).propertyChange(
								new PropertyChangeEvent(RecordingControlPanel.this, "RESET", null, null));
				connectionFilter.makeZeroCount();
			}
		}));

		menu.add(createMenuItem("Clear all filters", new AbstractAction() {
			/** UUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				connectionFilter.reset();
			}
		}));

		menu.add(createMenuItem("Clear connection count", new AbstractAction() {
			/** UUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				connectionFilter.makeZeroCount();
			}
		}));

		menu.add(createMenuItem("Reset browser cache", new AbstractAction() {
			/** UUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserServices browserServices = BrowserServices.getInstance();
				for (BrowserType type : BrowserFactoryEx.getSupportedBrowser()) {
					clearCacheIfSupported(browserServices, type);
				}
			}

			private void clearCacheIfSupported(BrowserServices browserServices, BrowserType type) {
				if (type.isSupported()) {
					try {
						browserServices.getCacheStorage(type).clearCache();
					} catch (Exception e) {
						NoOp.noOp();
					}
				}
			}

		}));
		menu.add(createMenuItem("Reset cookies", new AbstractAction() {
			/** UUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserServices browserServices = BrowserServices.getInstance();
				for (BrowserType type : BrowserFactoryEx.getSupportedBrowser()) {
					clearCookieIfSupported(browserServices, type);
				}
			}

			private void clearCookieIfSupported(BrowserServices browserServices, BrowserType type) {
				if (type.isSupported()) {
					try {
						HttpCookieStorage cookieStorage = browserServices.getCookieStorage(type);
						List<HttpCookie> cookies = cookieStorage.getCookies();
						cookieStorage.deleteCookie(cookies);
					} catch (Exception e) {
						NoOp.noOp();
					}
				}
			}

		}));
		resetDropDownButton.setMenu(menu);
		return panel;
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(200, 0);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(200, 0);
	}

	protected JTable createFilterTables(ConnectionFilter connectionFilter) {
		final FilterTableModel dm = new FilterTableModel(connectionFilter);
		JTable jTable = new JTable(dm);
		TableColumnModel columnModel = jTable.getColumnModel();
		setColumnWidth(columnModel.getColumn(0), 30, 30, 30);
		setColumnWidth(columnModel.getColumn(2), 30, 50, 30);
		jTable.setRowSorter(new TableRowSorter<TableModel>(dm));
		createUpdateCheckScheduledTask(dm);
		return jTable;
	}

	private void createUpdateCheckScheduledTask(final FilterTableModel dm) {
		this.timer = new Timer(true);
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				dm.update();
			}
		}, 1000, 1000);
	}

	private void setColumnWidth(TableColumn column, int initial, int max, int min) {
		if (initial != 0) {
			column.setPreferredWidth(initial);
		}
		if (max != 0) {
			column.setMaxWidth(max);
		}
		if (min != 0) {
			column.setMinWidth(min);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 1000);
	}

}
