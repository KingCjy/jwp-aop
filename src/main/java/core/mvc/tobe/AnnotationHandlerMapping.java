package core.mvc.tobe;

import core.annotation.Component;
import core.annotation.web.Controller;
import core.annotation.web.RequestMethod;
import core.di.beans.factory.BeanFactory;
import core.mvc.HandlerMapping;
import core.mvc.tobe.support.ArgumentResolver;
import core.mvc.tobe.support.ArgumentResolverComposite;
import core.util.OrderComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotationHandlerMapping implements HandlerMapping {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private Map<HandlerKey, HandlerExecution> handlerExecutions = new LinkedHashMap<>();
    private Controllers controllers;
    private BeanFactory beanFactory;

    public AnnotationHandlerMapping(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void initialize() {
        logger.info("## Initialized Annotation Handler Mapping");

        ArgumentResolver argumentResolver = new ArgumentResolverComposite(getArgumentResolvers());
        Object[] controllerInstances = getControllers();

        controllers = new Controllers(controllerInstances, argumentResolver);
        this.handlerExecutions = controllers.getHandlerExecutions();
    }

    private Object[] getControllers() {
        return beanFactory.getAnnotatedBeans(Controller.class);
    }

    private ArgumentResolver[] getArgumentResolvers() {
        return Arrays.stream(beanFactory.getAnnotatedBeans(Component.class))
                .filter(object -> ArgumentResolver.class.isAssignableFrom(object.getClass()))
                .map(object -> (ArgumentResolver) object)
                .sorted(OrderComparator.INSTANCE)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .toArray(new ArgumentResolver[]{});
    }

    public Object getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod rm = RequestMethod.valueOf(request.getMethod().toUpperCase());
        logger.debug("requestUri : {}, requestMethod : {}", requestUri, rm);

        return getHandlerInternal(new HandlerKey(requestUri, rm));
    }

    private HandlerExecution getHandlerInternal(HandlerKey requestHandlerKey) {
        return handlerExecutions.keySet().stream()
                .filter(handlerKey -> handlerKey.isMatch(requestHandlerKey))
                .map(handlerKey -> handlerExecutions.get(handlerKey))
                .findFirst()
                .orElse(null);
    }
}
