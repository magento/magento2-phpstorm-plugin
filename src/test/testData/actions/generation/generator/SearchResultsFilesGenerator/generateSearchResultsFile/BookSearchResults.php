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
     * Set items list.
     *
     * @param array $items
     *
     * @return BookSearchResultsInterface
     */
    public function setItems(array $items): BookSearchResultsInterface
    {
        return parent::setItems($items);
    }

    /**
     * Get items list.
     *
     * @return array
     */
    public function getItems(): array
    {
        return parent::getItems();
    }
}
