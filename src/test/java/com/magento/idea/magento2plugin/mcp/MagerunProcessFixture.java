/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp;

/** Standalone subprocess for process-runner tests, using only the JDK on every OS. */
public final class MagerunProcessFixture {
    private MagerunProcessFixture() {
    }

    public static void main(final String[] args) throws Exception {
        switch (args[0]) {
            case "echo":
                System.out.print(args[1]);
                break;
            case "sleep":
                Thread.sleep(30_000);
                break;
            case "output":
                while (true) {
                    System.out.print("long output data");
                    System.err.print("long error data");
                }
            case "stdin":
                System.in.transferTo(System.out);
                System.out.print("done");
                break;
            default:
                throw new IllegalArgumentException("Unknown fixture mode: " + args[0]);
        }
    }
}
