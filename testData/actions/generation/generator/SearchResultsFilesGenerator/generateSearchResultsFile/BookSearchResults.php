<?php

namespace Foo\Bar\Model;

use Foo\Bar\Api\Data\BookSearchResultsInterface;
use Magento\Framework\Api\SearchResults;

/**
 * Book entity search results implementation.
 */
class BookSearchResults extends SearchResults implements BookSearchResultsInterface
{
    /**
     * @inheritDoc
     */
    public function setItems(array $items): BookSearchResultsInterface
    {
        return parent::setItems($items);
    }

    /**
     * @inheritDoc
     */
    public function getItems(): array
    {
        return parent::getItems();
    }
}
