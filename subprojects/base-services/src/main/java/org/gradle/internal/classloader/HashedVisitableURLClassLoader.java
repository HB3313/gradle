/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.internal.classloader;

import com.google.common.hash.HashCode;
import org.gradle.internal.classpath.ClassPath;

import java.net.URL;
import java.util.Collection;

public class HashedVisitableURLClassLoader extends VisitableURLClassLoader implements HashedClassLoader {
    private final HashCode hashCode;

    public HashedVisitableURLClassLoader(ClassLoader parent, Collection<URL> urls, HashCode hashCode) {
        super(parent, urls);
        this.hashCode = hashCode;
    }

    public HashedVisitableURLClassLoader(ClassLoader parent, ClassPath classPath, HashCode hashCode) {
        super(parent, classPath);
        this.hashCode = hashCode;
    }

    @Override
    public HashCode getClassLoaderHash() {
        return hashCode;
    }
}
