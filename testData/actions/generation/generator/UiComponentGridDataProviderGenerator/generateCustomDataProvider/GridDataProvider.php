<?php

namespace Foo\Bar\Ui\Listing;

use Magento\Framework\View\Element\UiComponent\DataProvider\DataProvider;

/**
 * DataProvider component.
 */
class GridDataProvider extends DataProvider
{
    /**
     * @inheritDoc
     */
    public function getData()
    {
        //TODO: implement data retrieving here based on search criteria
        return [
            []
        ];
    }
}
