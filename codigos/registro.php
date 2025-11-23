<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "tu_clave"; // <--- CAMBIAR
$dbname = "movilesiot";

$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");

if ($conn->connect_error) { die(json_encode(["status" => "error", "mensaje" => "Error DB"])); }

$num_depto = isset($_REQUEST['numero_depto']) ? $conn->real_escape_string($_REQUEST['numero_depto']) : '';
$torre = isset($_REQUEST['torre']) ? $conn->real_escape_string($_REQUEST['torre']) : '';
$nombre = isset($_REQUEST['nombre']) ? $conn->real_escape_string($_REQUEST['nombre']) : '';
$email = isset($_REQUEST['email']) ? $conn->real_escape_string($_REQUEST['email']) : '';
$pass = isset($_REQUEST['password']) ? $conn->real_escape_string($_REQUEST['password']) : '';

if (empty($num_depto) || empty($nombre) || empty($email) || empty($pass)) {
    die(json_encode(["status" => "error", "mensaje" => "Faltan datos obligatorios"]));
}

$checkEmail = $conn->query("SELECT id_usuario FROM USUARIOS WHERE email = '$email'");
if ($checkEmail->num_rows > 0) {
    die(json_encode(["status" => "error", "mensaje" => "El email ya está registrado"]));
}

$sql_depto = "INSERT INTO DEPARTAMENTOS (numero, torre, otros_datos) VALUES ('$num_depto', '$torre', 'Registro desde App')";

if ($conn->query($sql_depto) === TRUE) {
    $id_nuevo_depto = $conn->insert_id;
    $sql_user = "INSERT INTO USUARIOS (id_departamento, nombre, email, password_hash, rol, es_administrador, estado)
                 VALUES ($id_nuevo_depto, '$nombre', '$email', '$pass', 'ADMINISTRADOR', 1, 'ACTIVO')";

    if ($conn->query($sql_user) === TRUE) {
        echo json_encode(["status" => "success", "mensaje" => "Departamento y Admin registrados con éxito"]);
    } else {
        $conn->query("DELETE FROM DEPARTAMENTOS WHERE id_departamento = $id_nuevo_depto");
        echo json_encode(["status" => "error", "mensaje" => "Error al crear usuario: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "mensaje" => "Error al registrar depto: " . $conn->error]);
}
$conn->close();
?>