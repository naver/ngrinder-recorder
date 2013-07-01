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
import net.grinder.plugin.http.xml.BodyType;
import net.grinder.plugin.http.xml.CommonHeadersType;
import net.grinder.plugin.http.xml.HeadersType;
import net.grinder.plugin.http.xml.RelativeURIType;
import net.grinder.plugin.http.xml.TokenReferenceType;
import net.grinder.plugin.http.xml.TokenType;

/**
 * Abstract Helper class for Freemaker templates. The instance of the subclass will be passed to
 * Freemaker templates as a name "f"
 * 
 * Freemaker templates can refer this like ${f.escapeQuote(hello)}.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public abstract class Functions {

	/**
	 * Generate token reference string.
	 * 
	 * @param token
	 *            token
	 * @return generated snippet
	 */
	public abstract String generateTokenReference(TokenReferenceType token);

	/**
	 * Generate the header reference string.
	 * 
	 * @param headers
	 *            headers
	 * @return generated snippet
	 */
	public abstract String generateHeaderParameter(HeadersType headers);

	/**
	 * Generate the POST method body string.
	 * 
	 * @param tokens
	 *            token list which POST method will use
	 * @param bodyType
	 *            bodyType
	 * @return generated snippet
	 */
	public abstract String generatePostParameter(TokenReferenceType[] tokens, BodyType bodyType);

	/**
	 * Generate URL construction string.
	 * 
	 * @param relativeURIType
	 *            uri type.
	 * @return generated snippet
	 */
	public abstract String generatePathString(RelativeURIType relativeURIType);

	private final Map<String, CommonHeadersType> headerMap;
	private final Map<String, TokenType> tokenMap;
	private final Set<GenerationOption> genetationOpts;
	private final Map<String, BaseURIType> urlMap;

	/**
	 * Constructor.
	 * 
	 * @param urlMap
	 *            map between url extends ids and real {@link BaseURIType}s
	 * @param headerMap
	 *            map between header extends ids and real {@link CommonHeadersType}s
	 * @param tokenMap
	 *            map between token extends ids and real {@link TokenType}s
	 * @param genetationOpts
	 *            script generation option list
	 */
	public Functions(Map<String, BaseURIType> urlMap, Map<String, CommonHeadersType> headerMap,
					Map<String, TokenType> tokenMap, Set<GenerationOption> genetationOpts) {
		this.urlMap = urlMap;
		this.headerMap = headerMap;
		this.tokenMap = tokenMap;
		this.genetationOpts = genetationOpts;
	}

	/**
	 * Quote the given value.
	 * 
	 * @param value
	 *            value
	 * @return Quote value
	 */
	public abstract String escapeQuote(String value);

	/**
	 * Check if the option is applied.
	 * 
	 * @param optionKey
	 *            option key
	 * @return true if applied.
	 * @see GenerationOption
	 */
	public boolean hasOption(String optionKey) {
		GenerationOption option = GenerationOption.valueOf(optionKey);
		return getGenetationOpts().contains(option);
	}

	/**
	 * Generate URI string from the given {@link BaseURIType} parameter.
	 * 
	 * @param uri
	 *            uri
	 * @return URL string.
	 */
	public String generateURLString(BaseURIType uri) {
		String url = uri.getScheme() + "://" + uri.getHost();
		if (!(uri.getScheme() == BaseURIType.Scheme.HTTP && uri.getPort() == 80)
						&& !(uri.getScheme() == BaseURIType.Scheme.HTTPS && uri.getPort() == 443)) {
			url = url + ":" + uri.getPort();
		}
		return url;
	}

	/**
	 * Wrap the given snippet with comment.
	 * 
	 * @param snippet
	 *            snippet
	 * @return commented snippet
	 */
	public abstract String wrapWithComment(String snippet);

	public Map<String, CommonHeadersType> getHeaderMap() {
		return headerMap;
	}

	public Map<String, TokenType> getTokenMap() {
		return tokenMap;
	}

	public Set<GenerationOption> getGenetationOpts() {
		return genetationOpts;
	}

	public Map<String, BaseURIType> getUrlMap() {
		return urlMap;
	}

}