import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorTCP {
	/* SERVIDOR */
	public static ServerSocket servidor_tcp;
	private static boolean servidor_tcp_status = false;
	private final static int servidor_tcp_puerto = 6000;
	/* CONSTANTES */
	private final static String archivo_mensajes = "mensajes.txt";
		
	public static void main(String[] args) throws IOException {
		servidor_tcp = new ServerSocket(servidor_tcp_puerto);
		servidor_tcp.setReuseAddress(true);
		servidor_tcp_status = true;
		System.out.println("SERVIDOR TCP");
		while( servidor_tcp_status ){
			Socket cliente = servidor_tcp.accept();
			DataInputStream inCliente = new DataInputStream(cliente.getInputStream());
			DataOutputStream outCliente = new DataOutputStream(cliente.getOutputStream());
			
			System.out.println("CONEXION ESTABLECIDA CON servidor_http");
			//if( inCliente.available() > 0){
			String mensaje_recibido=inCliente.readUTF();
			if( mensaje_recibido != null ){
				System.out.println("Mensaje nuevo: "+mensaje_recibido);
				
				/* request en servidor_http => 'POST /mensajes.html' */
				// inCliente = "G|emisor|destinatario|mensaje";
				if( mensaje_recibido.contains("G|" )){
					guardar_mensaje(outCliente,mensaje_recibido);
				}
				/* request en servidor_http => 'GET /mensajes.html' */
				// inCliente = "L";
				else if( mensaje_recibido.equals("L") ){
					enviar_mensaje(outCliente);
				}else{
					System.err.println("No se ha encontrado accion para: "+inCliente.readUTF());
				}
			}
			
			inCliente.close();
			outCliente.close();
			cliente.close();
		}
		
		if( !servidor_tcp.isClosed() ){
			System.out.println("Cerrando servidor HTTP"+servidor_tcp.getLocalSocketAddress());
			servidor_tcp.close();
		}
	}
	
	private static void guardar_mensaje(DataOutputStream outCliente, String data) throws IOException{
		try(PrintWriter archivo = new PrintWriter(new BufferedWriter(new FileWriter(archivo_mensajes, true)))) {
			String parametros[] = data.split("\\|",4);
			
			String emisor 		= parametros[1];
	    	String destinatario = parametros[2];
	    	String mensaje		= parametros[3];
	    	
	    	if( !mensaje.equals("") ){
	    		archivo.println(emisor+"|"+destinatario+"|"+mensaje);
	    	}
    		archivo.close();
    	}catch (IOException e) {
    	    System.err.println( e.getMessage() );
    	    e.printStackTrace();
    	}
		enviar_mensaje(outCliente);
		return;
	}
	private static void enviar_mensaje(DataOutputStream outCliente) throws IOException{
		try{
			BufferedReader br = new BufferedReader(new FileReader(archivo_mensajes));
			
			if(br!=null){
				String linea = br.readLine();
				while( linea!= null ){
			    	outCliente.writeUTF(linea);
			        linea = br.readLine();
				}
			}
			br.close();
		}catch(Exception e){
			outCliente.writeUTF("NOTHING");
		}
	}
}
