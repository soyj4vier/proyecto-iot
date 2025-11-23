<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "tu_clave"; // <--- CAMBIAR
$dbname = "movilesiot";

$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");
if ($conn->connect_error) { die(json_encode(["status" => "error", "mensaje" => "Error DB"])); }

$accion = isset($_REQUEST['accion']) ? strtoupper($_REQUEST['accion']) : '';
$id_admin = isset($_REQUEST['id_usuario']) ? intval($_REQUEST['id_usuario']) : 0;

if ($id_admin > 0) {
    $res_admin = $conn->query("SELECT id_departamento, rol FROM USUARIOS WHERE id_usuario = $id_admin LIMIT 1");
    if ($res_admin && $res_admin->num_rows > 0) {
        $row = $res_admin->fetch_assoc();
        if ($row['rol'] !== 'ADMINISTRADOR') die(json_encode(["status" => "error", "mensaje" => "Permiso denegado"]));
        $id_departamento = $row['id_departamento'];
    } else {
        die(json_encode(["status" => "error", "mensaje" => "Usuario no encontrado"]));
    }
} else {
    die(json_encode(["status" => "error", "mensaje" => "Admin no identificado"]));
}

if ($accion == 'LISTAR') {
    $sql = "SELECT id_usuario, nombre, email, rol, estado FROM USUARIOS WHERE id_departamento = $id_departamento AND rol = 'OPERADOR' ORDER BY id_usuario ASC";
    $result = $conn->query($sql);
    $usuarios = [];
    while($row = $result->fetch_assoc()) { $usuarios[] = $row; }
    echo json_encode(["status" => "success", "usuarios" => $usuarios]);

} elseif ($accion == 'AGREGAR') {
    $nombre = $conn->real_escape_string($_REQUEST['nombre']);
    $email = $conn->real_escape_string($_REQUEST['email']);
    $pass = $conn->real_escape_string($_REQUEST['password']);

    $check = $conn->query("SELECT id_usuario FROM USUARIOS WHERE email = '$email'");
    if ($check->num_rows > 0) die(json_encode(["status" => "error", "mensaje" => "Email duplicado"]));

    $sql = "INSERT INTO USUARIOS (id_departamento, nombre, email, password_hash, rol, es_administrador, estado)
            VALUES ($id_departamento, '$nombre', '$email', '$pass', 'OPERADOR', 0, 'ACTIVO')";
    if ($conn->query($sql)) echo json_encode(["status" => "success", "mensaje" => "Creado correctamente"]);
    else echo json_encode(["status" => "error", "mensaje" => "Error SQL"]);

} elseif ($accion == 'EDITAR') {
    $id_target = intval($_REQUEST['id_target']);
    $nombre = $conn->real_escape_string($_REQUEST['nombre']);
    $email = $conn->real_escape_string($_REQUEST['email']);
    $estado = $conn->real_escape_string($_REQUEST['estado']);
    $pass = $_REQUEST['password'];

    $sql_pass = (!empty($pass)) ? ", password_hash = '$pass'" : "";
    $sql = "UPDATE USUARIOS SET nombre = '$nombre', email = '$email', estado = '$estado' $sql_pass WHERE id_usuario = $id_target AND id_departamento = $id_departamento";

    if ($conn->query($sql)) echo json_encode(["status" => "success", "mensaje" => "Actualizado"]);
    else echo json_encode(["status" => "error", "mensaje" => "Error update"]);

} elseif ($accion == 'ELIMINAR') {
    $id_target = intval($_REQUEST['id_target']);
    $sql = "DELETE FROM USUARIOS WHERE id_usuario = $id_target AND id_departamento = $id_departamento AND rol = 'OPERADOR'";
    if ($conn->query($sql) && $conn->affected_rows > 0) echo json_encode(["status" => "success", "mensaje" => "Eliminado"]);
    else echo json_encode(["status" => "error", "mensaje" => "No se pudo eliminar"]);

} else {
    echo json_encode(["status" => "error", "mensaje" => "Acción inválida"]);
}
$conn->close();
?>