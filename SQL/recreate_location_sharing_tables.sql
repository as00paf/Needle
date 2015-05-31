USE Needle;

-- Location Sharing Table
DROP TABLE IF EXISTS location_sharing;
CREATE TABLE location_sharing
(
id INT PRIMARY KEY AUTO_INCREMENT,
senderId INT,
senderName INT,
receiverId INT,
receiverName TEXT,
timeLimit datetime,
lat decimal(9,7),
lng decimal(9,7),
pictureURL TEXT
);