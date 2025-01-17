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

package org.apache.shardingsphere.infra.expr.purelist;

import com.google.common.base.Strings;
import groovy.lang.Closure;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure List inline expression parser.
 */
public final class PureListInlineExpressionParser implements InlineExpressionParser {
    
    private static final char SPLITTER = ',';
    
    @Override
    public String handlePlaceHolder(final String inlineExpression) {
        return inlineExpression.contains("$->{") ? inlineExpression.replaceAll("\\$->\\{", "\\$\\{") : inlineExpression;
    }
    
    @Override
    public List<String> splitAndEvaluate(final String inlineExpression) {
        return Strings.isNullOrEmpty(inlineExpression) ? Collections.emptyList() : split(inlineExpression);
    }
    
    @Override
    public Closure<?> evaluateClosure(final String inlineExpression) {
        throw new UnsupportedOperationException("Groovy classes cannot be used directly within GraalVM Native Image.");
    }
    
    private List<String> split(final String inlineExpression) {
        List<String> result = new ArrayList<>();
        StringBuilder segment = new StringBuilder();
        for (int i = 0; i < inlineExpression.length(); i++) {
            char each = inlineExpression.charAt(i);
            if (each == SPLITTER) {
                result.add(segment.toString().trim());
                segment.setLength(0);
            } else {
                segment.append(each);
            }
        }
        if (segment.length() > 0) {
            result.add(segment.toString().trim());
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "PURELIST";
    }
}
