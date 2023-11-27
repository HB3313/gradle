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
import org.gradle.api.problems.ReportableProblemBuilder;
import org.gradle.api.problems.Severity;

import javax.annotation.Nullable;

/**
 * Builder for problems.
 *
 * @since 8.3
 */
@Incubating
public class DefaultReportableProblemBuilder extends DefaultBasicProblemBuilder implements ReportableProblemBuilder {

    private final InternalProblems problemsService;

    public DefaultReportableProblemBuilder(InternalProblems problemsService, String namespace) {
        super(namespace);
        this.problemsService = problemsService;

    }
    public DefaultReportableProblemBuilder(InternalProblems problemsService, ReportableProblem problem) {
        super(problem);
        this.problemsService = problemsService;
    }

    public ReportableProblem build() {
        if (!isExplicitlyUndocumented() && getDocLink() == null) {
            throw new IllegalStateException("Problem is not documented: " + getLabel());
        }

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


    public ReportableProblemBuilder label(String label, Object... args) {
        super.label(label, args);
        return this;
    }

    @Override
    public ReportableProblemBuilder severity(Severity severity) {
        super.severity(severity);
        return this;
    }

    public ReportableProblemBuilder location(String path, @javax.annotation.Nullable Integer line) {
        location(path, line, null);
        return this;
    }

    public ReportableProblemBuilder location(String path, @javax.annotation.Nullable Integer line, @javax.annotation.Nullable Integer column) {
        super.location(path, line, column);
        return this;
    }

    public ReportableProblemBuilder fileLocation(String path, @javax.annotation.Nullable Integer line, @javax.annotation.Nullable Integer column, @javax.annotation.Nullable Integer length) {
        super.fileLocation(path, line, column, length);
        return this;
    }

    @Override
    public ReportableProblemBuilder pluginLocation(String pluginId) {
        super.pluginLocation(pluginId);
        return this;
    }

    @Override
    public ReportableProblemBuilder stackLocation() {
        super.stackLocation();
        return this;
    }

    public ReportableProblemBuilder details(String details) {
        super.details(details);
        return this;
    }

    public ReportableProblemBuilder documentedAt(DocLink doc) {
        super.documentedAt(doc);
        return this;
    }

    @Override
    public ReportableProblemBuilder category(String category, String... details){
        super.category(category, details);
        return this;
    }

    public ReportableProblemBuilder solution(@Nullable String solution) {
        super.solution(solution);
        return this;
    }

    public ReportableProblemBuilder additionalData(String key, Object value) {
        super.additionalData(key, value);
        return this;
    }

    @Override
    public ReportableProblemBuilder withException(RuntimeException e) {
        super.withException(e);
        return this;
    }
}
