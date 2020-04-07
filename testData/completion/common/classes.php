<?php
namespace Magento\Framework\Option
{
    interface ArrayInterface {}
}

namespace Magento\Config\Model\Config\Source
{
    use Magento\Framework\Option\ArrayInterface;
    class Yesno implements ArrayInterface {}
}

namespace Magento\Backend\Model\Source
{
    use Magento\Framework\Option\ArrayInterface;
    class Roles implements ArrayInterface {}
}

namespace Magento\Customer\Model\Source
{
    use Magento\Framework\Option\ArrayInterface;
    class Roles implements ArrayInterface {}
}

namespace Magento\B2b\Model\Source
{
    use Magento\Framework\Option\ArrayInterface;
    class Roles implements ArrayInterface {}
}
