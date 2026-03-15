USE EyeCareHubDB;
GO

-- Drop the existing constraint
ALTER TABLE [dbo].[Accounts] DROP CONSTRAINT CK_Accounts_RoleCode;

-- Add the updated constraint with all current roles
ALTER TABLE [dbo].[Accounts] ADD CONSTRAINT CK_Accounts_RoleCode 
CHECK (RoleCode IN ('CUSTOMER', 'ADMIN', 'STAFF', 'MANAGER', 'OPERATIONS_STAFF'));
