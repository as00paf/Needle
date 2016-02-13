<?php
require("config.inc.php");

if (!empty($_POST)) {
	// Make sure required fields are not empty
	if (!is_set($_POST['haystackId']) || !is_set($_POST['id'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";

		die(json_encode($response));
	}
	
	//Check if user is in haystack
	$query = "Select * FROM haystack_users WHERE id = :id AND haystackId = :haystackId";
    $query_params = array(
        ':id' => $_POST['id'],
		':haystackId' => $_POST['haystackId']
    );
  
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. User with id : ".$_POST['id']." is not in haystack. Exception :" .$ex ;
		die(json_encode($response));
    }

	$row = $stmt->fetch();
	if ($row == "" || $row == " ") {
		$response["success"] = 0;
        $response["message"] = "Error! User with id : ".$_POST['id']." not in haystack.";
		die(json_encode($response));
    }else{
		//DELETE
		//New query
		$query = "DELETE FROM haystack_users WHERE id = :id AND haystackId = :haystackId";
		
		$query_params = array(
		':id' => $_POST['id'],
		':haystackId' => $_POST['haystackId']
		);
		
		try {
			$stmt   = $db->prepare($query);
			$result = $stmt->execute($query_params);
		}
		catch (PDOException $ex) {
			$response["success"] = 0;
			$response["message"] = "Database Error. Couldn't leave haystack ! Exception : " .$ex;
			die(json_encode($response));
		}
	}
	
	$response["message"] = "User successfully removed from haystack";
    $response["success"] = 1;
    echo json_encode($response);
   
} else {
?>
		<h1>Leave Haystack</h1> 
		<form action="leaveHaystack.php" method="post"> 
		    User Id:<br /> 
		    <input type="text" name="id" placeholder="0" />
		    <br /><br /> 
		    Haystack Id:<br /> 
		    <input type="text" name="haystackId" placeholder="0" /> 
		    <br /><br />
			
		    <input type="submit" value="Leave Haystack" /> 
		</form> 
	<?php
}