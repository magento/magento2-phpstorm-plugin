<?php

namespace Foo\Bar\Model\ResourceModel;

use Magento\Framework\Model\ResourceModel\Db\AbstractDb;

class TestResourceModel extends AbstractDb
{
    /**
     * @var string
     */
    protected $_eventPrefix = 'my_table_resource_model';

    /**
     * @inheritdoc
     */
    protected function _construct()
    {
        $this->_init('my_table', 'entity_id');
    }
}
