<?php

require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {

	// Make sure required fields are not empty
	if (!is_set($_POST['id'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";

		die(json_encode($response));
	}

	//public query
	$query = "Select * FROM haystack WHERE isPublic = :isPublic";

	$query_params = array(
			':isPublic' => 1
		);

	try {
		$stmt   = $db->prepare($query);
		$result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
		$response["success"] = 0;
		$response["message"] = "Database Error on public query : " . $ex;
		die(json_encode($response));
	}

	$rows = $stmt->fetchAll();
	$response["public_haystacks"] = array();

	if ($rows) {
		$response["success"] = 1;
		$response["message"] = "Public Haystacks Available!";
		
		foreach ($rows as $row) {
			$haystack           		= array();
			$haystack["id"]				= $row["id"];
			$haystack["name"]    		= $row["name"];
			$haystack["owner"] 			= $row["owner"];
			$haystack["isPublic"] 		= $row["isPublic"];
			$haystack["timeLimit"] 		= $row["timeLimit"];
			$haystack["zoneString"] 	= $row["zoneString"];
			$haystack["picture"] 	= $row["picture"];
			
			//user query
			$query = "SELECT *
					  FROM haystack_users 
					  INNER JOIN users
					  ON users.id = haystack_users.id
					  WHERE haystackId = :id";

			$query_params = array(
					':id' => $row["id"]
				);

			//execute query
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error While retrieving public users : " . $ex;
				die(json_encode($response));
			}
	 
			$users = $stmt->fetchAll();
			$haystack["users"] = array();
			if($users){
				foreach ($users as $user) {
					array_push($haystack["users"], $user);
				}
			}
			
			//active user query
			$query = "Select * FROM haystack_active_users 
					  INNER JOIN users
					  ON users.id = haystack_active_users.id
					  WHERE haystackId = :id";

			$query_params = array(
					':id' => $row["id"]
				);

			//execute query
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error while retrieving public active users : " . $ex;
				die(json_encode($response));
			}
	 
			$activeUsers = $stmt->fetchAll();
			$haystack["activeUsers"] = array();
			if($activeUsers){
				foreach ($activeUsers as $activeUser) {
					array_push($haystack["activeUsers"], $activeUser);
				}
			}		
			
			//update our repsonse JSON data
			array_push($response["public_haystacks"], $haystack);
		}
	} else {
		$response["success"] = 0;
		$response["message"] = "No Public Haystacks Available!";
	}

	//private query
	/*$query = 	"SELECT * 
				FROM haystack  
				INNER JOIN haystack_users
				ON haystack.id = haystack_users.haystackId 
				AND haystack_users.id = :id
				WHERE haystack.isPublic = :isPublic";*/

	$query = 	"SELECT * 
				FROM haystack  
				INNER JOIN haystack_users
				ON haystack.id = haystack_users.haystackId
				WHERE haystack.isPublic = :isPublic
				AND haystack_users.id = :id";
				
	$query_params = array(
			':isPublic' => 0,
			':id' => $_POST['id']
		);

	//execute query
	try {
		$stmt   = $db->prepare($query);
		$result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
		$response["success"] = 0;
		$response["message"] = "Database Error while".$ex;
		die(json_encode($response));
	}

	// Finally, we can retrieve all of the found rows into an array using fetchAll 
	$rows = $stmt->fetchAll();

	$response["private_haystacks"] = array();
	if ($rows) {
		$response["success"] = 1;
		$response["message"] = "Private Haystacks Available!";    
		
		foreach ($rows as $row) {
			$haystack           		= array();
			$haystack["id"]				= $row["id"];
			$haystack["name"]    		= $row["name"];
			$haystack["owner"] 			= $row["owner"];
			$haystack["isPublic"] 		= $row["isPublic"];
			$haystack["timeLimit"] 		= $row["timeLimit"];
			$haystack["zoneString"] 	= $row["zoneString"];
			$haystack["picture"] 	= $row["picture"];
			
			//user query
			$query = "Select * FROM haystack_users 
					  INNER JOIN users
					  ON users.id = haystack_users.id
					  WHERE haystackId = :id";

			$query_params = array(
					':id' => $row["id"]
				);

			//execute query
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error While retrieving private users : " . $ex;
				die(json_encode($response));
			}
	 
			$users = $stmt->fetchAll();
			$haystack["users"] = array();
			if($users){
				foreach ($users as $user) {
					array_push($haystack["users"], $user);
				}
			}
			
			//active user query
			$query = "Select * FROM haystack_active_users
					  INNER JOIN users
					  ON users.id = haystack_active_users.id
					  WHERE haystackId = :id";

			$query_params = array(
					':id' => $row["id"]
				);

			//execute query
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error While retrieving private active users : " . $ex;
				die(json_encode($response));
			}
	 
			$activeUsers = $stmt->fetchAll();
			$haystack["activeUsers"] = array();
			if($activeUsers){
				foreach ($activeUsers as $activeUser) {
					array_push($haystack["activeUsers"], $activeUser);
				}
			}
			
			//update our repsonse JSON data
			array_push($response["private_haystacks"], $haystack);
		}
	} else {
		$response["success"] = 0;
		$response["private_message"] = "No Private Haystacks Available!";
		die(json_encode($response));
	}

	// echoing JSON response
	echo json_encode($response);
}else{
?>
		<h1>GetHaystacks</h1> 
		<form action="getHaystacks.php" method="post"> 
		    HaystackId:<br /> 
		    <input type="text" name="id" placeholder="" />
		    <br />
			
		    <input type="submit" value="Get Haystacks" /> 
		</form> 
	
<?php
	}
?>

