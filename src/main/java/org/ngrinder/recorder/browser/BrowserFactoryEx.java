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
package org.ngrinder.recorder.browser;

import static net.grinder.util.Preconditions.checkNotNull;

import org.ngrinder.recorder.infra.NGrinderRuntimeException;
import org.ngrinder.recorder.infra.RecorderConfig;
import org.ngrinder.recorder.proxy.ProxyEndPointPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserFactory;
import com.teamdev.jxbrowser.chromium.CreateParams;
import com.teamdev.jxbrowser.chromium.CustomProxyConfig;
import com.teamdev.jxbrowser.chromium.HostPortPair;
import com.teamdev.jxbrowser.chromium.PopupContainer;
import com.teamdev.jxbrowser.chromium.PopupHandler;
import com.teamdev.jxbrowser.chromium.PopupParams;
import com.teamdev.jxbrowser.chromium.SilentDialogHandler;

/**
 * Extended Browser Factory.
 * 
 * This class is responsible to list and create the supported browsers in
 * nGrinder Recorder.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public abstract class BrowserFactoryEx {
	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserFactory.class);
	private static SilentDialogHandler silentDialogHandler = new SilentDialogHandler() {

	};
	private static PopupContainer popupContainer;
	private static ProxyEndPointPair endPoints;
	@SuppressWarnings("unused")
	private static RecorderConfig config;
	private static PopupHandler popupHandler = new PopupHandler() {
		@Override
		public PopupContainer handlePopup(PopupParams param) {
			return popupContainer;
		}
	};

	/**
	 * Create the browser.
	 * 
	 * @param type
	 *            browser type.
	 * @return created browser instance
	 */
	public static Browser create() {
		checkNotNull(endPoints, "setProxyEndPoints() should be called in advance.");
		try {
			CreateParams param = createBrowserProxy(endPoints);
			Browser browser = BrowserFactory.create(param);
			browser.setDialogHandler(silentDialogHandler);
			browser.setPopupHandler(popupHandler);
			return browser;
		} catch (Exception e) {
			LOGGER.error("Failed to create browser due to ", e);
			throw processException("Failed to create browser due to ", e);
		}
	}

	public static PopupContainer getPopupContainer() {
		return BrowserFactoryEx.popupContainer;
	}

	public static void setPopupContainer(PopupContainer popupContainer) {
		BrowserFactoryEx.popupContainer = popupContainer;
	}

	/**
	 * Set the proxy end point to which the browser will connect to.
	 * 
	 * @param endPoints
	 *            endPoints
	 */
	public static void setProxyEndPoints(ProxyEndPointPair endPoints) {
		BrowserFactoryEx.endPoints = endPoints;
	}

	public static void setPopupHandler(PopupHandler handler) {
		BrowserFactoryEx.popupHandler = handler;
	}

	/**
	 * Set the proxy end point to which the browser will connect to.
	 * 
	 * @param endPoints
	 *            endPoints
	 */
	public static void setRecorderConfig(RecorderConfig config) {
		BrowserFactoryEx.config = config;
	}

	/**
	 * Set up the given browser with the given end points.
	 * 
	 * @param browser
	 *            browser to be set up
	 * @param endPoints
	 *            endPoints to which the browser connects to
	 */
	public static CreateParams createBrowserProxy(ProxyEndPointPair endPoints) {
		CreateParams createParams = CreateParams.createDefault();
		if (endPoints == null) {
			return createParams;
		} else {
			HostPortPair httpServer = new HostPortPair(endPoints.getHttpEndPoint().getHost(), endPoints
					.getHttpEndPoint().getPort());
			HostPortPair httpsServer = new HostPortPair(endPoints.getHttpEndPoint().getHost(), endPoints
					.getHttpEndPoint().getPort());
			createParams.setProxyConfig(new CustomProxyConfig(httpServer, httpsServer, null, null));
		}
		return createParams;
	}
}
