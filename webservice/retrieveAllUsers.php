<?php

require("config.inc.php");

if (!empty($_POST)) {
	if (empty($_POST['id'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";
		
		die(json_encode($response));
	}

	$query = "Select * FROM users WHERE id != :id";
	$query_params = array(
			':id' => $_POST['id']
		);

	try {
		$stmt   = $db->prepare($query);
		$result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
		$response["success"] = 0;
		$response["message"] = "Database Error! Exception : " . $ex;
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
			
			//update our repsonse JSON data
			array_push($response["users"], $users);
		}
	} else {
		$response["success"] = 0;
		$response["message"] = "No Users Available!";
	}

	// echoing JSON response
	echo json_encode($response);

} else {
?>
	<h1>Retrieve All Users</h1> 
	<form action="retrieveAllUsers.php" method="post"> 
	    Haystack Id:<br /> 
	    <input type="description" name="haystackId" value="" />
	    <br /><br /> 
	    <input type="submit" value="Register New User" /> 
	</form>
	<?php
}

?>
