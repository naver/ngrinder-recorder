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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.grinder.util.Language;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

/**
 * Script Editor Control.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ScriptPageContent extends TabItemContent {

	/** UID. */
	private static final long serialVersionUID = -1042184585296206770L;
	private RSyntaxTextArea textArea = null;
	private JTextField searchField = null;
	private JTextField replaceField = null;

	/**
	 * Script page content.
	 * 
	 * @param script
	 *            script
	 * @param language
	 *            script language
	 */
	public ScriptPageContent(String script, Language language) {
		setLayout(new BorderLayout());

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setTabSize(4);
		textArea.setSyntaxEditingStyle(language.getContentType());
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		RTextScrollPane scrollPane = new RTextScrollPane(textArea);
		scrollPane.setFoldIndicatorEnabled(true);

		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textArea.setText(script);
		textArea.setVisible(true);
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
					searchField.requestFocus();
				}
				super.keyPressed(e);
			}
		});

		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		add(createToolBar(), BorderLayout.SOUTH);
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();

		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalStrut(20));
		JLabel searchFieldLable = new JLabel("Search For : ");
		toolBar.add(searchFieldLable);

		searchField = new JTextField(20);
		toolBar.add(searchField);

		final JButton nextButton = new JButton("Next");
		nextButton.setActionCommand("FindNext");
		expandBorder(nextButton);
		toolBar.add(nextButton);
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});
		JButton prevButton = new JButton("Prev");
		prevButton.setActionCommand("FindPrev");
		expandBorder(prevButton);
		toolBar.add(prevButton);
		toolBar.addSeparator();
		toolBar.add(Box.createHorizontalStrut(20));
		JLabel replaceFieldLable = new JLabel("Replace With : ");

		toolBar.add(replaceFieldLable);
		replaceField = new JTextField(20);
		toolBar.add(replaceField);
		final JButton replaceButton = new JButton("Next");
		replaceButton.setActionCommand("ReplaceNext");
		expandBorder(replaceButton);
		toolBar.add(replaceButton);
		toolBar.addSeparator();
		final JCheckBox regexCB = new JCheckBox("Regex");
		expandBorder(regexCB);
		toolBar.add(regexCB);
		final JCheckBox matchCaseCB = new JCheckBox("Match Case");
		expandBorder(matchCaseCB);
		toolBar.add(matchCaseCB);
		ActionListener findAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// "FindNext" => search forward, "FindPrev" => search backward
				String command = e.getActionCommand();
				boolean forward = "FindNext".equals(command);

				// Create an object defining our search parameters.
				SearchContext context = new SearchContext();
				String text = searchField.getText();
				if (text.length() == 0) {
					return;
				}
				context.setSearchFor(text);
				context.setMatchCase(matchCaseCB.isSelected());
				context.setRegularExpression(regexCB.isSelected());
				context.setSearchForward(forward);
				context.setWholeWord(false);

				boolean found = SearchEngine.find(textArea, context);
				if (!found) {
					JOptionPane.showMessageDialog(ScriptPageContent.this, "Text not found");
				}
			}
		};
		nextButton.addActionListener(findAction);
		prevButton.addActionListener(findAction);

		ActionListener replaceAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Create an object defining our search parameters.
				SearchContext context = new SearchContext();
				String text = searchField.getText();
				if (text.length() == 0) {
					return;
				}

				String replaceText = replaceField.getText();
				context.setSearchFor(text);
				context.setReplaceWith(replaceText);
				context.setMatchCase(matchCaseCB.isSelected());
				context.setRegularExpression(regexCB.isSelected());
				context.setSearchForward(true);
				context.setWholeWord(false);

				boolean found = SearchEngine.replace(textArea, context);
				if (!found) {
					JOptionPane.showMessageDialog(ScriptPageContent.this, "Text not found");
				}
			}
		};
		replaceButton.addActionListener(replaceAction);
		return toolBar;
	}

	private void expandBorder(JComponent component) {
		Border current = component.getBorder();
		Border empty = new EmptyBorder(0, 2, 0, 5);
		if (current == null) {
			component.setBorder(empty);
		} else {
			component.setBorder(new CompoundBorder(empty, current));
		}

	}

	@Override
	public void dispose() {
	}

}
