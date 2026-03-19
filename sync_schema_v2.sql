USE EyeCareHubDB;
GO

-- 1. Cập nhật bảng OrderItems
-- Thêm cột ItemNote nếu chưa có
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('OrderItems') AND name = 'ItemNote')
BEGIN
    ALTER TABLE OrderItems ADD ItemNote NVARCHAR(500) NULL;
    PRINT 'Added ItemNote to OrderItems.';
END
GO

-- 2. Cập nhật bảng Prescriptions
-- Thêm cột PdTotal nếu chưa có
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'PdTotal')
BEGIN
    ALTER TABLE Prescriptions ADD PdTotal DECIMAL(5,2) NULL;
    PRINT 'Added PdTotal to Prescriptions.';
END
GO

-- Renaming columns to match Java entities (SphereOD, CylOD, etc.)
-- Note: Java used SphereOD, AxisOD, etc.

-- Check and Rename OD (Right Eye)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OdSphere')
    EXEC sp_rename 'Prescriptions.OdSphere', 'SphereOD', 'COLUMN';
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OdCylinder')
    EXEC sp_rename 'Prescriptions.OdCylinder', 'CylOD', 'COLUMN';
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OdAxis')
    EXEC sp_rename 'Prescriptions.OdAxis', 'AxisOD', 'COLUMN';
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OdAdd')
    EXEC sp_rename 'Prescriptions.OdAdd', 'AddOD', 'COLUMN';

-- Check and Rename OS (Left Eye)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OsSphere')
    EXEC sp_rename 'Prescriptions.OsSphere', 'SphereOS', 'COLUMN';
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OsCylinder')
    EXEC sp_rename 'Prescriptions.OsCylinder', 'CylOS', 'COLUMN';
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OsAxis')
    EXEC sp_rename 'Prescriptions.OsAxis', 'AxisOS', 'COLUMN';
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Prescriptions') AND name = 'OsAdd')
    EXEC sp_rename 'Prescriptions.OsAdd', 'AddOS', 'COLUMN';

PRINT 'Standardized Prescription column names to match Java entities.';
GO

-- 3. Dọn dẹp Inventory (Xóa các bảng cũ trùng lặp)

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_stocks')
BEGIN
    DROP TABLE inventory_stocks;
    PRINT 'Dropped redundant table inventory_stocks.';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_locations')
BEGIN
    DROP TABLE inventory_locations;
    PRINT 'Dropped redundant table inventory_locations.';
END
GO
