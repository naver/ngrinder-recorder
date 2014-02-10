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
package org.ngrinder.recorder.infra;

import static net.grinder.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.recorder.proxy.ScriptRecorderProxy;
import org.ngrinder.recorder.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Spring component which is responsible to get the nGrinder config which is stored
 * ${NGRINDER_RECORDER_HOME}.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class RecorderConfig {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder_recorder";

	private static final Logger LOGGER = LoggerFactory.getLogger(RecorderConfig.class);
	private RecorderHome home;
	private PropertiesWrapper internalProperties;
	private PropertiesWrapper recorderProperties;

	/**
	 * Initialize.
	 * 
	 * @return initialized AgentCongig
	 */
	public RecorderConfig init() {
		home = resolveHome();
		copyDefaultConfigurationFiles();
		loadRecorderProperties();
		loadInternalProperties();
		configureLogging();
		return this;
	}

	private void configureLogging() {
		final Context context = (Context) LoggerFactory.getILoggerFactory();
		final JoranConfigurator configurator = new JoranConfigurator();
		context.putProperty("LOG_DIRECTORY", home.getDirectory().getAbsolutePath());
		configurator.setContext(context);
		try {
			configurator.doConfigure(ScriptRecorderProxy.class.getResource(isTestMode() ? "/logback-recorder-debug.xml"
							: "/logback-recorder.xml"));
		} catch (JoranException e) {
			LOGGER.error("Error during running configureLogging()");
			LOGGER.debug("Details:{}", e.getMessage(), e);
			System.exit(-1);
		}
	}

	private void loadInternalProperties() {
		InputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = RecorderConfig.class.getResourceAsStream("/internal.properties");
			properties.load(inputStream);
			internalProperties = new PropertiesWrapper(properties);
		} catch (IOException e) {
			LOGGER.error("Error while load internal.properties", e);
			internalProperties = new PropertiesWrapper(properties);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private void copyDefaultConfigurationFiles() {
		checkNotNull(home);
		InputStream agentConfIO = loadFromClassPath("recorder.conf");
		if (agentConfIO == null) {
			throw processException("Error while loading agent.conf file");
		}
		home.copyFileTo(agentConfIO, new File("recorder.conf"), false);
		IOUtils.closeQuietly(agentConfIO);
	}

	/**
	 * Load path file from class path.
	 * 
	 * @param path
	 *            path in the classpath
	 * @return {@link InputStream}
	 */
	public InputStream loadFromClassPath(String path) {
		return RecorderConfig.class.getClassLoader().getResourceAsStream(path);
	}

	private void loadRecorderProperties() {
		checkNotNull(home);
		Properties properties = home.getProperties("recorder.conf");
		properties.put("NGRINDER_RECORDER_HOME", home.getDirectory().getAbsolutePath());
		recorderProperties = new PropertiesWrapper(properties);
	}

	/**
	 * resolve NGrinder agent home path.
	 * 
	 * @return resolved {@link RecorderHome}
	 */
	protected RecorderHome resolveHome() {
		String userHomeFromEnv = System.getenv("NGRINDER_RECORDER_HOME");
		LOGGER.info("    System Environment:  NGRINDER_RECORDER_HOME={}", StringUtils.trimToEmpty(userHomeFromEnv));

		String userHomeFromProperty = System.getProperty("ngrinder.recorder.home");
		LOGGER.info("    Java Sytem Property:  ngrinder.recorder.home={}",
						StringUtils.trimToEmpty(userHomeFromProperty));

		if (StringUtils.isNotEmpty(userHomeFromEnv) && !StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			LOGGER.warn("The path to ngrinder recorder home is ambiguous:");
			LOGGER.warn("    '{}' is accepted.", userHomeFromProperty);
		}

		String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		if (StringUtils.isEmpty(userHome)) {
			userHome = System.getProperty("user.home") + File.separator + NGRINDER_DEFAULT_FOLDER;
		}
		LOGGER.info("Finally NGRINDER_RECORDER_HOME is resolved as {}", userHome);
		File homeDirectory = new File(userHome);
		try {
			homeDirectory.mkdirs();
			if (!homeDirectory.canWrite()) {
				throw processException("home directory " + userHome + " is not writable.");
			}
		} catch (Exception e) {
			throw processException("Error while resolve the home directory.", e);
		}
		return new RecorderHome(homeDirectory);
	}

	/**
	 * if there is testmode property in system.properties.. return true
	 * 
	 * @return true is test mode
	 */
	public boolean isTestMode() {
		return BooleanUtils.toBoolean(getAgentProperty("testmode", "false"));
	}

	String getAgentProperty(String key, String defaultValue) {
		return getRecorderProperties().getProperty(key, defaultValue);
	}

	public RecorderHome getHome() {
		return this.home;
	}

	/**
	 * Get agent properties.
	 * 
	 * @return agent properties
	 */
	public PropertiesWrapper getRecorderProperties() {
		checkNotNull(recorderProperties);
		return recorderProperties;
	}

	/**
	 * Get property string.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value
	 * 
	 * @return string value for given key. If not available, return default value.
	 */
	public String getProperty(String key, String defaultValue) {
		return getRecorderProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get property int.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value
	 * 
	 * @return int value for given key. If not available, return default value.
	 */
	public int getPropertyInt(String key, int defaultValue) {
		return getRecorderProperties().getPropertyInt(key, defaultValue);
	}

	/**
	 * Get nGrinder internal properties.
	 * 
	 * @param key
	 *            key
	 * @param defaultValue
	 *            default value
	 * @return value
	 */
	public String getInternalProperty(String key, String defaultValue) {
		return internalProperties.getProperty(key, defaultValue);
	}

	public File getCurrentDirectory() {
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Get property boolean.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value
	 * 
	 * @return boolean value for given key. If not available, return default value.
	 */
	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		return getRecorderProperties().getPropertyBoolean(key, defaultValue);
	}

}
