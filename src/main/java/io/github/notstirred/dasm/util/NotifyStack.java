package io.github.notstirred.dasm.util;

import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.notify.Notification;
import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;

import static io.github.notstirred.dasm.util.Format.format;

public class NotifyStack implements AutoCloseable {
    private final List<String> stack = new ArrayList<>();
    @Getter
    private final List<Notification> notifications = new ArrayList<>();

    public static NotifyStack of(ClassNode classNode) {
        NotifyStack notifyStack = new NotifyStack();
        notifyStack.push(classNode);
        return notifyStack;
    }

    /**
     * Adds a message about the parsed data
     * <p>The notification message will be modified to include the stack's location</p>
     */
    public void notify(Notification notification) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        this.stack.forEach(stringJoiner::add);
        notification.message = stringJoiner.add("->").add(notification.message).toString();
        this.notifications.add(notification);
    }

    public void notifyFromException(DasmException e) {
        this.notify(new Notification(e.getMessage(), Notification.Kind.ERROR, e.getClass()));
    }

    public NotifyStack push(ClassNode classNode) {
        this.stack.add(format(classNode));
        return this;
    }

    public NotifyStack push(MethodNode methodNode) {
        this.stack.add(format(methodNode));
        return this;
    }

    public NotifyStack push(FieldNode fieldNode) {
        this.stack.add(format(fieldNode));
        return this;
    }

    public NotifyStack pop() {
        this.stack.remove(this.stack.size() - 1);
        return this;
    }

    public boolean hasError() {
        return this.hasMessage(Notification.Kind.ERROR);
    }

    public boolean hasMessage(Notification.Kind minimumKind) {
        return this.notifications.stream().anyMatch(e -> e.kind.isAtLeast(minimumKind));
    }

    public NotifyStack join(NotifyStack other) {
        this.notifications.addAll(other.notifications);
        return this;
    }

    public static Collector<NotifyStack, ?, NotifyStack> joining() {
        return Collector.of(NotifyStack::new, NotifyStack::join, NotifyStack::join, Collector.Characteristics.IDENTITY_FINISH);
    }

    @Override
    public void close() {
        this.pop();
    }
}
