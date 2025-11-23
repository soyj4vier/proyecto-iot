<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "tu_clave"; // <--- CAMBIAR
$dbname = "movilesiot";

$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "mensaje" => "Error DB"]));
}

if (isset($_GET['email']) && isset($_GET['password'])) {
    $email = $conn->real_escape_string($_GET['email']);
    $pass_input = $_GET['password'];

    $sql = "SELECT id_usuario, nombre, password_hash, rol, estado FROM USUARIOS WHERE email = '$email' LIMIT 1";
    $result = $conn->query($sql);

    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();

        // Verificación de contraseña (Texto plano para este proyecto)
        if ($pass_input == $row['password_hash']) {
            if ($row['estado'] == 'ACTIVO') {
                echo json_encode([
                    "status" => "success",
                    "mensaje" => "Login exitoso",
                    "id_usuario" => $row['id_usuario'],
                    "nombre" => $row['nombre'],
                    "rol" => $row['rol']
                ]);
            } else {
                echo json_encode(["status" => "error", "mensaje" => "Usuario inactivo o bloqueado"]);
            }
        } else {
            echo json_encode(["status" => "error", "mensaje" => "Contraseña incorrecta"]);
        }
    } else {
        echo json_encode(["status" => "error", "mensaje" => "Usuario no encontrado"]);
    }
} else {
    echo json_encode(["status" => "error", "mensaje" => "Faltan datos"]);
}
$conn->close();
?>