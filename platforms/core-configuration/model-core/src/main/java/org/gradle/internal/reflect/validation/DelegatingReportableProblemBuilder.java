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

package org.gradle.internal.reflect.validation;

import org.gradle.api.NonNullApi;
import org.gradle.api.problems.DocLink;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.ReportableProblemBuilder;
import org.gradle.api.problems.Severity;

import javax.annotation.Nullable;

@NonNullApi
class DelegatingReportableProblemBuilder implements ReportableProblemBuilder {

    private final ReportableProblemBuilder delegate;

    DelegatingReportableProblemBuilder(ReportableProblemBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    public ReportableProblemBuilder label(String label, Object... args) {
        return validateDelegate(delegate).label(label, args);
    }

    @Override
    public ReportableProblemBuilder documentedAt(DocLink doc) {
        return validateDelegate(delegate.documentedAt(doc));
    }

    @Override
    public ReportableProblemBuilder undocumented() {
        return validateDelegate(delegate.undocumented());
    }

    @Override
    public ReportableProblemBuilder fileLocation(String path, @Nullable Integer line, @Nullable Integer column, @Nullable Integer length) {
        return validateDelegate(delegate.fileLocation(path, line, column, length));
    }

    @Override
    public ReportableProblemBuilder pluginLocation(String pluginId) {
        return validateDelegate(delegate.pluginLocation(pluginId));
    }

    private <T> T validateDelegate(T newDelegate) {
        if (delegate != newDelegate) {
            throw new IllegalStateException("Builder pattern expected to return 'this'");
        }
        return newDelegate;
    }

    @Override
    public ReportableProblemBuilder stackLocation() {
        return validateDelegate(delegate.stackLocation());
    }

    @Override
    public ReportableProblemBuilder noLocation() {
        return validateDelegate(delegate.noLocation());
    }

    @Override
    public ReportableProblemBuilder category(String category, String... details){
        return validateDelegate(delegate.category(category, details));
    }

    @Override
    public ReportableProblemBuilder details(String details) {
        return validateDelegate(delegate.details(details));
    }

    @Override
    public ReportableProblemBuilder solution(@Nullable String solution) {
        return validateDelegate(delegate.solution(solution));
    }

    @Override
    public ReportableProblemBuilder additionalData(String key, Object value) {
        return validateDelegate(delegate.additionalData(key, value));
    }

    @Override
    public ReportableProblemBuilder withException(RuntimeException e) {
        return validateDelegate(delegate.withException(e));
    }

    @Override
    public ReportableProblemBuilder severity(Severity severity) {
        return validateDelegate(delegate.severity(severity));
    }

    @Override
    public ReportableProblem build() {
        return delegate.build();
    }
}
