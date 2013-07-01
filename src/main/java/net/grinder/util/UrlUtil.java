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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.grinder.plugin.http.xml.BaseURIType;

import org.apache.commons.lang.StringUtils;

/**
 * Set of URL related methods.
 * 
 * @author JunHo Yoon
 */
public abstract class UrlUtil {
	/**
	 * Normalized URL to java recognizable form. It replace ":", "-" ... to "_".
	 * 
	 * @param scheme
	 *            schema
	 * @param host
	 *            host name
	 * @return normalized URL
	 */
	public static String toNormalize(BaseURIType.Scheme.Enum scheme, String host) {
		if (scheme.toString() != "http") {
			return StringUtils.replaceChars(scheme.toString() + "_" + host, "-.", "__");
		} else {
			return StringUtils.replaceChars(host, "-.", "__");
		}
	}

	/**
	 * Convert the given file to string formated URL.
	 * 
	 * @param file
	 *            file
	 * @return URL
	 */
	public static URL toURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
