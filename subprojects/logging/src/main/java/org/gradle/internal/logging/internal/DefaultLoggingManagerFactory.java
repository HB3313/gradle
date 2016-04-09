/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.internal.logging.internal;

import org.gradle.internal.Factory;
import org.gradle.internal.logging.LoggingManagerInternal;
import org.gradle.internal.logging.source.LoggingConfigurer;
import org.gradle.internal.logging.source.LoggingSystem;
import org.gradle.internal.logging.source.LoggingSystemAdapter;

public class DefaultLoggingManagerFactory implements Factory<LoggingManagerInternal> {
    private final LoggingSystem slfLoggingSystem;
    private final LoggingSystem javaUtilLoggingSystem;
    private final LoggingSystem stdOutLoggingSystem;
    private final LoggingSystem stdErrLoggingSystem;
    private final LoggingOutputInternal loggingOutput;
    private final DefaultLoggingManager rootManager;
    private boolean created;

    public DefaultLoggingManagerFactory(LoggingConfigurer loggingConfigurer, LoggingOutputInternal loggingOutput, LoggingSystem javaUtilLoggingSystem, LoggingSystem stdOutLoggingSystem, LoggingSystem stdErrLoggingSystem) {
        this.loggingOutput = loggingOutput;
        this.javaUtilLoggingSystem = javaUtilLoggingSystem;
        this.stdOutLoggingSystem = stdOutLoggingSystem;
        this.stdErrLoggingSystem = stdErrLoggingSystem;
        slfLoggingSystem = new LoggingSystemAdapter(loggingConfigurer);
        rootManager = newManager();
    }

    public LoggingManagerInternal getRoot() {
        return rootManager;
    }

    public LoggingManagerInternal create() {
        if (!created) {
            created = true;
            return getRoot();
        }
        return newManager();
    }

    private DefaultLoggingManager newManager() {
        return new DefaultLoggingManager(slfLoggingSystem, javaUtilLoggingSystem, stdOutLoggingSystem, stdErrLoggingSystem, loggingOutput);
    }
}
