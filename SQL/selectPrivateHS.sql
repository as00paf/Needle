SELECT * 
FROM haystack 
INNER JOIN haystack_users 
ON haystack.id = haystack_users.haystackId 
AND haystack_users.userId = 2 
WHERE haystack.isPublic = 0