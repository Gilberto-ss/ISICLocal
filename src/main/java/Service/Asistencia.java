package Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import modelBD.AsistenciasBD;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;


@Path("Asistencia")
public class Asistencia {
    
     private static SessionFactory sessionFactory;

    static {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }
    
    @POST
   @Path("guardar")
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   @Produces(MediaType.APPLICATION_JSON)
   public Response guardar(@FormParam("id_alumno") int id_alumno,
           @FormParam("fecha") String fecha,
           @FormParam("asistencia") int asistencia,
           @FormParam("activo") int activo) {

      
      if (id_alumno <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'id_alumno' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }
      if (fecha == null || fecha.trim().isEmpty()) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'fecha' es obligatorio.\"}")
                 .build();
      }
      if (asistencia <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'asistencia' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }
      if (activo <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'activo' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }
    
      Session session = null;
      Transaction transaction = null;
      try {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         Date fechaParsed = dateFormat.parse(fecha);

         session = sessionFactory.openSession();
         transaction = session.beginTransaction();

         AsistenciasBD asistencias = new AsistenciasBD();
         asistencias.setId_alumno(id_alumno);
         asistencias.setFecha(fechaParsed);
         asistencias.setAsistencia(true);
         asistencias.setActivos(true);

         session.save(asistencias);
         transaction.commit();

         return Response.ok("{\"message\":\"la asistencia se guardado exitosamente\"}")
                 .build();
         
        } catch (Exception e) {
         if (transaction != null) {
            transaction.rollback();
         }
         e.printStackTrace();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("{\"error\":\"No se logro registrar la asistencia: " + e.getMessage() + "\"}")
                 .build();
      } finally {
         if (session != null) {
            session.close();
         }
      }
   }
   
   @GET
   @Path("eliminar")
   @Produces(MediaType.APPLICATION_JSON)
   public Response eliminar(@QueryParam("id") Long id) {
      Session session = null;
      Transaction transaction = null;

      if (id == null || id <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'id' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }

      try {
         session = sessionFactory.openSession();
         transaction = session.beginTransaction();

         AsistenciasBD asistencias = session.get(AsistenciasBD.class, id);

         if (asistencias == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Asistencia no encontrado con id: " + id + "\"}")
                    .build();
         }

         asistencias.setActivos(false); 
         session.update(asistencias);
         transaction.commit();

         return Response.ok("{\"message\":\"Asistencia desactivada exitosamente\"}")
                 .build();
      } catch (Exception e) {
         if (transaction != null) {
            transaction.rollback();
         }
         e.printStackTrace();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("{\"error\":\"Error al desactivar la asistencia: " + e.getMessage() + "\"}")
                 .build();
      } finally {
         if (session != null) {
            session.close();
         }
      }
   }
}

   


