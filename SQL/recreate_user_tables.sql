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
	pictureURL text,
	coverURL text
);
