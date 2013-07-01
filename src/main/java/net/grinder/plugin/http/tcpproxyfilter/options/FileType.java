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
 * File types.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public enum FileType {
	// JPEG
	jpg(new String[] { "jpg", "jpeg" }, new String[] { "image/jpeg", "image/pjpeg" }, FileTypeCategory.image),
	// PNG
	png(new String[] { "png" }, new String[] { "image/png" }, FileTypeCategory.image),
	// GIF
	gif(new String[] { "gif" }, new String[] { "image/gif" }, FileTypeCategory.image),
	// ICON
	icon(new String[] { "ico" }, new String[] { "image/vnd.microsoft.icon" }, FileTypeCategory.image),
	// SWF
	swf(new String[] { "swf" }, new String[] { "application/x-shockwave-flash" }, FileTypeCategory.multimedia),
	// MOV
	mov(new String[] { "mov" }, new String[] { "video/" }, FileTypeCategory.multimedia),
	// Audio
	audio(new String[] { "mp4" }, new String[] { "audio/" }, FileTypeCategory.multimedia),
	// JavaScript
	javascript(new String[] { "js" }, new String[] { "text/javascript" }, FileTypeCategory.script),
	// CSS
	css(new String[] { "css" }, new String[] { "text/css" }, FileTypeCategory.css);

	private final String[] extensions;
	private final FileTypeCategory category;
	private final String[] contentTypes;

	/**
	 * Constructor.
	 * 
	 * @param extensions
	 *            extension string list
	 * @param contentTypes
	 *            context type list
	 * @param category
	 *            category of this type
	 */
	FileType(String[] extensions, String[] contentTypes, FileTypeCategory category) {
		this.extensions = extensions;
		this.contentTypes = contentTypes;
		this.category = category;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public FileTypeCategory getCategory() {
		return category;
	}

	public String[] getContentTypes() {
		return contentTypes;
	}

	/**
	 * Check if the given extension matches the current {@link FileType} instance.
	 * 
	 * @param extension
	 *            extension
	 * @return true if it's the extension
	 */
	public boolean isTypeOfExtension(String extension) {
		for (String each : extensions) {
			if (each.equals(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the given content type matches the current {@link FileType} instance.
	 * 
	 * @param contentType
	 *            content type
	 * @return true if it's the contentType of current type
	 */
	public boolean isTypeOfContentType(String contentType) {
		for (String each : contentTypes) {
			if (contentType.contains(each)) {
				return true;
			}
		}
		return false;
	}
}
