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
package org.ngrinder.recorder.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async Utility class.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public abstract class AsyncUtil {

	private static ExecutorService executorService = Executors.newCachedThreadPool();

	/**
	 * Execute runnable case in async.
	 * 
	 * @param runnable
	 *            runnable object
	 */
	public static void invokeAsync(Runnable runnable) {
		synchronized (executorService) {
			executorService.submit(runnable);
		}
	}

	/**
	 * Shutdown executor service to cancel all submitted runnables.
	 */
	public static void shutdown() {
		synchronized (executorService) {
			executorService.shutdown();
		}
	}
}
