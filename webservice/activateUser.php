<?php

//load and connect to MySQL database stuff
require("config.inc.php");

if (!empty($_POST)) {
	// Make sure required fields are set
    if (!is_set($_POST['userId']) || !is_set($_POST['haystackId'])) {
        $response["success"] = 0;
        $response["message"] = "Missing infos";
        
        die(json_encode($response));
    }
	
	//Check if user is part of haystack
	$query = "Select * FROM haystack_users WHERE userId = :userId AND haystackId = :haystackId";
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
        $response["message"] = "Database Error : userId or haystackId not not found. Exception : " . $ex ;
		die(json_encode($response));
    }

	$row = $stmt->fetch();
	if ($row == "" || $row == " ") {//Add user to haystack
		$query = "INSERT IGNORE INTO haystack_users SET userId = :userId, haystackId = :haystackId";
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
			$response["message"] = "Database Error. Couldn't add user to haystack! Exception : " .$ex;
			die(json_encode($response));
		}
		
		$msg = "User was added to haystack";
    }else{//User already user of haystack
		$msg = "User was already part of haystack";
	}
	
	//Activate user
	$query = "INSERT IGNORE INTO haystack_active_users SET userId = :userId, haystackId = :haystackId";
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
		$response["message"] = "Database Error. Couldn't activate user ! Exception : " .$ex;
		die(json_encode($response));
	}
	
	$response["message"] = "User Sucessfully Activated " . $msg;
    $response["success"] = 1;
    echo json_encode($response);
} else {
?>
		<h1>Activate User</h1> 
		<form action="activateUser.php" method="post"> 
		    UserId:<br /> 
		    <input type="text" name="userId" placeholder="" /> 
			
		    <br /><br /> 
			
		    HaystackId:<br /> 
		    <input type="text" name="haystackId" placeholder="" /> 
		    <br />
			
		    <input type="submit" value="Activate User" /> 
		</form> 
	<?php
}