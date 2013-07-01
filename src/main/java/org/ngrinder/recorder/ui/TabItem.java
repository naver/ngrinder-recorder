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
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ngrinder.recorder.util.ResourceUtil;

/**
 * TabItem.
 * 
 * This class is from JxBrowserDemo
 * 
 * @author Vladimir Ikryanov
 * @author JunHo Yoon (modified By)
 * @since 1.0
 */
public class TabItem extends JPanel {

	/** UUID. */
	private static final long serialVersionUID = -8603152783605459728L;
	private Icon icon;
	private String title;
	private boolean selected;

	private final TabItemComponent component;

	/**
	 * Constructor.
	 */
	public TabItem() {
		this.component = new TabItemComponent();
		createUI();
		initializeEvents();
	}

	private void createUI() {
		setLayout(new BorderLayout());
		setOpaque(false);
		add(component, BorderLayout.CENTER);
		add(Box.createHorizontalStrut(1), BorderLayout.EAST);
	}

	private void initializeEvents() {
		component.addPropertyChangeListener("CloseButtonPressed", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange("CloseButtonPressed", evt.getOldValue(), evt.getNewValue());
			}
		});
		component.addPropertyChangeListener("TabItemClicked", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				setSelected(true);
			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(155, 26);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(50, 26);
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	public Icon getIcon() {
		return icon;
	}

	/**
	 * Set the icon.
	 * 
	 * @param icon
	 *            icon to be set
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
		component.setIcon(icon);
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Set the title.
	 * 
	 * @param title
	 *            title to be set
	 */
	public void setTitle(String title) {
		this.title = title;
		component.setTitle(title);
	}

	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set the selection.
	 * 
	 * @param selected
	 *            true if selected.
	 */
	public void setSelected(boolean selected) {
		boolean oldValue = this.selected;
		this.selected = selected;
		component.setSelected(selected);
		firePropertyChange("Selected", oldValue, selected);
	}

	private static final class TabItemComponent extends JPanel {

		/** UID. */
		private static final long serialVersionUID = -2981557070951475679L;
		private JLabel label;
		private final Color selectedTabBackground;

		private TabItemComponent() {
			selectedTabBackground = getBackground();
			createUI();
			initializeEvents();
		}

		private void createUI() {
			setLayout(new BorderLayout());
			setOpaque(false);
			add(createLabel(), BorderLayout.CENTER);
			add(createCloseButton(), BorderLayout.EAST);
		}

		private void initializeEvents() {
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						firePropertyChange("TabItemClicked", false, true);
					}
					if (e.getButton() == MouseEvent.BUTTON2) {
						firePropertyChange("CloseButtonPressed", false, true);
					}
				}
			});
		}

		private JComponent createLabel() {
			label = new JLabel();
			label.setOpaque(false);
			label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			return label;
		}

		private JComponent createCloseButton() {
			JButton closeButton = new JButton();
			closeButton.setOpaque(false);
			closeButton.setToolTipText("Close");
			closeButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			closeButton.setPressedIcon(ResourceUtil.getIcon("close-pressed.png"));
			closeButton.setIcon(ResourceUtil.getIcon("close.png"));
			closeButton.setContentAreaFilled(false);
			closeButton.setFocusable(false);
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					firePropertyChange("CloseButtonPressed", false, true);
				}
			});
			return closeButton;
		}

		public void setTitle(final String title) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					label.setText(title);
					label.setToolTipText(title);
				}
			});
		}

		public void setIcon(Icon icon) {
			label.setIcon(icon);
		}

		public void setSelected(boolean selected) {
			setBackground(selected ? selectedTabBackground : new Color(150, 150, 150));
			repaint();
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setPaint(new GradientPaint(0, 0, Color.LIGHT_GRAY, 0, getHeight(), getBackground()));
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.dispose();
			super.paint(g);
		}
	}
}
