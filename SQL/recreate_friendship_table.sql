-- Friendship Table
DROP TABLE IF EXISTS friendship;
CREATE TABLE friendship
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
