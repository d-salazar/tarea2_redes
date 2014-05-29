import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Date;

public class Servidor2 {
	private static final int PORT = 8080;
	private static ServerSocket SERVER;
	public static boolean SERVER_STATUS = false;
	
    public static void main(String[] args) throws IOException {
        try {
            SERVER = new ServerSocket();
            SERVER.setReuseAddress(true);
            SERVER.bind(new InetSocketAddress(PORT));
            System.out.println("Servidor funcionando en el puerto: " + PORT);
            
            if(Desktop.isDesktopSupported())
            {
              Desktop.getDesktop().browse(new URI("http://localhost:"+PORT));
            }
            
            SERVER_STATUS = true;
            while ( SERVER_STATUS ) {
                new ThreadSocket(SERVER.accept());
                System.out.println("ServerStatus: " + SERVER_STATUS);
            }
        } catch (Exception e) {
        	System.err.println( e.getMessage());
        }
        finally{
        	if( !SERVER.isClosed() ){
        		System.out.println("Cerrando...");
        		SERVER.close();
        	}
        }
    }
}


class ThreadSocket extends Thread {
	private static final String FILENAME = "registro.txt";
    private Socket insocket;
    ThreadSocket(Socket insocket) {
        this.insocket = insocket;
        this.start();
    }
    @Override
    public void run() {
        try {
            InputStream is = insocket.getInputStream();
            PrintWriter out = new PrintWriter(insocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            
            String line;
            line = in.readLine();
            System.out.println("HTTP-HEADER: " + line);
            line = "";
            
            /* Checkea si se ha usado el metodo POST, comprobando si existe data enviada */
            int postDataI = -1;
            while ((line = in.readLine()) != null && (line.length() != 0)) {
                System.out.println("HTTP-HEADER: " + line);
                if (line.indexOf("Content-Length:") > -1) {
                    postDataI = new Integer( line.substring(line.indexOf("Content-Length:") + 16, line.length())).intValue();
                }
            }
            
            
            String postData = "";
            /* Existe informacion  POST -> Guardar en Archivo  */
            if (postDataI > 0) {
                char[] charArray = new char[postDataI];
                in.read(charArray, 0, postDataI);
                postData = new String(charArray);
                
                /* Cerrar conexion/puerto */
            	if( postData.equals("cerrar=true") ){
            		Servidor2.ServerStatus = false;
					this.interrupt();
					postDataI = 0;
					//return;
            	}
            	/* Guardar en el archivo */
            	else{
            		username = postData.split("=")[1];
            	}
            }
            /* HEADERS */
            out.println("HTTP/1.0 200 OK");
            out.println("Date:" + (new Date()).toString());
            out.println("Server: localhost");
            out.println("Content-Type: text/html; charset=utf-8");
            
            /* Salto de linea señala fin de los headers, si no tira error :S */
            out.println("");
            /* HTML  */
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("	<title>Avioncito de Papel</title>");
            out.println("	<meta charset=\"utf-8\">");
            out.println("	<link type=\"image/x-icon\" rel=\"shortcut icon\" href=\"favicon.ico\">");
            out.println("	<link rel=\"stylesheet\" href=\"http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css\">");
            out.println("	<style>body{background-color: #eee;}</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("	<div class=\"container\">");
            /* Cerrar conexion? */
            out.println("<div class=\"row\">");
            out.println("	<form action=\"\" method=\"post\" class=\"pull-right\">");
            out.println("		<button type=\"submit\" class=\"btn btn-danger\" name=\"cerrar\" value=\"true\">Cerrar conexion</button>");
            out.println("	</form>");
            out.println("</div>");
            out.println("<div class=\"row\">");
            /* Metodo: POST */
            if( postDataI > 0){
            	out.println("<style>#historial{min-height: 300px;overflow-y:scroll;}textarea{resize:none;}</style>");
            	out.println("<h1>Chat en Localhost:8080</h1>");
            	out.println("<div id=\"historial\" class=\"form-control form-group\"></div>");
            	out.println("<div class=\"row\">");
            	out.println("	<div class=\"col-xs-8\">");
            	out.println("		<textarea id=\"mensaje\" class=\"form-control\" rows=\"4\" placeholder=\"Ingresa tu mensaje aqu&iacute;\"></textarea>");
            	out.println("	</div>");
            	out.println("	<div class=\"col-xs-4\">");
            	out.println("		<div class=\"form-group\">");
            	out.println("			<button type=\"button\" id=\"enviar\" class=\"btn btn-primary btn-block\">Enviar</button>");
            	out.println("		</div>");
            	out.println("		<button type=\"button\" id=\"limpiar\" class=\"btn btn-default\" title=\"Limpiar historial del chat.\"><span class=\"glyphicon glyphicon-trash\"></span></button>");
            	out.println("		<button type=\"button\" id=\"limpiar\" class=\"btn btn-default\" title=\"Salir.\"><span class=\"glyphicon glyphicon-log-out\"></span></button>");
            	out.println("	</div>");
            	out.println("</div>");
            	out.println("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js\"></script>");
            	out.println("<script src=\"http://netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js\"></script>");
            	out.println("<script type=\"text/javascript\">");
            	out.println("	$('#enviar').on('click',function(){");
            	out.println("		var msg = $('#mensaje').val();");
            	out.println("		console.log('msg: '+msg);");
            	out.println("		$('#historial').append('<div class=\"text-muted\"><strong>"+username+"</strong></div>');");
            	out.println("		$('#historial').append('<div class=\"text-left\"><pre><em>'+msg+'</em></pre></div>');");
            	out.println("		$('#historial').prop({ scrollTop: $('#historial').prop('scrollHeight')});");
            	out.println("		$('#mensaje').val('');");
            	out.println("	});");
            	out.println("	$('#limpiar').on('click',function(){");
            	out.println("		$('#historial').html('');");
            	out.println("	});");
            	out.println("</script>");
            }
            /* Metodo GET */
            else{
            	out.println("<style>#conectar-form{min-width: 300px;}</style>");
            	out.println("<div class=\"row\">");
            	out.println("	<div id=\"conectar-form\" class=\"col-xs-4 col-xs-offset-4 col-md-3 col-md-offset-4\">");
            	out.println("		<div class=\"form-group\">");
            	out.println("			<img src=\"assets/paperplane.gif\" alt=\"logo\" class=\"img-thumbnail\">");
            	out.println("		</div>");
            	out.println("		<form action=\"\" method=\"post\">");
            	out.println("			<div class=\"form-group\">");
            	out.println("				<input type=\"text\" class=\"form-control\" name=\"username\" placeholder=\"Username\">");
            	out.println("			</div>");
            	out.println("			<div class=\"form-group\">");
            	out.println("				<button type=\"submit\" class=\"btn btn-primary btn-block\" value=\"conectar\">Conectar</button>");
            	out.println("			</div>");
            	out.println("			</form>");
            	out.println("	</div>");
            	out.println("</div>");
            }
            out.println("</div>");
            out.println("</div>"); // cierre container
            out.println("</body>");
            out.println("</html>");
            
            out.close();
            insocket.close();
        } catch (IOException e) {
        	System.err.println( e.getMessage() );
        }
    }
    /*
    public static void Writer(String data) throws FileNotFoundException{
    	try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
    		String[] s = data.split("&",3);
    		out.println(s[0].split("=")[1].replaceAll("[\f\n\r\t\'\"\\\\]", " ")+"|"+s[1].split("=")[1]+"|"+s[2].split("=")[1]);
    		
    	}catch (IOException e) {
    	    System.err.println( e.getMessage() );
    	}
    }
    }
	*/
}