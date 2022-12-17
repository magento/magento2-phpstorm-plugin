<?php

namespace Foo\Bar\Block\Form\Book;

use Foo\Bar\Model\Data\BookData;
use Magento\Framework\View\Element\UiComponent\Control\ButtonProviderInterface;

/**
 * Delete entity button.
 */
class DeleteBlock extends GenericButton implements ButtonProviderInterface
{
    /**
     * Retrieve Delete button settings.
     *
     * @return array
     */
    public function getButtonData(): array
    {
        if (!$this->getAmazingId()) {
            return [];
        }

        return $this->wrapButtonSettings(
            'Delete',
            'delete',
            sprintf("deleteConfirm('%s', '%s')",
                __('Are you sure you want to delete this book?'),
                $this->getUrl(
                    '*/*/delete',
                    [BookData::BOOK_ID => $this->getBookId()]
                )
            ),
            [],
            20
        );
    }
}
