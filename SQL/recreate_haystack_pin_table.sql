USE Needle;

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