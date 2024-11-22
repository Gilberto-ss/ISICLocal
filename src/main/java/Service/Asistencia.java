package Service;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public Response guardar(@FormParam("matricula_alumno") int matricula_alumno) {
  
    if (matricula_alumno <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"El campo 'matricula_alumno' es obligatorio y debe ser mayor a cero.\"}")
                .build();
    }

    Session session = null;
    Transaction transaction = null;

    try {
    
        Date fechaActual = new Date();

        session = sessionFactory.openSession();
        transaction = session.beginTransaction();

        AsistenciasBD asistencias = new AsistenciasBD();
        asistencias.setMatricula_alumno(matricula_alumno);
        asistencias.setFecha(fechaActual); 
        asistencias.setAsistencia(1);     
        asistencias.setActivos(true);     

        session.save(asistencias);
        transaction.commit();

        return Response.ok("{\"message\":\"La asistencia se ha guardado exitosamente.\"}")
                .build();

    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"No se logró registrar la asistencia: " + e.getMessage() + "\"}")
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
@Path("historialAsistencia")
@Produces(MediaType.APPLICATION_JSON)
public Response HistorialAsistencia(@QueryParam("matricula_alumno") int matricula_alumno) {
    Session session = null;

    // Validación de entrada
    if (matricula_alumno <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"El campo 'matricula_alumno' es obligatorio y debe ser mayor a cero.\"}")
                .build();
    }

    try {
        session = sessionFactory.openSession();

        // Consulta HQL para obtener las asistencias
        String hql = "FROM AsistenciasBD WHERE matricula_alumno = :matricula_alumno";
        List<AsistenciasBD> asistencias = session.createQuery(hql, AsistenciasBD.class)
                .setParameter("matricula_alumno", matricula_alumno)
                .getResultList();

        // Verificar si no se encontraron asistencias
        if (asistencias.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"No se encontraron registros para el alumno con matrícula: " + matricula_alumno + "\"}")
                    .build();
        }

        // Convertir las asistencias a formato JSON
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (AsistenciasBD asistencia : asistencias) {
            Map<String, Object> item = new HashMap<>();
            item.put("matricula_alumno", asistencia.getMatricula_alumno());
            item.put("fecha", asistencia.getFecha());
            item.put("asistencia", asistencia.getAsistencia());
            item.put("activo", asistencia.isActivos());
            resultado.add(item);
        }

        // Serializar las asistencias a JSON usando Gson
        Gson gson = new Gson();
        String asistenciasJson = gson.toJson(resultado);

        // Retornar la respuesta con el JSON generado
        return Response.ok(asistenciasJson).build();

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
      

   


