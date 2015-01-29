/*
 * Copyright 2011 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.windup.config.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * A {@link Condition} that evaluates against a {@link GraphRewrite} event.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class GraphCondition extends DefaultConditionBuilder
{

    private String inputVariablesName;
    private String outputVariablesName = Iteration.DEFAULT_VARIABLE_LIST_STRING;

    public abstract boolean evaluate(GraphRewrite event, EvaluationContext context);

    public void setInputVariablesName(String variablesName)
    {
        this.inputVariablesName = variablesName;
    }

    public String getInputVariablesName()
    {
        return inputVariablesName;
    }

    @Override
    public final boolean evaluate(Rewrite event, EvaluationContext context)
    {
        if (event instanceof GraphRewrite)
            return evaluate((GraphRewrite) event, context);
        return false;
    }

    public String getOutputVariablesName()
    {
        return outputVariablesName;
    }

    public void setOutputVariablesName(String outputVariablesName)
    {
        this.outputVariablesName = outputVariablesName;
    }
}
