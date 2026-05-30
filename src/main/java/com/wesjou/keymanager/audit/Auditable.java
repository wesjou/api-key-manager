package com.wesjou.keymanager.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    AuditAction action();
    AuditResourceType resourceType();
    int resourceIdArgIndex() default -1;
}
