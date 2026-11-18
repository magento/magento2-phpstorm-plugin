#!/usr/bin/env python3
"""Deterministic container-launcher fixture: only metadata, never Magento operations."""
import json
from pathlib import Path
import sys

root = Path.cwd()
arguments = sys.argv[1:]
with (root / 'metadata-calls.jsonl').open('a') as log:
    log.write(json.dumps(arguments) + '\n')
if arguments[:2] == ['test', '-f']:
    raise SystemExit(0 if (root / arguments[2]).is_file() else 1)
if arguments[:2] != ['php', 'bin/n98-magerun2.phar']:
    raise SystemExit(93)
arguments = arguments[2:]
if arguments[0] == '--version':
    print('n98-magerun2 version 10.1.0')
elif arguments[0] == 'list':
    print((root / '.metadata/catalogue.json').read_text())
elif arguments[:2] == ['help', 'sys:info']:
    print((root / '.metadata/sys-info-help.json').read_text())
elif arguments[:2] == ['help', 'mcp:server:start']:
    print(json.dumps({'name': 'mcp:server:start', 'definition': {'arguments': {},
        'options': {'include': {'name': '--include'}, 'exclude': {'name': '--exclude'}}}}))
else:
    (root / 'unexpected-operation').write_text(json.dumps(arguments))
    raise SystemExit(93)
