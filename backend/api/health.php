<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

echo json_encode([
    "success" => true,
    "status" => "online",
    "message" => "Backend Online"
]);
?>
