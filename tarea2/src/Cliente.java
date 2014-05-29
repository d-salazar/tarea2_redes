import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


public class Cliente {
	private static final String FILENAME_USUARIOS = "usuarios.txt";
	private static String USERNAME;
	private static String DIRRECION_IP;
	private static int PUERTO;
	
	public static void EnviarMensajeProtocolo(String msg){
		
	}
	
	public static void ListarUsuarios(PrintWriter out) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(FILENAME_USUARIOS));
		try {
            String line = br.readLine();
            String[] data;
           
            while (line != null) {
                data = line.split("\\|",3);
                if( !data[0].equals(USERNAME) ){
		            out.println("	<tr>");
		            out.println("		<td>"+data[0]+"</td>");
		            out.println("		<td>"+data[1]+"</td>");
		            out.println("		<td>"+data[2]+"</td>");
		            out.println("	</tr>");
                }
                line = br.readLine();
            }
           
        }
        catch( Exception e ){
        	System.err.println( e.getMessage() );
        }
        finally {
            br.close();
        }
	}
}
