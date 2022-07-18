package net.suqatri.cloud.node.console.setup.annotations;

import net.suqatri.cloud.node.console.setup.SetupSuggester;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface AnswerCompleter {

    Class<? extends SetupSuggester> value();

}
