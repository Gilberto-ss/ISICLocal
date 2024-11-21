package Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import modelBD.PagosBD;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

@Path("Pagos")
public class Pagos {

   private static SessionFactory sessionFactory;

   static {
      sessionFactory = new Configuration().configure().buildSessionFactory();
   }

   @POST
   @Path("guardar")
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   @Produces(MediaType.APPLICATION_JSON)
   public Response guardar(@FormParam("id_alumno") int id_alumno,
           @FormParam("monto") double monto,
           @FormParam("metodo_pago") String metodo_pago,
           @FormParam("fecha_pago") String fecha_pago,
           @FormParam("activo") boolean activo,
           @FormParam("matricula") int matricula) {

      Session session = null;
      Transaction transaction = null;
      try {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         Date fechaPagoParsed = dateFormat.parse(fecha_pago);

         session = sessionFactory.openSession();
         transaction = session.beginTransaction();

         PagosBD pago = new PagosBD();
         pago.setId_alumno(id_alumno);
         pago.setMonto(monto);
         pago.setMetodo_pago(metodo_pago);
         pago.setFecha_pago(fechaPagoParsed);
         pago.setMatricula(matricula);
         pago.setActivo(true);

         session.save(pago);
         transaction.commit();

         return Response.ok("{\"message\":\"Pago guardado exitosamente\"}")
                 .build();

      } catch (Exception e) {
         if (transaction != null) {
            transaction.rollback();
         }
         e.printStackTrace();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("{\"error\":\"No se pudo guardar el pago: " + e.getMessage() + "\"}")
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
         List<PagosBD> pagosList = session.createQuery(hql)
                 .setParameter("matricula", matricula_alumno)
                 .list();

         if (pagosList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No se encontró asistencia con la matrícula: " + matricula_alumno + "\"}")
                    .build();
         }

         for (PagosBD pagos : pagosList) {
            pagos.setActivo(false);
            session.update(pagos);
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
}
