USE EyeCareHubDB;
GO

-- Drop the table if it exists to ensure a clean state
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'FulfillmentTasks')
BEGIN
    DROP TABLE [dbo].[FulfillmentTasks];
END
GO

CREATE TABLE [dbo].[FulfillmentTasks] (
    [FulfillmentTaskId] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [OrderId] BIGINT NOT NULL,
    [OrderItemId] BIGINT NULL,
    [TaskType] NVARCHAR(30) NOT NULL,
    [Status] NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    [AssignedTo] BIGINT NULL,
    [Note] NVARCHAR(MAX) NULL,
    [StartedAt] DATETIME2 NULL,
    [DoneAt] DATETIME2 NULL,
    [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),
    [UpdatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_FulfillmentTasks_Order FOREIGN KEY (OrderId) REFERENCES Orders(OrderId),
    CONSTRAINT FK_FulfillmentTasks_OrderItem FOREIGN KEY (OrderItemId) REFERENCES OrderItems(OrderItemId),
    CONSTRAINT FK_FulfillmentTasks_Account FOREIGN KEY (AssignedTo) REFERENCES Accounts(AccountId)
);
GO
