<?php

namespace Foo\Bar\Api\Data;

interface SampleInterface
{
    /**
     * String constants for property names
     */
    const SAMPLE_PROPERTY = "sample_property";

    /**
     * @return string
     */
    public function getSampleProperty();

    /**
     * @param string $sampleProperty
     * @return $this
     */
    public function setSampleProperty($sampleProperty);
}
