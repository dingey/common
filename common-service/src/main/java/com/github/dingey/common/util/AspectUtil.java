package com.github.dingey.common.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

@SuppressWarnings("unused")
public class AspectUtil {

    private AspectUtil() {
    }

    /**
     * 计算spel表达式值
     *
     * @param pjp               pjp参数
     * @param expressionString  表达式
     * @param desiredResultType 返回类型
     * @param <T>               泛型
     * @return spel表达式的结果值
     */
    public static <T> T spel(ProceedingJoinPoint pjp, String expressionString, Class<T> desiredResultType) {
        return spel(pjp, expressionString, desiredResultType, null);
    }

    /**
     * 计算spel表达式值
     *
     * @param pjp               pjp参数
     * @param expressionString  表达式
     * @param desiredResultType 返回类型
     * @param argsMap           额外参数
     * @param <T>               泛型
     * @return spel表达式的结果值
     */
    public static <T> T spel(ProceedingJoinPoint pjp, String expressionString, Class<T> desiredResultType, Map<String, Object> argsMap) {
        String[] parameterNames = ((MethodSignature) pjp.getSignature()).getParameterNames();
        Object[] args = pjp.getArgs();
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (args.length > 0 && parameterNames != null) {
            for (int i = 0; i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        if (argsMap != null) {
            for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }
        return parser.parseExpression(expressionString).getValue(context, desiredResultType);
    }
}
