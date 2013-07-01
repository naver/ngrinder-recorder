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
package org.ngrinder.recorder.browser;

import java.util.Set;

import com.teamdev.jxbrowser.security.HttpSecurityAction;
import com.teamdev.jxbrowser.security.HttpSecurityHandler;
import com.teamdev.jxbrowser.security.SecurityProblem;

/**
 * {@link HttpSecurityHandler} instance which will ignore all the security problems the browser
 * emits.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class SilentSecurityHandler implements HttpSecurityHandler {
	@Override
	public HttpSecurityAction onSecurityProblem(Set<SecurityProblem> problems) {
		return HttpSecurityAction.CONTINUE;
	}
}
