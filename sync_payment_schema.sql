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

-- 2. Standardize payments table name
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'payments')
BEGIN
    DECLARE @currentName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'payments');
    IF @currentName <> 'payments' EXEC sp_rename @currentName, 'payments';
END

-- 3. Add missing columns
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'RawResponseJson')
BEGIN
    ALTER TABLE [payments] ADD [RawResponseJson] NVARCHAR(MAX) NULL;
    PRINT 'RawResponseJson column added to payments table.';
END

-- 4. Check for other essential columns
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'TransactionRef')
BEGIN
    ALTER TABLE [payments] ADD [TransactionRef] NVARCHAR(200) NULL;
    PRINT 'TransactionRef column added to payments table.';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'PaidAt')
BEGIN
    ALTER TABLE [payments] ADD [PaidAt] DATETIME2 NULL;
    PRINT 'PaidAt column added to payments table.';
END

GO
DROP PROCEDURE #RenameColumn;
GO
