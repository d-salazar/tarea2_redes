import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/* Clase ThreadSocket de la tarea-1, con su propio archivo para mejor comprension. */
class PeticionHTTP extends Thread { 

	final private static String archivo_contactos = "usuarios.txt";
	final private static String archivo_mensajes = "mensajes.txt";
	private Socket socket;

	public PeticionHTTP(Socket insocket){
		this.socket = insocket;
		this.start();
	}
	
	@Override
	public void run(){
		
		try{
			BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter salida = new PrintWriter(socket.getOutputStream());
			
			String request = entrada.readLine();
			System.out.println("["+this.getName()+"][HTTP-HEADER]: "+request); 
	
			if( request != null ){
				String[] req = request.split("\\s+");
				String metodo = req[0];
				String archivo_requerido = req[1];
				String instruccion = "";
				int post_data_index = -1;
				while( (instruccion = entrada.readLine()) != null && (instruccion.length() != 0)){
					System.out.println("["+this.getName()+"][HTTP-HEADER]: "+instruccion);
					
					if( instruccion.indexOf("Content-Length:") > -1){
						post_data_index = new Integer(instruccion.substring(instruccion.indexOf("Content-Length:") + 16,instruccion.length())).intValue();
					}
				}

				if( metodo.equals("POST")){
					/* revisar elementos enviados a traves del metodo POST, y guardar variables en post_data */
					char[] charArray = new char[post_data_index];
					entrada.read(charArray,0,post_data_index);
					String post_data = new String(charArray);

					/* cerrar_conexion */
					if( post_data.equals("cerrar=true") ){ 
						ServidorWeb.servidor_http_status = false;
						this.interrupt();
						return;
					}
					/* agregar_contacto */
					if( archivo_requerido.equals("/lista_contacto.html")){
						System.out.println("agregar_contacto--");
						try {
							agregar_contacto(post_data);
						} catch (FileNotFoundException e) {
							System.err.println( e.getMessage() );e.printStackTrace();
						}
					}
					/* mensaje_nuevo */
					if( archivo_requerido.equals("mensajes.html") ){
						enviar_mensaje(post_data);
					}
				}
				else{		
					if( archivo_requerido.equals("/") ){
						archivo_requerido = "/index.html";
					}
					else if( archivo_requerido.equals("mensajes.html") ){
						recibir_mensaje();
					}
				}
				retorna_direccion(archivo_requerido,salida);
				salida.close();
				socket.close();
				return;
			}
		}catch ( Exception e){
			System.err.println( e.getMessage() );e.printStackTrace();
		}
		return;
	}		

	private static void retorna_direccion(String direccion_archivo, PrintWriter salida){
		if( direccion_archivo.startsWith("/") ){
			direccion_archivo = direccion_archivo.substring(1);
		}
		try{
			if( Files.exists(Paths.get(direccion_archivo)) ){
				System.out.println("- Se solicita: "+direccion_archivo);
				/* HEADERS */
				salida.println("HTTP/1.0 200 OK");
				salida.println("DATE: "+(new Date().toString()));
				salida.println("SERVER: localhost");
				salida.println("HOST: localhost:"+ServidorWeb.servidor_http.getLocalPort());
				salida.println("Content-Type: "+Files.probeContentType(Paths.get(direccion_archivo)));
				salida.println("");
				/* ARCHIVO */
				BufferedReader fl = new BufferedReader(new FileReader(direccion_archivo));
				String linea = fl.readLine();
				int contador_linea = 0;
				while (linea != null ){
					/* filtrar para mensajes.html y listar_contactos.html */
					if( direccion_archivo.toLowerCase().contains("lista_contacto.html".toLowerCase()) && contador_linea==41){
						listar_contactos(salida);
					}else if( direccion_archivo.toLowerCase().contains("mensajes.html".toLowerCase()) && contador_linea==40){
						listar_mensajes(salida);
					}
					salida.println(linea);
					linea = fl.readLine();
					contador_linea++;
				}
				fl.close();
				System.out.println("- fin envio: "+direccion_archivo);
			}else{
				salida.println("HTTP/1.0 404 NOT FOUND");
				salida.println("<h1>ERROR 404 - NOT FOUND</h1>");
				salida.println("<p>Oops! No se encuentra la direccion solicitada.</p>");
				salida.println("<p>No se ha podido presentar la direccion: "+direccion_archivo+"</p>");
			}
		}catch( Exception e){
			System.err.println( e.getMessage() );e.printStackTrace();
		}
		return;
	}
	
	private static void listar_contactos(PrintWriter salida) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(archivo_contactos));
		
		String linea = br.readLine();
		String[] data;
		while( linea!= null ){
			data = linea.split("\\|",3);
			
			salida.println("<tr>");
	        salida.println("	<td>"+data[0]+"</td>");
	        salida.println("	<td>"+data[1]+"</td>");
	        salida.println("	<td>"+data[2]+"</td>");
	        salida.println("	<td><button type=\"button\" class=\"btn btn-link\" data-toggle=\"modal\" data-target=\"#modal_nuevo_mensaje\">Envia un mensaje!</button></td>");
	        salida.println("</tr>");
	        
	        linea = br.readLine();
		}
		br.close();
	}
		
    private static void agregar_contacto(String data) throws FileNotFoundException{
    	try(PrintWriter archivo = new PrintWriter(new BufferedWriter(new FileWriter(archivo_contactos, true)))) {
    		String[] s = data.split("&",3);
    		archivo.println(s[0].split("=")[1].replaceAll("[\f\n\r\t\'\"\\\\]", " ")+"|"+s[1].split("=")[1]+"|"+s[2].split("=")[1]);
    		archivo.close();
    	}catch (IOException e) {
    	    System.err.println( e.getMessage() );e.printStackTrace();
    	}
    }
    
	private static void listar_mensajes(PrintWriter salida) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(archivo_mensajes));
		
		String linea = br.readLine();
		String[] data;
		while( linea!= null ){
			data = linea.split("\\|",3);
			
			salida.println("<tr>");
	        salida.println("	<td>"+data[0]+"</td>");
	        salida.println("	<td>"+data[1]+"</td>");
	        salida.println("	<td>"+data[2]+"</td>");
	        salida.println("</tr>");
	        
	        linea = br.readLine();
		}
		br.close();
	}
	
    private static void enviar_mensaje(String data) throws IOException{
    	try {
			data = URLEncoder.encode(data,"UTF-8");
			// Enviar a servidor TCP.
	    	Runtime.getRuntime().exec("ServidorTCP 2 "+data);	
		} catch (UnsupportedEncodingException e) {
			System.err.println( e.getMessage() );e.printStackTrace();
		}
    }
    
    private static void recibir_mensaje(){
    	// Revisar servidor TCP y obtener variable "data"
    	//String data = revisar_mensajes_servidor_TCP();
    	String data = "emisor=diego&destinatario=rodrigo&mensaje=wena%20cabro!%20como%20va%20la%20tarea%3F";
    	try {
			String[] parametros = URLDecoder.decode(data,"UTF-8").split("&",3);
			
			String emisor 		= parametros[0].split("=")[1];
	    	String destinatario = parametros[1].split("=")[1];
	    	String mensaje		= parametros[2].split("=")[1];
	    	
	    	System.out.println("emisor:"+emisor+destinatario+mensaje);
	    	
		} catch (UnsupportedEncodingException e) {
			System.err.println( e.getMessage() );e.printStackTrace();
		}    	
    }
}