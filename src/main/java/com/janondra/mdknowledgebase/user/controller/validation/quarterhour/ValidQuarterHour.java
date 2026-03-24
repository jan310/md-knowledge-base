package com.janondra.mdknowledgebase.user.controller.validation.quarterhour;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuarterHourValidator.class)
public @interface ValidQuarterHour {
    String message() default "must be a time whose minute is 0, 15, 30, or 45";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
