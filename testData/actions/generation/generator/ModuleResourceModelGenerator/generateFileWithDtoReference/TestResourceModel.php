<?php

namespace Foo\Bar\Model\ResourceModel;

use Foo\Bar\Model\Data\EntityData;
use Magento\Framework\Model\ResourceModel\Db\AbstractDb;

class TestResourceModel extends AbstractDb
{
    /**
     * @var string
     */
    protected $_eventPrefix = 'my_table_resource_model';

    /**
     * Initialize resource model.
     */
    protected function _construct()
    {
        $this->_init('my_table', EntityData::ENTITY_ID);
        $this->_useIsObjectNew = true;
    }
}
