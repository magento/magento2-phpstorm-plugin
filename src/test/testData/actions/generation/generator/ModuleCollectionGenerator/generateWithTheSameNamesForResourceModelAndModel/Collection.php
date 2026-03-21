<?php

namespace Foo\Bar\Model\ResourceModel\Test;

use Foo\Bar\Model\ResourceModel\Test as ResourceModel;
use Foo\Bar\Model\Test as Model;
use Magento\Framework\Model\ResourceModel\Db\Collection\AbstractCollection;

class Collection extends AbstractCollection
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
        $this->_init(Model::class, ResourceModel::class);
    }
}
