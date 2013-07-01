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

import net.grinder.tools.tcpproxy.ConnectionDetails;
import net.grinder.util.AttributeStringParser;
import net.grinder.util.StringEscaper;
import net.grinder.util.http.URIParser;

/**
 * Factory for {@link ConnectionHandler}s. Modified to create {@link ConnectionHandlerImplEx}
 * instead of {@link ConnectionHandlerImplementation}
 * 
 * @author Philip Aston
 * @author JunHo Yoon (modified by)
 * @since 1.0
 */
public final class ConnectionHandlerFactoryImplEx implements ConnectionHandlerFactory {
	private final HTTPRecordingEx m_httpRecording;
	private final RegularExpressions m_regularExpressions;
	private final URIParser m_uriParser;
	private final AttributeStringParser m_attributeStringParser;
	private final StringEscaper m_postBodyStringEscaper;
	private final FileTypeFilter m_fileTypeFilter;

	/**
	 * Constructor.
	 * 
	 * @param httpRecording
	 *            Common recording state.
	 * @param regularExpressions
	 *            Compiled regular expressions.
	 * @param uriParser
	 *            A URI parser.
	 * @param attributeStringParser
	 *            An AttributeStringParser.
	 * @param postBodyStringEscaper
	 *            A StringCodec used to escape post body strings.
	 * @param fileTypeFilter
	 *            fileTypeFilter
	 */
	public ConnectionHandlerFactoryImplEx(HTTPRecordingEx httpRecording, RegularExpressions regularExpressions,
					URIParser uriParser, AttributeStringParser attributeStringParser,
					StringEscaper postBodyStringEscaper, FileTypeFilter fileTypeFilter) {
		m_httpRecording = httpRecording;
		m_regularExpressions = regularExpressions;
		m_uriParser = uriParser;
		m_attributeStringParser = attributeStringParser;
		m_postBodyStringEscaper = postBodyStringEscaper;
		m_fileTypeFilter = fileTypeFilter;
	}

	/**
	 * Factory method.
	 * 
	 * @param connectionDetails
	 *            Connection details.
	 * @return A new ConnectionHandler.
	 */
	public ConnectionHandler create(ConnectionDetails connectionDetails) {
		return new ConnectionHandlerImplEx(m_httpRecording, m_regularExpressions, m_uriParser, m_attributeStringParser,
						m_postBodyStringEscaper, connectionDetails, m_fileTypeFilter);
	}
}
