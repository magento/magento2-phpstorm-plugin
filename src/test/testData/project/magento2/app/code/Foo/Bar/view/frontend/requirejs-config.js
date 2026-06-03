var config = {
    map: {
        '*': {
            testFile: 'Foo_Bar/js/file',
        }
    },
    paths: {
        'testFile2': 'Foo_Bar/js/file2'
    },
    config: {
        mixins: {
            'Foo_Bar/js/file': {
                'Foo_Bar/js/file-mixin': true
            },
            'Foo_Bar/js/file2': {
                'Foo_Bar/js/object-mixin': true,
                'Foo_Bar/js/widget-mixin': true,
                'Foo_Bar/js/function-mixin': true
            }
        }
    }
}
