package core.aop;

import java.lang.reflect.Method;

/**
 * @author KingCjy
 */
public interface Pointcut {
    Pointcut DEFAULT_POINTCUT = method -> false;

    boolean matches(Method method);
}
