
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" >
    <title>Pet Stop Doc</title>
    <link rel="stylesheet" type="text/css" href="petstop.css" >
 </head> 
 
 
 
<body>


<div id="header">
	  
</div>

<div id="container"><div id="img"><img src="images/Logo2.gif" alt="Pet Stop Doc Logo" /></div>

<div id="right" class="column">
	<div id="info"><img src="images/Logo3.png" alt="Pet Stop Doc Logo" width="170px" height="180px">  
	<hr /><dl>
	<dt>Phone:</br> (505) 466-6008<br /><br /></dt>
	</dl>
	
<hr />

	<?php
	 include 'external.php';
	 ?>


	</div>
</div>

	
<div id="center" class="column">


<div class="section">
<form name="contactform" method="post" action="send_form_email.php">
<table>
<tr>
 <td valign="top">
  <label for="first_name">First Name*</label>
 </td>
 <td valign="top">
  <input  type="text" name="first_name" maxlength="50" size="30">
 <hr></td>
</tr>
<tr>
 <td valign="top"">
  <label for="last_name">Last Name*</label>
 </td>
 <td valign="top">
  <input  type="text" name="last_name" maxlength="50" size="30">
 <hr></td>
</tr>
<tr>
 <td valign="top">
  <label for="address">Street Address*</label>
 </td>
 <td valign="top">
  <input  type="text" name="address" maxlength="80" size="30">
 <hr></td>
</tr>
<tr>
 <td valign="top">
  <label for="city">City*</label>
 </td>
 <td valign="top">
  <input  type="text" name="city" maxlength="80" size="30">
<hr> </td>
</tr>
<tr>
 <td valign="top">
  <label for="postal">Postal / Zip Code*</label>
 </td>
 <td valign="top">
  <input  type="text" name="postal" maxlength="80" size="30">
 <hr></td>
</tr>
<tr>
 <td valign="top">
  <label for="email">Email*</label>
 </td>
 <td valign="top">
  <input  type="text" name="email" maxlength="80" size="30">
 <hr></td>
</tr>
<tr>
 <td valign="top">
  <label for="telephone">Home Phone*</label>
 </td>
 <td valign="top">
  <input  type="text" name="telephone" maxlength="30" size="30">
<hr> </td>
</tr>
<tr>
 <td valign="top">
  <label for="pet_name">Pet's Name*</label>
 </td>
 <td valign="top">
  <input  type="text" name="pet_name" maxlength="30" size="30">
 <hr></td>
</tr>
<tr>
 <td valign="top"><label for="species">Species*</label>
  <select name="species">
<option value="Canine">Canine</option>
<option value="Feline">Feline</option>
<option value="Avian">Avian</option>
<option value="Exotic">Exotic</option>
</select>
 <hr></td>

</tr>
<tr>
 <td valign="top"><label for="current">Are your pet's vaccines current?*</label>
  <select name="currnet">
<option value="Yes">Yes</option>
<option value="No">No</option>
<option value="I don't know">I don't know</option>
</select>
 <hr></td>

</tr>

<tr>
 <td valign="top">
  <label for="comments">Additional Information*</label>
 </td>
 <td valign="top">
  <textarea  name="comments" maxlength="1000" cols="25" rows="6"></textarea>
 </td>
</tr>
<tr>
 <td colspan="2" style="text-align:center">
  <input type="submit" value="Submit">
 </td>
</tr>
</table>
</form>
</div>

<div id="fineprint">
<h5><a href="legal/payment.html">Payment</a>&nbsp; <a href="legal/privacy.html">Privacy Policy</a> <a href="legal/return.html">Return-refund</a> <a href="legal/shipping.html">Shipping Policy</a></h5>
</div>
</div>



	<div id="left" class="column">
		<!-- INCLUDE VERTICAL NAVIGATION EXTERNALLY -->
		<?php 
		include 'externalNav.php';
		?>	

	
	<div id="youtube">
	<p>Friend us on Facebook and Check out usefull pet videos<br /><a href="http://www.youtube.com/user/AmerVetMedAssn" target="_blank"><br /><img border="0" src="images/youtube.gif" alt="AmerVetMedAssn" /></a><a href="http://www.facebook.com/pages/Pet-Stop-Doc/395716637185830" target="_blank"><img border="0" hspace="10" src="images/fbook.png" alt="Facebook" /></a></p>
	</div>
	</div>
	

	</div>
	</body>
</html>