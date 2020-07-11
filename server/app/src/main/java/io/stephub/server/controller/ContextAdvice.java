package io.stephub.server.controller;

import io.stephub.server.model.Context;
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
