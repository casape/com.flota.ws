package servicios;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author 
 * Instancia los objetos que ofrecen los servicios web que queremos que despliegue el servidor (p.e. Tomcat)
 * Se almacenan en un conjunto que es devuelto por el método gestSingletons
 *
 */

@ApplicationPath("/servicios") // Innecesario si se incluye como elemento servlet-mapping en el fichero web.xml
public class AplicacionJuego extends Application {
   private Set<Object> singletons = new HashSet<Object>();

   public AplicacionJuego() {
      singletons.add(new RecursoPartida());
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}
