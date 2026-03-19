USE EyeCareHubDB;
GO

-- 1. Helper for renaming columns if they exist with different case/names
IF OBJECT_ID('tempdb..#RenameColumn') IS NOT NULL DROP PROCEDURE #RenameColumn;
GO
CREATE PROCEDURE #RenameColumn @TableName NVARCHAR(255), @OldColumnName NVARCHAR(255), @NewColumnName NVARCHAR(255)
AS
BEGIN
    DECLARE @ActualColumnName NVARCHAR(255);
    SELECT @ActualColumnName = name FROM sys.columns 
    WHERE object_id = OBJECT_ID(@TableName) AND LOWER(name) = LOWER(@OldColumnName);

    IF @ActualColumnName IS NOT NULL AND @ActualColumnName <> @NewColumnName
    BEGIN
        DECLARE @RenameTarget NVARCHAR(500) = @TableName + '.' + @ActualColumnName;
        EXEC sp_rename @RenameTarget, @NewColumnName, 'COLUMN';
    END
END
GO

-- 2. Standardize CartItems table name (case sensitivity)
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'cartitems')
BEGIN
    DECLARE @currentName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'cartitems');
    IF @currentName <> 'CartItems' EXEC sp_rename @currentName, 'CartItems';
END

-- 3. Standardize column names
EXEC #RenameColumn 'CartItems', 'cart_item_id', 'CartItemId';
EXEC #RenameColumn 'CartItems', 'cart_id', 'CartId';
EXEC #RenameColumn 'CartItems', 'variant_id', 'VariantId';
EXEC #RenameColumn 'CartItems', 'qty', 'Qty';
EXEC #RenameColumn 'CartItems', 'quantity', 'Qty';
EXEC #RenameColumn 'CartItems', 'unit_price_snap', 'UnitPriceSnap';
EXEC #RenameColumn 'CartItems', 'is_preorder', 'IsPreorder';
EXEC #RenameColumn 'CartItems', 'preorder_expected_at', 'PreorderExpectedAt';
EXEC #RenameColumn 'CartItems', 'prescription_id', 'PrescriptionId';
EXEC #RenameColumn 'CartItems', 'added_at', 'AddedAt';

-- 4. Add missing columns
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'UnitPriceSnap')
BEGIN
    ALTER TABLE [CartItems] ADD [UnitPriceSnap] DECIMAL(10, 2) NOT NULL DEFAULT 0;
    PRINT 'UnitPriceSnap column added.';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'AddedAt')
BEGIN
    ALTER TABLE [CartItems] ADD [AddedAt] DATETIME2 NOT NULL DEFAULT GETDATE();
    PRINT 'AddedAt column added.';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'IsPreorder')
BEGIN
    ALTER TABLE [CartItems] ADD [IsPreorder] BIT NOT NULL DEFAULT 0;
    PRINT 'IsPreorder column added.';
END

-- 5. Ensure Qty is NOT NULL
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'Qty' AND is_nullable = 1)
BEGIN
    ALTER TABLE [CartItems] ALTER COLUMN [Qty] INT NOT NULL;
    PRINT 'Qty column set to NOT NULL.';
END

GO
DROP PROCEDURE #RenameColumn;
GO
