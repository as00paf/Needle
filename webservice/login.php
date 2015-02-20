<?php
require("config.inc.php");

if (!empty($_POST)) {
	// Make sure required fields are not empty
	if (!is_set($_POST['username']) || !is_set($_POST['password'])) {
		$response["success"] = 0;
		$response["message"] = "Missing infos";

		die(json_encode($response));
	}
    $query = " 
            SELECT * FROM users WHERE  username = :username";
    
    $query_params = array(
        ':username' => $_POST['username']
    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. Username not found in Database. Exception : " . $ex;
        die(json_encode($response));
        
    }
    
    $validated_info = false;
    
    $row = $stmt->fetch();
	$login_ok = false;
    if ($row) {
        //if we encrypted the password, we would unencrypt it here, but in our case we just
        //compare the two passwords
        if ($_POST['password'] === $row['password']) {
            $login_ok = true;
        }
    }
    
    if ($login_ok) {
        $response["success"] = 1;
		$response["userId"] = $row['id'];
        $response["message"] = "Login successful!";
        die(json_encode($response));
    } else {
        $response["success"] = 0;
        $response["message"] = "Invalid Credentials!";
        die(json_encode($response));
    }
} else {
?>
		<h1>Login</h1> 
		<form action="login.php" method="post"> 
		    Username:<br /> 
		    <input type="text" name="username" placeholder="username" /> 
		    <br /><br /> 
		    Password:<br /> 
		    <input type="password" name="password" placeholder="password" value="" /> 
		    <br /><br /> 
		    <input type="submit" value="Login" /> 
		</form> 
		<a href="register.php">Register</a>
	<?php
}

?> 
