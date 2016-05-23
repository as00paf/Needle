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