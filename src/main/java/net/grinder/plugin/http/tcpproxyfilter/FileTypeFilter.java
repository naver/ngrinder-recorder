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

import net.grinder.plugin.http.tcpproxyfilter.options.FileType;
import net.grinder.plugin.http.tcpproxyfilter.options.FileTypeCategory;

/**
 * File Type Filter.
 * 
 * This class is to check the give path and return ture of false depending on the path of the given
 * path is filtered.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public interface FileTypeFilter {

	/**
	 * Check if the given full path is filtered.
	 * 
	 * @param fullPath
	 *            full path
	 * @return true if filtered
	 */
	boolean isFiltered(String fullPath);

	/**
	 * Check if the given content type string (eg: text/python) is filtered.
	 * 
	 * @param contentType
	 *            contentType
	 * @return true if filtered
	 */
	boolean isFilteredContentType(String contentType);

	/**
	 * Add the given {@link FileType} in the filter.
	 * 
	 * @param fileType
	 *            filetype to be filtered.
	 */
	void addFilteredType(FileType fileType);

	/**
	 * Remove the given {@link FileType} from filter.
	 * 
	 * @param fileType
	 *            fileType to be removed.
	 */
	void removeFiltedType(FileType fileType);

	/**
	 * Add all {@link FileType}s belonging to the given {@link FileTypeCategory}.
	 * 
	 * @param fileTypeCategory
	 *            {@link FileTypeCategory} to be added.
	 */
	void addFilteredCategory(FileTypeCategory fileTypeCategory);

	/**
	 * Remove all {@link FileType}s belonging to the given {@link FileTypeCategory}.
	 * 
	 * @param fileTypeCategory
	 *            {@link FileTypeCategory} to be removed.
	 */
	void removeFilteredCategory(FileTypeCategory fileTypeCategory);

	/**
	 * Reset all file filter setting.
	 */
	void reset();
}
