<?php

//Load and connect to MySQL database stuff
require("config.inc.php");

if (!empty($_POST)) {

	// Make sure required fields are set
    if (!is_set($_POST['userId']) || !is_set($_POST['haystackId'])) {
        $response["success"] = 0;
        $response["message"] = "Missing infos";
        
        die(json_encode($response));
    }

	//Check if user is active in haystack
	$query = "Select * FROM haystack_active_users WHERE userId = :userId AND haystackId = :haystackId";
    $query_params = array(
        ':userId' => $_POST['userId'],
		':haystackId' => $_POST['haystackId']
    );
  
	//Execute query
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error : userId or haystackId not not found. Exception : " .$ex ;
		die(json_encode($response));
    }

	$row = $stmt->fetch();
	if ($row == "" || $row == " ") {
		$response["success"] = 0;
        $response["message"] = "Error. User : ".$_POST['userId']." not active in haystack. \\n";
		die(json_encode($response));
    }else{
		//Remove user from haystack
		$query = "DELETE FROM haystack_active_users WHERE userId = :userId AND haystackId = :haystackId";
		
		$query_params = array(
			':userId' => $_POST['userId'],
			':haystackId' => $_POST['haystackId']
		);
		
		try {
			$stmt   = $db->prepare($query);
			$result = $stmt->execute($query_params);
		}
		catch (PDOException $ex) {
			$response["success"] = 0;
			$response["message"] = "Database Error. Couldn't deactivate user ! Exception : " . $ex;
			die(json_encode($response));
		}
	}
	
	$response["message"] = "User Sucessfully Deactivated";	
    $response["success"] = 1;
    echo json_encode($response);
   
} else {
?>
		<h1>Deactivate Users</h1> 
		<form action="deactivateUsers.php" method="post"> 
		    UserId:<br /> 
		    <input type="text" name="userId" placeholder="" /> 
			
		    <br /><br /> 
			
		    HaystackId:<br /> 
		    <input type="text" name="haystackId" placeholder="" /> 
		    <br />
			
		    <input type="submit" value="Deactivate User" /> 
		</form> 
	
<?php
	}
?>