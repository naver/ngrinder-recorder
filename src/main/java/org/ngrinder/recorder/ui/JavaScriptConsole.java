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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.ngrinder.recorder.util.AsyncUtil;
import org.ngrinder.recorder.util.ResourceUtil;

import com.teamdev.jxbrowser.chromium.Browser;

/**
 * This class shows the javascript console.
 * 
 * It's from the JxBrowserDemo.
 * 
 * @author Oleg Zagrivyi
 * @author JunHo Yoon (modified by)
 */
public class JavaScriptConsole extends JPanel {

	/** UID. */
	private static final long serialVersionUID = 5770950299011831702L;
	private static final String NEW_LINE = "\n";
	private static final String QUERY_LINE_START = ">> ";
	public static final Color TITLE_PANEL_BACKGROUND = new Color(182, 191, 207);
	public static final String CONSOLE_CLOSED = "consoleClosed";

	private JTextArea console;
	private final Browser browser;

	/**
	 * Constructor.
	 * 
	 * @param browser
	 *            browser to which this console is attaching.
	 */
	public JavaScriptConsole(Browser browser) {
		this.browser = browser;
		createUI();
	}

	private void createUI() {
		setLayout(new BorderLayout());
		add(createTitle(), BorderLayout.NORTH);
		add(createConsoleOutput(), BorderLayout.CENTER);
		add(createConsoleInput(), BorderLayout.SOUTH);
	}

	private JComponent createConsoleInput() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBackground(Color.WHITE);

		JLabel label = new JLabel(QUERY_LINE_START);
		label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));

		final JTextField consoleInput = new JTextField();
		consoleInput.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		consoleInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String script = consoleInput.getText();
				AsyncUtil.invokeAsync(new Runnable() {
					public void run() {
						final String executionResult = executeScript(script);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								updateConsoleOutput(script, executionResult);
								consoleInput.setText("");
							}
						});
					}
				});
			}
		});
		result.add(label, BorderLayout.WEST);
		result.add(consoleInput, BorderLayout.CENTER);
		return result;
	}

	private JComponent createConsoleOutput() {
		console = new JTextArea();
		console.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		console.setEditable(false);
		console.setWrapStyleWord(true);
		console.setLineWrap(true);
		console.setText("");
		JScrollPane scrollPane = new JScrollPane(console);
		scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
		return scrollPane;
	}

	private JComponent createTitle() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(TITLE_PANEL_BACKGROUND);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		panel.add(createTitleLabel(), BorderLayout.WEST);
		panel.add(createCloseButton(), BorderLayout.EAST);
		return panel;
	}

	private JComponent createTitleLabel() {
		return new JLabel("JavaScript console");
	}

	private JComponent createCloseButton() {
		JButton closeButton = new JButton();
		closeButton.setOpaque(false);
		closeButton.setToolTipText("Close");
		closeButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		closeButton.setPressedIcon(ResourceUtil.getIcon("close-pressed.png"));
		closeButton.setIcon(ResourceUtil.getIcon("close.png"));
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusable(false);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firePropertyChange(CONSOLE_CLOSED, false, true);
			}
		});
		return closeButton;
	}

	private void updateConsoleOutput(String script, String executionResult) {
		displayScript(script);
		displayExecutionResult(executionResult);
		console.setCaretPosition(console.getText().length());
	}

	private String executeScript(String script) {
		try {
			return browser.executeJavaScriptAndReturnValue(script).getString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private void displayExecutionResult(String result) {
		console.append(result);
		console.append(NEW_LINE);
	}

	private void displayScript(String script) {
		console.append(QUERY_LINE_START);
		console.append(script);
		console.append(NEW_LINE);
	}
}
