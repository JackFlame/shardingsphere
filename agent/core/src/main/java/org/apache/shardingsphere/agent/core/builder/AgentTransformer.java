/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.core.builder;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.utility.JavaModule;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.core.builder.advisor.AdviceFactory;
import org.apache.shardingsphere.agent.core.builder.advisor.MethodAdvisorBuilder;
import org.apache.shardingsphere.agent.core.classloader.ClassLoaderContext;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;
import org.apache.shardingsphere.agent.core.plugin.PluginLifecycleServiceManager;
import org.apache.shardingsphere.agent.core.plugin.advisor.AdvisorConfiguration;

import java.util.Collection;
import java.util.Map;

/**
 * Agent transformer.
 */
@RequiredArgsConstructor
public final class AgentTransformer implements Transformer {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private final Map<String, PluginConfiguration> pluginConfigs;
    
    private final Collection<PluginJar> pluginJars;
    
    private final Map<String, AdvisorConfiguration> advisorConfigs;
    
    private final boolean isEnhancedForProxy;
    
    @SuppressWarnings("NullableProblems")
    @Override
    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (!advisorConfigs.containsKey(typeDescription.getTypeName())) {
            return builder;
        }
        ClassLoaderContext classLoaderContext = new ClassLoaderContext(classLoader, pluginJars);
        PluginLifecycleServiceManager.init(pluginConfigs, pluginJars, classLoaderContext.getAgentClassLoader(), isEnhancedForProxy);
        Builder<?> targetAdviceObjectBuilder = builder.defineField(EXTRA_DATA,
                Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE).implement(TargetAdviceObject.class).intercept(FieldAccessor.ofField(EXTRA_DATA));
        return new MethodAdvisorBuilder(new AdviceFactory(classLoaderContext), advisorConfigs.get(typeDescription.getTypeName()), typeDescription).build(targetAdviceObjectBuilder);
    }
}
