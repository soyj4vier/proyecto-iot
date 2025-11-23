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
$id_usuario = isset($_REQUEST['id_usuario']) ? intval($_REQUEST['id_usuario']) : 0;

if ($id_usuario > 0) {
    $res_admin = $conn->query("SELECT id_departamento FROM USUARIOS WHERE id_usuario = $id_usuario LIMIT 1");
    if ($res_admin && $res_admin->num_rows > 0) {
        $id_departamento = $res_admin->fetch_assoc()['id_departamento'];
    } else {
        die(json_encode(["status" => "error", "mensaje" => "Usuario no encontrado"]));
    }
}

if ($accion == 'LISTAR') {
    $sql = "SELECT * FROM SENSORES WHERE id_departamento = $id_departamento ORDER BY id_sensor DESC";
    $result = $conn->query($sql);
    $sensores = [];
    while($row = $result->fetch_assoc()) { $sensores[] = $row; }
    echo json_encode(["status" => "success", "sensores" => $sensores]);

} elseif ($accion == 'AGREGAR') {
    $codigo = isset($_REQUEST['codigo']) ? strtoupper(str_replace(' ', '', $_REQUEST['codigo'])) : '';
    $tipo = isset($_REQUEST['tipo']) ? $_REQUEST['tipo'] : 'TARJETA';
    if(empty($codigo)) die(json_encode(["status" => "error", "mensaje" => "Código vacío"]));
    
    $check = $conn->query("SELECT id_sensor FROM SENSORES WHERE codigo_sensor = '$codigo'");
    if($check->num_rows > 0) die(json_encode(["status" => "error", "mensaje" => "Sensor ya existe"]));

    $fecha = date("Y-m-d H:i:s");
    $sql = "INSERT INTO SENSORES (codigo_sensor, id_departamento, id_usuario_asociado, tipo, estado, fecha_alta)
            VALUES ('$codigo', $id_departamento, $id_usuario, '$tipo', 'ACTIVO', '$fecha')";
    if($conn->query($sql)) echo json_encode(["status" => "success", "mensaje" => "Registrado"]);
    else echo json_encode(["status" => "error", "mensaje" => "Error SQL"]);

} elseif ($accion == 'EDITAR') {
    $id_sensor = intval($_REQUEST['id_sensor']);
    $codigo = strtoupper(str_replace(' ', '', $_REQUEST['codigo']));
    $tipo = $_REQUEST['tipo'];
    $estado = $_REQUEST['estado'];
    $sql = "UPDATE SENSORES SET codigo_sensor = '$codigo', tipo = '$tipo', estado = '$estado' 
            WHERE id_sensor = $id_sensor AND id_departamento = $id_departamento";
    if($conn->query($sql)) echo json_encode(["status" => "success", "mensaje" => "Actualizado"]);
    else echo json_encode(["status" => "error", "mensaje" => "Error al actualizar"]);

} elseif ($accion == 'ELIMINAR') {
    $id_sensor = intval($_REQUEST['id_sensor']);
    $sql = "DELETE FROM SENSORES WHERE id_sensor = $id_sensor AND id_departamento = $id_departamento";
    if($conn->query($sql)) echo json_encode(["status" => "success", "mensaje" => "Eliminado"]);
    else echo json_encode(["status" => "error", "mensaje" => "No se pudo eliminar"]);

} elseif ($accion == 'OBTENER_ULTIMO') {
    if (file_exists("ultimo_uid.txt")) {
        echo json_encode(["status" => "success", "mensaje" => file_get_contents("ultimo_uid.txt")]);
    } else {
        echo json_encode(["status" => "error", "mensaje" => "Sin lecturas"]);
    }
} else {
    echo json_encode(["status" => "error", "mensaje" => "Acción inválida"]);
}
$conn->close();
?>