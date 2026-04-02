-- Align Accounts.Status check constraint with application enum values
IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_Accounts_Status'
      AND parent_object_id = OBJECT_ID('dbo.Accounts')
)
BEGIN
    ALTER TABLE [dbo].[Accounts] DROP CONSTRAINT [CK_Accounts_Status];
END
GO

ALTER TABLE [dbo].[Accounts]
ADD CONSTRAINT [CK_Accounts_Status]
CHECK ([Status] IN ('ACTIVE', 'BLOCKED', 'INACTIVE', 'SUSPENDED', 'DELETED'));
GO
