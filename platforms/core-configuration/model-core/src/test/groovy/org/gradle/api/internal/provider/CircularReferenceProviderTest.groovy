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

package org.gradle.api.internal.provider

import groovy.transform.MapConstructor
import org.gradle.api.Transformer
import org.gradle.api.provider.HasConfigurableValue
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.util.TestUtil
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.function.BiFunction
import java.util.function.Consumer

import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.CALCULATE_EXECUTION_TIME_VALUE
import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.CALCULATE_PRESENCE
import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.CALCULATE_VALUE
import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.GET
import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.GET_PRODUCER
import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.TO_STRING
import static org.gradle.api.internal.provider.CircularReferenceProviderTest.ProviderConsumer.WITH_FINAL_VALUE
import static org.gradle.api.internal.provider.ValueSupplier.ValueConsumer.IgnoreUnsafeRead

class CircularReferenceProviderTest extends Specification {
    enum ProviderConsumer implements Consumer<ProviderInternal<?>> {
        GET("get", { it.get() }),
        CALCULATE_VALUE("calculateValue", { it.calculateValue(IgnoreUnsafeRead) }),
        CALCULATE_PRESENCE("calculatePresence", { it.calculatePresence(IgnoreUnsafeRead) }),
        CALCULATE_EXECUTION_TIME_VALUE("calculateExecutionTimeValue", { it.calculateExecutionTimeValue() }),
        WITH_FINAL_VALUE("withFinalValue", { it.withFinalValue(IgnoreUnsafeRead) }),
        GET_PRODUCER("getProducer", { it.getProducer() }),
        TO_STRING("toString", { it.toString() })

        private final String stringValue
        private final Consumer<ProviderInternal<?>> impl

        ProviderConsumer(String stringValue, Consumer<ProviderInternal<?>> impl) {
            this.impl = impl
            this.stringValue = stringValue
        }

        @Override
        String toString() {
            return stringValue
        }

        @Override
        void accept(ProviderInternal<?> providerInternal) {
            impl.accept(providerInternal)
        }
    }

    def "detects circular reference in mapping function for #testName when calling #consumer"(
        String testName,
        ProviderInternal<?> provider,
        Consumer<? super ProviderInternal<?>> consumer
    ) {
        when:
        consumer.accept(provider)

        then:
        EvaluationContext.CircularEvaluationException ex = thrown()
        ex.evaluationCycle == [provider, provider]

        where:
        [testName, provider, consumer] << selfReferencingProviders()*.throwingCases().collectMany { it }
    }

    def "can call #consumer when mapping function references #testName"(
        String testName,
        ProviderInternal<?> provider,
        Consumer<? super ProviderInternal<?>> consumer
    ) {
        when:
        consumer.accept(provider)

        then:
        noExceptionThrown()

        where:
        [testName, provider, consumer] << selfReferencingProviders()*.notThrowingCases().collectMany { it }
    }

    def "detects circular reference for #testName when calling finalizeValue"(
        String testName,
        HasConfigurableValue property
    ) {
        when:
        property.finalizeValue()

        then:
        EvaluationContext.CircularEvaluationException ex = thrown()
        ex.evaluationCycle == [property, property]

        where:
        [testName, property] << [
            createSelfReferencingProperty(),
            createPropertyWithSelfAsConvention(),
            createSelfReferencingMapProperty(),
            createMapPropertyWithSelfAsConvention(),
            createSelfReferencingListProperty(),
            createListPropertyWithSelfAsConvention(),
            createSelfReferencingSetProperty(),
            createSetPropertyWithSelfAsConvention(),
        ].collect { [it.testCaseName, it.provider] }
    }


    static List<TestCases> selfReferencingProviders() {
        return [
            createSelfReferencingDefaultProvider(),
            createSelfReferencingBiProvider(),
            createSelfReferencingFlatMapProvider(),
            createSelfReferencingTransformProvider(),
            createSelfReferencingMappingProvider(),
            createSelfReferencingFilterProvider(),
            createSelfReferencingProperty(),
            createPropertyWithSelfAsConvention(),
            createSelfReferencingMapProperty(),
            createMapPropertyWithSelfAsConvention(),
            createSelfReferencingListProperty(),
            createListPropertyWithSelfAsConvention(),
            createSelfReferencingSetProperty(),
            createSetPropertyWithSelfAsConvention(),
        ]
    }

    @MapConstructor(includeFields = true, post = { assertAllConsumersCovered() })
    static class TestCases {
        private String testCaseName

        public ProviderInternal<?> provider

        private List<ProviderConsumer> throwing
        private List<ProviderConsumer> notThrowing

        def throwingCases() {
            return toTestArguments(throwing)
        }

        def notThrowingCases() {
            return toTestArguments(notThrowing)
        }

        String getTestCaseName() {
            testCaseName ?: provider.getClass().simpleName
        }

        String setTestCaseName(String testCaseName) {
            this.testCaseName = testCaseName
        }

        private def toTestArguments(List<ProviderConsumer> consumers) {
            return consumers.collect { [testCaseName ?: provider.getClass().simpleName, provider, it] }
        }

        private void assertAllConsumersCovered() {
            assert throwing.toUnique() == throwing
            assert notThrowing.toUnique() == notThrowing

            assert throwing.intersect(notThrowing).empty

            assert ProviderConsumer.values() as List == (throwing + notThrowing).toSorted()
        }
    }

    static TestCases createSelfReferencingDefaultProvider() {
        def callable = providerReferencingCallable()
        def provider = callable.provider = createProvider(callable)
        return new TestCases(
            provider: provider,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, WITH_FINAL_VALUE
            ],
            notThrowing: [
                GET_PRODUCER, TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingBiProvider() {
        def combiner = providerReferencingBiFunction()
        def provider = combiner.provider = new BiProvider(String, createProvider { "A" }, createProvider { "B" }, combiner)
        return new TestCases(
            provider: provider,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, WITH_FINAL_VALUE
            ],
            notThrowing: [
                GET_PRODUCER, TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingFlatMapProvider() {
        def transformer = providerReferencingFlatMapTransformer()
        def provider = transformer.provider = new FlatMapProvider(new DefaultProvider({ "value" }), transformer)
        return new TestCases(
            provider: provider,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingTransformProvider() {
        def transformer = providerReferencingTransformer()
        def provider = transformer.provider = new TransformBackedProvider<>(String, new DefaultProvider({ "value" }), transformer)
        return new TestCases(
            provider: provider,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, WITH_FINAL_VALUE
            ],
            notThrowing: [
                GET_PRODUCER, TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingMappingProvider() {
        def transformer = providerReferencingTransformer()
        def provider = transformer.provider = new MappingProvider<>(String, createProvider { "value" }, transformer)
        return new TestCases(
            provider: provider,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_EXECUTION_TIME_VALUE, WITH_FINAL_VALUE
            ],
            notThrowing: [
                CALCULATE_PRESENCE, GET_PRODUCER, TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingFilterProvider() {
        def spec = providerReferencingSpec()
        def provider = spec.provider = new FilteringProvider<>(new DefaultProvider({ "value" }), spec)
        return new TestCases(
            provider: provider,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, WITH_FINAL_VALUE
            ],
            notThrowing: [
                GET_PRODUCER, TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingProperty() {
        def property = TestUtil.objectFactory().property(String)
        property.set(property)
        return new TestCases(
            testCaseName: "property",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createPropertyWithSelfAsConvention() {
        def property = TestUtil.objectFactory().property(String)
        property.convention(property)
        return new TestCases(
            testCaseName: "property with self as convention",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingMapProperty() {
        def property = TestUtil.objectFactory().mapProperty(String, String)
        property.set(property)
        return new TestCases(
            testCaseName: "map property",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createMapPropertyWithSelfAsConvention() {
        def property = TestUtil.objectFactory().mapProperty(String, String)
        property.convention(property)
        return new TestCases(
            testCaseName: "map property with self as convention",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingListProperty() {
        def property = TestUtil.objectFactory().listProperty(String)
        property.set(property)
        return new TestCases(
            testCaseName: "list property",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createListPropertyWithSelfAsConvention() {
        def property = TestUtil.objectFactory().listProperty(String)
        property.convention(property)
        return new TestCases(
            testCaseName: "list property with self as convention",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createSelfReferencingSetProperty() {
        def property = TestUtil.objectFactory().setProperty(String)
        property.set(property)
        return new TestCases(
            testCaseName: "set property",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static TestCases createSetPropertyWithSelfAsConvention() {
        def property = TestUtil.objectFactory().setProperty(String)
        property.convention(property)
        return new TestCases(
            testCaseName: "set property with self as convention",
            provider: property as ProviderInternal<?>,
            throwing: [
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER, WITH_FINAL_VALUE
            ],
            notThrowing: [
                TO_STRING
            ]
        )
    }

    static def providerReferencingCallable() {
        return new Callable<String>() {
            Provider<String> provider

            @Override
            String call() throws Exception {
                return provider.get()
            }

            @Override
            String toString() {
                return "Callable " + provider
            }
        }
    }

    static def providerReferencingBiFunction() {
        return new BiFunction<String, String, String>() {
            Provider<String> provider

            @Override
            String apply(String s, String s2) {
                return provider.get()
            }

            @Override
            String toString() {
                return "BiFunction " + provider
            }
        }
    }

    static def providerReferencingTransformer() {
        return new Transformer<String, String>() {
            Provider<String> provider

            @Override
            String transform(String s) {
                return provider.get()
            }

            @Override
            String toString() {
                return "Transformer " + provider
            }
        }
    }

    static def providerReferencingFlatMapTransformer() {
        return new Transformer<Provider<String>, String>() {
            Provider<String> provider

            @Override
            Provider<String> transform(String s) {
                provider.get()
                return provider
            }

            @Override
            String toString() {
                return "Transformer " + provider
            }
        }
    }

    static def providerReferencingSpec() {
        return new Spec<String>() {
            Provider<String> provider

            @Override
            boolean isSatisfiedBy(String element) {
                return provider.orNull != null
            }

            @Override
            String toString() {
                return "Spec " + provider
            }
        }
    }

    static ProviderInternal<String> createProvider(Callable<String> callable) {
        return new DefaultProvider<>(callable)
    }
}
