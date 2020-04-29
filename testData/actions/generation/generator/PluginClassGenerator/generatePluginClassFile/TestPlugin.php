<?php


namespace Foo\Bar\Plugin;


use Foo\Bar\Service\SimpleService;

class TestPlugin
{

    /**
     * @param \Foo\Bar\Service\SimpleService $subject
     * @param int $param1
     * @param string $param2
     * @return array
     */
    public function beforeExecute(\Foo\Bar\Service\SimpleService $subject, int $param1, string $param2)
    {
        // TODO: Implement plugin method.
        return [$param1, $param2];
    }

    /**
     * @param \Foo\Bar\Service\SimpleService $subject
     * @param callable $proceed
     * @param int $param1
     * @param string $param2
     */
    public function aroundExecute(\Foo\Bar\Service\SimpleService $subject, callable $proceed, int $param1, string $param2)
    {
        // TODO: Implement plugin method.
        return $proceed($param1, $param2);
    }

    /**
     * @param \Foo\Bar\Service\SimpleService $subject
     * @param $result
     * @param int $param1
     * @param string $param2
     */
    public function afterExecute(\Foo\Bar\Service\SimpleService $subject, $result, int $param1, string $param2)
    {
        // TODO: Implement plugin method.
        return $result;
    }
}