USE Needle;

-- Haystack Table
DROP TABLE IF EXISTS haystack;
CREATE TABLE haystack
(
	id INT PRIMARY KEY AUTO_INCREMENT,
	name TEXT,
	owner INT,
	isPublic TINYINT(1),
	timeLimit datetime,
	zoneRadius INT,
	isCircle TINYINT(1),
	lat decimal(9,7),
	lng decimal(9,7),
	pictureURL TEXT
);

-- User Location Table
DROP TABLE IF EXISTS needle_location;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS user_location;
CREATE TABLE user_location
(
userId INT NOT NULL PRIMARY KEY,
	lat decimal(9,7),
	lng decimal(9,7)
);

-- Users Table
DROP TABLE IF EXISTS users;
CREATE TABLE users
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	username varchar(25),
	password varchar(25),
	gcm_regid text,
	loginType INT,
	fbId text,
	twitterId text,
	googleId text,
	pictureURL text
);

-- Haystack Users
DROP TABLE IF EXISTS haystack_users;
CREATE TABLE haystack_users
(
	haystackId INT,
	userId INT 
);

-- Haystack Active Users
DROP TABLE IF EXISTS haystack_active_users;
CREATE TABLE haystack_active_users
(
	haystackId INT,
	userId INT 
);

-- Haystack Banned Users
DROP TABLE IF EXISTS haystack_banned_users;
CREATE TABLE haystack_banned_users
(
haystackId INT,
userId INT
);

-- Location Sharing
DROP TABLE IF EXISTS location_sharing;
CREATE TABLE location_sharing
(
	id INT PRIMARY KEY AUTO_INCREMENT,
	senderId INT,
	senderName TEXT,
	receiverId INT,
	receiverName TEXT,
	timeLimit datetime,
	shareBack BOOLEAN
);