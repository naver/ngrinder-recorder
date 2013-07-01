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

import static net.grinder.util.NoOp.noOp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import net.grinder.plugin.http.tcpproxyfilter.XSLTHelper;
import net.grinder.plugin.http.tcpproxyfilter.options.GenerationOption;
import net.grinder.plugin.http.xml.AuthorizationHeaderType;
import net.grinder.plugin.http.xml.BaseURIType;
import net.grinder.plugin.http.xml.BodyType;
import net.grinder.plugin.http.xml.CommonHeadersType;
import net.grinder.plugin.http.xml.FormFieldType;
import net.grinder.plugin.http.xml.HeaderType;
import net.grinder.plugin.http.xml.HeadersType;
import net.grinder.plugin.http.xml.ParsedURIPartType;
import net.grinder.plugin.http.xml.RelativeURIType;
import net.grinder.plugin.http.xml.TokenReferenceType;
import net.grinder.plugin.http.xml.TokenType;
import net.grinder.plugin.http.xml.impl.ParsedURIPartTypeImpl;

import org.apache.commons.lang.StringUtils;
import org.python.core.PyString;

/**
 * {@link Functions} implementation for groovy language.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class GroovyFunctions extends Functions {
	/**
	 * Constructor.
	 * 
	 * @param urlMap
	 *            urlMap
	 * @param headerMap
	 *            headerMap
	 * @param tokenMap
	 *            tokenMap
	 * @param genetationOpts
	 *            genetationOpts
	 */
	public GroovyFunctions(Map<String, BaseURIType> urlMap, Map<String, CommonHeadersType> headerMap,
					Map<String, TokenType> tokenMap, Set<GenerationOption> genetationOpts) {
		super(urlMap, headerMap, tokenMap, genetationOpts);
	}

	@Override
	public String generatePathString(RelativeURIType relativeURIType) {
		ParsedURIPartType path = relativeURIType.getPath();
		ParsedURIPartType queryString = relativeURIType.getQueryString();
		StringBuilder sourceBuilder = new StringBuilder("\"").append(((ParsedURIPartTypeImpl) path).getStringValue());
		TokenReferenceType[] tokenReferenceArray = queryString == null ? new TokenReferenceType[0] : queryString
						.getTokenReferenceArray();
		int index = 0;
		int length = tokenReferenceArray.length;
		for (TokenReferenceType tokenReference : tokenReferenceArray) {
			if (index == 0) {
				sourceBuilder.append("?");
			}
			sourceBuilder.append(getTokenMap().get(tokenReference.getTokenId()).getName()).append("=$")
							.append(tokenReference.getTokenId());
			if (index != length - 1) {
				sourceBuilder.append("&");
			}
			index++;
		}
		sourceBuilder.append("\"");
		return sourceBuilder.toString();
	}

	@Override
	public String generateHeaderParameter(HeadersType headers) {
		StringBuilder headerBuilder = new StringBuilder();

		HeaderType[] headerArray = headers.getHeaderArray();
		int index = 0;
		int length = headerArray.length;
		if (length == 0) {
			if ("defaultHeaders".equals(headers.getExtends())) {
				return "null";
			} else {
				return headers.getExtends();
			}
		}
		if (headers.getAuthorizationArray().length != 0) {

			AuthorizationHeaderType authorizationHeaderType = headers.getAuthorizationArray()[0];
			headerBuilder.append("httpUtilities.basicAuthorizationHeader(")
							.append(escapeQuote(authorizationHeaderType.getBasic().getUserid())).append(", ")
							.append(escapeQuote(authorizationHeaderType.getBasic().getPassword())).append(")");
			if (headerArray.length != 0) {
				headerBuilder.append(" + \n\t\t\t\t");
			} else {
				return headerBuilder.toString();
			}

		}
		for (HeaderType headerType : headerArray) {
			if (index == 0) {

				if ("defaultHeaders".equals(headers.getExtends()) || headers.getExtends() == null) {
					headerBuilder.append("[\n");
				} else {
					headerBuilder.append(headers.getExtends()).append(" + [\n");
				}
			}
			headerBuilder.append("\t\t\t\tnew NVPair(").append(escapeQuote(headerType.getName())).append(", ")
							.append(escapeQuote(headerType.getValue())).append(")");

			if (index == length - 1) {
				headerBuilder.append("\n\t\t\t] as NVPair[]");
			} else {
				headerBuilder.append(",\n");
			}
			index++;
		}
		return headerBuilder.toString();

	}

	@Override
	public String generateTokenReference(TokenReferenceType token) {
		StringBuilder tokenBuilder = new StringBuilder();
		tokenBuilder.append(token.getTokenId()).append(" = ");
		String source = token.getSource();
		if ("RESPONSE_BODY_URI_QUERY_STRING".equals(source) || "RESPONSE_BODY_URI_PATH_PARAMETER".equals(source)) {
			tokenBuilder.append("httpUtilities.valueFromBodyURI(")
							.append(escapeQuote(getTokenMap().get(token.getTokenId()).getName())).append(")");
			tokenBuilder.append("   // ").append(token.getNewValue());
		} else if ("RESPONSE_BODY_HIDDEN_INPUT".equals(source)) {
			tokenBuilder.append("httpUtilities.valueFromHiddenInput(")
							.append(escapeQuote(getTokenMap().get(token.getTokenId()).getName())).append(")");
			tokenBuilder.append("   // ").append(token.getNewValue());
		} else if ("RESPONSE_LOCATION_HEADER_QUERY_STRING".equals(source)
						|| "RESPONSE_LOCATION_HEADER_PATH_PARAMETER".equals(source)) {
			tokenBuilder.append("httpUtilities.valueFromLocationURI(")
							.append(escapeQuote(getTokenMap().get(token.getTokenId()).getName())).append(")");
			tokenBuilder.append("   // ").append(token.getNewValue());
		} else {
			tokenBuilder.append(escapeQuote(token.getNewValue())).append("");
		}
		return tokenBuilder.toString();
	}

	/**
	 * Convert a bytes array to groovy recognized string.
	 * 
	 * 
	 * @param bytes
	 *            bytes The binary data.
	 * @return snippet.
	 */
	public static String byteToPython(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		result.append('"');
		if (bytes.length > 0) {
			for (int i = 0; i < bytes.length; ++i) {
				if (i > 0 && i % 16 == 0) {
					result.append('"');
					result.append("\n\t\t\t\t");
					result.append('"');
				}

				final int b = bytes[i] < 0 ? 0x100 + bytes[i] : bytes[i];

				if (b <= 0xF) {
					result.append("\\x0");
				} else {
					result.append("\\x");
				}

				result.append(Integer.toHexString(b).toUpperCase());
			}
		}
		result.append('"');
		return result.toString();
	}

	@Override
	public String generatePostParameter(TokenReferenceType[] tokens, BodyType body) {
		if (body != null && body.getBinary() != null) {
			return byteToPython(body.getBinary());
		}
		if (tokens == null
						&& (body == null || body.getForm() == null || body.getForm().getFormFieldArray().length == 0)) {
			return "[] as NVPair[]";
		}

		StringBuilder postBuilder = new StringBuilder("[\n");
		if (tokens != null) {
			for (TokenReferenceType each : tokens) {
				postBuilder.append("\t\t\t\t\tnew NVPair(")
								.append(escapeQuote(getTokenMap().get(each.getTokenId()).getName())).append(", ")
								.append(escapeQuote(each.getTokenId())).append("),\n");

			}
		}
		if (body.getForm() != null) {
			if (body.getForm().getFormFieldArray().length != 0) {
				for (FormFieldType formFieldType : body.getForm().getFormFieldArray()) {
					postBuilder.append("\t\t\t\t\tnew NVPair(").append(escapeQuote(formFieldType.getName()))
									.append(", ").append(escapeQuote(formFieldType.getValue())).append("),\n");
				}
			}

			if (body.getForm().getTokenReferenceArray().length != 0) {
				for (TokenReferenceType each : body.getForm().getTokenReferenceArray()) {
					postBuilder.append("\t\t\t\t\tnew NVPair(")
									.append(escapeQuote(getTokenMap().get(each.getTokenId()).getName())).append(", ")
									.append(escapeQuote(each.getTokenId())).append("),\n");
				}
			}
		}
		postBuilder.append("\t\t\t] as NVPair[]");
		return postBuilder.toString();
	}

	@Override
	public String escapeQuote(String value) {
		String unicodeEscape = PyString.encode_UnicodeEscape(value, true);
		if (unicodeEscape.contains("\\x")) {
			try {
				value = URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				noOp();
			}
		}
		return XSLTHelper.quoteForClojure(value);
	}

	@Override
	public String wrapWithComment(String message) {
		if (message == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (String eachLine : StringUtils.split(message, '\n')) {
			builder.append("// ").append(eachLine).append("\n");
		}
		return builder.toString();
	}
}
