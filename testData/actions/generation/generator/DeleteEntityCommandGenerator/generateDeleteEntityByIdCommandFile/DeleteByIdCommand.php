<?php

namespace Foo\Bar\Command\Book;

use Exception;
use Foo\Bar\Model\BookModel;
use Foo\Bar\Model\BookModelFactory;
use Foo\Bar\Model\Data\BookData;
use Foo\Bar\Model\ResourceModel\BookResource;
use Magento\Framework\Exception\CouldNotDeleteException;
use Magento\Framework\Exception\NoSuchEntityException;
use Psr\Log\LoggerInterface;

/**
 * Delete Book by id Command.
 */
class DeleteByIdCommand
{
    /**
     * @var LoggerInterface
     */
    private LoggerInterface $logger;

    /**
     * @var BookModelFactory
     */
    private BookModelFactory $modelFactory;

    /**
     * @var BookResource
     */
    private BookResource $resource;

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
     * Delete Book.
     *
     * @param int $entityId
     *
     * @return void
     * @throws CouldNotDeleteException
     */
    public function execute(int $entityId): void
    {
        try {
            /** @var BookModel $model */
            $model = $this->modelFactory->create();
            $this->resource->load($model, $entityId, BookData::BOOK_ID);

            if (!$model->getData(BookData::BOOK_ID)) {
                throw new NoSuchEntityException(
                    __('Could not find Book with id: `%id`',
                        [
                            'id' => $entityId
                        ]
                    )
                );
            }

            $this->resource->delete($model);
        } catch (Exception $exception) {
            $this->logger->error(
                __('Could not delete Book. Original message: {message}'),
                [
                    'message' => $exception->getMessage(),
                    'exception' => $exception
                ]
            );
            throw new CouldNotDeleteException(__('Could not delete Book.'));
        }
    }
}
