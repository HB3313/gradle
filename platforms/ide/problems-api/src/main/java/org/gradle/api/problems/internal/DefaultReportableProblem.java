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

import org.gradle.api.NonNullApi;
import org.gradle.api.problems.DocLink;
import org.gradle.api.problems.ProblemCategory;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.ReportableProblemBuilder;
import org.gradle.api.problems.Severity;
import org.gradle.api.problems.locations.ProblemLocation;
import org.gradle.internal.operations.OperationIdentifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@NonNullApi
public class DefaultReportableProblem extends DefaultProblem implements ReportableProblem {

    private transient InternalProblemReporter problemService;

    public DefaultReportableProblem(
        String message,
        Severity severity,
        List<ProblemLocation> location,
        @Nullable DocLink documentationUrl,
        @Nullable String description,
        @Nullable List<String> solutions,
        @Nullable RuntimeException cause,
        ProblemCategory problemCategory,
        Map<String, Object> additionalData,
        @Nullable OperationIdentifier buildOperationId,
        @Nullable InternalProblemReporter problemService) {
        super(
            message,
            severity,
            location,
            documentationUrl,
            description,
            solutions,
            cause,
            problemCategory,
            additionalData,
            buildOperationId
        );
        this.problemService = problemService;
    }

    public void setProblemService(InternalProblemReporter problemService) {
        this.problemService = problemService;
    }

    @Override
    public void report() {
        problemService.report(this);
    }

    @Override
    public ReportableProblemBuilder toBuilder() {
        return new DefaultReportableProblemBuilder(problemService, this);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.problemService == ((DefaultReportableProblem) o).problemService && super.equals(o);
    }

    @Override
    public int hashCode() {
        return problemService.hashCode() + super.hashCode();
    }
}
