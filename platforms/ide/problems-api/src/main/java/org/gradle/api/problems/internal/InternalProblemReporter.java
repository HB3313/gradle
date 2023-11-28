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

import org.gradle.api.problems.BasicProblemBuilder;
import org.gradle.api.problems.Problem;
import org.gradle.api.problems.ProblemReporter;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.ReportableProblemBuilder;
import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;

@ServiceScope(Scopes.BuildTree.class)
public interface InternalProblemReporter extends ProblemReporter {

    RuntimeException throwingInternal(InternalProblemBuilderSpec action);


    RuntimeException rethrowingInternal(RuntimeException e, InternalProblemBuilderSpec action);


    ReportableProblem createInternal(InternalProblemBuilderSpec action);

    // TODO (donat) maybe move the methods below to the implementation class, or at least to a sub-interface

    /**
     * Returns a new problem builder which can configure and create Problem instances.
     * <p>
     * Once all mandatory fields are set, the returned type will allow clients to call {@link BasicProblemBuilder#build()} to create a new Problem instance.
     * The {@link BasicProblemBuilder#build()} method doesn't have any side effects, it just creates a new instance. Problems should be reported separately with {@link ReportableProblem#report()}.
     *
     * @return a new problem builder
     */
    ReportableProblemBuilder createProblemBuilder();

    void report(Problem problem);
}
