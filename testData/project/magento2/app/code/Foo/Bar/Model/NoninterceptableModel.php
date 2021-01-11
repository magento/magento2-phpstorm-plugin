<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
namespace Foo\Bar\Model;

class NoninterceptableModel implements \Magento\Framework\ObjectManager\NoninterceptableInterface
{
    public function log($level, $message, array $context = array())
    {
    }
}
