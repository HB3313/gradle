/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.api.problems.internal;

import org.gradle.api.problems.DocLink;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.ReportableProblemBuilder;
import org.gradle.api.problems.Severity;

import javax.annotation.Nullable;

public interface InternalProblemBuilder extends ReportableProblemBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblem build();

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder label(String label, Object... args);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder category(String category, String... details);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder documentedAt(DocLink docLink);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder fileLocation(String path, @Nullable Integer line, @Nullable Integer column, @Nullable Integer length);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder pluginLocation(String pluginId);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder stackLocation();

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder details(String details);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder solution(String solution);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder additionalData(String key, Object value);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder withException(RuntimeException e);

    /**
     * {@inheritDoc}
     */
    @Override
    InternalProblemBuilder severity(Severity severity);
}
