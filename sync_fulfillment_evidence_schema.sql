USE EyeCareHubDB;
GO

IF COL_LENGTH('dbo.FulfillmentTasks', 'EvidenceUrls') IS NULL
BEGIN
    ALTER TABLE [dbo].[FulfillmentTasks]
    ADD [EvidenceUrls] NVARCHAR(MAX) NULL;
END
GO
