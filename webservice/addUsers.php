<?php

//load and connect to MySQL database stuff
require("config.inc.php");

if (!empty($_POST)) {

	// Make sure required fields are not empty
	if (empty($_POST['haystackId']) || $_POST['users'] == null) {
		// Create some data that will be the JSON response 
		$response["success"] = 0;
		$response["message"] = "Missing infos";
		
		//die will kill the page and not execute any code below, it will also
		//display the parameter... in this case the JSON data our Android
		//app will parse
		die(json_encode($response));
	}

	if( is_array( $_POST['users'] ) ) {
		foreach($_POST['users'] as $item){
			$query = "INSERT INTO haystack_users ( id, haystackId ) VALUES ( :id, :haystackId  )";
			
			//Again, we need to update our tokens with the actual data:
			$query_params = array(
				':haystackId' => $_POST['haystackId'],
				':id' => $item
			);
			
			try {
				$stmt   = $db->prepare($query);
				$result = $stmt->execute($query_params);
			}
			catch (PDOException $ex) {
				$response["success"] = 0;
				$response["message"] = "Database Error. Unable to add user ". $item . " to haystack ". $ex->getMessage();
				die(json_encode($response));
			}
		}
	}else{
		$response["success"] = 0;
        $response["message"] = "haystack_user is not an array ! ". $ex->getMessage();
        die(json_encode($response));
	}
	
	$response["message"] = "Users added successfuly !";
    $response["success"] = 1;
    echo json_encode($response);
   
} else {
?>
		<h1>Add Comment</h1> 
		<form action="addcomment.php" method="post"> 
		    Username:<br /> 
		    <input type="description" name="username" placeholder="username" />
		    <br /><br /> 
		    Title:<br /> 
		    <input type="description" name="description" placeholder="post description" />
		    <br /><br />
			Message:<br /> 
		    <input type="description" name="message" placeholder="post message" />
		    <br /><br />
		    <input type="submit" value="Add Comment" /> 
		</form> 
	<?php
}