<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
namespace Foo\Bar\Model;

class Logger implements Magento\Framework\Logger\LoggerInterface
{
    public function log($level, $message, array $context = array())
    {
    }
}
