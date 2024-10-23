package me.untouchedodin0.privatemines.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationEntry {

    String key();

    String section() default "";

    String value() default "Unprovided default value!";

    ConfigurationValueType type() default ConfigurationValueType.STRING;
}
