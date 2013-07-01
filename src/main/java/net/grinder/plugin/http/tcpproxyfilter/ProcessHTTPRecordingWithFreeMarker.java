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
package net.grinder.plugin.http.tcpproxyfilter;

import static net.grinder.util.CollectionUtils.newHashMap;
import static net.grinder.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.grinder.plugin.http.tcpproxyfilter.options.GenerationOption;
import net.grinder.plugin.http.xml.BaseURIType;
import net.grinder.plugin.http.xml.CommonHeadersType;
import net.grinder.plugin.http.xml.HTTPRecordingType;
import net.grinder.plugin.http.xml.HttpRecordingDocument;
import net.grinder.plugin.http.xml.PageType;
import net.grinder.plugin.http.xml.RequestType;
import net.grinder.plugin.http.xml.TokenType;
import net.grinder.util.Functions;
import net.grinder.util.Language;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * HTTP Recoding Processor with Freemarker template.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class ProcessHTTPRecordingWithFreeMarker implements HTTPRecordingResultProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessHTTPRecordingWithFreeMarker.class);
	private StringTemplateLoader loader = new StringTemplateLoader();
	private Writer m_writer;
	private Set<GenerationOption> options;

	/**
	 * Constructor.
	 */
	public ProcessHTTPRecordingWithFreeMarker() {
	}

	/**
	 * Prepare a freemarker template for the given language.
	 * 
	 * @param lang
	 *            language to be used in the template
	 * @return prepared template
	 */
	private Template prepareTemplate(Language lang) {
		InputStream resourceAsStream = ProcessHTTPRecordingWithFreeMarker.class.getResourceAsStream("/template/"
						+ lang.getTemplateName() + ".ftl");
		try {
			loader.putTemplate(lang.getTemplateName(), IOUtils.toString(resourceAsStream));
			Configuration freemarkerConfig = new Configuration();
			freemarkerConfig.setTemplateLoader(loader);
			DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper();
			objectWrapper.setExposureLevel(DefaultObjectWrapper.EXPOSE_ALL);
			freemarkerConfig.setObjectWrapper(objectWrapper);
			return freemarkerConfig.getTemplate(lang.getTemplateName());

		} catch (IOException e) {
			LOGGER.error("basic template load error", e);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
		}
		return null;
	}

	/**
	 * Produce output.
	 * 
	 * @param result
	 *            The result to process.
	 * @throws IOException
	 *             If an output error occurred.
	 */
	public void process(HttpRecordingDocument result) throws IOException {
		if (m_writer == null) {
			return;
		}
		try {
			checkNotNull(options, "Options should be set before run process(..) method.");
			Language lang = Language.Jython;
			for (GenerationOption each : options) {
				if (StringUtils.equals("Language", each.getGroup())) {
					lang = Language.valueOf(each.name());
					break;
				}
			}
			Template template = prepareTemplate(lang);
			Map<String, Object> map = newHashMap();
			HTTPRecordingType httpRecording = result.getHttpRecording();
			PageType[] pageArray = filterEmptyPage(httpRecording.getPageArray());
			Map<String, BaseURIType> filterUri = filterUri(pageArray,
							buildBaseURITypeMap(httpRecording.getBaseUriArray()));
			Map<String, CommonHeadersType> filterHeader = filterHeader(pageArray,
							buildCommonHeaderArrayTypeMap(httpRecording.getCommonHeadersArray()));
			Map<String, TokenType> buildTokenMap = buildTokenMap(httpRecording.getTokenArray());
			Functions f = lang.createFunctions(filterUri, filterHeader, buildTokenMap, options);
			map.put("rec", httpRecording);
			map.put("uris", filterUri);
			map.put("f", f);
			map.put("commonHeaders", filterHeader);
			map.put("pages", pageArray);
			map.put("meta", httpRecording.getMetadata());
			map.put("token", buildTokenMap);
			template.process(map, m_writer);
		} catch (Exception e) {
			LOGGER.error("Error while fetching template for quick start", e);
			LOGGER.debug("Details :", e);
		}
	}

	private PageType[] filterEmptyPage(PageType[] pageArray) {
		List<PageType> pageTypes = new ArrayList<PageType>();
		for (PageType each : pageArray) {
			if (each.getRequestArray().length != 0) {
				pageTypes.add(each);
			}
		}
		return pageTypes.toArray(new PageType[pageTypes.size()]);
	}

	private Map<String, TokenType> buildTokenMap(TokenType[] tokenTypeArray) {
		Map<String, TokenType> toeknMap = newHashMap();
		for (TokenType each : tokenTypeArray) {
			toeknMap.put(each.getTokenId(), each);
		}
		return toeknMap;
	}

	private Map<String, CommonHeadersType> filterHeader(PageType[] pageArray,
					Map<String, CommonHeadersType> commonHeadersMap) {
		Map<String, CommonHeadersType> baseUriMap = newHashMap();
		for (PageType eachPage : pageArray) {
			for (RequestType eachRequest : eachPage.getRequestArray()) {
				baseUriMap.put(eachRequest.getHeaders().getExtends(),
								commonHeadersMap.get(eachRequest.getHeaders().getExtends()));
			}
		}
		return baseUriMap;
	}

	private Map<String, CommonHeadersType> buildCommonHeaderArrayTypeMap(CommonHeadersType[] baseUriArray) {
		Map<String, CommonHeadersType> baseCommonArrayTypeMap = newHashMap();
		for (CommonHeadersType each : baseUriArray) {
			baseCommonArrayTypeMap.put(each.getHeadersId(), each);
		}
		return baseCommonArrayTypeMap;
	}

	private Map<String, BaseURIType> filterUri(PageType[] pageArray, Map<String, BaseURIType> urlMap) {
		Map<String, BaseURIType> baseUriMap = newHashMap();
		for (PageType eachPage : pageArray) {
			for (RequestType eachRequest : eachPage.getRequestArray()) {
				baseUriMap.put(eachRequest.getUri().getExtends(), urlMap.get(eachRequest.getUri().getExtends()));
			}
		}
		return baseUriMap;
	}

	private Map<String, BaseURIType> buildBaseURITypeMap(BaseURIType[] baseUriArray) {
		Map<String, BaseURIType> baseUriMap = newHashMap();
		for (BaseURIType each : baseUriArray) {
			baseUriMap.put(each.getUriId(), each);
		}
		return baseUriMap;
	}

	/**
	 * Set output writer.
	 * 
	 * @param writer
	 *            writer
	 */
	public void setWriter(Writer writer) {
		this.m_writer = writer;
	}

	/**
	 * Set the recoding options used in the code generation.
	 * 
	 * @param options
	 *            options
	 */
	public void setGenerationOptions(Set<GenerationOption> options) {
		this.options = options;
	}

}
