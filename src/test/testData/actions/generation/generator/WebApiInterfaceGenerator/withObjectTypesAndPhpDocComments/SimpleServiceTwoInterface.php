<?php

namespace Foo\Bar\Api;

use Foo\Bar\Model\SimpleModelOne;
use Foo\Bar\Model\SimpleModelTwo;

/**
 * Simple service two description.
 *
 * @api
 */
interface SimpleServiceTwoInterface
{
    /**
     * Simple method description.
     * @param \Foo\Bar\Model\SimpleModelTwo $simpleModelTwo
     * @param string|null $param2
     * @return \Foo\Bar\Model\SimpleModelOne|null
     */
    public function execute(SimpleModelTwo $simpleModelTwo, ?string $param2 = null): ?SimpleModelOne;

    /**
     * TODO: need to describe this method.
     * @return void
     */
    public function fetch(): void;
}
