package com.yupi.yuaiagent.config;

/**
 * 线程级上下文持有者，让 Tool 能获取当前用户和对话信息，
 * 用于 COS 文件上传时按用户/对话组织存储路径。
 * Controller 在每次请求前 set，请求结束后 clear。
 */
public class AgentContextHolder {

    private static final ThreadLocal<AgentContext> CONTEXT = new ThreadLocal<>();

    public static void set(AgentContext ctx) {
        CONTEXT.set(ctx);
    }

    public static AgentContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public record AgentContext(Long userId, String chatId) {}
}
