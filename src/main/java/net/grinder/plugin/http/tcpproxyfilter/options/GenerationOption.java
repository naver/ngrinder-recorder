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

import static net.grinder.util.CollectionUtils.newArrayList;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Script Generation Options.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public enum GenerationOption {
	/**
	 * Add Sleep Type.
	 */
	AddSleep("Add sleep time"),

	/**
	 * Follow Redirection.
	 */
	FollowRedirection("Follow Redirection"),

	/**
	 * Jython
	 */
	Jython("Jython", "Language"),

	/**
	 * Groovy
	 */
	Groovy("Groovy", "Language");

	private final String displayName;
	private final String group;

	/**
	 * Constructor.
	 * 
	 * @param displayName
	 *            display name
	 */
	GenerationOption(String displayName) {
		this(displayName, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param displayName
	 *            display name
	 */
	GenerationOption(String displayName, String group) {
		this.displayName = displayName;
		this.group = group;

	}

	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Get options which belonging to the given group.
	 * 
	 * @param group
	 *            group
	 * @return option list
	 */
	public static List<GenerationOption> getOptions(String group) {
		List<GenerationOption> result = newArrayList();
		for (GenerationOption each : values()) {
			if (StringUtils.equals(group, each.getGroup())) {
				result.add(each);
			}
		}
		return result;
	}

	public String getGroup() {
		return group;
	}
}
