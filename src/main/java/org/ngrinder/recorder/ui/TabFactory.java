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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import net.grinder.util.Language;

import org.ngrinder.recorder.browser.BrowserFactoryEx;
import org.ngrinder.recorder.util.ResourceUtil;
import org.python.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamdev.jxbrowser.chromium.Browser;

/**
 * Tab Factory.
 * 
 * This class is from JxBrowserDemo
 * 
 * @author Vladimir Ikryanov
 * @author JunHo Yoon (modified by)
 * @since 1.0
 */
public class TabFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(TabFactory.class);

	/**
	 * Constructor.
	 */
	public TabFactory() {
	}

	/**
	 * Create the given type of browser tab.
	 * 
	 * @param type
	 *            browser type
	 * @return created tab.
	 */
	public Tab createBrowserTab() {
		return createBrowserTab(BrowserFactoryEx.create());
	}

	/**
	 * Create a browser tab with the given browser.
	 * 
	 * @param browser
	 *            browser
	 * @return created tab
	 */
	public Tab createBrowserTab(Browser browser) {
		final TabItem tabItem = new TabItem();
		tabItem.setTitle("about:blank");
		tabItem.setIcon(ResourceUtil.getIcon("IE.png"));

		TabItemContent tabItemContent = new BrowserContent(browser);
		tabItemContent.addPropertyChangeListener("PageTitleChanged", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				tabItem.setTitle((String) evt.getNewValue());
			}
		});
		return new Tab(tabItem, tabItemContent);
	}

	/**
	 * Create a script tab.
	 * 
	 * @param script
	 *            script to be show.
	 * @param lang
	 *            the language of script
	 * @return created tab
	 */
	public Tab createScriptTab(Language lang, String script) {
		final TabItem tabItem = new TabItem();
		tabItem.setTitle("Script");
		TabItemContent tabItemContent = new ScriptPageContent(script, lang);
		return new Tab(tabItem, tabItemContent);
	}

	/**
	 * Create a page source tab.
	 * 
	 * @param browser
	 *            the browser from which page source is from
	 * @return created tab
	 */
	public Tab createPageSourceTab(Browser browser) {
		final TabItem tabItem = new TabItem();
		tabItem.setTitle("Page source: " + browser.getURL());
		tabItem.setIcon(ResourceUtil.getIcon("IE.png"));

		String pageContent = browser.getHTML();
		TabItemContent tabItemContent = new SourcePageContent(pageContent);
		tabItemContent.addPropertyChangeListener("PageTitleChanged", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				tabItem.setTitle((String) evt.getNewValue());
			}
		});
		return new Tab(tabItem, tabItemContent);
	}

	/**
	 * Create multiple supported browser tabs.
	 * 
	 * @return multiple tabs
	 */
	public List<Tab> createSupportedBrowserTabs() {
		List<Tab> result = Lists.newArrayList();
		try {
			addBrowserTabIfBrowserTypeSupported(result);
			LOGGER.info("Chrome is activated", "Chrome");
		} catch (Exception e) {
			LOGGER.error("Error while creating new browser tab chrome", e);
			e.printStackTrace();
		}

		return result;
	}

	private void addBrowserTabIfBrowserTypeSupported(List<Tab> result) {
		result.add(createBrowserTab());
	}
}
