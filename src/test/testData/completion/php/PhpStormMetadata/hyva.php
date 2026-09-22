<?php
/*
 * Minimal declarations, without implementation-based type inference.
 * Form factories: https://docs.hyva.io/hyva-checkout/devdocs/form-api/form-construction.html
 * Evaluation API: https://docs.hyva.io/hyva-checkout/devdocs/evaluation-api/evaluation-result-types.html
 * Magewire signatures: https://github.com/magewirephp/magewire/tree/1.x/src/Model
 */

namespace Hyva\Theme\Model {
    class ViewModelRegistry {
        public function require(string $class) {}
    }
}

namespace Hyva\Checkout\Model {
    class CustomConditionFactory {
        public function create(string $type, array $arguments = []) {}
    }
}

namespace Hyva\Checkout\Model\Form {
    interface EntityFormInterface {
        public function getFactoryFor(string $name): object;
    }
    abstract class AbstractEntityForm implements EntityFormInterface {
        public function getFactoryFor(string $name): object {}
    }
    class EntityFormFactory {}
    class EntityFormFieldFactory {}
}

namespace Hyva\Checkout\Model\Form\EntityField {
    class EavAttributeFieldFactory {
        const ACCESSOR = 'eav_fields';
    }
}

namespace Hyva\Checkout\Model\Magewire\Component {
    class EvaluationResultFactory {
        public function create(string $type, array $arguments = []) {}
        public function createCustom(string $type) {}
    }
}

namespace Hyva\Checkout\Model\Magewire\Component\Evaluation {
    class Batch {
        const TYPE = 'batch';
        public function clearByType(string $type) {}
    }
    class Redirect {
        const TYPE = 'redirect';
    }
}

namespace Hyva\Checkout\Model\Checkout {
    class Step {
        const UPDATE_TYPE_LAYOUT = 'layout';
        const UPDATE_TYPE_DEFAULT = 'default';
        const UPDATE_TYPE_CUSTOM = 'custom';
        public function getUpdates(string $type) {}
    }
}

namespace Hyva\Checkout\Model\Magewire\Component\Evaluation\Concern {
    trait MessagingCapabilities {
        public function asCustomType(string $type) {}
    }
}

namespace Magewirephp\Magewire\Model\Action\Type {
    class Factory {
        public function create(string $type) {}
    }
}

namespace Magewirephp\Magewire\Model\Element {
    class FlashMessage {
        const ERROR = 'error';
        const WARNING = 'warning';
        const NOTICE = 'notice';
        const SUCCESS = 'success';
    }
}

namespace Magewirephp\Magewire\Model\Concern {
    trait FlashMessage {
        public function dispatchMessage(string $type, $message): \Magewirephp\Magewire\Model\Element\FlashMessage {}
    }
}

namespace Example {
    class Form extends \Hyva\Checkout\Model\Form\AbstractEntityForm {}
    class Component {
        use \Magewirephp\Magewire\Model\Concern\FlashMessage;
    }
    class Message {
        use \Hyva\Checkout\Model\Magewire\Component\Evaluation\Concern\MessagingCapabilities;
    }
}
