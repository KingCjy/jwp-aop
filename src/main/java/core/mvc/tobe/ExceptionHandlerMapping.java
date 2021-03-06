package core.mvc.tobe;

import core.annotation.web.ControllerAdvice;
import core.di.beans.factory.BeanFactory;
import core.mvc.tobe.support.ArgumentResolverComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author KingCjy
 */
public class ExceptionHandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerMapping.class);

    private Map<Class<? extends Throwable>, HandlerExecution> handlerExecutions;

    public ExceptionHandlerMapping(BeanFactory beanFactory) {
        logger.info("## Initialized Annotation Handler Mapping");

        Object[] adviceInstances = getControllerAdvices(beanFactory);

        Advices advices = new Advices(adviceInstances, new ArgumentResolverComposite());
        this.handlerExecutions = advices.getHandlerExecutions();
    }

    private Object[] getControllerAdvices(BeanFactory beanFactory) {
        return beanFactory.getAnnotatedBeans(ControllerAdvice.class);
    }

    public HandlerExecution getHandler(Throwable throwable) {
        return handlerExecutions.get(throwable.getClass());
    }
}
