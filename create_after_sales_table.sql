USE EyeCareHubDB;
GO

-- Drop the table if it exists to ensure a clean state
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'AfterSalesCases')
BEGIN
    DROP TABLE [dbo].[AfterSalesCases];
END
GO

CREATE TABLE [dbo].[AfterSalesCases] (
    [CaseId] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [OrderId] BIGINT NOT NULL,
    [CaseType] NVARCHAR(20) NOT NULL,
    [Status] NVARCHAR(20) NOT NULL DEFAULT 'NEW',
    [Reason] NVARCHAR(MAX) NULL,
    [ItemsJson] NVARCHAR(MAX) NULL,
    [EvidenceUrls] NVARCHAR(MAX) NULL,
    [RequestedBy] BIGINT NOT NULL,
    [HandledBy] BIGINT NULL,
    [RefundAmount] DECIMAL(12, 2) NULL,
    [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),
    [UpdatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_AfterSalesCases_Orders FOREIGN KEY (OrderId) REFERENCES Orders(OrderId),
    CONSTRAINT FK_AfterSalesCases_RequestedBy FOREIGN KEY (RequestedBy) REFERENCES Accounts(AccountId),
    CONSTRAINT FK_AfterSalesCases_HandledBy FOREIGN KEY (HandledBy) REFERENCES Accounts(AccountId)
);
GO
