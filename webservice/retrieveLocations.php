<?php
require("config.inc.php");

if (!empty($_POST)) {
	// Make sure required fields are not empty
	if (!is_set($_POST['haystackId'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";

		die(json_encode($response));
	}

	$query =   "SELECT * 
				FROM user_location 
				INNER JOIN haystack_active_users ON user_location.id = haystack_active_users.id
				INNER JOIN haystack ON haystack.id = haystack_active_users.haystackId
				INNER JOIN users ON users.id = user_location.id
				WHERE haystack.id = :haystackId";
				
	$query_params = array(
		':haystackId' => $_POST['haystackId']
	);

	//execute query
	try {
		$stmt   = $db->prepare($query);
		$result = $stmt->execute($query_params);
	}
	catch (PDOException $ex) {
		$response["success"] = 0;
		$response["message"] = "Database Error! Exception : ". $ex;
		die(json_encode($response));
	}

	// Finally, we can retrieve all of the found rows into an array using fetchAll 
	$rows = $stmt->fetchAll();


	if ($rows) {
		$response["success"] = 1;
		$response["message"] = "Locations Available!";
		$response["locations"]   = array();
		
		foreach ($rows as $row) {
			$location             = array();
			$location["id"] = $row["id"];
			$location["name"] = $row["username"];
			$location["lat"]    = $row["lat"];
			$location["lng"]  = $row["lng"];
			
			array_push($response["locations"], $location);
		}
	} else {
		$response["success"] = 0;
		$response["message"] = "No Locations Available!";
	}

	echo json_encode($response);

} else {
?>
		<h1>Retrieve Locations</h1> 
		<form action="retrieveLocations.php" method="post"> 
		    Haystack Id:<br /> 
		    <input type="description" name="haystackId" placeholder="0" />
		    <br /><br />
			
		    <input type="submit" value="Retrieve Locations" /> 
		</form> 
	<?php
