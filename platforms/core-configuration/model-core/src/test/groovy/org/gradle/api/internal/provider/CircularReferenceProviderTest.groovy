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
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
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
import static org.gradle.api.internal.provider.ValueSupplier.ValueConsumer.IgnoreUnsafeRead

class CircularReferenceProviderTest extends Specification {
    enum ProviderConsumer implements Consumer<ProviderInternal<?>> {
        GET("get", { it.get() }),
        CALCULATE_VALUE("calculateValue", { it.calculateValue(IgnoreUnsafeRead) }),
        CALCULATE_PRESENCE("calculatePresence", { it.calculatePresence(IgnoreUnsafeRead) }),
        CALCULATE_EXECUTION_TIME_VALUE("calculateExecutionTimeValue", { it.calculateExecutionTimeValue() }),
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

    def "can detect circular references in mapping function for #provider.class.simpleName when calling #consumer"(
        ProviderInternal<?> provider,
        Consumer<? super ProviderInternal<?>> consumer
    ) {
        when:
        consumer.accept(provider)

        then:
        EvaluationContext.CircularEvaluationException ex = thrown()
        ex.evaluationCycle == [provider, provider]

        where:
        [provider, consumer] << selfReferencingProviders()*.throwingCases().collectMany { it }
    }

    def "can call #consumer when mapping function references the provider #provider.class.simpleName"(
        ProviderInternal<?> provider,
        Consumer<? super ProviderInternal<?>> consumer
    ) {
        when:
        consumer.accept(provider)

        then:
        noExceptionThrown()

        where:
        [provider, consumer] << selfReferencingProviders()*.notThrowingCases().collectMany { it }
    }


    static List<TestCases> selfReferencingProviders() {
        return [
            createSelfReferencingDefaultProvider(),
            createSelfReferencingBiProvider(),
            createSelfReferencingFlatMapProvider(),
            createSelfReferencingTransformProvider(),
            createSelfReferencingMappingProvider(),
            createSelfReferencingFilterProvider()
        ]
    }

    @MapConstructor(includeFields = true, post = { assertAllConsumersCovered() })
    static class TestCases {
        private ProviderInternal<?> provider
        private List<ProviderConsumer> throwing
        private List<ProviderConsumer> notThrowing

        def throwingCases() {
            return zipWithProvider(throwing)
        }

        def notThrowingCases() {
            return zipWithProvider(notThrowing)
        }

        private def zipWithProvider(List<ProviderConsumer> consumers) {
            return consumers.collect { [provider, it] }
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
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE
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
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE
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
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE, GET_PRODUCER
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
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE
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
                GET, CALCULATE_VALUE, CALCULATE_EXECUTION_TIME_VALUE
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
                GET, CALCULATE_VALUE, CALCULATE_PRESENCE, CALCULATE_EXECUTION_TIME_VALUE
            ],
            notThrowing: [
                GET_PRODUCER, TO_STRING
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
