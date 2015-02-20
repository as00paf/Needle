<?php

/*
Our "config.inc.php" file connects to database every time we include or require
it within a php script.  Since we want this script to add a new user to our db,
we will be talking with our database, and therefore,
let's require the connection to happen:
*/
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {

	// Make sure required fields are set
	if (!is_set($_POST['name']) || !is_set($_POST['timeLimit']) || !is_set($_POST['owner'])) {
        $response["success"] = 0;
        $response["message"] = "Missing infos";
        
        die(json_encode($response));
    }

	//Create Haystack
    $query = "INSERT INTO haystack ( name, owner, isPublic, timeLimit, zoneString, pictureURL ) VALUES ( :name, :owner, :isPublic, :timeLimit, :zone, :pictureURL  )";
    $query_params = array(
        ':name' => $_POST['name'],
        ':owner' => $_POST['owner'],
        ':isPublic' => $_POST['isPublic'],
        ':timeLimit' => $_POST['timeLimit'],
        ':zone' => $_POST['zone'],
        ':pictureURL' => $_POST['pictureURL']
    );
    
    // Execute query
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
		$haystack_id =  $db->lastInsertId();
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. Could not create haystack. Exception : ". $ex->getMessage();
        die(json_encode($response));
    }

	if( is_array( $_POST['haystack_user'] ) ) {
		foreach($_POST['haystack_user'] as $item){
			//Add users to haystack
			$query = "INSERT INTO haystack_users ( userId, haystackId ) VALUES ( :userId, :haystackId  )";
			$query_params = array(
				':haystackId' => $haystack_id,
				':userId' => $item
			);
			
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error. Could not add user to haystack. Exception : ". $ex->getMessage();
				die(json_encode($response));
			}
		}
	}else{
		$response["success"] = 0;
        $response["message"] = "haystack_user is not an array ! ". $ex->getMessage();
        die(json_encode($response));
	}
	
	
	if( is_array( $_POST['haystack_active_user'] ) ) {
		foreach($_POST['haystack_active_user'] as $item){
			$query = "INSERT INTO haystack_active_users ( userId, haystackId ) VALUES ( :userId, :haystackId  )";
			
			//Again, we need to update our tokens with the actual data:
			$query_params = array(
				':haystackId' => $haystack_id,
				':userId' => $item
			);
			
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error. Please Try Again! ". $ex->getMessage();
				die(json_encode($response));
			}
		}
	}else{
		$response["success"] = 0;
        $response["message"] = "haystack_active_user is not an array ! ". $ex->getMessage();
        die(json_encode($response));
	}
	
	
    //If we have made it this far without dying, we have successfully added
    //a new haystack to our database.  
	$response["success"] = 1;
    $response["message"] = "Haystack Successfully Created with id : ".$haystack_id . " ";
	$response["haystackId"] = $haystack_id;
    echo json_encode($response);
    
    
} else {
?>
	<h1>Create Haystack</h1> 
	<form action="createHaystack.php" method="post"> 
		    Name:<br /> 
		    <input type="text" name="name" placeholder="" /> 
		    <br /><br /> 
			
		    Time Limit:<br /> 
		    <input type="text" name="timeLimit" placeholder="" /> 
		    <br /><br /> 
			
			Owner :<br /> 
		    <input type="text" name="owner" placeholder="" /> 
		    <br /><br /> 
			
			Zone :<br /> 
		    <input type="text" name="zone" placeholder="" /> 
		    <br /><br /> 
			
			Picture URL :<br /> 
		    <input type="text" name="pictureURL" placeholder="" /> 
		    <br /><br />

			Haystack Users :<br /> 
		    <input type="text" name="haystack_user" placeholder="" /> 
		    <br /><br /> 
			
			Haystack Active Users :<br /> 
		    <input type="text" name="haystack_active_user" placeholder="" /> 
		    <br /><br /> 
			
		    <input type="submit" value="Create Haystack" /> 
		</form> 
	<?php
}

?>