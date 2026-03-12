USE EyeCareHubDB;
GO

-- 1. Create inventory_locations table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_locations')
BEGIN
    CREATE TABLE [inventory_locations] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [name] NVARCHAR(200) NOT NULL,
        [code] NVARCHAR(50) NOT NULL UNIQUE,
        [location_type] NVARCHAR(20) NOT NULL,
        [address] NVARCHAR(500) NULL,
        [is_active] BIT NOT NULL DEFAULT 1,
        [created_at] DATETIME2 NOT NULL,
        [updated_at] DATETIME2 NOT NULL
    );
    PRINT 'Table inventory_locations created successfully.';
END
ELSE
BEGIN
    PRINT 'Table inventory_locations already exists.';
END
GO

-- 2. Create inventory_stocks table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_stocks')
BEGIN
    CREATE TABLE [inventory_stocks] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [location_id] BIGINT NOT NULL,
        [variant_id] BIGINT NOT NULL,
        [on_hand_qty] INT NOT NULL DEFAULT 0,
        [reserved_qty] INT NOT NULL DEFAULT 0,
        [created_at] DATETIME2 NOT NULL,
        [updated_at] DATETIME2 NOT NULL,
        CONSTRAINT [FK_InventoryStock_Location] FOREIGN KEY ([location_id]) REFERENCES [inventory_locations]([id]),
        CONSTRAINT [FK_InventoryStock_Variant] FOREIGN KEY ([variant_id]) REFERENCES [ProductVariants]([VariantId]),
        CONSTRAINT [UQ_InventoryStock_Location_Variant] UNIQUE ([location_id], [variant_id])
    );
    PRINT 'Table inventory_stocks created successfully.';
END
ELSE
BEGIN
    PRINT 'Table inventory_stocks already exists.';
END
GO
