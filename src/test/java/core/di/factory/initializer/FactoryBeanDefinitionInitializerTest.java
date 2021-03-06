package core.di.factory.initializer;

import core.di.beans.factory.BeanFactory;
import core.di.beans.factory.definition.ClassBeanDefinition;
import core.di.beans.factory.definition.FactoryBeanDefinition;
import core.di.beans.factory.initializer.BeanInitializer;
import core.di.beans.factory.initializer.ClassBeanDefinitionInitializer;
import core.di.beans.factory.initializer.FactoryBeanDefinitionInitializer;
import core.di.beans.factory.initializer.MethodBeanDefinitionInitializer;
import core.di.context.support.AnnotationConfigApplicationContext;
import core.di.factory.proxy.example.CarDao;
import core.di.factory.proxy.example.TestFactoryBean;
import next.config.MyConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author KingCjy
 */
public class FactoryBeanDefinitionInitializerTest {

    BeanInitializer beanInitializer;
    BeanFactory beanFactory;

    @BeforeEach
    public void setUp() {
        beanFactory = new AnnotationConfigApplicationContext(MyConfiguration.class);
        this.beanInitializer = new FactoryBeanDefinitionInitializer(new ClassBeanDefinitionInitializer(), new MethodBeanDefinitionInitializer());
    }

    @Test
    public void initBeanTest() {
        FactoryBeanDefinition factoryBeanDefinition = new FactoryBeanDefinition(
                new ClassBeanDefinition(TestFactoryBean.class, TestFactoryBean.class.getName())
        );

        CarDao carDao = (CarDao) beanInitializer.instantiate(factoryBeanDefinition, beanFactory);

        assertThat(carDao).isNotNull();
        assertThat(carDao).isInstanceOf(CarDao.class);
        assertThat(carDao.getDataSource()).isNotNull();
    }

    @Test
    public void initBeanFromMethodTest() {
        CarDao carDao = beanFactory.getBean("myCarDao", CarDao.class);

        assertThat(carDao).isNotNull();
        assertThat(carDao).isInstanceOf(CarDao.class);
        assertThat(carDao.getDataSource()).isNotNull();
    }
}
