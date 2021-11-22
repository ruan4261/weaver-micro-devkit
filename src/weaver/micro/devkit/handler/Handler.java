package weaver.micro.devkit.handler;

import weaver.soa.workflow.request.RequestInfo;

/**
 * 与流程{@code Action}接口一同使用
 * 使用{@code Action}的{@code execute}方法作为本接口的代理
 *
 * @author ruan4261
 */
public interface Handler {

    String handle(RequestInfo requestInfo) throws Throwable;

}
