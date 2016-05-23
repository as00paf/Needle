USE needle;

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
	pictureURL TEXT,
	coverURL TEXT
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
	email text,
	username varchar(25),
	password varchar(25),
	gcm_regid text,
	loginType INT,
	fbId text,
	twitterId text,
	googleId text,
	pictureURL text,
	coverURL text
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

-- Haystack Pins
DROP TABLE IF EXISTS haystack_pins;
CREATE TABLE haystack_pins
(
	id INT PRIMARY KEY AUTO_INCREMENT,
	lat decimal(9,7),
	lng decimal(9,7),
	ownerId INT,
	haystackId INT, 
	text TEXT,
	addedAt datetime
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
	senderPictureURL TEXT,
	senderName TEXT
);

-- Friendship
DROP TABLE IF EXISTS friendship;
CREATE TABLE friendship
(
	userId INT not null,
	friendId INT not null,
	status INT,
	acceptDate datetime,
	CONSTRAINT pk_friends primary key (userId, friendId)
);