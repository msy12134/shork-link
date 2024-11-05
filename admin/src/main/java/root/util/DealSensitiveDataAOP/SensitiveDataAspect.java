package root.util.DealSensitiveDataAOP;

import com.google.common.primitives.Primitives;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

@Aspect
@Component
public class SensitiveDataAspect {

    @Around("@annotation(SensitiveData)")
    public Object handleSensitiveData(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行目标方法
        Object result = joinPoint.proceed();

        if (result == null) {
            return null;
        }

        // 处理返回对象的脱敏
        maskObject(result);

        return result;
    }

    private void maskObject(Object obj) {
        if (obj == null) return;

        if (obj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) obj;
            for (Object item : collection) {
                maskSingleObject(item);
            }
        } else {
            maskSingleObject(obj);
        }
    }

    private void maskSingleObject(Object obj) {
        if (obj == null) return;

        Class<?> clazz = obj.getClass();

        // 处理简单对象
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Mask.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value instanceof String) {
                        Mask mask = field.getAnnotation(Mask.class);
                        String maskedValue = MaskUtil.mask((String) value, mask.type());
                        field.set(obj, maskedValue);
                    }
                    // 可以扩展处理其他类型
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // 递归处理嵌套对象
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object nestedObj = field.get(obj);
                if (nestedObj != null && !isPrimitiveOrWrapper(nestedObj.getClass())) {
                    maskObject(nestedObj);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || Primitives.isWrapperType(type) || type.equals(String.class);
    }
}