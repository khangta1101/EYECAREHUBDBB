USE EyeCareHubDB;
SET NOCOUNT ON;

BEGIN TRY
    BEGIN TRAN;

    DECLARE @Now DATETIME2 = GETDATE();

    DECLARE @CustomerAccountId BIGINT;
    DECLARE @StaffAccountId BIGINT;
    DECLARE @CustomerId BIGINT;
    DECLARE @CategoryId BIGINT;
    DECLARE @ProductId BIGINT;
    DECLARE @VariantId BIGINT;
    DECLARE @PromotionId BIGINT;
    DECLARE @OrderId BIGINT;
    DECLARE @OrderItemId BIGINT;
    DECLARE @CartId BIGINT;
    DECLARE @LocationId BIGINT;

    /* 1) Accounts + Customer */
    IF OBJECT_ID('dbo.accounts', 'U') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [accounts] WHERE [Email] = 'manual.customer@eyecarehub.local')
        BEGIN
            INSERT INTO [accounts] ([Email], [Username], [PasswordHash], [RoleCode], [Status], [CreatedAt], [LastLoginAt])
            VALUES (
                'manual.customer@eyecarehub.local',
                'manual_customer',
                '$2a$10$manualTestHashDoNotUseInProduction',
                'CUSTOMER',
                'ACTIVE',
                @Now,
                @Now
            );
        END;

        IF NOT EXISTS (SELECT 1 FROM [accounts] WHERE [Email] = 'manual.staff@eyecarehub.local')
        BEGIN
            INSERT INTO [accounts] ([Email], [Username], [PasswordHash], [RoleCode], [Status], [CreatedAt], [LastLoginAt])
            VALUES (
                'manual.staff@eyecarehub.local',
                'manual_staff',
                '$2a$10$manualTestHashDoNotUseInProduction',
                'OPERATIONS_STAFF',
                'ACTIVE',
                @Now,
                @Now
            );
        END;

        SELECT @CustomerAccountId = [AccountId]
        FROM [accounts]
        WHERE [Email] = 'manual.customer@eyecarehub.local';

        SELECT @StaffAccountId = [AccountId]
        FROM [accounts]
        WHERE [Email] = 'manual.staff@eyecarehub.local';
    END;

    IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL AND @CustomerAccountId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [customers] WHERE [CustomerId] = @CustomerAccountId)
        BEGIN
            INSERT INTO [customers] ([CustomerId], [FullName], [Gender], [DateOfBirth], [AvatarUrl], [CreatedAt])
            VALUES (
                @CustomerAccountId,
                'Manual Test Customer',
                'OTHER',
                '1998-08-18',
                NULL,
                @Now
            );
        END;

        SET @CustomerId = @CustomerAccountId;
    END;

    /* 2) Category + Product + Variant */
    IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [categories] WHERE [Slug] = 'kinh-gong-test-tay')
        BEGIN
            INSERT INTO [categories] ([Name], [Slug], [ParentCategoryId], [IsActive], [CreatedAt])
            VALUES (N'Kinh gong test tay', 'kinh-gong-test-tay', NULL, 1, @Now);
        END;

        SELECT @CategoryId = [CategoryId]
        FROM [categories]
        WHERE [Slug] = 'kinh-gong-test-tay';
    END;

    IF OBJECT_ID('dbo.products', 'U') IS NOT NULL AND @CategoryId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [products] WHERE [SKU] = 'TEST-FRAME-001')
        BEGIN
            INSERT INTO [products]
            (
                [Name], [SearchTags], [ProductType], [PrimaryCategoryId],
                [Brand], [SKU], [Description], [IsActive], [CreatedAt]
            )
            VALUES
            (
                N'Gong kinh test tay ECH-01',
                'test,manual,frame',
                'FRAME',
                @CategoryId,
                'EyeCareHub',
                'TEST-FRAME-001',
                N'San pham mau de test tay luong dat hang va fulfillment.',
                1,
                @Now
            );
        END;

        SELECT @ProductId = [ProductId]
        FROM [products]
        WHERE [SKU] = 'TEST-FRAME-001';
    END;

    IF OBJECT_ID('dbo.ProductVariants', 'U') IS NOT NULL AND @ProductId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [ProductVariants] WHERE [SKU] = 'TEST-FRAME-001-BLACK')
        BEGIN
            INSERT INTO [ProductVariants]
            (
                [ProductId], [SKU], [VariantName], [Color], [Size], [Material],
                [AttributesJson], [Currency], [BasePrice], [SalePrice], [IsActive], [CreatedAt]
            )
            VALUES
            (
                @ProductId,
                'TEST-FRAME-001-BLACK',
                N'Den - size M',
                'Black',
                'M',
                N'Acetate',
                '{"bridge":"18","temple":"145"}',
                'VND',
                850000,
                790000,
                1,
                @Now
            );
        END;

        SELECT @VariantId = [VariantId]
        FROM [ProductVariants]
        WHERE [SKU] = 'TEST-FRAME-001-BLACK';
    END;

    /* 3) Promotion + Policy */
    IF OBJECT_ID('dbo.Promotions', 'U') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [Promotions] WHERE [Code] = 'MANUAL10')
        BEGIN
            INSERT INTO [Promotions]
            (
                [Code], [Name], [PromoType], [DiscountType], [DiscountValue],
                [MinOrderAmount], [MaxDiscount], [StartAt], [EndAt], [RuleJson], [IsActive], [CreatedAt]
            )
            VALUES
            (
                'MANUAL10',
                N'Giam 10 phan tram cho test tay',
                'COUPON',
                'PERCENTAGE',
                10,
                200000,
                150000,
                DATEADD(DAY, -30, @Now),
                DATEADD(DAY, 60, @Now),
                '{"channels":["ONLINE"]}',
                1,
                @Now
            );
        END;

        SELECT @PromotionId = [PromotionId]
        FROM [Promotions]
        WHERE [Code] = 'MANUAL10';
    END;

    IF OBJECT_ID('dbo.Policies', 'U') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [Policies] WHERE [Slug] = 'chinh-sach-doi-tra-test-tay-v1')
        BEGIN
            INSERT INTO [Policies]
            (
                [PolicyType], [Slug], [Title], [Content], [Version], [EffectiveFrom], [IsActive], [CreatedBy], [CreatedAt]
            )
            VALUES
            (
                'RETURN',
                'chinh-sach-doi-tra-test-tay-v1',
                N'Chinh sach doi tra ban test tay',
                N'Ap dung trong 7 ngay cho du lieu test.',
                1,
                @Now,
                1,
                'seed_manual_test_data.sql',
                @Now
            );
        END;
    END;

    /* 4) Cart + CartItem */
    IF OBJECT_ID('dbo.carts', 'U') IS NOT NULL AND @CustomerId IS NOT NULL
    BEGIN
        SELECT TOP 1 @CartId = [CartId]
        FROM [carts]
        WHERE [CustomerId] = @CustomerId AND [Status] = 'ACTIVE'
        ORDER BY [CartId] DESC;

        IF @CartId IS NULL
        BEGIN
            INSERT INTO [carts] ([CustomerId], [Status], [CreatedAt], [UpdatedAt])
            VALUES (@CustomerId, 'ACTIVE', @Now, @Now);

            SET @CartId = SCOPE_IDENTITY();
        END;
    END;

    IF OBJECT_ID('dbo.CartItems', 'U') IS NOT NULL AND @CartId IS NOT NULL AND @VariantId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [CartItems] WHERE [CartId] = @CartId AND [VariantId] = @VariantId)
        BEGIN
            INSERT INTO [CartItems]
            (
                [CartId], [VariantId], [Qty], [UnitPriceSnap], [IsPreorder], [PreorderExpectedAt], [PrescriptionId], [AddedAt]
            )
            VALUES
            (
                @CartId,
                @VariantId,
                1,
                790000,
                0,
                NULL,
                NULL,
                @Now
            );
        END;
    END;

    /* 5) Order + OrderItem + Fulfillment */
    IF OBJECT_ID('dbo.Orders', 'U') IS NOT NULL AND @CustomerId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [Orders] WHERE [OrderNo] = 'ORD-MANUAL-0001')
        BEGIN
            INSERT INTO [Orders]
            (
                [OrderNo], [CustomerId], [Channel], [OrderType], [Status], [PromotionId],
                [Subtotal], [DiscountTotal], [ShippingFee], [GrandTotal], [Note], [CreatedAt]
            )
            VALUES
            (
                'ORD-MANUAL-0001',
                @CustomerId,
                'ONLINE',
                'IN_STOCK',
                'NEW',
                @PromotionId,
                790000,
                79000,
                30000,
                741000,
                N'Don mau phuc vu test tay API',
                @Now
            );
        END;

        SELECT @OrderId = [OrderId]
        FROM [Orders]
        WHERE [OrderNo] = 'ORD-MANUAL-0001';
    END;

    IF OBJECT_ID('dbo.OrderItems', 'U') IS NOT NULL AND @OrderId IS NOT NULL AND @VariantId IS NOT NULL
    BEGIN
        IF COL_LENGTH('OrderItems', 'UnitPrice') IS NOT NULL
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM [OrderItems] WHERE [OrderId] = @OrderId AND [VariantId] = @VariantId)
            BEGIN
                INSERT INTO [OrderItems]
                (
                    [OrderId], [VariantId], [Qty], [UnitPrice], [LineTotal], [IsPrescription],
                    [PreorderExpectedAt], [PreorderReceivedAt], [ItemNote]
                )
                VALUES
                (
                    @OrderId,
                    @VariantId,
                    1,
                    790000,
                    790000,
                    0,
                    NULL,
                    NULL,
                    N'San pham test'
                );
            END;
        END
        ELSE IF COL_LENGTH('OrderItems', 'UnitPriceSnap') IS NOT NULL
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM [OrderItems] WHERE [OrderId] = @OrderId AND [VariantId] = @VariantId)
            BEGIN
                INSERT INTO [OrderItems]
                (
                    [OrderId], [VariantId], [Qty], [UnitPriceSnap], [LineTotal], [IsPrescription],
                    [PreorderExpectedAt], [PreorderReceivedAt]
                )
                VALUES
                (
                    @OrderId,
                    @VariantId,
                    1,
                    790000,
                    790000,
                    0,
                    NULL,
                    NULL
                );
            END;
        END;

        SELECT TOP 1 @OrderItemId = [OrderItemId]
        FROM [OrderItems]
        WHERE [OrderId] = @OrderId AND [VariantId] = @VariantId
        ORDER BY [OrderItemId] DESC;
    END;

    IF OBJECT_ID('dbo.FulfillmentTasks', 'U') IS NOT NULL AND @OrderId IS NOT NULL
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM [FulfillmentTasks]
            WHERE [OrderId] = @OrderId AND [TaskType] = 'PACK' AND [Status] = 'PENDING'
        )
        BEGIN
            INSERT INTO [FulfillmentTasks]
            (
                [OrderId], [OrderItemId], [TaskType], [Status], [AssignedTo], [Note],
                [EvidenceImageUrl], [StartedAt], [DoneAt], [CreatedAt], [UpdatedAt]
            )
            VALUES
            (
                @OrderId,
                @OrderItemId,
                'PACK',
                'PENDING',
                @StaffAccountId,
                N'Task dong goi don test tay',
                NULL,
                NULL,
                NULL,
                @Now,
                @Now
            );
        END;
    END;

    /* 6) Payment + Shipment (if these tables exist in your DB) */
    IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL AND @OrderId IS NOT NULL
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM [payments]
            WHERE [OrderId] = @OrderId AND [paymentPurpose] = 'FINAL' AND [provider] = 'COD'
        )
        BEGIN
            INSERT INTO [payments]
            (
                [OrderId], [paymentPurpose], [provider], [amount], [status],
                [transactionRef], [paidAt], [rawResponseJson], [createdAt], [updatedAt]
            )
            VALUES
            (
                @OrderId,
                'FINAL',
                'COD',
                741000,
                'PENDING',
                'PAY-MANUAL-0001',
                NULL,
                NULL,
                @Now,
                @Now
            );
        END;
    END;

    IF OBJECT_ID('dbo.shipments', 'U') IS NOT NULL AND @OrderId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [shipments] WHERE [OrderId] = @OrderId)
        BEGIN
            INSERT INTO [shipments]
            (
                [OrderId], [carrier], [trackingNumber], [trackingUrl], [status],
                [estimatedDelivery], [actualDelivery], [note], [createdAt], [updatedAt]
            )
            VALUES
            (
                @OrderId,
                'GHN',
                'TRK-MANUAL-0001',
                'https://tracking.example.local/TRK-MANUAL-0001',
                'CREATED',
                DATEADD(DAY, 3, @Now),
                NULL,
                N'Don vi van chuyen demo',
                @Now,
                @Now
            );
        END;
    END;

    /* 7) Inventory sample (if schema migration was applied) */
    IF OBJECT_ID('dbo.inventory_locations', 'U') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [inventory_locations] WHERE [code] = 'WH-HCM-01')
        BEGIN
            INSERT INTO [inventory_locations]
            (
                [name], [code], [location_type], [address], [is_active], [created_at], [updated_at]
            )
            VALUES
            (
                N'Kho Ho Chi Minh',
                'WH-HCM-01',
                'WAREHOUSE',
                N'Quan 7, TP Ho Chi Minh',
                1,
                @Now,
                @Now
            );
        END;

        SELECT @LocationId = [id]
        FROM [inventory_locations]
        WHERE [code] = 'WH-HCM-01';
    END;

    IF OBJECT_ID('dbo.inventory_stocks', 'U') IS NOT NULL AND @LocationId IS NOT NULL AND @VariantId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM [inventory_stocks] WHERE [location_id] = @LocationId AND [variant_id] = @VariantId)
        BEGIN
            INSERT INTO [inventory_stocks]
            (
                [location_id], [variant_id], [on_hand_qty], [reserved_qty], [created_at], [updated_at]
            )
            VALUES
            (
                @LocationId,
                @VariantId,
                25,
                2,
                @Now,
                @Now
            );
        END;
    END;

    COMMIT TRAN;
    PRINT 'Seed data inserted successfully for manual testing.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRAN;

    DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @ErrLine INT = ERROR_LINE();
    PRINT 'Seed failed at line ' + CAST(@ErrLine AS NVARCHAR(20)) + ': ' + @Err;
    THROW;
END CATCH;
