<?php include('session.php'); ?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head>
  <title>Robots -- Edit profile</title>
  <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
  <link rel="shortcut icon" href="robot.ico"/>
  <link rel="stylesheet" type="text/css" href="master.css"/>
 </head>

 <body>
  <div class="header">
   <table>
    <tr>
     <td>Hello, <?php echo $_SESSION['username']; ?>.</td>
     <td><a href="main.php">Main             </a>    </td>
     <td><a href="robots.php">Game      </a>    </td>
     <td><a href="highScores.php">High Scores</a>    </td>
     <td><a href="profile.php">View Profile  </a>    </td>
     <td>Donate!                             </a>    </td>
     <td><a href="logout.php">Logout         </a>    </td>
    </tr>
   </table>
  </div>

  <br/>

  <h1>Donate!</h1>

  <strong>
   <p>Remember that the only reason the Robots Game is here is because of donations!</p>
   <p>Philanthropists also earn an extra safe teleport for each $10 they donate!</p>
  </strong>

  <?php

   include ('databaseLogin.php');
   $username = $_SESSION['username'];

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

   if($_POST) {
    //Variable initialization
    $amount = $_POST['amount'];
    $errors = array();

    //make sure the donation amount is a number and POSITIVE!!!
    if(!is_numeric($amount)) {
     array_push($errors, "Please enter a number!!!.");
    }
    else {
     if($amount <= 0) {
      array_push($errors, "Please enter a POSITIVE amount!");
     }
     else {
      //Rounds the number to  the nearest hundredth.
      if ($amount < .01) {
       array_push($errors, "Please enter a larger amount.");
      }
      else {
       $amount = number_format($amount, 2);
      }
     }
    }

    if(empty($errors)) {
     $old_amount_query = sprintf("SELECT amountDonated FROM robots WHERE username='%s'",
          mysql_real_escape_string($username));
     $old_amount_result = mysql_query($old_amount_query);
     if($old_amount_row = mysql_fetch_array($old_amount_result, MYSQL_ASSOC)) {
      // Increases the amount donated.
      $update_query = sprintf("UPDATE robots SET amountDonated=amountDonated+%s WHERE username='$username'",
          mysql_real_escape_string($amount)); //Just to be safe.
      $update_result = mysql_query($update_query);
      if($update_result) {
       echo "Thanks for donating $$amount!\n<br/>\n<br/>\n";

       // Find new safe teleports to add, if applicable.
       $amountPerFree = 10;
       $newSafeTeleports = intval($amount / $amountPerFree);
       $new_remainder = $amount % $amountPerFree;
       $old_remainder = $old_amount_row['amountDonated'] % $amountPerFree;
       if($new_remainder + $old_remainder >= $amountPerFree) {
        $newSafeTeleports++;
       }
       if($newSafeTeleports > 0) {
        $update_safeTeleports_query = sprintf("UPDATE robots SET safeTeleports=safeTeleports+%s WHERE username='$username'",
            mysql_real_escape_string($newSafeTeleports)); //Just to be safe.
        $update_safeTeleports_result = mysql_query($update_safeTeleports_query);
        if($update_safeTeleports_result) {
         if($newSafeTeleports == 1) {
          echo "You earned an extra safe teleport!\n<br/>\n<br/>\n";
         }
         else {
          echo "You earned an extra ".$newSafeTeleports." safe teleports!\n<br/>\n<br/>\n";
         }
        }
        else {
         echo "Error giving extra safe teleports!\n<br/>\n";
        }
       }
	  }
      else {
       echo "Error adding donation!\n<br/>\n";
      }
     }
     else {
       echo "Error finding old amount donated!\n<br/>\n";
     }
    }
    else {
     echo "ERRORS EXIST:";
     foreach ($errors as $error) {
      echo "\n<br/>\n$error";
     }
	 echo "\n<br/>\n<br/>\n";
    }
   }

  //Lists total donated by user.
  $query = sprintf("SELECT amountDonated FROM robots WHERE username='%s'",
      mysql_real_escape_string($username));
  $result = mysql_query($query);
  if($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
   echo "You have donated $".number_format($row['amountDonated'], 2)."\n<br/>\n";
  }
  else {
   echo "Could not find out how much you have donated\n<br/>\n";
  }

  mysql_close($connect);
  ?>

  <form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post">
   <table>

    <tr>
     <td><label for="amount">Amount to donate: $</label></td>
     <td><input type=text" name="amount" maxlength="25"/></td>
    </tr>

    <tr>
     <td/>
     <td><button type="submit" name="submit">Donate!</button></td>
    </tr>

   </table>
  </form>

 </body>
</html>
