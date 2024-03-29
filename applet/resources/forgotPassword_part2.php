<?php session_start(); ?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head>
  <title>Robots -- Retrieve Lost Password</title>
  <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
  <link rel="shortcut icon" href="robot.ico"/>
  <link rel="stylesheet" type="text/css" href="master.css"/>
  <script language="javascript">
   // This function enables / disables textboxes.
   function enable(obj)
   {
    if(obj.value == "username")
    {
     document.forms[0].username.focus();
     document.forms[0].username.disabled = false;
     document.forms[0].answer.blur();
     document.forms[0].answer.disabled = true;
    }
    if(obj.value == "question")
    {
     document.forms[0].username.blur();
     document.forms[0].username.disabled = true;
     document.forms[0].answer.focus();
     document.forms[0].answer.disabled = false;
    }
   }
  </script>
 </head>

 <body>
  <h1>Email lost password -- part 2</h1>

   <?php
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

    if(isset($_SESSION['email'])) {
     $email = $_SESSION['email'];
     $query = sprintf("SELECT question, username FROM robots WHERE email='%s'",
         mysql_real_escape_string($email));
     $result = mysql_query($query);
     if($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
      $question = $row['question'];
      $username = $row['username'];
	 }
	 else {
      die("Error! Cannot retrieve information from the database!\n<br/>\n");
	 }
    }
    else {
     die("Error! You should not be here!\n<br/>\n");
    }

    if($_POST) {
     $recovery_type = $_POST['recovery_type'];
     $mail = true;
 
     if($recovery_type == 'username') {
      $username = $_POST['username'];
      //make sure the user and the password match.
      $query = sprintf("SELECT username, email FROM robots WHERE username='%s' AND email='%s'",
          mysql_real_escape_string($username),
          mysql_real_escape_string($email));
      $exists = mysql_query($query);
      if(mysql_affected_rows() == 0) {
       echo "Incorrect username\n<br/>\n";
       $mail = false; //Do not send an email.
      }
     }

     if($recovery_type == 'question') {
      $answer = $_POST['answer'];
      //make sure the user and the password match.
      $query = sprintf("SELECT username, answer, email FROM robots WHERE answer='%s' AND email='%s'",
          mysql_real_escape_string(strtoupper($answer)),
          mysql_real_escape_string($email));
      $exists = mysql_query($query);
      if(mysql_affected_rows() == 0) {
       echo "Incorrect answer\n<br/>\n";
       $mail = false; //Do not send an email.
      }
     }

     if($mail) { 
      //Generate new password.
      $new_password = "";
      for ($i=0; $i<8; $i++) {
       $new_password .= rand(0, 36) < 26 ? chr(rand(65,90)) : chr(rand(48,57));
      }
      //Add new password to the database.
      $query = sprintf("UPDATE robots SET password='%s' WHERE username='$username'",
          md5($new_password));
      $result = mysql_query($query);
      if(!$result) {
       die("Failed to update password.\n<br>\n");
      }

      $subject = "Robots password reset";
      $message = "Your new password is '".$new_password."'\n".
                 "And your username is '".$username."', in case you forgot it too.\n".
                 "Go to http://lucky.cs.montana.edu tu use it.\n".
                 "It is advised to change it to something else.";
      $headers = 'From: Robots Java Game <robots.java@gmail.com>' . "\r\n";

      if(mail($email, $subject, $message, $headers)) {
       echo "Password recovery instructions sent to '".$email."'\n<br/>\n";
      }
      else {
       echo "Could not send email to '".$email."'\n<br/>\n";
      }
     }
    }

    mysql_close($connect);
   ?>

  <form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post"> 
   <input type="hidden" name="email" value="<?php $_POST['email']; ?>"/>
   <table>
     <tr><td>Pick one:</td>   </td><td><input type="radio" name="recovery_type" value="username" onClick="enable(this)" checked>Username</td></tr>
     <tr><td/>                     <td><input type="radio" name="recovery_type" value="question" onClick="enable(this)"        >Question</td></tr>
     <tr><td><label for="username">Username:</label> </td><td><input type="text" name="username" maxlength="25"/></td></tr>
     <tr><td>                      Security Question:</td><td><?php echo $question; ?>                           </td></tr>
     <tr><td><label for="answer"  >Answer:  </label> </td><td><input type="text" name="answer"   maxlength="25"/></td></tr>
     <tr><td/><td><button type="submit" name="submit">Email</button></td></tr>
   </table>
  </form>
   
  <br/>

  <a href="forgotPassword.php">&lt;&lt;Back</a>

 </body>
</html>
