package org.springjutsu.validation.dsl;

import org.springjutsu.validation.executors.RuleExecutor;
import org.springjutsu.validation.rules.CollectionStrategy;
import org.springjutsu.validation.rules.RuleErrorMode;
import org.springjutsu.validation.rules.ValidationRule;

import com.fluentinterface.builder.Builder;

public interface ValidationRuleBuilder extends Builder<ValidationRule>
{
	public ValidationRuleBuilder forPath(String path);
	public ValidationRuleBuilder usingHandler(String handlerName);
	public <M,A> ValidationRuleBuilder usingHandler(RuleExecutor<M, A> handlerImpl);
	public ValidationRuleBuilder withArgument(String value);
	public ValidationRuleBuilder withErrorPath(String errorPath);
	public ValidationRuleBuilder withMessageCode(String message);
	public ValidationRuleBuilder withMessageText(String message);
	public ValidationRuleBuilder behaviorOnFail(RuleErrorMode onFail);
	public ValidationRuleBuilder withCollectionStrategy(CollectionStrategy collectionStrategy);
	public ValidationRuleBuilder havingValidationContexts(ValidationContextBuilder... contexts);
	public ValidationRuleBuilder havingRules(ValidationRuleBuilder... rules);
	
}