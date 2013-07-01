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
package net.grinder.util;

import java.util.Map;
import java.util.Set;

import net.grinder.plugin.http.tcpproxyfilter.options.GenerationOption;
import net.grinder.plugin.http.xml.BaseURIType;
import net.grinder.plugin.http.xml.CommonHeadersType;
import net.grinder.plugin.http.xml.TokenType;

/**
 * Language used in the code generation.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public enum Language {
	/** Jython Language. */
	Jython("jython_template", "text/python") {
		@Override
		public Functions createFunctions(Map<String, BaseURIType> urlMap, Map<String, CommonHeadersType> headerMap,
						Map<String, TokenType> tokenMap, Set<GenerationOption> options) {
			return new JythonFunctions(urlMap, headerMap, tokenMap, options);
		}
	},
	/** Groovy Language. */
	Groovy("groovy_template", "text/groovy") {
		@Override
		public Functions createFunctions(Map<String, BaseURIType> urlMap, Map<String, CommonHeadersType> headerMap,
						Map<String, TokenType> tokenMap, Set<GenerationOption> options) {
			return new GroovyFunctions(urlMap, headerMap, tokenMap, options);
		}
	};
	private final String templateName;
	private final String contentType;

	/**
	 * Constructor.
	 * 
	 * @param templateName
	 *            freemarker template name
	 * @param contentType
	 *            content type
	 */
	Language(String templateName, String contentType) {
		this.templateName = templateName;
		this.contentType = contentType;
	}

	/**
	 * Create the code generation helper class.
	 * 
	 * @param urlMap
	 *            urlMap
	 * @param headerMap
	 *            headerMap
	 * @param tokenMap
	 *            tokenMap
	 * @param options
	 *            code generation options
	 * @return the code generation helper class.
	 */
	public abstract Functions createFunctions(Map<String, BaseURIType> urlMap,
					Map<String, CommonHeadersType> headerMap, Map<String, TokenType> tokenMap,
					Set<GenerationOption> options);

	public String getTemplateName() {
		return templateName;
	}

	public String getContentType() {
		return contentType;
	}

}
