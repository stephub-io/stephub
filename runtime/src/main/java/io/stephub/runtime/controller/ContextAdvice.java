package io.stephub.runtime.controller;

import io.stephub.runtime.model.Context;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ContextAdvice {
    @ModelAttribute
    public static Context createDefaultContext() {
        return new Context() {
        };
    }
}
