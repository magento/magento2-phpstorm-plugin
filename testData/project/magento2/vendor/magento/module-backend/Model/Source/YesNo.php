<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
namespace Magento\Backend\Model\Source;

use Magento\Framework\Option\ArrayInterface;
use Magento\Framework\Logger\LoggerInterface;

class YesNo implements ArrayInterface
{
    /**
     * @var LoggerInterface
     */
    private $logger;

    /**
     * Constant to hold test string
     */
    public const TEST_STRING = "Test string";

    /**
     * YesNo constructor.
     *
     * @param LoggerInterface $logger
     */
    public function __construct(
        LoggerInterface $logger
    ) {
        $this->logger = $logger;
    }
}
