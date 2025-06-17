package io.github.notstirred.dasm.util;

import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.notify.Notification;
import lombok.Getter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;

import static io.github.notstirred.dasm.util.Format.format;
import static io.github.notstirred.dasm.util.Format.formatObjectType;

public class NotifyStack {
    private final List<String> stack;
    @Getter
    private final List<Notification> notifications;

    public NotifyStack() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    private NotifyStack(List<String> stack, List<Notification> notifications) {
        this.stack = stack;
        this.notifications = notifications;
    }

    public static NotifyStack of(ClassNode classNode) {
        NotifyStack notifyStack = new NotifyStack();
        return notifyStack.push(classNode);
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
        ArrayList<String> newStack = new ArrayList<>(this.stack);
        newStack.add(format(classNode));
        return new NotifyStack(newStack, this.notifications);
    }

    public NotifyStack push(MethodNode methodNode) {
        ArrayList<String> newStack = new ArrayList<>(this.stack);
        newStack.add(format(methodNode));
        return new NotifyStack(newStack, this.notifications);
    }

    public NotifyStack push(FieldNode fieldNode) {
        ArrayList<String> newStack = new ArrayList<>(this.stack);
        newStack.add(format(fieldNode));
        return new NotifyStack(newStack, this.notifications);
    }

    public NotifyStack push(Type type) {
        assert type.getSort() == Type.OBJECT;
        ArrayList<String> newStack = new ArrayList<>(this.stack);
        newStack.add(formatObjectType(type));
        return new NotifyStack(newStack, this.notifications);
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
}
