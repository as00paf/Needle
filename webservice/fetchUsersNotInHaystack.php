<?php

require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
	// Make sure required fields are not empty
	if (!is_set($_POST['haystackId'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";
		
		die(json_encode($response));
	}

	//Fetch Users Not In Haystack
	$query =   "SELECT id, username
				FROM users
				LEFT JOIN haystack_users
				ON users.id = haystack_users.userId AND haystack_users.haystackId = :haystackId
				WHERE haystack_users.userId IS NULL;";

	$query_params = array(
			':haystackId' => $_POST['haystackId']
		);

	//Execute query
	try {
		$stmt   = $db->prepare($query);
		$result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
		$response["success"] = 0;
		$response["message"] = "Database Error!";
		die(json_encode($response));
	}

	$rows = $stmt->fetchAll();
	$response["users"] = array();

	if ($rows) {
		$response["success"] = 1;
		$response["message"] = "Users Available!";
		
		foreach ($rows as $row) {
			$users           		= array();
			$users["id"]			= $row["id"];
			$users["username"]    	= $row["username"];
			
			array_push($response["users"], $users);
		}
	} else {
		$response["success"] = 0;
		$response["message"] = "No Users Available!";
	}

	// Echoing JSON response
	echo json_encode($response);
}else{
?>
		<h1>Fetch Users Not In Haystack</h1> 
		<form action="fetchUsersNotInHaystack.php" method="post"> 
		    HaystackId:<br /> 
		    <input type="text" name="haystackId" placeholder="" /> 
		    <br />
			
		    <input type="submit" value="Fetch Active Users" /> 
		</form> 
	
<?php
	}
?>
