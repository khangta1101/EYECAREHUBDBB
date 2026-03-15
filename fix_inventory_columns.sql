USE EyeCareHubDB;
GO

-- Add missing UpdatedAt column to InventoryLocations
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('[dbo].[InventoryLocations]') AND name = 'UpdatedAt')
BEGIN
    ALTER TABLE [dbo].[InventoryLocations] ADD [UpdatedAt] DATETIME2 NOT NULL DEFAULT GETDATE();
END
GO

-- Add missing CreatedAt column to InventoryStocks
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('[dbo].[InventoryStocks]') AND name = 'CreatedAt')
BEGIN
    ALTER TABLE [dbo].[InventoryStocks] ADD [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE();
END
GO
