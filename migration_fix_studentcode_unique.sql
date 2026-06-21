USE canteen_db;
GO

-- Drop legacy non-filtered index if exists
IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_customer_student_code' AND object_id = OBJECT_ID('dbo.customers'))
BEGIN
    DROP INDEX idx_customer_student_code ON dbo.customers;
END
GO

-- Drop any UNIQUE constraint/index on customers.student_code (if present)
DECLARE @uqName SYSNAME;
SELECT TOP 1 @uqName = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic ON kc.parent_object_id = ic.object_id AND kc.unique_index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE kc.parent_object_id = OBJECT_ID('dbo.customers')
  AND c.name = 'student_code'
  AND kc.type = 'UQ';

IF @uqName IS NOT NULL
BEGIN
    DECLARE @sql NVARCHAR(4000) = N'ALTER TABLE dbo.customers DROP CONSTRAINT ' + QUOTENAME(@uqName) + N';';
    EXEC sp_executesql @sql;
END
GO

-- Create a filtered unique index so only non-null student codes must be unique
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_customers_student_code_filtered' AND object_id = OBJECT_ID('dbo.customers'))
BEGIN
    CREATE UNIQUE INDEX ux_customers_student_code_filtered ON dbo.customers(student_code) WHERE student_code IS NOT NULL;
END
GO
