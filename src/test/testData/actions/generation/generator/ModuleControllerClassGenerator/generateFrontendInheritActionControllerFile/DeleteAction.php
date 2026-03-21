<?php

namespace Foo\Bar\Controller\Entity;

use Magento\Framework\App\Action\Action;
use Magento\Framework\App\Action\HttpDeleteActionInterface;
use Magento\Framework\App\ResponseInterface;
use Magento\Framework\Controller\ResultInterface;
use Magento\Framework\Exception\NotFoundException;

class DeleteAction extends Action implements HttpDeleteActionInterface
{
    /**
     * Execute action based on request and return result
     *
     * @return ResultInterface|ResponseInterface
     * @throws NotFoundException
     */
    public function execute()
    {
        // TODO: Implement execute method.
    }
}
