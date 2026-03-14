USE EyeCareHubDB;
GO

-- 1. Add new columns to CartItems table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'CartItemId')
BEGIN
    -- We need to handle the conversion from composite key to single identity key
    -- First, drop existing primary key if it exists
    DECLARE @ConstraintName NVARCHAR(255);
    SELECT @ConstraintName = name FROM sys.key_constraints WHERE parent_object_id = OBJECT_ID('CartItems') AND type = 'PK';
    IF @ConstraintName IS NOT NULL
    BEGIN
        DECLARE @DropSQL NVARCHAR(MAX) = 'ALTER TABLE [CartItems] DROP CONSTRAINT ' + QUOTENAME(@ConstraintName);
        EXEC(@DropSQL);
    END

    -- Add the new Identity column
    ALTER TABLE [CartItems] ADD [CartItemId] BIGINT IDENTITY(1,1);
    
    -- Set it as the new Primary Key
    ALTER TABLE [CartItems] ADD CONSTRAINT PK_CartItems PRIMARY KEY ([CartItemId]);
    PRINT 'CartItemId column added and set as Primary Key.';
END

-- 2. Add Business columns if they don't exist
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'IsPreorder')
BEGIN
    ALTER TABLE [CartItems] ADD [IsPreorder] BIT NOT NULL DEFAULT 0;
    PRINT 'IsPreorder column added.';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'PreorderExpectedAt')
BEGIN
    ALTER TABLE [CartItems] ADD [PreorderExpectedAt] DATETIME2 NULL;
    PRINT 'PreorderExpectedAt column added.';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('CartItems') AND name = 'PrescriptionId')
BEGIN
    ALTER TABLE [CartItems] ADD [PrescriptionId] BIGINT NULL;
    PRINT 'PrescriptionId column added.';
END

GO
