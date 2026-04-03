/* ==========================================================
   DATABASE: VendingMachineDB
   Máy Bán Nước Tự Động — Đại Học

   RÃ XUỐNG TỪ CLASS DIAGRAM:
   ┌─────────────────────────┬───────────────────────┐
   │ Class (Diagram)         │ Table (DB)            │
   ├─────────────────────────┼───────────────────────┤
   │ Beverage (abstract)     │ beverages             │
   │   ├─ Tea                │   (beverage_type=TEA) │
   │   ├─ SoftDrink          │   (=SOFT_DRINK)       │
   │   └─ MineralWater       │   (=MINERAL_WATER)    │
   │ InventoryItem           │ inventory_items       │
   │ Transaction             │ transactions          │
   ├─────────────────────────┼───────────────────────┤
   │ Inventory               │ (không persist)       │
   │ VendingMachine           │ (Singleton runtime)   │
   │ VendingMachineState     │ (behavior runtime)    │
   │ PaymentMethod (enum)    │ (CHECK constraint)    │
   └─────────────────────────┴───────────────────────┘
   ========================================================== */

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'VendingMachineDB')
    CREATE DATABASE VendingMachineDB;
GO
USE VendingMachineDB;
GO

/* ===============================
   TABLE: beverages
   Class: Beverage (abstract)
         ├─ Tea       { -flavor }
         ├─ SoftDrink  { -brand }
         └─ MineralWater { -volume }
   JPA: Single-Table Inheritance
   Discriminator: beverage_type
   =============================== */
IF OBJECT_ID('dbo.beverages','U') IS NULL
CREATE TABLE dbo.beverages (
    -- Beverage { -String id, -String name, -double price }
    beverage_id    NVARCHAR(10)  NOT NULL PRIMARY KEY,
    beverage_type  NVARCHAR(20)  NOT NULL,              -- discriminator
    name           NVARCHAR(100) NOT NULL,
    price          FLOAT         NOT NULL CHECK (price > 0),

    -- Tea { -String flavor }
    flavor         NVARCHAR(100) NULL,

    -- SoftDrink { -String brand }
    brand          NVARCHAR(100) NULL,

    -- MineralWater { -double volume }
    volume         FLOAT         NULL,

    CONSTRAINT CK_beverage_type CHECK (beverage_type IN ('TEA','SOFT_DRINK','MINERAL_WATER'))
);
GO

/* ===============================
   TABLE: inventory_items
   Class: InventoryItem { -Beverage beverage, -int quantity }
   Quan hệ: InventoryItem --> "1" Beverage
   =============================== */
IF OBJECT_ID('dbo.inventory_items','U') IS NULL
CREATE TABLE dbo.inventory_items (
    item_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    beverage_id  NVARCHAR(10) NOT NULL,                -- FK → InventoryItem.beverage
    quantity     INT          NOT NULL DEFAULT 0 CHECK (quantity >= 0),

    CONSTRAINT FK_inventory_beverage FOREIGN KEY (beverage_id)
        REFERENCES dbo.beverages(beverage_id),
    CONSTRAINT UQ_inventory_beverage UNIQUE (beverage_id) -- 1 item per beverage
);
GO

/* ===============================
   TABLE: transactions
   Class: Transaction {
       -Beverage beverage,
       -PaymentMethod method,
       -LocalDateTime timestamp
   }
   Quan hệ: Transaction --> "1" Beverage
             Transaction --> "1" PaymentMethod (enum → CHECK)
   Mở rộng: order_code → PayOS orderCode
   =============================== */
IF OBJECT_ID('dbo.transactions','U') IS NULL
CREATE TABLE dbo.transactions (
    transaction_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    beverage_id      NVARCHAR(10)  NOT NULL,               -- FK → Transaction.beverage
    payment_method   NVARCHAR(20)  NOT NULL,               -- Transaction.method (enum)
    order_code       BIGINT        NULL,                   -- PayOS orderCode (null nếu COD)
    [timestamp]      DATETIME2     NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_transaction_beverage FOREIGN KEY (beverage_id)
        REFERENCES dbo.beverages(beverage_id),
    CONSTRAINT CK_payment_method CHECK (payment_method IN ('COD','PAYOS'))
);
GO

/* ===============================
   INDEXES
   =============================== */
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_transactions_order_code')
CREATE INDEX IX_transactions_order_code
    ON dbo.transactions(order_code) WHERE order_code IS NOT NULL;
GO

/* ==========================================================
   SEED DATA
   ========================================================== */

DELETE FROM dbo.transactions;
DELETE FROM dbo.inventory_items;
DELETE FROM dbo.beverages;
GO

-- Beverages (10 sản phẩm, 3 loại)
INSERT INTO dbo.beverages (beverage_id, beverage_type, name, price, flavor, brand, volume) VALUES
('T01', 'TEA',           N'Trà Ô Long',  15000, N'Ô Long', NULL, NULL),
('T02', 'TEA',           N'Trà Xanh',    12000, N'Xanh',   NULL, NULL),
('T03', 'TEA',           N'Trà Đào',     13000, N'Đào',    NULL, NULL),
('S01', 'SOFT_DRINK',    N'Coca-Cola',    10000, NULL, N'Coca-Cola', NULL),
('S02', 'SOFT_DRINK',    N'Pepsi',        10000, NULL, N'PepsiCo',   NULL),
('S03', 'SOFT_DRINK',    N'Fanta Cam',    10000, NULL, N'Coca-Cola', NULL),
('S04', 'SOFT_DRINK',    N'7Up',           9000, NULL, N'PepsiCo',   NULL),
('W01', 'MINERAL_WATER', N'Aquafina',      8000, NULL, NULL, 500),
('W02', 'MINERAL_WATER', N'Lavie',         7000, NULL, NULL, 350),
('W03', 'MINERAL_WATER', N'Vĩnh Hảo',     6000, NULL, NULL, 500);
GO

-- Inventory (1 dòng per beverage → khớp UNIQUE constraint)
INSERT INTO dbo.inventory_items (beverage_id, quantity) VALUES
('T01', 5), ('T02', 4), ('T03', 3),
('S01', 5), ('S02', 4), ('S03', 3), ('S04', 3),
('W01', 6), ('W02', 5), ('W03', 4);
GO

-- Transactions (demo)
INSERT INTO dbo.transactions (beverage_id, payment_method, order_code, [timestamp]) VALUES
('T01', 'COD',   NULL,   '2026-04-01 09:15:00'),
('S01', 'PAYOS', 100001, '2026-04-01 10:30:00'),
('W01', 'COD',   NULL,   '2026-04-01 14:22:00'),
('T02', 'PAYOS', 100002, '2026-04-02 08:45:00'),
('S02', 'COD',   NULL,   '2026-04-02 11:10:00');
GO

/* ==========================================================
   VIEWS — Truy vấn tiện dụng
   ========================================================== */

-- Tồn kho + trạng thái
IF OBJECT_ID('dbo.v_inventory_summary','V') IS NOT NULL DROP VIEW dbo.v_inventory_summary;
GO
CREATE VIEW dbo.v_inventory_summary AS
SELECT
    b.beverage_id,
    b.beverage_type,
    b.name,
    b.price,
    i.quantity,
    CASE
        WHEN i.quantity = 0  THEN N'Hết hàng'
        WHEN i.quantity <= 2 THEN N'Sắp hết'
        ELSE N'Còn hàng'
    END AS stock_status
FROM dbo.beverages b
JOIN dbo.inventory_items i ON b.beverage_id = i.beverage_id;
GO

-- Lịch sử giao dịch kèm tên SP (amount tính từ beverages.price)
IF OBJECT_ID('dbo.v_transaction_history','V') IS NOT NULL DROP VIEW dbo.v_transaction_history;
GO
CREATE VIEW dbo.v_transaction_history AS
SELECT
    t.transaction_id,
    b.name           AS beverage_name,
    b.beverage_type,
    t.payment_method,
    t.order_code,
    b.price          AS amount,
    t.[timestamp]
FROM dbo.transactions t
JOIN dbo.beverages b ON t.beverage_id = b.beverage_id
GO

/* ==========================================================
   Kiểm tra
   ========================================================== */
SELECT '=== BEVERAGES ===' AS [info]; SELECT * FROM dbo.beverages;
SELECT '=== INVENTORY ===' AS [info]; SELECT * FROM dbo.v_inventory_summary;
SELECT '=== TRANSACTIONS ===' AS [info]; SELECT * FROM dbo.v_transaction_history ORDER BY [timestamp] DESC;
GO
