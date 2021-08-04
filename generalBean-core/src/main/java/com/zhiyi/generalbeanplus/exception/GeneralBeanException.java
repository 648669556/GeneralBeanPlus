package com.zhiyi.generalbeanplus.exception;

public class GeneralBeanException extends RuntimeException{
    public GeneralBeanException() {
    }

    public GeneralBeanException(String message) {
        super(message);
    }

    public GeneralBeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralBeanException(Throwable cause) {
        super(cause);
    }

    protected GeneralBeanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
