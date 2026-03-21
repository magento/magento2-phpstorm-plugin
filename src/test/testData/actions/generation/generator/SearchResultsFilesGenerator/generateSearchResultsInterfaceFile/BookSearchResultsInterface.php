<?php

namespace Foo\Bar\Api\Data;

use Magento\Framework\Api\SearchResultsInterface;

/**
 * Book entity search result.
 */
interface BookSearchResultsInterface extends SearchResultsInterface
{
    /**
     * Set items.
     *
     * @param \Foo\Bar\Model\Data\BookData[] $items
     *
     * @return BookSearchResultsInterface
     */
    public function setItems(array $items): BookSearchResultsInterface;

    /**
     * Get items.
     *
     * @return \Foo\Bar\Model\Data\BookData[]
     */
    public function getItems(): array;
}
