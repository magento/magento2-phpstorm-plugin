<?php

namespace Foo\Bar\Plugin;

use Foo\Bar\Service\SimpleService;

class TestPlugin
{
    /**
     * @param SimpleService $subject
     * @param int $param1
     * @param string $param2
     * @return array
     */
    public function beforeExecute(SimpleService $subject, int $param1, string $param2)
    {
        // TODO: Implement plugin method.
        return [$param1, $param2];
    }

    /**
     * @param SimpleService $subject
     * @param callable $proceed
     * @param int $param1
     * @param string $param2
     */
    public function aroundExecute(SimpleService $subject, callable $proceed, int $param1, string $param2)
    {
        // TODO: Implement plugin method.
        return $proceed($param1, $param2);
    }

    /**
     * @param SimpleService $subject
     * @param $result
     * @param int $param1
     * @param string $param2
     */
    public function afterExecute(SimpleService $subject, $result, int $param1, string $param2)
    {
        // TODO: Implement plugin method.
        return $result;
    }
}