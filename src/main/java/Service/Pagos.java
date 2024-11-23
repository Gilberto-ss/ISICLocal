package Service;

import java.util.Arrays;
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
    try {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    } catch (Throwable ex) {
        throw new ExceptionInInitializerError(ex);
    }
}

public static SessionFactory getSessionFactory() {
    return sessionFactory;
}


   static {
      sessionFactory = new Configuration().configure().buildSessionFactory();
   }

  @POST
@Path("guardar")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public Response guardar(@FormParam("matricula_alumno") int matricula_alumno,
                        @FormParam("monto") double monto,
                        @FormParam("metodo_pago") String metodo_pago) {
    if (matricula_alumno <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"El campo 'matricula_alumno' es obligatorio y debe ser mayor a cero.\"}")
                .build();
    }
    if (monto <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"El campo 'monto' es obligatorio y debe ser mayor a cero.\"}")
                .build();
    }
    if (metodo_pago == null || metodo_pago.isEmpty() || !isValidMetodoPago(metodo_pago)) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"El campo 'metodo_pago' es obligatorio y debe ser válido.\"}")
                .build();
    }

    Transaction transaction = null;

    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        transaction = session.beginTransaction();

        PagosBD pagos = new PagosBD();
        pagos.setMetodo_pago(metodo_pago);
        pagos.setMonto(monto);
        pagos.setMatricula(matricula_alumno);
        pagos.setFecha_pago(new Date());
        pagos.setActivo(true);

        session.save(pagos);
        transaction.commit();

        return Response.ok("{\"message\":\"El pago se ha guardado correctamente.\"}").build();
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"No se logró guardar el pago: " + e.getMessage() + "\"}")
                .build();
    }
}


   private boolean isValidMetodoPago(String metodo_pago) {
      List<String> metodosValidos = Arrays.asList("tarjeta", "efectivo", "transferencia");
      return metodosValidos.contains(metodo_pago);
   }

   @GET
   @Path("eliminar")
   @Produces(MediaType.APPLICATION_JSON)
   public Response eliminar(@QueryParam("matricula_alumno") int matricula_alumno
   ) {
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

         String hql = "FROM PagosBD WHERE matricula_alumno = :matricula";
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
