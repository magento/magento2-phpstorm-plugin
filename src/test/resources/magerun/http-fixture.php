<?php
// Deterministic metadata fixture. Invoked by the production process runner, never by test doubles.
$arguments = array_slice($argv, 1);
file_put_contents(__DIR__ . '/calls.jsonl', json_encode([
    'arguments' => $arguments,
    'cwd' => getcwd(),
    'noColor' => getenv('NO_COLOR'),
]) . "\n", FILE_APPEND | LOCK_EX);
if (file_exists(__DIR__ . '/fail-probe')) {
    fwrite(STDERR, "Fixture runtime unavailable\n");
    exit(7);
}
$args = array_values(array_filter($arguments, static fn($arg) => !str_starts_with($arg, '--root-dir=')));
if ($args[0] === '--version') {
    echo "n98-magerun2 version 9.0.0-test\n";
} elseif ($args[0] === 'list') {
    echo json_encode([
        'application' => ['name' => 'n98-magerun2', 'version' => '9.0.0-test'],
        'commands' => [
            ['name' => 'sys:info', 'description' => 'System information', 'usage' => ['sys:info', 'si']],
            ['name' => 'shop:custom', 'description' => 'Project custom command'],
            ['name' => 'mcp:server:start', 'description' => 'Start MCP server'],
            ['name' => 'hidden:command', 'description' => 'Hidden', 'hidden' => true],
        ],
    ]);
} elseif ($args[0] === 'help' && in_array($args[1], ['sys:info', 'mcp:server:start'], true)) {
    echo json_encode([
        'name' => $args[1],
        'definition' => ['arguments' => new stdClass(), 'options' => [
            'include' => ['name' => '--include'], 'exclude' => ['name' => '--exclude'],
        ]],
    ]);
} else {
    file_put_contents(__DIR__ . '/application-command-executed', json_encode($arguments));
    fwrite(STDERR, "Only version, list and help are permitted in this fixture\n");
    exit(93);
}
