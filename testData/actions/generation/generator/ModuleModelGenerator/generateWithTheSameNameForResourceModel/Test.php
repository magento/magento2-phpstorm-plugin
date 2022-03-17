<?php

namespace Foo\Bar\Model;

use Foo\Bar\Model\ResourceModel\Test as ResourceModel;
use Magento\Framework\Model\AbstractModel;

class Test extends AbstractModel
{
    /**
     * @var string
     */
    protected $_eventPrefix = 'my_table_model';

    /**
     * Initialize magento model.
     *
     * @return void
     */
    protected function _construct()
    {
        $this->_init(ResourceModel::class);
    }
}
