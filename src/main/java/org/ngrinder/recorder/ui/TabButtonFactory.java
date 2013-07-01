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

import java.util.ArrayList;
import java.util.List;

import org.ngrinder.recorder.browser.BrowserFactoryEx;

import com.teamdev.jxbrowser.BrowserType;

/**
 * Tab Button Factory.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author Vladimir Ikryanov
 * @author JunHo Yoon (modified by)
 * @since 1.0
 */
public class TabButtonFactory {

	/**
	 * Create new browser buttons.
	 * 
	 * @return buttons.
	 */
	public List<TabButton> createNewBrowserButtons() {
		List<TabButton> result = new ArrayList<TabButton>();
		for (BrowserType each : BrowserFactoryEx.getSupportedBrowser()) {
			addButtonIfTypeSupported(result, each);
		}
		return result;
	}

	private void addButtonIfTypeSupported(List<TabButton> result, BrowserType type) {
		if (type.isSupported()) {
			result.add(new NewBrowserButton(type));
		}
	}
}
