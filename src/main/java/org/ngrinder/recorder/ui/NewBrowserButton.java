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

import org.ngrinder.recorder.util.ResourceUtil;

import com.teamdev.jxbrowser.BrowserType;

/**
 * New Browser Button Class.
 * 
 * This class is from JxBrowserDemo.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class NewBrowserButton extends TabButton {

	/** UUID. */
	private static final long serialVersionUID = -6114752964224227198L;
	private final BrowserType browserType;

	/**
	 * Constructor.
	 * 
	 * @param browserType
	 *            browser type
	 */
	public NewBrowserButton(BrowserType browserType) {
		super(ResourceUtil.getIcon(browserType.getName() + "-new.png"), "New " + browserType.getName() + " tab");
		this.browserType = browserType;
	}

	/**
	 * Get the browser type of this button.
	 * 
	 * @return {@link BrowserType} instance.
	 */
	public BrowserType getBrowserType() {
		return browserType;
	}
}
