package ore.guard.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки классов проверок
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Check {
    /**
     * Название проверки
     */
    String name();

    /**
     * Описание проверки
     */
    String description();

    /**
     * На сколько тиков откатить при флаге
     */
    int setback();

    /**
     * Экспериментальная ли проверка (не флагать, только тестировать)
     */
    boolean experimental() default false;
}

