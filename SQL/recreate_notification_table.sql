USE Needle;

-- Notifications
DROP TABLE IF EXISTS notification;
CREATE TABLE notification
(
	id INT PRIMARY KEY AUTO_INCREMENT,
	type INT,
	userId INT,
	dataId INT, 
	text TEXT,
	sentAt datetime,
	seen BOOLEAN,
	senderId INT,
	senderPictureURL TEXT
);