<?php
class ImageUploadStatus {
	public $result_code = 0;
	public $message = "Image upload failed.";
}
class ImageResults {
	public $name = "";
	public $url = "";
	public $size = "";
}
$status = new ImageUploadStatus ();
$results = new ImageResults ();

if ($_FILES ["file"] ["error"] > 0) {
	header ( "HTTP/1.1 400 Bad Request" );
	// echo "Error: " . $_FILES ["file"] ["error"] . "<br /> \n";
	$status->message = "Error: " . $_FILES ["file"] ["error"];
} else {
	if ($_FILES ["file"] ["error"] > 0) {
		// echo "Error: " . $_FILES ["file"] ["error"] . "<br /> \n";
		$status->message = "Error: " . $_FILES ["file"] ["error"];
	} else {
		// echo "Upload: " . $_FILES ["file"] ["name"] . "<br /> \n";
		// echo "Type: " . $_FILES ["file"] ["type"] . "<br /> \n";
		// echo "Size: " . ($_FILES ["file"] ["size"] / 1024) . " Kb<br /> \n";
		// echo "Stored in: " . $_FILES ["file"] ["tmp_name"] . "<br /> \n";
	}
	
	$target_path = "../api/uploads/";
	$target_path = $target_path . basename ( $_FILES ['file'] ['name'] );
	
	if (is_uploaded_file ( $_FILES ['uploadedfile'] ['tmp_name'] )) {
		// echo "There was an error uploading the file, please try again!";
		$status->message = "There was an error uploading the file, please try again!";
	}
	
	if (move_uploaded_file ( $_FILES ['file'] ['tmp_name'], $target_path )) {
		// echo "The file " . basename ( $_FILES ['file'] ['name'] ) . " has been uploaded";
		
		$status->message = "Image upload successful";
		$status->result_code = 1;
		
		$results->name = $_FILES ["file"] ["name"] . "";
		$results->url = $target_path . "";
		$results->size = ($_FILES ["file"] ["size"] / 1); // 1024 to convert in KB
	} else {
		// echo "There was an error Moving the file, please try again!";
		$status->message = "There was an error Moving the file, please try again!";
	}
	
	$response = array (
			'status' => $status,
			'results' => $results 
	);
	
	// echo (json_encode ( $status ));
	// echo (json_encode ( $results ));
	echo (json_encode ( $response ));
}

?>