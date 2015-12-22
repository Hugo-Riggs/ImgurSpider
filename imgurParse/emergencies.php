
<html>

	<!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
	<!-- jQuery library -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<!-- Latest compiled JavaScript -->
	<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
		 

	 <head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" >
		<title>Pet Stop Doc</title>
		<link rel="stylesheet" type="text/css" href="petstop.css" >
		<meta name="viewport" content="width=device-width, initial-scale=1">
	 </head> 
 
 
<body>
 <!-- container has all the content -->
		<div class="container" id="content" tabindex="-1">
	<!-- HEADER -->
			<div class="row" id="header row">
				<div class="col-sm-12" id="header" > 
					<img class="pull-right" src="images/Logo2.gif" alt="Pet Stop Doc Logo" />
				</div>
			</div>


		<div class="row" id="body row">
	<!-- Navigation column -->
			<div class="col-sm-3" id="left column">
				<div class="row" id="nav"><?php include 'externalNav.php' ?></div>
	<!-- Row for youtube links-->
				<div class="row">
					<p>Friend us on Facebook and Check out usefull pet videos
					</p>
					
					<a href="http://www.youtube.com/user/AmerVetMedAssn" target="_blank">
						<img border="0" height="40px" src="images/youtube.gif" alt="AmerVetMedAssn" />
					</a>
					<a href="http://www.facebook.com/pages/Pet-Stop-Doc/395716637185830" target="_blank">
						<img border="0" hspace="10" height="40px" src="images/fbook.png" alt="Facebook" />
					</a>
				</div>
			</div>
	<!-- CENTER TEXT -->
			<div class="col-sm-6" id="middle column">
				<div id="vaccines">
				<h3>
				<span style="font-size:20px;">If you have an emergency after hours please contact the emergency clinic</span>
				</h3>
				</div>
				<br>
				<div id="vaccines">
				<p>
				Veterinary Emergency and Specialty Center of Santa Fe</br >  
				Located at:</br >  
				2001 Vivigen Way
				Santa Fe, NM 87505
				Phone: (505) 984-0625</br >
				Fax: (505) 984-8705</br >
				<a href="http://www.vescnm.com" target="_blank">vescnm.com</a>
				</p>
				</div>	
			</div>
	<!-- Row with logo and contact -->
			<div class="col-sm-3" id="Right column">
				<img class="pull-right" src="images/Logo3.png" alt="Pet Stop Doc Logo" width="170px" height="180px">  
				<div class="row">
					<div class="pull-right">
						<hr />
						<dl>
							<dt>Phone:</br> (505) 466-6008<br /><br /></dt>
						</dl>
						<hr />	
						<?php include 'external.php'; ?>
					</div>
				</div>
			</div>
		</div>

		<div class="row" id="footer">
			<div class="col-sm-12" id="fineprint">
				<div class="text-center"
					<h5>
					<a href="legal/payment.html">Payment</a>
					<a href="legal/privacy.html">Privacy Policy</a>
					<a href="legal/return.html">Return-refund</a>
					<a href="legal/shipping.html">Shipping Policy</a>
					</h5>
				</div>
			</div>
		</div>
	
	</div>
	</body>
</html>