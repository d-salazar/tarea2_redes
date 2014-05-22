import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.Date;

public class Servidor {
    
	private static final int PORT = 8080;
	private static ServerSocket server;
	public static boolean ServerStatus = false;
	
    public static void main(String[] args) throws IOException {
    	
    	
    	/*
    	try{
    		File htmlFile = new File("html/index.html");
    		Desktop.getDesktop().browse(htmlFile.toURI());
    		Desktop.getDesktop().open(htmlFile);
    	}catch(Exception e){
    		System.err.println( e.getMessage() );
    	}*/
    	
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(PORT));
            System.out.println("Servidor funcionando en el puerto: " + PORT);
            
            ServerStatus = true;
            while ( ServerStatus ) {
                new ThreadSocket(server.accept());
                System.out.println("ServerStatus: " + ServerStatus);
            }
        } catch (Exception e) {
        	System.err.println( e.getMessage());
        }
        finally{
        	if( !server.isClosed() ){
        		System.out.println("cerrando...");
        		server.close();
        	}
        }
    }
}


class ThreadSocket extends Thread {
	private static final String filename = "usuarios.txt";
	
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
                    postDataI = new Integer(
                            line.substring(
                                    line.indexOf("Content-Length:") + 16,
                                    line.length())).intValue();
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
            		Servidor.ServerStatus = false;
					this.interrupt();
					postDataI = 0;
					//return;
            	}
            	/* Guardar en el archivo */
            	else{
            		Writer(postData);
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
            out.println("</head>");
            out.println("<body>");
            out.println("	<div class=\"container\">");
            out.println("		<div class=\"page-header\">");
            out.println("			<h1>Bienvenido a Avioncito de Papel</h1>");
            out.println("		</div>");
            /* Cerrar conexion? */
            out.println("<div class=\"row\">");
            out.println("	<form action=\"\" method=\"post\" class=\"pull-right\">");
            out.println("		<button type=\"submit\" class=\"btn btn-danger\" name=\"cerrar\" value=\"true\" style=\"margin-top:-10px;margin-bottom:10px;\">Cerrar conexion</button>");
            out.println("	</form>");
            out.println("</div>");
            out.println("<div class=\"row\">");
            /* Metodo: POST */
            if( postDataI > 0){
            	out.println("<div class=\"alert alert-success alert-dismissable\">");
            	out.println("	<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-hidden=\"true\">&times;</button>");
            	out.println("	<strong>Guardado!</strong> Registro realizado correctamente!");
            	out.println("</div>");
                out.println("<table class=\"table table-striped\">");
                out.println("	<thead>");
                out.println("		<tr>");
                out.println("			<th>Nombre</th>");
                out.println("			<th>Puerto</th>");
                out.println("			<th>Direccion IP</th>");
                out.println("		</tr>");
                out.println("	</thead>");
                out.println("	<tbody>");
                Reader(out);
                out.println("	</tbody>");
                out.println("</table>");
                out.println("<form action=\"\" method=\"get\">");
                out.println("	<button type=\"submit\" class=\"btn btn-default\" value=\"agregar\">Agregar Contacto</button>");
                out.println("</form>");
                out.println("<!-- Necesarios para el funcionamiento de la alerta -->");
                out.println("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js\"></script>");
                out.println("<script src=\"http://netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js\"></script>");
                out.println("<script>");
                out.println("	$('.list-group-item').on('click',function(){");
                out.println("		$('.active').toggleClass('active');");
                out.println("		$(this).toggleClass('active');");
                out.println("	});");
                out.println("</script>");
            }
            /* Metodo GET */
            else{
            	out.println("<form action=\"\" method=\"post\" class=\"form-horizontal\">");
            	out.println("	<div class=\"form-group\">");
                out.println("		<label class=\"col-sm-2 control-label\">Nombre:</label>");
                out.println("		<div class=\"col-sm-10\">");
                out.println("			<input type=\"text\" class=\"form-control\" name=\"nombre\" placeholder=\"Nombre\" required>");
                out.println("		</div>");
                out.println("	</div>");
                out.println("	<div class=\"form-group\">");
                out.println("		<label class=\"col-sm-2 control-label\">Puerto:</label>");
                out.println("		<div class=\"col-sm-10\">");
                out.println("			<input type=\"text\" class=\"form-control\" name=\"puerto\" placeholder=\"Puerto\" required>");
                out.println("		</div>");
                out.println("	</div>");
                out.println("	<div class=\"form-group\">");
                out.println("		<label class=\"col-sm-2 control-label\">Direcci&oacute;n IP:</label>");
                out.println("		<div class=\"col-sm-10\">");
                out.println("			<input type=\"text\" class=\"form-control\" name=\"ip\" placeholder=\"IP\" required>");
                out.println("		</div>");
                out.println("	</div>");
                out.println("	<div class=\"form-group\">");
                out.println("		<div class=\"col-sm-offset-2 col-sm-10\">");
                out.println("			<button type=\"submit\" class=\"btn btn-default\" value=\"submit\">Guardar Contacto</button>");
                out.println("		</div>");
                out.println("	</div>");
                out.println("</form>");
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
    
    public static void Writer(String data) throws FileNotFoundException{
    	try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
    		String[] s = data.split("&",3);
    		out.println(s[0].split("=")[1].replaceAll("[\f\n\r\t\'\"\\\\]", " ")+"|"+s[1].split("=")[1]+"|"+s[2].split("=")[1]);
    		
    	}catch (IOException e) {
    	    System.err.println( e.getMessage() );
    	}
    }
    
    public static void Reader(PrintWriter out) throws IOException{
    	BufferedReader br = new BufferedReader(new FileReader(filename));
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