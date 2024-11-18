package Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import modelBD.AlumnosBD;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

@Path("Alumnos")
public class Alumnos {

    private static SessionFactory sessionFactory;
    
    static {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @POST
    @Path("guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response guardar(@FormParam("nombre") String nombre,
                            @FormParam("apellido") String apellido,
                            @FormParam("matricula") int matricula,
                            @FormParam("fecha_nacimiento") String fecha_nacimiento,
                            @FormParam("correo") String correo,
                            @FormParam("telefono") int telefono,
                            @FormParam("direccion") String direccion,
                            @FormParam("activo") int activo) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"nombre es obligatorio.\"}")
                    .build();
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"apellido es obligatorio.\"}")
                    .build();
        }
        if (fecha_nacimiento == null || fecha_nacimiento.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"fecha_nacimiento es obligatorio.\"}")
                    .build();
        }
        if (correo == null || correo.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"correo es obligatorio.\"}")
                    .build();
        }
        if (telefono == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"telefono es obligatorio.\"}")
                    .build();
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"direccion es obligatoria.\"}")
                    .build();
        }

        
        if (activo == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"activo es obligatorio y debe ser mayor a 0.\"}")
                    .build();
        }

        
        Session session = null;
        Transaction transaction = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNacimientoParsed = dateFormat.parse(fecha_nacimiento);

            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            
            AlumnosBD alumno = new AlumnosBD();
            alumno.setNombre(nombre);
            alumno.setApellido(apellido);
            alumno.setMatricula(matricula);
            alumno.setFecha_nacimiento(fechaNacimientoParsed);
            alumno.setCorreo(correo);
            alumno.setTelefono(telefono);
            alumno.setDireccion(direccion);
            alumno.setActivo(activo == 1);  

            session.save(alumno);
            transaction.commit();

            return Response.ok("{\"message\":\"Alumno guardado exitosamente\"}")
                           .build();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"No se pudo guardar el alumno: " + e.getClass().getName() + " - " + e.getMessage() + "\"}")
                           .build();
        } finally {
            if (session != null) session.close();
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

        AlumnosBD alumno = session.get(AlumnosBD.class, id);

        if (alumno == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Alumno no encontrado con id: " + id + "\"}")
                           .build();
        }

        alumno.setActivo(false);
        session.update(alumno);
        transaction.commit();

        return Response.ok("{\"message\":\"Alumno desactivado exitosamente\"}")
                       .build();
    } catch (Exception e) {
        if (transaction != null) transaction.rollback();
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\":\"Error al desactivar el alumno: " + e.getMessage() + "\"}")
                       .build();
    } finally {
        if (session != null) session.close();
    }
}
}

