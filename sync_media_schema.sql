USE EyeCareHubDB;
GO

-- 1. Create ProductMedia table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ProductMedia')
BEGIN
    CREATE TABLE [ProductMedia] (
        [MediaId] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [ProductId] BIGINT NOT NULL,
        [VariantId] BIGINT NULL,
        [MediaType] NVARCHAR(20) NOT NULL DEFAULT 'IMAGE',
        [Url] NVARCHAR(500) NOT NULL,
        [SortOrder] INT NOT NULL DEFAULT 0,
        [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [FK_ProductMedia_Product] FOREIGN KEY ([ProductId]) REFERENCES [Products]([ProductId]),
        CONSTRAINT [FK_ProductMedia_Variant] FOREIGN KEY ([VariantId]) REFERENCES [ProductVariants]([VariantId])
    );
    PRINT 'Table ProductMedia created successfully.';
END
ELSE
BEGIN
    -- 2. Ensure columns are correctly named (check for snake_case versions and rename)
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'media_id')
        EXEC sp_rename 'ProductMedia.media_id', 'MediaId', 'COLUMN';
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'product_id')
        EXEC sp_rename 'ProductMedia.product_id', 'ProductId', 'COLUMN';

    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'variant_id')
        EXEC sp_rename 'ProductMedia.variant_id', 'VariantId', 'COLUMN';

    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'media_type')
        EXEC sp_rename 'ProductMedia.media_type', 'MediaType', 'COLUMN';

    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'display_order')
        EXEC sp_rename 'ProductMedia.display_order', 'SortOrder', 'COLUMN';
        
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'created_at')
        EXEC sp_rename 'ProductMedia.created_at', 'CreatedAt', 'COLUMN';

    PRINT 'Table ProductMedia columns verified/renamed.';
END
GO

-- 3. Fix the CK_ProductMedia_Type constraint
-- First drop it if it exists
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_ProductMedia_Type' AND parent_object_id = OBJECT_ID('ProductMedia'))
BEGIN
    ALTER TABLE [ProductMedia] DROP CONSTRAINT [CK_ProductMedia_Type];
    PRINT 'Dropped old CK_ProductMedia_Type constraint.';
END
GO

-- Add it back with correct values (Hibernate sends uppercase enum names)
ALTER TABLE [ProductMedia] ADD CONSTRAINT [CK_ProductMedia_Type] 
CHECK ([MediaType] IN ('IMAGE', 'VIDEO', 'DOCUMENT'));
PRINT 'Created corrected CK_ProductMedia_Type constraint.';
GO

-- 4. Ensure SortOrder is used instead of SortOrder (it was display_order in code but sort_order in entity?) 
-- Actually in entity it is displayOrder mapped to SortOrder. Let's make sure it is named SortOrder in DB.
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'SortOrder')
BEGIN
    PRINT 'Column SortOrder already exists.';
END
ELSE IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('ProductMedia') AND name = 'SortOrder')
BEGIN
    PRINT 'Column SortOrder already exists.';
END
-- Wait, I already renamed display_order to SortOrder in step 2. 
GO
