<?php

namespace Foo\Bar\Service;

use Foo\Bar\Model\SimpleModelTwo;
use Foo\Bar\Model\SimpleModelOne;

/**
 * Simple service two description.
 */
class SimpleServiceTwo
{
    /**
     * Simple method description.
     *
     * @param SimpleModelTwo $simpleModelTwo
     * @param string|null $param2
     *
     * @return SimpleModelOne|null
     */
    public function execute(SimpleModelTwo $simpleModelTwo, ?string $param2 = null): ?SimpleModelOne
    {
        return null;
    }

    public function fetch(): void
    {
        return;
    }
}
