<?php

namespace Foo\Bar\Model\ResourceModel\TestModel;

use Foo\Bar\Model\ResourceModel\TestResource;
use Foo\Bar\Model\TestModel;
use Magento\Framework\Model\ResourceModel\Db\Collection\AbstractCollection;

class TestCollection extends AbstractCollection
{
    /**
     * @var string
     */
    protected $_eventPrefix = 'my_table_collection';

    /**
     * Initialize collection model.
     */
    protected function _construct()
    {
        $this->_init(TestModel::class, TestResource::class);
    }
}
