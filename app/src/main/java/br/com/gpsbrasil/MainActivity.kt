package br.com.gpsbrasil

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { MaterialTheme { GpsHome() } }
 }
}

@Composable
fun GpsHome() {
 val context=LocalContext.current
 val scope= rememberCoroutineScope()
 val start= remember { mutableStateOf(LatLng(-21.274, -43.18)) }
 var current by remember { mutableStateOf<LatLng?>(null) }
 var destination by remember { mutableStateOf("") }
 var route by remember { mutableStateOf<List<LatLng>>(emptyList()) }
 var status by remember { mutableStateOf("Escolha sua origem ou permita a localização.") }
 var km by remember { mutableStateOf("") }
 var duration by remember { mutableStateOf("") }
 var loading by remember { mutableStateOf(false) }
 val camera= rememberCameraPositionState { position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(start.value, 12f) }
 val fused= remember { LocationServices.getFusedLocationProviderClient(context) }
 fun locate() {
  val granted=ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
  if (!granted) { status="Permita a localização para usar o GPS."; return }
  try {
   fused.lastLocation.addOnSuccessListener { loc ->
    if(loc!=null) { val pt=LatLng(loc.latitude,loc.longitude); current=pt; start.value=pt; scope.launch { camera.animate(CameraUpdateFactory.newLatLngZoom(pt,15f)) }; status="Localização encontrada." }
    else status="A localização ainda não está disponível. Ative o GPS e tente novamente."
   }.addOnFailureListener { status="Falha ao obter localização: ${it.message}" }
  } catch(e:SecurityException) { status="Permissão de localização necessária." }
 }
 val askPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
  if (granted.values.any { it }) locate() else status="Sem permissão. Você pode usar uma origem manual."
 }
 Column(Modifier.fillMaxSize()) {
  Box(Modifier.weight(1f)) {
   GoogleMap(
    modifier=Modifier.fillMaxSize(),
    cameraPositionState=camera,
    properties=MapProperties(isMyLocationEnabled=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED),
    uiSettings=MapUiSettings(zoomControlsEnabled=true, myLocationButtonEnabled=false)
   ) {
    Marker(state=MarkerState(position=start.value),title="Origem")
    if(route.isNotEmpty()) {
     Polyline(points=route,color=androidx.compose.ui.graphics.Color(0xFF1677FF),width=15f)
     Marker(state=MarkerState(position=route.last()),title="Destino")
    }
   }
  }
  Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
   Text("GPS Brasil",style=MaterialTheme.typography.titleLarge)
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    Button(onClick={ askPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)) }) { Text("Minha localização") }
    OutlinedButton(onClick={scope.launch { camera.animate(CameraUpdateFactory.newLatLngZoom(start.value,14f)) }}) { Text("Centralizar") }
   }
   OutlinedTextField(value=destination,onValueChange={destination=it},label={Text("Destino (endereço ou cidade)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
   Button(enabled=!loading && destination.isNotBlank(),modifier=Modifier.fillMaxWidth(),onClick={
    loading=true; status="Buscando destino e calculando rota..."
    scope.launch {
     try {
      val result=withContext(Dispatchers.IO) { compute(context,start.value,destination) }
      route=result.points;km="%.1f km".format(Locale.US,result.km);duration="${result.minutes} min";status="Rota calculada. Confira o caminho antes de dirigir."
      if(result.points.isNotEmpty()) camera.animate(CameraUpdateFactory.newLatLngZoom(result.points.first(),11f))
     } catch(e:Exception){status="Não foi possível traçar rota: ${e.message}"}
     finally{loading=false}
    }
   }) { Text(if(loading) "Calculando..." else "Traçar rota azul") }
   if(km.isNotEmpty()) Text("$km • $duration",style=MaterialTheme.typography.titleMedium)
   Text(status,style=MaterialTheme.typography.bodySmall)
   Text("Protótipo: internet obrigatória. Rotas de teste OSRM e geocodificação Android; sem trânsito ao vivo ou navegação curva a curva. Não use como único GPS ao dirigir.",style=MaterialTheme.typography.labelSmall)
  }
 }
}
data class RouteData(val points:List<LatLng>,val km:Double,val minutes:Int)

@Suppress("DEPRECATION")
private fun compute(context:android.content.Context,origin:LatLng,text:String):RouteData {
 val geocoder=Geocoder(context,Locale("pt","BR"))
 val dest=geocoder.getFromLocationName(text,1)?.firstOrNull() ?: error("Destino não encontrado. Tente endereço e cidade.")
 val url="https://router.project-osrm.org/route/v1/driving/${origin.longitude},${origin.latitude};${dest.longitude},${dest.latitude}?overview=full&geometries=geojson"
 val conn=(URL(url).openConnection() as HttpURLConnection).apply {
  connectTimeout=14000;readTimeout=18000;setRequestProperty("User-Agent","GPSBrasilPrototype/1.0")
 }
 try {
  if(conn.responseCode!=200) error("Serviço de rotas indisponível (HTTP ${conn.responseCode})")
  val data=JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
  val r=data.optJSONArray("routes")?.optJSONObject(0) ?: error("Nenhuma rota encontrada.")
  val positions=r.getJSONObject("geometry").getJSONArray("coordinates")
  val pts=(0 until positions.length()).map { i->val p=positions.getJSONArray(i);LatLng(p.getDouble(1),p.getDouble(0)) }
  return RouteData(pts,r.getDouble("distance")/1000,(r.getDouble("duration")/60).toInt())
 } finally {conn.disconnect()}
}
