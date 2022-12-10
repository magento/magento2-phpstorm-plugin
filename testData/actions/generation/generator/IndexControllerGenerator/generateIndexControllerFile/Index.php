<?php

namespace Foo\Bar\Controller\Adminhtml\Book;

use Magento\Backend\App\Action;
use Magento\Framework\App\Action\HttpGetActionInterface;
use Magento\Framework\App\ResponseInterface;
use Magento\Framework\Controller\ResultFactory;
use Magento\Framework\Controller\ResultInterface;

/**
 * Book backend index (list) controller.
 */
class Index extends Action implements HttpGetActionInterface
{
    /**
     * Authorization level of a basic admin session.
     */
    public const ADMIN_RESOURCE = 'Foo_Bar::book_management';

    /**
     * Execute action based on request and return result.
     *
     * @return ResultInterface|ResponseInterface
     */
    public function execute()
    {
        $resultPage = $this->resultFactory->create(ResultFactory::TYPE_PAGE);

        $resultPage->setActiveMenu('Foo_Bar::book_management_menu');
        $resultPage->addBreadcrumb(__('Book'), __('Book'));
        $resultPage->addBreadcrumb(__('Manage Books'), __('Manage Books'));
        $resultPage->getConfig()->getTitle()->prepend(__('Book List'));

        return $resultPage;
    }
}
