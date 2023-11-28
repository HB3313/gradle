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

package org.gradle.api.problems;

import org.gradle.api.Incubating;

import javax.annotation.Nullable;

/**
 * {@link Problem} instance builder allowing the specification of all optional fields.
 *
 * This is the last interface in the builder chain. The order of steps can be traced from the {@link Problems} service interface.
 *
 * An example of how to use the builder:
 * <pre>{@code
 *  <problemService>.createProblemBuilder()
 *          .label("test problem")
 *          .undocumented()
 *          .noLocation()
 *          .cotegory("problemCategory")
 *          .severity(Severity.ERROR)
 *          .details("this is a test")
 *  }</pre>
 *
 * @since 8.6
 */
@Incubating
public interface ReportableProblemBuilder extends BasicProblemBuilder {

    /**
     * Creates the new problem. Calling {@link #build()} won't report the problem via build operations, it can be done separately by calling {@link ReportableProblem#report()}.
     *
     * @return the new problem
     */
    ReportableProblem build();

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder label(String label, Object... args);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder category(String category, String... details);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder documentedAt(DocLink doc);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder undocumented();

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder fileLocation(String path, @Nullable Integer line, @Nullable Integer column, @Nullable Integer length);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder pluginLocation(String pluginId);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder stackLocation();

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder noLocation();

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder details(String details);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder solution(String solution);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder additionalData(String key, Object value);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder withException(RuntimeException e);

    /**
     * {@inheritDoc}
     */
    @Override
    ReportableProblemBuilder severity(Severity severity);
}
