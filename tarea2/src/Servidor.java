import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * registro.txt
 * De|Para|Mensaje
 * diego|rodrigo|trabaje señor asalariado.
 * rodrigo|diego|deja de webear.
 * etc...
 */
public class Servidor {
	private static int PORT = 8080;
	public static String FILENAME_REGISTRO = "registro.txt";
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		// TODO Auto-generated method stub
		if(Desktop.isDesktopSupported()){
			Desktop.getDesktop().browse(new URI("http://localhost:"+PORT));
        }
	}

	public static void RevisarNuevosMensajes(PrintWriter out) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(FILENAME_REGISTRO));
		try {
            String line = br.readLine();
            String[] data;
           
            while (line != null) {
                data = line.split("\\|",3);

	            out.println("	<tr>");
	            out.println("		<td>"+data[0]+"</td>");
	            out.println("		<td>"+data[1]+"</td>");
	            out.println("		<td>"+data[2]+"</td>");
	            out.println("	</tr>");

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
