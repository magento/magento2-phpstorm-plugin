<?php

declare(strict_types=1);

namespace PHPSTORM_META {

    override(\Hyva\Theme\Model\ViewModelRegistry::require(0), map(['' => '@']));

    override(\Hyva\Checkout\Model\CustomConditionFactory::create(0), map(['' => '@']));
    override(
        \Hyva\Checkout\Model\Magewire\Component\EvaluationResultFactory::create(0),
        map(['' => '@'])
    );

    override(\Magewirephp\Magewire\Model\Action\Type\Factory::create(0), map(['' => '@']));

    override(
        \Hyva\Checkout\Model\Form\EntityFormInterface::getFactoryFor(0),
        map(
            [
                'elements' => \Hyva\Checkout\Model\Form\EntityFormFactory::class,
                'fields' => \Hyva\Checkout\Model\Form\EntityFormFieldFactory::class,
                'eav_fields' => \Hyva\Checkout\Model\Form\EntityField\EavAttributeFieldFactory::class,
            ]
        )
    );
    override(
        \Hyva\Checkout\Model\Form\AbstractEntityForm::getFactoryFor(0),
        map(
            [
                'elements' => \Hyva\Checkout\Model\Form\EntityFormFactory::class,
                'fields' => \Hyva\Checkout\Model\Form\EntityFormFieldFactory::class,
                'eav_fields' => \Hyva\Checkout\Model\Form\EntityField\EavAttributeFieldFactory::class,
            ]
        )
    );

    registerArgumentsSet(
        'hyva_checkout_form_factories',
        'elements',
        'fields',
        \Hyva\Checkout\Model\Form\EntityField\EavAttributeFieldFactory::ACCESSOR
    );
    expectedArguments(
        \Hyva\Checkout\Model\Form\EntityFormInterface::getFactoryFor(),
        0,
        argumentsSet('hyva_checkout_form_factories')
    );
    expectedArguments(
        \Hyva\Checkout\Model\Form\AbstractEntityForm::getFactoryFor(),
        0,
        argumentsSet('hyva_checkout_form_factories')
    );

    expectedArguments(
        \Hyva\Checkout\Model\Checkout\Step::getUpdates(),
        0,
        \Hyva\Checkout\Model\Checkout\Step::UPDATE_TYPE_LAYOUT,
        \Hyva\Checkout\Model\Checkout\Step::UPDATE_TYPE_DEFAULT,
        \Hyva\Checkout\Model\Checkout\Step::UPDATE_TYPE_CUSTOM
    );

    registerArgumentsSet(
        'hyva_checkout_evaluation_types',
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Batch::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Deferrable::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\ErrorMessage::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Event::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Executable::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\MessageDialog::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\NavigationTask::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Redirect::TYPE,
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Validation::TYPE
    );
    expectedArguments(
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Batch::clearByType(),
        0,
        argumentsSet('hyva_checkout_evaluation_types')
    );

    registerArgumentsSet(
        'magewire_message_types',
        \Magewirephp\Magewire\Model\Element\FlashMessage::ERROR,
        \Magewirephp\Magewire\Model\Element\FlashMessage::WARNING,
        \Magewirephp\Magewire\Model\Element\FlashMessage::NOTICE,
        \Magewirephp\Magewire\Model\Element\FlashMessage::SUCCESS
    );
    expectedArguments(
        \Magewirephp\Magewire\Model\Concern\FlashMessage::dispatchMessage(),
        0,
        argumentsSet('magewire_message_types')
    );
    expectedArguments(
        \Hyva\Checkout\Model\Magewire\Component\Evaluation\Concern\MessagingCapabilities::asCustomType(),
        0,
        argumentsSet('magewire_message_types')
    );

}
