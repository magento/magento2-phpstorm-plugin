<?php

namespace Foo\Bar\Ui\Component\Listing;

use Magento\Framework\View\Element\UiComponent\DataProvider\DataProvider;

/**
 * DataProvider component.
 */
class GridDataProvider extends DataProvider
{
    /**
     * Get data.
     *
     * @return array
     */
    public function getData()
    {
        //TODO: implement data retrieving here based on search criteria
        return [
            []
        ];
    }
}
