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
package org.ngrinder.recorder.proxy;

import static net.grinder.util.Preconditions.checkNotNull;
import static net.grinder.util.TypeUtil.cast;
import static org.ngrinder.recorder.util.NetworkUtil.getAvailablePort;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.grinder.plugin.http.tcpproxyfilter.ConnectedHostHTTPFilterEventListener;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionAwareNullRequestFilter;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionCache;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionFilter;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionFilterImpl;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionHandlerFactoryImplEx;
import net.grinder.plugin.http.tcpproxyfilter.FileTypeFilterImpl;
import net.grinder.plugin.http.tcpproxyfilter.HTTPRecordingImplEx;
import net.grinder.plugin.http.tcpproxyfilter.HTTPRequestFilter;
import net.grinder.plugin.http.tcpproxyfilter.HTTPResponseFilter;
import net.grinder.plugin.http.tcpproxyfilter.ParametersFromProperties;
import net.grinder.plugin.http.tcpproxyfilter.ProcessHTTPRecordingWithFreeMarker;
import net.grinder.plugin.http.tcpproxyfilter.RegularExpressionsImplementation;
import net.grinder.plugin.http.tcpproxyfilter.SwitchableRequestTcpProxyFilter;
import net.grinder.plugin.http.tcpproxyfilter.SwitchableResponseTcpProxyFilter;
import net.grinder.plugin.http.tcpproxyfilter.SwitchableTcpProxyFilter;
import net.grinder.plugin.http.tcpproxyfilter.options.FileTypeCategory;
import net.grinder.plugin.http.tcpproxyfilter.options.GenerationOption;
import net.grinder.tools.tcpproxy.AbstractTCPProxyEngine;
import net.grinder.tools.tcpproxy.CommentSourceImplementation;
import net.grinder.tools.tcpproxy.CompositeFilter;
import net.grinder.tools.tcpproxy.EndPoint;
import net.grinder.tools.tcpproxy.HTTPProxyTCPProxyEngineEx;
import net.grinder.tools.tcpproxy.NullFilter;
import net.grinder.tools.tcpproxy.TCPProxyFilter;
import net.grinder.tools.tcpproxy.TCPProxySSLSocketFactory;
import net.grinder.tools.tcpproxy.TCPProxySSLSocketFactoryImplementation;
import net.grinder.tools.tcpproxy.UpdatableCommentSource;
import net.grinder.util.AttributeStringParserImplementation;
import net.grinder.util.Language;
import net.grinder.util.Pair;
import net.grinder.util.SimpleStringEscaper;
import net.grinder.util.http.URIParserImplementation;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.recorder.event.MessageBus;
import org.ngrinder.recorder.event.MessageBusConnection;
import org.ngrinder.recorder.event.Topics;
import org.ngrinder.recorder.infra.NGrinderRuntimeException;
import org.ngrinder.recorder.infra.RecorderConfig;
import org.ngrinder.recorder.util.NetworkUtil;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxy control class.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ScriptRecorderProxy {
	private static final Logger LOG = LoggerFactory.getLogger(ScriptRecorderProxy.class);

	private AbstractTCPProxyEngine m_httpProxyEngine;

	private DefaultPicoContainer m_filterContainer;

	private final RecorderConfig recorderConfig;

	/**
	 * Constructor.
	 * 
	 * @param recorderConfig
	 *            config.
	 */
	public ScriptRecorderProxy(RecorderConfig recorderConfig) {
		this.recorderConfig = recorderConfig;
	}

	/**
	 * Stop proxy engine and container, release its related resource to finish generating output
	 * script files.
	 */
	public synchronized void stopProxy() {
		m_httpProxyEngine.stop();
		if (m_filterContainer.getLifecycleState().isStarted()) {
			m_filterContainer.stop();
			m_filterContainer.dispose();
		}
	}

	/**
	 * Start proxy.
	 * 
	 * This tries to create a proxy on the given ports. However if it's failed, it creates a proxy
	 * with the randomly selected port.
	 * 
	 * @param defaultPort
	 *            default port which will be used to try to make a socket.
	 * @return real setup proxy info.
	 */
	public ProxyEndPointPair startProxy(int defaultPort) {
		// #recorder.additional.headers=header names to be recorded
		System.setProperty("DHTTPPlugin.additionalHeaders",
						recorderConfig.getProperty("recorder.additional.headers", ""));

		LOG.info("Initilize proxy..");
		m_filterContainer = new DefaultPicoContainer(new Caching());
		m_filterContainer.addComponent(LOG);
		UpdatableCommentSource commentSource = new CommentSourceImplementation();
		m_filterContainer.addComponent(commentSource);

		final FilterChain requestFilterChain = new FilterChain();
		final FilterChain responseFilterChain = new FilterChain();
		requestFilterChain.add(SwitchableRequestTcpProxyFilter.class);
		responseFilterChain.add(SwitchableResponseTcpProxyFilter.class);
		m_filterContainer.addComponent(SwitchableRequestTcpProxyFilter.class);
		m_filterContainer.addComponent(SwitchableResponseTcpProxyFilter.class);
		m_filterContainer.addComponent(NullFilter.class);
		m_filterContainer.addComponent(ConnectionAwareNullRequestFilter.class);
		m_filterContainer.addComponent(FileTypeFilterImpl.class);
		m_filterContainer.as(Characteristics.USE_NAMES).addComponent(HTTPResponseFilter.class);
		m_filterContainer.as(Characteristics.USE_NAMES).addComponent(HTTPRequestFilter.class);
		m_filterContainer.addComponent(SwitchableTcpProxyFilter.class);
		m_filterContainer.addComponent(AttributeStringParserImplementation.class);
		m_filterContainer.addComponent("eventListener", ConnectedHostHTTPFilterEventListener.class);
		m_filterContainer.addComponent("connectionCache", ConnectionCache.class);
		m_filterContainer.addComponent(ConnectionHandlerFactoryImplEx.class);
		m_filterContainer.addComponent(ParametersFromProperties.class);
		m_filterContainer.addComponent(HTTPRecordingImplEx.class);
		m_filterContainer.addComponent(ProcessHTTPRecordingWithFreeMarker.class);
		m_filterContainer.addComponent(ConnectionFilterImpl.class);
		m_filterContainer.addComponent(RegularExpressionsImplementation.class);
		m_filterContainer.addComponent(URIParserImplementation.class);
		m_filterContainer.addComponent(SimpleStringEscaper.class);
		m_filterContainer.addComponent(recorderConfig);
		m_filterContainer.start();
		LOG.info("Pico container initiated..");

		final SwitchableResponseTcpProxyFilter switchableResponseFilter = m_filterContainer
						.getComponent(SwitchableResponseTcpProxyFilter.class);
		final SwitchableRequestTcpProxyFilter switchableRequestFilter = m_filterContainer
						.getComponent(SwitchableRequestTcpProxyFilter.class);
		final HTTPResponseFilter httpResponseFilter = m_filterContainer.getComponent(HTTPResponseFilter.class);
		final HTTPRequestFilter httpRequestFilter = m_filterContainer.getComponent(HTTPRequestFilter.class);
		final NullFilter nullFilter = m_filterContainer.getComponent(NullFilter.class);
		final ConnectionAwareNullRequestFilter connectionAwareNullRequestFilter = m_filterContainer
						.getComponent(ConnectionAwareNullRequestFilter.class);
		final FileTypeFilterImpl fileTypeFilter = m_filterContainer.getComponent(FileTypeFilterImpl.class);
		final HTTPRecordingImplEx httpRecording = m_filterContainer.getComponent(HTTPRecordingImplEx.class);
		final ConnectedHostHTTPFilterEventListener connectionCache = m_filterContainer
						.getComponent(ConnectedHostHTTPFilterEventListener.class);
		final MessageBus messageBus = MessageBus.getInstance();
		MessageBusConnection connect = messageBus.connect();
		LOG.info("Register event handler..");
		connect.subscribe(Topics.START_RECORDING, new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent evt) {
				initFileTypeFilter(fileTypeFilter, (List<FileTypeCategory>) evt.getNewValue());
				switchableResponseFilter.setTcpProxyFilter(httpResponseFilter);
				switchableRequestFilter.setTcpProxyFilter(httpRequestFilter);
			}
		});
		connect.subscribe(Topics.STOP_RECORDING, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				switchableResponseFilter.setTcpProxyFilter(nullFilter);
				switchableRequestFilter.setTcpProxyFilter(connectionAwareNullRequestFilter);
				Pair<List<FileTypeCategory>, Set<GenerationOption>> pair = cast(evt.getNewValue());
				initFileTypeFilter(fileTypeFilter, pair.getFirst());
				ProcessHTTPRecordingWithFreeMarker httpOutput = m_filterContainer
								.getComponent(ProcessHTTPRecordingWithFreeMarker.class);
				HTTPRecordingImplEx recoding = m_filterContainer.getComponent(HTTPRecordingImplEx.class);
				StringWriter writer = new StringWriter();
				Set<GenerationOption> second = (Set<GenerationOption>) pair.getSecond();
				httpOutput.setGenerationOptions(second);
				httpOutput.setWriter(writer);
				recoding.generate();
				Language lang = Language.Jython;
				for (GenerationOption each : second) {
					if (StringUtils.equals("Language", each.getGroup())) {
						lang = Language.valueOf(each.name());
						break;
					}
				}
				Pair<Language, String> result = Pair.of(lang, writer.toString());
				messageBus.getPublisher(Topics.SHOW_SCRIPT).propertyChange(
								new PropertyChangeEvent(this, "Show Script", null, result));
			}

		});

		connect.subscribe(Topics.START_USER_PAGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				httpRecording.setNewPageRequested();
			}
		});

		connect.subscribe(Topics.RESET, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				connectionCache.dispose();
				httpRecording.reset();
			}
		});
		LOG.info("Try to start proxy..");
		InetAddress localHostAddress = NetworkUtil.getLocalHostAddress();
		int proxyPort = getAvailablePort(localHostAddress, defaultPort);

		LOG.info("Recorder port is HTTP:{} / HTTPS:{}", proxyPort, proxyPort);
		final EndPoint localHttpEndPoint = new EndPoint(localHostAddress.getHostAddress(), proxyPort);
		final EndPoint localHttpsEndPoint = new EndPoint(localHostAddress.getHostAddress(), proxyPort);
		ProxyEndPointPair proxyEndPointPair = new ProxyEndPointPair(localHttpEndPoint, localHttpsEndPoint);
		final TCPProxyFilter requestFilter = requestFilterChain.resolveFilter();
		final TCPProxyFilter responseFilter = responseFilterChain.resolveFilter();
		try {
			TCPProxySSLSocketFactory sslSocketFactory = ceateTCPProxySSlSocketFactory();
			m_httpProxyEngine = new HTTPProxyTCPProxyEngineEx(sslSocketFactory, requestFilter, responseFilter, LOG,
							localHttpEndPoint, null, null);
			Thread httpProxyThread = new Thread(m_httpProxyEngine);
			httpProxyThread.start();
			LOG.info("Finish proxy initailization.");
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Failed to start the tcp proxy engine.", e);
		}
		return proxyEndPointPair;
	}

	/**
	 * Create TCPProxySSLSocketFactory.
	 * 
	 * @return configured {@link TCPProxySSLSocketFactory}
	 */
	protected TCPProxySSLSocketFactory ceateTCPProxySSlSocketFactory() {
		checkNotNull(recorderConfig, "setRecorderConfig should called in advance");
		File keyStoreFile = recorderConfig.getHome().getFile("keystore");
		String keyStorePassword = recorderConfig.getProperty("keystore.password", "");
		String keyStoreType = recorderConfig.getProperty("keystore.type", null);
		try {
			if (keyStoreFile.exists()) {
				if (StringUtils.isNotEmpty(keyStorePassword)) {
					LOG.info("user provided keystore {} is used", keyStoreFile.getAbsolutePath());
					return new TCPProxySSLSocketFactoryImplementation(keyStoreFile, keyStorePassword.toCharArray(),
									keyStoreType);
				}
				LOG.info("user provides keystore file but not provide keystore.password in recorder.conf is not set.");
			}
		} catch (Exception e) {
			LOG.info("exception occurs while configuring TCPProxySocketFactory using {}, {}.", keyStoreFile,
							keyStorePassword);
		}
		LOG.info("use the default keystore.");
		try {
			return new TCPProxySSLSocketFactoryImplementation();
		} catch (Exception e) {
			throw new RuntimeException("error occurs while configuring default TCPProxySocketFactory", e);
		}
	}

	private void initFileTypeFilter(final FileTypeFilterImpl fileTypeFilter, List<FileTypeCategory> categories) {
		fileTypeFilter.reset();
		for (FileTypeCategory each : categories) {
			fileTypeFilter.addFilteredCategory(each);
		}
	}

	/**
	 * Get connection filter.
	 * 
	 * @return connection filter
	 */
	public ConnectionFilter getConnectionFilter() {
		return m_filterContainer.getComponent(ConnectionFilter.class);
	}

	private class FilterChain {
		private final List<Class<? extends TCPProxyFilter>> filter = new ArrayList<Class<? extends TCPProxyFilter>>();

		public FilterChain() {
		}

		public <T extends TCPProxyFilter> void add(Class<T> theClass) {
			filter.add(theClass);
		}

		public TCPProxyFilter resolveFilter() {
			final CompositeFilter result = new CompositeFilter();
			for (Class<? extends TCPProxyFilter> each : filter) {
				result.add(m_filterContainer.getComponent(each));
			}
			return result;
		}

	}

}