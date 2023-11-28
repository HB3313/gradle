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
 * {@link Problem} instance configurator allowing the specification of all optional fields.
 *
 * This is the last interface in the builder chain. The order of steps can be traced from the {@link Problems} service interface.
 *
 * An example of how to use the builder:
 * <pre>{@code
 *  <problemService>.report(configurator -> configurator
 *          .label("test problem")
 *          .undocumented()
 *          .noLocation()
 *          .category("problemCategory")
 *          .severity(Severity.ERROR)
 *          .details("this is a test")
 *  }</pre>
 *
 * @since 8.5
 */
@Incubating
public interface ProblemBuilder {

    /**
     * Declares a short message for this problem.
     * @param label the short message
     * @param args the arguments for formatting the label with {@link String#format(String, Object...)}
     *
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder label(String label, Object... args);

    /**
     * Declares the problem category.
     *
     * It offers a hierarchical categorization with arbitrary details.
     * Clients must declare a main category string. Freeform with the following conventions and limitations.
     * Subcategories can be optionally specified with arbitrary details. The same conventions and limitations apply.
     * When a problem is created (with BuildableProblemBuilder.build()) the category can be obtained with {@link Problem}.
     * The `ProblemCategory` then represents a local category with some namespace, distinguishing built-in and third-party categories.
     * Example:
     * {@code category("validation", "missing-input") }

     * @param category the type name
     * @param details the type details
     * @return the builder for the next required property
     * @since 8.6
     * @see ProblemCategory
     */
    ProblemBuilder category(String category, String... details);

    /**
     * Declares the documentation for this problem.
     *
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder documentedAt(DocLink doc);

    /**
     * Marks this problem as undocumented
     *
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder undocumented();

    /**
     * Declares that this problem is in a file with optional position and length.
     *
     * @param path the file location
     * @param line the line number
     * @param column the column number
     * @param length the length of the text
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder fileLocation(String path, @Nullable Integer line, @Nullable Integer column, @Nullable Integer length);

    /**
     * Declares that this problem is emitted while applying a plugin.
     *
     * @param pluginId the ID of the applied plugin
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder pluginLocation(String pluginId);

    /**
     * Declares that this problem should automatically collect the location information based on the current stack trace.
     *
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder stackLocation();

    /**
     * Declares that this problem has no associated location data.
     *
     * @return the builder for the next required property
     * @since 8.6
     */
    ProblemBuilder noLocation();

    /**
     * The long description of this problem.
     *
     * @param details the details
     * @return this
     */
    ProblemBuilder details(String details);

    /**
     * The description of how to solve this problem
     *
     * @param solution the solution.
     * @return this
     */
    ProblemBuilder solution(String solution);

    /**
     * Specifies arbitrary data associated with this problem.
     * <p>
     * The only supported value type is {@link String}. Future Gradle versions may support additional types.
     *
     * @return this
     * @throws RuntimeException for null values and for values with unsupported type.
     * @since 8.6
     */
    ProblemBuilder additionalData(String key, Object value);

    /**
     * The exception causing this problem.
     *
     * @param e the exception.
     * @return this
     */
    ProblemBuilder withException(RuntimeException e);

    /**
     * Declares the severity of the problem.
     *
     * @param severity the severity
     * @return this
     */
    ProblemBuilder severity(Severity severity);
}
