USE EyeCareHubDB;
GO

IF COL_LENGTH('dbo.FulfillmentTasks', 'EvidenceUrls') IS NULL
BEGIN
    ALTER TABLE [dbo].[FulfillmentTasks]
    ADD [EvidenceUrls] NVARCHAR(MAX) NULL;
END
GO

-- Backfill from legacy single-url column if it exists
IF COL_LENGTH('dbo.FulfillmentTasks', 'EvidenceImageUrl') IS NOT NULL
BEGIN
    UPDATE [dbo].[FulfillmentTasks]
    SET [EvidenceUrls] = COALESCE(NULLIF([EvidenceUrls], ''), [EvidenceImageUrl])
    WHERE [EvidenceUrls] IS NULL OR LTRIM(RTRIM([EvidenceUrls])) = '';
END
GO
