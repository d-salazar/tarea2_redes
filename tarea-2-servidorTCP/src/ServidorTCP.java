import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.URLDecoder;

public class ServidorTCP {
	/* SERVIDOR */
	public static ServerSocket servidor_tcp;
	private static boolean servidor_tcp_status = false;
	private final static int servidor_tcp_puerto = 6000;
	/* CONSTANTES */
	private final static String archivo_mensajes = "mensajes.txt";
	/* PROTOCOLO */
	/*
	 * ServidorTCP 1 -> Inicia servidor.
	 * ServidorTCP 0 -> Cerrar servidor.
	 * ServidorTCP 2 "asdf" -> Nuevo mensaje. 
	 * 
	 */
	public final static int P_FIN_CON = 0;
	public final static int P_INICIO_CON = 1;
	public final static int P_MENSAJE = 2;
		
	public static void main(String[] args) throws IOException {
		
		switch(Integer.parseInt(args[1])){
			case P_INICIO_CON:
				iniciar();
				break;
			case P_FIN_CON:
				cerrar();
				break;
			case P_MENSAJE:
				guardar_mensaje(args[2]);
				break;
			default:
				System.err.println("Para iniciar use: 'ServidorTCP 1'");
				System.err.println("Para cerrar use: 'ServidorTCP 0'");
				break;
		}
		return;
	}
	
	private static void iniciar() throws IOException{
		servidor_tcp = new ServerSocket(servidor_tcp_puerto);
		servidor_tcp.setReuseAddress(true);
		servidor_tcp_status = true;
	}
	
	private static void cerrar() throws IOException{
		if( !servidor_tcp.isClosed() ){
			System.out.println("Cerrando servidor TCP "+servidor_tcp.getLocalSocketAddress());
			servidor_tcp_status = false;
			servidor_tcp.close();
		}
	}
	
	private static void guardar_mensaje(String data){
		try(PrintWriter archivo = new PrintWriter(new BufferedWriter(new FileWriter(archivo_mensajes, true)))) {
			String[] parametros = URLDecoder.decode(data,"UTF-8").split("&",3);
			
			String emisor 		= parametros[0].split("=")[1];
	    	String destinatario = parametros[1].split("=")[1];
	    	String mensaje		= parametros[2].split("=")[1];
    		archivo.println(emisor+"|"+destinatario+"|"+mensaje);
    		archivo.close();
    	}catch (IOException e) {
    	    System.err.println( e.getMessage() );e.printStackTrace();
    	}
		enviar_mensaje(data);
		return;
	}
	private static void enviar_mensaje(String data){
		return;
	}
}
