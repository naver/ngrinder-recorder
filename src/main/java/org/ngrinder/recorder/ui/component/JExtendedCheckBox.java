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
package org.ngrinder.recorder.ui.component;

import javax.swing.JCheckBox;

/**
 * Extended {@link JCheckBox} which keep the associated id.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class JExtendedCheckBox extends JCheckBox {
	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 6189092299540784636L;
	private final String id;

	/**
	 * Constuctor.
	 * 
	 * @param id
	 *            id which this CheckBox is binding to.
	 * @param title
	 *            title
	 * @param selected
	 *            true if selected at the first.
	 */
	public JExtendedCheckBox(String id, String title, boolean selected) {
		super(title, selected);
		this.id = id;
	}

	public String getId() {
		return id;
	}
}