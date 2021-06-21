<?php

namespace Foo\Bar\Command\Book;

use Exception;
use Foo\Bar\Model\BookModel;
use Foo\Bar\Model\BookModelFactory;
use Foo\Bar\Model\Data\BookData;
use Foo\Bar\Model\ResourceModel\BookResource;
use Magento\Framework\DataObject;
use Magento\Framework\Exception\CouldNotSaveException;
use Psr\Log\LoggerInterface;

/**
 * Save Book Command.
 */
class SaveCommand
{
    /**
     * @var LoggerInterface
     */
    private $logger;

    /**
     * @var BookModelFactory
     */
    private $modelFactory;

    /**
     * @var BookResource
     */
    private $resource;

    /**
     * @param LoggerInterface $logger
     * @param BookModelFactory $modelFactory
     * @param BookResource $resource
     */
    public function __construct(
        LoggerInterface $logger,
        BookModelFactory $modelFactory,
        BookResource $resource
    )
    {
        $this->logger = $logger;
        $this->modelFactory = $modelFactory;
        $this->resource = $resource;
    }

    /**
     * Save Book.
     *
     * @param BookData|DataObject $book
     *
     * @return int
     * @throws CouldNotSaveException
     */
    public function execute(BookData $book): int
    {
        try {
            /** @var BookModel $model */
            $model = $this->modelFactory->create();
            $model->addData($book->getData());
            $model->setHasDataChanges(true);

            if (!$model->getData(BookData::BOOK_ID)) {
                $model->isObjectNew(true);
            }
            $this->resource->save($model);
        } catch (Exception $exception) {
            $this->logger->error(
                __('Could not save Book. Original message: {message}'),
                [
                    'message' => $exception->getMessage(),
                    'exception' => $exception
                ]
            );
            throw new CouldNotSaveException(__('Could not save Book.'));
        }

        return (int)$model->getEntityId();
    }
}
