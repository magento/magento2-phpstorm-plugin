<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
namespace Magento\Framework\Logger;

/**
 * Describes a logger instance.
 */
interface LoggerInterface
{
    /**
     * Logs with an arbitrary level.
     *
     * @param mixed   $level
     * @param string  $message
     * @param mixed[] $context
     *
     * @return void
     */
    public function log($level, $message, array $context = array());
}
