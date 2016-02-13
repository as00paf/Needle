<?php

//load and connect to MySQL database stuff
require("config.inc.php");

if (!empty($_POST)) {
	// Make sure required fields are set
    if (!is_set($_POST['id']) || !is_set($_POST['haystackId'])) {
        $response["success"] = 0;
        $response["message"] = "Missing infos";
        
        die(json_encode($response));
    }
	
	//Check if user already added to haystack
	$query = "Select * FROM haystack_users WHERE id = :id AND haystackId = :haystackId";
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
        $response["message"] = "Database Error : id or haystackId not not found. Exception :" .$ex ;
		die(json_encode($response));
    }

	$row = $stmt->fetch();
	if ($row == "" || $row == " ") {
		//Add user to haystack
		$query = "INSERT IGNORE INTO haystack_users SET id = :id, haystackId = :haystackId";
		
		$query_params = array(
			':id' => $_POST['id'],
			':haystackId' => $_POST['haystackId']
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
			$response["message"] = "Database Error. Couldn't add user to haystack! " .$ex;
			die(json_encode($response));
		}
    }
	
	$response["message"] = "User Sucessfully Added!"; 
	
	
    $response["success"] = 1;
    echo json_encode($response);
   
} else {
?>
		<h1>Add User</h1> 
		<form action="addUser.php" method="post"> 
		    UserId:<br /> 
		    <input type="text" name="id" placeholder="" />
			
		    <br /><br /> 
			
		    HaystackId:<br /> 
		    <input type="text" name="haystackId" placeholder="" /> 
		    <br />
			
		    <input type="submit" value="Add User" /> 
		</form> 
	<?php
}