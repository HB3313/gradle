/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.launcher.cli

import org.gradle.cli.CommandLineParser
import org.gradle.internal.Factory
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.internal.service.DefaultServiceRegistry
import org.gradle.internal.service.ServiceRegistry
import org.gradle.launcher.daemon.bootstrap.ForegroundDaemonAction
import org.gradle.launcher.daemon.client.DaemonClient
import org.gradle.launcher.daemon.client.SingleUseDaemonClient
import org.gradle.launcher.daemon.configuration.DaemonParameters
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.tooling.internal.provider.SetupLoggingActionExecuter
import spock.util.environment.RestoreSystemProperties
import org.gradle.util.UsesNativeServices
import org.junit.Rule
import spock.lang.Specification

@UsesNativeServices
@RestoreSystemProperties
class BuildActionsFactoryTest extends Specification {

    @Rule
    TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider(getClass());
    ServiceRegistry loggingServices = new DefaultServiceRegistry()
    boolean useCurrentProcess

    BuildActionsFactory factory = new BuildActionsFactory(loggingServices) {
        @Override
        def boolean canUseCurrentProcess(DaemonParameters requiredBuildParameters) {
            return useCurrentProcess
        }
    }

    def setup() {
        def factory = Mock(Factory) { _ * create() >> Mock(LoggingManagerInternal) }
        loggingServices.add(OutputEventListener, Mock(OutputEventListener))
        loggingServices.add(StyledTextOutputFactory, Mock(StyledTextOutputFactory))
        loggingServices.addProvider(new Object() {
            Factory<LoggingManagerInternal> createFactory() {
                return factory
            }})
    }

    def "check that --max-workers overrides org.gradle.workers.max"() {
        when:
        RunBuildAction action = convert('--max-workers=5')

        then:
        action.startParameter.maxWorkerCount == 5
    }

    def "by default daemon is used"() {
        when:
        def action = convert('args')

        then:
        isDaemon action
    }

    def "daemon is used when command line option is used"() {
        when:
        def action = convert('--daemon', 'args')

        then:
        isDaemon action
    }

    def "does not use daemon when no-daemon command line option issued"() {
        given:
        useCurrentProcess = true

        when:
        def action = convert('--no-daemon', 'args')

        then:
        isInProcess action
    }

    def "shows status of daemons"() {
        when:
        def action = convert('--status')

        then:
        action instanceof ReportDaemonStatusAction
    }

    def "stops daemon"() {
        when:
        def action = convert('--stop')

        then:
        action instanceof StopDaemonAction
    }

    def "runs daemon in foreground"() {
        when:
        def action = convert('--foreground')

        then:
        action instanceof ForegroundDaemonAction
    }

    def "executes with single use daemon if current process cannot be used"() {
        given:
        useCurrentProcess = false

        when:
        def action = convert('--no-daemon')

        then:
        isSingleUseDaemon action
    }

    def convert(String... args) {
        def parser = new CommandLineParser()
        factory.configureCommandLineParser(parser)
        def cl = parser.parse(args)
        return factory.createAction(parser, cl)
    }

    void isDaemon(def action) {
        assert action instanceof RunBuildAction
        assert action.executer instanceof DaemonClient
    }

    void isInProcess(def action) {
        assert action instanceof RunBuildAction
        assert action.executer instanceof SetupLoggingActionExecuter
    }

    void isSingleUseDaemon(def action) {
        assert action instanceof RunBuildAction
        assert action.executer instanceof SingleUseDaemonClient
    }
}
