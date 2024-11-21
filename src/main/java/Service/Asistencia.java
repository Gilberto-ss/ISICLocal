package Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
           @FormParam("activo") int activo,
           @FormParam("matricula_alumno") int matricula_alumno){

      
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
      if (asistencia != 0 && activo != 1) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'asistencia' debe ser 0 (falso) o 1 (verdadero).\"}")
                 .build();
      }
      if (activo != 0 && activo != 1) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'activo' debe ser 0 (falso) o 1 (verdadero).\"}")
                 .build();
      }
     if (matricula_alumno <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'matricula_alumno' es obligatorio y debe ser mayor a cero.\"}")
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
         asistencias.setMatricula_alumno(matricula_alumno);

         session.save(asistencias);
         transaction.commit();

         return Response.ok("{\"message\":\"\"La asistencia se ha guardado exitosamente.\"}")
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
   public Response eliminar(@QueryParam("matricula_alumno") int matricula_alumno) {
      Session session = null;
      Transaction transaction = null;

      if (matricula_alumno <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'matricula_alumno' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }

      try {
         session = sessionFactory.openSession();
         transaction = session.beginTransaction();

        String hql = "FROM AsistenciasBD WHERE matricula_alumno = :matricula";
        List<AsistenciasBD> asistenciasList = session.createQuery(hql)
        .setParameter("matricula", matricula_alumno)
        .list();

        if (asistenciasList.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\":\"No se encontró asistencia con la matrícula: " + matricula_alumno + "\"}")
            .build();
        }

        
        for (AsistenciasBD asistencia : asistenciasList) {
        asistencia.setActivos(false); 
        session.update(asistencia); 
        }
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
   
         
    @GET
    @Path("/historialAsistencia/{id_alumno}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response HistorialAsistencia(@PathParam("id_alumno") int id_alumno) {
    Session session = null;
   
    if (id_alumno == 0 || id_alumno <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"El campo 'id' es obligatorio y debe ser mayor a cero.\"}")
                .build();
    }

    try {
        session = sessionFactory.openSession();
         List<AsistenciasBD> asitencias = session.createQuery("FROM AsistenciasBD WHERE id_alumno = :id_alumno", AsistenciasBD.class)
                 .setParameter("id_alumno", id_alumno)
                 .list();
         
          if (asitencias.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"No se encontraron registro para el alumno con id: " + id_alumno + "\"}")
                    .build();
         }

         return Response.ok(asitencias) 
                 .build();
      } catch (Exception e) {
         e.printStackTrace();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("{\"error\":\"Error al obtener el historial de asistencias: " + e.getMessage() + "\"}")
                 .build();
      } finally {
         if (session != null) {
            session.close();
         }
      }
   }
   }
      

   


