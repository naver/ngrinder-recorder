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
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.Topics;

/**
 * Source page content.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author Vladimir Ikryanov
 * @author JunHo Yoon (modified by)
 * @since 1.0
 */
public class SourcePageContent extends TabItemContent {

	/** UUID. */
	private static final long serialVersionUID = -1042184585296206770L;
	private JTextArea textArea = null;

	/**
	 * Source Page Content.
	 * 
	 * @param browserContent
	 *            source content.
	 */
	public SourcePageContent(String browserContent) {
		setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setCaretPosition(0);
		textArea.setTabSize(4);
		textArea.setLineWrap(false);
		textArea.setEditable(true);
		textArea.setText(browserContent);

		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textArea.setFont(new Font("Courier", Font.PLAIN, 12));

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textArea.setComponentPopupMenu(createPopupMenu());
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void dispose() {
	}

	private JPopupMenu createPopupMenu() {
		final JPopupMenu result = new JPopupMenu();
		JMenuItem copySelectedText = new JMenuItem("Copy Selected Text", KeyEvent.VK_C);
		result.add(copySelectedText);
		copySelectedText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringSelection data = new StringSelection(textArea.getSelectedText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(data, data);
			}
		});

		JMenuItem copyAllText = new JMenuItem("Copy All Text", KeyEvent.VK_A);
		result.add(copyAllText);
		copyAllText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringSelection data = new StringSelection(textArea.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(data, data);
			}
		});
		result.addSeparator();
		JMenuItem closeTab = new JMenuItem("Close Tab", KeyEvent.VK_W);
		result.add(closeTab);
		closeTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		final PropertyChangeListener publisher = MessageBus.getInstance().getPublisher(Topics.REMOVE_TAB);
		closeTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				publisher.propertyChange(new PropertyChangeEvent(SourcePageContent.this, "Remove Tab", null, null));
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent mouseEvent) {
				showPopup(mouseEvent);
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				showPopup(mouseEvent);
			}

			public void showPopup(MouseEvent mouseEvent) {
				if (mouseEvent.isPopupTrigger()) {
					result.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
				}
			}
		});
		return result;
	}
}
