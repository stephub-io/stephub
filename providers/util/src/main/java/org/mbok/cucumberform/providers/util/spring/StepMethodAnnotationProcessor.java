package org.mbok.cucumberform.providers.util.spring;

import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter.ProviderException;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter.SessionState;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Component
public class StepMethodAnnotationProcessor implements BeanPostProcessor {

    private final ConfigurableListableBeanFactory configurableBeanFactory;

    @Autowired
    public StepMethodAnnotationProcessor(final ConfigurableListableBeanFactory beanFactory) {
        this.configurableBeanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName)
            throws BeansException {
        if (bean instanceof SpringBeanProvider) {
            StepMethodAnnotationProcessor.scanForStepMethods(SpringBeanProvider.class.cast(bean), beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName)
            throws BeansException {
        return bean;
    }


    private static void scanForStepMethods(final SpringBeanProvider<SessionState> provider, final String beanName) {
        final Class<?> managedBeanClass = provider.getClass();
        ReflectionUtils.doWithMethods(managedBeanClass, method -> {
            provider.stepInvokers.put(method.getName(), buildInvoker(provider, method));
        }, method -> method.isAnnotationPresent(StepMethod.class));
    }

    private static SpringBeanProvider.StepInvoker buildInvoker(final SpringBeanProvider<SessionState> provider, final Method stepMethod) {
        final Parameter[] parameters = stepMethod.getParameters();
        final ParameterAccessor[] parameterAccessors = new ParameterAccessor[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            ParameterAccessor accessor = null;
            if (parameter.getType().isAssignableFrom(SessionState.class)) {
                accessor = ((sessionId, state, request) -> state);
            } else {
                final StepArgument expectedArgument = parameter.getAnnotation(StepArgument.class);
                if (expectedArgument != null) {
                    accessor = (((sessionId, state, request) ->
                            request.getArguments().stream().filter(argument ->
                                    argument.getName().equals(expectedArgument.name())).
                                    findFirst().orElseThrow(() -> new ProviderException("Missing argument with name=" + expectedArgument.name()))
                                    .getValue()
                    ));
                } else {
                    throw new ProviderException("Unsatisfiable step method parameter [" + i + "] with name=" + parameter.getName());
                }
            }
            parameterAccessors[i] = accessor;
        }
        return (((sessionId, state, request) -> {
            final Object[] args = new Object[parameterAccessors.length];
            for (int i = 0; i < parameterAccessors.length; i++) {
                args[i] = parameterAccessors[i].getParameter(sessionId, state, request);
            }
            try {
                return (StepResponse) stepMethod.invoke(provider, args);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new ProviderException("Failed to invoke step method=" + stepMethod.getName(), e);
            }
        }));
    }

    private interface ParameterAccessor {
        Object getParameter(String sessionId, SessionState state, StepRequest request);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface StepMethod {
        String pattern();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface StepArgument {
        String name();
    }

}
