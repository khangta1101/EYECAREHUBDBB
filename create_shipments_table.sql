USE EyeCareHubDB;
GO

-- Drop the table if it exists to ensure a clean state
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Shipments')
BEGIN
    DROP TABLE [dbo].[Shipments];
END
GO

CREATE TABLE [dbo].[Shipments] (
    [ShipmentId] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [OrderId] BIGINT NOT NULL UNIQUE,
    [Carrier] NVARCHAR(100) NULL,
    [TrackingNo] NVARCHAR(200) NULL,
    [TrackingUrl] NVARCHAR(500) NULL,
    [Status] NVARCHAR(20) NOT NULL DEFAULT 'CREATED',
    [EstimatedDelivery] DATETIME2 NULL,
    [ShippedAt] DATETIME2 NULL,
    [DeliveredAt] DATETIME2 NULL,
    [Note] NVARCHAR(MAX) NULL,
    [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),
    [UpdatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Shipments_Order FOREIGN KEY (OrderId) REFERENCES Orders(OrderId)
);
GO
