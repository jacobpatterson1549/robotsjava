<?php session_start(); ?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head>
  <title>Robots -- Retrieve Lost Password</title>
  <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
  <link rel="shortcut icon" href="robot.ico"/>
  <link rel="stylesheet" type="text/css" href="master.css"/>
  <link rel="stylesheet" type="text/css" href="cellCenter.css"/>
 </head>

 <body>
  <h1>Email lost password -- part 1</h1>

   <?php
    if($_POST) {
     include ('databaseLogin.php');

     //connect to MySQL
     $connect = mysql_connect($db_host, $db_user, $db_pwd);
     if(!$connect) {
      die("Could not make a connection to MySQL:\n<br/>\n".mysql_error());
     }

     //select the database to work with
     $db_selected = mysql_select_db($database, $connect);
     if(!$db_selected) {
      die("Unable to select database:\n<br/>\n".mysql_error());
     }

     $email = $_POST['email'];
 
     //make sure the user and the password match.
     $query = sprintf("SELECT username, email FROM robots WHERE email='%s'",
         mysql_real_escape_string($email));
     $exists = mysql_query($query);
     if(mysql_affected_rows() == 0) {
      echo "No account registered with '".$email."'\n<br/>\n";
     }
     else {
      session_register('email');
	  $_SESSION['email'] = $email;
      echo '<meta http-equiv="refresh" content="0, url=forgotPassword_part2.php"/>';
     }

     mysql_close($connect);
   }
  ?>

  <form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post"> 
   <table>
     <tr><td><label>Email:</label></td><td><input type="text" name="email" maxlength="128"/></td></tr>
     <tr><td/><td><button type="submit" name="submit">Continue</button></td></tr>
   </table>
  </form>
   
  <br/>

  <a href="login.php">&lt;&lt;Back</a>

 </body>
</html>
