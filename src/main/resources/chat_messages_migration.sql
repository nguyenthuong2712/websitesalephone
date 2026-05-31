-- Thêm các cột cho chat_messages nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chat_messages') AND name = 'status')
BEGIN
    ALTER TABLE chat_messages ADD status VARCHAR(20) NOT NULL DEFAULT 'SENT';
    ALTER TABLE chat_messages ADD delivered_at datetimeoffset NULL;
    ALTER TABLE chat_messages ADD read_at datetimeoffset NULL;
END
GO

-- Cập nhật toàn bộ tin nhắn hiện có thành SENT nếu status chưa được khởi tạo
UPDATE chat_messages SET status = 'SENT' WHERE status IS NULL;
GO
