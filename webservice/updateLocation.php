<?php
require("config.inc.php");

if (!empty($_POST)) {
	// Make sure required fields are not empty
	if (!is_set($_POST['lat']) || !is_set($_POST['lng']) || !is_set($_POST['userId'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";

		die(json_encode($response));
	}
	
	$query = "Select * FROM user_location WHERE userId = :userId";
    $query_params = array(
        ':userId' => $_POST['userId']
    );
  
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. Exception : " .$ex ;
    }

	$row = $stmt->fetch();
	if ($row == "" || $row == " ") {
		//INSERT
		$query = "INSERT INTO user_location ( userId, lat, lng ) VALUES ( :userId, :lat, :lng ) ";
		
		$query_params = array(
        ':userId' => $_POST['userId'],
		':lat' => $_POST['lat'],
		':lng' => $_POST['lng']
		);
		
		try {
			$stmt   = $db->prepare($query);
			$result = $stmt->execute($query_params);
		}
		catch (PDOException $ex) {
			$response["success"] = 0;
			$response["message"] = "Database Error. Couldn't add location in db ! Exception : " .$ex;
			die(json_encode($response));
		}
		
		$response["message"] = "Location Successfully Added! ".$row;
    }else{//UPDATE
		//New query
		$query = "UPDATE user_location SET lat=:lat, lng=:lng WHERE userId=:userId";
		
		$query_params = array(
        ':userId' => $_POST['userId'],
		':lat' => $_POST['lat'],
		':lng' => $_POST['lng']
		);
		
		try {
			$stmt   = $db->prepare($query);
			$result = $stmt->execute($query_params);
		}
		catch (PDOException $ex) {
			// For testing, you could use a die and message. 
			//die("Failed to run query: " . $ex->getMessage());
			
			//or just use this use this one:
			$response["success"] = 0;
			$response["message"] = "Database Error. Couldn't update location in db ! " .$ex ;
			die(json_encode($response));
		}
		$response["message"] = "Location Successfully Updated! ";
	}
	
    $response["success"] = 1;
    echo json_encode($response);
   
} else {
?>
		<h1>Update Location</h1> 
		<form action="updateLocation.php" method="post"> 
		    User Id:<br /> 
		    <input type="text" name="userId" placeholder="" /> 
		    <br /><br /> 
		    Lattitude:<br /> 
		    <input type="text" name="lat" placeholder="" /> 
		    <br /><br />
			Longitude:<br /> 
		    <input type="text" name="lng" placeholder="" /> 
		    <br /><br />
		    <input type="submit" value="Update Location" /> 
		</form> 
	<?php
}