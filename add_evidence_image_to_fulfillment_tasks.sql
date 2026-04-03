USE EyeCareHubDB;
GO

-- Add EvidenceImageUrl column to FulfillmentTasks table
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'FulfillmentTasks' AND COLUMN_NAME = 'EvidenceImageUrl'
)
BEGIN
    ALTER TABLE [dbo].[FulfillmentTasks]
    ADD [EvidenceImageUrl] NVARCHAR(500) NULL;
END
GO

PRINT 'Successfully added EvidenceImageUrl column to FulfillmentTasks table.';
GO
