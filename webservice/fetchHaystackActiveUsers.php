<?php

require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
	// Make sure required fields are set
	if (!is_set($_POST['id']) || !is_set($_POST['haystackId'])) {
			$response["success"] = 0;
			$response["message"] = "Missing infos";
			
			die(json_encode($response));
	}

	//Fetch Haystack Active Users
	$query =   "Select * 
				FROM haystack_active_users 
				JOIN users
				ON haystack_active_users.id = users.id
				WHERE id != :id AND haystackId = :haystackId";

	$query_params = array(
			':id' => $_POST['id'],
			':haystackId' => $_POST['haystackId']
		);

	//Execute query
	try {
		$stmt   = $db->prepare($query);
		$result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
		$response["success"] = 0;
		$response["message"] = "Database Error : id or haystackId not not found. Exception :" . $ex;
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
	
	echo json_encode($response);
}else{
?>
		<h1>Fetch Active Users</h1> 
		<form action="fetchHaystackActiveUsers.php" method="post"> 
		    UserId:<br /> 
		    <input type="description" name="id" placeholder="" />
			
		    <br /><br /> 
			
		    HaystackId:<br /> 
		    <input type="description" name="haystackId" placeholder="" />
		    <br />
			
		    <input type="submit" value="Fetch Active Users" /> 
		</form> 
	
<?php
	}
?>
