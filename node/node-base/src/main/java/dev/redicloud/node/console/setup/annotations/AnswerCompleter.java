package dev.redicloud.node.console.setup.annotations;

import dev.redicloud.node.console.setup.SetupSuggester;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface AnswerCompleter {

    Class<? extends SetupSuggester> value();

}
