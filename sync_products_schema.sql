USE EyeCareHubDB;
SET NOCOUNT ON;

-- Ensure products.SKU exists for JPA mapping (@Column "SKU")
IF COL_LENGTH('products', 'SKU') IS NULL
BEGIN
    ALTER TABLE [products] ADD [SKU] NVARCHAR(100) NULL;
    PRINT 'Added [products].[SKU]';
END
ELSE
BEGIN
    PRINT '[products].[SKU] already exists';
END;
