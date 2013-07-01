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

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import net.grinder.plugin.http.tcpproxyfilter.options.FileType;
import net.grinder.plugin.http.tcpproxyfilter.options.FileTypeCategory;

/**
 * @author JunHo Yoon
 * @since 1.0
 */
public class FileTypeFilterImpl implements FileTypeFilter {
	/**
	 * Constructor.
	 */
	public FileTypeFilterImpl() {
		addFilteredCategory(FileTypeCategory.image);
	}

	private Set<FileType> filteredType = new ConcurrentSkipListSet<FileType>();

	@Override
	public boolean isFiltered(String fullPath) {
		int lastIndexOf = fullPath.lastIndexOf('?');
		if (lastIndexOf != -1) {
			fullPath = fullPath.substring(0, lastIndexOf);
		}
		String extension = FilenameUtils.getExtension(fullPath);

		if (StringUtils.isEmpty(extension)) {
			return false;
		}
		for (FileType each : filteredType) {
			if (each.isTypeOfExtension(extension)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void addFilteredType(FileType fileType) {
		filteredType.add(fileType);
	}

	@Override
	public void removeFiltedType(FileType fileType) {
		filteredType.remove(fileType);
	}

	@Override
	public void addFilteredCategory(FileTypeCategory fileTypeCategory) {
		for (FileType each : FileType.values()) {
			if (each.getCategory() == fileTypeCategory) {
				filteredType.add(each);
			}
		}
	}

	@Override
	public void removeFilteredCategory(FileTypeCategory fileTypeCategory) {
		for (FileType each : FileType.values()) {
			if (each.getCategory() == fileTypeCategory) {
				filteredType.remove(each);
			}
		}
	}

	@Override
	public void reset() {
		filteredType.clear();
	}

	@Override
	public boolean isFilteredContentType(String contentType) {
		if (StringUtils.isBlank(contentType)) {
			return false;
		}
		for (FileType each : filteredType) {
			if (each.isTypeOfContentType(contentType)) {
				return true;
			}
		}
		return false;
	}

}
