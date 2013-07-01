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

import static net.grinder.util.NoOp.noOp;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.IOUtils;
import org.ngrinder.recorder.infra.RecorderConfig;
import org.ngrinder.recorder.util.ResourceUtil;

/**
 * nGrinder recorder "about" dialog class.
 * 
 * @author AlexQin
 * @version 1.0
 */
public final class AboutDialog extends JDialog {

	private static final long serialVersionUID = -2225460316898229436L;
	private static AboutDialog aboutDialog = null;
	private final RecorderConfig recordConfig;

	/**
	 * Get instance of AboutDialog class.
	 * 
	 * @param owner
	 *            owner frame
	 * @param recordConfig
	 *            recordConfig
	 * @return AboutDialog instance
	 */
	public static AboutDialog getInstance(Frame owner, RecorderConfig recordConfig) {
		if (aboutDialog == null) {
			aboutDialog = new AboutDialog(owner, recordConfig);
		}

		return aboutDialog;
	}

	/**
	 * Constructor.
	 * 
	 * @param owner
	 *            owner frame
	 * @param recordConfig
	 *            recordConfig
	 */
	private AboutDialog(Frame owner, RecorderConfig recordConfig) {
		super(owner, "About nGrinder Recorder", true);
		this.recordConfig = recordConfig;
		setName("aboutDialog");
		initUI();
		initKeyStroke();
		setResizable(false);
		pack();
		this.setSize(400, getHeight());
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	/**
	 * Initialize UI.
	 */
	protected void initUI() {
		JPanel jContentPane = new JPanel();
		jContentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));

		jContentPane.add(getIcon());
		jContentPane.add(Box.createVerticalStrut(16));
		jContentPane.add(getAppName());
		jContentPane.add(Box.createVerticalStrut(8));
		jContentPane.add(getVersion());
		jContentPane.add(Box.createVerticalStrut(8));
		jContentPane.add(getCompany());
		jContentPane.add(getRights());
		jContentPane.add(Box.createVerticalStrut(8));
		jContentPane.add(getLicenseTextArea());
		setContentPane(jContentPane);
	}

	private Component getLicenseTextArea() {
		JTextArea licenseTextArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(licenseTextArea);
		InputStream resourceAsStream = AboutDialog.class.getResourceAsStream("/license.txt");
		String licenseMessage;
		try {
			licenseMessage = IOUtils.toString(resourceAsStream);
			IOUtils.closeQuietly(resourceAsStream);
			licenseTextArea.setText(licenseMessage);
		} catch (IOException e) {
			noOp();
		}
		return scrollPane;
	}

	private void initKeyStroke() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dispose();
				}
			}
		});

		JRootPane rootPane = getRootPane();
		rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 421791976774749694L;

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "ESCAPE");
	}

	private JLabel getIcon() {
		JLabel icon = new JLabel();
		icon.setIcon(ResourceUtil.getIcon("recorder32x32.png"));
		icon.setAlignmentX(CENTER_ALIGNMENT);

		return icon;
	}

	private JLabel getAppName() {
		JLabel appName = new JLabel("nGrinder Recorder");
		appName.setAlignmentX(CENTER_ALIGNMENT);
		appName.setFont(appName.getFont().deriveFont(Font.BOLD, 12.0f));

		return appName;
	}

	private JLabel getVersion() {
		JLabel version = new JLabel("Version: " + recordConfig.getInternalProperty("ngrinder.recorder.version", "1.0"));
		version.setAlignmentX(CENTER_ALIGNMENT);
		return version;
	}

	private JLabel getCompany() {
		JLabel company = new JLabel("\u00A9 " + Calendar.getInstance().get(Calendar.YEAR) + " nGrinder Dev Community.");
		company.setAlignmentX(CENTER_ALIGNMENT);

		return company;
	}

	private JLabel getRights() {
		JLabel rights = new JLabel("All rights reserved.");
		rights.setAlignmentX(CENTER_ALIGNMENT);

		return rights;
	}
}
