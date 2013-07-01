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
package net.grinder.plugin.http.tcpproxyfilter.options;

/**
 * Category of the {@link FileType}.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public enum FileTypeCategory {
	/** Images. */
	image("Images(gif, jpg, png)"),
	/** Scripts. */
	script("Javascript(js)"),
	/** Css. */
	css("Css"),
	/** Multimedia files. */
	multimedia("Multimedia(swf, flv, avi...)");

	private final String displayName;

	/**
	 * Constructor.
	 * 
	 * @param displayName
	 *            display name
	 */
	FileTypeCategory(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
