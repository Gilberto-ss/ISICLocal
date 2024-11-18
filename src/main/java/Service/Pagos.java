package Service;

import java.text.SimpleDateFormat;
import java.util.Date;
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
           @FormParam("fecha_pago") String fecha_pago) {

      // Validaciones de entrada
      if (id_alumno <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'id_alumno' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }
      if (monto <= 0) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'monto' es obligatorio y debe ser mayor a cero.\"}")
                 .build();
      }
      if (metodo_pago == null || metodo_pago.trim().isEmpty()) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'metodo_pago' es obligatorio.\"}")
                 .build();
      }
      if (fecha_pago == null || fecha_pago.trim().isEmpty()) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity("{\"error\":\"El campo 'fecha_pago' es obligatorio.\"}")
                 .build();
      }

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
         pago.setActivos(true);  // Por defecto, el pago está activo

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

         PagosBD pago = session.get(PagosBD.class, id);

         if (pago == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Pago no encontrado con id: " + id + "\"}")
                    .build();
         }

         pago.setActivos(false); // Desactivar el pago en lugar de eliminarlo
         session.update(pago);
         transaction.commit();

         return Response.ok("{\"message\":\"Pago desactivado exitosamente\"}")
                 .build();
      } catch (Exception e) {
         if (transaction != null) {
            transaction.rollback();
         }
         e.printStackTrace();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("{\"error\":\"Error al desactivar el pago: " + e.getMessage() + "\"}")
                 .build();
      } finally {
         if (session != null) {
            session.close();
         }
      }
   }
}
