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

import org.gradle.api.Incubating;
import org.gradle.api.problems.DocLink;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.Severity;

import javax.annotation.Nullable;

/**
 * Builder for problems.
 *
 * @since 8.3
 */
@Incubating
public class DefaultReportableProblemBuilder extends DefaultBasicProblemBuilder implements InternalProblemBuilder {

    private final InternalProblemReporter problemsService;

    public DefaultReportableProblemBuilder(InternalProblemReporter problemsService, String namespace) {
        super(namespace);
        this.problemsService = problemsService;

    }
    public DefaultReportableProblemBuilder(InternalProblemReporter problemsService, ReportableProblem problem) {
        super(problem);
        this.problemsService = problemsService;
    }

    public ReportableProblem build() {
        // TODO (donat) we probably don't need this
//        if (!isExplicitlyUndocumented() && getDocLink() == null) {
//            throw new IllegalStateException("Problem is not documented: " + getLabel());
//        }

        return new DefaultReportableProblem(
            getLabel(),
            getSeverity(getSeverity()),
            getLocations(),
            getDocLink(),
            getDetails(),
            getSolutions(),
            getExceptionForProblemInstantiation(), //TODO: don't create exception if already reported often
            getProblemCategory(),
            getAdditionalData(),
            getCurrentOperationId(),
            problemsService);
    }

    @Override
    public InternalProblemBuilder label(String label, Object... args) {
        super.label(label, args);
        return this;
    }

    @Override
    public InternalProblemBuilder severity(Severity severity) {
        super.severity(severity);
        return this;
    }

    @Override
    public InternalProblemBuilder location(String path, @javax.annotation.Nullable Integer line) {
        location(path, line, null);
        return this;
    }

    @Override
    public InternalProblemBuilder location(String path, @javax.annotation.Nullable Integer line, @javax.annotation.Nullable Integer column) {
        super.location(path, line, column);
        return this;
    }

    @Override
    public InternalProblemBuilder fileLocation(String path, @javax.annotation.Nullable Integer line, @javax.annotation.Nullable Integer column, @javax.annotation.Nullable Integer length) {
        super.fileLocation(path, line, column, length);
        return this;
    }

    @Override
    public InternalProblemBuilder pluginLocation(String pluginId) {
        super.pluginLocation(pluginId);
        return this;
    }

    @Override
    public InternalProblemBuilder stackLocation() {
        super.stackLocation();
        return this;
    }

    public InternalProblemBuilder details(String details) {
        super.details(details);
        return this;
    }

    public InternalProblemBuilder documentedAt(DocLink doc) {
        super.documentedAt(doc);
        return this;
    }

    @Override
    public InternalProblemBuilder category(String category, String... details){
        super.category(category, details);
        return this;
    }

    public InternalProblemBuilder solution(@Nullable String solution) {
        super.solution(solution);
        return this;
    }

    public InternalProblemBuilder additionalData(String key, Object value) {
        super.additionalData(key, value);
        return this;
    }

    @Override
    public InternalProblemBuilder withException(RuntimeException e) {
        super.withException(e);
        return this;
    }
}
