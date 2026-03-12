USE EyeCareHubDB;
GO

-- Helper Procedure to drop constraints on a column
IF OBJECT_ID('tempdb..#DropConstraints') IS NOT NULL DROP PROCEDURE #DropConstraints;
GO
CREATE PROCEDURE #DropConstraints @TableName NVARCHAR(255), @ColumnName NVARCHAR(255)
AS
BEGIN
    DECLARE @ConstraintName NVARCHAR(255);
    DECLARE @SQL NVARCHAR(MAX);
    
    -- Drop Check Constraints
    DECLARE check_cursor CURSOR FOR 
    SELECT cc.name 
    FROM sys.check_constraints cc
    JOIN sys.columns c ON cc.parent_column_id = c.column_id AND cc.parent_object_id = c.object_id
    WHERE OBJECT_NAME(cc.parent_object_id) = @TableName AND LOWER(c.name) = LOWER(@ColumnName);

    OPEN check_cursor;
    FETCH NEXT FROM check_cursor INTO @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @SQL = 'ALTER TABLE ' + QUOTENAME(@TableName) + ' DROP CONSTRAINT ' + QUOTENAME(@ConstraintName);
        EXEC(@SQL);
        FETCH NEXT FROM check_cursor INTO @ConstraintName;
    END
    CLOSE check_cursor;
    DEALLOCATE check_cursor;

    -- Drop Default Constraints
    DECLARE default_cursor CURSOR FOR 
    SELECT dc.name 
    FROM sys.default_constraints dc
    JOIN sys.columns c ON dc.parent_column_id = c.column_id AND dc.parent_object_id = c.object_id
    WHERE OBJECT_NAME(dc.parent_object_id) = @TableName AND LOWER(c.name) = LOWER(@ColumnName);

    OPEN default_cursor;
    FETCH NEXT FROM default_cursor INTO @ConstraintName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @SQL = 'ALTER TABLE ' + QUOTENAME(@TableName) + ' DROP CONSTRAINT ' + QUOTENAME(@ConstraintName);
        EXEC(@SQL);
        FETCH NEXT FROM default_cursor INTO @ConstraintName;
    END
    CLOSE default_cursor;
    DEALLOCATE default_cursor;
END
GO

-- Helper Procedure to rename column case-insensitively
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

-- 1. Standardize Promotions Table
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'promotions')
BEGIN
    DECLARE @currentPromotionsName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'promotions');
    IF @currentPromotionsName <> 'Promotions' EXEC sp_rename @currentPromotionsName, 'Promotions';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Promotions')
BEGIN
    EXEC #DropConstraints 'Promotions', 'IsActive';
    EXEC #DropConstraints 'Promotions', 'PromoType';
    EXEC #DropConstraints 'Promotions', 'DiscountType';

    EXEC #RenameColumn 'Promotions', 'id', 'PromotionId';
    EXEC #RenameColumn 'Promotions', 'PromotionId', 'PromotionId'; -- Just to be sure
    EXEC #RenameColumn 'Promotions', 'Code', 'Code';
    EXEC #RenameColumn 'Promotions', 'promo_type', 'PromoType';
    EXEC #RenameColumn 'Promotions', 'discount_type', 'DiscountType';
    EXEC #RenameColumn 'Promotions', 'discount_value', 'DiscountValue';
    EXEC #RenameColumn 'Promotions', 'min_order_amount', 'MinOrderAmount';
    EXEC #RenameColumn 'Promotions', 'max_discount', 'MaxDiscount';
    EXEC #RenameColumn 'Promotions', 'start_at', 'StartAt';
    EXEC #RenameColumn 'Promotions', 'end_at', 'EndAt';
    EXEC #RenameColumn 'Promotions', 'usage_limit', 'UsageLimit';
    EXEC #RenameColumn 'Promotions', 'used_count', 'UsedCount';
    EXEC #RenameColumn 'Promotions', 'rule_json', 'RuleJson';
    EXEC #RenameColumn 'Promotions', 'is_active', 'IsActive';
    EXEC #RenameColumn 'Promotions', 'created_at', 'CreatedAt';
    EXEC #RenameColumn 'Promotions', 'updated_at', 'UpdatedAt';
    PRINT 'Table Promotions standardized.';
END
GO

-- 2. Standardize Orders Table
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'orders')
BEGIN
    DECLARE @currentOrdersName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'orders');
    IF @currentOrdersName <> 'Orders' EXEC sp_rename @currentOrdersName, 'Orders';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Orders')
BEGIN
    EXEC #DropConstraints 'Orders', 'Channel';
    EXEC #DropConstraints 'Orders', 'Status';
    EXEC #DropConstraints 'Orders', 'OrderType';

    EXEC #RenameColumn 'Orders', 'id', 'OrderId';
    EXEC #RenameColumn 'Orders', 'OrderId', 'OrderId';
    EXEC #RenameColumn 'Orders', 'order_no', 'OrderNo';
    EXEC #RenameColumn 'Orders', 'customer_id', 'CustomerId';
    EXEC #RenameColumn 'Orders', 'shipping_address_id', 'ShippingAddressId';
    EXEC #RenameColumn 'Orders', 'sales_account_id', 'SalesAccountId';
    EXEC #RenameColumn 'Orders', 'channel', 'Channel';
    EXEC #RenameColumn 'Orders', 'order_type', 'OrderType';
    EXEC #RenameColumn 'Orders', 'status', 'Status';
    EXEC #RenameColumn 'Orders', 'promotion_id', 'PromotionId';
    EXEC #RenameColumn 'Orders', 'subtotal', 'Subtotal';
    EXEC #RenameColumn 'Orders', 'discount_total', 'DiscountTotal';
    EXEC #RenameColumn 'Orders', 'shipping_fee', 'ShippingFee';
    EXEC #RenameColumn 'Orders', 'grand_total', 'GrandTotal';
    EXEC #RenameColumn 'Orders', 'note', 'Note';
    EXEC #RenameColumn 'Orders', 'created_at', 'CreatedAt';
    EXEC #RenameColumn 'Orders', 'updated_at', 'UpdatedAt';
    PRINT 'Table Orders standardized.';
END
GO

-- 3. Standardize OrderItems Table
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'order_items')
BEGIN
    DECLARE @currentOrderItemsName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'order_items');
    IF @currentOrderItemsName <> 'OrderItems' EXEC sp_rename @currentOrderItemsName, 'OrderItems';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'OrderItems')
BEGIN
    EXEC #DropConstraints 'OrderItems', 'Qty';
    EXEC #DropConstraints 'OrderItems', 'IsPrescription';

    EXEC #RenameColumn 'OrderItems', 'id', 'OrderItemId';
    EXEC #RenameColumn 'OrderItems', 'OrderItemId', 'OrderItemId';
    EXEC #RenameColumn 'OrderItems', 'order_id', 'OrderId';
    EXEC #RenameColumn 'OrderItems', 'variant_id', 'VariantId';
    EXEC #RenameColumn 'OrderItems', 'qty', 'Qty';
    EXEC #RenameColumn 'OrderItems', 'unit_price_snap', 'UnitPriceSnap';
    EXEC #RenameColumn 'OrderItems', 'line_total', 'LineTotal';
    EXEC #RenameColumn 'OrderItems', 'is_prescription', 'IsPrescription';
    EXEC #RenameColumn 'OrderItems', 'preorder_expected_at', 'PreorderExpectedAt';
    EXEC #RenameColumn 'OrderItems', 'preorder_received_at', 'PreorderReceivedAt';
    EXEC #RenameColumn 'OrderItems', 'created_at', 'CreatedAt';
    EXEC #RenameColumn 'OrderItems', 'updated_at', 'UpdatedAt';
    PRINT 'Table OrderItems standardized.';
END
GO

-- 4. Standardize Policies Table
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'policies')
BEGIN
    DECLARE @currentPoliciesName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'policies');
    IF @currentPoliciesName <> 'Policies' EXEC sp_rename @currentPoliciesName, 'Policies';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Policies')
BEGIN
    EXEC #DropConstraints 'Policies', 'IsPublished';

    EXEC #RenameColumn 'Policies', 'id', 'PolicyId';
    EXEC #RenameColumn 'Policies', 'PolicyId', 'PolicyId';
    EXEC #RenameColumn 'Policies', 'type', 'Type';
    EXEC #RenameColumn 'Policies', 'title', 'Title';
    EXEC #RenameColumn 'Policies', 'slug', 'Slug';
    EXEC #RenameColumn 'Policies', 'content', 'Content';
    EXEC #RenameColumn 'Policies', 'is_published', 'IsPublished';
    EXEC #RenameColumn 'Policies', 'display_order', 'DisplayOrder';
    EXEC #RenameColumn 'Policies', 'created_at', 'CreatedAt';
    EXEC #RenameColumn 'Policies', 'updated_at', 'UpdatedAt';
    EXEC #RenameColumn 'Policies', 'published_at', 'PublishedAt';
    PRINT 'Table Policies standardized.';
END
GO

-- 5. Standardize Prescriptions Table
IF EXISTS (SELECT * FROM sys.tables WHERE LOWER(name) = 'prescriptions')
BEGIN
    DECLARE @currentPrescriptionsName NVARCHAR(255) = (SELECT name FROM sys.tables WHERE LOWER(name) = 'prescriptions');
    IF @currentPrescriptionsName <> 'Prescriptions' EXEC sp_rename @currentPrescriptionsName, 'Prescriptions';
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Prescriptions')
BEGIN
    EXEC #RenameColumn 'Prescriptions', 'id', 'PrescriptionId';
    EXEC #RenameColumn 'Prescriptions', 'PrescriptionId', 'PrescriptionId';
    EXEC #RenameColumn 'Prescriptions', 'od_sphere', 'OdSphere';
    EXEC #RenameColumn 'Prescriptions', 'od_cylinder', 'OdCylinder';
    EXEC #RenameColumn 'Prescriptions', 'od_axis', 'OdAxis';
    EXEC #RenameColumn 'Prescriptions', 'od_add', 'OdAdd';
    EXEC #RenameColumn 'Prescriptions', 'os_sphere', 'OsSphere';
    EXEC #RenameColumn 'Prescriptions', 'os_cylinder', 'OsCylinder';
    EXEC #RenameColumn 'Prescriptions', 'os_axis', 'OsAxis';
    EXEC #RenameColumn 'Prescriptions', 'os_add', 'OsAdd';
    EXEC #RenameColumn 'Prescriptions', 'pd_right', 'PdRight';
    EXEC #RenameColumn 'Prescriptions', 'pd_left', 'PdLeft';
    EXEC #RenameColumn 'Prescriptions', 'prescription_file_url', 'PrescriptionFileUrl';
    EXEC #RenameColumn 'Prescriptions', 'notes', 'Notes';
    EXEC #RenameColumn 'Prescriptions', 'created_at', 'CreatedAt';
    EXEC #RenameColumn 'Prescriptions', 'updated_at', 'UpdatedAt';
    PRINT 'Table Prescriptions standardized.';
END
GO
