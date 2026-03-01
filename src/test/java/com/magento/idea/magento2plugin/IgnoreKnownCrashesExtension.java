/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import com.intellij.testFramework.LoggedErrorProcessor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.util.EnumSet;
import java.util.Set;

/**
 * JUnit 5 Extension to swallow known background crashes and upstream issues during tests.
 * This replaces the manual logic previously implemented in BaseProjectTestCase.
 */
public class IgnoreKnownCrashesExtension implements BeforeEachCallback, AfterEachCallback, InvocationInterceptor {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(IgnoreKnownCrashesExtension.class);
    private static final String PREVIOUS_UNCAUGHT_HANDLER = "previousUncaughtHandler";

    @Override
    public void beforeEach(ExtensionContext context) {
        // Handle Uncaught Exceptions (background threads)
        Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        context.getStore(NAMESPACE).put(PREVIOUS_UNCAUGHT_HANDLER, previousHandler);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e != null) {
                Throwable cur = e;
                while (cur != null) {
                    String m = cur.getMessage();
                    String st = java.util.Arrays.toString(cur.getStackTrace());
                    if ((m != null && (m.contains("kotlin.sequences.SequencesKt.sequenceOf") || m.contains("fleet.kernel")))
                            || (st != null && st.contains("fleet.kernel"))) {
                        return; // swallow only this known background crash to keep tests green
                    }
                    cur = cur.getCause();
                }
            }
            if (previousHandler != null) {
                previousHandler.uncaughtException(t, e);
            }
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Restore UncaughtExceptionHandler
        Thread.UncaughtExceptionHandler previousHandler = context.getStore(NAMESPACE).get(PREVIOUS_UNCAUGHT_HANDLER, Thread.UncaughtExceptionHandler.class);
        if (previousHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(previousHandler);
        } else {
             Thread.setDefaultUncaughtExceptionHandler(null);
        }
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        wrapWithLoggedErrorProcessor(invocation);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        wrapWithLoggedErrorProcessor(invocation);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        wrapWithLoggedErrorProcessor(invocation);
    }

    private void wrapWithLoggedErrorProcessor(Invocation<Void> invocation) throws Throwable {
        final Throwable[] throwable = new Throwable[1];
        LoggedErrorProcessor.executeWith(new LoggedErrorProcessor() {
            private boolean shouldIgnore(String message, Throwable t) {
                if (message != null && (message.contains("filetype.phar.display.name") || message.contains("messages.PhpBundle")
                        || message.contains("kotlin.sequences.SequencesKt.sequenceOf") || message.contains("fleet.kernel"))) {
                    return true;
                }
                if (t != null) {
                    Throwable cur = t;
                    while (cur != null) {
                        String m = cur.getMessage();
                        if (m != null && (m.contains("filetype.phar.display.name") || m.contains("messages.PhpBundle")
                                || m.contains("kotlin.sequences.SequencesKt.sequenceOf") || m.contains("fleet.kernel"))) {
                            return true;
                        }
                        cur = cur.getCause();
                    }
                }
                return false;
            }

            @Override
            public @NotNull Set<Action> processError(@NotNull String category, @NotNull String message, @NotNull String[] details, @NotNull Throwable t) {
                if (shouldIgnore(message, t)) {
                    return EnumSet.noneOf(Action.class); // ignore only this known upstream issue
                }
                return super.processError(category, message, details, t);
            }
        }, () -> {
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throwable[0] = e;
            }
        });

        if (throwable[0] != null) {
            throw throwable[0];
        }
    }
}
