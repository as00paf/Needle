USE Needle;
DROP TABLE IF EXISTS haystack;
CREATE TABLE haystack
(
id INT PRIMARY KEY AUTO_INCREMENT,
name TEXT,
owner INT,
isPublic  TINYINT(1),
timeLimit datetime,
zoneString TEXT,
pictureURL TEXT
);
DROP TABLE IF EXISTS needle_location;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS user_location;
CREATE TABLE user_location
(
userId INT NOT NULL PRIMARY KEY,
lat decimal(9,7),
lng decimal(9,7)
);
DROP TABLE IF EXISTS users;
CREATE TABLE users
(
id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
username varchar(25),
password varchar(25)
);
DROP TABLE IF EXISTS haystack_users;
CREATE TABLE haystack_users
(
haystackId INT,
userId INT 
);
DROP TABLE IF EXISTS haystack_active_users;
CREATE TABLE haystack_active_users
(
haystackId INT,
userId INT 
);
DROP TABLE IF EXISTS haystack_banned_users;
CREATE TABLE haystack_banned_users
(
haystackId INT,
userId INT
);
