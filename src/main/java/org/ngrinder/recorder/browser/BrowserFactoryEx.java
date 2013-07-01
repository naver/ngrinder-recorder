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

import static net.grinder.util.CollectionUtils.newArrayList;
import static net.grinder.util.Preconditions.checkNotNull;

import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.ngrinder.recorder.infra.NGrinderRuntimeException;
import org.ngrinder.recorder.infra.RecorderConfig;
import org.ngrinder.recorder.proxy.ProxyEndPointPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;
import com.teamdev.jxbrowser.proxy.ProxyConfig;
import com.teamdev.jxbrowser.proxy.ProxyServer;
import com.teamdev.jxbrowser.proxy.ServerType;

/**
 * Extended Browser Factory.
 * 
 * This class is responsible to list and create the supported browsers in nGrinder Recorder.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public abstract class BrowserFactoryEx {
	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserFactory.class);
	private static SilentSecurityHandler securityHandler = new SilentSecurityHandler();
	private static PromptIgnoreService promptIgnoreService = new PromptIgnoreService();
	private static ProxyEndPointPair endPoints;
	@SuppressWarnings("unused")
	private static RecorderConfig config;

	/**
	 * Create the browser.
	 * 
	 * @param type
	 *            browser type.
	 * @return created browser instance
	 */
	public static Browser create(BrowserType type) {
		checkNotNull(endPoints, "setProxyEndPoints() should be called in advance.");
		try {
			Browser browser = BrowserFactory.createBrowser(type);
			browser.getServices().setPromptService(promptIgnoreService);
			browser.setHttpSecurityHandler(securityHandler);
			setupBrowserProxy(browser, endPoints);
			return browser;
		} catch (Exception e) {
			LOGGER.error("Failed to create browser due to ", e);
			throw new NGrinderRuntimeException("Failed to create browser due to ", e);
		}
	}

	/**
	 * Get the supported browser.
	 * 
	 * @return supported browser list
	 */
	public static List<BrowserType> getSupportedBrowser() {
		List<BrowserType> result = newArrayList();
		if (SystemUtils.IS_OS_WINDOWS) {
			addIfAvailable(result, BrowserType.IE);
			if (BrowserType.Mozilla15.isSupported()) {
				addIfAvailable(result, BrowserType.Mozilla15);
			} else if (BrowserType.Mozilla.isSupported()) {
				addIfAvailable(result, BrowserType.Mozilla);
			}
		} else if (SystemUtils.IS_OS_MAC) {
			if (BrowserType.Mozilla15.isSupported()) {
				addIfAvailable(result, BrowserType.Mozilla15);
			} else if (BrowserType.Mozilla.isSupported()) {
				addIfAvailable(result, BrowserType.Mozilla);
			} else {
				addIfAvailable(result, BrowserType.Safari);
			}
		} else {
			if (BrowserType.Mozilla.isSupported()) {
				addIfAvailable(result, BrowserType.Mozilla);
			} else {
				addIfAvailable(result, BrowserType.Mozilla15);
			}
		}
		return result;
	}

	private static void addIfAvailable(List<BrowserType> result, BrowserType each) {
		if (each.isSupported()) {
			result.add(each);
		}
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
	public static void setupBrowserProxy(Browser browser, ProxyEndPointPair endPoints) {
		if (endPoints == null) {
			return;
		}
		ProxyConfig proxyConfig = browser.getServices().getProxyConfig();
		ProxyServer proxyServer = new ProxyServer(endPoints.getHttpEndPoint().getHost(), endPoints.getHttpEndPoint()
						.getPort());
		proxyConfig.setProxy(ServerType.HTTP, proxyServer);
		// Only IE supports HTTPS Recording due to JXBrowser XULRunner problem.
		// This should be fixed soon.
		if (SystemUtils.IS_OS_WINDOWS) {
			ProxyServer sslProxyServer = new ProxyServer(endPoints.getHttpsEndPoint().getHost(), endPoints
							.getHttpsEndPoint().getPort());
			proxyConfig.setProxy(ServerType.SSL, sslProxyServer);
		}
	}
}
