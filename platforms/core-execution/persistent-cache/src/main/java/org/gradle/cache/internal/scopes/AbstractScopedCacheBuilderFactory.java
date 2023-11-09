/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.cache.internal.scopes;

import org.gradle.cache.CacheBuilder;
import org.gradle.cache.UnscopedCacheBuilderFactory;
import org.gradle.cache.internal.CacheScopeMapping;
import org.gradle.cache.internal.VersionStrategy;
import org.gradle.cache.scopes.ScopedCacheBuilderFactory;
import org.gradle.util.GradleVersion;

import java.io.File;

/**
 * Abstract implementation of {@link ScopedCacheBuilderFactory}, implements interface using {@link CacheScopeMapping}.
 */
public abstract class AbstractScopedCacheBuilderFactory implements ScopedCacheBuilderFactory {
    private final CacheScopeMapping cacheScopeMapping;
    private final UnscopedCacheBuilderFactory unscopedCacheBuilderFactory;
    private final File rootContentDir;
    private final File lockDir;

    public AbstractScopedCacheBuilderFactory(File rootContentDir, UnscopedCacheBuilderFactory unscopedCacheBuilderFactory) {
        this(rootContentDir, rootContentDir, unscopedCacheBuilderFactory);
    }

    public AbstractScopedCacheBuilderFactory(File rootContentDir, File lockDir, UnscopedCacheBuilderFactory unscopedCacheBuilderFactory) {
        this.rootContentDir = rootContentDir;
        this.cacheScopeMapping = new DefaultCacheScopeMapping(rootContentDir, GradleVersion.current());
        this.unscopedCacheBuilderFactory = unscopedCacheBuilderFactory;
        this.lockDir = lockDir;
    }

    protected UnscopedCacheBuilderFactory getCacheRepository() {
        return unscopedCacheBuilderFactory;
    }

    @Override
    public File getRootDir() {
        return rootContentDir;
    }

    @Override
    public CacheBuilder createCacheBuilder(String key) {
        return unscopedCacheBuilderFactory.cache(baseDirForCache(key));
    }

    @Override
    public CacheBuilder createCrossVersionCacheBuilder(String key) {
        return unscopedCacheBuilderFactory.cache(baseDirForCrossVersionCache(key));
    }

    @Override
    public File baseDirForCache(String key) {
        return cacheScopeMapping.getBaseDirectory(rootContentDir, key, VersionStrategy.CachePerVersion);
    }

    @Override
    public File baseDirForCrossVersionCache(String key) {
        return cacheScopeMapping.getBaseDirectory(rootContentDir, key, VersionStrategy.SharedCache);
    }

    @Override
    public File lockDirForCache(String key) {
        return cacheScopeMapping.getBaseDirectory(lockDir, key, VersionStrategy.CachePerVersion);
    }

    @Override
    public File lockDirForCrossVersionCache(String key) {
        return cacheScopeMapping.getBaseDirectory(rootContentDir, key, VersionStrategy.SharedCache);
    }
}
