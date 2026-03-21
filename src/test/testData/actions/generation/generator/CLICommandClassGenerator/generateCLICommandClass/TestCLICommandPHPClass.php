<?php

namespace Foo\Bar\Console\Command;

use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

class TestCLICommandPHPClass extends Command
{
    /**
     * Initialization of the command.
     */
    protected function configure()
    {
        $this->setName('bar:test-command');
        $this->setDescription('This is the test command');
        parent::configure();
    }

    /**
     * CLI command description.
     *
     * @param InputInterface $input
     * @param OutputInterface $output
     *
     * @return void
     */
    protected function execute(InputInterface $input, OutputInterface $output): void
    {
        // todo: implement CLI command logic here
    }
}
