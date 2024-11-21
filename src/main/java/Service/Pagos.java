package Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import modelBD.PagosBD;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

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
}