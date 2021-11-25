<?php

namespace Foo\Bar\Model\Data;

use Foo\Bar\Api\Data\SampleInterface;
use Magento\Framework\DataObject;

class Sample extends DataObject implements SampleInterface
{
    /**
     * @inheritDoc
     */
    public function getSampleProperty()
    {
        return $this->getData(self::SAMPLE_PROPERTY);
    }

    /**
     * @inheritDoc
     */
    public function setSampleProperty($sampleProperty)
    {
        return $this->setData(self::SAMPLE_PROPERTY, $sampleProperty);
    }
}
