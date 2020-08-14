package weaver.interfaces.micro.devkit.exception.runtime;

import weaver.interfaces.micro.devkit.exception.abs.AbstractRuntimeException;

/**
 * 流程Action接口无法继续执行，退回流程，继承自{@code AbstractException}
 *
 * @author ruan4261
 */
public class ActionStopException extends AbstractRuntimeException {

    public ActionStopException() {
        super();
    }

    public ActionStopException(String message) {
        super(message);
    }

    public ActionStopException(Throwable throwable) {
        super(throwable);
    }

}
