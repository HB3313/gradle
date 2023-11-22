/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.workers.internal;

import java.io.File;

/**
 * Represents the directories a worker needs to understand in order to execute.
 *
 * TODO: This, and its subclasses, should probably be renamed to indicate that it is only about directories (for now)
 */
public interface WorkerRequirement {
    /**
     * Returns the root directory for the build, use∂ to create and access caches.
     *
     * @return root project dir directory
     */
    File getRootProjectDirectory();

    /**
     * Returns the directory in which to execute new workers.
     *
     * @return current directory to use for new workers
     */
    File getWorkerDirectory();

    /**
     * Returns the directory for the worker to use to create and access caches.
     *
     * @return cache directory
     */
    File getCacheDirectory();
}
