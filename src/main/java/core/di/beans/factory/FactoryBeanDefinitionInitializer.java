package core.di.beans.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author KingCjy
 */
public class FactoryBeanDefinitionInitializer implements BeanInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FactoryBeanDefinitionInitializer.class);
    private static final Method GET_OBJECT = FactoryBean.class.getMethods()[0];
    private static final Method SET_BEAN_FACTORY = BeanFactoryAware.class.getMethods()[0];

    private ClassBeanDefinitionInitializer classBeanDefinitionInitializer;
    private MethodBeanDefinitionInitializer methodBeanDefinitionInitializer;


    public FactoryBeanDefinitionInitializer(ClassBeanDefinitionInitializer classBeanDefinitionInitializer, MethodBeanDefinitionInitializer methodBeanDefinitionInitializer) {
        this.classBeanDefinitionInitializer = classBeanDefinitionInitializer;
        this.methodBeanDefinitionInitializer = methodBeanDefinitionInitializer;
    }

    @Override
    public boolean support(BeanDefinition beanDefinition) {
        return beanDefinition instanceof FactoryBeanDefinition;
    }

    @Nullable
    @Override
    public Object instantiate(BeanDefinition definition, BeanFactory beanFactory) {
        FactoryBeanDefinition beanDefinition = (FactoryBeanDefinition) definition;

        FactoryBean<?> factoryBean = createInstance(beanDefinition, beanFactory);
        setBeanFactory(factoryBean, beanFactory);
        Object instance = invokeFactoryBean(factoryBean);

        logger.info("bean " + instance.getClass() + " instantiate from FactoryBean");

        return instance;
    }

    private void setBeanFactory(FactoryBean<?> factory, BeanFactory beanFactory) {
        if(!BeanFactoryAware.class.isAssignableFrom(factory.getClass())) {
            return;
        }

        try {
            SET_BEAN_FACTORY.invoke(factory, beanFactory);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(factory.getClass(), "illegal bean registered");
        } catch (InvocationTargetException e) {
            throw new BeanInstantiationException(GET_OBJECT, "Method threw exception", e.getTargetException());
        }
    }

    private Object invokeFactoryBean(FactoryBean<?> factoryBean) {
        try {
            return GET_OBJECT.invoke(factoryBean);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(factoryBean.getClass(), "illegal bean registered");
        } catch (InvocationTargetException e) {
            throw new BeanInstantiationException(GET_OBJECT, "Method threw exception", e.getTargetException());
        }
    }

    private FactoryBean<?> createInstance(FactoryBeanDefinition beanDefinition, BeanFactory beanFactory) {
        if(beanDefinition.getBeanDefinition() instanceof ClassBeanDefinition) {
            return (FactoryBean<?>) classBeanDefinitionInitializer.instantiate(beanDefinition.getBeanDefinition(), beanFactory);
        }

        return (FactoryBean<?>) methodBeanDefinitionInitializer.instantiate(beanDefinition.getBeanDefinition(), beanFactory);
    }
}
